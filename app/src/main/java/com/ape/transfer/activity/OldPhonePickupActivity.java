package com.ape.transfer.activity;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ape.backuprestore.BackupEngine;
import com.ape.backuprestore.BackupService;
import com.ape.backuprestore.PersonalItemData;
import com.ape.backuprestore.RecordXmlComposer;
import com.ape.backuprestore.RecordXmlInfo;
import com.ape.backuprestore.ResultDialog;
import com.ape.backuprestore.modules.AppBackupComposer;
import com.ape.backuprestore.modules.CalendarBackupComposer;
import com.ape.backuprestore.modules.CallLogBackupComposer;
import com.ape.backuprestore.modules.Composer;
import com.ape.backuprestore.modules.ContactBackupComposer;
import com.ape.backuprestore.modules.MmsBackupComposer;
import com.ape.backuprestore.modules.MusicBackupComposer;
import com.ape.backuprestore.modules.NoteBookBackupComposer;
import com.ape.backuprestore.modules.PictureBackupComposer;
import com.ape.backuprestore.modules.SmsBackupComposer;
import com.ape.backuprestore.utils.Constants;
import com.ape.backuprestore.utils.FileUtils;
import com.ape.backuprestore.utils.ModuleType;
import com.ape.backuprestore.utils.SDCardUtils;
import com.ape.backuprestore.utils.Utils;
import com.ape.transfer.R;
import com.ape.transfer.adapter.OldPhonePickupAdapter;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.TDevice;
import com.ape.transfer.widget.MobileDataWarningContainer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by android on 16-7-13.
 */
