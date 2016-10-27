package com.ape.transfer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.ape.transfer.R;
import com.ape.transfer.model.ApStatusEvent;
import com.ape.transfer.service.WifiApService;
import com.ape.transfer.util.RxBus;
import com.trello.rxlifecycle.ActivityEvent;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static com.ape.transfer.service.WifiApService.ARG_SSID;

public abstract class ApBaseActivity extends BaseActivity {
    private static final String TAG = "ApBaseActivity";
    private boolean isOpeningWifiAp;

    @Override
    public void onBackPressed() {
        if (isOpeningWifiAp) {
            Toast.makeText(getApplicationContext(), R.string.waiting_creating_ap, Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }

    protected abstract String getSSID();

    protected abstract boolean shouldCloseWifiAp();

    protected void onWifiApStatusChanged(ApStatusEvent status) {
        isOpeningWifiAp = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.getInstance().toObservable(ApStatusEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<ApStatusEvent>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Action1<ApStatusEvent>() {
                    @Override
                    public void call(ApStatusEvent event) {
                        //do some thing
                        onWifiApStatusChanged(event);
                    }
                });
    }

    protected void startWifiAp() {
        isOpeningWifiAp = true;
        startService();
    }

    protected void stopWifiAp() {
        stopService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (shouldCloseWifiAp())
            stopWifiAp();
    }

    private void startService() {
        Intent intent = new Intent(this, WifiApService.class);
        intent.putExtra(ARG_SSID, getSSID());
        startService(intent);
    }

    private void stopService() {
        this.stopService(new Intent(this, WifiApService.class));
    }

}
