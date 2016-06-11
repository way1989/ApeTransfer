package com.ape.transfer.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ape.transfer.R;
import com.ape.transfer.p2p.p2pcore.P2PManager;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;
import com.ape.transfer.p2p.p2pinterface.Melon_Callback;
import com.ape.transfer.util.NetworkUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateGroupActivity extends BaseActivity {

    @BindView(R.id.iv_warning)
    ImageView ivWarning;
    @BindView(R.id.tv_warning)
    TextView tvWarning;
    @BindView(R.id.iv_warning_arrow)
    ImageView ivWarningArrow;
    @BindView(R.id.tv_step1)
    TextView tvStep1;
    @BindView(R.id.tv_step1_intro)
    TextView tvStep1Intro;
    @BindView(R.id.tv_ssid)
    TextView tvSsid;
    @BindView(R.id.tv_step2)
    TextView tvStep2;
    @BindView(R.id.tv_step2_intro)
    TextView tvStep2Intro;
    @BindView(R.id.tv_step2_ip)
    TextView tvStep2Ip;
    @BindView(R.id.tv_step2_other)
    TextView tvStep2Other;
    @BindView(R.id.tv_code_intro)
    TextView tvCodeIntro;
    @BindView(R.id.tv_code_intro_san)
    TextView tvCodeIntroSan;
    @BindView(R.id.iv_code)
    ImageView ivCode;
    @BindView(R.id.loading)
    ProgressBar loading;
    @BindView(R.id.tv_loading)
    TextView tvLoading;
    @BindView(R.id.tv_prompt)
    TextView tvPrompt;
    @BindView(R.id.rl_loading)
    RelativeLayout rlLoading;

    private P2PManager mP2PManager;
    private List<P2PNeighbor> neighbors = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        ButterKnife.bind(this);
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
        mP2PManager.stop();
    }
}
