package com.ape.transfer.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ape.transfer.R;
import com.ape.transfer.model.ApStatusEvent;
import com.ape.transfer.model.PeerEvent;
import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.service.TransferService;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.PreferenceUtil;
import com.ape.transfer.util.QrCodeUtils;
import com.ape.transfer.util.TDevice;
import com.ape.transfer.util.WifiApUtils;
import com.ape.transfer.widget.MobileDataWarningContainer;
import com.google.zxing.WriterException;

import butterknife.BindView;

public class QrCodeActivity extends BaseTransferActivity {
    public static final String EXCHANGE_SSID_SUFFIX = "@exchange";
    private static final String TAG = "QrCodeActivity";
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
    @BindView(R.id.mobile_data_warning)
    MobileDataWarningContainer mobileDataWarning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startWifiAp();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_qr_code;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected String getSSID() {
        return "ApeTransfer@" + PreferenceUtil.getInstance().getAlias() + EXCHANGE_SSID_SUFFIX;
    }

    @Override
    protected boolean shouldCloseWifiAp() {
        return mTransferService == null || mTransferService.isEmpty();
    }

    @Override
    protected void onWifiApStatusChanged(ApStatusEvent event) {
        Log.i(TAG, "onWifiApStatusChanged isAp enabled = " + (event.getStatus() == WifiApUtils.WIFI_AP_STATE_ENABLED));
//        boolean hasInternet = TDevice.hasInternet();
        if (event.getStatus() == WifiApUtils.WIFI_AP_STATE_ENABLED) {
            updateUI(event.getSsid());
            startP2P();
        } else if (event.getStatus() == WifiApUtils.WIFI_AP_STATE_FAILED) {
            mobileDataWarning.setVisibility(View.GONE);
            finish();
        }
    }

    private void updateUI(String ssid) {
        try {
            boolean hasInternet = TDevice.hasInternet();
            Log.i(TAG, "updateUI hasInternet = " + hasInternet);
            if (hasInternet)
                mobileDataWarning.setVisibility(View.VISIBLE);
            rlLoading.setVisibility(View.GONE);

            Bitmap qrCode = QrCodeUtils.create2DCode(ssid);
            ivQrcode.setImageBitmap(qrCode);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPeerChanged(PeerEvent peerEvent) {
        Log.i(TAG, "onPeerChanged：" + peerEvent.getPeer() + ", type = " + peerEvent.getType());
        Peer peer = peerEvent.getPeer();
        int type = peerEvent.getType();
        if (peer != null && type == PeerEvent.ADD) {
            Intent intent = new Intent(this, OldPhonePickupActivity.class);
            intent.putExtra("neighbor", peer);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onPostServiceConnected() {

    }
}
