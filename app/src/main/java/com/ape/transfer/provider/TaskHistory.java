package com.ape.transfer.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ape.transfer.p2p.beans.TransferFile;

import java.util.ArrayList;

/**
 * Created by android on 16-7-4.
 */
public class TaskHistory {
    private static TaskHistory sInstance = null;

    private TransferDB mTransferDB = null;

    public TaskHistory() {
        mTransferDB = TransferDB.getInstance();
    }

    public static final synchronized TaskHistory getInstance() {
        if (sInstance == null) {
            sInstance = new TaskHistory();
        }
        return sInstance;
    }

    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TaskHistoryColumns.TABLE_NAME
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TaskHistoryColumns.WIFI_MAC + " TEXT NOT NULL,"
                + TaskHistoryColumns.TITLE + " TEXT NOT NULL,"
                + TaskHistoryColumns.DIRECTION + " TEXT NOT NULL,"
                + TaskHistoryColumns.CREATE_TIME + " TEXT NOT NULL,"
                + TaskHistoryColumns.SAVE_PATH + " TEXT,"
                + TaskHistoryColumns.FILE_PATH + " TEXT NOT NULL,"
                + TaskHistoryColumns.THUMB_URL + " TEXT,"
                + TaskHistoryColumns.CATEGORY + " TEXT NOT NULL,"
                + TaskHistoryColumns.MIME_TYPE + " TEXT,"
                + TaskHistoryColumns.SIZE + " TEXT NOT NULL,"
                + TaskHistoryColumns.POSITION + " TEXT,"
                + TaskHistoryColumns.LAST_MODIFIED + " TEXT,"
                + TaskHistoryColumns.STATUS + " TEXT NOT NULL,"
                + TaskHistoryColumns.MD5 + " TEXT NOT NULL,"
                + TaskHistoryColumns.READ + " TEXT NOT NULL,"
                + TaskHistoryColumns.DELETED + " TEXT NOT NULL);");
    }

    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TaskHistoryColumns.TABLE_NAME);
        onCreate(db);
    }

    public Cursor queryFileInfos(final String direction) {
        final SQLiteDatabase database = mTransferDB.getReadableDatabase();
        return database.query(TaskHistoryColumns.TABLE_NAME,
                null, TaskHistoryColumns.DIRECTION + " = ?", new String[]{direction}, null, null,
                TaskHistoryColumns.CREATE_TIME + " DESC");
    }

    public ArrayList<TransferFile> getAllFileInfos(boolean isSend) {
        Cursor searches = queryFileInfos(isSend ? "0" : "1");

        ArrayList<TransferFile> results = new ArrayList<>();
        if (searches == null)
            return results;
        if (searches.getCount() < 1) {
            searches.close();
            return results;
        }

        try {
            while (searches.moveToNext()) {
                TransferFile fileInfo = new TransferFile();
                fileInfo.wifiMac = searches.getString(searches.getColumnIndex(TaskHistoryColumns.WIFI_MAC));
                fileInfo.name = searches.getString(searches.getColumnIndex(TaskHistoryColumns.TITLE));
                fileInfo.direction = searches.getInt(searches.getColumnIndex(TaskHistoryColumns.DIRECTION));
                fileInfo.createTime = searches.getLong(searches.getColumnIndex(TaskHistoryColumns.CREATE_TIME));
                fileInfo.path = searches.getString(searches.getColumnIndex(TaskHistoryColumns.FILE_PATH));
                fileInfo.savePath = searches.getString(searches.getColumnIndex(TaskHistoryColumns.SAVE_PATH));
                fileInfo.thumbUrl = searches.getString(searches.getColumnIndex(TaskHistoryColumns.THUMB_URL));
                fileInfo.type = searches.getInt(searches.getColumnIndex(TaskHistoryColumns.CATEGORY));
                fileInfo.size = searches.getInt(searches.getColumnIndex(TaskHistoryColumns.SIZE));
                fileInfo.position = searches.getInt(searches.getColumnIndex(TaskHistoryColumns.POSITION));
                fileInfo.lastModify = searches.getInt(searches.getColumnIndex(TaskHistoryColumns.LAST_MODIFIED));
                fileInfo.status = searches.getInt(searches.getColumnIndex(TaskHistoryColumns.STATUS));
                fileInfo.md5 = searches.getString(searches.getColumnIndex(TaskHistoryColumns.MD5));
                fileInfo.read = searches.getInt(searches.getColumnIndex(TaskHistoryColumns.READ));
                fileInfo.deleted = searches.getInt(searches.getColumnIndex(TaskHistoryColumns.DELETED));

                results.add(fileInfo);
            }
        } finally {
            if (searches != null) {
                searches.close();
            }
        }

        return results;
    }

    public void addFileInfo(TransferFile fileInfo) {
        if (fileInfo == null) {
            return;
        }
        final SQLiteDatabase database = mTransferDB.getWritableDatabase();
        database.beginTransaction();

        try {
            final ContentValues values = new ContentValues();
            values.put(TaskHistoryColumns.WIFI_MAC, fileInfo.wifiMac);
            values.put(TaskHistoryColumns.TITLE, fileInfo.name);
            values.put(TaskHistoryColumns.DIRECTION, fileInfo.direction);
            values.put(TaskHistoryColumns.CREATE_TIME, fileInfo.createTime);
            values.put(TaskHistoryColumns.SAVE_PATH, fileInfo.savePath);
            values.put(TaskHistoryColumns.FILE_PATH, fileInfo.path);
            values.put(TaskHistoryColumns.CATEGORY, fileInfo.type);
            values.put(TaskHistoryColumns.MIME_TYPE, fileInfo.mineType);
            values.put(TaskHistoryColumns.SIZE, fileInfo.size);
            values.put(TaskHistoryColumns.POSITION, fileInfo.position);
            values.put(TaskHistoryColumns.LAST_MODIFIED, fileInfo.lastModify);
            values.put(TaskHistoryColumns.STATUS, fileInfo.status);
            values.put(TaskHistoryColumns.MD5, fileInfo.md5);
            values.put(TaskHistoryColumns.READ, fileInfo.read);
            values.put(TaskHistoryColumns.DELETED, fileInfo.deleted);
            database.insert(TaskHistoryColumns.TABLE_NAME, null, values);

        } finally {
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    public void updateFileInfo(TransferFile fileInfo) {
        if (fileInfo == null) {
            return;
        }
        final SQLiteDatabase database = mTransferDB.getWritableDatabase();
        database.beginTransaction();
        try {
            final ContentValues values = new ContentValues();
            values.put(TaskHistoryColumns.POSITION, fileInfo.position);
            values.put(TaskHistoryColumns.STATUS, fileInfo.status);

            database.update(TaskHistoryColumns.TABLE_NAME, values, TaskHistoryColumns.CREATE_TIME + " = ?"
                    + " and " + TaskHistoryColumns.MD5 + " = ?", new String[]{String.valueOf(fileInfo.createTime), fileInfo.md5});

        } finally {
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    public interface TaskHistoryColumns {
        /* Table name */
        String TABLE_NAME = "tasks";
        public static final String WIFI_MAC = "wifi_mac";
        public static final String TITLE = "title";
        public static final String DIRECTION = "direction";
        public static final String CREATE_TIME = "create_time";
        public static final String SAVE_PATH = "save_path";
        public static final String FILE_PATH = "file_path";
        public static final String THUMB_URL = "thumb_url";
        public static final String CATEGORY = "category";
        public static final String MIME_TYPE = "mine_type";
        public static final String SIZE = "size";
        public static final String POSITION = "position";
        public static final String LAST_MODIFIED = "last_modified";
        public static final String STATUS = "status";
        public static final String MD5 = "md5";
        public static final String READ = "read";
        public static final String DELETED = "deleted";

    }
}
