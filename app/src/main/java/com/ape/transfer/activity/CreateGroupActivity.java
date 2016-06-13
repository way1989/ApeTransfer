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
import com.ape.transfer.p2p.p2pinterface.Melon_Callback;
import com.ape.transfer.service.WifiApService;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.NetworkUtils;
import com.ape.transfer.util.QrCodeUtils;
import com.ape.transfer.util.WifiApUtils;
import com.google.zxing.WriterException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
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
    protected void permissionGranted() {
        if (mBackupService != null) {
            mBackupService.openWifiAp();
        }
    }

    @Override
    protected void permissionRefused() {
        finish();
    }

    @Override
    protected void afterServiceConnected() {
        if (mBackupService != null) {
            mBackupService.setOnWifiApStatusListener(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(this)) {
                mBackupService.openWifiAp();
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                mBackupService.openWifiAp();
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
    }

    private void initP2p() {
        mP2PManager = new P2PManager(getApplicationContext());
        P2PNeighbor melonInfo = new P2PNeighbor();
        melonInfo.alias = Build.MODEL;
        String ip = null;
        try {
            ip = getLocalIpAddress();
            Log.i(TAG, "getLocalIpAddress = " + ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(ip))
            ip = NetworkUtils.getLocalIp(getApplicationContext());
        Log.i(TAG, "NetworkUtils.getLocalIp = " + ip);
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
        if (neighbors.isEmpty()) {
            unBindService();
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
