package com.ape.backuprestore.modules;

import android.content.Context;
import android.net.Uri;
import android.provider.CallLog;

import com.ape.backuprestore.utils.ModuleType;

/**
 * Created by android on 16-7-20.
 */
public class CallLogComposer extends Composer{
    private static final Uri CALL_LOG_URI = CallLog.Calls.CONTENT_URI;
    static final String[] CALL_LOG_PROJECTION = new String[] {
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.COUNTRY_ISO,
            CallLog.Calls.GEOCODED_LOCATION,
            CallLog.Calls.NUMBER_PRESENTATION,
            CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME,
            CallLog.Calls.PHONE_ACCOUNT_ID,
            CallLog.Calls.FEATURES,
            CallLog.Calls.DATA_USAGE,
            CallLog.Calls.TRANSCRIPTION,
            CallLog.Calls.DURATION_TYPE
    };

    static final int DATE_COLUMN_INDEX = 0;
    static final int DURATION_COLUMN_INDEX = 1;
    static final int NUMBER_COLUMN_INDEX = 2;
    static final int CALL_TYPE_COLUMN_INDEX = 3;
    static final int COUNTRY_ISO_COLUMN_INDEX = 4;
    static final int GEOCODED_LOCATION_COLUMN_INDEX = 5;
    static final int NUMBER_PRESENTATION_COLUMN_INDEX = 6;
    static final int ACCOUNT_COMPONENT_NAME = 7;
    static final int ACCOUNT_ID = 8;
    static final int FEATURES = 9;
    static final int DATA_USAGE = 10;
    static final int TRANSCRIPTION_COLUMN_INDEX = 11;
    static final int DURATION_TYPE_COLUMN_INDEX = 12;

    public CallLogComposer(Context context) {
        super(context);
    }

    @Override
    public int getModuleType() {
        return ModuleType.TYPE_CALL_LOG;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public boolean isAfterLast() {
        return false;
    }

    @Override
    public boolean init() {
        return false;
    }

    @Override
    protected boolean implementComposeOneEntity() {
        return false;
    }
}
