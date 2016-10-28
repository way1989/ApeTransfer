package com.ape.transfer.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;

import com.ape.transfer.App;
import com.ape.transfer.R;
import com.ape.transfer.model.PeerEvent;
import com.ape.transfer.service.TransferService;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.RxBus;
import com.trello.rxlifecycle.ActivityEvent;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by kui.xie on 16-8-5.
 */
public abstract class BaseTransferActivity extends ApBaseActivity {
    private static final String TAG = "BaseTransferActivity";
    protected TransferService mTransferService;
    private boolean mIsBound = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            Log.i(TAG, "onServiceConnected");
            mTransferService = ((TransferService.P2PBinder) service).getService();
            if (mTransferService != null) {
                onPostServiceConnected();
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            Log.i(TAG, "onServiceDisconnected");
            mTransferService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindTransferService();
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

    protected abstract void onPostServiceConnected();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindTransferService();
    }

    protected void startP2P() {
        if (mTransferService != null && !mTransferService.isP2PRunning())
            mTransferService.startP2P();
    }

    @Override
    public void onBackPressed() {
        if (mTransferService == null || mTransferService.isEmpty()) {
            super.onBackPressed();
            return;
        }

        new AlertDialog.Builder(this).setTitle(R.string.connect_dialog_title).setMessage(R.string.transfer_discontent)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTransferService.stopP2P();
                        unbindTransferService();
                        stopTransferService();
                        stopWifiAp();
                        finish();
                    }
                }).setNegativeButton(android.R.string.cancel, null).show();
    }

    /**
     * bind service
     */
    public void bindTransferService() {
        if (mTransferService != null) {
            onPostServiceConnected();
            return;
        }
        startTransferService();//startP2P service first
        Intent bindIntent = new Intent(App.getContext(), TransferService.class);
        mIsBound = bindService(bindIntent, mServiceConnection,
                Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
    }

    /**
     * unbind service
     */
    public void unbindTransferService() {
        if (mTransferService == null)
            return;
        try {
            //java.lang.IllegalArgumentException: Service not registered
            if (mIsBound) {
                unbindService(mServiceConnection);
                mIsBound = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * startP2P service
     */
    public void startTransferService() {
        startService(new Intent(App.getContext(), TransferService.class));
    }

    /**
     * stopP2P service
     */
    public void stopTransferService() {
        stopService(new Intent(App.getContext(), TransferService.class));
    }
}
