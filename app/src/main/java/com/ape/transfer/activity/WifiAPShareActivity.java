package com.ape.transfer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ape.transfer.R;
import com.ape.transfer.util.AndroidWebServer;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.QrCodeUtils;
import com.ape.transfer.util.WifiApUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import fi.iki.elonen.NanoHTTPD;

public class WifiAPShareActivity extends BaseActivity {

    /* 数据段begin */
    private final String TAG = "WifiApServerActivity";
    @BindView(R.id.tv_ssid)
    TextView tvSsid;
    @BindView(R.id.tv_step2_ip)
    TextView tvStep2Ip;
    @BindView(R.id.iv_code)
    ImageView ivCode;
    @BindView(R.id.rl_loading)
    RelativeLayout rlLoading;

    private WifiManager mWifiManager;
    private WifiApUtils mWifiApServerManager;
    // NanoHTTPServer
    private NanoHTTPD mNanoHTTPServer;

    private WifiStateReceiver mWifiStateReceiver;
    private WifiApStateReceiver mWifiApStateReceiver;

    // 服务端MAC（本机）
    private String mMAC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_apshare);
        ButterKnife.bind(this);


        initData();
        registerReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mMAC == null) {
            return;
        }

        if (mWifiApServerManager.isWifiApEnabled()) {
            return;
        }

        mWifiApServerManager.setWifiApEnabled(mWifiApServerManager.generateWifiConfiguration(
                WifiApUtils.AuthenticationType.TYPE_NONE, "ApeTransfer", mMAC, null), true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mWifiApServerManager.setWifiApEnabled(null, false);
        mNanoHTTPServer.stop();
    }

    private void initData() {
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiApServerManager = WifiApUtils.getInstance(mWifiManager);
        mNanoHTTPServer = new AndroidWebServer(8080);

        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo == null || wifiInfo.getMacAddress() == null) {
            // 打开Wifi开关以便Wifi上报MAC
            mWifiManager.setWifiEnabled(true);
        } else {
            mMAC = wifiInfo.getMacAddress();
        }
    }

    private void registerReceiver() {
        mWifiStateReceiver = new WifiStateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mWifiStateReceiver, intentFilter);

        mWifiApStateReceiver = new WifiApStateReceiver();
        IntentFilter intentFilterForAp = new IntentFilter();
        intentFilterForAp.addAction(WifiApUtils.WIFI_AP_STATE_CHANGED_ACTION);
        registerReceiver(mWifiApStateReceiver, intentFilterForAp);
    }

    private void unregisterReceiver() {
        unregisterReceiver(mWifiStateReceiver);
        unregisterReceiver(mWifiApStateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver();
    }

    private String getIpAccess() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        final String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        return "http://" + formatedIpAddress + ":";
    }

    /* 内部类begin */
    private class WifiApStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(WifiApUtils.EXTRA_WIFI_AP_STATE, -1);
            // 判断Wifi AP状态（由于在不同的API level中，值的定义不一致，反射获取较麻烦，此处直接用magic number判断）
            switch (state) {
                case 0:
                case WifiApUtils.WIFI_AP_STATE_DISABLING:
                    Log.d(TAG, "wifi ap disabling");
                    break;

                case 1:
                case WifiApUtils.WIFI_AP_STATE_DISABLED:
                    Log.d(TAG, "wifi ap disabled");

                    // 关闭NanoHTTPServer
                    mNanoHTTPServer.stop();
                    break;

                case 2:
                case WifiApUtils.WIFI_AP_STATE_ENABLING:
                    Log.d(TAG, "wifi ap enabling");
                    break;

                case 3:
                case WifiApUtils.WIFI_AP_STATE_ENABLED:
                    Log.d(TAG, "wifi ap enabled");

                    // 开启NanoHTTPServer
                    try {
                        mNanoHTTPServer.start();
                        rlLoading.setVisibility(View.GONE);
                        tvStep2Ip.setText("192.168.43.1:8080");
                        tvSsid.setText(mWifiApServerManager.getWifiApConfiguration().SSID);
                        Bitmap qrCode = QrCodeUtils.create2DCode("http://192.168.43.1:8080");
                        if (qrCode != null) {
                            ivCode.setVisibility(View.VISIBLE);
                            ivCode.setImageBitmap(qrCode);
                        } else {
                            ivCode.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;

                case 4:
                case WifiApUtils.WIFI_AP_STATE_FAILED:
                    Log.d(TAG, "wifi ap failed");
                    break;

                default:
                    Log.e(TAG, "wifi ap state = " + state);
                    break;
            }
        }
    }

    private class WifiStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
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

                        break;

                    case WifiManager.WIFI_STATE_UNKNOWN:
                        Log.d(TAG, "wifi unknown");
                        break;

                    default:
                        Log.e(TAG, "wifi state = " + state);
                        break;

                }
            }
        }
    }
    /* 内部类end */
}
