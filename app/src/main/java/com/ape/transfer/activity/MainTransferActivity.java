package com.ape.transfer.activity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ape.transfer.R;
import com.ape.transfer.adapter.PagerAdapter;
import com.ape.transfer.adapter.PhoneItemAdapter;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;
import com.ape.transfer.service.TransferService;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.PreferenceUtil;
import com.ape.transfer.util.WifiApUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainTransferActivity extends ApBaseActivity implements TransferService.Callback {
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
    private TransferService.P2PBinder mTransferService;
    private ServiceConnection mServiceCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            Log.i(TAG, "onServiceConnected");
            mTransferService = (TransferService.P2PBinder) service;
            if (mTransferService != null) {
                mTransferService.setCallback(MainTransferActivity.this);
                startP2P();
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            Log.i(TAG, "onServiceDisconnected");
            mTransferService = null;
        }
    };
    private PhoneItemAdapter mPhoneItemAdapter;
    private ArrayList<P2PNeighbor> mNeighbors;

    private void startP2P() {
        mTransferService.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintransfer);
        ButterKnife.bind(this);
        tvMeName.setText(PreferenceUtil.getInstance().getAlias());
        ivMeAvatar.setImageResource(UserInfoActivity.HEAD[PreferenceUtil.getInstance().getHead()]);
        btnDisconnect.setEnabled(false);
        rvPhones.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, false));
        mPhoneItemAdapter = new PhoneItemAdapter(getApplicationContext());
        mNeighbors = new ArrayList<>();
        rvPhones.setAdapter(mPhoneItemAdapter);
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        indicator.setupWithViewPager(pager);
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
            tvStatus.setText(R.string.waiting_connect);
            tvStatusInfo.setVisibility(View.VISIBLE);
            btnDisconnect.setEnabled(true);
            bindService();
        } else if (status == WifiApUtils.WIFI_AP_STATE_DISABLED ||
                status == WifiApUtils.WIFI_AP_STATE_FAILED) {
            finish();
        }
    }

    @Override
    public void onNeighborConnected(P2PNeighbor neighbor) {
        rvPhones.setVisibility(View.VISIBLE);
        rlWaitingConnect.setVisibility(View.INVISIBLE);
        mNeighbors.add(neighbor);
        mPhoneItemAdapter.setDatas(mNeighbors);
    }

    @Override
    public void onNeighborDisconnected(P2PNeighbor neighbor) {
        rvPhones.setVisibility(View.INVISIBLE);
        rlWaitingConnect.setVisibility(View.VISIBLE);
        mNeighbors.remove(neighbor);
        mPhoneItemAdapter.setDatas(mNeighbors);
    }

    private void startService() {
        this.startService(new Intent(this, TransferService.class));
    }

    private void stopService() {
        this.stopService(new Intent(this, TransferService.class));
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

    @OnClick({R.id.bt_send, R.id.bt_cancel, R.id.btnDisconnect})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_send:
                break;
            case R.id.bt_cancel:
                break;
            case R.id.btnDisconnect:
                onBackPressed();
                break;
        }
    }
}
