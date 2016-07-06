package com.ape.transfer.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import com.ape.transfer.R;
import com.ape.transfer.util.Log;

/**
 * Created by android on 16-6-14.
 */
public abstract class RequestWriteSettingsBaseActivity extends BaseActivity {
    private static final String TAG = "RequestWriteSettingsBaseActivity";
    private static final int REQUEST_CODE_WRITE_SETTINGS = 2;
    private Dialog mDialog;

    protected abstract void permissionWriteSystemGranted();

    protected abstract void permissionWriteSystemRefused();

    protected boolean canWriteSystem() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(this));
    }

    protected void showRequestWriteSettingsDialog() {
        if(mDialog != null && mDialog.isShowing()) return;
        if(mDialog == null) {
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
                    Toast.makeText(getApplicationContext(), R.string.permission_refused, Toast.LENGTH_LONG).show();
                    finish();
                }
            });
            mDialog = builder.create();
        }
        mDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
        mDialog = null;
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
