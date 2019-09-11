package com.ape.transfer.activity;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;

import com.ape.backuprestore.PersonalItemData;
import com.ape.backuprestore.RecordXmlComposer;
import com.ape.backuprestore.RecordXmlInfo;
import com.ape.backuprestore.RecordXmlParser;
import com.ape.backuprestore.RestoreService;
import com.ape.backuprestore.ResultDialog;
import com.ape.backuprestore.modules.Composer;
import com.ape.backuprestore.utils.BackupFilePreview;
import com.ape.backuprestore.utils.Constants;
import com.ape.backuprestore.utils.Logger;
import com.ape.backuprestore.utils.ModuleType;
import com.ape.backuprestore.utils.StorageUtils;
import com.ape.backuprestore.utils.Utils;
import com.ape.transfer.R;
import com.ape.transfer.fragment.loader.BaseLoader;
import com.ape.transfer.fragment.loader.RestoreDataLoader;
import com.ape.transfer.model.TransferTaskFinishEvent;
import com.ape.transfer.p2p.beans.TransferFile;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.RxBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by android on 16-7-13.
 */
public class NewPhoneExchangeActivity extends BaseActivity implements
        LoaderManager.LoaderCallbacks<BaseLoader.Result>, RestoreService.OnRestoreStatusListener {
    private static final String TAG = "NewPhoneExchangeActivity";
    protected RestoreService.RestoreBinder mRestoreService;
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
            Logger.i(TAG, " onServiceConnected");
            mRestoreService = (RestoreService.RestoreBinder) service;
            if (mRestoreService != null) {
                mRestoreService.setOnRestoreChangedListner(NewPhoneExchangeActivity.this);
            }
            startLoadRestoreData();
        }

        public void onServiceDisconnected(ComponentName name) {
            Logger.i(TAG, " onServiceDisconnected");
            if (mRestoreService != null) {
                mRestoreService.setOnRestoreChangedListner(null);
            }
            mRestoreService = null;
        }
    };
    private String mRestoreFolderPath;
    private ProgressDialog mProgressDialog;
    private boolean mIsStoped = false;
    private boolean mIsDestroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        bindRestoreService();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_new_phone_exchange;
    }

    @Override
    public void onBackPressed() {
        if (mRestoreService != null && mRestoreService.getState() == Constants.State.RUNNING) {
            return;
        }
        super.onBackPressed();
    }

    private void init() {
        mRestoreFolderPath = StorageUtils.getBackupPath();

        if (TextUtils.isEmpty(mRestoreFolderPath)) {
            Logger.d(TAG, "SDCard is removed");
            Toast.makeText(this, R.string.nosdcard_notice, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mDisposable.add(RxBus.getInstance().toObservable(TransferTaskFinishEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(finishEvent -> {
                    //do some thing
                    if (finishEvent.getDirection() == TransferFile.Direction.DIRECTION_RECEIVE) {
                        startLoadRestoreData();
                    }
                }));
    }

    private void startLoadRestoreData() {
        setButtonsEnable(false);
        showLoadingContent(true);
        getSupportLoaderManager().initLoader(1, null, this);
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
        Logger.i(TAG, " onDestroy");

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
        unBindRestoreService();
    }

    public void startRestore(ArrayList<Integer> restoreModeLists) {
        if (!isCanStartRestore()) {
            return;
        }
        startService();
        Logger.d(TAG, "startRestore");
        if (restoreModeLists.size() == 0) {
            Toast.makeText(this, getString(R.string.no_item_selected), Toast.LENGTH_SHORT).show();
            return;
        }
        mRestoreService.setRestoreModelList(restoreModeLists);
        boolean ret = mRestoreService.startRestore(mRestoreFolderPath);
        if (ret) {
            String path = StorageUtils.getBackupPath();
            if (path == null) {
                // no sdcard
                Logger.d(TAG, "SDCard is removed");
                return;
            }
            int count = BackupFilePreview.getInstance().getItemCount(restoreModeLists.get(0));
            int type = restoreModeLists.get(0);
            showProgressDialog(count, type);
        } else {
            stopService();
        }
    }

    private void showProgressDialog(int count, int type) {
        if (mProgressDialog == null) {
            mProgressDialog = createProgressDlg();
        }
        mProgressDialog.setMessage(getString(R.string.restoring,
                ModuleType.getModuleStringFromType(getApplicationContext(), type)));
        mProgressDialog.setMax(count);
        mProgressDialog.setProgress(0);
        try {
            if (!mProgressDialog.isShowing())
                mProgressDialog.show();
        } catch (WindowManager.BadTokenException e) {
            Logger.e(TAG, " BadTokenException :" + e.toString());
        }
    }

    protected boolean isCanStartRestore() {
        if (mRestoreService == null) {
            Logger.e(TAG, "isCanStartRestore(): mRestoreService is null");
            return false;
        }

        if (mRestoreService.getState() != Constants.State.INIT) {
            Logger.e(TAG,
                    "isCanStartRestore(): Can not to start Restore. Restore Service state is "
                            + mRestoreService.getState());
            return false;
        }
        return true;
    }

    protected ProgressDialog createProgressDlg() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMessage(getString(R.string.restoring));
            mProgressDialog.setCancelable(false);
        }
        return mProgressDialog;
    }

    protected void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void setButtonsEnable(boolean enabled) {
        btnSure.setEnabled(enabled);
        btnCancel.setEnabled(enabled);
    }

    private void showLoadingContent(boolean show) {
    }

    private void updateData(ArrayList<PersonalItemData> list) {
        ArrayList<Integer> restoreModeLists = new ArrayList<>();
        for (PersonalItemData item : list) {
            restoreModeLists.add(item.getType());
        }
        startRestore(restoreModeLists);
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

    private void bindRestoreService() {
        bindService(new Intent(this, RestoreService.class), mServiceCon, Service.BIND_AUTO_CREATE);
    }

    private void unBindRestoreService() {
        if (mRestoreService != null) {
            mRestoreService.setOnRestoreChangedListner(null);
        }
        try {
            unbindService(mServiceCon);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                showProgressDialog(max, type);
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
                    Logger.i(TAG, "onProgressChange, setProgress = " + progress);
                    mProgressDialog.setProgress(progress);
                }
            }
        });
    }

    @Override
    public void onRestoreEnd(boolean bSuccess, ArrayList<ResultDialog.ResultEntity> resultRecord) {
        final ArrayList<ResultDialog.ResultEntity> iResultRecord = resultRecord;
        Logger.d(TAG, "onRestoreEnd");
        boolean hasSuccess = false;
        for (ResultDialog.ResultEntity result : resultRecord) {
            if (ResultDialog.ResultEntity.SUCCESS == result.getResult()) {
                hasSuccess = true;
                break;
            }
        }

        if (hasSuccess) {
            String recordXmlFile = mRestoreFolderPath + File.separator + Constants.RECORD_XML;
            String content = Utils.readFromFile(recordXmlFile);
            ArrayList<RecordXmlInfo> recordList = new ArrayList<>();
            if (content != null) {
                recordList = RecordXmlParser.parse(content.toString());
            }
            RecordXmlComposer xmlComposer = new RecordXmlComposer();
            xmlComposer.startCompose();

            RecordXmlInfo restoreInfo = new RecordXmlInfo();
            restoreInfo.setRestore(true);
            restoreInfo.setDevice(Utils.getPhoneSearialNumber());
            restoreInfo.setTime(String.valueOf(System.currentTimeMillis()));

            boolean bAdded = false;
            for (RecordXmlInfo record : recordList) {
                if (record.getDevice().equals(restoreInfo.getDevice())) {
                    xmlComposer.addOneRecord(restoreInfo);
                    bAdded = true;
                } else {
                    xmlComposer.addOneRecord(record);
                }
            }

            if (!bAdded) {
                xmlComposer.addOneRecord(restoreInfo);
            }
            xmlComposer.endCompose();
            Utils.writeToFile(xmlComposer.getXmlInfo(), recordXmlFile);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Logger.d(TAG, " Restore show Result Dialog");
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
        Logger.i(TAG, "onRestoreErr");
        if (!mIsStoped && errChecked()) {
            if (mRestoreService != null && mRestoreService.getState() != Constants.State.INIT
                    && mRestoreService.getState() != Constants.State.FINISH) {
                mRestoreService.pauseRestore();
            }
        }
    }

    protected boolean errChecked() {
        boolean ret = false;

        boolean isStorageMissing = StorageUtils.isStorageMissing();
        String path = StorageUtils.getBackupPath();

        if (isStorageMissing) {
            Logger.i(TAG, "SDCard is removed");
            stopService(new Intent(this, RestoreService.class));
            Utils.exitLockTaskModeIfNeeded(this);
            finish();
        } else if (StorageUtils.getAvailableSize(path) <= StorageUtils.MINIMUM_SIZE) {
            Logger.i(TAG, "SDCard is full");
            ret = true;

        } else {
            Logger.e(TAG, "Unkown error, don't pause.");
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
            Logger.e(TAG, "BadTokenException");
        }
    }

    @Override
    public Loader<BaseLoader.Result> onCreateLoader(int id, Bundle args) {
        return new RestoreDataLoader(getApplicationContext());
    }

    @Override
    public void onLoadFinished(Loader<BaseLoader.Result> loader, BaseLoader.Result data) {
        Logger.d(TAG, "onLoadFinished... data.size = " + data.lists.size());
        if (!data.lists.isEmpty()) {
            Log.i(TAG, "updateData... mBackupDataList.size = " + data.lists.size());
            updateData(data.lists);
            setButtonsEnable(true);
            showLoadingContent(false);

        } else {
            setButtonsEnable(false);
            showLoadingContent(false);
        }
    }

    @Override
    public void onLoaderReset(Loader<BaseLoader.Result> loader) {
        updateData(new ArrayList<PersonalItemData>());
    }

}
