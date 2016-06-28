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
        FileCategoryHelper.FileCategory fileCategory = FileCategoryHelper.sCategories[position];
        switch (fileCategory) {
            case Music:
                return FileFragment.newInstance(FileCategoryHelper.FileCategory.Music);
            case Video:
                return FileFragment.newInstance(FileCategoryHelper.FileCategory.Video);
            case Picture:
                return FileFragment.newInstance(FileCategoryHelper.FileCategory.Picture);
            case Doc:
                return FileFragment.newInstance(FileCategoryHelper.FileCategory.Doc);
            case Apk:
                return FileFragment.newInstance(FileCategoryHelper.FileCategory.Apk);
            case Zip:
                return FileFragment.newInstance(FileCategoryHelper.FileCategory.Zip);
            default:
                break;
        }
        return FileFragment.newInstance(FileCategoryHelper.FileCategory.Music);
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
