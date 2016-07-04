package com.ape.transfer.p2p.p2pentity;


import com.ape.transfer.p2p.p2pcore.P2PManager;

import java.io.File;


/**
 * Created by 郭攀峰 on 2015/9/16.
 * android设备中的文件
 */
public class P2PFileInfo {
    public String savePath;
    public String name;
    public long size;
    public int type;
    public int percent;
    public boolean success;
    public long LengthNeeded = 0;


    public String wifiMac;
    public String filePath;
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
        filePath = str[1];
        md5 = str[2];
        lastModify = Long.parseLong(str[3]);
        thumbUrl = str[4];
        size = Long.parseLong(str[5]);
        type = Integer.parseInt(str[6
                ]);

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
                && (((P2PFileInfo) (o)).savePath.equals(savePath));
    }

    @Override
    public String toString() {
        return name + ":" + filePath + ":" + md5 + ":" + lastModify + ":" + thumbUrl + ":" + size + ":" + type + "\0";
    }

    public P2PFileInfo duplicate() {
        P2PFileInfo file = new P2PFileInfo();

        file.name = this.name;
        file.size = this.size;
        file.savePath = this.savePath;
        file.type = this.type;
        file.percent = this.percent;
        file.success = this.success;
        file.LengthNeeded = this.LengthNeeded;

        return file;
    }
}
