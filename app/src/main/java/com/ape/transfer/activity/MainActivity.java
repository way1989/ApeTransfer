package com.ape.transfer.activity;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.ape.transfer.R;
import com.ape.transfer.fragment.ExchangeFragment;
import com.ape.transfer.fragment.TransferFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener,
        AdapterView.OnItemSelectedListener {
    private static final int PAGE_TRANSFER = 0;
    private static final int PAGE_EXCHANGE = 1;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.spinner)
    Spinner spinner;
    @BindView(R.id.container)
    ViewPager container;

    Runnable navigateTransfer = new Runnable() {
        public void run() {
            navView.getMenu().findItem(R.id.nav_transfer).setChecked(true);
            spinner.setSelection(PAGE_TRANSFER, true);
            container.setCurrentItem(PAGE_TRANSFER);
        }
    };
    Runnable navigateExchange = new Runnable() {
        public void run() {
            navView.getMenu().findItem(R.id.nav_exchange).setChecked(true);
            spinner.setSelection(PAGE_EXCHANGE, true);
            container.setCurrentItem(PAGE_EXCHANGE);
        }
    };
    private ActionBarDrawerToggle toggle;
    private MainPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navView.setNavigationItemSelectedListener(this);

        //setup viewpager
        mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragment(TransferFragment.newInstance("", ""), getString(R.string.main_bottom_transfer));
        mPagerAdapter.addFragment(ExchangeFragment.newInstance("", ""), getString(R.string.main_bottom_exchange));
        container.setAdapter(mPagerAdapter);
        container.addOnPageChangeListener(this);

        // Setup spinner
        spinner.setAdapter(new SpinnerAdapter(
                toolbar.getContext(),
                new String[]{
                        getResources().getString(R.string.main_bottom_transfer),
                        getResources().getString(R.string.main_bottom_exchange)
                }));
        spinner.setOnItemSelectedListener(this);

        navigateTransfer.run();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //viewpager do nothing
    }

    @Override
    public void onPageSelected(int position) {
        spinner.setSelection(position, true);
        switch (position) {
            case PAGE_TRANSFER:
                navView.getMenu().findItem(R.id.nav_transfer).setChecked(true);
                break;
            case PAGE_EXCHANGE:
                navView.getMenu().findItem(R.id.nav_exchange).setChecked(true);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        //viewpager do nothing
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case PAGE_TRANSFER:
                spinner.post(navigateTransfer);
                break;
            case PAGE_EXCHANGE:
                spinner.post(navigateExchange);
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //spinner do nothing
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        drawerLayout.removeDrawerListener(toggle);
        container.removeOnPageChangeListener(this);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_transfer) {
            item.setChecked(true);
            navView.postDelayed(navigateTransfer, 350L);
        } else if (id == R.id.nav_exchange) {
            item.setChecked(true);
            navView.postDelayed(navigateExchange, 350L);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private static class SpinnerAdapter extends ArrayAdapter<String> implements ThemedSpinnerAdapter {
        private final Helper mDropDownHelper;

        public SpinnerAdapter(Context context, String[] objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
            mDropDownHelper = new Helper(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                view = convertView;
            }

            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getItem(position));

            return view;
        }

        @Override
        public Resources.Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Resources.Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }
    }

    public class MainPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
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
