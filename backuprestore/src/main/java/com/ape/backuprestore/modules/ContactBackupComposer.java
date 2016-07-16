package com.ape.backuprestore.modules;

import android.content.Context;

import com.ape.backuprestore.utils.ModuleType;

/**
 * Created by android on 16-7-16.
 */
public class ContactBackupComposer extends Composer {
    public ContactBackupComposer(Context context) {
        super(context);
    }

    @Override
    public int getModuleType() {
        return ModuleType.TYPE_CONTACT;
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
