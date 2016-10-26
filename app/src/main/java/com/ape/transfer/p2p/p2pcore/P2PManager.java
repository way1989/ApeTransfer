package com.ape.transfer.p2p.p2pcore;


import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.ape.transfer.p2p.p2pconstant.P2PConstant;
import com.ape.transfer.p2p.p2pentity.P2PFileInfo;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;
import com.ape.transfer.p2p.p2pentity.param.ParamIPMsg;
import com.ape.transfer.p2p.p2pentity.param.ParamReceiveFiles;
import com.ape.transfer.p2p.p2pentity.param.ParamSendFiles;
import com.ape.transfer.p2p.p2pentity.param.ParamTCPNotify;
import com.ape.transfer.p2p.p2pinterface.NeighborCallback;
import com.ape.transfer.p2p.p2pinterface.ReceiveFileCallback;
import com.ape.transfer.p2p.p2pinterface.SendFileCallback;
import com.ape.transfer.p2p.p2ptimer.OSTimer;
import com.ape.transfer.p2p.p2ptimer.Timeout;
import com.ape.transfer.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;


/**
 * Created by way on 2015/10/22.
 */
public class P2PManager {
    private static final String TAG = "P2PManager";

    private static String SAVE_DIR = Environment.getExternalStorageDirectory().getPath()
            + File.separator + P2PConstant.FILE_SHARE_SAVE_PATH;

    private P2PNeighbor mNeighbor;
    private NeighborCallback mNeighborCallback;
    private P2PWorkHandler mP2PWorkHandler;
    private P2PManagerHandler mMainUIHandler;

    private ReceiveFileCallback mReceiveFileCallback;
    private SendFileCallback mSendFileCallback;


    public P2PManager() {
        mMainUIHandler = new P2PManagerHandler(this);
    }

    public static String getSavePath(int type) {
        String[] typeStr = {"APP", "Picture", "Video", "Zip", "Document", "Music"};
        return SAVE_DIR + File.separator + typeStr[type];
    }

    public void start(P2PNeighbor melon, NeighborCallback melon_callback) {
        this.mNeighbor = melon;
        this.mNeighborCallback = melon_callback;

        HandlerThread handlerThread = new HandlerThread(TAG, Thread.MAX_PRIORITY);
        handlerThread.start();

        mP2PWorkHandler = new P2PWorkHandler(handlerThread.getLooper());
        mP2PWorkHandler.init(this);
    }

    public void receiveFile(ReceiveFileCallback callback) {
        mReceiveFileCallback = callback;
        mP2PWorkHandler.initReceive();
    }

    public void sendFile(P2PNeighbor[] dsts, P2PFileInfo[] files,
                         SendFileCallback callback) {
        this.mSendFileCallback = callback;
        Log.i(TAG, "sendFile mP2PWorkHandler = " + mP2PWorkHandler);
        mP2PWorkHandler.initSend();

        ParamSendFiles paramSendFiles = new ParamSendFiles(dsts, files);
        mP2PWorkHandler.send2Handler(P2PConstant.CommandNum.SEND_FILE_REQ,
                P2PConstant.Src.MANAGER, P2PConstant.Recipient.FILE_SEND, paramSendFiles);
    }

    public void ackReceive() {
        mP2PWorkHandler.send2Handler(P2PConstant.CommandNum.RECEIVE_FILE_ACK,
                P2PConstant.Src.MANAGER, P2PConstant.Recipient.FILE_RECEIVE, null);
    }

    public P2PNeighbor getSelfMeMelonInfo() {
        return mNeighbor;
    }

    public Handler getHandler() {
        return mMainUIHandler;
    }

    public void stop() {
        if (mP2PWorkHandler != null) {
            Log.d(TAG, "p2pManager stop");
            mP2PWorkHandler.release();
            mP2PWorkHandler.getLooper().quitSafely();
            mP2PWorkHandler = null;
        }
    }

    public void cancelReceive() {
        mP2PWorkHandler.send2Handler(P2PConstant.CommandNum.RECEIVE_ABORT_SELF,
                P2PConstant.Src.MANAGER, P2PConstant.Recipient.FILE_RECEIVE, null);
    }

