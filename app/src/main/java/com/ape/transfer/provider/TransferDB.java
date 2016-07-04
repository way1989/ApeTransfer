package com.ape.transfer.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by android on 16-7-4.
 */
public class TransferDB extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "transfer.db";
    public static final int VERSION = 1;
    private static TransferDB sInstance = null;

    private final Context mContext;

    public TransferDB(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        mContext = context;
    }

    public static final synchronized TransferDB getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new TransferDB(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        DeviceHistory.getInstance(mContext).onCreate(db);
        TaskHistory.getInstance(mContext).onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DeviceHistory.getInstance(mContext).onUpgrade(db, oldVersion, newVersion);
        TaskHistory.getInstance(mContext).onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DeviceHistory.getInstance(mContext).onDowngrade(db, oldVersion, newVersion);
        TaskHistory.getInstance(mContext).onDowngrade(db, oldVersion, newVersion);
    }
}
