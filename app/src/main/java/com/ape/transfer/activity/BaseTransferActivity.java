package com.ape.transfer.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.ape.transfer.R;
import com.ape.transfer.model.PeerEvent;
import com.ape.transfer.service.TransferService;
import com.ape.transfer.service.TransferServiceUtil;
import com.ape.transfer.util.RxBus;
import com.trello.rxlifecycle.ActivityEvent;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by kui.xie on 16-8-5.
 */
public abstract class BaseTransferActivity extends ApBaseActivity implements TransferServiceUtil.Callback {
    protected TransferService.P2PBinder mTransferService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransferServiceUtil.getInstance().setCallback(this);
        TransferServiceUtil.getInstance().bindTransferService();
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

    protected abstract void onPeerChanged(PeerEvent peerEvent);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TransferServiceUtil.getInstance().setCallback(null);
    }

    protected void startP2P() {
        if (mTransferService != null && !mTransferService.isP2PRunning())
            mTransferService.startP2P();
    }

    @Override
    public void onBackPressed() {
        if (mTransferService == null || mTransferService.isEmpty())
            super.onBackPressed();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.connect_dialog_title).setMessage(R.string.transfer_discontent)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTransferService.sendOffLine();
                        mTransferService.stopP2P();
                        TransferServiceUtil.getInstance().unbindTransferService();
                        TransferServiceUtil.getInstance().stopTransferService();
                        stopWifiAp();
                        finish();
                    }
                }).setNegativeButton(android.R.string.cancel, null).show();
    }
}
