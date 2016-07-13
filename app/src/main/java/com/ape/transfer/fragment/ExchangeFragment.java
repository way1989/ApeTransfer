package com.ape.transfer.fragment;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ape.transfer.R;
import com.ape.transfer.activity.InviteFriendActivity;
import com.ape.transfer.activity.QrCodeActivity;
import com.ape.transfer.util.Util;
import com.ape.transfer.zxing.activity.CaptureActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class ExchangeFragment extends Fragment {


    public ExchangeFragment() {
        // Required empty public constructor
    }

    public static ExchangeFragment newInstance() {
        ExchangeFragment fragment = new ExchangeFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exchange, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick({R.id.rl_invite, R.id.mainSendBtn, R.id.mainReceiveBtn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_invite:
                startActivity(new Intent(getActivity(), InviteFriendActivity.class));
                break;
            case R.id.mainSendBtn:
                startActivity(new Intent(getActivity(), QrCodeActivity.class));
                break;
            case R.id.mainReceiveBtn:
                ExchangeFragmentPermissionsDispatcher.startQrCodeScanWithCheck(this);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ExchangeFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


    @NeedsPermission(Manifest.permission.CAMERA)
    void startQrCodeScan() {
        startActivity(new Intent(getActivity(), CaptureActivity.class));
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    void showCameraRationale(final PermissionRequest request) {
        showRationaleDialog(R.string.required_permissions_promo, request);
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    void showCameraDenied() {
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    void showGotoSetting() {
        showNeverAskAgainDialog(R.string.enable_permission_procedure);

    }

    private void showNeverAskAgainDialog(@StringRes int messageResId) {
        new AlertDialog.Builder(getActivity())
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        Util.startSettingsPermission(getContext().getApplicationContext());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(false)
                .setTitle(R.string.permission_never_ask_title)
                .setMessage(messageResId)
                .show();
    }

    private void showRationaleDialog(@StringRes int messageResId, final PermissionRequest request) {
        new AlertDialog.Builder(getActivity())
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setTitle(R.string.permission_title)
                .setMessage(messageResId)
                .show();
    }
}
