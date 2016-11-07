package com.ape.backuprestore.modules;

import android.content.Context;

import com.ape.backuprestore.utils.BackupZip;
import com.ape.backuprestore.utils.Constants;
import com.ape.backuprestore.utils.Logger;
import com.ape.backuprestore.utils.ModuleType;
import com.ape.backuprestore.utils.Utils;
import com.ape.packagemanager.PackageManagerUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by android on 16-7-16.
 */
public class AppRestoreComposer extends Composer {
    private static final String TAG = "AppRestoreComposer";
    private final Object mLock = new Object();
    private int mIndex;
    private List<String> mFileNameList;
    private String mZipFileName;
    private String mDestPath;
    private boolean isPlatformSigned;

    /**
     * @param context context
     */
    public AppRestoreComposer(Context context) {
        super(context);
    }

    /**
     * @return int
     */
    @Override
    public int getModuleType() {
        return ModuleType.TYPE_APP;
    }

    /**
     * @return int
     */
    @Override
    public int getCount() {
        int count = 0;
        if (mFileNameList != null) {
            count = mFileNameList.size();
        }
        Logger.d(TAG, "getCount():" + count);
        return count;
    }

    /**
     * @return boolean
     */
    @Override
    public boolean init() {
        //get the app dest path
        String parentPath = (new File(mParentFolderPath)).getParent();
        if (parentPath == null) {
            Logger.e(TAG, "init() parentPath == null");
            return false;
        } else {
            mDestPath = parentPath + File.separator + RESTORE
                    + File.separator + Constants.ModulePath.FOLDER_APP;
        }

        boolean result = false;
        mFileNameList = new ArrayList<>();

        String sourcePath = mParentFolderPath + File.separator + Constants.ModulePath.FOLDER_APP;
        File folder = new File(sourcePath);
        if (folder.exists() && folder.isDirectory()) {
            try {
                mZipFileName = sourcePath + File.separator + Constants.ModulePath.NAME_APPZIP;
                File file = new File(mZipFileName);
                if (file.exists()) {
                    mFileNameList = BackupZip.getFileList(mZipFileName, false, true, ".*");
                    result = mFileNameList.size() > 0;
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (super.mReporter != null) {
                    super.mReporter.onErr(e);
                }
            }
        }
        isPlatformSigned = Utils.isPlatformSigned(mContext, mContext.getPackageName());
        Logger.d(TAG, "init():" + result + ", count:" + getCount() + ", isPlatformSigned = " + isPlatformSigned);
        return result;
    }

    /**
     * @return boolean
     */
    @Override
    public boolean isAfterLast() {
        if (mDestPath == null) {
            Logger.e(TAG, "isAfterLast... mDestPath == null");
            return true;
        }
        boolean result = true;
        if (mFileNameList != null) {
            result = (mIndex >= mFileNameList.size());
        }

        Logger.d(TAG, "isAfterLast():" + result);
        return result;
    }

    /**
     * @return boolean
     */
    @Override
    public boolean implementComposeOneEntity() {
        if (mDestPath == null) {
            Logger.e(TAG, "implementComposeOneEntity... mDestPath == null");
            return false;
        }

        boolean result = false;

        if (mFileNameList != null && mIndex < mFileNameList.size()) {
            String apkName = mFileNameList.get(mIndex++);
            String destFileName = mDestPath + File.separator + apkName;

            File destFile = new File(destFileName);
            if (destFile.exists()) {
                installApk(destFile);
                return true;
            }

            try {
                BackupZip.unZipFile(mZipFileName, apkName, destFileName);
                File apkFile = new File(destFileName);
                if (apkFile.exists()) {
                    installApk(apkFile);
                    result = true;
                } else {
                    Logger.d(TAG, "install failed");
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (super.mReporter != null) {
                    super.mReporter.onErr(e);
                }
                Logger.d(TAG, "unZipFile failed");
            }
        }

        return result;
    }

    private boolean installApk(File apkFile) {
        boolean result = false;
        if (isPlatformSigned) {
            PackageManagerUtil packageManagerUtil = new PackageManagerUtil(mContext, mLock);
            packageManagerUtil.installPackage(apkFile);

            synchronized (mLock) {
                while (!packageManagerUtil.isFinished()) {
                    try {
                        mLock.wait();
                    } catch (InterruptedException e) {
                        Logger.d(TAG, "InterruptedException");
                    }
                }

                if (packageManagerUtil.isSuccess()) {
                    apkFile.delete();
                    result = true;
                    Logger.d(TAG, "install success");
                } else {
                    Logger.d(TAG, "install fail");
                }
            }
        }
        return result;
    }

    /**
     * onStart.
     */
    @Override
    public void onStart() {
        super.onStart();
        if (mDestPath != null) {
            File tmpFolder = new File(mDestPath);

            if (!tmpFolder.exists()) {
                tmpFolder.mkdirs();
            }
        }
    }

    /**
     * onEnd.
     */
    @Override
    public void onEnd() {
        super.onEnd();
        if (mFileNameList != null) {
            mFileNameList.clear();
        }
        Logger.d(TAG, "onEnd()");
    }

}
