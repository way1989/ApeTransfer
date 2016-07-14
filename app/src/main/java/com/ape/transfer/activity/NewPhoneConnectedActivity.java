package com.ape.transfer.activity;

import android.os.Bundle;
import android.text.TextUtils;

import com.ape.transfer.R;

import butterknife.ButterKnife;

/**
 * Created by android on 16-7-13.
 */
public class NewPhoneConnectedActivity extends BaseActivity{
    public static final String ARGS_SSID = "args_ssid";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_phone_connected);
        ButterKnife.bind(this);
        String ssid = getIntent().getStringExtra(ARGS_SSID);
        if(TextUtils.isEmpty(ssid)){
            finish();
            return;
        }
    }
}
