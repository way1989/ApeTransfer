package com.ape.transfer.activity;

import android.os.Bundle;

import com.ape.transfer.R;
import com.ape.transfer.p2p.beans.Peer;

/**
 * Created by android on 16-7-13.
 */
public class OldPhoneExchangeActivity extends BaseActivity {
    private Peer mPeer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPeer = (Peer) getIntent().getSerializableExtra("neighbor");
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_old_phone_exchange;
    }
}
