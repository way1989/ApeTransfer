package com.ape.backuprestore.utils;


import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;


public class SDCardUtils {

    public final static int MINIMUM_SIZE = 512;
    private static final String CLASS_TAG = "SDCardUtils";

    public static String getSavePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }


    public static String getStoragePath(Context context) {
        String storagePath = getSavePath();
        storagePath = storagePath + File.separator + "ApeTransfer";
        MyLogger.logD(CLASS_TAG, "getStoragePath: path is " + storagePath);
        File file = new File(storagePath);
        if (file.exists() && file.isDirectory()) {
            return storagePath;
        } else if (file.mkdir()) {
            return storagePath;
        } else {
            return null;
        }
    }

    public static String getPersonalDataBackupPath(Context context) {
        String path = getStoragePath(context);
        if (path != null) {
            return path + File.separator + Constants.ModulePath.FOLDER_BACKUP;
        }

        return path;
    }

    public static String getAppsBackupPath(Context context) {
        String path = getStoragePath(context);
        MyLogger.logD(CLASS_TAG, "getAppsBackupPath path = " + path);
        if (path != null) {
            return path + File.separator + Constants.ModulePath.FOLDER_APP;
        }
        return path;
    }

    public static boolean isSdCardAvailable(Context context) {
        return (getStoragePath(context) != null);
    }

    public static long getAvailableSize(String file) {
        android.os.StatFs stat = new android.os.StatFs(file);
        long count = stat.getAvailableBlocks();
        long size = stat.getBlockSize();
        long totalSize = count * size;
        MyLogger.logD(CLASS_TAG, "file remain size = " + totalSize);
        return totalSize;
    }

    public static boolean isSdCardMissing(Context context) {
        boolean isSDCardMissing = false;
        String path = getStoragePath(context);
        if (path == null) {
            isSDCardMissing = true;
        } else {
            // create file to check for sure
            File temp = new File(path + File.separator + ".temp");
            if (temp.exists()) {
                if (!temp.delete()) {
                    isSDCardMissing = true;
                }
            } else {
                try {
                    if (!temp.createNewFile()) {
                        isSDCardMissing = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    MyLogger.logE(CLASS_TAG, "Cannot create temp file");
                    isSDCardMissing = true;
                } finally {
                    temp.delete();
                }
            }
        }
        return isSDCardMissing;
    }

    /*
     * If SD card is removed or full, kill this process
     */
    public static void killProcessIfNecessary(Context context) {
        if (isSdCardMissing(context)) {
            Log.i(CLASS_TAG, "SD card removed, kill process");
            Utils.killMyProcess();
        } else {
            String path = getStoragePath(context);
            if (getAvailableSize(path) <= MINIMUM_SIZE) {
                Log.i(CLASS_TAG, "SD full, kill process");
                Utils.killMyProcess();
            }
        }
        Log.i(CLASS_TAG, "SD card OK, no need to kill process");
    }
}

