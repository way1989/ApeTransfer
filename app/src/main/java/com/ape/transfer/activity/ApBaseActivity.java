package com.ape.transfer.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.ape.transfer.util.Log;

public abstract class ApBaseActivity extends BaseActivity {
    private static final String TAG = "ApBaseActivity";
    private static final int REQUEST_CODE_WRITE_SETTINGS = 2;
    protected boolean isRequestWriteSetting;

    protected abstract int getContentLayout();

    protected abstract void permissionGranted();

    protected abstract void permissionRefused();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentLayout());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
            requestWriteSettings();
        } else {
            permissionGranted();
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onRestart() {
        super.onRestart();
        if(isRequestWriteSetting){
            isRequestWriteSetting = false;
            if (Settings.System.canWrite(this)) {
                permissionGranted();
            } else {
                permissionRefused();
            }
        }
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
            }else{
                permissionRefused();
            }
        }
    }
}
