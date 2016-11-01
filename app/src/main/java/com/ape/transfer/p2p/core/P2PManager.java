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
import com.ape.transfer.p2p.util.Constant;
import com.ape.transfer.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;


/**
 * Created by way on 2015/10/22.
 */
public class P2PManager {
    private static final String TAG = "P2PManager";
    private static final String SAVE_DIR = Environment.getExternalStorageDirectory().getPath()
            + File.separator + Constant.FILE_SHARE_SAVE_PATH;
    private static final String[] TYPE_DIR = {"APP", "Picture", "Video", "Zip", "Document", "Music", "Backup"};

    private volatile static P2PManager INSTANCE;
    private Peer mSelfPeer;
    private HandlerThread mWorkThread;
    private WorkHandler mWorkHandler;
    private MainUIHandler mMainUIHandler;

    private PeerCallback mPeerCallback;
    private ReceiveFileCallback mReceiveFileCallback;
    private SendFileCallback mSendFileCallback;

    private P2PManager() {
        mMainUIHandler = new MainUIHandler(this);
    }

    /**
     * 获取单例
     *
     * @return P2PManager
     */
    public static P2PManager getInstance() {
        if (INSTANCE == null) {
            synchronized (P2PManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new P2PManager();
                }
            }
        }
        return INSTANCE;
    }

    public static String getSavePath(int type) {
        return SAVE_DIR + File.separator + TYPE_DIR[type];
    }

    public void start(Peer self, PeerCallback peerCallback) {
        this.mSelfPeer = self;
        this.mPeerCallback = peerCallback;

        mWorkThread = new HandlerThread(TAG, Thread.MAX_PRIORITY);
        mWorkThread.start();

        mWorkHandler = new WorkHandler(mWorkThread.getLooper());
        mWorkHandler.init(this);
    }

    public void receiveFile(ReceiveFileCallback callback) {
        mReceiveFileCallback = callback;
        mWorkHandler.initReceive();
    }

    public void sendFile(Peer[] dsts, TransferFile[] files, SendFileCallback callback) {
        this.mSendFileCallback = callback;
        Log.i(TAG, "sendFile mWorkHandler = " + mWorkHandler);
        mWorkHandler.initSend();

        ParamSendFiles paramSendFiles = new ParamSendFiles(dsts, files);
        mWorkHandler.send2Handler(Constant.Command.SEND_FILE_REQ,
                Constant.Src.MANAGER, Constant.Recipient.FILE_SEND, paramSendFiles);
    }

    public Peer getSelfPeer() {
        return mSelfPeer;
    }

    public Handler getHandler() {
        return mMainUIHandler;
    }

    public void stop() {
        Log.d(TAG, "p2pManager stop... isStop = " + isStop());
        if (!isStop())
            mWorkHandler.stop();
    }

    public boolean isStop() {
        return mWorkThread == null || mWorkHandler == null;
    }

    private void release() {
        if (mWorkThread != null) {
            mWorkThread.quitSafely();
        }
        mWorkThread = null;
        if (mWorkHandler != null) {
            mWorkHandler.getLooper().quitSafely();
        }
        mWorkHandler = null;
    }

    public void cancelReceive() {
        mWorkHandler.send2Handler(Constant.Command.RECEIVE_ABORT_SELF,
                Constant.Src.MANAGER, Constant.Recipient.FILE_RECEIVE, null);
    }

    public void cancelSend(Peer neighbor) {
        mWorkHandler.send2Handler(Constant.Command.SEND_ABORT_SELF,
                Constant.Src.MANAGER, Constant.Recipient.FILE_SEND, neighbor);
    }

    private static class MainUIHandler extends Handler {
        private WeakReference<P2PManager> weakReference;

        public MainUIHandler(P2PManager manager) {
            this.weakReference = new WeakReference<>(manager);
        }

        @Override
        public void handleMessage(Message msg) {
            P2PManager manager = weakReference.get();
            if (manager == null)
                return;

            switch (msg.what) {
                case Constant.UI.ADD_NEIGHBOR:
                    Log.d(TAG, "handler ui ADD_NEIGHBOR manager.mPeerCallback = " + manager.mPeerCallback);
                    if (manager.mPeerCallback != null)
                        manager.mPeerCallback.onPeerFound((Peer) msg.obj);
                    break;
                case Constant.UI.REMOVE_NEIGHBOR:
                    Log.d(TAG, "handler ui REMOVE_NEIGHBOR manager.mPeerCallback = " + manager.mPeerCallback);
                    if (manager.mPeerCallback != null)
                        manager.mPeerCallback.onPeerRemoved((Peer) msg.obj);
                    break;
                case Constant.Command.SEND_FILE_REQ: //收到请求发送文件
                    if (manager.mReceiveFileCallback != null) {
                        ParamReceiveFiles params = (ParamReceiveFiles) msg.obj;
                        manager.mReceiveFileCallback.onPreReceiving(params.peer,
                                params.transferFiles);
                    }
                    break;
                case Constant.Command.SEND_FILE_START: //发送端开始发送
                    ParamTCPNotify preNotify = (ParamTCPNotify) msg.obj;
                    if (manager.mSendFileCallback != null) {
                        manager.mSendFileCallback.onPreSending((TransferFile[]) preNotify.object,
                                preNotify.peer);
                    }
                    break;
                case Constant.Command.SEND_PERCENTS:
                    ParamTCPNotify notify = (ParamTCPNotify) msg.obj;
                    if (manager.mSendFileCallback != null)
                        manager.mSendFileCallback.onSending((TransferFile) notify.object,
                                notify.peer);
                    break;
                case Constant.Command.SEND_OVER:
                    if (manager.mSendFileCallback != null)
                        manager.mSendFileCallback.onPostSending((Peer) msg.obj);
                    break;
                case Constant.Command.ALL_SEND_OVER:
                    if (manager.mSendFileCallback != null)
                        manager.mSendFileCallback.onPostAllSending();
                    break;
                case Constant.Command.SEND_ABORT_SELF: //通知接收者，发送者退出了
                    if (manager.mReceiveFileCallback != null) {
                        ParamIPMsg paramIPMsg = (ParamIPMsg) msg.obj;
                        if (paramIPMsg != null)
                            manager.mReceiveFileCallback.onAbortReceiving(
                                    Constant.Command.SEND_ABORT_SELF,
                                    paramIPMsg.peerMSG.senderAlias);
                    }
                    break;
                case Constant.Command.RECEIVE_ABORT_SELF: //通知发送者，接收者退出了
                    if (manager.mSendFileCallback != null)
                        manager.mSendFileCallback.onAbortSending(msg.what,
                                (Peer) msg.obj);
                    break;
                case Constant.Command.RECEIVE_OVER:
                    if (manager.mReceiveFileCallback != null)
                        manager.mReceiveFileCallback.onPostReceiving();
                    break;
                case Constant.Command.RECEIVE_PERCENT:
                    ParamTCPNotify receiveNotify = (ParamTCPNotify) msg.obj;
                    if (manager.mReceiveFileCallback != null)
                        manager.mReceiveFileCallback.onReceiving(receiveNotify.peer, (TransferFile) receiveNotify.object);
                    break;
                case Constant.UI.STOP:
                    manager.release();
                    break;
            }
        }
    }


}
