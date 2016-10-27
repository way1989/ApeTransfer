package com.ape.transfer.util;

import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;

import com.ape.transfer.R;
import com.ape.transfer.p2p.util.Constant;

import java.util.HashMap;

public class FileCategoryHelper {
    public static final String[] DOC_SUPPORTED_SUFFIX = {".txt", ".log", ".xml", ".ini", ".lrc"};
    public static final String[] SUPPORTED_ARCHIVES = {".zip", ".rar"};
    private static final String TAG = "FileCategoryHelper";
    public static HashMap<Integer, Integer> categoryNames = new HashMap<Integer, Integer>();
    public static int[] sCategories = new int[]{
            Constant.TYPE.MUSIC, Constant.TYPE.VIDEO, Constant.TYPE.PIC,
            Constant.TYPE.DOC, Constant.TYPE.ZIP, Constant.TYPE.APP
    };

    static {
        categoryNames.put(Constant.TYPE.MUSIC, R.string.category_music);
        categoryNames.put(Constant.TYPE.VIDEO, R.string.category_video);
        categoryNames.put(Constant.TYPE.PIC, R.string.category_picture);
        categoryNames.put(Constant.TYPE.DOC, R.string.category_document);
        categoryNames.put(Constant.TYPE.ZIP, R.string.category_zip);
        categoryNames.put(Constant.TYPE.APP, R.string.category_apk);
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

    public static String buildSelectionByCategory(int cat) {
        String selection = null;
        switch (cat) {
            case Constant.TYPE.DOC:
                selection = buildDocSelection();
                break;
            case Constant.TYPE.ZIP:
                selection = "(format != 12289) and (";
                for (String str : SUPPORTED_ARCHIVES) {
                    selection = selection + "(" + FileColumns.DATA + " LIKE '%"
                            + str + "') OR ";
                }
                selection = selection.substring(0, selection.length() - 3);
                selection += ")";
                break;
            case Constant.TYPE.APP:
                selection = FileColumns.DATA + " LIKE '%.apk'";
                break;
            default:
                selection = null;
        }
        return selection;
    }

    public static Uri getContentUriByCategory(int cat) {
        Uri uri;
        String volumeName = "external";
        switch (cat) {
            case Constant.TYPE.DOC:
            case Constant.TYPE.ZIP:
            case Constant.TYPE.APP:
                uri = Files.getContentUri(volumeName);
                break;
            case Constant.TYPE.MUSIC:
                uri = Audio.Media.getContentUri(volumeName);
                break;
            case Constant.TYPE.VIDEO:
                uri = Video.Media.getContentUri(volumeName);
                break;
            case Constant.TYPE.PIC:
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

    public static String buildSortOrder(int fileCategory) {
        switch (fileCategory) {
            case Constant.TYPE.DOC:
            case Constant.TYPE.ZIP:
            case Constant.TYPE.APP:
                return buildSortOrder(SortMethod.name);
            case Constant.TYPE.MUSIC:
            case Constant.TYPE.VIDEO:
            case Constant.TYPE.PIC:
                return buildSortOrder(SortMethod.date);
            default:
                break;
        }
        return buildSortOrder(SortMethod.name);
    }

    public enum SortMethod {
        name, size, date, type
    }
}
