package com.ape.transfer.util;

import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;

import com.ape.transfer.R;

import java.util.HashMap;

public class FileCategoryHelper {
    public static final String[] DOC_SUPPORTED_SUFFIX = {".txt", ".log", ".xml", ".ini", ".lrc"};
    public static final String[] SUPPORTED_ARCHIVES = {".zip", ".rar"};
    private static final String TAG = "FileCategoryHelper";
    public static HashMap<FileCategory, Integer> categoryNames = new HashMap<FileCategory, Integer>();
    public static FileCategory[] sCategories = new FileCategory[]{
            FileCategory.Music, FileCategory.Video, FileCategory.Picture,
            FileCategory.Doc, FileCategory.Zip, FileCategory.Apk
    };

    static {
        categoryNames.put(FileCategory.Music, R.string.category_music);
        categoryNames.put(FileCategory.Video, R.string.category_video);
        categoryNames.put(FileCategory.Picture, R.string.category_picture);
        categoryNames.put(FileCategory.Doc, R.string.category_document);
        categoryNames.put(FileCategory.Zip, R.string.category_zip);
        categoryNames.put(FileCategory.Apk, R.string.category_apk);
    }


    private FileCategoryHelper() {
    }

    private static String buildDocSelection() {
        StringBuilder selection = new StringBuilder();
        selection.append("(format != 12289) and (");
        for (String str : DOC_SUPPORTED_SUFFIX) {
            selection.append("(" + FileColumns.DATA + " LIKE '%" + str + "') OR ");
        }
        return selection.substring(0, selection.lastIndexOf(")") + 1) + ")";
    }

    public static String buildSelectionByCategory(FileCategory cat) {
        String selection = null;
        switch (cat) {
            case Doc:
                selection = buildDocSelection();
                break;
            case Zip:
                selection = "(format != 12289) and (";
                for (String str : SUPPORTED_ARCHIVES) {
                    selection = selection + "(" + FileColumns.DATA + " LIKE '%"
                            + str + "') OR ";
                }
                selection = selection.substring(0, selection.length() - 3);
                selection += ")";
                break;
            case Apk:
                selection = FileColumns.DATA + " LIKE '%.apk'";
                break;
            default:
                selection = null;
        }
        return selection;
    }

    public static Uri getContentUriByCategory(FileCategory cat) {
        Uri uri;
        String volumeName = "external";
        switch (cat) {
            case Doc:
            case Zip:
            case Apk:
                uri = Files.getContentUri(volumeName);
                break;
            case Music:
                uri = Audio.Media.getContentUri(volumeName);
                break;
            case Video:
                uri = Video.Media.getContentUri(volumeName);
                break;
            case Picture:
                uri = Images.Media.getContentUri(volumeName);
                break;
            default:
                uri = null;
        }
        return uri;
    }

    public static String buildSortOrder(SortMethod sort) {
        String sortOrder = null;
        switch (sort) {
            case name:
                sortOrder = FileColumns.TITLE + " asc";
                break;
            case size:
                sortOrder = FileColumns.SIZE + " desc";
                break;
            case date:
                sortOrder = FileColumns.DATE_MODIFIED + " desc";
                break;
            case type:
                sortOrder = FileColumns.MIME_TYPE + " asc, " + FileColumns.TITLE + " asc";
                break;
        }
        return sortOrder;
    }

    public static String[] getProjection() {
        return new String[]{
                FileColumns._ID, FileColumns.DATA, FileColumns.SIZE, FileColumns.DATE_MODIFIED
        };
    }

    public static String buildSortOrder(FileCategory fileCategory) {
        switch (fileCategory) {
            case Doc:
            case Zip:
            case Apk:
                return buildSortOrder(SortMethod.name);
            case Music:
            case Video:
            case Picture:
                return buildSortOrder(SortMethod.date);
            default:
                break;
        }
        return buildSortOrder(SortMethod.name);
    }


    public enum FileCategory {
        Music, Video, Picture, Doc, Zip, Apk
    }

    public enum SortMethod {
        name, size, date, type
    }
}
