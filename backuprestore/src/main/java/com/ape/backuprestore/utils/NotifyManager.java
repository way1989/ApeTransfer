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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.ape.backuprestore.R;


public class NotifyManager {
    public static final int NOTIFY_NEW_DETECTION = 1;
    public static final int NOTIFY_BACKUPING = 2;
    public static final int NOTIFY_RESTORING = 3;
    public static final int FP_NEW_DETECTION_NOTIFY_TYPE_DEAFAULT = 0;
    public static final int FP_NEW_DETECTION_NOTIFY_TYPE_LIST = 1;
    public static final String FP_NEW_DETECTION_INTENT_LIST =
            "com.mediatek.backuprestore.intent.MainActivity";
    public static final String BACKUP_PERSONALDATA_INTENT =
            "com.mediatek.backuprestore.intent.PersonalDataBackupActivity";
    public static final String BACKUP_APPLICATION_INTENT =
            "com.mediatek.backuprestore.intent.AppBackupActivity";
    public static final String RESTORE_PERSONALDATA_INTENT =
            "com.mediatek.backuprestore.intent.PersonalDataRestoreActivity";
    public static final String RESTORE_APPLICATION_INTENT =
            "com.mediatek.backuprestore.intent.AppRestoreActivity";
    private static final String CLASS_TAG = MyLogger.LOG_TAG + "/NotifyManager:";
    // SystemUi can't cover quick frequency notification.
    // So it requested update 2~3 item by second.
    // For this issue, we add workaround : update 2 item by second
    private static final long WAIT_TIME = 500;
    private static NotifyManager sNotifyManager;
    private static long sLastNotifyTime = 0;
    private Notification.Builder mNotification;
    private int mNotificationType;
    private Context mNotificationContext;
    private NotificationManager mNotificationManager;
    private int mMaxPercent = 100;
    private boolean mNeedRefresh = false;

    /**
     * Constructor function.
     *
     * @param context environment context
     */
    private NotifyManager(Context context) {
        mNotificationContext = context;
        mNotification = null;
        mNotificationManager = (NotificationManager) mNotificationContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * @param context
     * @return
     */
    public static NotifyManager getInstance(Context context) {
        if (sNotifyManager == null) {
            sNotifyManager = new NotifyManager(context);
        }
        return sNotifyManager;
    }

    public void setMaxPercent(int maxPercent) {
        mMaxPercent = maxPercent;
    }

    /**
     * @param type
     * @param folder
     */
    public void showNewDetectionNotification(int type, String folder) {
        mNotificationType = NOTIFY_NEW_DETECTION;
        CharSequence contentTitle = mNotificationContext.getText(R.string.detect_new_data_title);
        CharSequence contentText = mNotificationContext.getText(R.string.detect_new_data_text);
        String intentFilter = ((type == FP_NEW_DETECTION_NOTIFY_TYPE_LIST) ?
                FP_NEW_DETECTION_INTENT_LIST
                : RESTORE_PERSONALDATA_INTENT);
        if (type == FP_NEW_DETECTION_NOTIFY_TYPE_DEAFAULT
                && (folder == null || folder.trim().equals(""))) {
            MyLogger.logD(CLASS_TAG,
                    "[showNewDetectionNotification] ERROR notification ! folder is null !");
            return;
        }
        Intent intent = new Intent(Constants.ACTION_NEW_DATA_DETECTED_TRANSFER);
        intent.putExtra(Constants.FILENAME, folder);
        intent.putExtra(Constants.NOTIFY_TYPE, type);
        MyLogger.logD(CLASS_TAG, "[showNewDetectionNotification] Folder = " + folder + " Type = "
                + type);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mNotificationContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder mNotification = new Notification.Builder(mNotificationContext);
        mNotification.setAutoCancel(true).setContentTitle(contentTitle).setContentText(contentText)
                .setSmallIcon(R.drawable.ic_new_data_notify).setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent);

        mNotificationManager.notify(NOTIFY_NEW_DETECTION, mNotification.getNotification());

    }

    /**
     * @param contentText
     * @param type
     * @param currentProgress
     */
    public void showBackupNotification(String contentText, int type, int currentProgress) {
        MyLogger.logD(CLASS_TAG, "[showBackupNotification] mMaxPercent : " + mMaxPercent);
        if (mMaxPercent == 0) {
            return;
        }
        mNotificationType = NOTIFY_BACKUPING;
        CharSequence contentTitle =
                mNotificationContext.getText(R.string.notification_backup_title);
        String intentFilter = null;
        if (type == ModuleType.TYPE_APP) {
            intentFilter = BACKUP_APPLICATION_INTENT;
        } else {
            intentFilter = BACKUP_PERSONALDATA_INTENT;
        }
        try {
            MyLogger.logD(
                    CLASS_TAG,
                    "[showBackupNotification] sLastNotiyTime : " + sLastNotifyTime);
            if (System.currentTimeMillis() - sLastNotifyTime > WAIT_TIME) {
                setNotificationProgress(
                        null,
                        R.drawable.ic_backuprestore_notify_am,
                        contentTitle,
                        contentText,
                        currentProgress,
                        intentFilter);
                sLastNotifyTime = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - sLastNotifyTime <= 0) {
                MyLogger.logD(
                        CLASS_TAG,
                        "[showBackupNotification] reset sLastNotiyTime : " + sLastNotifyTime);
                sLastNotifyTime = System.currentTimeMillis();
            }
        } catch (NullPointerException e) {
            MyLogger.logE(CLASS_TAG, "turn on the USB storage!");
            e.printStackTrace();
        }
    }

