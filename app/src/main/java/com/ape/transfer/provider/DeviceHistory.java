package com.ape.transfer.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.ape.transfer.p2p.p2pentity.P2PNeighbor;

/**
 * Created by android on 16-7-4.
 */
public class DeviceHistory {
    private static DeviceHistory sInstance = null;

    private TransferDB mMusicDatabase = null;

    public DeviceHistory(final Context context) {
        mMusicDatabase = TransferDB.getInstance(context);
    }

    public static final synchronized DeviceHistory getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new DeviceHistory(context.getApplicationContext());
        }
        return sInstance;
    }

    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DeviceHistoryColumns.NAME
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DeviceHistoryColumns.DEVICE_ID + " TEXT NOT NULL,"
                + DeviceHistoryColumns.NICK_NAME + " TEXT NOT NULL,"
                + DeviceHistoryColumns.AVATAR + " TEXT NOT NULL,"
                + DeviceHistoryColumns.IMEI + " TEXT NOT NULL,"
                + DeviceHistoryColumns.MODEL + " TEXT NOT NULL,"
                + DeviceHistoryColumns.BRAND + " TEXT NOT NULL,"
                + DeviceHistoryColumns.OS + " TEXT NOT NULL,"
                + DeviceHistoryColumns.SDK_INT + " TEXT NOT NULL,"
                + DeviceHistoryColumns.VERSION_CODE + " TEXT NOT NULL,"
                + DeviceHistoryColumns.DATABASE_VERSION + " TEXT NOT NULL,"
                + DeviceHistoryColumns.LAST_TIME + " TEXT NOT NULL);");
    }

    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DeviceHistoryColumns.NAME);
        onCreate(db);
    }
    public void addDevice(P2PNeighbor neighbor){
        if(neighbor == null)
            return;
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
    }
    public interface DeviceHistoryColumns {
        /* Table name */
        String NAME = "devices";
        public static final String DEVICE_ID = "device_id";
        public static final String NICK_NAME = "nick_name";
        public static final String AVATAR = "avatar";
        public static final String IMEI = "imei";
        public static final String MODEL = "model";
        public static final String BRAND = "brand";
        public static final String OS = "os";
        public static final String SDK_INT = "sdk_int";
        public static final String VERSION_CODE = "version_code";
        public static final String DATABASE_VERSION = "database_version";
        public static final String LAST_TIME = "last_time";

    }

}
