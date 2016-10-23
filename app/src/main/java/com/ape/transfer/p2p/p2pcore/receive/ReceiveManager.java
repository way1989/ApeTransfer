package com.ape.transfer.p2p.p2pcore.receive;


import com.ape.transfer.p2p.p2pconstant.P2PConstant;
import com.ape.transfer.p2p.p2pcore.P2PWorkHandler;
import com.ape.transfer.p2p.p2pentity.P2PFileInfo;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;
import com.ape.transfer.p2p.p2pentity.param.ParamIPMsg;
import com.ape.transfer.p2p.p2pentity.param.ParamReceiveFiles;
import com.ape.transfer.util.Log;

/**
 * Created by way on 2016/10/20.
 */
public class ReceiveManager {

    private static final String TAG = "ReceiveManager";
    P2PWorkHandler mP2PWorkHandler;
    private Receiver mReceiver;

    public ReceiveManager(P2PWorkHandler handler) {
        mP2PWorkHandler = handler;
    }

    public void init() {
        if (mReceiver != null)
            mReceiver = null;
    }

    public void disPatchMsg(int cmd, Object obj, int src) {
        switch (src) {
            case P2PConstant.Src.COMMUNICATE: {
                ParamIPMsg paramIPMsg = (ParamIPMsg) obj;
                if (cmd == P2PConstant.CommandNum.SEND_FILE_REQ) {
                    invoke(paramIPMsg);
                } else {
                    if (mReceiver != null)
                        mReceiver.dispatchCommMSG(cmd, paramIPMsg);
                }
                break;
            }
            case P2PConstant.Src.MANAGER:
                if (mReceiver != null)
                    mReceiver.dispatchUIMSG(cmd, obj);
                break;
            case P2PConstant.Src.RECEIVE_TCP_THREAD:
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

        String[] strArray = paramIPMsg.peerMSG.addition.split(P2PConstant.MSG_SEPARATOR);
        P2PFileInfo[] files = new P2PFileInfo[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            Log.i(TAG, "receive strArray = " + strArray[i]);
            files[i] = new P2PFileInfo(strArray[i]);
        }

        P2PNeighbor neighbor = mP2PWorkHandler.getP2PPeerManager().getNeighbors().get(peerIP);

        mReceiver = new Receiver(this, neighbor, files);

        ParamReceiveFiles paramReceiveFiles = new ParamReceiveFiles(neighbor, files);
        if (mP2PWorkHandler != null)
            mP2PWorkHandler.send2UI(P2PConstant.CommandNum.SEND_FILE_REQ, paramReceiveFiles);
    }
}
