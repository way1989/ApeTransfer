package com.ape.transfer.fragment;


import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ape.transfer.R;
import com.ape.transfer.activity.InviteFriendActivity;
import com.ape.transfer.activity.QrCodeActivity;
import com.ape.transfer.zxing.activity.CaptureActivity;
import com.jakewharton.rxbinding3.view.RxView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;

public class ExchangeFragment extends BaseFragment {


    @BindView(R.id.iv_invite)
    ImageView mIvInvite;
    @BindView(R.id.mainSendBtn)
    Button mMainSendBtn;
    @BindView(R.id.mainReceiveBtn)
    Button mMainReceiveBtn;

    public ExchangeFragment() {
        // Required empty public constructor
    }

    public static ExchangeFragment newInstance() {
        return new ExchangeFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_exchange;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RxPermissions rxPermissions = new RxPermissions(this);
        mDisposable.add(RxView.clicks(mIvInvite)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(unit -> startActivity(new Intent(getActivity(), InviteFriendActivity.class))));
        mDisposable.add(RxView.clicks(mMainSendBtn)
                .throttleFirst(1, TimeUnit.SECONDS)
                .compose(rxPermissions.ensureEachCombined(Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_CALENDAR, Manifest.permission.READ_SMS))
                .subscribe(unit -> startActivity(new Intent(getActivity(), QrCodeActivity.class))));
        mDisposable.add(RxView.clicks(mMainReceiveBtn)
                .throttleFirst(1, TimeUnit.SECONDS)
                .compose(rxPermissions.ensureEachCombined(Manifest.permission.CAMERA))
                .subscribe(unit -> startActivity(new Intent(getActivity(), CaptureActivity.class))));
    }
}
