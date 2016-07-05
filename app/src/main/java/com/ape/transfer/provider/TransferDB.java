package com.ape.transfer.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ape.transfer.App;

/**
 * Created by android on 16-7-4.
 */
public class TransferDB extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "transfer.db";
    public static final int VERSION = 1;
    private static TransferDB sInstance = null;


    public TransferDB(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    public static final synchronized TransferDB getInstance() {
        if (sInstance == null) {
            sInstance = new TransferDB(App.getContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        DeviceHistory.getInstance().onCreate(db);
        TaskHistory.getInstance().onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DeviceHistory.getInstance().onUpgrade(db, oldVersion, newVersion);
        TaskHistory.getInstance().onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DeviceHistory.getInstance().onDowngrade(db, oldVersion, newVersion);
        TaskHistory.getInstance().onDowngrade(db, oldVersion, newVersion);
    }
}
