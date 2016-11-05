package com.ape.transfer.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;

import com.ape.transfer.App;
import com.ape.transfer.BuildConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by android on 16-6-29.
 */
public class Util {
    private static final String PACKAGE_URI_PREFIX = "package:";
    private static Signature[] sSystemSignature;

    public static String formatDateString(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-DD HH:mm");
        return formatter.format(new Date(time));
    }

    public static String getNameFromFilename(String filename) {
        if (filename != null) {
            int dotPosition = filename.lastIndexOf('.');
            if (dotPosition != -1) {
                return filename.substring(0, dotPosition);
            }
        }
        return "";
    }

    public static String getNameFromFilepath(String filepath) {
        if (!TextUtils.isEmpty(filepath)) {
            int pos = filepath.lastIndexOf('/');
            if (pos != -1) {
                return filepath.substring(pos + 1);
            }
        }
        return "";
    }

    /*
     * 采用了新的办法获取APK图标，之前的失败是因为android中存在的一个BUG,通过
     * appInfo.publicSourceDir = apkPath;来修正这个问题，详情参见:
     * http://code.google.com/p/android/issues/detail?id=9151
     */
    public static Drawable getApkIcon(Context context, String apkPath) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkPath,
                    PackageManager.GET_ACTIVITIES);
            if (info != null) {
                ApplicationInfo appInfo = info.applicationInfo;
                appInfo.sourceDir = apkPath;
                appInfo.publicSourceDir = apkPath;
                return appInfo.loadIcon(pm);
            }
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * 获取单个文件的MD5值！
     *
     * @param file
     * @return
     */

    public static String getFileMD5(File file) {
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[4096];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    /**
     * 字符串MD5加密
     *
     * @param string
     * @return
     */
    public static String getStringMD5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    public static void startSettingsPermission(Context context) {
        final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse(PACKAGE_URI_PREFIX + context.getPackageName()));
        if (!(context instanceof Activity))
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean isSystemPackage(PackageManager pm, PackageInfo pkg) {
        if (sSystemSignature == null) {
            sSystemSignature = new Signature[]{getSystemSignature(pm)};
        }
        return sSystemSignature[0] != null && sSystemSignature[0].equals(getFirstSignature(pkg));
    }

    private static Signature getFirstSignature(PackageInfo pkg) {
        if (pkg != null && pkg.signatures != null && pkg.signatures.length > 0) {
            return pkg.signatures[0];
        }
        return null;
    }

    private static Signature getSystemSignature(PackageManager pm) {
        try {
            final PackageInfo sys = pm.getPackageInfo("android", PackageManager.GET_SIGNATURES);
            return getFirstSignature(sys);
        } catch (PackageManager.NameNotFoundException e) {
        }
        return null;
    }

    public boolean isPlatformSigned() {
        PackageManager packageManager = App.getContext().getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(BuildConfig.APPLICATION_ID,
                    PackageManager.GET_SIGNATURES);
            return isSystemPackage(packageManager, packageInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}
