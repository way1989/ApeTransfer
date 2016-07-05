package com.ape.transfer.p2p.p2pentity;


import com.ape.transfer.p2p.p2pcore.P2PManager;

import java.io.File;


/**
 * Created by 郭攀峰 on 2015/9/16.
 * android设备中的文件
 */
public class P2PFileInfo {
    public String path;
    public String name;
    public long size;
    public int type;
    public int percent;
    public boolean success;
    public long LengthNeeded = 0;


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

    public P2PFileInfo() {

    }

    public P2PFileInfo(String string) {
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

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
        if (percent == 100) {
            success = true;
        }
    }

    @Override
    public boolean equals(Object o) {
        return (((P2PFileInfo) (o)).name.equals(name))
                && (((P2PFileInfo) (o)).size == size)
                && (((P2PFileInfo) (o)).type == type)
                &&(((P2PFileInfo) (o)).md5.equals(md5))
                && (((P2PFileInfo) (o)).path.equals(path));
    }

    @Override
    public String toString() {
        return name + ":" + path + ":" + md5 + ":" + wifiMac + ":" + createTime + ":" + thumbUrl + ":" + size + ":" + type + "\0";
    }

    public P2PFileInfo duplicate() {
        P2PFileInfo file = new P2PFileInfo();

        file.name = this.name;
        file.size = this.size;
        file.path = this.path;
        file.type = this.type;
        file.percent = this.percent;
        file.success = this.success;
        file.LengthNeeded = this.LengthNeeded;

        return file;
    }

    public interface Status{
        public static final int STATUS_READY = 0;
        public static final int STATUS_SENDING = 1;
        public static final int STATUS_RECEIVING = 2;
        public static final int STATUS_FAILED = 3;
        public static final int STATUS_SCUEED = 4;
    }

    public interface Direction{
        public static final int DIRECTION_SEND = 0;
        public static final int DIRECTION_RECEIVE = 1;
    }
}
