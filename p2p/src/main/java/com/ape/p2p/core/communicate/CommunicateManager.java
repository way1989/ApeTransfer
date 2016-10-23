package com.ape.p2p.core.communicate;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

import com.ape.p2p.bean.P2PNeighbor;
import com.ape.p2p.bean.ParamIPMsg;
import com.ape.p2p.bean.SigMessage;
import com.ape.p2p.util.P2PConstant;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.ape.p2p.util.P2PConstant.CommandNum.OFF_LINE;
import static com.ape.p2p.util.P2PConstant.CommandNum.ON_LINE;


/**
 * Created by way on 2016/10/22.
 */

public class CommunicateManager implements P2PCommunicateThread.Callback {
    private static final String TAG = "CommunicateManager";
    private static final int PARSE_MESSGAE = 0;
    private static final int ERROR_MESSGAE = 1;
    private static final int SEND_MESSGAE = 2;
    private final MainHandler mHandler = new MainHandler();
    private Callback mCallback;
    private WorkThread mWorkThread;
    private Handler mWorkHandler;
    private P2PCommunicateThread mCommunicateThread;
    private P2PNeighbor mSelf;

    public CommunicateManager(P2PNeighbor self, Callback callback) {
        mSelf = self;
        mCallback = callback;
        mWorkThread = new WorkThread();
        mWorkThread.start();
        mWorkHandler = new Handler(mWorkThread.getLooper(), mWorkThread);

        mCommunicateThread = new P2PCommunicateThread(this);
        mCommunicateThread.start();
    }

    public void start() {
        try {
            sendMessage2Peer(InetAddress.getByName(P2PConstant.MULTI_ADDRESS), ON_LINE, null);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            mCallback.onError(e);
        }
    }

    public void stop() {
        try {
            sendMessage2Peer(InetAddress.getByName(P2PConstant.MULTI_ADDRESS), OFF_LINE, null);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            mCallback.onError(e);
        }
        releaseMosaicThread();
        if (mCommunicateThread != null) mCommunicateThread.quit();
    }

    private void releaseMosaicThread() {
        if (mWorkThread != null) {
            mWorkThread.getLooper().quit();
            mWorkThread = null;
        }
    }

    public void sendMessage2Peer(InetAddress sendTo, int cmd, String add) {
        mWorkHandler.sendMessage(mWorkHandler.obtainMessage(SEND_MESSGAE, getMsg(sendTo, cmd, add)));
    }

    private SendMsg getMsg(InetAddress sendTo, int cmd, String add) {
        SigMessage sigMessage = getSelfMsg(cmd, mSelf);
        sigMessage.addition = TextUtils.isEmpty(add) ? "null" : add;
        sigMessage.recipient = 0;
        SendMsg msg = new SendMsg();
        msg.message = sigMessage.toProtocolString();
        msg.sendTo = sendTo;
        return msg;
    }

    private SigMessage getSelfMsg(int cmd, P2PNeighbor self) {
        SigMessage msg = new SigMessage();
        msg.commandNum = cmd;
        if (self != null) {
            msg.senderAlias = self.alias;
            msg.senderIp = self.ip;
            msg.senderAvatar = self.avatar;
            msg.wifiMac = self.wifiMac;
            msg.brand = self.brand;
            msg.mode = self.mode;
            msg.sdkInt = self.sdkInt;
            msg.versionCode = self.versionCode;
            msg.databaseVersion = self.databaseVersion;
        }
        return msg;
    }

    @Override
    public void onParseMessage(ParamIPMsg ipMsg) {
        mHandler.sendMessage(mHandler.obtainMessage(PARSE_MESSGAE, ipMsg));
    }

    @Override
    public void onError(Exception e) {
        mHandler.sendMessage(mHandler.obtainMessage(ERROR_MESSGAE, e));
    }

    public interface Callback {
        public void onParseMessage(ParamIPMsg ipMsg);

        public void onError(Exception e);
    }

    /**
     * This Handler is used to post message back onto the main thread of the
     * application
     */
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PARSE_MESSGAE:
                    mCallback.onParseMessage((ParamIPMsg) msg.obj);
                    break;
                case ERROR_MESSGAE:
                    mCallback.onError((Exception) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    private class WorkThread extends HandlerThread implements
            Handler.Callback {

        public WorkThread() {
            super(TAG, Thread.MAX_PRIORITY);
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_MESSGAE:
                    SendMsg sendMessage = (SendMsg) msg.obj;
                    mCommunicateThread.sendUdpData(sendMessage.message, sendMessage.sendTo);
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    private class SendMsg {
        InetAddress sendTo;
        String message;
    }
}
