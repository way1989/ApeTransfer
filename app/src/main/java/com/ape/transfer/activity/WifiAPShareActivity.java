package com.ape.transfer.activity;

import android.graphics.Bitmap;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ape.transfer.R;
import com.ape.transfer.service.WifiApService;
import com.ape.transfer.util.AndroidWebServer;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.QrCodeUtils;
import com.ape.transfer.util.WifiApUtils;
import com.ape.transfer.util.WifiUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import fi.iki.elonen.NanoHTTPD;

public class WifiAPShareActivity extends ApBaseActivity implements WifiApService.OnWifiApStatusListener {

    /* 数据段begin */
    private static final String TAG = "WifiApServerActivity";
    private static final int PORT = 8080;
    @BindView(R.id.tv_ssid)
    TextView tvSsid;
    @BindView(R.id.tv_step2_ip)
    TextView tvStep2Ip;
    @BindView(R.id.iv_code)
    ImageView ivCode;
    @BindView(R.id.rl_loading)
    RelativeLayout rlLoading;

    // NanoHTTPServer
    private NanoHTTPD mNanoHTTPServer;

    @Override
    protected void afterServiceConnected() {
        if (mWifiApService != null) {
            mWifiApService.setOnWifiApStatusListener(this);
            if (canWriteSystem()) {
                mWifiApService.openWifiAp();
            }
        } else {
            Log.i(TAG, "afterServiceConnected service == null");
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_apshare);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        mNanoHTTPServer = new AndroidWebServer(PORT);
        if (canWriteSystem()) {
            permissionWriteSystemGranted();
        } else {
            showRequestWriteSettingsDialog();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mWifiApService != null) {
            if (canWriteSystem()) {
                mWifiApService.openWifiAp();
            }
            if (mWifiApService.isWifiApEnabled()) {
                onWifiApStatusChanged(WifiApUtils.WIFI_AP_STATE_ENABLED);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mNanoHTTPServer.stop();
        if (mWifiApService != null) {
            mWifiApService.closeWifiAp();
            unBindService();
            stopService();
        }
    }


    @Override
    protected void permissionWriteSystemGranted() {
        if (mWifiApService != null) {
            mWifiApService.openWifiAp();
        }
    }

    @Override
    protected void permissionWriteSystemRefused() {
        finish();
    }

    @Override
    public void onWifiApStatusChanged(int statuss) {
        if (statuss == WifiApUtils.WIFI_AP_STATE_ENABLED) {
            // 开启NanoHTTPServer
            try {
                mNanoHTTPServer.start();
                rlLoading.setVisibility(View.GONE);
                String ip = WifiUtils.getLocalIP();
                tvStep2Ip.setText("192.168.43.1:8080");
                WifiConfiguration wifiConfiguration = mWifiApService.getWifiApConfiguration();
                tvSsid.setText(wifiConfiguration.SSID);
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
        } else if (statuss == WifiApUtils.WIFI_AP_STATE_DISABLED ||
                statuss == WifiApUtils.WIFI_AP_STATE_FAILED) {
            finish();
        }
    }

}
