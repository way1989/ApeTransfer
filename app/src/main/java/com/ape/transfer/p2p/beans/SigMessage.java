package com.ape.transfer.p2p.beans;


import com.ape.transfer.p2p.util.Constant;
import com.ape.transfer.util.Log;


/**
 * Created by way on 2016/10/20.
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
        return packetNum + ":" + senderAlias + ":" + senderIp + ":" + senderAvatar + ":" + wifiMac
                + ":" + mode + ":" + brand + ":" + sdkInt + ":" + versionCode + ":"
                + databaseVersion + ":" + commandNum + ":" + recipient + ":" + addition
                + Constant.MSG_SEPARATOR;
    }

}
