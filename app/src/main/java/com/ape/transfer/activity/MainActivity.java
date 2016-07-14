package com.ape.transfer.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.ape.transfer.R;
import com.ape.transfer.fragment.ExchangeFragment;
import com.ape.transfer.fragment.TransferFragment;
import com.ape.transfer.util.OsUtil;
import com.ape.transfer.util.PreferenceUtil;
import com.ape.transfer.util.Util;
import com.ape.transfer.util.WifiApUtils;
import com.ape.transfer.zxing.activity.CaptureActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener,
        AdapterView.OnItemSelectedListener {
    private static final int PAGE_TRANSFER = 0;
    private static final int PAGE_EXCHANGE = 1;
    private static final int REQUEST_CODE = 2;
    private static final String TAG = "MainActivity";
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
    Runnable navigateShare = new Runnable() {
        public void run() {
            String url = getString(R.string.share_app);
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            sharingIntent.putExtra(Intent.EXTRA_TEXT, url);
            sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Intent chooserIntent = Intent.createChooser(sharingIntent, null);
            startActivity(chooserIntent);
        }
    };
    Runnable navigateFeedback = new Runnable() {
        public void run() {
            Map<String, String> map = new HashMap<>();
            map.put("themeColor", "#4CAF50");
            map.put("hideLoginSuccess", "true");
            map.put("enableAudio", "0");
            FeedbackAPI.setUICustomInfo(map);
            FeedbackAPI.openFeedbackActivity(MainActivity.this);
        }
    };

    Runnable navigateHelp = new Runnable() {
        public void run() {
            Intent intent = new Intent(MainActivity.this, UserGuideActivity.class);
            startActivity(intent);
        }
    };

    Runnable navigateAbout = new Runnable() {
        public void run() {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        }
    };

    View.OnClickListener headViewOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivityForResult(new Intent(MainActivity.this, UserInfoActivity.class), REQUEST_CODE);
        }
    };
    private ActionBarDrawerToggle toggle;

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
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        setupNavView();

        //setup viewpager
        setupViewpager();

        // Setup spinner
        setupSpinner();

        setupWifiMac();
        navigateTransfer.run();
    }

    private void setupWifiMac() {
        if (TextUtils.isEmpty(PreferenceUtil.getInstance().getMac())) {
            String mac = WifiApUtils.getInstance((WifiManager) getSystemService(Context.WIFI_SERVICE)).getWifiMacFromDevice();
            if (!TextUtils.isEmpty(mac))
                PreferenceUtil.getInstance().setMac(mac);
            else
                throw new NullPointerException("cann't get wifi mac");
        }
    }

    private void setupNavView() {
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navView.setNavigationItemSelectedListener(this);
        setupAliasAndHead();
    }

    private void setupSpinner() {
        spinner.setAdapter(new SpinnerAdapter(
                toolbar.getContext(),
                new String[]{
                        getResources().getString(R.string.main_bottom_transfer),
                        getResources().getString(R.string.main_bottom_exchange)
                }));
        spinner.setOnItemSelectedListener(this);
    }

    private void setupViewpager() {
        MainPagerAdapter mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragment(TransferFragment.newInstance(), getString(R.string.main_bottom_transfer));
        mPagerAdapter.addFragment(ExchangeFragment.newInstance(), getString(R.string.main_bottom_exchange));
        container.setAdapter(mPagerAdapter);
        container.addOnPageChangeListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            setupAliasAndHead();
        }
    }

    private void setupAliasAndHead() {
        ImageView headView = (ImageView) navView.getHeaderView(0).findViewById(R.id.nav_head_icon);
        headView.setOnClickListener(headViewOnClick);
        TextView aliasView = (TextView) navView.getHeaderView(0).findViewById(R.id.nav_head_alias);
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(getApplicationContext());
        int headPosition = preferenceUtil.getHead();
        String alias = preferenceUtil.getAlias();
        headView.setImageResource(UserInfoActivity.HEAD[headPosition]);
        aliasView.setText(TextUtils.isEmpty(alias) ? Build.MODEL : alias);
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
        if (drawerLayout != null)
            drawerLayout.removeDrawerListener(toggle);
        if (container != null)
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
        switch (item.getItemId()) {
            case R.id.action_history:
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_transfer:
                item.setChecked(true);
                navView.removeCallbacks(navigateTransfer);
                navView.postDelayed(navigateTransfer, 350L);
                break;
            case R.id.nav_exchange:
                item.setChecked(true);
                navView.removeCallbacks(navigateExchange);
                navView.postDelayed(navigateExchange, 350L);
                break;
            case R.id.nav_help:
                navView.removeCallbacks(navigateHelp);
                navView.postDelayed(navigateHelp, 350L);
                break;
            case R.id.nav_share:
                navView.removeCallbacks(navigateShare);
                navView.postDelayed(navigateShare, 250L);
                break;
            case R.id.nav_feedback:
                navView.removeCallbacks(navigateFeedback);
                navView.postDelayed(navigateFeedback, 350L);
                break;
            case R.id.nav_settings:
                break;
            case R.id.nav_about:
                navView.removeCallbacks(navigateAbout);
                navView.postDelayed(navigateAbout, 350L);
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @OnClick(R.id.fab)
    public void onClick() {
        MainActivityPermissionsDispatcher.startQrCodeScanWithCheck(this);
    }


    private void showNeverAskAgainDialog(@StringRes int messageResId) {
        new AlertDialog.Builder(this)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        Util.startSettingsPermission(getApplicationContext());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(false)
                .setTitle(R.string.permission_never_ask_title)
                .setMessage(messageResId)
                .show();
    }

    private void showRationaleDialog(@StringRes int messageResId, final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setTitle(R.string.permission_title)
                .setMessage(messageResId)
                .show();
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    void startQrCodeScan() {
        startActivity(new Intent(this, CaptureActivity.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    void showCameraRationale(PermissionRequest request) {
        showRationaleDialog(R.string.required_permissions_promo, request);
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    void showCameraDenied() {
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    void showCameraNeverAsk() {
        showNeverAskAgainDialog(R.string.enable_permission_procedure);
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
