package com.ape.emoji.images.common;

import java.io.IOException;

/**
 * Exception while image loading
 */
public class ImageLoadException extends IOException {
    private static final long serialVersionUID = -4839501562683274911L;

    public ImageLoadException() {
    }

    public ImageLoadException(String detailMessage) {
        super(detailMessage);
    }

    public ImageLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImageLoadException(Throwable cause) {
        super(cause);
    }
}
