package com.ape.transfer.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.ape.transfer.R;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.OsUtil;


/**
 * Activity to check if the user has required permissions. If not, it will try to prompt the user
 * to grant permissions. However, the OS may not actually prompt the user if the user had
 * previously checked the "Never ask again" checkbox while denying the required permissions.
 */
@SuppressWarnings("ALL")
public class PermissionCheckActivity extends RequestWriteSettingsBaseActivity {
    private static final String TAG = "PermissionCheckActivity";
    private static final int REQUIRED_PERMISSIONS_REQUEST_CODE = 1;
    private static final long AUTOMATED_RESULT_THRESHOLD_MILLLIS = 250;
    private static final String PACKAGE_URI_PREFIX = "package:";
    private long mRequestTimeMillis;
    private Dialog mDialog;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
        mDialog = null;
    }

    private void showRequestPermissionDialog() {
        if(mDialog != null && mDialog.isShowing()) return;
        if(mDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.permission_title_all)
                    .setMessage(R.string.required_permissions_all)
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), R.string.permission_refused, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    })
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tryRequestPermission();
                        }
                    });
            mDialog = builder.create();
        }
        mDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "canWriteSystem = " + canWriteSystem());
        if (canWriteSystem()) {
            if (redirectIfNeeded()) {

            } else {
                showRequestPermissionDialog();
            }
        } else {
            showRequestWriteSettingsDialog();
        }

    }

    private void tryRequestPermission() {
        final String[] missingPermissions = OsUtil.getMissingRequiredPermissions();
        if (missingPermissions.length == 0) {
            redirect();
            return;
        }

        mRequestTimeMillis = SystemClock.elapsedRealtime();
        ActivityCompat.requestPermissions(this, missingPermissions, REQUIRED_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, @NonNull final String permissions[], @NonNull final int[] grantResults) {
        if (requestCode == REQUIRED_PERMISSIONS_REQUEST_CODE) {
            // We do not use grantResults as some of the granted permissions might have been
            // revoked while the permissions dialog box was being shown for the missing permissions.
            if (OsUtil.hasRequiredPermissions()) {
                //Factory.get().onRequiredPermissionsAcquired();
                redirect();
            } else {
                final long currentTimeMillis = SystemClock.elapsedRealtime();
                // If the permission request completes very quickly, it must be because the system
                // automatically denied. This can happen if the user had previously denied it
                // and checked the "Never ask again" check box.
                if ((currentTimeMillis - mRequestTimeMillis) < AUTOMATED_RESULT_THRESHOLD_MILLLIS) {
                    gotoSettings();
                } else {
                    showRequestPermissionDialog();
                }
            }
        }
    }

    private void gotoSettings() {
        new AlertDialog.Builder(PermissionCheckActivity.this).setTitle(R.string.permission_title)
                .setMessage(R.string.enable_permission_procedure)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse(PACKAGE_URI_PREFIX + getPackageName()));
                        startActivity(intent);
                    }
                }).create().show();
    }

    /**
     * Returns true if the redirecting was performed
     */
    private boolean redirectIfNeeded() {
        if (!OsUtil.hasRequiredPermissions()) {
            return false;
        }

        redirect();
        return true;
    }

    private void redirect() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }


    @Override
    protected void permissionWriteSystemGranted() {
        if (redirectIfNeeded()) {

        } else {
            showRequestPermissionDialog();
        }
    }

    @Override
    protected void permissionWriteSystemRefused() {
       showRequestWriteSettingsDialog();
    }
}
