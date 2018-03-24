package net.majorkernelpanic.streaming.exceptions;

import java.io.IOException;

public class StorageUnavailableException extends IOException {
    private static final long serialVersionUID = -7537890350373995089L;

    public StorageUnavailableException(String message) {
        super(message);
    }
}
