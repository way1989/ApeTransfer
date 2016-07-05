/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.ape.transfer.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import com.ape.transfer.App;

public final class PreferenceUtil {

    private static final String PERFERENCE_MAC = "perference_mac";
    private static final String PERFERENCE_ALIAS = "perference_alias";
    private static final String PERFERENCE_HEAD = "perference_head";

    private static PreferenceUtil sInstance;

    private static SharedPreferences mPreferences;

    private PreferenceUtil(final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static final PreferenceUtil getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new PreferenceUtil(context.getApplicationContext());
        }
        return sInstance;
    }

    public static final PreferenceUtil getInstance() {
        if (sInstance == null) {
            sInstance = new PreferenceUtil(App.getContext());
        }
        return sInstance;
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public String getAlias() {
        return mPreferences.getString(PERFERENCE_ALIAS, "");
    }

    public void setAlias(String alias) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PERFERENCE_ALIAS, alias);
        editor.apply();
    }

    public int getHead() {
        return mPreferences.getInt(PERFERENCE_HEAD, 0);
    }

    public void setHead(int head) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(PERFERENCE_HEAD, head);
        editor.apply();
    }

    public String getMac() {
        return mPreferences.getString(PERFERENCE_MAC, "");
    }

    public void setMac(String mac) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PERFERENCE_MAC, mac);
        editor.apply();
    }
}