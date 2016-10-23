package com.ape.p2p;


import com.ape.p2p.bean.P2PFileInfo;
import com.ape.p2p.bean.P2PNeighbor;
import com.ape.p2p.bean.ParamIPMsg;
import com.ape.p2p.core.communicate.CommunicateManager;
import com.ape.p2p.core.peer.PeerManager;
import com.ape.p2p.core.receive.ReceiveManager;
import com.ape.p2p.core.send.SendManager;

import java.net.InetAddress;
import java.util.Map;

import static com.ape.p2p.util.P2PConstant.CommandNum.OFF_LINE;
import static com.ape.p2p.util.P2PConstant.CommandNum.ON_LINE;
import static com.ape.p2p.util.P2PConstant.CommandNum.ON_LINE_ANS;
import static com.ape.p2p.util.P2PConstant.CommandNum.RECEIVE_ABORT_SELF;
import static com.ape.p2p.util.P2PConstant.CommandNum.RECEIVE_FILE_ACK;
import static com.ape.p2p.util.P2PConstant.CommandNum.SEND_ABORT_SELF;
import static com.ape.p2p.util.P2PConstant.CommandNum.SEND_FILE_REQ;
import static com.ape.p2p.util.P2PConstant.CommandNum.SEND_FILE_START;


/**
 * Created by android on 16-10-20.
 */

public class P2PManager implements IP2PManager, PeerManager.Callback, CommunicateManager.Callback,
        SendManager.Callback, ReceiveManager.Callback {
    private static final String TAG = "P2PManager";
    private static P2PManager mP2PManager = new P2PManager();
    private CommunicateManager mCommunicateManager;
    private PeerManager mPeerManager;
    private SendManager mSendManager;
    private ReceiveManager mReceiveManager;
    private NeighborCallback mNeighborsCallback;
    private SendFileCallback mSendFileCallback;
    private ReceiveFileCallback mReceiveFileCallback;

    private P2PManager() {

    }

    public static P2PManager getInstance() {
        return mP2PManager;
    }

    @Override
    public void start(P2PNeighbor self, NeighborCallback neighborCallback) {
        mNeighborsCallback = neighborCallback;
        mCommunicateManager = new CommunicateManager(self, this);
        mPeerManager = new PeerManager(this);
        mSendManager = new SendManager(this);
        mReceiveManager = new ReceiveManager(this);
    }

    @Override
    public void stop() {
        mCommunicateManager.stop();
        mPeerManager.stop();
    }

    @Override
    public void sendFile(P2PNeighbor[] dsts, P2PFileInfo[] files, SendFileCallback callback) {
        mSendFileCallback = callback;
    }

    @Override
    public void ackReceive() {
        //sendMessage2Peer();
    }

    @Override
    public void receiveFile(ReceiveFileCallback callback) {
        mReceiveFileCallback = callback;
    }

    @Override
    public void cancelReceive() {

    }

    @Override
    public void cancelSend(P2PNeighbor neighbor) {

    }

    @Override
    public Map<String, P2PNeighbor> getNeighbors() {
        return mP2PManager.getNeighbors();
    }

    @Override
    public void sendMessage2Peer(InetAddress sendTo, int cmd, String add) {
        mCommunicateManager.sendMessage2Peer(sendTo, cmd, add);
    }

    @Override
    public void onParseMessage(ParamIPMsg ipMsg) {
        switch (ipMsg.peerMSG.commandNum) {
            case ON_LINE: //收到上线广播
            case ON_LINE_ANS: //收到对方上线的回复
            case OFF_LINE://收到离线的消息
                mPeerManager.onParseMessage(ipMsg);
                break;
            case SEND_FILE_REQ://发送文件请求
            case SEND_ABORT_SELF://取消文件发送
            case RECEIVE_FILE_ACK://对方接受请求
            case RECEIVE_ABORT_SELF: //接收者退出
                mSendManager.onParseMessage(ipMsg);
                break;
            case SEND_FILE_START://接收端收到开始发送文件的消息
                //case SEND_ABORT_SELF: //发送者退出
                //case RECEIVE_FILE_ACK: //发送接收文件的消息给发送者
                mReceiveManager.onParseMessage(ipMsg);
                break;
            default:
                break;
        }
    }

    @Override
    public void onError(Exception e) {
        // TODO: 2016/10/22 update ui
    }

    @Override
    public void onNeighborFound(P2PNeighbor neighbor) {
        mNeighborsCallback.onNeighborFound(neighbor);
    }

    @Override
    public void onNeighborRemoved(P2PNeighbor neighbor) {
        mNeighborsCallback.onNeighborRemoved(neighbor);
    }
}
