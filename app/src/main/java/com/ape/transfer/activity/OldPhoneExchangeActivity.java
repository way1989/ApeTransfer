package com.ape.transfer.activity;

import android.os.Bundle;

import com.ape.transfer.R;
import com.ape.transfer.p2p.p2pentity.P2PNeighbor;

import butterknife.ButterKnife;

/**
 * Created by android on 16-7-13.
 */
public class OldPhoneExchangeActivity extends BaseActivity{
    private P2PNeighbor mP2PNeighbor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_phone_exchange);
        ButterKnife.bind(this);
        mP2PNeighbor = (P2PNeighbor) getIntent().getSerializableExtra("neighbor");
    }
}
