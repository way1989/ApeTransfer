package com.ape.transfer.p2p.p2pcore.send;


import android.util.Log;

import com.ape.transfer.p2p.p2pconstant.P2PConstant;
import com.ape.transfer.p2p.p2pcore.P2PWorkHandler;
import com.ape.transfer.p2p.p2pentity.P2PFileInfo;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;
import com.ape.transfer.p2p.p2pentity.param.ParamIPMsg;
import com.ape.transfer.p2p.p2pentity.param.ParamSendFiles;

import java.util.HashMap;

/**
 * Created by 郭攀峰 on 2015/9/20.
 */
public class SendManager {
    private static final String TAG = "SendManager";

    private P2PWorkHandler mP2PWorkHandler;
    private HashMap<String, Sender> mSenderHashMap;

    private SendServer mSendServer;

    public SendManager(P2PWorkHandler handler) {
        this.mP2PWorkHandler = handler;
        mSenderHashMap = new HashMap<>();
        init();
    }

    private void init() {
        mSenderHashMap.clear();
    }

    public void disPatchMsg(int what, Object obj, int src) {
        switch (src) {
            case P2PConstant.Src.COMMUNICATE: {
                Log.i(TAG, "disPatchMsg COMMUNICATE");
                String peerIP = ((ParamIPMsg) obj).peerIAddr.getHostAddress();
                Sender sender = getSender(peerIP);
                sender.dispatchCommMSG(what, (ParamIPMsg) obj);    //dispatch
                break;
            }
            case P2PConstant.Src.MANAGER: {//准备发送或取消发送
                Log.i(TAG, "disPatchMsg MANAGER");
                if (what == P2PConstant.CommandNum.SEND_FILE_REQ) {
                    if (!mSenderHashMap.isEmpty())
                        return;
                    ParamSendFiles param = (ParamSendFiles) obj;
                    invoke(param.neighbors, param.files);
                } else if (what == P2PConstant.CommandNum.SEND_ABORT_SELF) {
                    Sender sender = getSender(((P2PNeighbor) obj).ip);
                    sender.dispatchUIMSG(what);
                }
                break;
            }
            case P2PConstant.Src.SEND_TCP_THREAD: {
                Log.i(TAG, "disPatchMsg SEND_TCP_THREAD");
                String peerIP = ((P2PNeighbor) obj).ip;
                Sender sender = getSender(peerIP);
                if (sender == null)
                    return;

                sender.dispatchTCPMsg(what, (P2PNeighbor) obj);
                break;
            }

        }
    }

    private void invoke(P2PNeighbor[] neighbors, P2PFileInfo[] files) {
        StringBuffer stringBuffer = new StringBuffer("");
        for (P2PFileInfo fileInfo : files) {
            stringBuffer.append(fileInfo.toString());
        }
        String add = stringBuffer.toString();

        for (P2PNeighbor neighbor : neighbors) {
            P2PNeighbor melon = mP2PWorkHandler.getP2PPeerManager().getNeighbors()
                    .get(neighbor.ip);
            Sender sender = null;
            if (melon != null) {
                sender = new Sender(mP2PWorkHandler, this, melon, files);
            }

            mSenderHashMap.put(neighbor.ip, sender);

            if (melon != null) //通知对方，我要发送文件了
            {
                if (mP2PWorkHandler != null)
                    mP2PWorkHandler.send2Receiver(melon.inetAddress,
                            P2PConstant.CommandNum.SEND_FILE_REQ, add);
            }
        }
    }

    public void startSend(String peerIP, Sender fileSender) {
        if (mSendServer == null) {
            Log.d(TAG, "SendManager start send");

            SendServerHandler sendServerHandler = new SendServerHandler(this);
            mSendServer = new SendServer(sendServerHandler, P2PConstant.PORT);
            mSendServer.start();
        }
        mSenderHashMap.put(fileSender.neighbor.ip, fileSender);
    }

    public void removeSender(String peerIP) {
        mSenderHashMap.remove(peerIP);
        checkAllOver();
    }

    public void checkAllOver() {
        if (mSenderHashMap.isEmpty()) {
            mP2PWorkHandler.releaseSend();
        }
    }

    public void quit() {
        mSenderHashMap.clear();
        if (mSendServer != null) {
            mSendServer.quit();
            mSendServer = null;
        }
    }

    protected Sender getSender(String peerIP) {
        return mSenderHashMap.get(peerIP);
    }
}
