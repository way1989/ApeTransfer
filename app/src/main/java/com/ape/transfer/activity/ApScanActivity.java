package com.ape.transfer.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ape.transfer.R;
import com.ape.transfer.p2p.p2pcore.P2PManager;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;
import com.ape.transfer.p2p.p2pinterface.Melon_Callback;
import com.ape.transfer.util.NetworkUtils;
import com.ape.transfer.widget.RandomTextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ApScanActivity extends BaseActivity {

    @BindView(R.id.iv_scan)
    ImageView ivScan;
    @BindView(R.id.iv_head)
    ImageView ivHead;
    @BindView(R.id.mine_tv_name)
    TextView mineTvName;
    @BindView(R.id.rl_phones)
    RandomTextView rlPhones;
    private Animation rotateAnim;
    private P2PManager mP2PManager;
    private List<P2PNeighbor> neighbors = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ap_scan);
        ButterKnife.bind(this);
        rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        rotateAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        initDatas();
    }
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
    private void initDatas() {
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
                    rlPhones.addKeyWord(melon.alias);
                    rlPhones.show();
                }
            }

            @Override
            public void Melon_Removed(P2PNeighbor melon) {
                if (melon != null) {
                    neighbors.remove(melon);
                    rlPhones.removeKeyWord(melon.alias);
                    rlPhones.show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mP2PManager.stop();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (rotateAnim != null) {
            if (hasFocus) ivScan.startAnimation(rotateAnim);
            else ivScan.clearAnimation();
        }
    }
}
