package com.ape.backuprestore;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.IBinder;

import com.ape.backuprestore.modules.Composer;
import com.ape.backuprestore.utils.Constants;
import com.ape.backuprestore.utils.Logger;
import com.ape.backuprestore.utils.ModuleType;
import com.ape.backuprestore.utils.NotifyManager;
import com.ape.backuprestore.utils.StorageUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * @author way
 */
public class RestoreService extends Service implements ProgressReporter, RestoreEngine.OnRestoreDoneListner {
    private static final String TAG = "RestoreService";
    private HashMap<Integer, ArrayList<String>> mParasMap = new HashMap<>();
    private RestoreBinder mBinder = new RestoreBinder();
    private int mState;
    private RestoreEngine mRestoreEngine;
    private ArrayList<ResultDialog.ResultEntity> mResultList;
    private RestoreProgress mCurrentProgress = new RestoreProgress();
    private OnRestoreStatusListener mRestoreStatusListener;
    private ArrayList<ResultDialog.ResultEntity> mAppResultList;
    private String mFileName = "";

    @Override
    public IBinder onBind(Intent intent) {
        Logger.i(TAG, "onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        Logger.i(TAG, "onUnbind");
        // If SD card removed or full, kill process
        StorageUtils.killProcessIfNecessary();
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        moveToState(Constants.State.INIT);
        Logger.i(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Logger.i(TAG, "onStartCommand");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        Logger.i(TAG, "onDestroy");
        if (mRestoreEngine != null && mRestoreEngine.isRunning()) {
            mRestoreEngine.setOnRestoreEndListner(null);
            mRestoreEngine.cancel();
        }
    }

    /**
     * @param state state
     */
    public void moveToState(int state) {
        synchronized (this) {
            Logger.d(TAG, "Move from " + mState + " to " + state);
            mState = state;
        }
    }

    private int getRestoreState() {
        synchronized (this) {
            return mState;
        }
    }

    /**
     * @param iComposer iComposer
     */
    public void onStart(Composer iComposer) {
        mCurrentProgress.mType = iComposer.getModuleType();
        mCurrentProgress.mMax = iComposer.getCount();
        mCurrentProgress.mCurNum = 0;
        if (mRestoreStatusListener != null) {
            mRestoreStatusListener.onComposerChanged(mCurrentProgress.mType,
                    mCurrentProgress.mMax);
        }

        if (mCurrentProgress.mMax != 0) {
            NotifyManager.getInstance(RestoreService.this).setMaxPercent(mCurrentProgress.mMax);
        }
    }

    /**
     * @param composer composer
     * @param result   result
     */
    public void onOneFinished(Composer composer, boolean result) {

        mCurrentProgress.mCurNum++;
        if (composer.getModuleType() == ModuleType.TYPE_APP) {
            if (mAppResultList == null) {
                mAppResultList = new ArrayList<>();
            }
            ResultDialog.ResultEntity entity = new ResultDialog.ResultEntity(ModuleType.TYPE_APP,
                    result ? ResultDialog.ResultEntity.SUCCESS : ResultDialog.ResultEntity.FAIL);
            if (mParasMap.get(ModuleType.TYPE_APP) != null
                    && mParasMap.get(ModuleType.TYPE_APP).size() > mCurrentProgress.mCurNum - 1) {
                entity.setKey(mParasMap.get(ModuleType.TYPE_APP).get(mCurrentProgress.mCurNum - 1));
                mAppResultList.add(entity);
            }
        }

        if (getRestoreState() != Constants.State.RUNNING) {
            Logger.w(TAG, "onOneFinished: State is not Running " + getRestoreState());
            return;
        }

        if (mRestoreStatusListener != null) {
            mRestoreStatusListener.onProgressChanged(composer, mCurrentProgress.mCurNum);
        }

        if (mCurrentProgress.mMax != 0) {
            NotifyManager
                    .getInstance(RestoreService.this)
                    .showRestoreNotification(
                            ModuleType.getModuleStringFromType(this, composer.getModuleType()),
                            mFileName,
                            composer.getModuleType(),
                            mCurrentProgress.mCurNum);
        }
    }

    /**
     * @param composer composer
     * @param result   result
     */
    public void onEnd(Composer composer, boolean result) {
        if (mResultList == null) {
            mResultList = new ArrayList<>();
        }
        ResultDialog.ResultEntity item = new ResultDialog.ResultEntity(
                composer.getModuleType(),
                result ? ResultDialog.ResultEntity.SUCCESS : ResultDialog.ResultEntity.FAIL);
        mResultList.add(item);
    }

    /**
     * @param e exception
     */
    public void onErr(IOException e) {
        if (mRestoreStatusListener != null) {
            mRestoreStatusListener.onRestoreErr(e);
        }
    }

    /**
     * @param bSuccess if restore success
     */
    public void onFinishRestore(boolean bSuccess) {
        moveToState(Constants.State.FINISH);
        if (StorageUtils.getBackupPath() == null) {
            moveToState(Constants.State.INIT);
        }
        if (mRestoreStatusListener != null) {
            mRestoreStatusListener.onRestoreEnd(bSuccess, mResultList);
        }
        boolean succeeded = true;
        if (mResultList != null) {
            for (ResultDialog.ResultEntity entity : mResultList) {
                if (entity.getResult() == ResultDialog.ResultEntity.FAIL) {
                    succeeded = false;
                    break;
                }
            }
        }
        if (succeeded) {
            NotifyManager.getInstance(this)
                    .showFinishNotification(NotifyManager.NOTIFY_RESTORING, true);
        } else {
            NotifyManager.getInstance(this)
                    .showFinishNotification(NotifyManager.NOTIFY_RESTORING, false);
        }
    }

    private void stayForeground() {
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_application_am)
                .build();
        //notification.flags |= Notification.FLAG_HIDE_NOTIFICATION;
        startForeground(1, notification);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logger.d(TAG, "onConfigurationChanged: setRefreshFlag");
        NotifyManager.getInstance(this).setRefreshFlag();
    }

    public interface OnRestoreStatusListener {
        /**
         * @param type type
         * @param num  number
         */
        public void onComposerChanged(final int type, final int num);

        /**
         * @param composer composer
         * @param progress progress
         */
        public void onProgressChanged(Composer composer, int progress);

        /**
         * @param bSuccess     bSuccess
         * @param resultRecord resultRecord
         */
        public void onRestoreEnd(boolean bSuccess, ArrayList<ResultDialog.ResultEntity> resultRecord);

        /**
         * @param e exception
         */
        public void onRestoreErr(IOException e);
    }

    /**
     * @author mtk81330
     */
    public static class RestoreProgress {
        int mType;
        int mMax;
        int mCurNum;
    }

    /**
     * @author mtk81330
     */
    public class RestoreBinder extends Binder {
        public int getState() {
            return getRestoreState();
        }

        /**
         * @param list list
         */
        public void setRestoreModelList(ArrayList<Integer> list) {
            reset();
            if (mRestoreEngine == null) {
                mRestoreEngine = RestoreEngine
                        .getInstance(RestoreService.this, RestoreService.this);
            }
            mRestoreEngine.setRestoreModelList(list);
        }

        /**
         * @param itemType itemType
         * @param paraList paraList
         */
        public void setRestoreItemParam(int itemType, ArrayList<String> paraList) {
            mParasMap.put(itemType, paraList);
            mRestoreEngine.setRestoreItemParam(itemType, paraList);
        }

        /**
         * @param itemType itemType
         * @return ArrayList<String>
         */
        public ArrayList<String> getRestoreItemParam(int itemType) {
            return mParasMap.get(itemType);
        }

        /**
         * @param fileName file name
         * @return .
         */
        public boolean startRestore(String fileName) {
            stayForeground();
            if (mRestoreEngine == null) {
                Logger.e(TAG, "startRestore Error: engine is not initialed");
                return false;
            }
            mRestoreEngine.setOnRestoreEndListner(RestoreService.this);
            mRestoreEngine.startRestore(fileName);
            mFileName = fileName;
            moveToState(Constants.State.RUNNING);
            return true;
        }

        /**
         * pauseRestore.
         */
        public void pauseRestore() {
            moveToState(Constants.State.PAUSE);
            if (mRestoreEngine != null) {
                mRestoreEngine.pause();
            }
            Logger.d(TAG, "pauseRestore");
        }

        /**
         * continueRestore.
         */
        public void continueRestore() {
            moveToState(Constants.State.RUNNING);
            if (mRestoreEngine != null) {
                mRestoreEngine.continueRestore();
            }
            Logger.d(TAG, "continueRestore");
        }

        public void cancelRestore() {
            moveToState(Constants.State.CANCELLING);
            if (mRestoreEngine != null) {
                mRestoreEngine.cancel();
            }
            Logger.d(TAG, "cancelRestore");
        }

        /**
         * reset.
         */
        public void reset() {
            Logger.d(TAG, "reset()");
            moveToState(Constants.State.INIT);
            if (mResultList != null) {
                mResultList.clear();
            }
            if (mAppResultList != null) {
                mAppResultList.clear();
            }
            if (mParasMap != null) {
                mParasMap.clear();
            }
        }

        public RestoreProgress getCurRestoreProgress() {
            return mCurrentProgress;
        }

        public void setOnRestoreChangedListner(OnRestoreStatusListener listener) {
            mRestoreStatusListener = listener;
        }

        public ArrayList<ResultDialog.ResultEntity> getRestoreResult() {
            return mResultList;
        }

        public ArrayList<ResultDialog.ResultEntity> getAppRestoreResult() {
            return mAppResultList;
        }
    }
}
