package com.ape.transfer.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.ape.transfer.R;
import com.ape.transfer.p2p.p2pcore.P2PManager;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;
import com.ape.transfer.p2p.p2pinterface.NeighborCallback;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.WifiUtils;
import com.ape.transfer.widget.RandomTextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

public class ApScanActivity extends RequestWriteSettingsBaseActivity {
    private static final String TAG = "ApScanActivity";
    private static final String PACKAGE_URI_PREFIX = "package:";
    @BindView(R.id.iv_scan)
    ImageView ivScan;
    @BindView(R.id.iv_head)
    ImageView ivHead;
    @BindView(R.id.mine_tv_name)
    TextView mineTvName;
    @BindView(R.id.rl_phones)
    RandomTextView rlPhones;
    private WifiManager mWifiManager;
    private WifiUtils mWifiUtils;
    private Animation rotateAnim;
    private P2PManager mP2PManager;
    private List<P2PNeighbor> p2PNeighbors = new ArrayList<>();
    //    private boolean isHandleScanResult;
    private boolean isHandleWifiConnected;
    private long mRequestTimeMillis;
    PermissionCallback permissionCallback = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            getScanResults();
        }

        @Override
        public void permissionRefused() {
            final long currentTimeMillis = SystemClock.elapsedRealtime();
            // If the permission request completes very quickly, it must be because the system
            // automatically denied. This can happen if the user had previously denied it
            // and checked the "Never ask again" check box.
            if ((currentTimeMillis - mRequestTimeMillis) < 250L) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ApScanActivity.this);
                builder.setTitle(R.string.request_scan_result_title)
                        .setMessage(R.string.request_scan_result_message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startSettingsPermission();
                            }
                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create().show();

            } else {
                finish();
            }
        }
    };
    private boolean isStartScan;
    private Dialog mRequestScanResultDialog;
    BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                handleWifiState(state);
            } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                Log.d(TAG, "wifi scanned");
                HandleScanResult();
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                NetworkInfo.State state = networkInfo.getState();
                handleConnectState(state);
            }
        }
    };

    private void handleConnectState(NetworkInfo.State state) {
        switch (state) {
            case CONNECTED:
                Log.d(TAG, "wifi connected");
                if (isHandleWifiConnected)
                    return;
                isHandleWifiConnected = true;
                WifiInfo info = mWifiManager.getConnectionInfo();
                String ssid = info != null ? info.getSSID() : "";
                if (ssid.contains("ApeTransfer")) {

                    initP2P();
                }
                break;
            case CONNECTING:
                Log.d(TAG, "wifi connecting");
                break;
            case DISCONNECTED:
                Log.d(TAG, "wifi disconnected");
                break;
            case DISCONNECTING:
                Log.d(TAG, "wifi disconnecting");
                break;
            case SUSPENDED:
                Log.d(TAG, "wifi suspended");
                break;
            case UNKNOWN:
                Log.d(TAG, "wifi unknown");
                break;
            default:
                break;
        }
    }

    private void handleWifiState(int state) {
        switch (state) {
            case WifiManager.WIFI_STATE_DISABLING:
                Log.d(TAG, "wifi disabling");
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                Log.d(TAG, "wifi disabled");
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                Log.d(TAG, "wifi enabling");
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                Log.d(TAG, "wifi enabled");
                if (!isStartScan) {
                    startScanWifi();
                }
                break;
            case WifiManager.WIFI_STATE_UNKNOWN:
                Log.d(TAG, "wifi unknown");
                break;
            default:
                break;
        }
    }

    private void HandleScanResult() {
        Log.i(TAG, "HandleScanResult... canWriteSystem = " + canWriteSystem()
                + ", isStartScan = " + isStartScan);
        if (!canWriteSystem())
            return;
        if (!isStartScan)
            return;
//        if (isHandleScanResult)
//            return;
//        isHandleScanResult = true;
        if (Nammu.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            getScanResults();
        } else {
            if (mRequestScanResultDialog != null && mRequestScanResultDialog.isShowing())
                return;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.request_scan_result_title)
                    .setMessage(R.string.request_scan_result_message).setPositiveButton(android.R.string
                    .ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    tryRequestPermission();
                }
            }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            mRequestScanResultDialog = builder.create();
            mRequestScanResultDialog.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void tryRequestPermission() {
        Nammu.askForPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION, permissionCallback);
        mRequestTimeMillis = SystemClock.elapsedRealtime();
    }

    private void startSettingsPermission() {
        final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse(PACKAGE_URI_PREFIX + getPackageName()));
        startActivity(intent);
    }

    private void getScanResults() {
        List<ScanResult> scanResults = mWifiUtils.getScanResults();
        Log.i(TAG, "getScanResults size = " + scanResults.size());
        for (ScanResult scanResult : scanResults) {
            String ssid = scanResult.SSID;
            if (ssid.contains("ApeTransfer")) {
                Log.i(TAG, "getScanResults ssid = " + ssid);
                String[] alias = ssid.split("@");
                if (alias.length > 1) {
                    rlPhones.addKeyWord(alias[1]);
                    rlPhones.show();
                    return;
                }
//                String capabilities = scanResult.capabilities;
//                WifiUtils.AuthenticationType type = mWifiUtils.getWifiAuthenticationType(capabilities);
//                switch (type) {
//                    case TYPE_NONE:
//                        mWifiUtils.connect(mWifiUtils.generateWifiConfiguration(type, ssid, null));
//                        break;
//                    case TYPE_WEP:
//                    case TYPE_WPA:
//                    case TYPE_WPA2:
//                        mWifiUtils.connect(mWifiUtils.generateWifiConfiguration(type, ssid, "12345678"));
//                    default:
//                        assert (false);
//                        break;
//                }
//                break;
            }
        }
        rlPhones.removeAllViews();
        //have no ssid equal ApeTransfer, rescan system will scan every 15 second
        //isHandleScanResult = false;
        //startScanWifi();
    }

    private void startScanWifi() {
        if (!canWriteSystem())
            return;
        Log.i(TAG, "startScanWifi...");
        if (mWifiUtils.isWifiEnabled()) {
            mWifiUtils.startScan();
            isStartScan = true;
        }
    }

    @Override
    protected void permissionWriteSystemGranted() {
        if (mWifiUtils != null && !mWifiUtils.isWifiOpen())
            mWifiUtils.setWifiEnabled(true);
        startScanWifi();
    }

    @Override
    protected void permissionWriteSystemRefused() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ap_scan);
        ButterKnife.bind(this);
        initData();
        if (canWriteSystem()) {
            permissionWriteSystemGranted();
        } else {
            showRequestWriteSettingsDialog();
        }
    }

    private void initData() {
        rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        rotateAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiUtils = WifiUtils.getInstance(mWifiManager);

        mP2PManager = new P2PManager(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver();
        if (mWifiUtils.isWifiEnabled()) {
            startScanWifi();
        }
        if (rotateAnim != null && ivScan != null)
            ivScan.startAnimation(rotateAnim);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver();
        if (mP2PManager != null && p2PNeighbors.isEmpty())
            mP2PManager.stop();
        if (ivScan != null)
            ivScan.clearAnimation();
    }

    private void initP2P() {
        final String ip = mWifiUtils.getGatewayIP();
        P2PNeighbor neighbor = new P2PNeighbor();
        neighbor.alias = Build.MODEL;
        neighbor.ip = ip;

        Log.i(TAG, "alias = " + neighbor.alias + ", ip = " + ip);

        mP2PManager.start(neighbor, new NeighborCallback() {
            @Override
            public void NeighborFound(P2PNeighbor neighbor) {
                if (neighbor != null) {
                    if (!p2PNeighbors.contains(neighbor) && !TextUtils.equals(neighbor.ip, ip))
                        p2PNeighbors.add(neighbor);
                    rlPhones.addKeyWord(neighbor.alias);
                    rlPhones.show();
                }
            }

            @Override
            public void NeighborRemoved(P2PNeighbor neighbor) {
                if (neighbor != null) {
                    p2PNeighbors.remove(neighbor);
                    rlPhones.removeKeyWord(neighbor.alias);
                    rlPhones.show();
                }
            }
        });
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mWifiStateReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        unregisterReceiver(mWifiStateReceiver);
    }
}
