package com.ape.transfer.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ape.transfer.BuildConfig;
import com.ape.transfer.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutActivity extends BaseActivity {

    @BindView(R.id.iv_logo)
    ImageView ivLogo;
    @BindView(R.id.tv_appName)
    TextView tvAppName;
    @BindView(R.id.tv_version)
    TextView tvVersion;
    @BindView(R.id.dividing_line)
    View dividingLine;
    @BindView(R.id.my_avatar)
    RelativeLayout myAvatar;
    @BindView(R.id.listView)
    ListView listView;
    @BindView(R.id.editText)
    TextView editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setElevation(0f);
        tvAppName.setText(R.string.app_name);
        tvVersion.setText(BuildConfig.VERSION_NAME);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_about;
    }
}
