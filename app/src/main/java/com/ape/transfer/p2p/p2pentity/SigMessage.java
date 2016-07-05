package com.ape.transfer.p2p.p2pentity;


import com.ape.transfer.p2p.p2pconstant.P2PConstant;
import com.ape.transfer.util.Log;

import java.util.Date;


/**
 * Created by 郭攀峰 on 2015/9/17.
 * 局域网用户之间的upd消息
 */
public class SigMessage {
    private static final String TAG = "SigMessage";
    /**
     * 发送包的编号 时间即编号
     */
    public String packetNum;
    /**
     * 发送者的昵称
     */
    public String senderAlias;
    /**
     * 发送者的ip地址
     */
    public String senderIp;
    /**
     * 发送者的头像id
     */
    public int senderHead;
    /**
     *
     */
    public int commandNum;
    /**
     *
     */
    public int recipient;
    /**
     * 内容
     */
    public String addition;

    public SigMessage() {
        this.packetNum = getTime();
    }

    public SigMessage(String protocolString) {
        protocolString = protocolString.trim();
        String[] args = protocolString.split(":");

        packetNum = args[0];
        senderAlias = args[1];
        senderIp = args[2];
        senderHead = Integer.parseInt(args[3]);
        commandNum = Integer.parseInt(args[4]);
        recipient = Integer.parseInt(args[5]);
        if (args.length > 6)
            addition = args[6];
        else
            addition = null;

        for (int i = 7; i < args.length; i++) {
            addition += (":" + args[i]);
        }
        Log.i(TAG, "SigMessage addition = " + addition);
    }

    public String toProtocolString() {
        StringBuffer sb = new StringBuffer();
        sb.append(packetNum);
        sb.append(":");
        sb.append(senderAlias);
        sb.append(":");
        sb.append(senderIp);
        sb.append(":");
        sb.append(senderHead);
        sb.append(":");
        sb.append(commandNum);
        sb.append(":");
        sb.append(recipient);
        sb.append(":");
        sb.append(addition);
        sb.append(P2PConstant.MSG_SEPARATOR);

        return sb.toString();
    }

    private String getTime() {
        Date nowDate = new Date();
        return Long.toString(nowDate.getTime());
    }
}
