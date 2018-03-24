package javazoom.jl.decoder;

public interface Control {
    double getPosition();

    boolean isPlaying();

    boolean isRandomAccess();

    void pause();

    void setPosition(double d);

    void start();

    void stop();
}
