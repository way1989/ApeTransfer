package com.ape.transfer.p2p.core.send;


import android.util.Log;

import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.p2p.beans.TransferFile;
import com.ape.transfer.p2p.beans.param.ParamIPMsg;
import com.ape.transfer.p2p.beans.param.ParamSendFiles;
import com.ape.transfer.p2p.beans.param.ParamTCPNotify;
import com.ape.transfer.p2p.core.WorkHandler;
import com.ape.transfer.p2p.util.Constant;

import java.util.HashMap;

/**
 * Created by way on 2016/10/20.
 */
public class SendManager {
    private static final String TAG = "SendManager";

    private WorkHandler mWorkHandler;
    private HashMap<String, Sender> mSenderHashMap;

    private SendServer mSendServer;

    public SendManager(WorkHandler handler) {
        this.mWorkHandler = handler;
        mSenderHashMap = new HashMap<>();
        init();
    }

    private void init() {
        mSenderHashMap.clear();
    }

    public void disPatchMsg(int what, Object obj, int src) {
        switch (src) {
            case Constant.Src.COMMUNICATE: {
                Log.i(TAG, "disPatchMsg COMMUNICATE");
                String peerIP = ((ParamIPMsg) obj).peerIAddress.getHostAddress();
                Sender sender = getSender(peerIP);
                sender.dispatchCommMSG(what, (ParamIPMsg) obj);    //dispatch
                break;
            }
            case Constant.Src.MANAGER: {//准备发送或取消发送
                Log.i(TAG, "disPatchMsg MANAGER");
                if (what == Constant.Command.SEND_FILE_REQ) {
                    if (!mSenderHashMap.isEmpty())
                        return;
                    ParamSendFiles param = (ParamSendFiles) obj;
                    invoke(param.neighbors, param.transferFiles);
                } else if (what == Constant.Command.SEND_ABORT_SELF) {
                    Sender sender = getSender(((Peer) obj).ip);
                    sender.dispatchUIMSG(what);
                }
                break;
            }
            case Constant.Src.SEND_TCP_THREAD: {
                Log.i(TAG, "disPatchMsg SEND_TCP_THREAD");
                String peerIP = ((Peer) obj).ip;
                Sender sender = getSender(peerIP);
                if (sender == null)
                    return;

                sender.dispatchTCPMsg(what, (Peer) obj);
                break;
            }

        }
    }

    private void invoke(Peer[] neighbors, TransferFile[] files) {
        StringBuilder sb = new StringBuilder("");
        for (TransferFile fileInfo : files)
            sb.append(fileInfo.toString());

        String add = sb.toString();

        for (Peer neighbor : neighbors) {
            Peer peer = mWorkHandler.getPeerManager().getPeerHashMap().get(neighbor.ip);
            //如果对方不在线，则跳过
            if (peer == null) continue;

            Sender sender = new Sender(mWorkHandler, this, peer, files);

            mSenderHashMap.put(neighbor.ip, sender);

            //通知对方，我要发送文件了
            mWorkHandler.send2Receiver(peer.inetAddress, Constant.Command.SEND_FILE_REQ, add);
        }
    }

    public void startSend(Sender fileSender) {
        if (mSendServer == null) {
            Log.d(TAG, "SendManager start send");

            SendServerProxyProxy sendServerHandler = new SendServerProxyProxy(this);
            mSendServer = new SendServer(sendServerHandler, Constant.FILE_TRANSFER_PORT);
            mSendServer.start();
        }
        mSenderHashMap.put(fileSender.neighbor.ip, fileSender);
    }

    public void removeSender(String peerIP) {
        mSenderHashMap.remove(peerIP);
        if (mSenderHashMap.isEmpty()) {
            mWorkHandler.send2UI(Constant.Command.ALL_SEND_OVER, null);//文件全部发送完毕
            mWorkHandler.stopSend();
        }

    }

    public void stop() {
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
