package com.ape.p2p.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by way on 2016/10/21.
 */

public class Util {
    private static String SAVE_DIR = Environment.getExternalStorageDirectory().getPath()
            + File.separator + P2PConstant.FILE_SHARE_SAVE_PATH;


    public static String getSavePath(int type) {
        String[] typeStr = {"APP", "Picture", "Video", "Zip", "Document", "Music"};
        return SAVE_DIR + File.separator + typeStr[type];
    }
}
