package com.ape.transfer.model;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.ape.transfer.util.FileCategoryHelper;
import com.ape.transfer.util.Log;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by android on 16-6-28.
 */
public class FileItem implements Serializable{
    private static final long serialVersionUID = -636934079297822919L;

    public static final int COLUMN_ID = 0;
    public static final int COLUMN_PATH = 1;
    public static final int COLUMN_SIZE = 2;
    public static final int COLUMN_DATE = 3;
    private static final String TAG = "FileItem";
    public boolean selected;
    public long dateModified;
    public long size;
    public long id;
    public String path;
    public Drawable appLogo;
    public CharSequence appLabel;

    public FileItem() {

    }

    public FileItem(Cursor c) {
        id = c.getLong(COLUMN_ID);
        path = c.getString(COLUMN_PATH);
        size = c.getLong(COLUMN_SIZE);
        dateModified = c.getLong(COLUMN_DATE);
        selected = false;
    }

    public static ArrayList<FileItem> getMusicItems(Context context, FileCategoryHelper.FileCategory fileCategory) {
        ArrayList<FileItem> musicItems = new ArrayList<>();

        if (fileCategory == FileCategoryHelper.FileCategory.Apk) {
            musicItems.addAll(getThirdPartApp(context));
        }

        final ContentResolver resolver = context.getContentResolver();

        Uri uri = FileCategoryHelper.getContentUriByCategory(fileCategory);
        String[] projection = FileCategoryHelper.getProjection();
        String selection = FileCategoryHelper.buildSelectionByCategory(fileCategory);
        String[] selectionArgs = null;
        String sortOrder = FileCategoryHelper.buildSortOrder(fileCategory);

        Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, sortOrder);

        if (cursor == null) {
            return musicItems;
        }

        if (cursor.getCount() == 0) {
            cursor.close();
            return musicItems;
        }

        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            FileItem musicItem = new FileItem(cursor);
            musicItems.add(musicItem);
        }
        cursor.close();

        return musicItems;
    }

    private static List<FileItem> getThirdPartApp(Context context) {
        PackageManager pkManager = context.getPackageManager();
        List<ApplicationInfo> applicationInfos = pkManager
                .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        Collections.sort(applicationInfos, new ApplicationInfo.DisplayNameComparator(pkManager));
        List<FileItem> items = new ArrayList<>();
        items.clear();
        for (ApplicationInfo app : applicationInfos) { // get the third APP
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                FileItem info = getAppInfo(pkManager, app);
                if (info == null)
                    continue;
                else if (!items.contains(info))
                    items.add(info);
            }
        }
        Log.i(TAG, "getThirdPartApp items = " + items);
        return items;
    }

    private static FileItem getAppInfo(PackageManager pkManager, ApplicationInfo app) {
        String filepath = null;
        try {
            filepath = pkManager
                    .getApplicationInfo(app.packageName, 0).sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (filepath == null) {
            return null;
        }
        FileItem appInfo = new FileItem();
        File file = new File(filepath);
        long fileSize = file.length();
        if (fileSize <= 0)
            return null;
        appInfo.path = filepath;
        appInfo.size = file.length();
        appInfo.dateModified = file.lastModified();
        appInfo.appLogo = app.loadIcon(pkManager);
        appInfo.appLabel = app.loadLabel(pkManager);
        return appInfo;
    }

    @Override
    public String toString() {
        return "FileItem{" +
                "selected=" + selected +
                ", dateModified=" + dateModified +
                ", size=" + size +
                ", id=" + id +
                ", path='" + path + '\'' +
                '}';
    }
}
