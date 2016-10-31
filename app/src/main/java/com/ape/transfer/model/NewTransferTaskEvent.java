package com.ape.transfer.model;

/**
 * Created by android on 16-10-31.
 */

public class NewTransferTaskEvent {
    private int direction;

    public NewTransferTaskEvent(int direction) {
        this.direction = direction;
    }


    public int getDirection() {
        return direction;
    }
}
