package com.ape.transfer.p2p.beans;


import com.ape.transfer.p2p.core.P2PManager;

import java.io.File;


/**
 * Created by way on 2016/10/16.
 * android设备中的文件
 */
public class TransferFile {
    public String path;
    public String name;
    public long size;
    public int type;


    public String wifiMac;
    public String savePath;
    public String md5;
    public String thumbUrl;
    public String mineType;
    public long position;
    public long lastModify;
    public long createTime;
    public int status;
    public int read;
    public int deleted;
    public int direction;

    public TransferFile() {

    }

    public TransferFile(String string) {
        String str[] = string.split(":");
        name = str[0];
        path = str[1];
        md5 = str[2];
        wifiMac = str[3];
        createTime = Long.parseLong(str[4]);
        thumbUrl = str[5];
        size = Long.parseLong(str[6]);
        type = Integer.parseInt(str[7]);

        savePath = P2PManager.getSavePath(type) + File.separator + name;
    }

    @Override
    public boolean equals(Object o) {
        TransferFile file = (TransferFile) o;
        return (file.name.equals(name) && file.size == size && file.type == type
                && file.md5.equals(md5) && file.path.equals(path));
    }

    @Override
    public String toString() {
        return name + ":" + path + ":" + md5 + ":" + wifiMac + ":" + createTime + ":"
                + thumbUrl + ":" + size + ":" + type + "\0";
    }

    /**
     * 文件传输的状态
     */
    public interface Status {
        int STATUS_READY = 0;
        int STATUS_SENDING = 1;
        int STATUS_RECEIVING = 2;
        int STATUS_FAILED = 3;
        int STATUS_SCUEED = 4;
    }

    /**
     * 文件传输的方向，发送或接收
     */
    public interface Direction {
        int DIRECTION_SEND = 0;
        int DIRECTION_RECEIVE = 1;
    }
}
