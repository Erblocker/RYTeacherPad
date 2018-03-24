package net.majorkernelpanic.streaming.exceptions;

public class CameraInUseException extends RuntimeException {
    private static final long serialVersionUID = -1866132102949435675L;

    public CameraInUseException(String message) {
        super(message);
    }
}
