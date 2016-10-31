package com.ape.transfer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ape.transfer.R;
import com.ape.transfer.model.PeerEvent;
import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.service.TransferService;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.PreferenceUtil;
import com.ape.transfer.util.RxBus;
import com.ape.transfer.util.TDevice;
import com.ape.transfer.util.WifiUtils;
import com.trello.rxlifecycle.ActivityEvent;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by android on 16-7-13.
 */
public class NewPhoneConnectedActivity extends BaseActivity {
    public static final String ARGS_SSID = "args_ssid";
    private static final String TAG = "NewPhoneConnectedActivity";
    private static final int MSG_START_P2P = 0;
    private static final int MSG_CONNECT_TIMEOUT = 3;
    private static final long CONNECT_TIMEOUT = 30000L;
    private static final long DELAY_START_P2P = 500L;
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
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_START_P2P:
                    startP2P();
                    break;
                case MSG_CONNECT_TIMEOUT:
                    Toast.makeText(getApplicationContext(), R.string.text_connetion_tip, Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        }
    };
    BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                NetworkInfo.State state = networkInfo.getState();
                handleConnectState(state);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSSID = getIntent().getStringExtra(ARGS_SSID);
        Log.i(TAG, "ssid = " + mSSID);
        if (TextUtils.isEmpty(mSSID)) {
            finish();
            return;
        }
        registerReceiver();
        initDatas();

        connectSSID(mSSID);

        RxBus.getInstance().toObservable(PeerEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<PeerEvent>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Action1<PeerEvent>() {
                    @Override
                    public void call(PeerEvent peerEvent) {
                        //do some thing
                        onPeerChanged(peerEvent);
                    }
                });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
        mHandler.removeCallbacksAndMessages(null);
    }

    private void startP2P() {
        Log.i(TAG, "startP2P...");
        Intent intent = new Intent(this, TransferService.class);
        intent.setAction(TransferService.ACTION_START_P2P);
        startService(intent);
    }

    private void connectSSID(String ssid) {
        boolean isWifiConnected = TDevice.isWifiConnected(getApplicationContext());
        Log.i(TAG, "isWifiEnabled = " + WifiUtils.getInstance().isWifiEnabled() + ", isWifiConnected = "
                + isWifiConnected + ", ssid = " + WifiUtils.getInstance().getSSID()
                + ", ScanResult ssid = " + "\"" + ssid + "\"");
        if (WifiUtils.getInstance().isWifiEnabled() && isWifiConnected && TextUtils.equals(
                WifiUtils.getInstance().getSSID(), "\"" + ssid + "\"")) {
            mHandler.removeMessages(MSG_START_P2P);
            mHandler.sendEmptyMessageDelayed(MSG_START_P2P, DELAY_START_P2P);//不知道为什么连接上后又会断开,然后又连上,所以这里延迟久一点
        } else {
            WifiUtils.getInstance().setWifiEnabled(true);
            WifiUtils.getInstance().connect(WifiUtils.getInstance().generateWifiConfiguration(WifiUtils.AuthenticationType.TYPE_NONE, ssid, null));
        }
        mHandler.removeMessages(MSG_CONNECT_TIMEOUT);
        mHandler.sendEmptyMessageDelayed(MSG_CONNECT_TIMEOUT, CONNECT_TIMEOUT);//连接超时处理
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mWifiStateReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        unregisterReceiver(mWifiStateReceiver);
    }

    private void handleConnectState(NetworkInfo.State state) {
        switch (state) {
            case CONNECTED:
                String ssid = WifiUtils.getInstance().getSSID();
                Log.d(TAG, "wifi connected ssid = " + ssid);
                if (TextUtils.equals(ssid, mSSID)) {
                    mHandler.removeMessages(MSG_START_P2P);
                    //不知道为什么连接上后又会断开,然后又连上,所以这里延迟久一点
                    mHandler.sendEmptyMessageDelayed(MSG_START_P2P, DELAY_START_P2P);
                }
                break;
            case CONNECTING:
                Log.d(TAG, "wifi connecting");
                break;
            case DISCONNECTED:
                Log.d(TAG, "wifi disconnected");
                break;
            case DISCONNECTING:
                Log.d(TAG, "wifi disconnecting");
                break;
            case SUSPENDED:
                Log.d(TAG, "wifi suspended");
                break;
            case UNKNOWN:
                Log.d(TAG, "wifi unknown");
                break;
            default:
                break;
        }
    }

    public void onPeerChanged(PeerEvent event) {
        Log.i(TAG, "onPeerChanged：" + event.getPeer() + ", type = " + event.getType());
        Peer peer = event.getPeer();
        int type = event.getType();
        if (peer != null && type == PeerEvent.ADD) {
            updateUI();
            mHandler.removeMessages(MSG_CONNECT_TIMEOUT);
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
