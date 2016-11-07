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

package com.ape.backuprestore;

import android.content.Context;
import android.widget.Toast;

import com.ape.backuprestore.modules.AppRestoreComposer;
import com.ape.backuprestore.modules.CalendarRestoreComposer;
import com.ape.backuprestore.modules.Composer;
import com.ape.backuprestore.modules.ContactRestoreComposer;
import com.ape.backuprestore.modules.MessageRestoreComposer;
import com.ape.backuprestore.modules.MmsRestoreComposer;
import com.ape.backuprestore.modules.MusicRestoreComposer;
import com.ape.backuprestore.modules.NoteBookRestoreComposer;
import com.ape.backuprestore.modules.PictureRestoreComposer;
import com.ape.backuprestore.modules.SmsRestoreComposer;
import com.ape.backuprestore.utils.Constants;
import com.ape.backuprestore.utils.Logger;
import com.ape.backuprestore.utils.ModuleType;
import com.ape.backuprestore.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author way
 */
public class RestoreEngine {
    private static final String TAG = "RestoreEngine";
    private static RestoreEngine sSelfInstance;
    private final Object mLock = new Object();
    ArrayList<Integer> mModuleList;
    HashMap<Integer, ArrayList<String>> mParasMap = new HashMap<>();
    private Context mContext;
    private String mRestoreFolder;
    private OnRestoreDoneListner mRestoreDoneListner;
    private boolean mIsRunning = false;
    private long mThreadIdentifier = -1;
    private boolean mIsPause = false;
    private ProgressReporter mReporter;
    private List<Composer> mComposerList;

    private RestoreEngine(Context context, ProgressReporter reporter) {
        mContext = context;
        mReporter = reporter;
        mComposerList = new ArrayList<Composer>();
    }

    /**
     * @param context  context
     * @param reporter reporter
     * @return RestoreEngine
     */
    public static synchronized RestoreEngine getInstance(final Context context,
                                                         final ProgressReporter reporter) {
        if (sSelfInstance == null) {
            sSelfInstance = new RestoreEngine(context, reporter);
        } else {
            sSelfInstance.updateInfo(context, reporter);
        }
        return sSelfInstance;
    }

