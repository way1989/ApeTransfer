package com.ape.transfer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ape.transfer.App;
import com.ape.transfer.R;
import com.ape.transfer.adapter.PagerAdapter;
import com.ape.transfer.adapter.PhoneItemAdapter;
import com.ape.transfer.fragment.FileFragment;
import com.ape.transfer.model.ApStatusEvent;
import com.ape.transfer.model.FileEvent;
import com.ape.transfer.model.FileItem;
import com.ape.transfer.model.PeerEvent;
import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.service.TransferService;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.PreferenceUtil;
import com.ape.transfer.util.RxBus;
import com.ape.transfer.util.TDevice;
import com.ape.transfer.util.WifiApUtils;
import com.ape.transfer.widget.MobileDataWarningContainer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;

public class MainTransferActivity extends BaseTransferActivity implements
        FileFragment.OnFileItemChangeListener {
    private static final String TAG = "MainTransferActivity";
    @BindView(R.id.indicator)
    TabLayout indicator;
    @BindView(R.id.pager)
    ViewPager pager;
    @BindView(R.id.bt_send)
    Button btSend;
    @BindView(R.id.bt_cancel)
    Button btCancel;
    @BindView(R.id.ll_send_file)
    LinearLayout llSendFile;
    @BindView(R.id.tv_send_size)
    TextView tvSendSize;
    @BindView(R.id.rl_send_file)
    RelativeLayout rlSendFile;
    @BindView(R.id.iv_me_avatar)
    ImageView ivMeAvatar;
    @BindView(R.id.tv_me_name)
    TextView tvMeName;
    @BindView(R.id.rl_me)
    RelativeLayout rlMe;
    @BindView(R.id.iv_divide_right)
    ImageView ivDivideRight;
    @BindView(R.id.btnDisconnect)
    Button btnDisconnect;
    @BindView(R.id.rl_disconnect)
    RelativeLayout rlDisconnect;
    @BindView(R.id.rv_phones)
    RecyclerView rvPhones;
    @BindView(R.id.loading)
    ProgressBar loading;
    @BindView(R.id.tv_status)
    TextView tvStatus;
    @BindView(R.id.tv_status_info)
    TextView tvStatusInfo;
    @BindView(R.id.rl_waiting_connect)
    RelativeLayout rlWaitingConnect;
    @BindView(R.id.rl_device)
    RelativeLayout rlDevice;
    @BindView(R.id.iv_direction)
    ImageView ivDirection;
    @BindView(R.id.root)
    RelativeLayout root;
    @BindView(R.id.mobile_data_warning)
    MobileDataWarningContainer mobileDataWarning;

    private PhoneItemAdapter mPhoneItemAdapter;
    private ArrayList<FileItem> mFileItems = new ArrayList<>();
    private boolean isSendViewShow;
    private Peer mPeer;
    private HashSet<Peer> mPeerList = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        //标题栏下无阴影
        if (actionBar != null)
            actionBar.setElevation(0f);
        //是否有连接上的peer
        if (getIntent().hasExtra(Peer.TAG))
            mPeer = (Peer) getIntent().getSerializableExtra(Peer.TAG);

        tvMeName.setText(PreferenceUtil.getInstance().getAlias());
        ivMeAvatar.setImageResource(UserInfoActivity.HEAD[PreferenceUtil.getInstance().getHead()]);
        btnDisconnect.setEnabled(false);
        btSend.setEnabled(false);

        setupWithNeighbor();
        setupWithViewPager();

        if (mPeer != null) {
            onPeerChanged(new PeerEvent(mPeer, PeerEvent.ADD));
        } else {
            startWifiAp();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_history:
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_maintransfer;
    }

    private void setupWithNeighbor() {
        rvPhones.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, false));
        mPhoneItemAdapter = new PhoneItemAdapter(getApplicationContext());
        rvPhones.setAdapter(mPhoneItemAdapter);
    }

    private void setupWithViewPager() {
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pager.setOffscreenPageLimit(pagerAdapter.getCount() - 1);
        pager.setAdapter(pagerAdapter);
        indicator.setupWithViewPager(pager);
    }

    @Override
    protected boolean shouldCloseWifiAp() {
        return isWifiApEnabled() && (mTransferService == null || mTransferService.isEmpty());
    }

    @Override
    protected String getSSID() {
        return "ApeTransfer@" + PreferenceUtil.getInstance().getAlias();
    }

    @Override
    protected void onWifiApStatusChanged(ApStatusEvent event) {
        Log.i(TAG, "onWifiApStatusChanged isAp enabled = " + (event.getStatus() == WifiApUtils.WIFI_AP_STATE_ENABLED));
//        boolean hasInternet = TDevice.hasInternet();
        if (event.getStatus() == WifiApUtils.WIFI_AP_STATE_ENABLED) {
            boolean hasInternet = TDevice.hasInternet();
            Log.i(TAG, "updateUI hasInternet = " + hasInternet);
            if (hasInternet)
                mobileDataWarning.setVisibility(View.VISIBLE);

            tvStatus.setText(R.string.waiting_connect);
            tvStatusInfo.setVisibility(View.VISIBLE);
            btnDisconnect.setEnabled(true);
            startP2P();
        } else if (event.getStatus() == WifiApUtils.WIFI_AP_STATE_FAILED) {
            mobileDataWarning.setVisibility(View.GONE);
            finish();
        }
    }

    @Override
    protected void onPeerChanged(PeerEvent peerEvent) {
        Log.i(TAG, "onPeerChanged... peerEvent = " + peerEvent);
        if (peerEvent.getType() == PeerEvent.ADD) {
            mPeerList.add(peerEvent.getPeer());
        } else {
            mPeerList.remove(peerEvent.getPeer());
        }
        mPhoneItemAdapter.setDatas(mPeerList);
        updateUI(mPeerList.size() > 0);
    }

    @Override
    protected void onPostServiceConnected() {
        //如果wifi热点已经开启或者没有建立热点的启动，则启动p2p
        if(isWifiApEnabled() || mPeer != null) startP2P();
    }

    private void updateUI(boolean hasNeighbor) {
        if (hasNeighbor) {
            rvPhones.setVisibility(View.VISIBLE);
            rlWaitingConnect.setVisibility(View.INVISIBLE);
            btnDisconnect.setEnabled(true);
            btSend.setEnabled(true);
        } else {
            if (mPeer != null) {
                finish();
                return;
            }
            rvPhones.setVisibility(View.INVISIBLE);
            rlWaitingConnect.setVisibility(View.VISIBLE);
            btSend.setEnabled(false);
        }
    }

    @OnClick({R.id.bt_send, R.id.bt_cancel, R.id.btnDisconnect})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_send:
                if (mPhoneItemAdapter.getItemCount() < 1) {
                    return;
                }
                if (mTransferService != null)
                    mTransferService.sendFile(mFileItems);

                RxBus.getInstance().post(new FileEvent(mFileItems));//post message to fragment
                mFileItems.clear();
                updateSendUI();
                break;
            case R.id.bt_cancel:
                onBackPressed();
                break;
            case R.id.btnDisconnect:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onFileItemChange(FileItem item) {
        Log.i(TAG, "onFileItemChange...");
        if (item.selected) {
            mFileItems.add(item);
        } else {
            mFileItems.remove(item);
        }
        updateSendUI();
    }

    private void updateSendUI() {
        long sumSize = 0L;
        for (FileItem item : mFileItems) {
            sumSize += item.size;
        }
        Log.i(TAG, "updateSendUI... sumSize = " + sumSize);
        final float height = getResources().getDimension(R.dimen.send_layout_margin_bottom);
        final String sendText = getString(R.string.select_text, mFileItems.size(),
                Formatter.formatFileSize(App.getContext(), sumSize));
        if (mFileItems.isEmpty()) {
            if (isSendViewShow) {
                rlSendFile.animate().translationYBy(Math.abs(height));
                isSendViewShow = false;
            }
            tvSendSize.setText(sendText);
        } else {
            if (!isSendViewShow) {
                rlSendFile.animate().translationYBy(height);
                isSendViewShow = true;
            }
            tvSendSize.setText(sendText);
        }
    }

}