    public void cancelSend(P2PNeighbor neighbor) {
        mP2PWorkHandler.send2Handler(P2PConstant.CommandNum.SEND_ABORT_SELF,
                P2PConstant.Src.MANAGER, P2PConstant.Recipient.FILE_SEND, neighbor);
    }

    public void sendOffLine(final P2PNeighbor neighbor) {
        Log.i(TAG, "sendOffLine... mNeighbor = " + neighbor);
        if (mP2PWorkHandler == null) return;
        Timeout timeOut = new Timeout() {
            @Override
            public void onTimeOut() {
                android.util.Log.d(TAG, "sendOffLine... ");
                mP2PWorkHandler.send2Neighbor(neighbor.inetAddress, P2PConstant.CommandNum.OFF_LINE, null);

            }
        };
        new OSTimer(mP2PWorkHandler, timeOut, 0);
    }

    private static class P2PManagerHandler extends Handler {
        private WeakReference<P2PManager> weakReference;

        public P2PManagerHandler(P2PManager manager) {
            this.weakReference = new WeakReference<>(manager);
        }

        @Override
        public void handleMessage(Message msg) {
            P2PManager manager = weakReference.get();
            if (manager == null)
                return;

            switch (msg.what) {
                case P2PConstant.UI_MSG.ADD_NEIGHBOR:
                    if (manager.mNeighborCallback != null)
                        manager.mNeighborCallback.onNeighborFound((P2PNeighbor) msg.obj);
                    break;
                case P2PConstant.UI_MSG.REMOVE_NEIGHBOR:
                    if (manager.mNeighborCallback != null)
                        manager.mNeighborCallback.onNeighborRemoved((P2PNeighbor) msg.obj);
                    break;
                case P2PConstant.CommandNum.SEND_FILE_REQ: //收到请求发送文件
                    if (manager.mReceiveFileCallback != null) {
                        ParamReceiveFiles params = (ParamReceiveFiles) msg.obj;
                        manager.mReceiveFileCallback.onQueryReceiving(params.Neighbor,
                                params.Files);
                    }
                    break;
                case P2PConstant.CommandNum.SEND_FILE_START: //发送端开始发送
                    if (manager.mSendFileCallback != null) {
                        manager.mSendFileCallback.onPreSending();
                    }
                    break;
                case P2PConstant.CommandNum.SEND_PERCENTS:
                    ParamTCPNotify notify = (ParamTCPNotify) msg.obj;
                    if (manager.mSendFileCallback != null)
                        manager.mSendFileCallback.OnSending((P2PFileInfo) notify.Obj,
                                notify.Neighbor);
                    break;
                case P2PConstant.CommandNum.SEND_OVER:
                    if (manager.mSendFileCallback != null)
                        manager.mSendFileCallback.onPostSending((P2PNeighbor) msg.obj);
                    break;
                case P2PConstant.CommandNum.SEND_ABORT_SELF: //通知接收者，发送者退出了
                    if (manager.mReceiveFileCallback != null) {
                        ParamIPMsg paramIPMsg = (ParamIPMsg) msg.obj;
                        if (paramIPMsg != null)
                            manager.mReceiveFileCallback.onAbortReceiving(
                                    P2PConstant.CommandNum.SEND_ABORT_SELF,
                                    paramIPMsg.peerMSG.senderAlias);
                    }
                    break;
                case P2PConstant.CommandNum.RECEIVE_ABORT_SELF: //通知发送者，接收者退出了
                    if (manager.mSendFileCallback != null)
                        manager.mSendFileCallback.onAbortSending(msg.what,
                                (P2PNeighbor) msg.obj);
                    break;
                case P2PConstant.CommandNum.RECEIVE_OVER:
                    if (manager.mReceiveFileCallback != null)
                        manager.mReceiveFileCallback.onPostReceiving();
                    break;
                case P2PConstant.CommandNum.RECEIVE_PERCENT:
                    if (manager.mReceiveFileCallback != null)
                        manager.mReceiveFileCallback.onReceiving((P2PFileInfo) msg.obj);
                    break;
            }
        }
    }

}
