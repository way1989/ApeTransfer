package com.ape.transfer.fragment;


import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ape.transfer.R;
import com.ape.transfer.activity.ApScanActivity;
import com.ape.transfer.activity.InviteFriendActivity;
import com.ape.transfer.activity.MainTransferActivity;
import com.jakewharton.rxbinding3.view.RxView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;
import kotlin.Unit;

public class TransferFragment extends BaseFragment {

    @BindView(R.id.iv_invite)
    ImageView mIvInvite;
    @BindView(R.id.mainSendBtn)
    Button mMainSendBtn;
    @BindView(R.id.mainReceiveBtn)
    Button mMainReceiveBtn;

    public TransferFragment() {
        // Required empty public constructor
    }

    public static TransferFragment newInstance() {
        return new TransferFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_transfer;
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
                .compose(rxPermissions.ensureEachCombined(Manifest.permission.ACCESS_COARSE_LOCATION))
                .subscribe(unit -> startActivity(new Intent(getActivity(), MainTransferActivity.class))));
        mDisposable.add(RxView.clicks(mMainReceiveBtn)
                .throttleFirst(1, TimeUnit.SECONDS)
                .compose(rxPermissions.ensureEachCombined(Manifest.permission.ACCESS_COARSE_LOCATION))
                .subscribe(unit -> startActivity(new Intent(getActivity(), ApScanActivity.class))));
    }

}
