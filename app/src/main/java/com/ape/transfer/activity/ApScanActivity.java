package com.ape.transfer.activity;

import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ape.transfer.R;
import com.ape.transfer.model.PeerEvent;
import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.PreferenceUtil;
import com.ape.transfer.util.TDevice;
import com.ape.transfer.util.WifiUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

import static com.tencent.bugly.crashreport.crash.c.e;

public class ApScanActivity extends BaseWifiConnectActivity implements View.OnClickListener {
    private static final String TAG = "ApScanActivity";
    @BindView(R.id.iv_scan)
    ImageView ivScan;
    @BindView(R.id.iv_head)
    ImageView ivHead;
    @BindView(R.id.mine_tv_name)
    TextView mineTvName;
    @BindView(R.id.rl_phones)
    RelativeLayout rlPhones;
    private Animation rotateAnim;
    private boolean isStartScan;//是否开始搜索
    private boolean isHandleScanResult;//是否处理搜索结果
    private boolean isHandleWifiConnected;//是否处理连接


    @Override
    protected void handleConnectState(NetworkInfo.State state) {
        super.handleConnectState(state);
        if (state == NetworkInfo.State.CONNECTED) {
            if (isHandleWifiConnected)
                return;
            isHandleWifiConnected = true;
            if (!isHandleScanResult)
                return;
            String ssid = WifiUtils.getInstance().getSSID();
            if (ssid.startsWith("ApeTransfer@") && !ssid.endsWith("@exchange")) {
                //不知道为什么连接上后又会断开,然后又连上,所以这里延迟一点
                mHandler.removeMessages(MSG_START_P2P);
                mHandler.sendEmptyMessageDelayed(MSG_START_P2P, DELAY_START_P2P);
            }
        }
    }

    @Override
    protected void handleWifiState(int state) {
        super.handleWifiState(state);
        if (state == WifiManager.WIFI_STATE_ENABLED) {
            if (!isStartScan) {
                startScanWifi();
            }
        }
    }

    @Override
    protected void handleScanResult() {
        super.handleScanResult();
        Log.i(TAG, "handleScanResult... isStartScan = " + isStartScan + ", isHandleScanResult = " + isHandleScanResult);

        if (!isStartScan)
            return;
        if (isHandleScanResult)
            return;
        parseScanResults();
    }

    @Override
    protected boolean isNeedScanWifi() {
        return true;
    }

    private void parseScanResults() {
        List<ScanResult> filterResults = new ArrayList<>();
        List<ScanResult> scanResults = WifiUtils.getInstance().getScanResults();
        Log.i(TAG, "parseScanResults size = " + scanResults.size());
        for (ScanResult scanResult : scanResults) {
            String ssid = scanResult.SSID;
            if (ssid.startsWith("ApeTransfer")) {
                Log.i(TAG, "parseScanResults ssid = " + ssid);
                String[] alias = ssid.split("@");
                if (alias.length > 1) {
                    filterResults.add(scanResult);
                }

            }
        }
        addToRandom(filterResults);
        mHandler.removeMessages(MSG_TIMEOUT);//扫描结束
    }

    private void addToRandom(List<ScanResult> filterResults) {
        rlPhones.removeAllViews();
        if (filterResults.isEmpty())
            return;
        LayoutInflater inflater = getLayoutInflater();
        final int size = Math.min(filterResults.size(), 4);
        for (int i = 0; i < size; i++) {
            View item = inflater.inflate(R.layout.item_ap_scan_activity, rlPhones, false);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            switch (i) {
                case 0:
                    lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    //lp.bottomMargin = Screen.dp(8);
                    break;
                case 1:
                    lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    lp.addRule(RelativeLayout.CENTER_VERTICAL);
                    //lp.rightMargin = Screen.dp(8);
                    break;
                case 2:
                    lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    //lp.topMargin = Screen.dp(8);
                    break;
                case 3:
                    lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    lp.addRule(RelativeLayout.CENTER_VERTICAL);
                    //lp.leftMargin = Screen.dp(8);
                    break;
            }
            item.setLayoutParams(lp);
            item.setTag(filterResults.get(i));
            item.setOnClickListener(this);
            bindDatas(filterResults.get(i), item);
            rlPhones.addView(item);
        }
    }

