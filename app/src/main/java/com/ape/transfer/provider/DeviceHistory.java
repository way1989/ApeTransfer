package com.ape.transfer.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.ape.transfer.p2p.beans.Peer;

/**
 * Created by android on 16-7-4.
 */
public class DeviceHistory {
    private static DeviceHistory sInstance = null;

    private TransferDB mTransferDB = null;

    public DeviceHistory() {
        mTransferDB = TransferDB.getInstance();
    }

    public static final synchronized DeviceHistory getInstance() {
        if (sInstance == null) {
            sInstance = new DeviceHistory();
        }
        return sInstance;
    }

    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DeviceHistoryColumns.TABLE_NAME
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DeviceHistoryColumns.WIFI_MAC + " TEXT NOT NULL,"
                + DeviceHistoryColumns.ALIAS + " TEXT NOT NULL,"
                + DeviceHistoryColumns.AVATAR + " TEXT NOT NULL,"
                + DeviceHistoryColumns.MODEL + " TEXT NOT NULL,"
                + DeviceHistoryColumns.BRAND + " TEXT NOT NULL,"
                + DeviceHistoryColumns.SDK_INT + " TEXT NOT NULL,"
                + DeviceHistoryColumns.VERSION_CODE + " TEXT NOT NULL,"
                + DeviceHistoryColumns.DATABASE_VERSION + " TEXT NOT NULL,"
                + DeviceHistoryColumns.LAST_TIME + " TEXT NOT NULL);");
    }

    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DeviceHistoryColumns.TABLE_NAME);
        onCreate(db);
    }

    public void addDevice(Peer neighbor) {
        if (neighbor == null)
            return;
        final SQLiteDatabase database = mTransferDB.getWritableDatabase();
        database.beginTransaction();
        try {
            database.delete(DeviceHistoryColumns.TABLE_NAME,
                    DeviceHistoryColumns.WIFI_MAC + " = ? COLLATE NOCASE", new String[]{neighbor.wifiMac});

            final ContentValues values = new ContentValues();
            values.put(DeviceHistoryColumns.WIFI_MAC, neighbor.wifiMac);
            values.put(DeviceHistoryColumns.ALIAS, neighbor.alias);
            values.put(DeviceHistoryColumns.AVATAR, neighbor.avatar);
            values.put(DeviceHistoryColumns.MODEL, neighbor.mode);
            values.put(DeviceHistoryColumns.BRAND, neighbor.brand);
            values.put(DeviceHistoryColumns.SDK_INT, neighbor.sdkInt);
            values.put(DeviceHistoryColumns.VERSION_CODE, neighbor.versionCode);
            values.put(DeviceHistoryColumns.DATABASE_VERSION, neighbor.databaseVersion);
            values.put(DeviceHistoryColumns.LAST_TIME, neighbor.lastTime);
            database.insert(DeviceHistoryColumns.TABLE_NAME, null, values);

        } finally {
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    public Peer getDevice(String wifiMac) {
        if (TextUtils.isEmpty(wifiMac))
            return null;
        final SQLiteDatabase database = mTransferDB.getReadableDatabase();
        Cursor cursor = database.query(DeviceHistoryColumns.TABLE_NAME,
                null, DeviceHistoryColumns.WIFI_MAC + " = ?", new String[]{wifiMac}, null, null, null);
        if (cursor == null)
            return null;
        if (cursor.getCount() < 1) {
            cursor.close();
            return null;
        }
        cursor.moveToFirst();
        Peer neighbor = new Peer();
        neighbor.wifiMac = cursor.getString(cursor.getColumnIndex(DeviceHistoryColumns.WIFI_MAC));
        neighbor.alias = cursor.getString(cursor.getColumnIndex(DeviceHistoryColumns.ALIAS));
        neighbor.avatar = cursor.getInt(cursor.getColumnIndex(DeviceHistoryColumns.AVATAR));
        neighbor.mode = cursor.getString(cursor.getColumnIndex(DeviceHistoryColumns.MODEL));
        neighbor.brand = cursor.getString(cursor.getColumnIndex(DeviceHistoryColumns.BRAND));
        neighbor.sdkInt = cursor.getInt(cursor.getColumnIndex(DeviceHistoryColumns.SDK_INT));
        neighbor.versionCode = cursor.getInt(cursor.getColumnIndex(DeviceHistoryColumns.VERSION_CODE));
        neighbor.databaseVersion = cursor.getInt(cursor.getColumnIndex(DeviceHistoryColumns.DATABASE_VERSION));
        neighbor.lastTime = cursor.getInt(cursor.getColumnIndex(DeviceHistoryColumns.LAST_TIME));

        cursor.close();
        return neighbor;
    }


    public interface DeviceHistoryColumns {
        /* Table name */
        String TABLE_NAME = "devices";
        public static final String WIFI_MAC = "wifi_mac";
        public static final String ALIAS = "alias";
        public static final String AVATAR = "avatar";
        public static final String MODEL = "model";
        public static final String BRAND = "brand";
        public static final String SDK_INT = "sdk_int";
        public static final String VERSION_CODE = "version_code";
        public static final String DATABASE_VERSION = "database_version";
        public static final String LAST_TIME = "last_time";

    }

}
