package com.ape.transfer.fragment.loader;

import android.content.Context;

import com.ape.backuprestore.PersonalItemData;
import com.ape.backuprestore.modules.AppBackupComposer;
import com.ape.backuprestore.modules.CalendarBackupComposer;
import com.ape.backuprestore.modules.CallLogBackupComposer;
import com.ape.backuprestore.modules.Composer;
import com.ape.backuprestore.modules.ContactBackupComposer;
import com.ape.backuprestore.modules.MmsBackupComposer;
import com.ape.backuprestore.modules.MusicBackupComposer;
import com.ape.backuprestore.modules.NoteBookBackupComposer;
import com.ape.backuprestore.modules.PictureBackupComposer;
import com.ape.backuprestore.modules.SmsBackupComposer;
import com.ape.backuprestore.utils.ModuleType;
import com.ape.transfer.util.Log;

import java.util.ArrayList;

/**
 * Created by android on 16-11-1.
 */

public class BackupDataLoader extends BaseLoader<PersonalItemData> {
    private static final String TAG = "BackupDataLoader";
    private static final int TYPES[] = new int[]{
            ModuleType.TYPE_CONTACT,
            ModuleType.TYPE_MESSAGE,
            ModuleType.TYPE_CALL_LOG,
            ModuleType.TYPE_CALENDAR,
            ModuleType.TYPE_PICTURE,
            ModuleType.TYPE_MUSIC,
            ModuleType.TYPE_APP
    };

    public BackupDataLoader(Context context) {
        super(context);
    }

    @Override
    public Result loadInBackground() {
        ArrayList<PersonalItemData> personalItemDatas = getData();

        Result<PersonalItemData> result = new Result<>();
        result.lists = personalItemDatas;
        return result;
    }

    private ArrayList<PersonalItemData> getData() {
        ArrayList<PersonalItemData> personalItemDatas = new ArrayList<>();
        for (int type : TYPES) {
            int count = 0;
            switch (type) {
                case ModuleType.TYPE_CONTACT:
                    count = getModulesCount(new ContactBackupComposer(getContext()));
                    break;
                case ModuleType.TYPE_MESSAGE:
                    int countSMS = 0;
                    int countMMS = 0;
                    Composer smsBackupComposer = new SmsBackupComposer(getContext());
                    if (smsBackupComposer.init()) {
                        countSMS = smsBackupComposer.getCount();
                        smsBackupComposer.onEnd();
                    }

                    Composer mmsBackupComposer = new MmsBackupComposer(getContext());
                    if (mmsBackupComposer.init()) {
                        countMMS = mmsBackupComposer.getCount();
                        mmsBackupComposer.onEnd();
                    }
                    count = countSMS + countMMS;
                    Log.i(TAG, "countSMS = " + countSMS + ", countMMS = " + countMMS);
                    break;
                case ModuleType.TYPE_PICTURE:
                    count = getModulesCount(new PictureBackupComposer(getContext()));
                    break;
                case ModuleType.TYPE_CALENDAR:
                    count = getModulesCount(new CalendarBackupComposer(getContext()));
                    break;
                case ModuleType.TYPE_APP:
                    count = getModulesCount(new AppBackupComposer(getContext()));
                    break;
                case ModuleType.TYPE_MUSIC:
                    count = getModulesCount(new MusicBackupComposer(getContext()));

                    break;
                case ModuleType.TYPE_NOTEBOOK:
                    count = getModulesCount(new NoteBookBackupComposer(getContext()));
                    break;
                case ModuleType.TYPE_CALL_LOG:
                    count = getModulesCount(new CallLogBackupComposer(getContext()));
                    break;
                default:
                    Log.i(TAG, "Unknown module type: " + type);
                    break;
            }
            PersonalItemData item = new PersonalItemData(type, count);
            Log.i(TAG, "Add module type: " + type);
            personalItemDatas.add(item);
        }
        return personalItemDatas;
    }

    private int getModulesCount(Composer... composers) {
        int count = 0;
        for (Composer composer : composers) {
            if (composer.init()) {
                count += composer.getCount();
                composer.onEnd();
            }
        }
        Log.i(TAG, "getModulesCount : " + count);
        return count;
    }
}
