package com.ape.transfer.p2p.core.send;


import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.p2p.beans.TransferFile;
import com.ape.transfer.p2p.beans.param.ParamIPMsg;
import com.ape.transfer.p2p.beans.param.ParamTCPNotify;
import com.ape.transfer.p2p.core.WorkHandler;
import com.ape.transfer.p2p.util.Constant;
import com.ape.transfer.util.Log;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by way on 2016/10/20.
 */
public class Sender {
    private static final String TAG = "Sender";

    WorkHandler p2PHandler;
    Peer neighbor;
    Queue<TransferFile> mSendFileQueue = new LinkedList<>();
    private SendManager sendManager;

    public Sender(WorkHandler handler, SendManager man, Peer n, TransferFile[] fs) {
        this.p2PHandler = handler;
        this.sendManager = man;
        this.neighbor = n;
        for (TransferFile fileInfo : fs)
            mSendFileQueue.offer(fileInfo);//新任务加入队列
    }

    public void dispatchCommMSG(int cmd, ParamIPMsg ipmsg) {
        switch (cmd) {
//            case Constant.CommandNum.RECEIVE_FILE_ACK:
//                Log.i(TAG, "dispatchCommMSG RECEIVE_FILE_ACK");
//                startSelf();
//                //通知界面开始发送
//                p2PHandler.send2UI(Constant.CommandNum.SEND_FILE_START, null);
//                //通知接收端 开始发送文件
//                p2PHandler.send2Receiver(ipmsg.peerIAddress,
//                        Constant.CommandNum.SEND_FILE_START, null);
//                break;
            case Constant.CommandNum.RECEIVE_ABORT_SELF: //接收者退出
                Log.i(TAG, "dispatchCommMSG RECEIVE_ABORT_SELF");
                clearSelf();
                //通知UI
                p2PHandler.send2UI(cmd, neighbor);
                break;
        }
    }

    public void dispatchTCPMsg(int cmd, Peer notify) {
        switch (cmd) {
            case Constant.CommandNum.SEND_PERCENTS: {
                TransferFile fileInfo = mSendFileQueue.peek();//取当前队列头的任务, 但不出队列

                ParamTCPNotify tcpNotify = new ParamTCPNotify(neighbor, fileInfo);
                p2PHandler.send2UI(Constant.CommandNum.SEND_PERCENTS, tcpNotify);
                if (fileInfo.position == fileInfo.size) {
                    mSendFileQueue.poll();
                    //clearTask();
                    if (mSendFileQueue.isEmpty()) {
                        clearSelf();
                        p2PHandler.send2UI(Constant.CommandNum.SEND_OVER, neighbor);
                    }
                }

                break;
            }
            case Constant.CommandNum.SEND_TCP_ESTABLISHED:
                break;
        }
    }

    public void dispatchUIMSG(int cmd) {
        switch (cmd) {
            case Constant.CommandNum.SEND_ABORT_SELF:
                clearSelf();

                //notify peer
                p2PHandler.send2Receiver(neighbor.inetAddress,
                        Constant.CommandNum.SEND_ABORT_SELF, null);

                break;
        }
    }

//    private void clearTask() {
//        if (mSendTasks.size() > 0) {
//            SendTask task = mSendTasks.get(0);
//            if (task != null) {
//                task.quit();
//            }
//            mSendTasks.remove(0);
//        }
//    }

//    private void startSelf() {
//        sendManager.startSend(neighbor.ip, this);
//    }

    public void clearSelf() {
        sendManager.removeSender(neighbor.ip);
    }

}
