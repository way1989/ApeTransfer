package com.ape.p2p.bean;



import android.util.Log;

import com.ape.p2p.util.P2PConstant;

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
    public int senderAvatar;
    public String wifiMac;//发送者的wifi mac地址
    public String mode;//发送者的mode
    public String brand;//发送者的brand
    public int sdkInt;//发送者的sdkInt
    public int versionCode;//发送者的versionCode
    public int databaseVersion;//发送者的databaseVersion
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
        this.packetNum = String.valueOf(System.currentTimeMillis());
    }

    public SigMessage(String protocolString) {
        protocolString = protocolString.trim();
        String[] args = protocolString.split(":");

        packetNum = args[0];
        senderAlias = args[1];
        senderIp = args[2];
        senderAvatar = Integer.parseInt(args[3]);
        wifiMac = args[4];
        mode = args[5];
        brand = args[6];
        sdkInt = Integer.parseInt(args[7]);
        versionCode = Integer.parseInt(args[8]);
        databaseVersion = Integer.parseInt(args[9]);

        commandNum = Integer.parseInt(args[10]);
        recipient = Integer.parseInt(args[11]);
        if (args.length > 12)
            addition = args[12];
        else
            addition = null;

        for (int i = 13; i < args.length; i++) {
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
        sb.append(senderAvatar);
        sb.append(":");
        sb.append(wifiMac);
        sb.append(":");
        sb.append(mode);
        sb.append(":");
        sb.append(brand);
        sb.append(":");
        sb.append(sdkInt);
        sb.append(":");
        sb.append(versionCode);
        sb.append(":");
        sb.append(databaseVersion);
        sb.append(":");

        sb.append(commandNum);
        sb.append(":");
        sb.append(recipient);
        sb.append(":");
        sb.append(addition);
        sb.append(P2PConstant.MSG_SEPARATOR);

        return sb.toString();
    }

}
