package com.ape.backuprestore.utils;


import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;


public class StorageUtils {

    public final static int MINIMUM_SIZE = 512;
    private static final String CLASS_TAG = "StorageUtils";

    public static String getStoragePath() {
        String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "ApeTransfer" + File.separator + Constants.ModulePath.FOLDER_BACKUP;
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

    public static String getBackupPath() {
        return getStoragePath();
    }

    public static long getAvailableSize(String file) {
        android.os.StatFs stat = new android.os.StatFs(file);
        long count = stat.getAvailableBlocks();
        long size = stat.getBlockSize();
        long totalSize = count * size;
        MyLogger.logD(CLASS_TAG, "file remain size = " + totalSize);
        return totalSize;
    }

    public static boolean isStorageMissing() {
        boolean isStorageMissing = false;
        String path = getStoragePath();
        if (path == null) {
            isStorageMissing = true;
        } else {
            // create file to check for sure
            File temp = new File(path + File.separator + ".temp");
            if (temp.exists()) {
                if (!temp.delete()) {
                    isStorageMissing = true;
                }
            } else {
                try {
                    if (!temp.createNewFile()) {
                        isStorageMissing = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    MyLogger.logE(CLASS_TAG, "Cannot create temp file");
                    isStorageMissing = true;
                } finally {
                    temp.delete();
                }
            }
        }
        return isStorageMissing;
    }

    /*
     * If SD card is removed or full, kill this process
     */
    public static void killProcessIfNecessary() {
        if (isStorageMissing()) {
            Log.i(CLASS_TAG, "SD card removed, kill process");
            Utils.killMyProcess();
        } else {
            String path = getStoragePath();
            if (getAvailableSize(path) <= MINIMUM_SIZE) {
                Log.i(CLASS_TAG, "SD full, kill process");
                Utils.killMyProcess();
            }
        }
        Log.i(CLASS_TAG, "SD card OK, no need to kill process");
    }
}

