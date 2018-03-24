package wei.mark.standout.ui;

import java.util.Locale;

public class TouchInfo {
    public double dist;
    public double firstHeight;
    public double firstWidth;
    public int firstX;
    public int firstY;
    public int lastX;
    public int lastY;
    public boolean moving;
    public float ratio;
    public double scale;

    public String toString() {
        return String.format(Locale.US, "WindowTouchInfo { firstX=%d, firstY=%d,lastX=%d, lastY=%d, firstWidth=%d, firstHeight=%d }", new Object[]{Integer.valueOf(this.firstX), Integer.valueOf(this.firstY), Integer.valueOf(this.lastX), Integer.valueOf(this.lastY), Double.valueOf(this.firstWidth), Double.valueOf(this.firstHeight)});
    }
}
