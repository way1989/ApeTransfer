package com.ape.emoji.images.common;

import java.io.IOException;

/**
 * Exception for image saving
 */
public class ImageSaveException extends IOException {
    private static final long serialVersionUID = -7567383722913326338L;

    public ImageSaveException() {
    }

    public ImageSaveException(String detailMessage) {
        super(detailMessage);
    }

    public ImageSaveException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImageSaveException(Throwable cause) {
        super(cause);
    }
}
