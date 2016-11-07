package com.ape.backuprestore.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Build;
import android.util.Log;

import com.ape.backuprestore.R;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Utils {
    private static final String[] REQUESTED_RUNTIME_PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_SMS,
            //Manifest.permission.WRITE_SMS,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final String TAG = "Utils";
    public static boolean isRestoring;
    public static boolean isBackingUp;
    private static String[] sRequestedPermissions = null;
    private static boolean sRequestedPermissionsCached = false;
    private static Signature[] sSystemSignature;

    public static int getWorkingInfo() {
        int stringId = -1;
        if (isRestoring || isBackingUp) {
            return R.string.state_running;
        }
        return stringId;

    }

    public static void exitLockTaskModeIfNeeded(Activity activity) {
        ActivityManager am
                = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        if (am.isInLockTaskMode()) {
            Log.d(TAG, "exitLockTaskModeIfNeeded: in lock mode");
            activity.stopLockTask();
            Log.d(TAG, "exitLockTaskModeIfNeeded: lock mode exited");
        }
    }

    public static String getPhoneSearialNumber() {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.boot.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }

    public static void writeToFile(String content, String filePath) {
        try {
            FileOutputStream outStream = new FileOutputStream(filePath);
            byte[] buf = content == null ? new byte[0] : content.getBytes();
            outStream.write(buf, 0, buf.length);
            outStream.flush();
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readFromFile(String fileName) {
        try {
            InputStream is = new FileInputStream(fileName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len = -1;
            byte[] buffer = new byte[512];
            while ((len = is.read(buffer, 0, 512)) != -1) {
                baos.write(buffer, 0, len);
            }
            is.close();
            return baos.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @param timestamp the byte[] which Feature Phone saved in file PDU.o
     * @return Feature Phone sendbox's SMS send time.If failed, return
     * System.currentTimeMillis.
     */
    public static long getFPsendSMSTime(byte[] timestamp) {
        if (timestamp.length != 4) {
            return System.currentTimeMillis();
        }
        long time = unsigned4BytesToInt(timestamp);
        return getFPsendSMSTime(time);
    }

    private static long getFPsendSMSTime(long time) {
        long sec = time % (3600 * 24);
        long hour = sec / 3600;
        sec %= 3600;
        long min = sec / 60;
        long secd = sec % 60;
        int d = 0;
        long day = time / (3600 * 24);
        int y = 1970;
        for (; day > 0; y++) {
            d = 365 + isLeakYeay(y);
            if (day >= d) {
                day -= d;
            } else {
                break;
            }
        }
        int m = 1;
        for (; m < 13; m++) {
            d = getLaseDayPerMonth(m, y);
            if (day >= d) {
                day -= d;
            } else {
                break;
            }
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            String sendtime = y + "-" + m + "-" + (day + 1) + " " + hour + ":" + min + ":" + secd;
            Date date = format.parse(sendtime);
            return date.getTime();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return System.currentTimeMillis();
        }

    }

    private static int getLaseDayPerMonth(int m, int y) {
        int days = 0;
        if (m == 2) {
            if (isLeakYeay(y) == 1) {
                days = 29;
            } else {
                days = 28;
            }
        } else {
            switch (m) {
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 10:
                case 12:
                    days = 31;
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    days = 30;
                    break;
            }
        }
        return days;
    }

    private static int isLeakYeay(int y) {
        if ((y % 4 == 0 && y % 100 != 0) || y % 400 == 0) {
            return 1;
        }
        return 0;
    }

    private static long unsigned4BytesToInt(byte[] buf) {
        int firstByte = 0;
        int secondByte = 0;
        int thirdByte = 0;
        int fourthByte = 0;
        int index = 0;
        firstByte = (0x000000FF & ((int) buf[index]));
        secondByte = (0x000000FF & ((int) buf[index + 1]));
        thirdByte = (0x000000FF & ((int) buf[index + 2]));
        fourthByte = (0x000000FF & ((int) buf[index + 3]));
        index = index + 4;
        return ((long) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL;
    }

    public static void killMyProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /*
     * Get all permission this package requested in manifest.
     * @param context android context
     * @return null - no permission requested
     *         array of requested permissions
     */
    private static synchronized String[] getPermissions(Context context) {
        if (!sRequestedPermissionsCached) {
            PackageManager pm = context.getPackageManager();
            try {
                PackageInfo pi = pm.getPackageInfo(
                        "com.mediatek.datatransfer",
                        PackageManager.GET_PERMISSIONS);
                sRequestedPermissions = pi.requestedPermissions;
                sRequestedPermissionsCached = true;
            } catch (NameNotFoundException e) {
                throw new Error(e);
            }
        }
        return sRequestedPermissions;
    }

    /**
     * Get unsatisfied permissions of this package.
     *
     * @param context android context
     * @return null - all permissions are granted or no permission requested
     * array of all unsatisfied permissions
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static String[] getUnsatisfiedPermissions(Context context) {
        String[] requestedPermissions = REQUESTED_RUNTIME_PERMISSIONS;
        if (requestedPermissions == null) {
            return null;
        }
        List<String> unsatisfiedPermissions = new ArrayList<String>();
        for (String p : requestedPermissions) {
            Log.d(TAG, "Checking: " + p);
            int result = context.checkSelfPermission(p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                unsatisfiedPermissions.add(p);
                Log.d(TAG, "Denied");
            } else {
                Log.d(TAG, "Granted");
            }
        }
        if (unsatisfiedPermissions.size() == 0) {
            return null;
        } else {
            String[] result = new String[unsatisfiedPermissions.size()];
            for (int i = 0; i < unsatisfiedPermissions.size(); ++i) {
                result[i] = unsatisfiedPermissions.get(i);
            }
            return result;
        }
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

    public static boolean isPlatformSigned(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);
            return isSystemPackage(packageManager, packageInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}
