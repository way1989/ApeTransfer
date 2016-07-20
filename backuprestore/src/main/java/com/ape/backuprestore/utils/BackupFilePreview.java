/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.ape.backuprestore.utils;

import android.content.Context;
import android.text.TextUtils;

import com.ape.backuprestore.RecordXmlComposer;
import com.ape.backuprestore.RecordXmlInfo;
import com.ape.backuprestore.RecordXmlParser;
import com.ape.backuprestore.modules.CalendarRestoreComposer;
import com.ape.backuprestore.modules.Composer;
import com.ape.backuprestore.modules.ContactRestoreComposer;
import com.ape.backuprestore.modules.MessageRestoreComposer;
import com.ape.backuprestore.modules.MusicRestoreComposer;
import com.ape.backuprestore.modules.NoteBookRestoreComposer;
import com.ape.backuprestore.modules.PictureRestoreComposer;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class BackupFilePreview {
    private final String CLASS_TAG = MyLogger.LOG_TAG + "/BackupFilePreview";
    private final int UN_PARESED_TYPE = -1;

    private File mFolderName = null;
    private long mSize = 0;
    private int mTypes = UN_PARESED_TYPE;
    private String backupTime;
    private boolean mIsRestored = false;
    private boolean mIsOtherBackup = true;
    private boolean mIsSelfBackup = true;
    private HashMap<Integer, Integer> mNumberMap = new HashMap<>();

    public BackupFilePreview(File file) {
        if (file == null) {
            MyLogger.logE(CLASS_TAG, "constractor error! file is null");
            return;
        }
        mNumberMap.clear();
        mFolderName = file;
        MyLogger.logI(CLASS_TAG, "new BackupFilePreview: file is " + file.getAbsolutePath());
        computeSize();
        checkRestored();
    }

    private void computeSize() {
        mSize = FileUtils.computeAllFileSizeInFolder(mFolderName);
        MyLogger.logI(CLASS_TAG, "new BackupFilePreview: size = " + mSize);
    }

    private void checkRestored() {
        mIsRestored = false;
        mIsOtherBackup = true;
        mIsSelfBackup = true;
        String xmlFilePath = mFolderName + File.separator + Constants.RECORD_XML;
        File recordXmlFile = new File(xmlFilePath);
        if (!recordXmlFile.exists()) {
            addToCurrentBackupHistory(xmlFilePath);
            return;
        }

        String content = Utils.readFromFile(xmlFilePath);
        ArrayList<RecordXmlInfo> recordList;
        if (!TextUtils.isEmpty(content)) {
            recordList = RecordXmlParser.parse(content);
            if (recordList != null && recordList.size() > 0) {
                if (recordList.size() > 1) {
                    mIsSelfBackup = false;
                }
                String currentDevice = Utils.getPhoneSearialNumber();
                for (RecordXmlInfo record : recordList) {
                    if (record.getDevice().equals(currentDevice)) {
                        mIsOtherBackup = false;
                        if (record.isRestore()) {
                            mIsRestored = true;
                        }
                    }
                }
                if (mIsOtherBackup) {
                    addCurrentSN(recordList, xmlFilePath);
                }
            }
        }
        MyLogger.logI(CLASS_TAG, "mIsRestored = " + mIsRestored + ", mIsOtherBackup = " + mIsOtherBackup
                + ", mIsSelfBackup = " + mIsSelfBackup);
    }

    public boolean isSelfBackup() {
        return mIsSelfBackup;
    }

    private void addToCurrentBackupHistory(String xmlFilePath) {
        MyLogger.logD(CLASS_TAG, "addToCurrentBackupHistory() xmlFilePath : "
                + xmlFilePath);
        RecordXmlInfo backupInfo = new RecordXmlInfo();
        backupInfo.setRestore(false);
        backupInfo.setDevice(Utils.getPhoneSearialNumber());
        backupInfo.setTime("" + System.currentTimeMillis());
        RecordXmlComposer xmlCompopser = new RecordXmlComposer();
        xmlCompopser.startCompose();
        xmlCompopser.addOneRecord(backupInfo);
        xmlCompopser.endCompose();
        if (xmlFilePath != null && xmlFilePath.length() > 0) {
            Utils.writeToFile(xmlCompopser.getXmlInfo(), xmlFilePath);
        }
    }

    /**
     * add this phone's SN info to record.xml
     *
     * @return add success return true, otherwise false.
     */
    private boolean addCurrentSN(List<RecordXmlInfo> recordList, String path) {
        // TODO Auto-generated method stub
        boolean success = false;
        try {
            RecordXmlInfo restoreInfo = new RecordXmlInfo();
            restoreInfo.setRestore(false);
            restoreInfo.setDevice(Utils.getPhoneSearialNumber());
            restoreInfo.setTime(String.valueOf(System.currentTimeMillis()));

            recordList.add(restoreInfo);

            RecordXmlComposer xmlCompopser = new RecordXmlComposer();
            xmlCompopser.startCompose();
            for (RecordXmlInfo record : recordList) {
                xmlCompopser.addOneRecord(record);
            }
            xmlCompopser.endCompose();
            Utils.writeToFile(xmlCompopser.getXmlInfo(), path);
            success = true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return success;
        }
        MyLogger.logI(CLASS_TAG, "addCurrentSN() success : " + success);
        return success;
    }

    public boolean isRestored() {
        return mIsRestored;
    }

    public boolean isOtherDeviceBackup() {
        MyLogger.logD(CLASS_TAG, "isOtherDeviceBackup() : " + mIsOtherBackup);
        return mIsOtherBackup;
    }

    public File getFile() {
        return mFolderName;
    }

    public String getFileName() {
        String showNameString = mFolderName.getName();
        if (showNameString != null && showNameString.length() == 14
                && showNameString.trim().length() == 14) {
            try {
                Double.parseDouble(showNameString);
                String showNameString2 = formatString(showNameString);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateFormat.setLenient(false);
                try {
                    Date date = dateFormat.parse(showNameString2);
                    showNameString = showNameString2;
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NullPointerException ne) {
                    // TODO Auto-generated catch block
                    ne.printStackTrace();
                }
            } catch (NumberFormatException e) {
                return showNameString;
            }
        }
        return showNameString;
    }

    private String formatString(String showNameString) {
        // TODO Auto-generated method stub
        if (showNameString != null) {
            String yearString = showNameString.substring(0, 4);
            String monthString = showNameString.substring(4, 6);
            String dayString = showNameString.substring(6, 8);
            String hourString = showNameString.substring(8, 10);
            String minString = showNameString.substring(10, 12);
            String secrString = showNameString.substring(12, 14);
            return yearString + "-" + monthString + "-" + dayString + "  " + hourString + ":"
                    + minString + ":" + secrString;
        }
        return null;
    }

    public String getBackupTime() {
        if (backupTime == null) {
            Long time = mFolderName.lastModified();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            backupTime = dateFormat.format(new Date(time));
        }
        return backupTime;
    }

    public void setBackupTime(String backupTime) {
        this.backupTime = backupTime;
    }

    public long getFileSize() {
        return mSize;
    }

    public int getBackupModules(Context context) {
        if (mTypes == UN_PARESED_TYPE) {
            mTypes = peekBackupModules(context);
        }
        return mTypes;
    }

    /**
     * parse backup items.
     */
    private int peekBackupModules(Context context) {

        File[] files = mFolderName.listFiles();
        mTypes = 0;
        if (files != null) {
            for (File file : files) {
                String[] moduleFolders = new String[]{
                        Constants.ModulePath.FOLDER_CALENDAR,
                        Constants.ModulePath.FOLDER_CONTACT,
                        Constants.ModulePath.FOLDER_MMS,
                        Constants.ModulePath.FOLDER_MUSIC,
                        Constants.ModulePath.FOLDER_PICTURE,
                        Constants.ModulePath.FOLDER_SMS
                        //, ModulePath.FOLDER_BOOKMARK
                };

                int[] moduleTypes = new int[]{
                        ModuleType.TYPE_CALENDAR,
                        ModuleType.TYPE_CONTACT,
                        ModuleType.TYPE_MESSAGE,
                        ModuleType.TYPE_MUSIC,
                        ModuleType.TYPE_PICTURE,
                        ModuleType.TYPE_MESSAGE
                        //, ModuleType.TYPE_BOOKMARK
                };

                if (file.isDirectory() && !FileUtils.isEmptyFolder(file)) {
                    String name = file.getName();
                    int count = moduleFolders.length;
                    for (int index = 0; index < count; index++) {
                        if (moduleFolders[index].equalsIgnoreCase(name)) {
                            initNumByType(context, moduleTypes[index]);
                            if (getItemCount(moduleTypes[index]) > 0) {
                                mTypes |= moduleTypes[index];
                            }
                        }
                    }
                }
            }
        }
        MyLogger.logI(CLASS_TAG, "parseItemTypes: mTypes =  " + mTypes);
        return mTypes;
    }

    private void initNumByType(Context context, final int type) {
        Composer composer = null;
        switch (type) {
            case ModuleType.TYPE_CONTACT:
                composer = new ContactRestoreComposer(context);
                break;

            case ModuleType.TYPE_CALENDAR:
                composer = new CalendarRestoreComposer(context);
                break;

            case ModuleType.TYPE_MESSAGE:
                composer = new MessageRestoreComposer(context);
                break;

            case ModuleType.TYPE_MUSIC:
                composer = new MusicRestoreComposer(context);
                break;

            case ModuleType.TYPE_NOTEBOOK:
                composer = new NoteBookRestoreComposer(context);
                break;

            case ModuleType.TYPE_PICTURE:
                composer = new PictureRestoreComposer(context);
                break;

//            case ModuleType.TYPE_BOOKMARK:
//                composer = new BookmarkRestoreComposer(context);
//                break;

            default:
                break;
        }
        if (composer != null) {
            composer.setParentFolderPath(mFolderName.getAbsolutePath());
            composer.init();
            int count = composer.getCount();
            MyLogger.logD(CLASS_TAG, "initNumByType: count = " + count);
            mNumberMap.put(type, count);
        }
    }

    public int getItemCount(int type) {
        MyLogger.logD(CLASS_TAG, "list.get(0).type = " + type);
        MyLogger.logD(CLASS_TAG, "mNumberMap.size() = " + mNumberMap.size());
        int count = 0;
        if (mNumberMap.get(type) != null) {
            count = mNumberMap.get(type);
        }

        MyLogger.logD(CLASS_TAG, "getItemCount: type = " + type + ",count = " + count);
        return count;
    }
}
