package com.ape.transfer.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ape.transfer.R;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;
import com.ape.transfer.service.TransferService;
import com.ape.transfer.service.TransferServiceUtil;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.PreferenceUtil;
import com.ape.transfer.util.QrCodeUtils;
import com.ape.transfer.util.TDevice;
import com.ape.transfer.util.WifiApUtils;
import com.ape.transfer.widget.MobileDataWarningContainer;
import com.google.zxing.WriterException;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QrCodeActivity extends ApBaseActivity implements TransferService.Callback,
        TransferServiceUtil.Callback {
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

    private TransferService.P2PBinder mTransferService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);
        ButterKnife.bind(this);
        startWifiAp();
        TransferServiceUtil.getInstance().setCallback(this);
        TransferServiceUtil.getInstance().bindTransferService();
    }

    @Override
    public void onBackPressed() {
        if ((mWifiApService != null && mWifiApService.isWifiApEnabled())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.connect_dialog_title).setMessage(R.string.transfer_discontent)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mTransferService != null && !mTransferService.isEmpty()) {
                                if (mWifiApService.isWifiApEnabled()) {
                                    mTransferService.sendOffLine();
                                }
                                mTransferService.stopP2P();
                                TransferServiceUtil.getInstance().unbindTransferService();
                                TransferServiceUtil.getInstance().stopTransferService();
                            }
                            stopWifiAp();
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null).show();
            return;
        }

        super.onBackPressed();
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
        return true;
    }

    private void startP2P() {
        if (mTransferService != null && !mTransferService.isP2PRunning())
            mTransferService.startP2P();
    }

    @Override
    public void onWifiApStatusChanged(int status) {
        super.onWifiApStatusChanged(status);
        Log.i(TAG, "onWifiApStatusChanged isAp enabled = " + (status == WifiApUtils.WIFI_AP_STATE_ENABLED));
//        boolean hasInternet = TDevice.hasInternet();
        if (status == WifiApUtils.WIFI_AP_STATE_ENABLED) {
            updateUI();
            startP2P();
        } else if (status == WifiApUtils.WIFI_AP_STATE_DISABLED ||
                status == WifiApUtils.WIFI_AP_STATE_FAILED) {
            mobileDataWarning.setVisibility(View.GONE);
            finish();
        }
    }

    private void updateUI() {
        try {
            boolean hasInternet = TDevice.hasInternet();
            Log.i(TAG, "updateUI hasInternet = " + hasInternet);
            if (hasInternet)
                mobileDataWarning.setVisibility(View.VISIBLE);
            rlLoading.setVisibility(View.GONE);

            WifiConfiguration wifiConfiguration = mWifiApService.getWifiApConfiguration();
            String ssid = wifiConfiguration.SSID;
            Bitmap qrCode = QrCodeUtils.create2DCode(ssid);
            ivQrcode.setImageBitmap(qrCode);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceConnected(TransferService.P2PBinder service) {
        Log.i(TAG, "onServiceConnected... service = " + service);
        mTransferService = service;
        mTransferService.setCallback(this);
    }

    @Override
    public void onServiceDisconnected() {
        Log.i(TAG, "onServiceConnected... service = " + mTransferService);
        mTransferService = null;
    }

    @Override
    public void onNeighborChanged(List<P2PNeighbor> neighbors) {
        Log.i(TAG, "onNeighborChanged... neighbors = " + neighbors);
        if (!neighbors.isEmpty()) {
            Intent intent = new Intent(this, OldPhonePickupActivity.class);
            intent.putExtra("neighbor", neighbors.get(0));
            startActivity(intent);
        }
        finish();
    }
}
