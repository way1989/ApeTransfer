package com.ape.p2p.core.communicate;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.ape.p2p.bean.P2PNeighbor;
import com.ape.p2p.bean.ParamIPMsg;
import com.ape.p2p.bean.SigMessage;
import com.ape.p2p.p2ptimer.OSTimer;
import com.ape.p2p.p2ptimer.Timeout;
import com.ape.p2p.util.P2PConstant;

import java.net.InetAddress;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

/**
 * Created by android on 16-10-20.
 */

public class P2PWorkHandler extends Handler {
    private P2PCommunicateThread mCommunicateThread;
    private Callback mCallback;
    private HashMap<String, P2PNeighbor> mNeighbors;
    private Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case P2PConstant.UI_MSG.ADD_NEIGHBOR:
                    if (mCallback != null) mCallback.onNeighborFound((P2PNeighbor) msg.obj);
                    break;
                case P2PConstant.UI_MSG.REMOVE_NEIGHBOR:
                    if (mCallback != null) mCallback.onNeighborRemoved((P2PNeighbor) msg.obj);
                    break;
            }
        }
    };

    public P2PWorkHandler(Looper looper, P2PNeighbor self, Callback callback) {
        super(looper);
        mCallback = callback;
        mNeighbors = new HashMap<>();

        mCommunicateThread = new P2PCommunicateThread(this, self);
        mCommunicateThread.start();
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case P2PConstant.Message.MESSAGE:
                parseMessage((ParamIPMsg) msg.obj);
                break;
            case P2PConstant.Message.ERROR:
                break;
            default:
                break;
        }
    }

    private void parseMessage(ParamIPMsg ipMsg) {
        switch (ipMsg.peerMSG.commandNum) {
            case P2PConstant.CommandNum.ON_LINE: //收到上线广播
                Log.d(TAG, "receive on_line and send on_line_ans message");
                addNeighbor(ipMsg.peerMSG, ipMsg.peerIAddr);
                //回复上线
                mCommunicateThread.sendMsg2Peer(ipMsg.peerIAddr, P2PConstant.CommandNum.ON_LINE_ANS, P2PConstant.Recipient.NEIGHBOR, null);
                break;
            case P2PConstant.CommandNum.ON_LINE_ANS: //收到对方上线的回复
                Log.d(TAG, "received on_line_ans message");
                addNeighbor(ipMsg.peerMSG, ipMsg.peerIAddr);
                break;
            case P2PConstant.CommandNum.OFF_LINE://收到离线的消息
                delNeighbor(ipMsg.peerIAddr.getHostAddress());
                break;
        }
    }

    public HashMap<String, P2PNeighbor> getNeighbors() {
        return mNeighbors;
    }

    private void addNeighbor(SigMessage sigMessage, InetAddress address) {
        String ip = address.getHostAddress();
        P2PNeighbor neighbor = mNeighbors.get(ip);
        if (neighbor != null) return;

        neighbor = new P2PNeighbor();
        neighbor.alias = sigMessage.senderAlias;
        neighbor.avatar = sigMessage.senderAvatar;
        neighbor.ip = ip;
        neighbor.inetAddress = address;
        neighbor.wifiMac = sigMessage.wifiMac;
        neighbor.brand = sigMessage.brand;
        neighbor.mode = sigMessage.mode;
        neighbor.sdkInt = sigMessage.sdkInt;
        neighbor.versionCode = sigMessage.versionCode;
        neighbor.databaseVersion = sigMessage.databaseVersion;
        mNeighbors.put(ip, neighbor);

        mUIHandler.sendMessage(mUIHandler.obtainMessage(P2PConstant.UI_MSG.ADD_NEIGHBOR, neighbor));
    }

    private void delNeighbor(String ip) {
        P2PNeighbor neighbor = mNeighbors.get(ip);
        if (neighbor == null) return;
        mNeighbors.remove(ip);
        mUIHandler.sendMessage(mUIHandler.obtainMessage(P2PConstant.UI_MSG.REMOVE_NEIGHBOR, neighbor));
    }

    public void onLine() {
        Timeout timeout = new Timeout() {
            @Override
            public void onTimeOut() {
                Log.d(TAG, "broadcast 广播 msg");
                mCommunicateThread.broadcastMSG(P2PConstant.CommandNum.ON_LINE,
                        P2PConstant.Recipient.NEIGHBOR);
            }
        };
        //发送两个广播消息
        new OSTimer(mUIHandler, timeout, 250).start();
        new OSTimer(mUIHandler, timeout, 500).start();
    }

    public void offLine() {
        Timeout timeOut = new Timeout() {
            @Override
            public void onTimeOut() {
                mCommunicateThread.broadcastMSG(P2PConstant.CommandNum.OFF_LINE,
                        P2PConstant.Recipient.NEIGHBOR);

            }
        };
        timeOut.onTimeOut();
        new OSTimer(mUIHandler, timeOut, 0);
        new OSTimer(mUIHandler, timeOut, 250);
    }

    public void quit() {
        if (mCommunicateThread != null) mCommunicateThread.quit();
        removeCallbacksAndMessages(null);
        getLooper().quitSafely();
    }

    public interface Callback {
        void onNeighborFound(P2PNeighbor neighbor);

        void onNeighborRemoved(P2PNeighbor neighbor);
    }
}
