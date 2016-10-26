package com.ape.transfer.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;

import com.ape.transfer.BuildConfig;
import com.ape.transfer.model.FileItem;
import com.ape.transfer.model.P2PFileInfoEvent;
import com.ape.transfer.p2p.p2pconstant.P2PConstant;
import com.ape.transfer.p2p.p2pcore.P2PManager;
import com.ape.transfer.p2p.p2pentity.P2PFileInfo;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;
import com.ape.transfer.p2p.p2pinterface.NeighborCallback;
import com.ape.transfer.p2p.p2pinterface.ReceiveFileCallback;
import com.ape.transfer.p2p.p2pinterface.SendFileCallback;
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
        mNeighbors.clear();
        if (mP2PManager != null) {
            mP2PManager.stop();
            isP2pRunning = false;
        }
    }

    private void init() {
        mP2PManager = new P2PManager();
    }

    @Override
    public void onNeighborFound(P2PNeighbor neighbor) {
        if (neighbor == null)
            return;
        if (!mNeighbors.contains(neighbor) && !TextUtils.equals(neighbor.ip, mP2PManager.getSelfMeMelonInfo().ip)) {
            neighbor.lastTime = System.currentTimeMillis();
            DeviceHistory.getInstance().addDevice(neighbor);
            mNeighbors.add(neighbor);
            if (mCallback != null) mCallback.onNeighborChanged(mNeighbors);
        }
    }

    @Override
    public void onNeighborRemoved(P2PNeighbor neighbor) {
        if (neighbor != null) {
            mNeighbors.remove(neighbor);
            if (mCallback != null) mCallback.onNeighborChanged(mNeighbors);
        }
    }

    @Override
    public boolean onQueryReceiving(P2PNeighbor src, P2PFileInfo[] files) {
        Log.i(TAG, "onQueryReceiving....");
        mP2PManager.ackReceive();
        for (P2PFileInfo fileInfo : files) {
            fileInfo.wifiMac = src.wifiMac;
            fileInfo.direction = P2PFileInfo.Direction.DIRECTION_RECEIVE;
            fileInfo.deleted = 0;
            fileInfo.status = P2PFileInfo.Status.STATUS_READY;
            fileInfo.read = 0;
            fileInfo.position = 0;
            TaskHistory.getInstance().addFileInfo(fileInfo);
        }
        return false;
    }

    @Override
    public void onPreReceiving(P2PNeighbor src, P2PFileInfo[] files) {
        Log.i(TAG, "onPreReceiving....");
    }

    @Override
    public void onReceiving(P2PFileInfo file) {
        Log.i(TAG, "onReceiving....  position = " + file.position + ", sumSize = " + file.size);
        file.status = P2PFileInfo.Status.STATUS_RECEIVING;
        TaskHistory.getInstance().updateFileInfo(file);
        EventBus.getDefault().post(new P2PFileInfoEvent(file));
    }

    @Override
    public void onPostReceiving() {
        Log.i(TAG, "onPostReceiving....");
    }

    @Override
    public void onAbortReceiving(int error, String alias) {
        Log.i(TAG, "onAbortReceiving....");
    }

    public interface Callback {
        void onNeighborChanged(List<P2PNeighbor> neighbors);
    }

    public class P2PBinder extends Binder {
        public void setCallback(Callback callback) {
            mCallback = callback;
        }

        public P2PNeighbor getMe() {
            P2PNeighbor neighbor = new P2PNeighbor();
            neighbor.alias = PreferenceUtil.getInstance().getAlias();
            neighbor.avatar = PreferenceUtil.getInstance().getHead();
            neighbor.wifiMac = Util.getStringMD5(PreferenceUtil.getInstance().getMac());
            neighbor.brand = Build.BRAND;
            neighbor.mode = Build.MODEL;
            neighbor.sdkInt = Build.VERSION.SDK_INT;
            neighbor.versionCode = BuildConfig.VERSION_CODE;
            neighbor.databaseVersion = TransferDB.VERSION;

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

        public void sendFile(ArrayList<FileItem> fileItems) {
            int size = fileItems.size();
            P2PFileInfo[] fileArray = new P2PFileInfo[size];
            for (int i = 0; i < size; i++) {
                FileItem item = fileItems.get(i);
                P2PFileInfo info = new P2PFileInfo();
                if (item.type == P2PConstant.TYPE.APP) {
                    info.name = item.fileName;
                } else {
                    info.name = Util.getNameFromFilepath(item.path);
                }
                info.type = item.type;
                info.size = item.size;
                info.path = item.path;

                File file = new File(item.path);
                info.wifiMac = mNeighbors.get(0).wifiMac;
                info.md5 = Util.getFileMD5(file);
                info.lastModify = file.lastModified();
                info.createTime = System.currentTimeMillis();
                info.status = P2PFileInfo.Status.STATUS_READY;
                info.read = 1;
                info.deleted = 0;
                TaskHistory.getInstance().addFileInfo(info);

                fileArray[i] = info;
            }
            mP2PManager.sendFile(new P2PNeighbor[]{mNeighbors.get(0)}, fileArray, new SendFileCallback() {
                @Override
                public void onPreSending() {
                    Log.i(TAG, "onPreSending....");
                }

                @Override
                public void OnSending(P2PFileInfo file, P2PNeighbor dest) {
                    Log.i(TAG, "OnSending...." + file.name + ", position = " + file.position
                            + ", sumSize = " + file.size);
                    file.status = P2PFileInfo.Status.STATUS_SENDING;
                    TaskHistory.getInstance().updateFileInfo(file);
                    EventBus.getDefault().post(new P2PFileInfoEvent(file));
                }

                @Override
                public void onPostSending(P2PNeighbor dest) {
                    Log.i(TAG, "onPostSending....");
                }

                @Override
                public void onPostAllSending() {
                    Log.i(TAG, "onPostAllSending....");
                }

                @Override
                public void onAbortSending(int error, P2PNeighbor dest) {
                    Log.i(TAG, "onAbortSending....");
                }
            });
        }

        public void sendOffLine() {
            Log.i(TAG, "sendOffLine...");
            if (!isEmpty()) {
                for (P2PNeighbor neighbor : mNeighbors) {
                    mP2PManager.sendOffLine(neighbor);
                }
            }
        }
    }
}