public class OldPhonePickupActivity extends BaseActivity implements OldPhonePickupAdapter.OnItemClickListener,
        BackupService.OnBackupStatusListener {
    private static final String TAG = "OldPhonePickupActivity";
    protected BackupService.BackupBinder mBackupService;
    protected ProgressDialog mProgressDialog;
    @BindView(R.id.mobile_data_warning)
    MobileDataWarningContainer mobileDataWarning;
    @BindView(R.id.rv_data_category)
    RecyclerView rvDataCategory;
    @BindView(R.id.storageView)
    TextView storageView;
    @BindView(R.id.btn_sure)
    Button btnSure;
    private List<String> mMessageEnable = new ArrayList<>();
    private OldPhonePickupAdapter mAdapter;
    private P2PNeighbor mP2PNeighbor;
    private InitPersonalDataTask mInitDataTask;
    private String mFolderName;
    private boolean mIsShowWarning = true;
    private ServiceConnection mServiceCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mBackupService = (BackupService.BackupBinder) service;
            if (mBackupService != null) {
                mBackupService.setOnBackupChangedListner(OldPhonePickupActivity.this);
            }
            afterServiceConnected();
            Log.i(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mBackupService = null;
            Log.i(TAG, "onServiceDisconnected");
        }
    };
    private String mBackupFolderPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_phone_pickup);
        ButterKnife.bind(this);
        bindService();
        if (TDevice.hasInternet()) {
            mobileDataWarning.setVisibility(View.VISIBLE);
        }
        if (getIntent().hasExtra("neighbor")) {
            mP2PNeighbor = (P2PNeighbor) (getIntent().getSerializableExtra("neighbor"));
        }
        mAdapter = new OldPhonePickupAdapter(getApplicationContext(), this);
        rvDataCategory.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
        rvDataCategory.setAdapter(mAdapter);
        mInitDataTask = new InitPersonalDataTask();
        mInitDataTask.execute();
        createProgressDlg();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBackupService != null && mBackupService.getState() == Constants.State.INIT) {
            stopService();
        }
        if (mBackupService != null) {
            mBackupService.setOnBackupChangedListner(null);
        }
    }

    private int getModulesCount(Composer... composers) {
        int count = 0;
        for (Composer composer : composers) {
            if (composer.init()) {
                count += composer.getCount();
                composer.onEnd();
            }
        }
        Log.i(TAG, "getModulesCount : " + count);
        return count;
    }

    public void setButtonsEnable(boolean isEnabled) {
        btnSure.setEnabled(isEnabled);
    }

    private void updateData(ArrayList<PersonalItemData> mBackupDataList) {
        Log.i(TAG, "updateData... mBackupDataList.size = " + mBackupDataList.size());
        mAdapter.setDatas(mBackupDataList);
    }

    private void showLoadingContent(boolean isShow) {
    }

    private void showWarningForLargeFiles(PersonalItemData itemData, ArrayList<Integer> list) {
        int type = itemData.getType();
        switch (type) {
            case ModuleType.TYPE_CONTACT:
            case ModuleType.TYPE_CALL_LOG:
            case ModuleType.TYPE_MESSAGE:
            case ModuleType.TYPE_CALENDAR:
                break;
            case ModuleType.TYPE_MUSIC:
            case ModuleType.TYPE_APP:
            case ModuleType.TYPE_PICTURE:
                if (itemData.isSelected() && mIsShowWarning) {
                    showWarningDialog(itemData);
                }
                if (list.contains(ModuleType.TYPE_PICTURE) ||
                        list.contains(ModuleType.TYPE_APP) ||
                        list.contains(ModuleType.TYPE_MUSIC)) {
                    mIsShowWarning = false;
                } else {
                    mIsShowWarning = true;
                }
                break;
        }
    }

    private void showWarningDialog(final PersonalItemData itemData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.connect_dialog_title).setMessage(R.string.backup_cost_more_time_prompt)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mIsShowWarning = false;
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAdapter.notifyPersonalItemData(itemData);
                        mIsShowWarning = true;
                    }
                }).show();
    }

    @Override
    public void onItemClick(View v) {
        ArrayList<Integer> selectedList = getSelectedItemList();
        showWarningForLargeFiles((PersonalItemData) v.getTag(), selectedList);
        btnSure.setEnabled(!getSelectedItemList().isEmpty());
        storageView.setText(getSelectedItemList().isEmpty() ?
                getResources().getQuantityString(R.plurals.total_selected, 0, 0):
                getResources().getQuantityString(R.plurals.total_selected, getSelectedItemList().size(),
                        getSelectedItemList().size()));
    }

    @Override
    public void onComposerChanged(final Composer composer) {
        if (composer == null) {
            Log.e(TAG, "onComposerChanged: error[composer is null]");
            return;
        }
        Log.i(TAG, "onComposerChanged: type = " + composer.getModuleType()
                + "Max = " + composer.getCount());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String msg = getProgressDlgMessage(composer.getModuleType());
                Log.d(TAG, "mProgressDialog : " + mProgressDialog);
                if (mProgressDialog != null
                        && !mProgressDialog.isShowing()) {
                    if (mBackupService != null
                            && mBackupService.getState() != Constants.State.PAUSE) {
                        showProgress(0, null);
                    }
                }
                if (mProgressDialog != null) {
                    mProgressDialog.setMessage(msg);
                    mProgressDialog.setMax(composer.getCount());
                    mProgressDialog.setProgress(0);
                }
            }
        });

    }

    protected String getProgressDlgMessage(final int type) {
        StringBuilder builder = new StringBuilder(getString(R.string.backuping));
        builder.append("(");
        builder.append(ModuleType.getModuleStringFromType(this, type));
        builder.append(")");
        return builder.toString();
    }

    @Override
    public void onProgressChanged(Composer composer, final int progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.setProgress(progress);
                }
            }
        });
    }

    @Override
    public void onBackupEnd(BackupEngine.BackupResultType resultCode, ArrayList<ResultDialog.ResultEntity> resultRecord, ArrayList<ResultDialog.ResultEntity> appResultRecord) {
        if (resultCode != BackupEngine.BackupResultType.Cancel) {
            RecordXmlInfo backupInfo = new RecordXmlInfo();
            backupInfo.setRestore(false);
            backupInfo.setDevice(Utils.getPhoneSearialNumber());
            backupInfo.setTime(String.valueOf(System.currentTimeMillis()));
            RecordXmlComposer xmlCompopser = new RecordXmlComposer();
            xmlCompopser.startCompose();
            xmlCompopser.addOneRecord(backupInfo);
            xmlCompopser.endCompose();
            if (mBackupFolderPath != null && !mBackupFolderPath.isEmpty()) {
                Utils.writeToFile(xmlCompopser.getXmlInfo(), mBackupFolderPath + File.separator
                        + Constants.RECORD_XML);
            }
        } else {
            Log.e(TAG, "ResultCode is cancel, not write record.xml");
        }

        final BackupEngine.BackupResultType iResultCode = resultCode;
        final ArrayList<ResultDialog.ResultEntity> iResultRecord = resultRecord;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showBackupResult(iResultCode, iResultRecord);
            }
        });

    }

    private void showBackupResult(final BackupEngine.BackupResultType result,
                                  final ArrayList<ResultDialog.ResultEntity> list) {
        Log.i(TAG, "showBackupResult");
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }

        if (result != BackupEngine.BackupResultType.Cancel) {
            Toast.makeText(getApplicationContext(), R.string.backup_result, Toast.LENGTH_SHORT).show();
        } else {
            stopService();
        }
    }

    @Override
    public void onBackupErr(IOException e) {
        if (mBackupService != null &&
                mBackupService.getState() != Constants.State.INIT &&
                mBackupService.getState() != Constants.State.FINISH) {
            mBackupService.pauseBackup();
        }
    }

    @OnClick(R.id.btn_sure)
    public void onClick() {
        if (Utils.getWorkingInfo() < 0) {
            startBackup();
        } else {
            //showDialog(DialogID.DLG_RUNNING);
        }
    }

    private ProgressDialog createProgressDlg() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMessage(getString(R.string.backuping));
            mProgressDialog.setCancelable(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
            //mProgressDialog.setCancelMessage(mHandler.obtainMessage(MessageID.PRESS_BACK));
        }
        return mProgressDialog;
    }

    protected void showProgress(int defaltMax, String defaltModule) {
        if (mProgressDialog == null) {
            mProgressDialog = createProgressDlg();
        }
        Log.i(TAG, "no need to set max");
        if (defaltMax != 0 && defaltModule != null) {
            mProgressDialog.setMax(defaltMax);
            mProgressDialog.setMessage(defaltModule);
        }

        if (this != null && !this.isFinishing()) {
            try {
                mProgressDialog.show();
            } catch (WindowManager.BadTokenException e) {
                Log.e(TAG, " BadTokenException :" + e.toString());
            }
        }
    }

    private void startBackup() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        mFolderName = dateFormat.format(
                new Date(System.currentTimeMillis()));

        String path = SDCardUtils
                .getPersonalDataBackupPath(getApplicationContext());
        if (path == null) {
            return;
        }
        /** M: Bug Fix for CR ALPS01694645 @{ */
        String pathSD = SDCardUtils
                .getStoragePath(getApplicationContext());
        if (SDCardUtils.getAvailableSize(pathSD) <= SDCardUtils.MINIMUM_SIZE) {
            // no space
            Log.d(TAG, "SDCard is full");
            //mUiHandler.obtainMessage(DialogID.DLG_SDCARD_FULL).sendToTarget();
            Toast.makeText(getApplicationContext(), "SDCard is full...", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder builder = new StringBuilder(path);
        builder.append(File.separator);
        builder.append(mFolderName);
        mBackupFolderPath = builder.toString();
        Log.d(TAG, "[processClickStart] mBackupFolderPath is " + mBackupFolderPath);
        File folder = new File(mBackupFolderPath);
        File[] files = null;
        if (folder.exists()) {
            files = folder.listFiles();
        }
        if (files != null && files.length > 0) {
            Log.d(TAG, "[processClickStart] DLG_BACKUP_CONFIRM_OVERWRITE Here! ");
            for (File file : files) {
                FileUtils.deleteFileOrFolder(file);
            }
            return;
        }
        startPersonalDataBackup(mBackupFolderPath);
    }

    private void startPersonalDataBackup(String folderName) {
        if (folderName == null || folderName.trim().equals("")) {
            return;
        }
        final ArrayList<Integer> list = getSelectedItemList();
        if (list.isEmpty())
            return;

        startService();
        if (mBackupService != null) {
            mBackupService.setBackupModelList(list);
            if (list.contains(ModuleType.TYPE_MESSAGE)) {
                ArrayList<String> params = new ArrayList<String>();
                params.add(Constants.ModulePath.NAME_SMS);
                params.add(Constants.ModulePath.NAME_MMS);
                mBackupService.setBackupItemParam(ModuleType.TYPE_MESSAGE, params);
            }
            boolean ret = mBackupService.startBackup(folderName);
            if (!ret) {
                String path = SDCardUtils.getStoragePath(this);
                if (path == null) {
                    // no sdcard
                    Log.d(TAG, "SDCard is removed");
                    ret = true;
                } else if (SDCardUtils.getAvailableSize(path) <= SDCardUtils.MINIMUM_SIZE) {
                    // no space
                    Log.d(TAG, "SDCard is full");
                    ret = true;
                    //mUiHandler.obtainMessage(DialogID.DLG_SDCARD_FULL).sendToTarget();
                } else {
                    Log.e(TAG, "Unknown error");
                    Bundle b = new Bundle();
                    b.putString("name", folderName.substring(folderName.lastIndexOf('/') + 1));
                    //mUiHandler.obtainMessage(DialogID.DLG_CREATE_FOLDER_FAILED, b).sendToTarget();
                }
                stopService();
            }
        } else {
            stopService();
            Log.e(TAG, "startPersonalDataBackup: error! service is null");
        }

    }

    private ArrayList<Integer> getSelectedItemList() {
        ArrayList<Integer> list = new ArrayList<>();
        int count = mAdapter.getItemCount();
        for (int position = 0; position < count; position++) {
            PersonalItemData item = mAdapter.getItemByPosition(position);
            if (item.isSelected()) {
                list.add(item.getType());
            }
        }
        return list;
    }

    protected void afterServiceConnected() {
        //mBackupListener = new PersonalDataBackupStatusListener();
        setOnBackupStatusListener();
        //checkBackupState();
    }

    public void setOnBackupStatusListener() {
        if (mBackupService != null) {
            mBackupService.setOnBackupChangedListner(this);
        }
    }

    private void bindService() {
        this.getApplicationContext().bindService(new Intent(this, BackupService.class),
                mServiceCon, Service.BIND_AUTO_CREATE);
    }

    private void unBindService() {
        if (mBackupService != null) {
            mBackupService.setOnBackupChangedListner(null);
        }
        this.getApplicationContext().unbindService(mServiceCon);
    }

    protected void startService() {
        this.startService(new Intent(this, BackupService.class));
    }

    protected void stopService() {
        if (mBackupService != null) {
            mBackupService.reset();
        }
        this.stopService(new Intent(this, BackupService.class));
    }

    private class InitPersonalDataTask extends AsyncTask<Void, Void, Long> {
        private static final String TASK_TAG = "InitPersonalDataTask";
        ArrayList<PersonalItemData> mBackupDataList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, TASK_TAG + "---onPreExecute");
            showLoadingContent(true);
            setTitle(R.string.backup_personal_data);
            setButtonsEnable(false);
        }

        @Override
        protected void onPostExecute(Long arg0) {
            showLoadingContent(false);
            updateData(mBackupDataList);
            setButtonsEnable(!getSelectedItemList().isEmpty());
            storageView.setText(getResources().getQuantityString(R.plurals.total_selected, getSelectedItemList().size(), getSelectedItemList().size()));
            setOnBackupStatusListener();
            Log.i(TAG, "---onPostExecute----getTitle " + OldPhonePickupActivity.this.getTitle());
            super.onPostExecute(arg0);
        }

        @Override
        protected Long doInBackground(Void... arg0) {
            try {
                mMessageEnable.clear();
                mBackupDataList = new ArrayList<>();
                int types[] = new int[]{
                        ModuleType.TYPE_CONTACT,
                        ModuleType.TYPE_MESSAGE,
                        ModuleType.TYPE_CALL_LOG,
                        ModuleType.TYPE_CALENDAR,
                        ModuleType.TYPE_PICTURE,
                        ModuleType.TYPE_MUSIC,
                        ModuleType.TYPE_APP
                };

                int num = types.length;
                boolean skipAppend;
                for (int i = 0; i < num; i++) {
                    skipAppend = false;
                    int count = 0;
                    Composer composer;
                    switch (types[i]) {
                        case ModuleType.TYPE_CONTACT:
                            count = getModulesCount(new ContactBackupComposer(
                                    OldPhonePickupActivity.this));
                            break;
                        case ModuleType.TYPE_MESSAGE:
                            int countSMS = 0;
                            int countMMS = 0;
                            composer = new SmsBackupComposer(OldPhonePickupActivity.this);
                            if (composer.init()) {
                                countSMS = composer.getCount();
                                composer.onEnd();
                            }
                            if (countSMS != 0) {
                                mMessageEnable.add(getString(R.string.message_sms));
                            }
                            composer = new MmsBackupComposer(OldPhonePickupActivity.this);
                            if (composer.init()) {
                                countMMS = composer.getCount();
                                composer.onEnd();
                            }
                            count = countSMS + countMMS;
                            Log.i(TAG, "countSMS = " + countSMS + ", countMMS = " + countMMS);
                            if (countMMS != 0) {
                                mMessageEnable.add(getString(R.string.message_mms));
                            }
                            break;
                        case ModuleType.TYPE_PICTURE:
                            count = getModulesCount(new PictureBackupComposer(
                                    OldPhonePickupActivity.this));
                            break;
                        case ModuleType.TYPE_CALENDAR:
                            count = getModulesCount(new CalendarBackupComposer(
                                    OldPhonePickupActivity.this));
                            break;
                        case ModuleType.TYPE_APP:
                            count = getModulesCount(new AppBackupComposer(
                                    OldPhonePickupActivity.this));
                            break;
                        case ModuleType.TYPE_MUSIC:
                            count = getModulesCount(new MusicBackupComposer(
                                    OldPhonePickupActivity.this));

                            break;
                        case ModuleType.TYPE_NOTEBOOK:
                            count = getModulesCount(new NoteBookBackupComposer(
                                    OldPhonePickupActivity.this));
                            break;
//                    case ModuleType.TYPE_BOOKMARK:
//                        count = getModulesCount(new BookmarkBackupComposer(
//                                OldPhonePickupActivity.this));
//                        break;
                        case ModuleType.TYPE_CALL_LOG:
                            count = getModulesCount(new CallLogBackupComposer(
                                    OldPhonePickupActivity.this));
                            break;
                        default:
                            Log.i(TAG, "Unknown module type: " + types[i]);
                            break;
                    }
                    composer = null;
                    PersonalItemData item = new PersonalItemData(types[i], count);
                    if (!skipAppend) {
                        Log.i(TAG, "Add module type: " + types[i]);
                        mBackupDataList.add(item);
                    } else {
                        Log.i(TAG, "Skip module type: " + types[i]);
                    }
                }
            } catch (SecurityException e) {
                Log.i(TAG, "Permission not satisified");
                e.printStackTrace();
                Utils.exitLockTaskModeIfNeeded(OldPhonePickupActivity.this);
                finish();
            }

            return null;
        }
    }
}
