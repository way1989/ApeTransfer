package com.ape.transfer.p2p.core;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.p2p.beans.param.ParamIPMsg;
import com.ape.transfer.p2p.core.receive.ReceiveManager;
import com.ape.transfer.p2p.core.send.SendManager;
import com.ape.transfer.p2p.util.Constant;

import java.net.InetAddress;


/**
 * Created by way on 2016/10/20.
 * 所有的message中转的handler，可以接受来自UI或者thread的message，也可以转发message到UI
 */
public class WorkHandler extends Handler {
    private static final String TAG = "WorkHandler";

    private P2PManager mP2PManager;
    private CommunicateThread mCommunicateThread;
    private PeerManager mPeerManager;
    private ReceiveManager mReceiveManager;
    private SendManager mSendManager;

    public WorkHandler(Looper looper) {
        super(looper);
    }

    public PeerManager getPeerManager() {
        return mPeerManager;
    }

    public void init(P2PManager manager) {
        Log.i(TAG, "thread id = " + Thread.currentThread().getId());
        this.mP2PManager = manager;
        mCommunicateThread = new CommunicateThread(mP2PManager.getSelfPeer(), this);
        mCommunicateThread.start();

        mPeerManager = new PeerManager(this);

        onLine();
    }

    public void onLine() {
        Log.d(TAG, "onLine... out");
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onLine... post in");
                mCommunicateThread.broadcastMSG(Constant.CommandNum.ON_LINE, Constant.Recipient.NEIGHBOR);
            }
        }, 2000L);
    }

    public void offLine() {
        Log.d(TAG, "offLine... out");
        this.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "offLine... post in");
                for(Peer peer : mPeerManager.getNeighbors().values()){
                    mCommunicateThread.sendMsg2Peer(peer.inetAddress, Constant.CommandNum.OFF_LINE,
                            Constant.Recipient.NEIGHBOR, null);
                }
                //mCommunicateThread.broadcastMSG(Constant.CommandNum.OFF_LINE, Constant.Recipient.NEIGHBOR);
                mCommunicateThread.quit();
                WorkHandler.this.getLooper().quitSafely();
            }
        });
    }

    public void initSend() {
        mSendManager = new SendManager(this);
    }

    public void initReceive() {
        mReceiveManager = new ReceiveManager(this);
    }

    @Override
    public void handleMessage(Message msg) {//进行网络相关操作
        int src = msg.arg1;
        int dst = msg.arg2;
        switch (dst) {
            case Constant.Recipient.NEIGHBOR: //好友状态上线或者离线
                Log.d(TAG, "received mNeighbor message");
                if (mPeerManager != null)
                    mPeerManager.dispatchMSG((ParamIPMsg) msg.obj);
                break;
            case Constant.Recipient.FILE_SEND: //发送文件
                Log.d(TAG, "received file send");
                if (mSendManager != null)
                    mSendManager.disPatchMsg(msg.what, msg.obj, src);
                break;
            case Constant.Recipient.FILE_RECEIVE: //接收文件
                Log.d(TAG, "received file receive");
                if (mReceiveManager != null)
                    mReceiveManager.disPatchMsg(msg.what, msg.obj, src);
                break;
        }
    }

    public void release() {
        Log.d(TAG, "p2pHandler release");
        releaseReceive();
        releaseSend();
        offLine();
    }

    private void releaseReceive() {
        if (mReceiveManager != null)
            mReceiveManager.quit();
        mReceiveManager = null;
    }

    public void releaseSend() {
        if (mSendManager != null)
            mSendManager.quit();
        mSendManager = null;
    }

    public void send2Handler(int cmd, int src, int dst, Object obj) {
        this.sendMessage(this.obtainMessage(cmd, src, dst, obj));
    }

    public void send2Neighbor(InetAddress peer, int cmd, String add) {
        if (mCommunicateThread != null)
            mCommunicateThread.sendMsg2Peer(peer, cmd, Constant.Recipient.NEIGHBOR, add);
    }

    public void send2Receiver(InetAddress peer, int cmd, String add) {
        if (mCommunicateThread != null)
            mCommunicateThread.sendMsg2Peer(peer, cmd, Constant.Recipient.FILE_RECEIVE, add);
    }

    public void send2UI(int cmd, Object obj) {
        if (mP2PManager != null)
            mP2PManager.getHandler().sendMessage(mP2PManager.getHandler().obtainMessage(cmd, obj));
    }

    public void send2Sender(InetAddress peer, int cmd, String add) {
        if (mCommunicateThread != null)
            mCommunicateThread.sendMsg2Peer(peer, cmd, Constant.Recipient.FILE_SEND, add);
    }
}
