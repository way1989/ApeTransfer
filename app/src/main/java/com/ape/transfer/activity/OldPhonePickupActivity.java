package com.ape.transfer.activity;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ape.transfer.R;
import com.ape.transfer.util.TDevice;
import com.ape.transfer.widget.MobileDataWarningContainer;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by android on 16-7-13.
 */
public class OldPhonePickupActivity extends BaseActivity {
    @BindView(R.id.mobile_data_warning)
    MobileDataWarningContainer mobileDataWarning;
    @BindView(R.id.rv_data_category)
    RecyclerView rvDataCategory;
    @BindView(R.id.storageView)
    TextView storageView;
    @BindView(R.id.btn_sure)
    Button btnSure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_phone_pickup);
        ButterKnife.bind(this);
        if(TDevice.hasInternet()){
            mobileDataWarning.setVisibility(View.VISIBLE);
        }
    }
}
