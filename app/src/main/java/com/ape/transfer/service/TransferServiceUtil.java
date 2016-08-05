package com.ape.transfer.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.ape.transfer.App;
import com.ape.transfer.util.Log;

/**
 * Created by android on 16-6-29.
 */
public class TransferServiceUtil {
    private static final String TAG = "TransferServiceUtil";
    private static TransferServiceUtil sTransferServiceUtil;
    private TransferService.P2PBinder mTransferService;
    private Callback mCallback;
    private boolean mIsBound = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            Log.i(TAG, "onServiceConnected");
            mTransferService = (TransferService.P2PBinder) service;
            if (mTransferService != null) {
                if (mCallback != null)
                    mCallback.onServiceConnected(mTransferService);
            } else {
                if (mCallback != null)
                    mCallback.onServiceDisconnected();
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            Log.i(TAG, "onServiceDisconnected");
            mTransferService = null;
            if (mCallback != null)
                mCallback.onServiceDisconnected();
        }
    };

    private TransferServiceUtil() {
    }

    public static TransferServiceUtil getInstance() {
        if (sTransferServiceUtil == null)
            sTransferServiceUtil = new TransferServiceUtil();
        return sTransferServiceUtil;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * bind service
     */
    public void bindTransferService() {
        if (mTransferService != null && mCallback != null) {
            mCallback.onServiceConnected(mTransferService);
            return;
        }
        startTransferService();//startP2P service first
        Intent bindIntent = new Intent(App.getContext(), TransferService.class);
        mIsBound = App.getContext().bindService(bindIntent, mServiceConnection,
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
                App.getContext().unbindService(mServiceConnection);
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
        App.getContext().startService(new Intent(App.getContext(), TransferService.class));
    }

    /**
     * stopP2P service
     */
    public void stopTransferService() {
        App.getContext().stopService(new Intent(App.getContext(), TransferService.class));
    }

    public interface Callback {
        void onServiceConnected(TransferService.P2PBinder service);

        void onServiceDisconnected();
    }
}