    private void removeAllFromRadom(boolean reScanWifi) {
        mHandler.removeMessages(MSG_TIMEOUT);
        rlPhones.removeAllViews();
        ivScan.startAnimation(rotateAnim);
        if (reScanWifi)
            startScanWifi();
    }

    private void bindDatas(ScanResult scanResult, View item) {
        TextView name = (TextView) item.findViewById(R.id.tv_name);
        name.setText(scanResult.SSID.split("@")[1]);
    }

    private void startScanWifi() {
        Log.i(TAG, "startScanWifi... isStartScan = " + isStartScan
                + ", isWifiOpen = " + WifiUtils.getInstance().isWifiEnabled());

        if (isStartScan) return;

        if (!WifiUtils.getInstance().isWifiEnabled())
            WifiUtils.getInstance().setWifiEnabled(true);

        isStartScan = WifiUtils.getInstance().startScan();
        isHandleScanResult = false;
        isHandleWifiConnected = false;

        mHandler.removeMessages(MSG_TIMEOUT);
        mHandler.sendEmptyMessageDelayed(MSG_TIMEOUT, DURATION_TIMEOUT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        startScanWifi();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_ap_scan;
    }

    private void initData() {
        rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        rotateAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        ivScan.startAnimation(rotateAnim);

        ivHead.setImageResource(UserInfoActivity.HEAD[PreferenceUtil.getInstance().getHead()]);
        mineTvName.setText(PreferenceUtil.getInstance().getAlias());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeAllFromRadom(false);
    }


    public void onPeerChanged(PeerEvent event) {
        Log.i(TAG, "onPeerChanged：" + event.getPeer() + ", type = " + event.getType());
        Peer peer = event.getPeer();
        int type = event.getType();
        if (peer != null && type == PeerEvent.ADD) {
            mHandler.removeMessages(MSG_TIMEOUT);
            Intent intent = new Intent(this, MainTransferActivity.class);
            intent.putExtra(Peer.TAG, peer);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "rl_phones  onClick....");
        v.setEnabled(false);
        ScanResult scanResult = (ScanResult) v.getTag();
        if (scanResult == null)
            return;
        final String capabilities = scanResult.capabilities;
        final String ssid = scanResult.SSID;
        boolean isWifiConnected = TDevice.isWifiConnected();
        Log.i(TAG, "isWifiConnected = " + isWifiConnected + ", ssid = " + WifiUtils.getInstance().getSSID()
                + ", ScanResult ssid = " + "\"" + ssid + "\"");
        if (isWifiConnected && TextUtils.equals(WifiUtils.getInstance().getSSID(), "\"" + ssid + "\"")) {
            //由于已经连接上了此wifi，所以直接启动p2p
            mHandler.removeMessages(MSG_START_P2P);
            mHandler.sendEmptyMessage(MSG_START_P2P);
        } else {
            WifiUtils.AuthenticationType type = WifiUtils.getInstance().getWifiAuthenticationType(capabilities);
            switch (type) {
                case TYPE_NONE:
                    WifiUtils.getInstance().connect(WifiUtils.getInstance().generateWifiConfiguration(type, ssid, null));
                    break;
                case TYPE_WEP:
                case TYPE_WPA:
                case TYPE_WPA2:
                    //WifiUtils.getInstance().onNeighborConnected(WifiUtils.getInstance().generateWifiConfiguration(type, ssid, "12345678"));
                default:
                    break;
            }
        }
        mHandler.removeMessages(MSG_TIMEOUT);
        mHandler.sendEmptyMessageDelayed(MSG_TIMEOUT, DURATION_TIMEOUT);//连接超时处理

        isHandleScanResult = true;
        isHandleWifiConnected = false;

        ivScan.clearAnimation();
        ivScan.setVisibility(View.INVISIBLE);

        View title = v.findViewById(R.id.tv_connect);
        View progress = v.findViewById(R.id.iv_route);
        ImageView ivHead = (ImageView) v.findViewById(R.id.iv_head);
        title.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        ivHead.setImageResource(R.drawable.unconnect_focus);
        progress.startAnimation(rotateAnim);

    }

}
