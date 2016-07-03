package com.ape.transfer.p2p.p2pcore;


import android.content.Context;
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

    private P2PManager p2PManager;
    private P2PCommunicateThread p2PCommunicate;
    private P2PCommunicateManager neighborManager;
    private ReceiveManager receiveManager;
    private SendManager sendManager;

    public P2PWorkHandler(Looper looper) {
        super(looper);
    }

    public P2PCommunicateManager getNeighborManager() {
        return neighborManager;
    }

    public void init(P2PManager manager, Context context) {
        this.p2PManager = manager;
        p2PCommunicate = new P2PCommunicateThread(p2PManager, this, context);
        p2PCommunicate.start();

        neighborManager = new P2PCommunicateManager(p2PManager, this, p2PCommunicate);
//        new Thread() {
//            public void run() {
                neighborManager.sendBroadcast();
//            }
//        }.start();
    }

    public void initSend() {
        sendManager = new SendManager(this);
    }

    public void initReceive() {
        receiveManager = new ReceiveManager(this);
    }

//    public void releaseReceive() {
//        neighborManager.offLine();
//        receiveManager.quit();
//    }

    @Override
    public void handleMessage(Message msg) {//进行网络相关操作
        int src = msg.arg1;
        int dst = msg.arg2;
        switch (dst) {
            case P2PConstant.Recipient.NEIGHBOR: //好友状态上线或者离线
                Log.d(TAG, "received neighbor message");
                if (neighborManager != null)
                    neighborManager.dispatchMSG((ParamIPMsg) msg.obj);
                break;
            case P2PConstant.Recipient.FILE_SEND: //发送文件
                Log.d(TAG, "received file send");
                if (sendManager != null)
                    sendManager.disPatchMsg(msg.what, msg.obj, src);
                break;
            case P2PConstant.Recipient.FILE_RECEIVE: //接收文件
                Log.d(TAG, "received file receive");
                if (receiveManager != null)
                    receiveManager.disPatchMsg(msg.what, msg.obj, src);
                break;
        }
    }

    public void release() {
        Log.d(TAG, "p2pHandler release");

        if (receiveManager != null)
            receiveManager.quit();
            //releaseReceive();

        if (sendManager != null) {
            sendManager.quit();
        }
        if (neighborManager != null) {
            neighborManager.offLine();
        }

        Timeout timeout = new Timeout() {
            @Override
            public void onTimeOut() {
                if (p2PCommunicate != null) {
                    p2PCommunicate.quit();
                    p2PCommunicate = null;
                }
            }
        };
        new OSTimer(this, timeout, 250);

        neighborManager = null;
    }

    public void releaseSend() {
        sendManager.quit();
        sendManager = null;
    }

    public void send2Handler(int cmd, int src, int dst, Object obj) {
        this.sendMessage(this.obtainMessage(cmd, src, dst, obj));
    }

    public void send2Neighbor(InetAddress peer, int cmd, String add) {
        if (p2PCommunicate != null)
            p2PCommunicate.sendMsg2Peer(peer, cmd, P2PConstant.Recipient.NEIGHBOR, add);
    }

    public void send2Receiver(InetAddress peer, int cmd, String add) {
        if (p2PCommunicate != null)
            p2PCommunicate.sendMsg2Peer(peer, cmd, P2PConstant.Recipient.FILE_RECEIVE,
                    add);
    }

    public void send2UI(int cmd, Object obj) {
        if (p2PManager != null)
            p2PManager.getHandler().sendMessage(
                    p2PManager.getHandler().obtainMessage(cmd, obj));
    }

    public void send2Sender(InetAddress peer, int cmd, String add) {
        if (p2PCommunicate != null)
            p2PCommunicate.sendMsg2Peer(peer, cmd, P2PConstant.Recipient.FILE_SEND, add);
    }
}
