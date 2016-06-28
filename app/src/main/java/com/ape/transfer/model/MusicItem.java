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
public class MusicItem {
    public static ArrayList<MusicItem> getMusicItems(Context context) {
        ArrayList<MusicItem> musicItems = new ArrayList<>();

        final ContentResolver resolver = context.getContentResolver();

        Uri uri = FileCategoryHelper.getContentUriByCategory(FileCategoryHelper.FileCategory.Music);
        String[] projection = FileCategoryHelper.getProjection();
        String selection = "";
        String[] selectionArgs = null;
        String sortOrder = FileCategoryHelper.buildSortOrder(FileCategoryHelper.SortMethod.name);

        Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, sortOrder);

        if (cursor == null) {
            return musicItems;
        }

        if (cursor.getCount() == 0) {
            cursor.close();
            return musicItems;
        }

        return musicItems;
    }
}
