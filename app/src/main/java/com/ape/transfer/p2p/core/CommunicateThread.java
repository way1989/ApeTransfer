package com.ape.transfer.p2p.core;


import android.text.TextUtils;
import android.util.Log;

import com.ape.transfer.p2p.beans.Peer;
import com.ape.transfer.p2p.beans.SigMessage;
import com.ape.transfer.p2p.beans.param.ParamIPMsg;
import com.ape.transfer.p2p.util.Constant;

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
 * Created by way on 2016/10/19. 接收端和发送端的udp交互
 */
public class CommunicateThread extends Thread {
    private static final String TAG = "CommunicateThread";

    private WorkHandler mWorkHandler;

    private DatagramSocket mUdpSocket;
    private DatagramPacket mReceivePacket;
    private byte[] RECEIVE_BUFFER = new byte[Constant.BUFFER_LENGTH];
    private ArrayList<String> mLocalIPs;
    private boolean isStopped = false;
    private Peer mSelf;


    public CommunicateThread(Peer self, WorkHandler handler) {
        mSelf = self;
        mWorkHandler = handler;
        setPriority(MAX_PRIORITY);
        init();
    }

    public void broadcastMSG(int cmd, int recipient) {
        try {
            sendMsg2Peer(InetAddress.getByName(Constant.MULTI_ADDRESS)
                    /*P2PManager.getBroadcastAddress(mContext)*/, cmd, recipient, null);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg2Peer(InetAddress sendTo, int cmd, int recipient, String add) {
        SigMessage sigMessage = getSelfMsg(cmd);
        if (add == null)
            sigMessage.addition = "null";
        else
            sigMessage.addition = add;
        sigMessage.recipient = recipient;

        sendUdpData(sigMessage.toProtocolString(), sendTo);
    }

    private synchronized void sendUdpData(String sendStr, InetAddress sendTo) {
        try {
            byte[] sendBuffer = sendStr.getBytes(Constant.FORMAT);
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, sendTo,
                    Constant.PORT);
            if (mUdpSocket != null) {
                mUdpSocket.send(sendPacket);
                Log.d(TAG, "send udp data = " + sendStr + "; sendto = " + sendTo.getHostAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        mLocalIPs = getLocalAllIP();
        try {
            mUdpSocket = new DatagramSocket(null);
            mUdpSocket.setReuseAddress(true);
            mUdpSocket.bind(new InetSocketAddress(Constant.PORT));
        } catch (SocketException e) {
            e.printStackTrace();
            if (mUdpSocket != null) {
                mUdpSocket.close();
                isStopped = true;
                return;
            }
        }
        mReceivePacket = new DatagramPacket(RECEIVE_BUFFER, Constant.BUFFER_LENGTH);
        isStopped = false;
    }

    private SigMessage getSelfMsg(int cmd) {
        SigMessage msg = new SigMessage();
        msg.commandNum = cmd;
        Peer melonInfo = mSelf;
        if (melonInfo != null) {
            msg.senderAlias = melonInfo.alias;
            msg.senderIp = melonInfo.ip;
            msg.senderAvatar = melonInfo.avatar;
            msg.wifiMac = melonInfo.wifiMac;
            msg.brand = melonInfo.brand;
            msg.mode = melonInfo.mode;
            msg.sdkInt = melonInfo.sdkInt;
            msg.versionCode = melonInfo.versionCode;
            msg.databaseVersion = melonInfo.databaseVersion;
        }

        return msg;
    }

    @Override
    public void run() {
        while (!isStopped) {
            try {
                mUdpSocket.receive(mReceivePacket);
            } catch (IOException e) {
                e.printStackTrace();
                isStopped = true;
                break;
            }
            if (mReceivePacket == null || mReceivePacket.getLength() == 0) continue;//空消息直接忽略

            String ip = mReceivePacket.getAddress().getHostAddress();
            if (TextUtils.isEmpty(ip) || isLocal(ip)) continue;//空ip或者自己ip的消息直接忽略

            String strReceive;
            try {
                strReceive = new String(RECEIVE_BUFFER, 0, mReceivePacket.getLength(), Constant.FORMAT);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                continue;
            }
            if (TextUtils.isEmpty(strReceive)) continue;//空消息直接忽略

            if (isStopped) break;//如果已经停止就跳出循环

            Log.d(TAG, "sig communicate process received udp message = " + strReceive);
            ParamIPMsg msg = new ParamIPMsg(strReceive, mReceivePacket.getAddress(),
                    mReceivePacket.getPort());
            mWorkHandler.send2Handler(msg.peerMSG.commandNum, Constant.Src.COMMUNICATE,
                    msg.peerMSG.recipient, msg);

            if (mReceivePacket != null)
                mReceivePacket.setLength(Constant.BUFFER_LENGTH);
        }

        release();
    }

    public void quit() {
        isStopped = true;
        release();
    }

    private void release() {
        Log.d(TAG, "sigCommunicate release");
        if (mUdpSocket != null)
            mUdpSocket.close();
        if (mReceivePacket != null)
            mReceivePacket = null;
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