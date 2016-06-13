package com.ape.transfer.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import com.ape.transfer.service.WifiApService;
import com.ape.transfer.util.Log;

public abstract class ApBaseActivity extends BaseActivity {
    private static final String TAG = "ApBaseActivity";
    private static final int REQUEST_CODE_WRITE_SETTINGS = 2;
    protected boolean isRequestWriteSetting;
    protected WifiApService.WifiApBinder mBackupService;
    private ServiceConnection mServiceCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mBackupService = (WifiApService.WifiApBinder) service;
            afterServiceConnected();
            Log.i(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mBackupService = null;
            Log.i(TAG, "onServiceDisconnected");
        }
    };


    protected abstract void permissionGranted();

    protected abstract void permissionRefused();

    /**
     * after service connected, will can the function, the son activity can do
     * anything that need service connected
     */
    protected abstract void afterServiceConnected();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
            showRequestWriteSettingsDialog();
        } else {
            permissionGranted();
        }
        bindService();
    }

    private void showRequestWriteSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("").setMessage("开关wifi需要申请修改系统设置权限，")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestWriteSettings();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).create().show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestWriteSettings() {
        isRequestWriteSetting = true;
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult requestCode = " + requestCode);
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
            isRequestWriteSetting = false;
            if (Settings.System.canWrite(this)) {
                Log.i(TAG, "onActivityResult write settings granted");
                permissionGranted();
            } else {
                permissionRefused();
            }
        }
    }

    private void bindService() {
        this.getApplicationContext().bindService(new Intent(this, WifiApService.class),
                mServiceCon, Service.BIND_AUTO_CREATE);
    }

    protected void unBindService() {
        if (mBackupService != null) {
            mBackupService.setOnWifiApStatusListener(null);
        }
        try {
            this.getApplicationContext().unbindService(mServiceCon);
        }catch (Exception e){}
    }

    protected void startService() {
        this.startService(new Intent(this, WifiApService.class));
    }

    protected void stopService() {
        if (mBackupService != null) {
            mBackupService.reset();
        }
        this.stopService(new Intent(this, WifiApService.class));
    }

}
