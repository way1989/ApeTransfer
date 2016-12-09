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
import android.view.ViewGroup;
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
import com.ape.transfer.model.TransferTaskFinishEvent;
import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.PreferenceUtil;
import com.ape.transfer.util.RxBus;
import com.ape.transfer.util.SendFloating;
import com.ape.transfer.util.WifiApUtils;
import com.mikepenz.actionitembadge.library.ActionItemBadge;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.ufreedom.floatingview.Floating;
import com.ufreedom.floatingview.FloatingBuilder;
import com.ufreedom.floatingview.FloatingElement;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

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
    private static final String BADGE_NEW = "new";
    private int badgeCount;
    private PhoneItemAdapter mPhoneItemAdapter;
    private ArrayList<FileItem> mFileItems = new ArrayList<>();
    private boolean isSendViewShow;
    private HashMap<String, Peer> mPeerHashMap = new HashMap<>();
    private Floating mRocketAnimFloating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        //标题栏下无阴影
        if (actionBar != null)
            actionBar.setElevation(0f);
        mRocketAnimFloating = new Floating(this);

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
        RxBus.getInstance().toObservable(TransferTaskFinishEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<TransferTaskFinishEvent>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Action1<TransferTaskFinishEvent>() {
                    @Override
                    public void call(TransferTaskFinishEvent taskFinishEvent) {
                        //do some thing
                        badgeCount = 0;
                        supportInvalidateOptionsMenu();
                    }
                });
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
        getMenuInflater().inflate(R.menu.menu_transfer, menu);
        MenuItem menuItem = menu.findItem(R.id.action_history);
        //you can add some logic (hide it if the count == 0)
        ActionItemBadge.update(this, menuItem, getDrawable(R.drawable.ic_history_white),
                ActionItemBadge.BadgeStyles.RED, badgeCount > 0 ? BADGE_NEW : null);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_history);
        if (item != null) {
            ActionItemBadge.update(item, badgeCount > 0 ? BADGE_NEW : null);
        }
        return super.onPrepareOptionsMenu(menu);
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
    protected String getSSID() {
        return "ApeTransfer@" + PreferenceUtil.getInstance().getAlias();
    }

    @Override
    protected void onWifiApStatusChanged(ApStatusEvent event) {
        Log.i(TAG, "onWifiApStatusChanged isAp enabled = " + (event.getStatus() == WifiApUtils.WIFI_AP_STATE_ENABLED));
//        boolean hasInternet = TDevice.hasInternet();
        if (event.getStatus() == WifiApUtils.WIFI_AP_STATE_ENABLED) {
            tvStatus.setText(R.string.waiting_connect);
            tvStatusInfo.setVisibility(View.VISIBLE);
            btnDisconnect.setEnabled(true);
            startP2P();
        } else if (event.getStatus() == WifiApUtils.WIFI_AP_STATE_FAILED
                || event.getStatus() == WifiApUtils.WIFI_AP_STATE_DISABLED) {
            finish();
        }
    }

    @Override
    protected void onPeerChanged(PeerEvent peerEvent) {
        Log.i(TAG, "onPeerChanged... type = " + peerEvent.getType() + ", peer = " + peerEvent.getPeer());
        if (peerEvent.getType() == PeerEvent.ADD) {
            mPeerHashMap.put(peerEvent.getPeer().ip, peerEvent.getPeer());
        } else {
            mPeerHashMap.remove(peerEvent.getPeer().ip);
        }
        Log.d(TAG, "onPeerChanged... process result size = " + mPeerHashMap.size());
        mPhoneItemAdapter.setData(mPeerHashMap.values());
        updateUI(mPeerHashMap.size() > 0);
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
                sendFiles2Peer();
                playSendAnim(view);
                break;
            case R.id.bt_cancel:
            case R.id.btnDisconnect:
                onBackPressed();
                break;
        }
    }

    private void playSendAnim(View view) {
        ImageView imageView = new ImageView(MainTransferActivity.this);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        imageView.setImageResource(R.drawable.rocket);

        FloatingElement floatingElement = new FloatingBuilder()
                .anchorView(view)
                .targetView(imageView)
                .floatingTransition(new SendFloating())
                .build();
        mRocketAnimFloating.startFloating(floatingElement);
    }

    private void sendFiles2Peer() {
        if (mPhoneItemAdapter.getItemCount() < 1) {
            return;
        }
        if (mTransferService != null)
            mTransferService.sendFile(mFileItems);

        RxBus.getInstance().post(new FileEvent(mFileItems));//post message to fragment
        //刷新menu脚标
        badgeCount = mFileItems.size();
        supportInvalidateOptionsMenu();//refresh menu item

        mFileItems.clear();
        updateSendUI();
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
//        //刷新menu脚标
//        badgeCount = mFileItems.size();
//        supportInvalidateOptionsMenu();//refresh menu item
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
