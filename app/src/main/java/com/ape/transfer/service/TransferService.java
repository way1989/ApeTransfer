package com.ape.transfer.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ape.transfer.BuildConfig;
import com.ape.transfer.model.FileItem;
import com.ape.transfer.model.PeerEvent;
import com.ape.transfer.model.TransferFileEvent;
import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.p2p.beans.TransferFile;
import com.ape.transfer.p2p.callback.PeerCallback;
import com.ape.transfer.p2p.callback.ReceiveFileCallback;
import com.ape.transfer.p2p.callback.SendFileCallback;
import com.ape.transfer.p2p.core.P2PManager;
import com.ape.transfer.p2p.util.Constant;
import com.ape.transfer.provider.DeviceHistory;
import com.ape.transfer.provider.TaskHistory;
import com.ape.transfer.provider.TransferDB;
import com.ape.transfer.util.Log;
import com.ape.transfer.util.PreferenceUtil;
import com.ape.transfer.util.Util;
import com.ape.transfer.util.WifiUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TransferService extends Service {
    public static final String ACTION_START_P2P = "com.ape.transfer.startP2P";
    private static final String TAG = "TransferService";
    private IBinder mBinder = new P2PBinder();
    private P2PManager mP2PManager;
    private List<Peer> mNeighbors = new ArrayList<>();
    private Callback mCallback;
    private boolean isP2pRunning;
    private PeerCallback mPeerCallback = new PeerCallback() {
        @Override
        public void onPeerFound(Peer peer) {
            if (peer == null)
                return;
            if (!mNeighbors.contains(peer)) {
                peer.lastTime = System.currentTimeMillis();
                DeviceHistory.getInstance().addDevice(peer);
                mNeighbors.add(peer);
                if (mCallback != null) mCallback.onPeerChanged(mNeighbors);
                EventBus.getDefault().post(new PeerEvent(peer, PeerEvent.ADD));
            }
        }

        @Override
        public void onPeerRemoved(Peer peer) {
            if (peer != null) {
                mNeighbors.remove(peer);
                if (mCallback != null) mCallback.onPeerChanged(mNeighbors);
                EventBus.getDefault().post(new PeerEvent(peer, PeerEvent.REMOVED));
            }
        }
    };

    private SendFileCallback mSendFileCallback = new SendFileCallback() {
        @Override
        public void onPreSending() {
            Log.i(TAG, "onPreSending....");
        }

        @Override
        public void onSending(TransferFile file, Peer dest) {
            Log.i(TAG, "onSending...." + file.name + ", position = " + file.position
                    + ", sumSize = " + file.size);
            file.status = TransferFile.Status.STATUS_SENDING;
            TaskHistory.getInstance().updateFileInfo(file);
            EventBus.getDefault().post(new TransferFileEvent(file));
        }

        @Override
        public void onPostSending(Peer dest) {
            Log.i(TAG, "onPostSending.... peer = " + dest);
        }

        @Override
        public void onPostAllSending() {
            Log.i(TAG, "onPostAllSending....");
        }

        @Override
        public void onAbortSending(int error, Peer dest) {
            Log.i(TAG, "onAbortSending.... error = " + error + ", peer = " + dest);
        }
    };
    private ReceiveFileCallback mReceiveFileCallback = new ReceiveFileCallback() {
        @Override
        public void onPreReceiving(Peer src, TransferFile[] files) {
            Log.i(TAG, "onPreReceiving....");
            for (TransferFile fileInfo : files) {
                fileInfo.wifiMac = src.wifiMac;
                fileInfo.direction = TransferFile.Direction.DIRECTION_RECEIVE;
                fileInfo.deleted = 0;
                fileInfo.status = TransferFile.Status.STATUS_READY;
                fileInfo.read = 0;
                fileInfo.position = 0;
                TaskHistory.getInstance().addFileInfo(fileInfo);
            }
        }

        @Override
        public void onReceiving(TransferFile file) {
            Log.i(TAG, "onReceiving....  position = " + file.position + ", sumSize = " + file.size);
            file.status = TransferFile.Status.STATUS_RECEIVING;
            TaskHistory.getInstance().updateFileInfo(file);
            EventBus.getDefault().post(new TransferFileEvent(file));
        }

        @Override
        public void onPostReceiving() {
            Log.i(TAG, "onPostReceiving....");
        }

        @Override
        public void onAbortReceiving(int error, String alias) {
            Log.i(TAG, "onAbortReceiving....");
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mP2PManager = new P2PManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && TextUtils.equals(ACTION_START_P2P, intent.getAction())) {
            startP2P();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mNeighbors.clear();
        if (mP2PManager != null) {
            mP2PManager.stop();
            isP2pRunning = false;
        }
    }


    private Peer getSelf() {
        Peer peer = new Peer();
        peer.alias = PreferenceUtil.getInstance().getAlias();
        peer.avatar = PreferenceUtil.getInstance().getHead();
        peer.wifiMac = Util.getStringMD5(PreferenceUtil.getInstance().getMac());
        peer.brand = Build.BRAND;
        peer.mode = Build.MODEL;
        peer.sdkInt = Build.VERSION.SDK_INT;
        peer.versionCode = BuildConfig.VERSION_CODE;
        peer.databaseVersion = TransferDB.VERSION;

        final String ip = WifiUtils.getLocalIP();
        Log.i(TAG, "WifiUtils.getLocalIp = " + ip);
        peer.ip = ip;
        return peer;
    }

    @NonNull
    private TransferFile[] getTransferFiles(ArrayList<FileItem> fileItems) {
        int size = fileItems.size();
        TransferFile[] fileArray = new TransferFile[size];
        for (int i = 0; i < size; i++) {
            FileItem item = fileItems.get(i);
            TransferFile info = new TransferFile();
            info.name = (item.type == Constant.TYPE.APP) ? item.fileName
                    : Util.getNameFromFilepath(item.path);
            info.type = item.type;
            info.size = item.size;
            info.path = item.path;

            File file = new File(item.path);
            info.wifiMac = mNeighbors.get(0).wifiMac;
            info.md5 = Util.getFileMD5(file);
            info.lastModify = file.lastModified();
            info.createTime = System.currentTimeMillis();
            info.status = TransferFile.Status.STATUS_READY;
            info.read = 1;
            info.deleted = 0;
            TaskHistory.getInstance().addFileInfo(info);

            fileArray[i] = info;
        }
        return fileArray;
    }

    public void startP2P() {
        Log.i(TAG, "p2p startP2P....");
        mP2PManager.start(getSelf(), mPeerCallback);
        isP2pRunning = true;

        mP2PManager.receiveFile(mReceiveFileCallback);
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

    public void sendFile(ArrayList<FileItem> fileItems) {
        TransferFile[] sendFiles = getTransferFiles(fileItems);
        mP2PManager.sendFile(new Peer[]{mNeighbors.get(0)}, sendFiles, mSendFileCallback);
    }

    public void sendOffLine() {
        Log.i(TAG, "sendOffLine...");
        if (!isEmpty()) {
            for (Peer neighbor : mNeighbors) {
                mP2PManager.sendOffLine(neighbor);
            }
        }
    }

    public interface Callback {
        void onPeerChanged(List<Peer> neighbors);
    }

    public class P2PBinder extends Binder {
        public void setCallback(Callback callback) {
            mCallback = callback;
        }

        public TransferService getService() {
            return TransferService.this;
        }

        public void startP2P() {
            TransferService.this.startP2P();
        }

        public void stopP2P() {
            TransferService.this.stopP2P();
        }

        public boolean isEmpty() {
            return TransferService.this.isEmpty();
        }

        public boolean isP2PRunning() {
            return TransferService.this.isP2PRunning();
        }

        public void sendFile(ArrayList<FileItem> fileItems) {
            TransferService.this.sendFile(fileItems);
        }

        public void sendOffLine() {
            TransferService.this.sendOffLine();
        }
    }
}
