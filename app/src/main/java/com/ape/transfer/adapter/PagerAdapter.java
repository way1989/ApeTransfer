package com.ape.transfer.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.ape.transfer.App;
import com.ape.transfer.fragment.MusicFragment;
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
        FileCategoryHelper.FileCategory fileCategory = FileCategoryHelper.sCategories[position];
        switch (fileCategory) {
            case Music:
                return MusicFragment.newInstance();
            case Video:
                return MusicFragment.newInstance();
            case Picture:
                return MusicFragment.newInstance();
            case Doc:
                return MusicFragment.newInstance();
            case Apk:
                return MusicFragment.newInstance();
            case Zip:
                return MusicFragment.newInstance();
            default:
                break;
        }
        return MusicFragment.newInstance();
    }

    @Override
    public int getCount() {
        return FileCategoryHelper.sCategories.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        FileCategoryHelper.FileCategory fileCategory = FileCategoryHelper.sCategories[position];
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


}
