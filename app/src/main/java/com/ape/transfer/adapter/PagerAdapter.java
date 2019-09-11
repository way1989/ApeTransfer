package com.ape.transfer.adapter;

import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.ape.transfer.App;
import com.ape.transfer.fragment.FileFragment;
import com.ape.transfer.util.FileCategoryHelper;

/**
 * Created by android on 16-6-28.
 */
public class PagerAdapter extends FragmentPagerAdapter {
    private Fragment mCurrentFragment;

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        int fileCategory = FileCategoryHelper.sCategories[position];
        return FileFragment.newInstance(fileCategory);

    }

    @Override
    public int getCount() {
        return FileCategoryHelper.sCategories.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        int fileCategory = FileCategoryHelper.sCategories[position];
        return App.getApp().getString(FileCategoryHelper.categoryNames.get(fileCategory));
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        mCurrentFragment = (Fragment) object;
    }

    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

}
