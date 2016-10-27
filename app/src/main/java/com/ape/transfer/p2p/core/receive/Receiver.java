package com.ape.transfer.p2p.core.receive;


import android.util.Log;

import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.p2p.beans.TransferFile;
import com.ape.transfer.p2p.beans.param.ParamIPMsg;
import com.ape.transfer.p2p.core.WorkHandler;
import com.ape.transfer.p2p.util.Constant;


/**
 * Created by way on 2015/10/21.
 */
public class Receiver {
    private static final String TAG = "Receiver";
    public ReceiveManager mReceiveManager;
    public Peer mNeighbor;
    public TransferFile[] mReceiveFileInfos;
    public WorkHandler mWorkHandler;

    public Receiver(ReceiveManager receiveManager, Peer neighbor,
                    TransferFile[] files) {
        this.mReceiveManager = receiveManager;
        this.mNeighbor = neighbor;
        this.mReceiveFileInfos = files;
        mWorkHandler = receiveManager.mWorkHandler;
    }

    public void dispatchCommMSG(int cmd, ParamIPMsg ipMsg) {
        switch (cmd) {
            case Constant.CommandNum.SEND_FILE_START: //接收端收到开始发送文件的消息
                //开始tcp
                Log.d(TAG, "start receiver task");
                ReceiveTask receiveTask = new ReceiveTask(mWorkHandler, this);
                receiveTask.start();
                break;
            case Constant.CommandNum.SEND_ABORT_SELF: //发送者退出
                clearSelf();
                if (mWorkHandler != null) {
                    mWorkHandler.send2UI(cmd, ipMsg);
                }
                break;
        }
    }

    public void dispatchTCPMSG(int cmd, Object obj) {
        switch (cmd) {
            case Constant.CommandNum.RECEIVE_TCP_ESTABLISHED:
                break;
            case Constant.CommandNum.RECEIVE_PERCENT:
                if (mWorkHandler != null)
                    mWorkHandler.send2UI(Constant.CommandNum.RECEIVE_PERCENT, obj);
                break;
            case Constant.CommandNum.RECEIVE_OVER:
                clearSelf();
                if (mWorkHandler != null)
                    mWorkHandler.send2UI(Constant.CommandNum.RECEIVE_OVER, null);
                break;
        }
    }

    public void dispatchUIMSG(int cmd, Object obj) {
        switch (cmd) {
            case Constant.CommandNum.RECEIVE_FILE_ACK: //发送接收文件的消息给发送者
                if (mWorkHandler != null)
                    mWorkHandler.send2Sender(mNeighbor.inetAddress, cmd, null);
                break;
            case Constant.CommandNum.RECEIVE_ABORT_SELF: //接收者退出
                clearSelf();
                //通知发送者接收者已经推出
                if (mWorkHandler != null)
                    mWorkHandler.send2Sender(mNeighbor.inetAddress, cmd, null);
                break;
        }
    }

    private void clearSelf() {
        mReceiveManager.init();
    }
}
