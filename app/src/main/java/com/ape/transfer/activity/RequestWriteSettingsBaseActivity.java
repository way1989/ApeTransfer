package com.ape.transfer.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.ape.transfer.R;
import com.ape.transfer.util.Log;

/**
 * Created by android on 16-6-14.
 */
public abstract class RequestWriteSettingsBaseActivity extends BaseActivity {
    private static final String TAG = "RequestWriteSettingsBaseActivity";
    private static final int REQUEST_CODE_WRITE_SETTINGS = 2;

    protected abstract void permissionWriteSystemGranted();

    protected abstract void permissionWriteSystemRefused();

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
//            showRequestWriteSettingsDialog();
//        } else {
//            permissionWriteSystemGranted();
//        }
//    }
    protected boolean canWriteSystem(){
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(this));
    }
    protected void showRequestWriteSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.request_write_settings_title).setMessage(R.string.request_write_settings_message)
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
            if (Settings.System.canWrite(this)) {
                Log.i(TAG, "onActivityResult write settings granted");
                permissionWriteSystemGranted();
            } else {
                permissionWriteSystemRefused();
            }
        }
    }
}