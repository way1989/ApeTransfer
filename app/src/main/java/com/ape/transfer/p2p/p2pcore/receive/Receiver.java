package com.ape.transfer.p2p.p2pcore.receive;


import android.util.Log;

import com.ape.transfer.p2p.p2pconstant.P2PConstant;
import com.ape.transfer.p2p.p2pcore.P2PWorkHandler;
import com.ape.transfer.p2p.p2pentity.P2PFileInfo;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;
import com.ape.transfer.p2p.p2pentity.param.ParamIPMsg;


/**
 * Created by way on 2015/10/21.
 */
public class Receiver {
    private static final String TAG = "Receiver";
    public ReceiveManager mReceiveManager;
    public P2PNeighbor mNeighbor;
    public P2PFileInfo[] mReceiveFileInfos;
    public P2PWorkHandler mP2PWorkHandler;

    public Receiver(ReceiveManager receiveManager, P2PNeighbor neighbor,
                    P2PFileInfo[] files) {
        this.mReceiveManager = receiveManager;
        this.mNeighbor = neighbor;
        this.mReceiveFileInfos = files;
        mP2PWorkHandler = receiveManager.mP2PWorkHandler;
    }

    public void dispatchCommMSG(int cmd, ParamIPMsg ipMsg) {
        switch (cmd) {
            case P2PConstant.CommandNum.SEND_FILE_START: //接收端收到开始发送文件的消息
                //开始tcp
                Log.d(TAG, "start receiver task");
                ReceiveTask receiveTask = new ReceiveTask(mP2PWorkHandler, this);
                receiveTask.start();
                break;
            case P2PConstant.CommandNum.SEND_ABORT_SELF: //发送者退出
                clearSelf();
                if (mP2PWorkHandler != null) {
                    mP2PWorkHandler.send2UI(cmd, ipMsg);
                }
                break;
        }
    }

    public void dispatchTCPMSG(int cmd, Object obj) {
        switch (cmd) {
            case P2PConstant.CommandNum.RECEIVE_TCP_ESTABLISHED:
                break;
            case P2PConstant.CommandNum.RECEIVE_PERCENT:
                if (mP2PWorkHandler != null)
                    mP2PWorkHandler.send2UI(P2PConstant.CommandNum.RECEIVE_PERCENT, obj);
                break;
            case P2PConstant.CommandNum.RECEIVE_OVER:
                clearSelf();
                if (mP2PWorkHandler != null)
                    mP2PWorkHandler.send2UI(P2PConstant.CommandNum.RECEIVE_OVER, null);
                break;
        }
    }

    public void dispatchUIMSG(int cmd, Object obj) {
        switch (cmd) {
            case P2PConstant.CommandNum.RECEIVE_FILE_ACK: //发送接收文件的消息给发送者
                if (mP2PWorkHandler != null)
                    mP2PWorkHandler.send2Sender(mNeighbor.inetAddress, cmd, null);
                break;
            case P2PConstant.CommandNum.RECEIVE_ABORT_SELF: //接收者退出
                clearSelf();
                //通知发送者接收者已经推出
                if (mP2PWorkHandler != null)
                    mP2PWorkHandler.send2Sender(mNeighbor.inetAddress, cmd, null);
                break;
        }
    }

    private void clearSelf() {
        mReceiveManager.init();
    }
}
