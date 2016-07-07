package com.ape.transfer.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ape.transfer.App;
import com.ape.transfer.R;
import com.ape.transfer.adapter.PagerAdapter;
import com.ape.transfer.adapter.PhoneItemAdapter;
import com.ape.transfer.fragment.FileFragment;
import com.ape.transfer.model.FileEvent;
import com.ape.transfer.model.FileItem;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;
import com.ape.transfer.service.TransferService;
import com.ape.transfer.service.TransferServiceUtil;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.PreferenceUtil;
import com.ape.transfer.util.WifiApUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainTransferActivity extends ApBaseActivity implements TransferService.Callback,
        TransferServiceUtil.Callback, FileFragment.OnFileItemChangeListener {
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

    private PhoneItemAdapter mPhoneItemAdapter;
    private ArrayList<P2PNeighbor> mNeighbors;
    private ArrayList<FileItem> mFileItems = new ArrayList<>();
    private boolean isSendViewShow;
    private P2PNeighbor mP2PNeighbor;

    private void startP2P() {
        if (mP2PNeighbor == null &&
                mTransferService != null && !mTransferService.isP2PRunning())
            mTransferService.startP2P();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintransfer);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setElevation(0f);
        TransferServiceUtil.getInstance().setCallback(this);
        TransferServiceUtil.getInstance().bindTransferService();

        mP2PNeighbor = (P2PNeighbor) getIntent().getSerializableExtra("neighbor");

        tvMeName.setText(PreferenceUtil.getInstance().getAlias());
        ivMeAvatar.setImageResource(UserInfoActivity.HEAD[PreferenceUtil.getInstance().getHead()]);
        btnDisconnect.setEnabled(false);
        btSend.setEnabled(false);

        setupWithNeighbor();
        setupWithViewPager();

        if (mP2PNeighbor != null) {
            onNeighborConnected(mP2PNeighbor);
        } else {
            startWifiAp();
        }
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
    public void onBackPressed() {
        if (isOpeningWifiAp) {//正在打开热点,禁用返回键
            Toast.makeText(getApplicationContext(), R.string.waiting_creating_ap, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mNeighbors != null || (mWifiApService != null && mWifiApService.isWifiApEnabled())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.connect_dialog_title).setMessage(R.string.transfer_discontent)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mTransferService != null && !mTransferService.isEmpty()) {
                                mTransferService.stopP2P();
                                TransferServiceUtil.getInstance().stopTransferService();
                                TransferServiceUtil.getInstance().unbindTransferService();
                            }
                            stopWifiAp();
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null).create().show();
            return;
        }

        super.onBackPressed();
    }

    private void setupWithNeighbor() {
        rvPhones.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, false));
        mPhoneItemAdapter = new PhoneItemAdapter(getApplicationContext());
        mNeighbors = new ArrayList<>();
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
            startP2P();
        } else if (status == WifiApUtils.WIFI_AP_STATE_DISABLED ||
                status == WifiApUtils.WIFI_AP_STATE_FAILED) {
            finish();
        }
    }

    @Override
    public void onServiceConnected(TransferService.P2PBinder service) {
        Log.i(TAG, "onServiceConnected... service = " + service);
        mTransferService = service;
        mTransferService.setCallback(MainTransferActivity.this);
        startP2P();
    }

    @Override
    public void onServiceDisconnected() {
        Log.i(TAG, "onServiceConnected... service = " + mTransferService);
        mTransferService = null;
    }

    @Override
    public void onNeighborConnected(P2PNeighbor neighbor) {
        Log.i(TAG, "onNeighborConnected... neighbor.ip = " + neighbor.ip);
        rvPhones.setVisibility(View.VISIBLE);
        rlWaitingConnect.setVisibility(View.INVISIBLE);
        mNeighbors.add(neighbor);
        mPhoneItemAdapter.setDatas(mNeighbors);
        btnDisconnect.setEnabled(true);
        btSend.setEnabled(true);
    }

    @Override
    public void onNeighborDisconnected(P2PNeighbor neighbor) {
        Log.i(TAG, "onNeighborDisconnected... neighbor.ip = " + neighbor.ip);
        if (mNeighbors != null) {
            finish();
            return;
        }
        rvPhones.setVisibility(View.INVISIBLE);
        rlWaitingConnect.setVisibility(View.VISIBLE);
        mNeighbors.remove(neighbor);
        mPhoneItemAdapter.setDatas(mNeighbors);
        btSend.setEnabled(false);
    }


    @OnClick({R.id.bt_send, R.id.bt_cancel, R.id.btnDisconnect})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_send:
                if (mTransferService != null)
                    mTransferService.sendFile(mFileItems);

                EventBus.getDefault().post(new FileEvent(mFileItems));//post message to fragment
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