    private final void updateInfo(final Context context, final ProgressReporter reporter) {
        mContext = context;
        mReporter = reporter;
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    /**
     * @return
     */
    public void pause() {
        mIsPause = true;
    }

    public boolean isPaused() {
        return mIsPause;
    }

    /**
     * continueRestore.
     */
    public void continueRestore() {/*
        if (mIsPause) {
            synchronized (mLock) {
                mIsPause = false;
                mLock.notify();
            }
        }
    */
    }

    /**
     * cancel.
     */
    public void cancel() {

        Logger.e(TAG, "cancel");
        if (mComposerList != null && mComposerList.size() > 0) {
            for (Composer composer : mComposerList) {
                composer.setCancel(true);
            }
        }
        Utils.isRestoring = mIsRunning = false;
    }

    public void setOnRestoreEndListner(OnRestoreDoneListner restoreEndListner) {
        mRestoreDoneListner = restoreEndListner;
    }

    /**
     * @param moduleList list
     */
    public void setRestoreModelList(ArrayList<Integer> moduleList) {
        reset();
        mModuleList = moduleList;
    }

    /**
     * @param itemType type
     * @param paraList list
     */
    public void setRestoreItemParam(int itemType, ArrayList<String> paraList) {
        mParasMap.put(itemType, paraList);
    }

    /**
     * @param path path
     */
    public void startRestore(final String path) {
        if (path != null && mModuleList.size() > 0) {
            mRestoreFolder = path;
            setupComposer(mModuleList);
            Utils.isRestoring = mIsRunning = true;
            mThreadIdentifier = System.currentTimeMillis();
            new RestoreThread(mThreadIdentifier).start();
        }
    }

    /**
     * @param path path
     * @param list list
     */
    public void startRestore(String path, List<Integer> list) {
        reset();
        if (path != null && list.size() > 0) {
            mRestoreFolder = path;
            setupComposer(list);
            Utils.isRestoring = mIsRunning = true;
            mThreadIdentifier = System.currentTimeMillis();
            new RestoreThread(mThreadIdentifier).start();
        }
    }

    private void addComposer(Composer composer) {
        if (composer != null) {
            int type = composer.getModuleType();
            ArrayList<String> params = mParasMap.get(type);
            if (params != null) {
                composer.setParams(params);
            }
            composer.setReporter(mReporter);
            composer.setParentFolderPath(mRestoreFolder);
            mComposerList.add(composer);
        }
    }

    private void reset() {
        if (mComposerList != null) {
            mComposerList.clear();
        }
        mIsPause = false;
    }

    private boolean setupComposer(List<Integer> list) {
        boolean bSuccess = true;
        for (int type : list) {
            switch (type) {
                case ModuleType.TYPE_CONTACT:
                    addComposer(new ContactRestoreComposer(mContext));
                    break;

                case ModuleType.TYPE_MESSAGE:
                    addComposer(new MessageRestoreComposer(mContext));
                    break;

                case ModuleType.TYPE_SMS:
                    addComposer(new SmsRestoreComposer(mContext));
                    break;

                case ModuleType.TYPE_MMS:
                    addComposer(new MmsRestoreComposer(mContext));
                    break;

                case ModuleType.TYPE_PICTURE:
                    addComposer(new PictureRestoreComposer(mContext));
                    break;

                case ModuleType.TYPE_CALENDAR:
                    addComposer(new CalendarRestoreComposer(mContext));
                    break;

                case ModuleType.TYPE_APP:
                    addComposer(new AppRestoreComposer(mContext));
                    break;

                case ModuleType.TYPE_MUSIC:
                    addComposer(new MusicRestoreComposer(mContext));
                    break;

                case ModuleType.TYPE_NOTEBOOK:
                    addComposer(new NoteBookRestoreComposer(mContext));
                    break;

//            case ModuleType.TYPE_BOOKMARK:
//                addComposer(new BookmarkRestoreComposer(mContext));
//                break;

                default:
                    bSuccess = false;
                    break;
            }
        }

        return bSuccess;
    }

    public interface OnRestoreDoneListner {
        public void onFinishRestore(boolean bSuccess);
    }

    /**
     * @author mtk81330
     */
    public class RestoreThread extends Thread {
        private final long mId;

        public RestoreThread(long id) {
            super();
            mId = id;
        }

        @Override
        public void run() {
            try {
                Logger.d(TAG, "RestoreThread begin...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    for (Composer composer : mComposerList) {
                        Logger.d(TAG,
                                "RestoreThread composer: " + composer.getModuleType() + " start..");
                        Logger.d(TAG, "begin restore:" + System.currentTimeMillis());
                        if (!composer.isCancel() && mId == mThreadIdentifier) {
                            if (!composer.init()) {
                                composer.onEnd();
                                continue;
                            }
                            Logger.d(TAG, "RestoreThread composer: " + composer.getModuleType()
                                    + " init finish");
                            composer.onStart();
                            while (!composer.isAfterLast() &&
                                    !composer.isCancel() &&
                                    mId == mThreadIdentifier) {
                                if (mIsPause) {
                                    synchronized (mLock) {
                                        try {
                                            Logger.d(TAG, "RestoreThread wait...");
                                            while (mIsPause) {
                                                mLock.wait();
                                            }
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                composer.composeOneEntity();
                                Logger.d(TAG, "RestoreThread composer: " + composer.getModuleType()
                                        + " compose one entiry");
                            }
                        }

                        try {
                            sleep(Constants.TIME_SLEEP_WHEN_COMPOSE_ONE);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        composer.onEnd();
                        Logger.d(TAG, "End restore:" + System.currentTimeMillis());
                        Logger.d(TAG,
                                "RestoreThread composer: " + composer.getModuleType()
                                        + " compose finish");
                    }
                } catch (java.util.ConcurrentModificationException e) {
                    Logger.e(TAG, "ConcurrentModificationException");
                    e.fillInStackTrace();
                }
                Logger.d(TAG, "RestoreThread run finish");
                Utils.isRestoring = mIsRunning = false;

                if (mRestoreDoneListner != null && mId == mThreadIdentifier) {
                    mRestoreDoneListner.onFinishRestore(true);
                }
            } catch (SecurityException e) {
                e.printStackTrace();
                Toast.makeText(
                        mContext,
                        R.string.permission_not_satisfied_exit,
                        Toast.LENGTH_SHORT).show();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
    }
}
