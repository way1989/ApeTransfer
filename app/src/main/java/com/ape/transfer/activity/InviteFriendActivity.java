package com.ape.transfer.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.ape.transfer.App;
import com.ape.transfer.BuildConfig;
import com.ape.transfer.R;
import com.ape.transfer.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class InviteFriendActivity extends BaseActivity {
    private static final int BLUETOOTH_SHARE_REQUEST_CODE = 4098;
    private static final String TAG = "InviteFriendActivity";
    private static final int REQUEST_CODE_WRITE_SETTINGS = 2;
    Runnable navigateShare = new Runnable() {
        public void run() {
            String url = getString(R.string.share_app);
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            sharingIntent.putExtra(Intent.EXTRA_TEXT, url);
            sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Intent chooserIntent = Intent.createChooser(sharingIntent, null);
            startActivity(chooserIntent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_self);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.bt_bluetooth, R.id.bt_saveTraffic, R.id.bt_more})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_bluetooth:
                sendFile(this);
                break;
            case R.id.bt_saveTraffic:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
                    requestWriteSettings();
                } else {
                    sendFileByWifiAP();
                }
                break;
            case R.id.bt_more:
                view.removeCallbacks(navigateShare);
                view.postDelayed(navigateShare, 250L);
                break;
        }
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
                sendFileByWifiAP();
            }
        }
    }

    private void sendFileByWifiAP() {
        startActivity(new Intent(this, WifiAPShareActivity.class));
    }

    /**
     * 通过蓝牙发送文件
     */
    private void sendFile(Activity activity) {
        PackageManager localPackageManager = activity.getPackageManager();
        Intent intent = new Intent();
        HashMap<String, ActivityInfo> shareItems = new HashMap<>();
        try {
            intent.setAction(Intent.ACTION_SEND);
            String filepath = App.getContext().getPackageManager()
                    .getApplicationInfo(BuildConfig.APPLICATION_ID, 0).sourceDir;
            File file = new File(filepath);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("*/*");
            List<ResolveInfo> resolveInfos = localPackageManager.queryIntentActivities(
                    intent, 0);
            for (ResolveInfo resolveInfo : resolveInfos) {
                ActivityInfo activityInfo = resolveInfo.activityInfo;
                String progressName = activityInfo.applicationInfo.processName;
                if (progressName.contains("bluetooth"))
                    shareItems.put(progressName, activityInfo);
            }

        } catch (Exception e) {
            Log.e(e.getMessage(), e);
        }
        ActivityInfo activityInfo = shareItems.get("com.android.bluetooth");
        if (activityInfo == null)
            activityInfo = shareItems.get("com.mediatek.bluetooth");

        if (activityInfo == null) {
            Iterator<ActivityInfo> iterator = shareItems.values().iterator();
            if (iterator.hasNext())
                activityInfo = iterator.next();
        }
        if (activityInfo != null) {
            intent.setComponent(new ComponentName(activityInfo.packageName, activityInfo.name));
            activity.startActivityForResult(intent, BLUETOOTH_SHARE_REQUEST_CODE);
        }
    }
}
