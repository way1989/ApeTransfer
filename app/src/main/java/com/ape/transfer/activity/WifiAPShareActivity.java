package com.ape.transfer.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ape.transfer.R;
import com.ape.transfer.model.ApStatusEvent;
import com.ape.transfer.util.AndroidWebServer;
import com.ape.transfer.util.PreferenceUtil;
import com.ape.transfer.util.QrCodeUtils;
import com.ape.transfer.util.WifiApUtils;

import butterknife.BindView;
import fi.iki.elonen.NanoHTTPD;

public class WifiAPShareActivity extends BaseApCreateActivity {

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startWifiAp();
        mNanoHTTPServer = new AndroidWebServer(PORT);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_wifi_apshare;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNanoHTTPServer.stop();
    }

    @Override
    protected String getSSID() {
        return PreferenceUtil.getInstance().getAlias();
    }

    @Override
    protected boolean shouldCloseWifiAp() {
        return true;
    }


    @Override
    protected void onWifiApStatusChanged(ApStatusEvent event) {
        if (event.getStatus() == WifiApUtils.WIFI_AP_STATE_ENABLED) {
            // 开启NanoHTTPServer
            try {
                mNanoHTTPServer.start();
                rlLoading.setVisibility(View.GONE);
                //String ip = WifiUtils.getLocalIP();
                tvStep2Ip.setText("192.168.43.1:8080");
                tvSsid.setText(event.getSsid());
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
        } else if (event.getStatus() == WifiApUtils.WIFI_AP_STATE_FAILED
                || event.getStatus() == WifiApUtils.WIFI_AP_STATE_DISABLED) {
            finish();
        }
    }

}
