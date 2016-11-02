package com.ape.transfer.activity;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ape.backuprestore.BackupEngine;
import com.ape.backuprestore.BackupService;
import com.ape.backuprestore.PersonalItemData;
import com.ape.backuprestore.RecordXmlComposer;
import com.ape.backuprestore.RecordXmlInfo;
import com.ape.backuprestore.ResultDialog;
import com.ape.backuprestore.modules.Composer;
import com.ape.backuprestore.utils.Constants;
import com.ape.backuprestore.utils.ModuleType;
import com.ape.backuprestore.utils.StorageUtils;
import com.ape.backuprestore.utils.Utils;
import com.ape.transfer.R;
import com.ape.transfer.adapter.OldPhonePickupAdapter;
import com.ape.transfer.fragment.loader.BackupDataLoader;
import com.ape.transfer.fragment.loader.BaseLoader;
import com.ape.transfer.model.ApStatusEvent;
import com.ape.transfer.model.PeerEvent;
import com.ape.transfer.p2p.beans.TransferFile;
import com.ape.transfer.p2p.util.Constant;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.PreferenceUtil;
import com.ape.transfer.util.TDevice;
import com.ape.transfer.util.Util;
import com.ape.transfer.util.WifiApUtils;
import com.ape.transfer.widget.MobileDataWarningContainer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;

import static com.ape.backuprestore.utils.FileUtils.deleteFileOrFolder;
import static com.ape.transfer.activity.QrCodeActivity.EXCHANGE_SSID_SUFFIX;

/**
 * Created by android on 16-7-13.
 */