    /**
     * @param type
     * @param success
     */
    public void showFinishNotification(int type, boolean success) {
        clearNotification();
        String contentTitle = mNotificationContext.getString(R.string.notification_backup_ok);
        switch (type) {
            case NOTIFY_BACKUPING:
                contentTitle = mNotificationContext
                        .getString(success ? R.string.notification_backup_ok
                                : R.string.notification_backup_failed);
                break;
            case NOTIFY_RESTORING:
                contentTitle = mNotificationContext
                        .getString(success ? R.string.notification_restore_ok
                                : R.string.notification_restore_failed);
                break;
            default:
        }
        mNotificationType = NOTIFY_BACKUPING;
        if (mNotification == null) {
            mNotification = new Notification.Builder(mNotificationContext);
            if (mNotification == null) {
                return;
            }
        }
        PackageManager pm = mNotificationContext.getPackageManager();
        mNotification
                .setAutoCancel(true)
                .setContentTitle(contentTitle)
                .setSmallIcon(R.drawable.ic_backuprestore_notify_am)
                .setContentIntent(
                        PendingIntent.getActivity(
                                mNotificationContext,
                                0,
                                pm.getLaunchIntentForPackage(mNotificationContext.getPackageName()),
                                PendingIntent.FLAG_UPDATE_CURRENT));
        mNotificationManager.notify(mNotificationType, mNotification.getNotification());
    }

    /**
     * @param contentText     notification content
     * @param mFileName       file name
     * @param type            type
     * @param currentProgress current progress.
     */
    public void showRestoreNotification(
            String contentText,
            String mFileName,
            int type,
            int currentProgress) {
        if (mMaxPercent == 0) {
            return;
        }
        mNotificationType = NOTIFY_RESTORING;
        CharSequence contentTitle = mNotificationContext
                .getText(R.string.notification_restore_title);
        String intentFilter = null;
        if (type == ModuleType.TYPE_APP) {
            intentFilter = RESTORE_APPLICATION_INTENT;
        } else {
            intentFilter = RESTORE_PERSONALDATA_INTENT;
        }
        try {
            setNotificationProgress(
                    mFileName,
                    R.drawable.ic_backuprestore_notify_am,
                    contentTitle,
                    contentText,
                    currentProgress,
                    intentFilter);
        } catch (NullPointerException e) {
            MyLogger.logE(CLASS_TAG, "turn on the USB storage!");
            e.printStackTrace();
        }
    }

    private void setNotificationProgress(
            String mFileName,
            int iconDrawableId,
            CharSequence contentTitle,
            String contentText,
            int currentProgress,
            String intentFilter)
            throws NullPointerException {
        if (mNeedRefresh) {
            clearNotification();
        }
        if (mNotification == null) {
            mNotification = new Notification.Builder(mNotificationContext);
            if (mNotification == null) {
                return;
            }
        }
        mNotification
                .setAutoCancel(true)
                .setOngoing(true)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(iconDrawableId)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(getPendingIntenActivity(mFileName, intentFilter));

        String percent = "" + (currentProgress * 100) / mMaxPercent + "%";
        mNotification
                .setProgress(mMaxPercent, currentProgress, false)
                .setContentInfo(percent);

        if (mNotificationContext instanceof Service) {
            ((Service) mNotificationContext).startForeground(
                    mNotificationType,
                    mNotification.getNotification());
        } else {
            mNotificationManager.notify(mNotificationType, mNotification.getNotification());
        }
    }

    private PendingIntent getPendingIntenActivity(String mFileName, String intentFilter) {

        Intent notificationIntent = new Intent(intentFilter);
        if (mFileName != null) {
            notificationIntent.putExtra(Constants.FILENAME, mFileName);
        }
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(
                mNotificationContext,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return contentIntent;
    }

    /**
     * Clear current notification.
     */
    public void clearNotification() {
        MyLogger.logD(CLASS_TAG, "clearNotification");
        if (mNotification != null) {
            mNotification.setOngoing(false);
            if (mNotificationContext instanceof Service) {
                ((Service) mNotificationContext).stopForeground(true);
                MyLogger.logD(CLASS_TAG, "mNotificationContext instanceof Service");
            }
            mNotificationManager.cancel(mNotificationType);
            MyLogger.logD(CLASS_TAG, "clearNotification+mNotificationType = "
                    + mNotificationType);
            mNotification = null;
        }
        mNeedRefresh = false;
    }

    /**
     * Clear current notification and reset it for configuration change.
     */
    public void setRefreshFlag() {
        mNeedRefresh = true;
    }
}
