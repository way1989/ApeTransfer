package com.ape.p2p.core.communicate;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.ape.p2p.bean.P2PNeighbor;
import com.ape.p2p.bean.ParamIPMsg;
import com.ape.p2p.bean.SigMessage;
import com.ape.p2p.util.P2PConstant;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by android on 16-10-20.
 */

public class P2PCommunicateThread extends Thread {
    private static final String TAG = "P2PCommunicateThread";
    private static final String FORMAT = "gbk";
    private static final int PORT = 10000;
    private static final int BUFFER_LENGTH = 8192;
    private static final byte[] RECEIVE_BUFFER = new byte[BUFFER_LENGTH];
    private Handler mWorkHandler;
    private P2PNeighbor mSelf;
    private DatagramSocket mUDPSocket;
    private DatagramPacket mReceivePacket;
    private ArrayList<String> mLocalIPs;
    private boolean isStopped = false;

    public P2PCommunicateThread(Handler workHandler, P2PNeighbor me) {
        setPriority(Thread.MAX_PRIORITY);
        mWorkHandler = workHandler;
        mSelf = me;

        mLocalIPs = getLocalAllIP();

        try {
            mUDPSocket = new DatagramSocket(null);
            mUDPSocket.setReuseAddress(true);
            mUDPSocket.bind(new InetSocketAddress(PORT));
        } catch (SocketException e) {
            e.printStackTrace();
            mWorkHandler.sendMessage(mWorkHandler.obtainMessage(P2PConstant.Message.ERROR, e));
        }

        mReceivePacket = new DatagramPacket(RECEIVE_BUFFER, BUFFER_LENGTH);
        isStopped = false;
    }

    public void broadcastMSG(int cmd, int recipient) {

        try {
            sendMsg2Peer(InetAddress.getByName(P2PConstant.MULTI_ADDRESS), cmd, recipient, null);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            mWorkHandler.sendMessage(mWorkHandler.obtainMessage(P2PConstant.Message.ERROR, e));
        }

    }

    public void sendMsg2Peer(InetAddress sendTo, int cmd, int recipient, String add) {
        SigMessage sigMessage = getSelfMsg(cmd, mSelf);
        sigMessage.addition = TextUtils.isEmpty(add) ? "null" : add;
        sigMessage.recipient = recipient;
        try {
            sendUdpData(sigMessage.toProtocolString(), sendTo);
        } catch (IOException e) {
            e.printStackTrace();
            mWorkHandler.sendMessage(mWorkHandler.obtainMessage(P2PConstant.Message.ERROR, e));
        }
    }

    private synchronized void sendUdpData(String sendStr, InetAddress sendTo) throws IOException {
        Log.d(TAG, "send upd data = " + sendStr + "; sendto = " + sendTo.getHostAddress());
        byte[] sendBuffer = sendStr.getBytes(P2PConstant.FORMAT);
        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, sendTo,
                P2PConstant.PORT);
        if (mUDPSocket != null) mUDPSocket.send(sendPacket);
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

    public void quit() {
        Log.d(TAG, "sigCommunicate release");
        isStopped = true;
        release();
    }

    private void release() {
        if (mUDPSocket != null)
            mUDPSocket.close();
        if (mReceivePacket != null)
            mReceivePacket = null;
    }

    @Override
    public void run() {
        super.run();
        while (!isStopped) {
            try {
                mUDPSocket.receive(mReceivePacket);
            } catch (IOException e) {
                e.printStackTrace();
                mWorkHandler.sendMessage(mWorkHandler.obtainMessage(P2PConstant.Message.ERROR, e));
                isStopped = true;
                break;
            }
            if (mReceivePacket == null || mReceivePacket.getLength() == 0) continue;//空消息直接忽略

            String ip = mReceivePacket.getAddress().getHostAddress();
            if (TextUtils.isEmpty(ip) || isLocal(ip)) continue;//空ip或者自己ip的消息直接忽略

            String strReceive;
            try {
                strReceive = new String(RECEIVE_BUFFER, 0, mReceivePacket.getLength(), FORMAT);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                continue;
            }
            if (TextUtils.isEmpty(strReceive)) continue;//空消息直接忽略

            if (isStopped) break;//如果已经停止就跳出循环

            Log.d(TAG, "sig communicate process received udp message = " + strReceive);
            ParamIPMsg msg = new ParamIPMsg(strReceive, mReceivePacket.getAddress());
            mWorkHandler.sendMessage(mWorkHandler.obtainMessage(P2PConstant.Message.MESSAGE, msg));

            //重置
            if (mReceivePacket != null)
                mReceivePacket.setLength(BUFFER_LENGTH);
        }
        release();
    }

    private boolean isLocal(String ip) {
        if (mLocalIPs == null || mLocalIPs.isEmpty())
            return false;
        return mLocalIPs.contains(ip);
    }

    private ArrayList<String> getLocalAllIP() {
        ArrayList<String> ipLists = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (inet.hasMoreElements()) {
                    InetAddress ip = inet.nextElement();
                    if (!ip.isLoopbackAddress())
                        ipLists.add(ip.getHostAddress());

                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipLists;
    }
}