public class OldPhonePickupActivity extends BaseTransferActivity implements LoaderManager.LoaderCallbacks<BaseLoader.Result>,
        OldPhonePickupAdapter.OnItemClickListener, BackupService.OnBackupStatusListener {
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
    ArrayList<TransferFile> mSendFiles = new ArrayList<>();
    private OldPhonePickupAdapter mAdapter;
    private boolean mIsShowWarning = true;
    private ServiceConnection mServiceCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            Log.i(TAG, "onServiceConnected");
            mBackupService = (BackupService.BackupBinder) service;
            if (mBackupService != null)
                mBackupService.setOnBackupChangedListner(OldPhonePickupActivity.this);
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            Log.i(TAG, "onServiceDisconnected");
            if (mBackupService != null)
                mBackupService.setOnBackupChangedListner(null);
            mBackupService = null;
        }
    };
    private String mBackupFolderPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindBackupService();

        startWifiAp();//启动wifi热点
        mAdapter = new OldPhonePickupAdapter(getApplicationContext(), this);
        rvDataCategory.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
        rvDataCategory.setAdapter(mAdapter);
        getSupportLoaderManager().initLoader(0, null, OldPhonePickupActivity.this);
        setButtonsEnable(false);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_old_phone_pickup;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unBindBackupService();
        if (mBackupService != null && mBackupService.getState() == Constants.State.INIT) {
            stopService();
        }
    }

    public void setButtonsEnable(boolean isEnabled) {
        btnSure.setEnabled(isEnabled);
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
        setButtonsEnable(!getSelectedItemList().isEmpty());
        storageView.setText(getSelectedItemList().isEmpty() ?
                getResources().getQuantityString(R.plurals.total_selected, 0, 0) :
                getResources().getQuantityString(R.plurals.total_selected, getSelectedItemList().size(),
                        getSelectedItemList().size()));
    }

    @Override
    public void onComposerChanged(final Composer composer) {
        if (composer == null) {
            Log.e(TAG, "onComposerChanged: error[composer is null]");
            return;
        }
        Log.i(TAG, "onComposerChanged: type = " + composer.getModuleType() + "Max = " + composer.getCount());
        if (mBackupService == null || mBackupService.getState() == Constants.State.PAUSE) {
            Log.e(TAG, "onComposerChanged: error[mBackupService is null] or [state is pause]");
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog == null) createProgressDlg();
                Log.d(TAG, "mProgressDialog : " + mProgressDialog);
                String msg = getString(R.string.backuping, ModuleType.getModuleStringFromType(
                        getApplicationContext(), composer.getModuleType()));
                mProgressDialog.setMessage(msg);
                mProgressDialog.setMax(composer.getCount());
                mProgressDialog.setProgress(0);
                try {
                    if (!mProgressDialog.isShowing()) mProgressDialog.show();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

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
    public void onBackupEnd(BackupEngine.BackupResultType resultCode,
                            ArrayList<ResultDialog.ResultEntity> resultRecord) {
        if (resultCode != BackupEngine.BackupResultType.Cancel) {
            RecordXmlInfo backupInfo = new RecordXmlInfo();
            backupInfo.setRestore(false);
            backupInfo.setDevice(Utils.getPhoneSearialNumber());
            backupInfo.setTime(String.valueOf(System.currentTimeMillis()));
            RecordXmlComposer xmlComposer = new RecordXmlComposer();
            xmlComposer.startCompose();
            xmlComposer.addOneRecord(backupInfo);
            xmlComposer.endCompose();
            Log.i(TAG, "onBackupEnd.. write xml = " + xmlComposer.getXmlInfo() + ", dir = " + mBackupFolderPath);
            if (!TextUtils.isEmpty(mBackupFolderPath)) {
                Utils.writeToFile(xmlComposer.getXmlInfo(), mBackupFolderPath + File.separator
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
            // TODO: 16-10-31 开始传输备份数据
            Toast.makeText(getApplicationContext(), "back result = "
                    + (result == BackupEngine.BackupResultType.Success), Toast.LENGTH_SHORT).show();
            send2Peer();
        } else {
            stopService();
        }
    }

    private void send2Peer() {
        File folder = new File(mBackupFolderPath);
        File[] files = null;
        if (folder.exists()) {
            files = folder.listFiles();
        }
        if (files != null && files.length > 0) {
            Log.d(TAG, "[processClickStart] DLG_BACKUP_CONFIRM_OVERWRITE Here! ");
            for (File file : files) {
                add2TransferList(file);
            }
        }
        Log.i(TAG, "send files size = " + mSendFiles.size());
        if(mTransferService != null)
            mTransferService.sendBackupFile(mSendFiles);
    }

    private void add2TransferList(File file) {
        if (!file.exists()) return;
        if (file.isFile()) {
            TransferFile info = new TransferFile();
            info.name = file.getName();
            info.type = Constant.TYPE.BACKUP;
            info.size = file.length();
            info.path = file.getAbsolutePath();

            //info.wifiMac = mNeighbors.get(0).wifiMac;
            info.md5 = Util.getFileMD5(file);
            info.lastModify = file.lastModified();
            info.createTime = System.currentTimeMillis();
            info.status = TransferFile.Status.STATUS_READY;
            info.read = 1;
            info.deleted = 0;
            mSendFiles.add(info);
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files)
                add2TransferList(f);
        }
    }

    @Override
    public void onBackupErr(IOException e) {
        if (mBackupService != null && mBackupService.getState() != Constants.State.INIT
                && mBackupService.getState() != Constants.State.FINISH) {
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
        }
        return mProgressDialog;
    }

    private void startBackup() {
        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        //String folderName = dateFormat.format(new Date(System.currentTimeMillis()));

        String path = StorageUtils.getBackupPath();
        if (path == null) {
            Toast.makeText(getApplicationContext(), "can not get the storage...", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (StorageUtils.getAvailableSize(path) <= StorageUtils.MINIMUM_SIZE) {
            // no space
            Log.d(TAG, "SDCard is full");
            Toast.makeText(getApplicationContext(), "storage is full...", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mBackupFolderPath = path;
        Log.d(TAG, "[processClickStart] mBackupFolderPath is " + mBackupFolderPath);
        File folder = new File(mBackupFolderPath);
        File[] files = null;
        if (folder.exists()) {
            files = folder.listFiles();
        }
        if (files != null && files.length > 0) {
            Log.d(TAG, "[processClickStart] DLG_BACKUP_CONFIRM_OVERWRITE Here! ");
            for (File file : files) {
                deleteFileOrFolder(file);
            }
        }
        startPersonalDataBackup();
    }

    private void startPersonalDataBackup() {
        if (TextUtils.isEmpty(mBackupFolderPath)) {
            Toast.makeText(getApplicationContext(), "back path is null", Toast.LENGTH_SHORT).show();
            return;
        }

        final ArrayList<Integer> selectedItemList = getSelectedItemList();
        if (selectedItemList.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please select on or more items", Toast.LENGTH_SHORT).show();
            return;
        }

        startService();
        if (mBackupService != null) {
            mBackupService.setBackupModelList(selectedItemList);
            if (selectedItemList.contains(ModuleType.TYPE_MESSAGE)) {
                ArrayList<String> params = new ArrayList<>();
                params.add(Constants.ModulePath.NAME_SMS);
                params.add(Constants.ModulePath.NAME_MMS);
                mBackupService.setBackupItemParam(ModuleType.TYPE_MESSAGE, params);
            }
            boolean result = mBackupService.startBackup(mBackupFolderPath);
            if (!result) {
                String path = StorageUtils.getBackupPath();
                if (path == null) {
                    // no sdcard
                    Log.d(TAG, "SDCard is removed");
                } else if (StorageUtils.getAvailableSize(path) <= StorageUtils.MINIMUM_SIZE) {
                    // no space
                    Log.d(TAG, "SDCard is full");
                } else {
                    Log.e(TAG, "Unknown error");
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

    @Override
    protected void onWifiApStatusChanged(ApStatusEvent event) {
        Log.i(TAG, "onWifiApStatusChanged isAp enabled = " + (event.getStatus() == WifiApUtils.WIFI_AP_STATE_ENABLED));
        if (event.getStatus() == WifiApUtils.WIFI_AP_STATE_ENABLED) {
            if (TDevice.hasInternet()) mobileDataWarning.setVisibility(View.VISIBLE);
            startP2P();
        } else if (event.getStatus() == WifiApUtils.WIFI_AP_STATE_FAILED) {
            mobileDataWarning.setVisibility(View.GONE);
            finish();
        }
    }

    @Override
    protected String getSSID() {
        return "ApeTransfer@" + PreferenceUtil.getInstance().getAlias() + EXCHANGE_SSID_SUFFIX;
    }

    @Override
    protected void onPeerChanged(PeerEvent peerEvent) {
    }

    @Override
    public Loader<BaseLoader.Result> onCreateLoader(int id, Bundle args) {
        return new BackupDataLoader(getApplicationContext());
    }

    @Override
    public void onLoadFinished(Loader<BaseLoader.Result> loader, BaseLoader.Result data) {
        if (!data.lists.isEmpty()) {
            Log.i(TAG, "updateData... mBackupDataList.size = " + data.lists.size());
            mAdapter.setDatas(data.lists);
            setButtonsEnable(!getSelectedItemList().isEmpty());
            storageView.setText(getSelectedItemList().isEmpty() ?
                    getResources().getQuantityString(R.plurals.total_selected, 0, 0) :
                    getResources().getQuantityString(R.plurals.total_selected, getSelectedItemList().size(),
                            getSelectedItemList().size()));
        } else {
            setButtonsEnable(false);
            storageView.setText(getResources().getQuantityString(R.plurals.total_selected, 0, 0));
        }
    }

    @Override
    public void onLoaderReset(Loader<BaseLoader.Result> loader) {
        mAdapter.reset();
    }

    private void bindBackupService() {
        bindService(new Intent(this, BackupService.class), mServiceCon, Service.BIND_AUTO_CREATE);
    }

    private void unBindBackupService() {
        try {
            unbindService(mServiceCon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void startService() {
        this.startService(new Intent(this, BackupService.class));
    }

    protected void stopService() {
        if (mBackupService != null) mBackupService.reset();
        stopService(new Intent(this, BackupService.class));
    }

}
