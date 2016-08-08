package com.ape.transfer.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.ape.transfer.R;
import com.ape.transfer.service.TransferService;
import com.ape.transfer.service.TransferServiceUtil;

/**
 * Created by kui.xie on 16-8-5.
 */
public abstract class BaseTransferActivity extends ApBaseActivity implements TransferServiceUtil.Callback,
        TransferService.Callback {
    protected TransferService.P2PBinder mTransferService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransferServiceUtil.getInstance().setCallback(this);
        TransferServiceUtil.getInstance().bindTransferService();
    }

    protected void startP2P() {
        if (mTransferService != null && !mTransferService.isP2PRunning())
            mTransferService.startP2P();
    }

    @Override
    public void onBackPressed() {
        if ((mWifiApService != null && mWifiApService.isWifiApEnabled())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.connect_dialog_title).setMessage(R.string.transfer_discontent)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mTransferService != null && !mTransferService.isEmpty()) {
                                if (mWifiApService.isWifiApEnabled()) {
                                    mTransferService.sendOffLine();
                                }
                                mTransferService.stopP2P();
                                TransferServiceUtil.getInstance().unbindTransferService();
                                TransferServiceUtil.getInstance().stopTransferService();
                            }
                            stopWifiAp();
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null).show();
            return;
        }

        super.onBackPressed();
    }
}
