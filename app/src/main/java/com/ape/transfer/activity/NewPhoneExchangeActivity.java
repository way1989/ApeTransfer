package com.ape.transfer.activity;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ape.backuprestore.PersonalItemData;
import com.ape.backuprestore.RecordXmlComposer;
import com.ape.backuprestore.RecordXmlInfo;
import com.ape.backuprestore.RecordXmlParser;
import com.ape.backuprestore.RestoreService;
import com.ape.backuprestore.ResultDialog;
import com.ape.backuprestore.modules.Composer;
import com.ape.backuprestore.utils.BackupFilePreview;
import com.ape.backuprestore.utils.Constants;
import com.ape.backuprestore.utils.ModuleType;
import com.ape.backuprestore.utils.MyLogger;
import com.ape.backuprestore.utils.SDCardUtils;
import com.ape.backuprestore.utils.Utils;
import com.ape.transfer.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by android on 16-7-13.
 */
public class NewPhoneExchangeActivity extends BaseActivity implements RestoreService.OnRestoreStatusListener {
    private static final String TAG = "NewPhoneExchangeActivity";
    private static final String RESTORE_PATH = "restorePath";
    protected RestoreService.RestoreBinder mRestoreService;
    BackupFilePreview mPreview = null;
    @BindView(R.id.iv_complete)
    ImageView ivComplete;
    @BindView(R.id.loading)
    ProgressBar loading;
    @BindView(R.id.tv_process_title)
    TextView tvProcessTitle;
    @BindView(R.id.rv_process)
    RecyclerView rvProcess;
    @BindView(R.id.btnSure)
    Button btnSure;
    @BindView(R.id.btnCancel)
    Button btnCancel;
    ServiceConnection mServiceCon = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder service) {
            mRestoreService = (RestoreService.RestoreBinder) service;

            afterServiceConnected();
            MyLogger.logI(TAG, " onServiceConnected");
        }

        public void onServiceDisconnected(ComponentName name) {
            mRestoreService = null;
            MyLogger.logI(TAG, " onServiceDisconnected");
        }
    };
    private String mRestoreFolderPath;
    private FilePreviewTask mPreviewTask;
    private ProgressDialog mProgressDialog;
    private boolean mIsStoped = false;
    private boolean mIsDestroyed = false;
    private ArrayList<Integer> mRestoreModeLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_phone_exchange);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            mRestoreFolderPath = savedInstanceState.getString(RESTORE_PATH);
        } else {
            mRestoreFolderPath = SDCardUtils.getPersonalDataBackupPath(getApplicationContext());
        }
        if (mRestoreFolderPath == null) {
            finish();
            return;
        }
        MyLogger.logI(TAG, "onCreate with file " + mRestoreFolderPath);
        init();
         /*
         * bind Restore Service when activity onCreate, and unBind Service when
         * activity onDestroy
         */
        this.bindService();
    }

    @Override
    public void onBackPressed() {
        if (mRestoreService != null && mRestoreService.getState() == Constants.State.RUNNING) {
            return;
        }
        super.onBackPressed();
    }

    private void init() {
        if (SDCardUtils.getStoragePath(this) == null) {
            MyLogger.logD(TAG, "SDCard is removed");
            Toast.makeText(this, R.string.nosdcard_notice, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // update to avoid files deleted
        if (new File(mRestoreFolderPath).exists()) {
            mPreviewTask = new FilePreviewTask();
            mPreviewTask.execute();
        }
        createProgressDlg();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mIsStoped = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mIsStoped = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsDestroyed = true;
        MyLogger.logI(TAG, " onDestroy");
        if (null != mPreviewTask) {
            boolean result = mPreviewTask.cancel(true);
            MyLogger.logD(TAG, "onDestory result : " + result);
        }

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }

        // when startService when to Restore and stopService when onDestroy if
        // the service in IDLE
        if (mRestoreService != null && mRestoreService.getState() == Constants.State.INIT) {
            this.stopService();
        }

        // set listener to null avoid some special case when restart after
        // configure changed
        if (mRestoreService != null) {
            mRestoreService.setOnRestoreChangedListner(null);
        }
        this.unBindService();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(RESTORE_PATH, mRestoreFolderPath);
    }

    public void startRestore() {
        if (!isCanStartRestore()) {
            return;
        }
        startService();
        MyLogger.logD(TAG, "startRestore");
        ArrayList<Integer> list = mRestoreModeLists;
        if (list.size() == 0) {
            Toast.makeText(this, getString(R.string.no_item_selected), Toast.LENGTH_SHORT).show();
            return;
        }
        mRestoreService.setRestoreModelList(list);
        boolean ret = mRestoreService.startRestore(mRestoreFolderPath);
        if (ret) {
            String path = SDCardUtils.getStoragePath(this);
            if (path == null) {
                // no sdcard
                MyLogger.logD(TAG, "SDCard is removed");
                return;
            }
            showProgressDialog();
            String msg = getProgressDlgMessage(list.get(0));
            setProgressDialogMessage(msg);
            setProgressDialogProgress(0);
            setProgress(0);
            if (mPreview != null) {
                int count = mPreview.getItemCount(list.get(0));
                setProgressDialogMax(count);
            }
        } else {
            stopService();
        }
    }

    protected boolean isCanStartRestore() {
        if (mRestoreService == null) {
            MyLogger.logE(TAG, "isCanStartRestore(): mRestoreService is null");
            return false;
        }

        if (mRestoreService.getState() != Constants.State.INIT) {
            MyLogger.logE(TAG,
                    "isCanStartRestore(): Can not to start Restore. Restore Service state is "
                            + mRestoreService.getState());
            return false;
        }
        return true;
    }

    private String getProgressDlgMessage(int type) {
        StringBuilder builder = new StringBuilder(getString(R.string.restoring));

        builder.append("(").append(ModuleType.getModuleStringFromType(this, type)).append(")");
        return builder.toString();
    }

    protected ProgressDialog createProgressDlg() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            if (mProgressDialog == null)
                return null;
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMessage(getString(R.string.restoring));
            mProgressDialog.setCancelable(false);
        }
        return mProgressDialog;
    }

    protected void showProgressDialog() {
        if (isFinishing()) {
            return;
        }
        if (mProgressDialog == null) {
            mProgressDialog = createProgressDlg();
        }
        try {
            mProgressDialog.show();
        } catch (WindowManager.BadTokenException e) {
            MyLogger.logE(TAG, " BadTokenException :" + e.toString());
        }
    }

    protected void setProgressDialogMax(int max) {
        if (mProgressDialog == null) {
            mProgressDialog = createProgressDlg();
        }
        mProgressDialog.setMax(max);
    }

    protected void setProgressDialogProgress(int value) {
        if (mProgressDialog == null) {
            mProgressDialog = createProgressDlg();
        }
        mProgressDialog.setProgress(value);
    }

    protected void setProgressDialogMessage(CharSequence message) {
        if (mProgressDialog == null) {
            mProgressDialog = createProgressDlg();
        }
        mProgressDialog.setMessage(message);
    }

    protected boolean isProgressDialogShowing() {
        return mProgressDialog.isShowing();
    }

    protected void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void setButtonsEnable(boolean enabled) {
    }

    private void afterServiceConnected() {
        setOnRestoreStatusListener();
    }

    private void setOnRestoreStatusListener() {
        if (mRestoreService != null)
            mRestoreService.setOnRestoreChangedListner(this);
    }

    private void showLoadingContent(boolean show) {
    }

    private void updateData(ArrayList<PersonalItemData> list) {
        if (list.isEmpty()) {
            Toast.makeText(getApplicationContext(), "not restore datas...", Toast.LENGTH_SHORT).show();
            return;
        }
        mRestoreModeLists = new ArrayList<>();
        for (PersonalItemData item : list) {
            mRestoreModeLists.add(item.getType());
        }
        startRestore();
    }

    @OnClick({R.id.btnSure, R.id.btnCancel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSure:
                break;
            case R.id.btnCancel:
                break;
        }
    }

    private void bindService() {
        getApplicationContext().bindService(new Intent(this, RestoreService.class), mServiceCon,
                Service.BIND_AUTO_CREATE);
    }

    private void unBindService() {
        if (mRestoreService != null) {
            mRestoreService.setOnRestoreChangedListner(null);
        }
        getApplicationContext().unbindService(mServiceCon);
    }

    protected void startService() {
        startService(new Intent(this, RestoreService.class));
    }

    protected void stopService() {
        if (mRestoreService != null) {
            mRestoreService.reset();
        }
        stopService(new Intent(this, RestoreService.class));
    }

    @Override
    public void onComposerChanged(final int type, final int max) {
        runOnUiThread(new Runnable() {

            public void run() {
                String msg = getProgressDlgMessage(type);
                setProgressDialogMessage(msg);
                setProgressDialogMax(max);
                setProgressDialogProgress(0);
            }
        });
    }

    @Override
    public void onProgressChanged(Composer composer, final int progress) {
        if (mIsStoped) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    MyLogger.logI(TAG, "onProgressChange, setProgress = " + progress);
                    mProgressDialog.setProgress(progress);
                }
            }
        });
    }

    @Override
    public void onRestoreEnd(boolean bSuccess, ArrayList<ResultDialog.ResultEntity> resultRecord) {
        final ArrayList<ResultDialog.ResultEntity> iResultRecord = resultRecord;
        MyLogger.logD(TAG, "onRestoreEnd");
        boolean hasSuccess = false;
        for (ResultDialog.ResultEntity result : resultRecord) {
            if (ResultDialog.ResultEntity.SUCCESS == result.getResult()) {
                hasSuccess = true;
                break;
            }
        }

        if (hasSuccess) {
            String recrodXmlFile = mRestoreFolderPath + File.separator + Constants.RECORD_XML;
            String content = Utils.readFromFile(recrodXmlFile);
            ArrayList<RecordXmlInfo> recordList = new ArrayList<>();
            if (content != null) {
                recordList = RecordXmlParser.parse(content.toString());
            }
            RecordXmlComposer xmlCompopser = new RecordXmlComposer();
            xmlCompopser.startCompose();

            RecordXmlInfo restoreInfo = new RecordXmlInfo();
            restoreInfo.setRestore(true);
            restoreInfo.setDevice(Utils.getPhoneSearialNumber());
            restoreInfo.setTime(String.valueOf(System.currentTimeMillis()));

            boolean bAdded = false;
            for (RecordXmlInfo record : recordList) {
                if (record.getDevice().equals(restoreInfo.getDevice())) {
                    xmlCompopser.addOneRecord(restoreInfo);
                    bAdded = true;
                } else {
                    xmlCompopser.addOneRecord(record);
                }
            }

            if (!bAdded) {
                xmlCompopser.addOneRecord(restoreInfo);
            }
            xmlCompopser.endCompose();
            Utils.writeToFile(xmlCompopser.getXmlInfo(), recrodXmlFile);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyLogger.logD(TAG, " Restore show Result Dialog");
                int state = mRestoreService.getState();
                if (mIsStoped && !TextUtils.isEmpty(mRestoreFolderPath) && state != Constants.State.FINISH) {
                    //mNeedUpdateResult = true;
                } else {
                    if (!mIsDestroyed) {
                        showRestoreResult(iResultRecord);
                    }
                }
            }
        });
    }

    @Override
    public void onRestoreErr(IOException e) {
        MyLogger.logI(TAG, "onRestoreErr");
        if (!mIsStoped && errChecked()) {
            if (mRestoreService != null && mRestoreService.getState() != Constants.State.INIT
                    && mRestoreService.getState() != Constants.State.FINISH) {
                mRestoreService.pauseRestore();
            }
        }
    }

    protected boolean errChecked() {
        boolean ret = false;

        boolean isSDCardMissing = SDCardUtils.isSdCardMissing(this);
        String path = SDCardUtils.getStoragePath(this);

        if (isSDCardMissing) {
            MyLogger.logI(TAG, "SDCard is removed");
            stopService(new Intent(this, RestoreService.class));
            Utils.exitLockTaskModeIfNeeded(this);
            finish();
        } else if (SDCardUtils.getAvailableSize(path) <= SDCardUtils.MINIMUM_SIZE) {
            MyLogger.logI(TAG, "SDCard is full");
            ret = true;

        } else {
            MyLogger.logE(TAG, "Unkown error, don't pause.");
        }
        return ret;
    }

    private void showRestoreResult(ArrayList<ResultDialog.ResultEntity> list) {
        dismissProgressDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList("result", list);
        try {
            Toast.makeText(getApplicationContext(), "scuessed!", Toast.LENGTH_SHORT).show();
            //showDialog(Constants.DialogID.DLG_RESULT, args);
        } catch (WindowManager.BadTokenException e) {
            MyLogger.logE(TAG, "BadTokenException");
        }
    }

    private class FilePreviewTask extends AsyncTask<Void, Void, Long> {
        private int mModule = 0;

        @Override
        protected void onPostExecute(Long arg0) {
            super.onPostExecute(arg0);
            int types[] = new int[]{
                    ModuleType.TYPE_CONTACT,
                    ModuleType.TYPE_MESSAGE,
                    ModuleType.TYPE_PICTURE,
                    ModuleType.TYPE_CALENDAR,
                    ModuleType.TYPE_MUSIC,
//                    ModuleType.TYPE_BOOKMARK
            };

            ArrayList<PersonalItemData> list = new ArrayList<>();
            for (int type : types) {
                if ((mModule & type) != 0) {
                    PersonalItemData item = new PersonalItemData(type, 1);
                    list.add(item);
                }
            }
            updateData(list);
            setButtonsEnable(true);
            showLoadingContent(false);

            setOnRestoreStatusListener();
            MyLogger.logD(TAG, "mIsDataInitialed is ok");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setButtonsEnable(false);
            showLoadingContent(true);
        }

        @Override
        protected Long doInBackground(Void... arg0) {
            mPreview = new BackupFilePreview(new File(mRestoreFolderPath));
            mModule = mPreview.getBackupModules(NewPhoneExchangeActivity.this);
            return null;
        }
    }
}
