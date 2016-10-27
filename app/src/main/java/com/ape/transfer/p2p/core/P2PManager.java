package com.ape.transfer.p2p.core;


import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.p2p.beans.TransferFile;
import com.ape.transfer.p2p.beans.param.ParamIPMsg;
import com.ape.transfer.p2p.beans.param.ParamReceiveFiles;
import com.ape.transfer.p2p.beans.param.ParamSendFiles;
import com.ape.transfer.p2p.beans.param.ParamTCPNotify;
import com.ape.transfer.p2p.callback.PeerCallback;
import com.ape.transfer.p2p.callback.ReceiveFileCallback;
import com.ape.transfer.p2p.callback.SendFileCallback;
import com.ape.transfer.p2p.timer.OSTimer;
import com.ape.transfer.p2p.timer.Timeout;
import com.ape.transfer.p2p.util.Constant;
import com.ape.transfer.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;


/**
 * Created by way on 2015/10/22.
 */
public class P2PManager {
    private static final String TAG = "P2PManager";

    private static String SAVE_DIR = Environment.getExternalStorageDirectory().getPath()
            + File.separator + Constant.FILE_SHARE_SAVE_PATH;

    private Peer mNeighbor;
    private PeerCallback mPeerCallback;
    private WorkHandler mWorkHandler;
    private P2PManagerHandler mMainUIHandler;

    private ReceiveFileCallback mReceiveFileCallback;
    private SendFileCallback mSendFileCallback;


    public P2PManager() {
        mMainUIHandler = new P2PManagerHandler(this);
    }

    public static String getSavePath(int type) {
        String[] typeStr = {"APP", "Picture", "Video", "Zip", "Document", "Music", "Backup"};
        return SAVE_DIR + File.separator + typeStr[type];
    }

    public void start(Peer melon, PeerCallback melon_callback) {
        this.mNeighbor = melon;
        this.mPeerCallback = melon_callback;

        HandlerThread handlerThread = new HandlerThread(TAG, Thread.MAX_PRIORITY);
        handlerThread.start();

        mWorkHandler = new WorkHandler(handlerThread.getLooper());
        mWorkHandler.init(this);
    }

    public void receiveFile(ReceiveFileCallback callback) {
        mReceiveFileCallback = callback;
        mWorkHandler.initReceive();
    }

    public void sendFile(Peer[] dsts, TransferFile[] files,
                         SendFileCallback callback) {
        this.mSendFileCallback = callback;
        Log.i(TAG, "sendFile mWorkHandler = " + mWorkHandler);
        mWorkHandler.initSend();

        ParamSendFiles paramSendFiles = new ParamSendFiles(dsts, files);
        mWorkHandler.send2Handler(Constant.CommandNum.SEND_FILE_REQ,
                Constant.Src.MANAGER, Constant.Recipient.FILE_SEND, paramSendFiles);
    }

//    public void ackReceive() {
//        mWorkHandler.send2Handler(Constant.CommandNum.RECEIVE_FILE_ACK,
//                Constant.Src.MANAGER, Constant.Recipient.FILE_RECEIVE, null);
//    }

    public Peer getSelfMeMelonInfo() {
        return mNeighbor;
    }

    public Handler getHandler() {
        return mMainUIHandler;
    }

    public void stop() {
        if (mWorkHandler != null) {
            Log.d(TAG, "p2pManager stop");
            mWorkHandler.release();
            mWorkHandler.getLooper().quitSafely();
            mWorkHandler = null;
        }
    }

    public void cancelReceive() {
        mWorkHandler.send2Handler(Constant.CommandNum.RECEIVE_ABORT_SELF,
                Constant.Src.MANAGER, Constant.Recipient.FILE_RECEIVE, null);
    }

    public void cancelSend(Peer neighbor) {
        mWorkHandler.send2Handler(Constant.CommandNum.SEND_ABORT_SELF,
                Constant.Src.MANAGER, Constant.Recipient.FILE_SEND, neighbor);
    }

    public void sendOffLine(final Peer neighbor) {
        Log.i(TAG, "sendOffLine... mNeighbor = " + neighbor);
        if (mWorkHandler == null) return;
        Timeout timeOut = new Timeout() {
            @Override
            public void onTimeOut() {
                android.util.Log.d(TAG, "sendOffLine... ");
                mWorkHandler.send2Neighbor(neighbor.inetAddress, Constant.CommandNum.OFF_LINE, null);

            }
        };
        new OSTimer(mWorkHandler, timeOut, 0);
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
                case Constant.UI_MSG.ADD_NEIGHBOR:
                    if (manager.mPeerCallback != null)
                        manager.mPeerCallback.onPeerFound((Peer) msg.obj);
                    break;
                case Constant.UI_MSG.REMOVE_NEIGHBOR:
                    if (manager.mPeerCallback != null)
                        manager.mPeerCallback.onPeerRemoved((Peer) msg.obj);
                    break;
                case Constant.CommandNum.SEND_FILE_REQ: //收到请求发送文件
                    if (manager.mReceiveFileCallback != null) {
                        ParamReceiveFiles params = (ParamReceiveFiles) msg.obj;
                        manager.mReceiveFileCallback.onPreReceiving(params.Neighbor,
                                params.Files);
                    }
                    break;
                case Constant.CommandNum.SEND_FILE_START: //发送端开始发送
                    if (manager.mSendFileCallback != null) {
                        manager.mSendFileCallback.onPreSending();
                    }
                    break;
                case Constant.CommandNum.SEND_PERCENTS:
                    ParamTCPNotify notify = (ParamTCPNotify) msg.obj;
                    if (manager.mSendFileCallback != null)
                        manager.mSendFileCallback.onSending((TransferFile) notify.Obj,
                                notify.Neighbor);
                    break;
                case Constant.CommandNum.SEND_OVER:
                    if (manager.mSendFileCallback != null)
                        manager.mSendFileCallback.onPostSending((Peer) msg.obj);
                    break;
                case Constant.CommandNum.ALL_SEND_OVER:
                    if (manager.mSendFileCallback != null)
                        manager.mSendFileCallback.onPostAllSending();
                    break;
                case Constant.CommandNum.SEND_ABORT_SELF: //通知接收者，发送者退出了
                    if (manager.mReceiveFileCallback != null) {
                        ParamIPMsg paramIPMsg = (ParamIPMsg) msg.obj;
                        if (paramIPMsg != null)
                            manager.mReceiveFileCallback.onAbortReceiving(
                                    Constant.CommandNum.SEND_ABORT_SELF,
                                    paramIPMsg.peerMSG.senderAlias);
                    }
                    break;
                case Constant.CommandNum.RECEIVE_ABORT_SELF: //通知发送者，接收者退出了
                    if (manager.mSendFileCallback != null)
                        manager.mSendFileCallback.onAbortSending(msg.what,
                                (Peer) msg.obj);
                    break;
                case Constant.CommandNum.RECEIVE_OVER:
                    if (manager.mReceiveFileCallback != null)
                        manager.mReceiveFileCallback.onPostReceiving();
                    break;
                case Constant.CommandNum.RECEIVE_PERCENT:
                    if (manager.mReceiveFileCallback != null)
                        manager.mReceiveFileCallback.onReceiving((TransferFile) msg.obj);
                    break;
            }
        }
    }

}
