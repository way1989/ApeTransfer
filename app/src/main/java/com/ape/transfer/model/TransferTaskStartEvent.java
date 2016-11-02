package com.ape.transfer.model;

/**
 * Created by android on 16-10-31.
 */

public class TransferTaskStartEvent {
    private int direction;

    public TransferTaskStartEvent(int direction) {
        this.direction = direction;
    }


    public int getDirection() {
        return direction;
    }
}
