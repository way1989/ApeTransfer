package com.ape.transfer.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by android on 16-7-4.
 */
public class TaskHistory {
    private static TaskHistory sInstance = null;

    private TransferDB mTransferDB = null;

    public TaskHistory(final Context context) {
        mTransferDB = TransferDB.getInstance(context);
    }

    public static final synchronized TaskHistory getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new TaskHistory(context.getApplicationContext());
        }
        return sInstance;
    }

    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TaskHistoryColumns.NAME
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TaskHistoryColumns.DEVICE_ID + " STRING NOT NULL,"
                + TaskHistoryColumns.TITLE + " STRING NOT NULL,"
                + TaskHistoryColumns.DIRECTION + " STRING NOT NULL,"
                + TaskHistoryColumns.CREATE_TIME + " STRING NOT NULL,"
                + TaskHistoryColumns.NET + " TEXT NOT NULL,"
                + TaskHistoryColumns.SAVE_PATH + " TEXT NOT NULL,"
                + TaskHistoryColumns.FILE_PATH + " TEXT NOT NULL,"
                + TaskHistoryColumns.THUMB_URL + " TEXT NOT NULL,"
                + TaskHistoryColumns.CATEGORY + " TEXT NOT NULL,"
                + TaskHistoryColumns.MIME_TYPE + " TEXT NOT NULL,"
                + TaskHistoryColumns.SIZE + " TEXT NOT NULL,"
                + TaskHistoryColumns.POSITION + " TEXT NOT NULL,"
                + TaskHistoryColumns.STATUS + " TEXT NOT NULL,"
                + TaskHistoryColumns.STATUS + " TEXT NOT NULL,"
                + TaskHistoryColumns.MD5 + " TEXT NOT NULL,"
                + TaskHistoryColumns.PRIORITY + " TEXT NOT NULL,"
                + TaskHistoryColumns.READ + " TEXT NOT NULL,"
                + TaskHistoryColumns.DELETED + " TEXT NOT NULL,"
                + TaskHistoryColumns.LAST_MODIFIED + " TEXT NOT NULL);");
    }

    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TaskHistoryColumns.NAME);
        onCreate(db);
    }

    public interface TaskHistoryColumns {
        /* Table name */
        String NAME = "tasks";
        public static final String DEVICE_ID = "device_id";
        public static final String TITLE = "title";
        public static final String DIRECTION = "direction";
        public static final String CREATE_TIME = "create_time";
        public static final String NET = "net";
        public static final String SAVE_PATH = "save_path";
        public static final String FILE_PATH = "file_path";
        public static final String THUMB_URL = "thumb_url";
        public static final String CATEGORY = "category";
        public static final String MIME_TYPE = "mine_type";
        public static final String SIZE = "size";
        public static final String POSITION = "position";
        public static final String STATUS = "status";
        public static final String MD5 = "md5";
        public static final String PRIORITY = "priority";
        public static final String READ = "read";
        public static final String DELETED = "deleted";
        public static final String LAST_MODIFIED = "last_modified";

    }
}
