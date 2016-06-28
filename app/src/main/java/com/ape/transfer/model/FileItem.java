package com.ape.transfer.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.ape.transfer.util.FileCategoryHelper;

import java.util.ArrayList;

/**
 * Created by android on 16-6-28.
 */
public class FileItem {
    private static final int COLUMN_ID = 0;
    private static final int COLUMN_DATA = 1;
    private static final int COLUMN_SIZE = 2;
    private static final int COLUMN_DATE_MODIFIED = 3;

    public boolean selected;
    public long dateModified;
    public long size;
    public long id;
    public String path;

    public FileItem(Cursor c) {
        id = c.getLong(COLUMN_ID);
        path = c.getString(COLUMN_DATA);
        size = c.getLong(COLUMN_SIZE);
        dateModified = c.getLong(COLUMN_DATE_MODIFIED);
        selected = false;
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

    public static ArrayList<FileItem> getMusicItems(Context context, FileCategoryHelper.FileCategory fileCategory) {

        final ContentResolver resolver = context.getContentResolver();

        Uri uri = FileCategoryHelper.getContentUriByCategory(fileCategory);
        String[] projection = FileCategoryHelper.getProjection();
        String selection = FileCategoryHelper.buildSelectionByCategory(fileCategory);
        String[] selectionArgs = null;
        String sortOrder = FileCategoryHelper.buildSortOrder(fileCategory);

        Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, sortOrder);

        if (cursor == null) {
            return null;
        }

        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }

        ArrayList<FileItem> musicItems = new ArrayList<>();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            FileItem musicItem = new FileItem(cursor);
            musicItems.add(musicItem);
        }
        cursor.close();
        return musicItems;
    }
}
