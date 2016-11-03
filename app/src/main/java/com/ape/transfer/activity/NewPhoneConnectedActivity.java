package com.ape.transfer.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.ape.transfer.R;
import com.ape.transfer.model.PeerEvent;
import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.PreferenceUtil;
import com.ape.transfer.util.TDevice;
import com.ape.transfer.util.WifiUtils;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by android on 16-7-13.
 */
public class NewPhoneConnectedActivity extends BaseWifiConnectActivity {
    public static final String ARGS_SSID = "args_ssid";
    private static final String TAG = "NewPhoneConnectedActivity";
    @BindView(R.id.iv_head_newphone)
    CircleImageView ivHeadNewphone;
    @BindView(R.id.tv_new_phone)
    TextView tvNewPhone;
    @BindView(R.id.tv_old_phone)
    TextView tvOldPhone;
    @BindView(R.id.iv_olphone)
    CircleImageView ivOlphone;
    @BindView(R.id.iv_loading)
    ImageView ivLoading;
    @BindView(R.id.tv_subTitle)
    TextView tvSubTitle;
    private String mSSID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSSID = getIntent().getStringExtra(ARGS_SSID);
        Log.i(TAG, "ssid = " + mSSID);
        if (TextUtils.isEmpty(mSSID)) {
            finish();
            return;
        }
        initDatas();
        connectSSID(mSSID);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_new_phone_connected;
    }

    private void initDatas() {
        tvNewPhone.setText(getString(R.string.newphone_name, PreferenceUtil.getInstance().getAlias()));
        ivHeadNewphone.setImageResource(UserInfoActivity.HEAD[PreferenceUtil.getInstance().getHead()]);
        tvOldPhone.setText(getString(R.string.oldphone_name, mSSID.split("@")[1]));
        AnimationDrawable loadingAnim = (AnimationDrawable) ivLoading.getDrawable();
        showLoading(loadingAnim, true);
    }

    private void showLoading(AnimationDrawable loadingAnim, boolean show) {
        if (loadingAnim != null) {
            if (show) {
                loadingAnim.start();
            } else {
                loadingAnim.stop();
            }
        }
    }


    private void connectSSID(String ssid) {
        boolean isWifiConnected = TDevice.isWifiConnected();
        Log.i(TAG, "isWifiEnabled = " + WifiUtils.getInstance().isWifiEnabled() + ", isWifiConnected = "
                + isWifiConnected + ", ssid = " + WifiUtils.getInstance().getSSID()
                + ", ScanResult ssid = " + "\"" + ssid + "\"");
        if (WifiUtils.getInstance().isWifiEnabled() && isWifiConnected && TextUtils.equals(
                WifiUtils.getInstance().getSSID(), "\"" + ssid + "\"")) {
            mHandler.removeMessages(MSG_START_P2P);
            mHandler.sendEmptyMessageDelayed(MSG_START_P2P, DELAY_START_P2P);//不知道为什么连接上后又会断开,然后又连上,所以这里延迟久一点
        } else {
            WifiUtils.getInstance().setWifiEnabled(true);
            WifiUtils.getInstance().connect(WifiUtils.getInstance()
                    .generateWifiConfiguration(WifiUtils.AuthenticationType.TYPE_NONE, ssid, null));
        }
        mHandler.removeMessages(MSG_TIMEOUT);
        mHandler.sendEmptyMessageDelayed(MSG_TIMEOUT, DURATION_TIMEOUT);//连接超时处理
    }

    @Override
    protected void handleConnectState(NetworkInfo.State state) {
        super.handleConnectState(state);
        if (state == NetworkInfo.State.CONNECTED) {
            String ssid = WifiUtils.getInstance().getSSID();
            Log.d(TAG, "wifi connected ssid = " + ssid + ", mSSID = " + mSSID);
            if (TextUtils.equals(ssid, mSSID)) {
                mHandler.removeMessages(MSG_START_P2P);
                //不知道为什么连接上后又会断开,然后又连上,所以这里延迟久一点
                mHandler.sendEmptyMessageDelayed(MSG_START_P2P, DELAY_START_P2P);
            }
        }
    }

    @Override
    protected boolean isNeedScanWifi() {
        return false;
    }

    @Override
    public void onPeerChanged(PeerEvent event) {
        Log.i(TAG, "onPeerChanged：" + event.getPeer() + ", type = " + event.getType());
        Peer peer = event.getPeer();
        int type = event.getType();
        if (peer != null && type == PeerEvent.ADD) {
            updateUI();
            mHandler.removeMessages(MSG_TIMEOUT);
            Intent intent = new Intent(this, NewPhoneExchangeActivity.class);
            intent.putExtra(Peer.TAG, peer);
            startActivity(intent);
            finish();
        }
    }

    private void updateUI() {
        tvSubTitle.setText(R.string.new_phone_connected_tip);
    }
}
