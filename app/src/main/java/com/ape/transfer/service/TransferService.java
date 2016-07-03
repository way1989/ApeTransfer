package com.ape.transfer.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import com.ape.transfer.model.FileItem;
import com.ape.transfer.p2p.p2pconstant.P2PConstant;
import com.ape.transfer.p2p.p2pcore.P2PManager;
import com.ape.transfer.p2p.p2pentity.P2PFileInfo;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;
import com.ape.transfer.p2p.p2pinterface.NeighborCallback;
import com.ape.transfer.p2p.p2pinterface.ReceiveFileCallback;
import com.ape.transfer.p2p.p2pinterface.SendFileCallback;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.PreferenceUtil;
import com.ape.transfer.util.Util;
import com.ape.transfer.util.WifiUtils;

import java.util.ArrayList;
import java.util.List;

public class TransferService extends Service implements NeighborCallback, ReceiveFileCallback {
    private static final String TAG = "TransferService";
    private IBinder mBinder = new P2PBinder();
    private P2PManager mP2PManager;
    private List<P2PNeighbor> mNeighbors = new ArrayList<>();
    private Callback mCallback;
    private boolean isP2pRunning;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mP2PManager != null) {
            mP2PManager.stop();
            isP2pRunning = false;
        }
    }

    private void init() {
        mP2PManager = new P2PManager(getApplicationContext());
    }

    @Override
    public void NeighborFound(P2PNeighbor neighbor) {
        if (neighbor == null)
            return;
        if (!mNeighbors.contains(neighbor) && !TextUtils.equals(neighbor.ip, mP2PManager.getSelfMeMelonInfo().ip)) {
            mNeighbors.add(neighbor);
            if (mCallback != null) mCallback.onNeighborConnected(neighbor);
        }
    }

    @Override
    public void NeighborRemoved(P2PNeighbor neighbor) {
        if (neighbor != null) {
            mNeighbors.remove(neighbor);
            if (mCallback != null) mCallback.onNeighborDisconnected(neighbor);
        }
    }

    @Override
    public boolean QueryReceiving(P2PNeighbor src, P2PFileInfo[] files) {
        Log.i(TAG, "QueryReceiving....");
        mP2PManager.ackReceive();
        return false;
    }

    @Override
    public void BeforeReceiving(P2PNeighbor src, P2PFileInfo[] files) {
        Log.i(TAG, "BeforeReceiving....");
    }

    @Override
    public void OnReceiving(P2PFileInfo files) {
        Log.i(TAG, "OnReceiving.... percent = " + files.percent);
    }

    @Override
    public void AfterReceiving() {
        Log.i(TAG, "AfterReceiving....");
    }

    @Override
    public void AbortReceiving(int error, String alias) {
        Log.i(TAG, "AbortReceiving....");
    }

    public interface Callback {
        void onNeighborConnected(P2PNeighbor neighbor);

        void onNeighborDisconnected(P2PNeighbor neighbor);
    }

    public class P2PBinder extends Binder {
        public void setCallback(Callback callback) {
            mCallback = callback;
        }

        public P2PNeighbor getMe() {
            P2PNeighbor neighbor = new P2PNeighbor();
            neighbor.alias = PreferenceUtil.getInstance().getAlias();
            neighbor.icon = PreferenceUtil.getInstance().getHead();
            final String ip = WifiUtils.getLocalIP();
            Log.i(TAG, "WifiUtils.getLocalIp = " + ip);
            neighbor.ip = ip;
            return neighbor;
        }

        public void startP2P() {
            Log.i(TAG, "p2p startP2P....");
            final P2PNeighbor me = getMe();
            mP2PManager.start(me, TransferService.this);
            isP2pRunning = true;

            mP2PManager.receiveFile(TransferService.this);
        }

        public void stopP2P() {
            Log.i(TAG, "p2p stopP2P....");
            mP2PManager.stop();
            isP2pRunning = false;
        }

        public boolean isEmpty() {
            return mNeighbors.isEmpty();
        }

        public boolean isP2PRunning() {
            return isP2pRunning;
        }

        public void sendFile(ArrayList<FileItem> fileItems){
            int size = fileItems.size();
            P2PFileInfo[] fileArray = new P2PFileInfo[size];
            for(int i = 0; i < size; i++){
                FileItem item = fileItems.get(i);
                P2PFileInfo info = new P2PFileInfo();
                info.name = Util.getNameFromFilepath(item.path);
                info.type = P2PConstant.TYPE.APP;
                info.size = item.size;
                info.path = item.path;

                fileArray[i] = info;
            }
            mP2PManager.sendFile(new P2PNeighbor[]{mNeighbors.get(0)}, fileArray, new SendFileCallback() {
                @Override
                public void BeforeSending() {
                    Log.i(TAG, "BeforeSending....");
                }

                @Override
                public void OnSending(P2PFileInfo file, P2PNeighbor dest) {
                    Log.i(TAG, "OnSending.... file.percent = " + file.percent);
                }

                @Override
                public void AfterSending(P2PNeighbor dest) {
                    Log.i(TAG, "AfterSending....");
                }

                @Override
                public void AfterAllSending() {
                    Log.i(TAG, "AfterAllSending....");
                }

                @Override
                public void AbortSending(int error, P2PNeighbor dest) {
                    Log.i(TAG, "AbortSending....");
                }
            });
        }
    }
}
