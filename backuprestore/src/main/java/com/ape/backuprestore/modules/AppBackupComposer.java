package com.ape.backuprestore.modules;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.ape.backuprestore.utils.Constants;
import com.ape.backuprestore.utils.ModuleType;
import com.ape.backuprestore.utils.MyLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by android on 16-7-16.
 */
public class AppBackupComposer extends Composer {
    private static final String CLASS_TAG = MyLogger.LOG_TAG + "/AppBackupComposer";

    private List<ApplicationInfo> mUserAppInfoList = null;
    private int mAppIndex = 0;

    public AppBackupComposer(Context context) {
        super(context);
    }

    public static List<ApplicationInfo> getUserAppInfoList(final Context context) {
        List<ApplicationInfo> userAppInfoList = null;
        if (context != null) {
            List<ApplicationInfo> allAppInfoList = context.getPackageManager()
                    .getInstalledApplications(0);
            userAppInfoList = new ArrayList<>();
            for (ApplicationInfo appInfo : allAppInfoList) {
                if (!((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM)
                        && !appInfo.packageName.equalsIgnoreCase(context.getPackageName())) {
                    userAppInfoList.add(appInfo);
                }
            }
        }
        return userAppInfoList;
    }

    @Override
    public int getModuleType() {
        return ModuleType.TYPE_APP;
    }

    @Override
    public int getCount() {
        int count = 0;
        if (mUserAppInfoList != null && mUserAppInfoList.size() > 0) {
            count = mUserAppInfoList.size();
        }

        MyLogger.logD(CLASS_TAG, "getCount():" + count);
        return count;
    }

    @Override
    public boolean isAfterLast() {
        boolean result = true;
        if (mUserAppInfoList != null && mAppIndex < mUserAppInfoList.size()) {
            result = false;
        }

        MyLogger.logD(CLASS_TAG, "isAfterLast():" + result);
        return result;
    }

    @Override
    public boolean init() {
        boolean result = false;
        final PackageManager pm = mContext.getPackageManager();
        if (mParams != null) {
            List<ApplicationInfo> tmpList = getUserAppInfoList(mContext);
            HashMap tmpMap = new HashMap<>();
            if (tmpList != null) {
                for (ApplicationInfo appInfo : tmpList) {
                    tmpMap.put(appInfo.packageName, appInfo);
                }
            }

            mUserAppInfoList = new ArrayList<>();
            for (int i = 0; i < mParams.size(); ++i) {
                ApplicationInfo appInfo = (ApplicationInfo) tmpMap.get(mParams.get(i));
                if (appInfo != null) {
                    mUserAppInfoList.add(appInfo);
                }
            }
        } else {
            mUserAppInfoList = getUserAppInfoList(mContext);
            Collections.sort(mUserAppInfoList, new Comparator<ApplicationInfo>() {
                public int compare(ApplicationInfo object1, ApplicationInfo object2) {
                    String left = object1.loadLabel(pm).toString();
                    String right = object1.loadLabel(pm).toString();
                    return left.compareTo(right);
                }
            });
        }
        result = true;
        mAppIndex = 0;

        MyLogger.logD(CLASS_TAG, "init():" + result);
        return result;
    }

    @Override
    protected boolean implementComposeOneEntity() {
        boolean result = false;
        if (mUserAppInfoList != null && mAppIndex < mUserAppInfoList.size()) {
            ApplicationInfo appInfo = mUserAppInfoList.get(mAppIndex);
            String appSrc = appInfo.publicSourceDir;
            String appDest = mParentFolderPath + File.separator + Constants.ModulePath.FOLDER_APP
                    + File.separator + appInfo.packageName + Constants.ModulePath.FILE_EXT_APP;
            CharSequence tmpLable = "";
            if (appInfo.uid == -1) {
                tmpLable = getApkFileLabel(mContext, appInfo.sourceDir, appInfo);
            } else {
                tmpLable = appInfo.loadLabel(mContext.getPackageManager());
            }
            String label = (tmpLable == null) ? appInfo.packageName : tmpLable.toString();
            MyLogger.logD(CLASS_TAG, mAppIndex + ":" + appSrc + ",pacageName:" + appInfo.packageName
                    + ",sourceDir:" + appInfo.sourceDir + ",dataDir:" + appInfo.dataDir
                    + ",lable:" + label);
            try {
                copyFile(appSrc, appDest);
                MyLogger.logD(CLASS_TAG, "addFile " + appSrc + "success");
                result = true;
            } catch (IOException e) {
                if (super.mReporter != null) {
                    super.mReporter.onErr(e);
                }
                MyLogger.logD(CLASS_TAG, "addFile:" + appSrc + "fail");
                e.printStackTrace();
            }

            ++mAppIndex;
        }

        return result;
    }

    private CharSequence getApkFileLabel(final Context context, final String apkPath,
                                         final ApplicationInfo appInfo) {
        if (context == null || appInfo == null || apkPath == null
                || !(new File(apkPath).exists())) {
            return null;
        }
        PackageManager pkManager = context.getPackageManager();

        CharSequence label = null;
        if (0 != appInfo.labelRes) {
            label = appInfo.loadLabel(pkManager);
        }

        return label;
    }

    @Override
    public final void onEnd() {
        super.onEnd();
        if (mUserAppInfoList != null) {
            mUserAppInfoList.clear();
        }
        MyLogger.logD(CLASS_TAG, "onEnd()");
    }

    private void copyFile(String srcFile, String destFile) throws IOException {
        InputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            File f1 = new File(srcFile);
            if (f1.exists() && f1.isFile()) {
                inStream = new FileInputStream(srcFile);
                outStream = new FileOutputStream(destFile);
                byte[] buf = new byte[1024];
                int byteRead = 0;
                while ((byteRead = inStream.read(buf)) != -1) {
                    outStream.write(buf, 0, byteRead);
                }
                outStream.flush();
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outStream != null) {
                outStream.close();
            }
            if (inStream != null) {
                inStream.close();
            }
            outStream = null;
            inStream = null;
        }
    }

    /**
     * Describe <code>onStart</code> method here.
     */
    public final void onStart() {
        super.onStart();
        if (getCount() > 0) {
            File path = new File(mParentFolderPath + File.separator + Constants.ModulePath.FOLDER_APP);
            if (!path.exists()) {
                path.mkdirs();
            }
        }
    }
}
