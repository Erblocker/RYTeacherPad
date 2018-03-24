package net.sourceforge.opencamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class TakePhoto extends Activity {
    private static final String TAG = "TakePhoto";
    public static final String TAKE_PHOTO = "net.sourceforge.opencamera.TAKE_PHOTO";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(335544320);
        intent.putExtra(TAKE_PHOTO, true);
        startActivity(intent);
        finish();
    }

    protected void onResume() {
        super.onResume();
    }
}
