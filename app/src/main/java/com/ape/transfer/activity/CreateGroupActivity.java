package com.ape.transfer.activity;

import android.graphics.Bitmap;
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
import com.ape.transfer.p2p.p2pinterface.NeighborCallback;
import com.ape.transfer.service.WifiApService;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.QrCodeUtils;
import com.ape.transfer.util.WifiApUtils;
import com.ape.transfer.util.WifiUtils;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateGroupActivity extends ApBaseActivity implements WifiApService.OnWifiApStatusListener {
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

    private P2PManager mP2PManager;
    private List<P2PNeighbor> neighbors = new ArrayList<>();


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
    protected void afterServiceConnected() {
        if (mWifiApService != null) {
            mWifiApService.setOnWifiApStatusListener(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(this)
                    || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
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
        setContentView(R.layout.activity_create_group);
        ButterKnife.bind(this);
        if(canWriteSystem()){
            permissionWriteSystemGranted();
        }else {
            showRequestWriteSettingsDialog();
        }
    }

    private void initP2p() {
        mP2PManager = new P2PManager(getApplicationContext());
        P2PNeighbor melonInfo = new P2PNeighbor();
        melonInfo.alias = Build.MODEL;
        final String ip = WifiUtils.getLocalIP();
        Log.i(TAG, "NetworkUtils.getLocalIp = " + ip);
        melonInfo.ip = ip;

        mP2PManager.start(melonInfo, new NeighborCallback() {
            @Override
            public void NeighborFound(P2PNeighbor neighbor) {
                if (neighbor != null) {
                    if (!neighbors.contains(neighbor) && !TextUtils.equals(neighbor.ip, ip))
                        neighbors.add(neighbor);
                }
            }

            @Override
            public void NeighborRemoved(P2PNeighbor neighbor) {
                if (neighbor != null) {
                    neighbors.remove(neighbor);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mWifiApService != null) {
            if (canWriteSystem()) {
                mWifiApService.openWifiAp();
            }
            if(mWifiApService.isWifiApEnabled()){
                onWifiApStatusChanged(WifiApUtils.WIFI_AP_STATE_ENABLED);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (neighbors.isEmpty()) {
            if(mWifiApService != null) {
                mWifiApService.closeWifiAp();
                unBindService();
                stopService();
            }
            if (mP2PManager != null)
                mP2PManager.stop();
        }
    }

    @Override
    public void onWifiApStatusChanged(int statuss) {
        if (statuss == WifiApUtils.WIFI_AP_STATE_ENABLED) {
            rlLoading.setVisibility(View.GONE);
            Bitmap qrCode = null;
            try {
                qrCode = QrCodeUtils.create2DCode("ApeTransfer");
            } catch (WriterException e) {
                e.printStackTrace();
            }
            if (qrCode != null) {
                ivQrcode.setVisibility(View.VISIBLE);
                ivQrcode.setImageBitmap(qrCode);
            } else {
                ivQrcode.setVisibility(View.GONE);
            }
            initP2p();
        } else if (statuss == WifiApUtils.WIFI_AP_STATE_DISABLED ||
                statuss == WifiApUtils.WIFI_AP_STATE_FAILED) {
            finish();
        }
    }
}
