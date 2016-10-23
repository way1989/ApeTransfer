package com.ape.transfer.p2p.p2pcore;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.ape.transfer.p2p.p2pconstant.P2PConstant;
import com.ape.transfer.p2p.p2pcore.receive.ReceiveManager;
import com.ape.transfer.p2p.p2pcore.send.SendManager;
import com.ape.transfer.p2p.p2pentity.param.ParamIPMsg;
import com.ape.transfer.p2p.p2ptimer.OSTimer;
import com.ape.transfer.p2p.p2ptimer.Timeout;

import java.net.InetAddress;


/**
 * Created by 郭攀峰 on 2015/9/19.
 * 所有的message中转的handler，可以接受来自UI或者thread的message，也可以转发message到UI
 */
public class P2PWorkHandler extends Handler {
    private static final String TAG = "P2PWorkHandler";

    private P2PManager mP2PManager;
    private P2PCommunicateThread mP2PCommunicateThread;
    private P2PPeerManager mP2PPeerManager;
    private ReceiveManager mReceiveManager;
    private SendManager mSendManager;

    public P2PWorkHandler(Looper looper) {
        super(looper);
    }

    public P2PPeerManager getP2PPeerManager() {
        return mP2PPeerManager;
    }

    public void init(P2PManager manager) {
        Log.i(TAG, "thread id = " + Thread.currentThread().getId());
        this.mP2PManager = manager;
        mP2PCommunicateThread = new P2PCommunicateThread(mP2PManager.getSelfMeMelonInfo(), this);
        mP2PCommunicateThread.start();

        mP2PPeerManager = new P2PPeerManager(this);

        onLine();
    }

    public void onLine() {
        Timeout timeout = new Timeout() {
            @Override
            public void onTimeOut() {
                Log.d(TAG, "onLine...");
                mP2PCommunicateThread.broadcastMSG(P2PConstant.CommandNum.ON_LINE, P2PConstant.Recipient.NEIGHBOR);
            }
        };
        //发送两个广播消息
        new OSTimer(this, timeout, 250).start();
        new OSTimer(this, timeout, 500).start();
    }

    public void offLine() {
        Timeout timeOut = new Timeout() {
            @Override
            public void onTimeOut() {
                Log.d(TAG, "offLine...");
                mP2PCommunicateThread.broadcastMSG(P2PConstant.CommandNum.OFF_LINE, P2PConstant.Recipient.NEIGHBOR);
            }
        };
        new OSTimer(this, timeOut, 0);
        new OSTimer(this, timeOut, 250);
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
            case P2PConstant.Recipient.NEIGHBOR: //好友状态上线或者离线
                Log.d(TAG, "received mNeighbor message");
                if (mP2PPeerManager != null)
                    mP2PPeerManager.dispatchMSG((ParamIPMsg) msg.obj);
                break;
            case P2PConstant.Recipient.FILE_SEND: //发送文件
                Log.d(TAG, "received file send");
                if (mSendManager != null)
                    mSendManager.disPatchMsg(msg.what, msg.obj, src);
                break;
            case P2PConstant.Recipient.FILE_RECEIVE: //接收文件
                Log.d(TAG, "received file receive");
                if (mReceiveManager != null)
                    mReceiveManager.disPatchMsg(msg.what, msg.obj, src);
                break;
        }
    }

    public void release() {
        Log.d(TAG, "p2pHandler release");

        if (mReceiveManager != null)
            mReceiveManager.quit();

        if (mSendManager != null)
            mSendManager.quit();

        offLine();

        Timeout timeout = new Timeout() {
            @Override
            public void onTimeOut() {
                if (mP2PCommunicateThread != null) {
                    mP2PCommunicateThread.quit();
                    mP2PCommunicateThread = null;
                }
            }
        };
        new OSTimer(this, timeout, 500);

        mP2PPeerManager = null;
    }

    public void releaseSend() {
        mSendManager.quit();
        mSendManager = null;
    }

    public void send2Handler(int cmd, int src, int dst, Object obj) {
        this.sendMessage(this.obtainMessage(cmd, src, dst, obj));
    }

    public void send2Neighbor(InetAddress peer, int cmd, String add) {
        if (mP2PCommunicateThread != null)
            mP2PCommunicateThread.sendMsg2Peer(peer, cmd, P2PConstant.Recipient.NEIGHBOR, add);
    }

    public void send2Receiver(InetAddress peer, int cmd, String add) {
        if (mP2PCommunicateThread != null)
            mP2PCommunicateThread.sendMsg2Peer(peer, cmd, P2PConstant.Recipient.FILE_RECEIVE, add);
    }

    public void send2UI(int cmd, Object obj) {
        if (mP2PManager != null)
            mP2PManager.getHandler().sendMessage(mP2PManager.getHandler().obtainMessage(cmd, obj));
    }

    public void send2Sender(InetAddress peer, int cmd, String add) {
        if (mP2PCommunicateThread != null)
            mP2PCommunicateThread.sendMsg2Peer(peer, cmd, P2PConstant.Recipient.FILE_SEND, add);
    }
}
