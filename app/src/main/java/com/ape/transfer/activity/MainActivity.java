package com.ape.transfer.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.ape.transfer.R;
import com.ape.transfer.fragment.ExchangeFragment;
import com.ape.transfer.fragment.TransferFragment;
import com.ape.transfer.util.OsUtil;
import com.ape.transfer.util.PreferenceUtil;
import com.ape.transfer.util.Screen;
import com.ape.transfer.util.WifiApUtils;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 2;
    private static final String TAG = "MainActivity";
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.container)
    ViewPager mViewPager;

    View.OnClickListener headViewOnClick = v -> startActivityForResult(new Intent(MainActivity.this, UserInfoActivity.class), REQUEST_CODE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (OsUtil.redirectToPermissionCheckIfNeeded(this)) {
            return;
        }
        if (TextUtils.isEmpty(PreferenceUtil.getInstance(getApplicationContext()).getAlias())) {
            startActivityForResult(new Intent(MainActivity.this, UserInfoActivity.class), REQUEST_CODE);
        }
        if (PreferenceUtil.getInstance().isFirstRun())
            startActivity(new Intent(MainActivity.this, GuideActivity.class));

        setContentView(R.layout.app_bar_main);
        ButterKnife.bind(this);

        setupToolbar();

        setupNavigationIcon();

        setupViewpager();

        setupWifiMac();
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setupNavigationIcon() {
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(getApplicationContext());
        int headPosition = preferenceUtil.getHead();
        Drawable logo = getDrawable(UserInfoActivity.HEAD[headPosition]);
        logo = Screen.zoomDrawable(logo, Screen.dp(72), Screen.dp(72));
        if (logo != null) {
            mToolbar.setNavigationIcon(logo);
            mToolbar.setNavigationOnClickListener(headViewOnClick);
        }
    }

    private void setupWifiMac() {
        if (TextUtils.isEmpty(PreferenceUtil.getInstance().getMac())) {
            String mac = WifiApUtils.getInstance().getWifiMacFromDevice();
            if (!TextUtils.isEmpty(mac)) {
                PreferenceUtil.getInstance().setMac(mac);
            } else {
                //throw new NullPointerException("cann't get wifi mac");
                Toast.makeText(getApplicationContext(), "cann't get device wifi mac!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void setupViewpager() {
        MainPagerAdapter mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragment(TransferFragment.newInstance(), getString(R.string.main_bottom_transfer));
        mPagerAdapter.addFragment(ExchangeFragment.newInstance(), getString(R.string.main_bottom_exchange));
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            setupNavigationIcon();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_history:
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            case R.id.action_help:
                startActivity(new Intent(MainActivity.this, UserGuideActivity.class));
                break;
            case R.id.action_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class MainPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            //mCurrentFragment = (Fragment) object;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }
}
