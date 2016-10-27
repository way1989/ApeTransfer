package com.ape.transfer.model;


/**
 * Created by way on 2016/10/27.
 */

public class ApStatusEvent {
    private String ssid;
    private int status;

    public ApStatusEvent(String ssid, int status) {
        this.ssid = ssid;
        this.status = status;
    }

    public String getSsid() {
        return ssid;
    }

    public int getStatus() {
        return status;
    }
}
