package com.ape.transfer.activity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ape.transfer.R;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;
import com.ape.transfer.service.TransferService;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.PreferenceUtil;
import com.ape.transfer.util.QrCodeUtils;
import com.ape.transfer.util.WifiApUtils;
import com.google.zxing.WriterException;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateGroupActivity extends ApBaseActivity implements TransferService.Callback {
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
    private TransferService.P2PBinder mTransferService;
    private ServiceConnection mServiceCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            Log.i(TAG, "onServiceConnected");
            mTransferService = (TransferService.P2PBinder) service;
            if (mTransferService != null) {
                mTransferService.setCallback(CreateGroupActivity.this);
                startP2P();
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            Log.i(TAG, "onServiceDisconnected");
            mTransferService = null;
        }
    };

    private void startP2P() {
        mTransferService.startP2P();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        ButterKnife.bind(this);

    }

    @Override
    protected boolean shouldCloseWifiAp() {
        return mTransferService == null || mTransferService.isEmpty();
    }

    @Override
    protected String getSSID() {
        return "ApeTransfer@" + PreferenceUtil.getInstance().getAlias();
    }

    @Override
    public void onWifiApStatusChanged(int status) {
        super.onWifiApStatusChanged(status);
        if (status == WifiApUtils.WIFI_AP_STATE_ENABLED) {
            rlLoading.setVisibility(View.GONE);
            Bitmap qrCode = null;
            try {
                qrCode = QrCodeUtils.create2DCode(getSSID());
            } catch (WriterException e) {
                e.printStackTrace();
            }
            if (qrCode != null) {
                ivQrcode.setVisibility(View.VISIBLE);
                ivQrcode.setImageBitmap(qrCode);
            } else {
                ivQrcode.setVisibility(View.GONE);
            }
            bindService();
        } else if (status == WifiApUtils.WIFI_AP_STATE_DISABLED ||
                status == WifiApUtils.WIFI_AP_STATE_FAILED) {
            finish();
        }
    }

    private void startService() {
        this.startService(new Intent(this, TransferService.class));
    }

    private void bindService() {
        startService();
        if (mTransferService == null)
            this.getApplicationContext().bindService(new Intent(this, TransferService.class),
                    mServiceCon, Service.BIND_AUTO_CREATE);
    }

    private void unBindService() {
        try {
            this.getApplicationContext().unbindService(mServiceCon);
        } catch (Exception e) {
        }
    }

    @Override
    public void onNeighborChanged(List<P2PNeighbor> neighbors) {
        if(neighbors.isEmpty())
            finish();

    }

}
