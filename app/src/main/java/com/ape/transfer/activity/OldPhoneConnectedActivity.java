package com.ape.transfer.activity;

import android.os.Bundle;

import com.ape.transfer.R;

import butterknife.ButterKnife;

/**
 * Created by android on 16-7-13.
 */
public class OldPhoneConnectedActivity extends BaseActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_phone_connected);
        ButterKnife.bind(this);
    }
}