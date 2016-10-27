package com.ape.transfer.p2p.core.receive;


import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.p2p.beans.TransferFile;
import com.ape.transfer.p2p.beans.param.ParamIPMsg;
import com.ape.transfer.p2p.beans.param.ParamReceiveFiles;
import com.ape.transfer.p2p.core.WorkHandler;
import com.ape.transfer.p2p.util.Constant;
import com.ape.transfer.util.Log;

/**
 * Created by way on 2016/10/20.
 */
public class ReceiveManager {

    private static final String TAG = "ReceiveManager";
    WorkHandler mWorkHandler;
    private Receiver mReceiver;

    public ReceiveManager(WorkHandler handler) {
        mWorkHandler = handler;
    }

    public void init() {
        if (mReceiver != null)
            mReceiver = null;
    }

    public void disPatchMsg(int cmd, Object obj, int src) {
        switch (src) {
            case Constant.Src.COMMUNICATE: {
                ParamIPMsg paramIPMsg = (ParamIPMsg) obj;
                if (cmd == Constant.CommandNum.SEND_FILE_REQ) {
                    invoke(paramIPMsg);
                } else {
                    if (mReceiver != null)
                        mReceiver.dispatchCommMSG(cmd, paramIPMsg);
                }
                break;
            }
            case Constant.Src.MANAGER:
                if (mReceiver != null)
                    mReceiver.dispatchUIMSG(cmd, obj);
                break;
            case Constant.Src.RECEIVE_TCP_THREAD:
                if (mReceiver != null)
                    mReceiver.dispatchTCPMSG(cmd, obj);
                break;
        }
    }

    public void quit() {
        init();
    }

    private void invoke(ParamIPMsg paramIPMsg) {
        String peerIP = paramIPMsg.peerIAddr.getHostAddress();

        String[] strArray = paramIPMsg.peerMSG.addition.split(Constant.MSG_SEPARATOR);
        TransferFile[] files = new TransferFile[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            Log.i(TAG, "receive strArray = " + strArray[i]);
            files[i] = new TransferFile(strArray[i]);
        }

        Peer neighbor = mWorkHandler.getPeerManager().getNeighbors().get(peerIP);
        if (neighbor == null) {
            Log.i(TAG, "sender is exit...");
            return;
        }

        mReceiver = new Receiver(this, neighbor, files);

        ParamReceiveFiles paramReceiveFiles = new ParamReceiveFiles(neighbor, files);
        //通知UI,准备开始接收文件
        mWorkHandler.send2UI(Constant.CommandNum.SEND_FILE_REQ, paramReceiveFiles);
        //开始接收
        mWorkHandler.send2Handler(Constant.CommandNum.SEND_FILE_START, Constant.Src.COMMUNICATE,
                Constant.Recipient.FILE_RECEIVE, null);
        //mReceiver.dispatchCommMSG(Constant.CommandNum.SEND_FILE_START, null);
    }
}
