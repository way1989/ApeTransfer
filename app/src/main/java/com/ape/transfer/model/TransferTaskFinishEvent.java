package com.ape.transfer.model;

/**
 * Created by android on 16-10-31.
 */

public class TransferTaskFinishEvent {
    private int direction;

    public TransferTaskFinishEvent(int direction) {
        this.direction = direction;
    }


    public int getDirection() {
        return direction;
    }
}
