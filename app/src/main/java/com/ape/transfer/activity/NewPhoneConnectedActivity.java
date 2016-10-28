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
import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.service.TransferService;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.PreferenceUtil;
import com.ape.transfer.util.TDevice;
import com.ape.transfer.util.WifiUtils;

import java.util.List;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by android on 16-7-13.
 */
public class NewPhoneConnectedActivity extends BaseActivity {
    public static final String ARGS_SSID = "args_ssid";
    private static final String TAG = "NewPhoneConnectedActivity";
    private static final int MSG_START_P2P = 0;
    private static final int MSG_CONNECT_TIMEOUT = 3;
    private static final long CONNECT_TIMEOUT = 30000;
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
    private TransferService mTransferService;
    private WifiManager mWifiManager;
    private WifiUtils mWifiUtils;
    private String mSSID;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_START_P2P:
                    initP2P();
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
        if (TextUtils.isEmpty(mSSID)) {
            finish();
            return;
        }
        registerReceiver();
        initDatas();

        connectSSID(mSSID);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_new_phone_connected;
    }

    private void initDatas() {
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiUtils = WifiUtils.getInstance(mWifiManager);
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
    }

    private void initP2P() {
        Log.i(TAG, "init P2P mTransferService = " + mTransferService);
        if (mTransferService == null) {
        } else {
            startP2P();
        }
    }

    private void startP2P() {
        Log.i(TAG, "startP2P...");
        mTransferService.startP2P();
        mHandler.removeMessages(MSG_CONNECT_TIMEOUT);
        mHandler.sendEmptyMessageDelayed(MSG_CONNECT_TIMEOUT, CONNECT_TIMEOUT);//连接超时处理
    }

    private void connectSSID(String ssid) {
        boolean isWifiConnected = TDevice.isWifiConnected(getApplicationContext());
        Log.i(TAG, "isWifiConnected = " + isWifiConnected + ", ssid = " + mWifiManager.getConnectionInfo().getSSID()
                + ", ScanResult ssid = " + "\"" + ssid + "\"");
        if (isWifiConnected && TextUtils.equals(mWifiManager.getConnectionInfo().getSSID(), "\"" + ssid + "\"")) {
            mHandler.removeMessages(MSG_START_P2P);
            mHandler.sendEmptyMessageDelayed(MSG_START_P2P, 250L);//不知道为什么连接上后又会断开,然后又连上,所以这里延迟久一点
        } else {
            mWifiUtils.connect(mWifiUtils.generateWifiConfiguration(WifiUtils.AuthenticationType.TYPE_NONE, ssid, null));
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
                mHandler.removeMessages(MSG_CONNECT_TIMEOUT);
                String ssid = mWifiManager.getConnectionInfo().getSSID();
                Log.d(TAG, "wifi connected ssid = " + ssid);
                if (TextUtils.equals(ssid, mSSID)) {
                    mHandler.removeMessages(MSG_START_P2P);
                    mHandler.sendEmptyMessageDelayed(MSG_START_P2P, 250L);//不知道为什么连接上后又会断开,然后又连上,所以这里延迟久一点
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

    public void onPeerChanged(List<Peer> neighbors) {
        Log.i(TAG, "onPeerChanged neighbors = " + neighbors);
        if (!neighbors.isEmpty()) {
//            Intent intent = new Intent(this, NewPhoneExchangeActivity.class);
//            intent.putExtra("neighbor", neighbors.get(0));
//            startActivity(intent);
            updateUI();
        }
    }

    private void updateUI() {
        tvSubTitle.setText(R.string.new_phone_connected_tip);
    }
}
