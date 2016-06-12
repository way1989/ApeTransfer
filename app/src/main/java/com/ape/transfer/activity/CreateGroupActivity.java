package com.ape.transfer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ape.transfer.R;
import com.ape.transfer.p2p.p2pcore.P2PManager;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;
import com.ape.transfer.p2p.p2pinterface.Melon_Callback;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.NetworkUtils;
import com.ape.transfer.util.WifiApUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateGroupActivity extends ApBaseActivity {
    private static final String TAG = "CreateGroupActivity";
    @BindView(R.id.iv_warning)
    ImageView ivWarning;
    @BindView(R.id.tv_warning)
    TextView tvWarning;
    @BindView(R.id.iv_warning_arrow)
    ImageView ivWarningArrow;
    @BindView(R.id.tv_qrcode)
    TextView tvQrcode;
    @BindView(R.id.ivQrcode)
    ImageView ivQrcode;
    @BindView(R.id.tv_loading)
    TextView tvLoading;
    @BindView(R.id.loading)
    ProgressBar loading;
    @BindView(R.id.tv_prompt)
    TextView tvPrompt;
    @BindView(R.id.rl_loading)
    RelativeLayout rlLoading;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiApUtils.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(
                        WifiApUtils.EXTRA_WIFI_AP_STATE, WifiApUtils.WIFI_AP_STATE_FAILED);
                handleWifiApStateChanged(state);
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                handleWifiStateChanged(state);
            } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                enableWifiSwitch();
            }

        }
    };
    private WifiManager mWifiManager;
    private WifiApUtils mWifiApUtils;
    // 服务端MAC（本机）
    private String mMAC;
    private boolean isGetMAC;

    private P2PManager mP2PManager;
    private List<P2PNeighbor> neighbors = new ArrayList<>();

    /**
     * scan all net Adapter to get all IP, just return 192
     */
    public static String getLocalIpAddress() throws UnknownHostException {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                        .hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && (inetAddress.getAddress().length == 4)
                            && inetAddress.getHostAddress().startsWith("192.168")) {
                        return inetAddress.getHostAddress();
                    }

                }
            }
        } catch (SocketException ex) {
        }
        return null;
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_create_group;
    }

    @Override
    protected void permissionGranted() {
        if(mWifiManager == null)
            initData();
        if (mWifiApUtils.isWifiApEnabled()) {
            return;
        }

        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo == null || wifiInfo.getMacAddress() == null) {
            // 打开Wifi开关以便Wifi上报MAC
            mWifiManager.setWifiEnabled(true);
            isGetMAC = true;
        } else {
            mMAC = wifiInfo.getMacAddress();
        }

        if (mMAC == null) {
            return;
        }
        isGetMAC = false;
        mWifiApUtils.setWifiApEnabled(mWifiApUtils.generateWifiConfiguration(
                WifiApUtils.AuthenticationType.TYPE_NONE, "ApeTransfer", mMAC, null), true);
    }

    @Override
    protected void permissionRefused() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mWifiApUtils.setWifiApEnabled(null, false);
    }

    private void initData() {
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiApUtils = WifiApUtils.getInstance(mWifiManager);

        IntentFilter intentFilterForAp = new IntentFilter();
        intentFilterForAp.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilterForAp.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intentFilterForAp.addAction(WifiApUtils.WIFI_AP_STATE_CHANGED_ACTION);
        registerReceiver(receiver, intentFilterForAp);
    }

    private void initP2p() {
        mP2PManager = new P2PManager(getApplicationContext());
        P2PNeighbor melonInfo = new P2PNeighbor();
        melonInfo.alias = Build.MODEL;
        String ip = null;
        try {
            ip = getLocalIpAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(ip))
            ip = NetworkUtils.getLocalIp(getApplicationContext());
        melonInfo.ip = ip;

        mP2PManager.start(melonInfo, new Melon_Callback() {
            @Override
            public void Melon_Found(P2PNeighbor melon) {
                if (melon != null) {
                    if (!neighbors.contains(melon))
                        neighbors.add(melon);
                    //randomTextView.addKeyWord(melon.alias);
                    //randomTextView.show();
                }
            }

            @Override
            public void Melon_Removed(P2PNeighbor melon) {
                if (melon != null) {
                    neighbors.remove(melon);
                    //randomTextView.removeKeyWord(melon.alias);
                    //randomTextView.show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if (mP2PManager != null)
            mP2PManager.stop();
    }

    private void handleWifiApStateChanged(int state) {
        switch (state) {
            case WifiApUtils.WIFI_AP_STATE_DISABLING:
                Log.d(TAG, "wifi ap disabling");
                break;
            case WifiApUtils.WIFI_AP_STATE_DISABLED:
                Log.d(TAG, "wifi ap disabled");
                break;
            case WifiApUtils.WIFI_AP_STATE_ENABLING:
                Log.d(TAG, "wifi ap enabling");
                break;
            case WifiApUtils.WIFI_AP_STATE_ENABLED:
                Log.d(TAG, "wifi ap enabled");
                rlLoading.setVisibility(View.GONE);
                break;
            case WifiApUtils.WIFI_AP_STATE_FAILED:
                Log.d(TAG, "wifi ap failed");
                break;
            default:
                Log.e(TAG, "wifi ap state = " + state);
                break;
        }
    }

    private void handleWifiStateChanged(int state) {
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
                mMAC = mWifiManager.getConnectionInfo().getMacAddress();
                // Wifi已上报MAC，关闭Wifi开关
                mWifiManager.setWifiEnabled(false);
                if(isGetMAC && !mWifiApUtils.isWifiApEnabled()){
                    mWifiApUtils.setWifiApEnabled(mWifiApUtils.generateWifiConfiguration(
                            WifiApUtils.AuthenticationType.TYPE_NONE, "ApeTransfer", mMAC, null), true);
                }

                break;

            case WifiManager.WIFI_STATE_UNKNOWN:
                Log.d(TAG, "wifi unknown");
                break;

            default:
                Log.e(TAG, "wifi state = " + state);
                break;
        }
    }

    private void enableWifiSwitch() {
        boolean isAirplaneMode = Settings.Global.getInt(getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        if (!isAirplaneMode) {

        } else {
            finish();
        }

    }
}
