package com.ape.p2p.core.communicate;

import android.text.TextUtils;
import android.util.Log;

import com.ape.p2p.bean.ParamIPMsg;
import com.ape.p2p.util.P2PConstant;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
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
    private Callback mCallback;
    private DatagramSocket mUDPSocket;
    private DatagramPacket mReceivePacket;
    private ArrayList<String> mLocalIPs;
    private boolean isStopped = false;

    public P2PCommunicateThread(Callback workHandler) {
        setPriority(Thread.MAX_PRIORITY);
        mCallback = workHandler;

        mLocalIPs = getLocalAllIP();

        try {
            mUDPSocket = new DatagramSocket(null);
            mUDPSocket.setReuseAddress(true);
            mUDPSocket.bind(new InetSocketAddress(PORT));
        } catch (SocketException e) {
            e.printStackTrace();
            //mCallback.sendMessage(mCallback.obtainMessage(P2PConstant.Message.ERROR, e));
            mCallback.onError(e);
        }

        mReceivePacket = new DatagramPacket(RECEIVE_BUFFER, BUFFER_LENGTH);
        isStopped = false;
    }

//    public void broadcastMSG(int cmd, int recipient) {
//        try {
//            sendMsg2Peer(InetAddress.getByName(P2PConstant.MULTI_ADDRESS), cmd, recipient, null);
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//            mCallback.onError(e);
//        }
//    }


    public synchronized void sendUdpData(String sendStr, InetAddress sendTo) {
        Log.d(TAG, "send upd data = " + sendStr + "; sendto = " + sendTo.getHostAddress());
        try {
            byte[] sendBuffer = sendStr.getBytes(P2PConstant.FORMAT);
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, sendTo,
                    P2PConstant.PORT);
            if (mUDPSocket != null) mUDPSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            mCallback.onError(e);
        }

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
                //mCallback.sendMessage(mCallback.obtainMessage(P2PConstant.Message.ERROR, e));
                mCallback.onError(e);
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
            //mCallback.sendMessage(mCallback.obtainMessage(P2PConstant.Message.MESSAGE, msg));
            mCallback.onParseMessage(msg);

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

    interface Callback {
        void onParseMessage(ParamIPMsg ipMsg);

        void onError(Exception e);
    }
}
