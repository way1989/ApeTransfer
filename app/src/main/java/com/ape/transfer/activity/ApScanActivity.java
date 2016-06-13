package com.ape.transfer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
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

public class ApScanActivity extends BaseActivity {
    private static final String TAG = "ApScanActivity";
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
    private boolean isHandleScanResult;
    private boolean isHandleWifiConnected;
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
                handleNetworkState(state);
            }
        }
    };

    private void handleNetworkState(NetworkInfo.State state) {
        switch (state) {
            case CONNECTED:
                Log.d(TAG, "wifi connected");
                if (isHandleWifiConnected)
                    return;
                isHandleWifiConnected = true;
                WifiInfo info = mWifiManager.getConnectionInfo();
                String ssid = info != null ? info.getSSID() : "";
                if (ssid.contains("ApeTransfer")) {
                    String gatewayIP = mWifiUtils.getGatewayIP();
                    initP2P(gatewayIP);
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
                break;
            case WifiManager.WIFI_STATE_UNKNOWN:
                Log.d(TAG, "wifi unknown");
                break;
            default:
                break;
        }
    }

    private void HandleScanResult() {
        if (isHandleScanResult)
            return;
        isHandleScanResult = true;
        List<ScanResult> scanResults = mWifiUtils.getScanResults();
        for (ScanResult scanResult : scanResults) {
            String ssid = scanResult.SSID;
            if (ssid.contains("ApeTransfer")) {
                String capabilities = scanResult.capabilities;
                WifiUtils.AuthenticationType type = mWifiUtils.getWifiAuthenticationType(capabilities);
                switch (type) {
                    case TYPE_NONE:
                        mWifiUtils.connect(mWifiUtils.generateWifiConfiguration(type, ssid, null));
                        break;
                    case TYPE_WEP:
                    case TYPE_WPA:
                    case TYPE_WPA2:
                        mWifiUtils.connect(mWifiUtils.generateWifiConfiguration(type, ssid, "12345678"));
                    default:
                        assert (false);
                        break;
                }
                break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ap_scan);
        ButterKnife.bind(this);
        initData();
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
        mWifiUtils.setWifiEnabled(true);
        mWifiUtils.startScan();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver();
        if (mP2PManager != null && p2PNeighbors.isEmpty())
            mP2PManager.stop();
    }

    private void initP2P(final String ip) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (rotateAnim != null) {
            if (hasFocus) ivScan.startAnimation(rotateAnim);
            else ivScan.clearAnimation();
        }
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
