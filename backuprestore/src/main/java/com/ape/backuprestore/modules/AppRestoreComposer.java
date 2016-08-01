package com.ape.backuprestore.modules;

import android.content.Context;
import android.net.Uri;

import com.ape.backuprestore.utils.Constants;
import com.ape.backuprestore.utils.ModuleType;
import com.ape.backuprestore.utils.MyLogger;
import com.ape.packagemanager.PackageManagerUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by android on 16-7-16.
 */
public class AppRestoreComposer extends Composer {
    private static final String CLASS_TAG = MyLogger.LOG_TAG + "/AppRestoreComposer";
    private int mIndex;
    private List<String> mFileNameList;
    private Object mLock = new Object();

    /**
     * @param context
     */
    public AppRestoreComposer(Context context) {
        super(context);
    }

    /**
     * @return int
     */
    public int getModuleType() {
        return ModuleType.TYPE_APP;
    }

    /**
     * @return int
     */
    public int getCount() {
        int count = 0;
        if (mFileNameList != null) {
            count = mFileNameList.size();
        }
        MyLogger.logD(CLASS_TAG, "getCount():" + count);
        return count;
    }

    /**
     * @return boolean
     */
    public boolean init() {
        boolean result = false;
        if (mParams != null) {
            mFileNameList = mParams;
            result = true;
        } else {
            String path = mParentFolderPath + File.separator + Constants.ModulePath.FOLDER_APP;
            mFileNameList = new ArrayList<>();
            File folder = new File(path);
            if (folder.exists() && folder.isDirectory()) {
                File[] fileLists = folder.listFiles();
                for (File file : fileLists) {
                    if (file.isFile()) {
                        mFileNameList.add(file.getAbsolutePath());
                    }
                }
            }
        }

        MyLogger.logD(CLASS_TAG, "init():" + result + ", count:" + getCount());
        return result;
    }

    /**
     * @return boolean
     */
    public boolean isAfterLast() {
        boolean result = true;
        if (mFileNameList != null) {
            result = (mIndex >= mFileNameList.size());
        }

        MyLogger.logD(CLASS_TAG, "isAfterLast():" + result);
        return result;
    }

    /**
     * @return boolean
     */
    public boolean implementComposeOneEntity() {
        boolean result = false;
        if (mFileNameList != null && mIndex < mFileNameList.size()) {
            try {
                String apkFileName = mFileNameList.get(mIndex++);
                File apkFile = new File(apkFileName);
                if (apkFile.exists()) {
                    PackageManagerUtil packageManagerUtil = new PackageManagerUtil(mContext, mLock);

                    packageManagerUtil.installPackage(apkFile);

                    synchronized (mLock) {
                        while (!packageManagerUtil.isFinished()) {
                            try {
                                mLock.wait();
                            } catch (InterruptedException e) {
                                MyLogger.logD(CLASS_TAG, "InterruptedException");
                            }
                        }

                        if (packageManagerUtil.isSuccess()) {
                            result = true;
                            MyLogger.logD(CLASS_TAG, "install success");
                        } else {
                            MyLogger.logD(CLASS_TAG, "install fail");
                        }
                    }
                } else {
                    MyLogger.logD(CLASS_TAG, "install failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * onStart.
     */
    public void onStart() {
        super.onStart();
        //delteTempFolder();
    }

    /**
     * onEnd.
     */
    public void onEnd() {
        super.onEnd();
        if (mFileNameList != null) {
            mFileNameList.clear();
        }
        //delteTempFolder();
        MyLogger.logD(CLASS_TAG, "onEnd()");
    }
}
