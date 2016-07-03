package com.ape.transfer.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

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
        return App.getContext().getString(FileCategoryHelper.categoryNames.get(fileCategory));
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        mCurrentFragment = (Fragment) object;
    }

    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }


    public void clearAllSelect() {
    }
}
