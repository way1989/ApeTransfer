package com.ape.transfer.p2p.p2pcore.send;


import com.ape.transfer.p2p.p2pconstant.P2PConstant;
import com.ape.transfer.p2p.p2pcore.P2PWorkHandler;
import com.ape.transfer.p2p.p2pentity.P2PFileInfo;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;
import com.ape.transfer.p2p.p2pentity.SocketTransInfo;
import com.ape.transfer.p2p.p2pentity.param.ParamIPMsg;
import com.ape.transfer.p2p.p2pentity.param.ParamTCPNotify;
import com.ape.transfer.util.Log;

import java.util.ArrayList;

/**
 * Created by 郭攀峰 on 2015/9/20.
 */
public class Sender {
    private static final String TAG = "Sender";

    P2PWorkHandler p2PHandler;
    P2PFileInfo[] files;
    SendManager sendManager;
    P2PNeighbor neighbor;
    ArrayList<SendTask> mSendTasks = new ArrayList<>();
    int index = 0;
    boolean flagPercents = false;

    public Sender(P2PWorkHandler handler, SendManager man, P2PNeighbor n, P2PFileInfo[] fs) {
        this.p2PHandler = handler;
        this.sendManager = man;
        this.neighbor = n;

        files = new P2PFileInfo[fs.length];
        for (int i = 0; i < files.length; i++) {
            files[i] = fs[i].duplicate();
            files[i].percent = 0;
        }
    }

    public void dispatchCommMSG(int cmd, ParamIPMsg ipmsg) {
        switch (cmd) {
            case P2PConstant.CommandNum.RECEIVE_FILE_ACK:
                Log.i(TAG, "dispatchCommMSG RECEIVE_FILE_ACK");
                startSelf();
                //通知界面开始发送
                if (p2PHandler != null)
                    p2PHandler.send2UI(P2PConstant.CommandNum.SEND_FILE_START, null);
                //通知接收端 开始发送文件
                if (p2PHandler != null)
                    p2PHandler.send2Receiver(ipmsg.peerIAddr,
                            P2PConstant.CommandNum.SEND_FILE_START, null);
                break;
            case P2PConstant.CommandNum.RECEIVE_ABORT_SELF: //接收者退出
                Log.i(TAG, "dispatchCommMSG RECEIVE_ABORT_SELF");
                clearSelf();
                //通知UI
                if (p2PHandler != null)
                    p2PHandler.send2UI(cmd, neighbor);
                break;
        }
    }

    public void dispatchTCPMsg(int cmd, ParamTCPNotify notify) {
        switch (cmd) {
            case P2PConstant.CommandNum.SEND_PERCENTS: {
                SocketTransInfo socketTransInfo = (SocketTransInfo) notify.Obj;
                P2PFileInfo fileInfo = files[index];
                int lastPercent, percent;
                lastPercent = fileInfo.getPercent();

                percent = (int) (((float) (fileInfo.size - (fileInfo.LengthNeeded - socketTransInfo.Transferred)) / fileInfo.size) * 100);

                ParamTCPNotify tcpNotify;
                if (percent < 100) {
                    if (percent != lastPercent) {
                        fileInfo.setPercent(percent);
                        tcpNotify = new ParamTCPNotify(neighbor, fileInfo);
                        if (p2PHandler != null)
                            p2PHandler.send2UI(P2PConstant.CommandNum.SEND_PERCENTS,
                                    tcpNotify);

                    }
                } else if (percent == 100) {
                    fileInfo.setPercent(percent);
                    tcpNotify = new ParamTCPNotify(neighbor, fileInfo);
                    p2PHandler.send2UI(P2PConstant.CommandNum.SEND_PERCENTS, tcpNotify);

                    index++;
                    clearTask();
                    if (index == files.length) {
                        if (p2PHandler != null)
                            p2PHandler
                                    .send2UI(P2PConstant.CommandNum.SEND_OVER, neighbor);
                    }
                }
                break;
            }
            case P2PConstant.CommandNum.SEND_TCP_ESTABLISHED:
                break;
        }
    }

    public void dispatchUIMSG(int cmd) {
        switch (cmd) {
            case P2PConstant.CommandNum.SEND_ABORT_SELF:
                clearSelf();

                //notify peer
                p2PHandler.send2Receiver(neighbor.inetAddress,
                        P2PConstant.CommandNum.SEND_ABORT_SELF, null);

                break;
        }
    }

    private void clearTask() {
        if (mSendTasks.size() > 0) {
            SendTask task = mSendTasks.get(0);
            if (task != null && task.finished == false) {
                task.quit();
            }
            mSendTasks.remove(0);
        }
    }

    private void startSelf() {
        sendManager.startSend(neighbor.ip, this);
    }

    public void clearSelf() {
        sendManager.removeSender(neighbor.ip);
    }

}
