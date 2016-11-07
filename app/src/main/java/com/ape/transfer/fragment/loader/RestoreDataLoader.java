package com.ape.transfer.fragment.loader;

import android.content.Context;

import com.ape.backuprestore.PersonalItemData;
import com.ape.backuprestore.utils.BackupFilePreview;
import com.ape.backuprestore.utils.ModuleType;

import java.util.ArrayList;

/**
 * Created by android on 16-11-1.
 */

public class RestoreDataLoader extends BaseLoader<PersonalItemData> {
    private static final String TAG = "BackupDataLoader";
    private static final int TYPES[] = new int[]{
            ModuleType.TYPE_APP,
            ModuleType.TYPE_CALENDAR,
            ModuleType.TYPE_CONTACT,
            ModuleType.TYPE_MESSAGE,
            ModuleType.TYPE_MUSIC,
            ModuleType.TYPE_PICTURE,
            ModuleType.TYPE_CALL_LOG,
    };

    public RestoreDataLoader(Context context) {
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
        BackupFilePreview preview = BackupFilePreview.getInstance();
        if (preview.init()) {
            int modules = preview.getBackupModules(getContext().getApplicationContext());
            for (int type : TYPES) {
                if ((modules & type) != 0) {
                    PersonalItemData item = new PersonalItemData(type, 1);
                    personalItemDatas.add(item);
                }
            }
        }
        return personalItemDatas;
    }

}
