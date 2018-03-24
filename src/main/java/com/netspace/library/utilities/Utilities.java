package com.netspace.library.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.UserManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import com.eclipsesource.v8.Platform;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.error.ErrorCode;
import com.netspace.library.fragment.UserHonourFragment;
import com.netspace.library.interfaces.IDownloadStatus;
import com.netspace.library.parser.ServerConfigurationParser.Subject;
import com.netspace.library.servers.NanoHTTPD;
import com.netspace.library.service.ScreenRecorderService;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.ui.UI;
import com.netspace.pad.library.R;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;
import com.xsj.crasheye.Crasheye;
import eu.janmuller.android.simplecropimage.CropImage;
import io.vov.vitamio.provider.MediaStore.Video.VideoColumns;
import io.vov.vitamio.widget.VideoView;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.sqlcipher.database.SQLiteDatabase;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

public class Utilities {
    private static /* synthetic */ int[] $SWITCH_TABLE$android$graphics$Bitmap$Config = null;
    private static final String HEX = "0123456789ABCDEF";
    private static final String TAG = "Utilities";
    private static String[][] htmlEscape;
    private static Gson mGson = null;
    private static long mLastClickTime = 0;
    private static boolean mbMobileWarn = false;
    private static HashMap<Integer, Integer> mmapThemeCustomColor = new HashMap();
    private static long mnLastBeepStartTime = 0;

    /* renamed from: com.netspace.library.utilities.Utilities$1 */
    class AnonymousClass1 implements OnClickListener {
        private final /* synthetic */ Runnable val$onGoRunnable;

        AnonymousClass1(Runnable runnable) {
            this.val$onGoRunnable = runnable;
        }

        public void onClick(DialogInterface dialog, int which) {
            Utilities.mbMobileWarn = true;
            this.val$onGoRunnable.run();
        }
    }

    /* renamed from: com.netspace.library.utilities.Utilities$2 */
    class AnonymousClass2 implements Runnable {
        private final /* synthetic */ Context val$finalContext;
        private final /* synthetic */ String val$szFinalMessage;

        AnonymousClass2(Context context, String str) {
            this.val$finalContext = context;
            this.val$szFinalMessage = str;
        }

        public void run() {
            Toast.makeText(this.val$finalContext, this.val$szFinalMessage, 0).show();
        }
    }

    /* renamed from: com.netspace.library.utilities.Utilities$3 */
    class AnonymousClass3 implements Runnable {
        private final /* synthetic */ int val$duration;
        private final /* synthetic */ Context val$finalContext;
        private final /* synthetic */ String val$szFinalMessage;

        AnonymousClass3(Context context, String str, int i) {
            this.val$finalContext = context;
            this.val$szFinalMessage = str;
            this.val$duration = i;
        }

        public void run() {
            Toast.makeText(this.val$finalContext, this.val$szFinalMessage, this.val$duration).show();
        }
    }

    /* renamed from: com.netspace.library.utilities.Utilities$4 */
    class AnonymousClass4 implements Runnable {
        private final /* synthetic */ Context val$finalContext;
        private final /* synthetic */ String val$szFinalMessage;
        private final /* synthetic */ String val$szFinalTitle;

        AnonymousClass4(Context context, String str, String str2) {
            this.val$finalContext = context;
            this.val$szFinalTitle = str;
            this.val$szFinalMessage = str2;
        }

        public void run() {
            try {
                new Builder(this.val$finalContext).setTitle(this.val$szFinalTitle).setMessage(this.val$szFinalMessage).setPositiveButton("确定", null).show();
            } catch (BadTokenException e) {
                Log.e(Utilities.TAG, "showAlertMessage failed with BadTokenException. Will try UI.getCurrentActivity.");
                AlertDialog alert = new Builder(this.val$finalContext).setTitle(this.val$szFinalTitle).setMessage(this.val$szFinalMessage).setPositiveButton("确定", null).create();
                alert.getWindow().setType(2003);
                alert.show();
            }
        }
    }

    /* renamed from: com.netspace.library.utilities.Utilities$5 */
    class AnonymousClass5 implements Runnable {
        private final /* synthetic */ Context val$finalContext;
        private final /* synthetic */ OnClickListener val$onClickListener;
        private final /* synthetic */ String val$szFinalMessage;
        private final /* synthetic */ String val$szFinalTitle;

        AnonymousClass5(Context context, String str, String str2, OnClickListener onClickListener) {
            this.val$finalContext = context;
            this.val$szFinalTitle = str;
            this.val$szFinalMessage = str2;
            this.val$onClickListener = onClickListener;
        }

        public void run() {
            try {
                new Builder(this.val$finalContext).setTitle(this.val$szFinalTitle).setMessage(this.val$szFinalMessage).setPositiveButton("确定", this.val$onClickListener).show();
            } catch (BadTokenException e) {
                Log.e(Utilities.TAG, "showAlertMessage failed with BadTokenException. Will try UI.getCurrentActivity()");
                AlertDialog alert = new Builder(this.val$finalContext).setTitle(this.val$szFinalTitle).setMessage(this.val$szFinalMessage).setPositiveButton("确定", this.val$onClickListener).create();
                alert.getWindow().setType(2003);
                alert.show();
            }
        }
    }

    /* renamed from: com.netspace.library.utilities.Utilities$6 */
    class AnonymousClass6 implements Runnable {
        private final /* synthetic */ ContextThemeWrapper val$finalContext;
        private final /* synthetic */ OnClickListener val$onClickNoListener;
        private final /* synthetic */ OnClickListener val$onClickYesListener;
        private final /* synthetic */ String val$szFinalMessage;
        private final /* synthetic */ String val$szFinalTitle;

        AnonymousClass6(ContextThemeWrapper contextThemeWrapper, String str, String str2, OnClickListener onClickListener, OnClickListener onClickListener2) {
            this.val$finalContext = contextThemeWrapper;
            this.val$szFinalTitle = str;
            this.val$szFinalMessage = str2;
            this.val$onClickYesListener = onClickListener;
            this.val$onClickNoListener = onClickListener2;
        }

        public void run() {
            try {
                new Builder(this.val$finalContext).setTitle(this.val$szFinalTitle).setMessage(this.val$szFinalMessage).setPositiveButton("是", this.val$onClickYesListener).setNegativeButton("否", this.val$onClickNoListener).show();
            } catch (BadTokenException e) {
                Log.e(Utilities.TAG, "showAlertMessage failed with BadTokenException. Will try UI.getCurrentActivity()");
                AlertDialog alert = new Builder(this.val$finalContext).setTitle(this.val$szFinalTitle).setMessage(this.val$szFinalMessage).setPositiveButton("是", this.val$onClickYesListener).setNegativeButton("否", this.val$onClickNoListener).create();
                alert.getWindow().setType(2003);
                alert.show();
            }
        }
    }

    /* renamed from: com.netspace.library.utilities.Utilities$7 */
    class AnonymousClass7 implements Runnable {
        private final /* synthetic */ Context val$finalContext;
        private final /* synthetic */ String val$szFinalMessage;
        private final /* synthetic */ String val$szFinalTitle;

        AnonymousClass7(Context context, String str, String str2) {
            this.val$finalContext = context;
            this.val$szFinalTitle = str;
            this.val$szFinalMessage = str2;
        }

        public void run() {
            View eulaLayout = LayoutInflater.from(this.val$finalContext).inflate(R.layout.checkbox, null);
            final CheckBox dontShowAgain = (CheckBox) eulaLayout.findViewById(R.id.skip);
            Builder alertDialog = new Builder(this.val$finalContext);
            alertDialog.setTitle(this.val$szFinalTitle).setMessage(this.val$szFinalMessage);
            alertDialog.setView(eulaLayout);
            final Context context = this.val$finalContext;
            final String str = this.val$szFinalTitle;
            alertDialog.setPositiveButton("确定", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (dontShowAgain.isChecked()) {
                        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                        editor.putBoolean("AlertDialog_" + str + "_NeverShow", true);
                        editor.commit();
                    }
                }
            });
            alertDialog.show();
        }
    }

    static /* synthetic */ int[] $SWITCH_TABLE$android$graphics$Bitmap$Config() {
        int[] iArr = $SWITCH_TABLE$android$graphics$Bitmap$Config;
        if (iArr == null) {
            iArr = new int[Config.values().length];
            try {
                iArr[Config.ALPHA_8.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Config.ARGB_4444.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Config.ARGB_8888.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[Config.RGB_565.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            $SWITCH_TABLE$android$graphics$Bitmap$Config = iArr;
        }
        return iArr;
    }

    static {
        r0 = new String[8][];
        r0[0] = new String[]{"&lt;", "<"};
        r0[1] = new String[]{"&gt;", ">"};
        r0[2] = new String[]{"&amp;", "&"};
        r0[3] = new String[]{"&quot;", "\""};
        r0[4] = new String[]{"&nbsp;", " "};
        r0[5] = new String[]{"&copy;", "©"};
        r0[6] = new String[]{"&reg;", "®"};
        r0[7] = new String[]{"&euro;", "₠"};
        htmlEscape = r0;
    }

    public static final String md5(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();
            StringBuffer hexString = new StringBuffer();
            for (byte b : messageDigest) {
                String h = Integer.toHexString(b & 255);
                while (h.length() < 2) {
                    h = "0" + h;
                }
                hexString.append(h);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void writeObjectToIntent(Object object, Intent intent) {
        if (mGson == null) {
            mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        }
        intent.putExtra(object.getClass().getSimpleName(), mGson.toJson(object));
    }

    public static Object readObjectFromIntent(Object object, Intent intent) {
        if (mGson == null) {
            mGson = new GsonBuilder().excludeFieldsWithModifiers(128).create();
        }
        if (intent.getExtras() == null) {
            return object;
        }
        String szJsonData = intent.getStringExtra(object.getClass().getSimpleName());
        if (szJsonData != null) {
            return mGson.fromJson(szJsonData, object.getClass());
        }
        return object;
    }

    public static String getFileMD5(File targetFile) {
        String output = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            try {
                InputStream is = new FileInputStream(targetFile);
                byte[] buffer = new byte[8192];
                while (true) {
                    try {
                        int read = is.read(buffer);
                        if (read <= 0) {
                            break;
                        }
                        digest.update(buffer, 0, read);
                    } catch (IOException e) {
                        throw new RuntimeException("Unable to process file for MD5", e);
                    } catch (Throwable th) {
                        try {
                            is.close();
                        } catch (IOException e2) {
                            Log.e(TAG, "Exception on closing MD5 input stream", e2);
                        }
                    }
                }
                output = String.format("%32s", new Object[]{new BigInteger(1, digest.digest()).toString(16)}).replace(' ', '0');
                try {
                    is.close();
                } catch (IOException e22) {
                    Log.e(TAG, "Exception on closing MD5 input stream", e22);
                }
            } catch (FileNotFoundException e3) {
                Log.e(TAG, "Exception while getting FileInputStream", e3);
            }
        } catch (NoSuchAlgorithmException e4) {
            Log.e(TAG, "Exception while getting digest", e4);
        }
        return output;
    }

    public static String unescape(String s) {
        return unescape(s, 0);
    }

    public static void wakeupScreen() {
        WakeLock wakeLock = ((PowerManager) MyiBaseApplication.getBaseAppContext().getSystemService("power")).newWakeLock(805306378, "Utilities.wakeupScreen");
        wakeLock.acquire();
        wakeLock.release();
    }

    public static void ensureScreenOn() {
        if (!isScreenOn()) {
            wakeupScreen();
        }
    }

    public static void ensureScreenOn(boolean bDisableKeyguard) {
        if (!isScreenOn()) {
            wakeupScreen();
            if (bDisableKeyguard) {
                disableScreenLock();
            }
        }
    }

    public static void disableScreenLock() {
        KeyguardLock keyguardLock = ((KeyguardManager) MyiBaseApplication.getBaseAppContext().getSystemService("keyguard")).newKeyguardLock("Utilities.disableScreenLock");
        if (keyguardLock != null) {
            keyguardLock.disableKeyguard();
        }
    }

    public static String dateToString(Date dtDate) {
        if (dtDate == null) {
            return null;
        }
        return DateFormat.format("yyyy-MM-dd HH:mm:ss", dtDate).toString();
    }

    public static String unescape(String s, int start) {
        int i = s.indexOf("&", start);
        start = i + 1;
        if (i <= -1) {
            return s;
        }
        int j = s.indexOf(";", i);
        if (j <= i) {
            return s;
        }
        String temp = s.substring(i, j + 1);
        int k = 0;
        while (k < htmlEscape.length && !htmlEscape[k][0].equals(temp)) {
            k++;
        }
        if (k < htmlEscape.length) {
            return unescape(s.substring(0, i) + htmlEscape[k][1] + s.substring(j + 1), i);
        }
        return s;
    }

    public static String toHex(String txt) {
        return toHex(txt.getBytes());
    }

    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }

    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = Integer.valueOf(hexString.substring(i * 2, (i * 2) + 2), 16).byteValue();
        }
        return result;
    }

    public static boolean isScreenOn() {
        PowerManager pm = (PowerManager) MyiBaseApplication.getBaseAppContext().getSystemService("power");
        if (pm == null) {
            return false;
        }
        if (VERSION.SDK_INT >= 21) {
            return pm.isInteractive();
        }
        return pm.isScreenOn();
    }

    public static String toHex(byte[] buf) {
        if (buf == null) {
            return "";
        }
        StringBuffer result = new StringBuffer(buf.length * 2);
        for (byte appendHex : buf) {
            appendHex(result, appendHex);
        }
        return result.toString();
    }

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 15)).append(HEX.charAt(b & 15));
    }

    public static void showDialog(BaseActivity activity, DialogFragment dialog, String szName) {
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        if (activity.getSupportFragmentManager().findFragmentByTag(szName) == null) {
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            dialog.show(ft, szName);
        }
    }

    public static void setDialogToScreenSize(DialogFragment dialog, int nScreenPercent) {
        if (dialog.getDialog() != null) {
            int width = dialog.getDialog().getWindow().getDecorView().getWidth();
            int height = dialog.getDialog().getWindow().getDecorView().getHeight();
            int nScreenWidth = getScreenWidth(MyiBaseApplication.getBaseAppContext());
            if (width < nScreenWidth / nScreenPercent) {
                dialog.getDialog().getWindow().setLayout(nScreenWidth / nScreenPercent, -1);
            }
        }
    }

    public static void setDialogToScreenSize(DialogFragment dialog, float fScreenPercent) {
        if (dialog.getDialog() != null) {
            int width = dialog.getDialog().getWindow().getDecorView().getWidth();
            int height = dialog.getDialog().getWindow().getDecorView().getHeight();
            dialog.getDialog().getWindow().setLayout((int) (((float) getScreenWidth(MyiBaseApplication.getBaseAppContext())) * fScreenPercent), -1);
        }
    }

    public static int getIntSettings(String szSettingName, int nDefaultValue) {
        SharedPreferences Settings = PreferenceManager.getDefaultSharedPreferences(MyiBaseApplication.getBaseAppContext());
        int i = nDefaultValue;
        try {
            return Settings.getInt(szSettingName, nDefaultValue);
        } catch (ClassCastException e) {
            try {
                return Integer.valueOf(Settings.getString(szSettingName, String.valueOf(nDefaultValue))).intValue();
            } catch (ClassCastException e2) {
                return nDefaultValue;
            }
        }
    }

    public static void clearApplicationData() {
        File appDir = new File(MyiBaseApplication.getBaseAppContext().getCacheDir().getParent());
        if (appDir.exists()) {
            for (String s : appDir.list()) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                }
            }
        }
    }

    public static void resetMobileWarn() {
        mbMobileWarn = false;
    }

    public static void checkAndWarnNetworkType(Runnable onGoRunnable) {
        NetworkInfo info = ((ConnectivityManager) MyiBaseApplication.getBaseAppContext().getSystemService("connectivity")).getActiveNetworkInfo();
        int netType = info.getType();
        int netSubtype = info.getSubtype();
        if (netType == 1) {
            mbMobileWarn = false;
            onGoRunnable.run();
        } else if (netType != 0) {
            showAlertMessage(null, "无网络", "当前没有有效的互联网连接。");
        } else if (mbMobileWarn) {
            onGoRunnable.run();
        } else {
            showAlertMessage(null, "数据网络", "当前正在使用数据网络，继续观看会消耗数据流量，这将产生费用。是否继续？", new AnonymousClass1(onGoRunnable), null);
        }
    }

    public static String getMacAddr() {
        try {
            NetworkInterface nif;
            Iterator it = Collections.list(NetworkInterface.getNetworkInterfaces()).iterator();
            do {
                if (!it.hasNext()) {
                    return "02:00:00:00:00:00";
                }
                nif = (NetworkInterface) it.next();
            } while (!nif.getName().equalsIgnoreCase("wlan0"));
            byte[] macBytes = nif.getHardwareAddress();
            if (macBytes == null) {
                return "";
            }
            StringBuilder res1 = new StringBuilder();
            int length = macBytes.length;
            for (int i = 0; i < length; i++) {
                res1.append(String.format("%02X:", new Object[]{Byte.valueOf(macBytes[i])}));
            }
            if (res1.length() > 0) {
                res1.deleteCharAt(res1.length() - 1);
            }
            return res1.toString();
        } catch (Exception e) {
        }
    }

    public static String getBatteryLevel() {
        Intent BatteryIntent = MyiBaseApplication.getBaseAppContext().registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (BatteryIntent == null) {
            return "--%";
        }
        int level = BatteryIntent.getIntExtra("level", 0);
        return new StringBuilder(String.valueOf(String.valueOf((level * 100) / BatteryIntent.getIntExtra(CropImage.SCALE, 100)))).append("%").toString();
    }

    public static String getTempFileName(String szExtName) {
        String szFileName = "";
        try {
            szFileName = File.createTempFile("tmpfile", "." + szExtName, MyiBaseApplication.getBaseAppContext().getExternalCacheDir()).getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return szFileName;
    }

    public static String getScreenStatus() {
        if (!((PowerManager) MyiBaseApplication.getBaseAppContext().getSystemService("power")).isScreenOn()) {
            return " screenoff ";
        }
        if (Boolean.valueOf(((KeyguardManager) MyiBaseApplication.getBaseAppContext().getSystemService("keyguard")).inKeyguardRestrictedInputMode()).booleanValue()) {
            return " screenlockon ";
        }
        return " screenon ";
    }

    private static boolean checkSurfaceView(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                if (checkSurfaceView(group.getChildAt(i))) {
                    return true;
                }
            }
        }
        if ((view instanceof SurfaceView) || (view instanceof VideoView) || (view instanceof android.widget.VideoView)) {
            return true;
        }
        return false;
    }

    public static boolean captureScreen(String szTargetFileName) {
        boolean bCaptureSuccess = false;
        String szPictureFileName = szTargetFileName;
        if (null == null) {
            Activity Activity = UI.getCurrentActivity();
            if (Activity == null) {
                Activity = ScreenRecorderService.getActivity();
            }
            if (Activity != null && checkSurfaceView(Activity.findViewById(16908290))) {
                return false;
            }
            if (Activity != null) {
                View v2 = Activity.getWindow().getDecorView().getRootView();
                v2.setDrawingCacheEnabled(true);
                Bitmap bmp = Bitmap.createBitmap(v2.getDrawingCache());
                v2.setDrawingCacheEnabled(false);
                try {
                    FileOutputStream fos = new FileOutputStream(new File(szPictureFileName));
                    bmp.compress(CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                    bCaptureSuccess = true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return false;
                } catch (IOException e2) {
                    e2.printStackTrace();
                    return false;
                }
            }
        }
        if (true && !bCaptureSuccess) {
            boolean bFileOK = false;
            File targetFile = new File(new StringBuilder(String.valueOf(szPictureFileName)).append(".png").toString());
            if (executeCommand("screencap -p " + szPictureFileName + ".png", new StringBuilder(String.valueOf(szPictureFileName)).append(".png").toString()) && targetFile.length() > 0) {
                bFileOK = true;
            }
            if (bFileOK) {
                Bitmap myBitmap = BitmapFactory.decodeFile(new StringBuilder(String.valueOf(szPictureFileName)).append(".png").toString());
                try {
                    BufferedOutputStream bufOutStr = new BufferedOutputStream(new FileOutputStream(szPictureFileName));
                    myBitmap.compress(CompressFormat.JPEG, 100, bufOutStr);
                    bufOutStr.flush();
                    bufOutStr.close();
                    targetFile.delete();
                    bCaptureSuccess = true;
                } catch (FileNotFoundException exception) {
                    exception.printStackTrace();
                    bCaptureSuccess = false;
                } catch (IOException exception2) {
                    exception2.printStackTrace();
                    bCaptureSuccess = false;
                }
            }
        }
        if (bCaptureSuccess) {
            return true;
        }
        return false;
    }

    private static boolean executeCommand(String szCommand, String szFileName) {
        CommandCapture command;
        InterruptedException e;
        TimeoutException e2;
        RootDeniedException e3;
        IOException e4;
        CommandCapture commandCapture;
        boolean bResult = false;
        RootTools.debugMode = false;
        if (!RootTools.isRootAvailable() || !RootTools.isAccessGiven()) {
            return false;
        }
        if (szCommand.split(";").length == 1) {
            command = new CommandCapture(0, "busybox mount -o remount,rw /system", szCommand);
        } else {
            command = new CommandCapture(0, "busybox mount -o remount,rw /system", szCommand.split(";")[0], szCommand.split(";")[1]);
        }
        try {
            if (!new File("/system/bin/screencap").exists()) {
                return false;
            }
            if (true) {
                RootTools.getShell(true).add(command).waitForFinish();
                bResult = true;
                try {
                    RootTools.getShell(true).add(new CommandCapture(0, "busybox mount -o remount,rw /system", "chmod 777 " + szFileName)).waitForFinish();
                } catch (InterruptedException e5) {
                    e = e5;
                    e.printStackTrace();
                    return bResult;
                } catch (TimeoutException e6) {
                    e2 = e6;
                    e2.printStackTrace();
                    return bResult;
                } catch (RootDeniedException e7) {
                    e3 = e7;
                    e3.printStackTrace();
                    return bResult;
                } catch (IOException e8) {
                    e4 = e8;
                    e4.printStackTrace();
                    return bResult;
                }
            }
            return bResult;
        } catch (InterruptedException e9) {
            e = e9;
            commandCapture = command;
            e.printStackTrace();
            return bResult;
        } catch (TimeoutException e10) {
            e2 = e10;
            commandCapture = command;
            e2.printStackTrace();
            return bResult;
        } catch (RootDeniedException e11) {
            e3 = e11;
            commandCapture = command;
            e3.printStackTrace();
            return bResult;
        } catch (IOException e12) {
            e4 = e12;
            commandCapture = command;
            e4.printStackTrace();
            return bResult;
        }
    }

    public static boolean checkIPValid(String szIP) {
        if (Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$").matcher(szIP).find()) {
            return true;
        }
        return false;
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String file : children) {
                if (!deleteDir(new File(dir, file))) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static String downloadFileToLocalFile(String szURL, File outputFile, IDownloadStatus callBack) {
        if (szURL == null) {
            throw new IllegalArgumentException("szURL cannot be null.");
        }
        HttpGet httpGet = new HttpGet(szURL);
        boolean bGzip = false;
        if (callBack != null && callBack.isCancelled()) {
            return null;
        }
        try {
            BasicHttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
            HttpConnectionParams.setSoTimeout(httpParams, 20000);
            HttpClient httpClient = null;
            if (MyiBaseApplication.isUseSSL()) {
                HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
                DefaultHttpClient client2 = new DefaultHttpClient();
                SchemeRegistry registry = new SchemeRegistry();
                SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
                socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
                registry.register(new Scheme(HttpHost.DEFAULT_SCHEME_NAME, PlainSocketFactory.getSocketFactory(), 80));
                registry.register(new Scheme("https", socketFactory, 443));
                httpClient = new DefaultHttpClient(new SingleClientConnManager(client2.getParams(), registry), httpParams);
                HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
            }
            if (httpClient == null) {
                httpClient = new DefaultHttpClient(httpParams);
            }
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                if (callBack != null) {
                    callBack.onBeginDownload();
                }
                long nTotalLength = httpResponse.getEntity().getContentLength();
                InputStream inputStream = httpResponse.getEntity().getContent();
                if (httpResponse.containsHeader(HTTP.CONTENT_ENCODING) && httpResponse.getFirstHeader(HTTP.CONTENT_ENCODING).getValue().indexOf("gzip") != -1) {
                    bGzip = true;
                    inputStream = new GZIPInputStream(inputStream);
                }
                if (outputFile != null) {
                    byte[] Buffer = new byte[5242880];
                    int nCurrentPos = 0;
                    FileOutputStream OutputStream = new FileOutputStream(outputFile.getPath());
                    do {
                        int nReadCount = inputStream.read(Buffer);
                        if (nReadCount == -1) {
                            break;
                        }
                        OutputStream.write(Buffer, 0, nReadCount);
                        nCurrentPos += nReadCount;
                        if (callBack != null) {
                            callBack.onDownloadProgress((long) nCurrentPos, nTotalLength);
                            callBack.onProgressFileBlock(Buffer, (long) nReadCount);
                        }
                    } while (!callBack.isCancelled());
                    inputStream.close();
                    OutputStream.close();
                    if (!callBack.isCancelled()) {
                        String szFinalFileName;
                        if (bGzip) {
                            szFinalFileName = outputFile.getPath();
                            if (callBack != null) {
                                callBack.onDownloadComplete(outputFile);
                            }
                            return szFinalFileName;
                        } else if (((long) nCurrentPos) >= nTotalLength) {
                            szFinalFileName = outputFile.getPath();
                            if (callBack != null) {
                                callBack.onDownloadComplete(outputFile);
                            }
                            return szFinalFileName;
                        } else {
                            if (callBack != null) {
                                callBack.onDownloadError(ErrorCode.ERROR_FILE_DOWNLOADINCOMPLETE, ErrorCode.getErrorMessage(ErrorCode.ERROR_FILE_DOWNLOADINCOMPLETE));
                            }
                            return null;
                        }
                    }
                }
                BufferedReader textBuffer = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                String szOneLine = textBuffer.readLine();
                while (szOneLine != null) {
                    if (callBack != null) {
                        callBack.onProgressLineContent(szOneLine);
                    }
                    szOneLine = textBuffer.readLine();
                    if (callBack.isCancelled()) {
                        break;
                    }
                }
                if (!(callBack.isCancelled() || callBack == null)) {
                    callBack.onDownloadComplete(outputFile);
                }
                inputStream.close();
                return "";
            }
            if (callBack != null) {
                callBack.onDownloadError(2, "服务器端返回错误代码" + httpResponse.getStatusLine().getStatusCode());
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            if (callBack != null) {
                callBack.onDownloadError(28, e.getMessage());
            }
        }
    }

    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir, dir);
            }
        } catch (Exception e) {
        }
    }

    public static void trimExtCache(Context context) {
        try {
            File dir = context.getExternalCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir, dir);
            }
        } catch (Exception e) {
        }
    }

    public static void trimExtCacheTempFile(Context context) {
        String[] arrTempFilePrefix = new String[]{"audio_", "tmp", "camera_", "cache_"};
        File dir = context.getExternalCacheDir();
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                String szFileName = children[i];
                for (String startsWith : arrTempFilePrefix) {
                    if (szFileName.startsWith(startsWith)) {
                        if (new File(dir, children[i]).delete()) {
                            Log.i(TAG, "Temp file " + children[i] + " is deleted.");
                        } else {
                            Log.e(TAG, "Temp file " + children[i] + " delete failed.");
                        }
                    }
                }
            }
        }
    }

    public static boolean deleteDir(File dir, File CacheDir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String file : children) {
                if (!deleteDir(new File(dir, file), CacheDir)) {
                    return false;
                }
            }
        }
        if (dir.getAbsolutePath().equalsIgnoreCase(CacheDir.getAbsolutePath())) {
            return false;
        }
        return dir.delete();
    }

    public static double getFolderSize(File directory) {
        if (directory == null) {
            return 0.0d;
        }
        double length = 0.0d;
        if (directory.listFiles() == null) {
            return 0.0d;
        }
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                length += (double) file.length();
            } else {
                length += getFolderSize(file);
            }
        }
        return length;
    }

    public static String formatFileSize(double size) {
        double b = size;
        double k = size / 1024.0d;
        double m = (size / 1024.0d) / 1024.0d;
        double g = ((size / 1024.0d) / 1024.0d) / 1024.0d;
        double t = (((size / 1024.0d) / 1024.0d) / 1024.0d) / 1024.0d;
        DecimalFormat dec = new DecimalFormat("0.00");
        if (t > 1.0d) {
            return dec.format(t).concat(" TB");
        }
        if (g > 1.0d) {
            return dec.format(g).concat(" GB");
        }
        if (m > 1.0d) {
            return dec.format(m).concat(" MB");
        }
        if (k > 1.0d) {
            return dec.format(k).concat(" KB");
        }
        return dec.format(b).concat(" Bytes");
    }

    public static String getExternalFreeSpace() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        return formatFileSize(((double) stat.getAvailableBlocks()) * ((double) stat.getBlockSize()));
    }

    public static boolean checkClickTime(long nMS) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < nMS) {
            return false;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        return true;
    }

    public static String msToString(int nMS) {
        if (nMS <= 0) {
            return "00:00:00";
        }
        int nSeconds = nMS / 1000;
        nSeconds -= (nSeconds / 3600) * 3600;
        nSeconds -= (nSeconds / 60) * 60;
        return String.format("%02d:%02d:%02d", new Object[]{Integer.valueOf(nHour), Integer.valueOf(nMinutes), Integer.valueOf(nSeconds)});
    }

    public static boolean deleteFile(String szFilePath) {
        if (szFilePath == null) {
            Log.e(TAG, "deleteFile given null filename.");
            return false;
        }
        File TempFile = new File(szFilePath);
        if (TempFile.exists()) {
            return TempFile.delete();
        }
        return false;
    }

    public static boolean isNetworkConnected() {
        return isNetworkConnected(MyiBaseApplication.getBaseAppContext());
    }

    public static boolean isNetworkConnected(Context Context) {
        NetworkInfo activeNetwork = ((ConnectivityManager) Context.getSystemService("connectivity")).getActiveNetworkInfo();
        boolean isConnected = false;
        if (activeNetwork != null) {
            isConnected = activeNetwork.isConnectedOrConnecting();
        }
        if (isConnected && activeNetwork.isConnected()) {
            return true;
        }
        return false;
    }

    public static String getFileName(String szFullPath) {
        if (szFullPath != null) {
            return new File(szFullPath).getName();
        }
        throw new NullPointerException("szFullPath can not be null.");
    }

    public static String getMimeType(Context context, Uri uri) {
        String extension;
        if (uri.getScheme().equals("content")) {
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    public static String getURLFileName(String szURL) {
        int nPos = szURL.lastIndexOf("/");
        if (nPos != -1) {
            return szURL.substring(nPos + 1);
        }
        return "";
    }

    public static String getUploadPath(Context context) {
        File Folder = new File(new StringBuilder(String.valueOf(context.getExternalCacheDir().getAbsolutePath())).append("/upload").toString());
        Folder.mkdir();
        return Folder.getAbsolutePath();
    }

    public static String getFileExtName(String szFullPath) {
        int nPos = szFullPath.lastIndexOf(".");
        if (nPos != -1) {
            return szFullPath.substring(nPos + 1);
        }
        return "";
    }

    public static boolean isInArray(ArrayList<String> arrArray, String szData) {
        for (int i = 0; i < arrArray.size(); i++) {
            if (((String) arrArray.get(i)).equals(szData)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInArray(String[] arrArray, String szData) {
        if (arrArray == null) {
            return false;
        }
        for (String OneData : arrArray) {
            if (OneData.equals(szData)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInArray(List<String> arrArray, String szData) {
        for (int i = 0; i < arrArray.size(); i++) {
            if (((String) arrArray.get(i)).equals(szData)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInArray(ArrayList<String> arrArray, String szData, Integer[] nIndex) {
        for (int i = 0; i < arrArray.size(); i++) {
            if (((String) arrArray.get(i)).equals(szData)) {
                nIndex[0] = Integer.valueOf(i);
                return true;
            }
        }
        return false;
    }

    public static boolean isInArray(String[] arrArray, String szData, Integer[] nIndex) {
        for (int i = 0; i < arrArray.length; i++) {
            if (arrArray[i].equals(szData)) {
                nIndex[0] = Integer.valueOf(i);
                return true;
            }
        }
        return false;
    }

    public static boolean isInArray(ArrayList<Integer> arrArray, int nData) {
        for (int i = 0; i < arrArray.size(); i++) {
            if (((Integer) arrArray.get(i)).intValue() == nData) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInArray(int[] arrArray, int nData) {
        for (int i : arrArray) {
            if (i == nData) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInArray(int[] arrArray, int nData, Integer[] index) {
        for (int i = 0; i < arrArray.length; i++) {
            if (arrArray[i] == nData) {
                index[0] = Integer.valueOf(i);
                return true;
            }
        }
        return false;
    }

    public static boolean isInArray(ArrayList<Integer> arrArray, int nData, Integer[] nIndex) {
        for (int i = 0; i < arrArray.size(); i++) {
            if (((Integer) arrArray.get(i)).intValue() == nData) {
                nIndex[0] = Integer.valueOf(i);
                return true;
            }
        }
        return false;
    }

    public static String getErrorMessage(int nErrorCode) {
        return ErrorCode.getErrorMessage(nErrorCode);
    }

    public static String getErrorMessage(int nErrorCode, String szDescription) {
        String szErrorDescription = ErrorCode.getErrorMessage(nErrorCode);
        if (szErrorDescription.equalsIgnoreCase("未知错误")) {
            return szDescription;
        }
        return new StringBuilder(String.valueOf(szErrorDescription)).append("，").append(szDescription).toString();
    }

    public static String trimToHtml(String szText) {
        try {
            szText = szText.replace("<p dir=\"ltr\">", "").replace("<p dir=ltr>", "").replace("</p>", "").replace("<p>", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return szText;
    }

    private static String replceLast(String yourString, String frist, String second) {
        StringBuilder b = new StringBuilder(yourString);
        b.replace(yourString.lastIndexOf(frist), yourString.lastIndexOf(frist) + frist.length(), second);
        return b.toString();
    }

    public static boolean isURLAccessable(String szURL) {
        HttpGet httpGet = new HttpGet(szURL);
        try {
            BasicHttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            HttpConnectionParams.setSoTimeout(httpParams, 10000);
            HttpClient client = new DefaultHttpClient(httpParams);
            HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
            DefaultHttpClient client2 = new DefaultHttpClient();
            SchemeRegistry registry = new SchemeRegistry();
            SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
            socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
            registry.register(new Scheme(HttpHost.DEFAULT_SCHEME_NAME, PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", socketFactory, 443));
            client = new DefaultHttpClient(new SingleClientConnManager(client2.getParams(), registry), httpParams);
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
            String szServer = null;
            Header[] arrServer = client.execute(httpGet).getHeaders(HTTP.SERVER_HEADER);
            if (arrServer.length > 0) {
                szServer = arrServer[0].getValue().toLowerCase();
            }
            if (szServer == null) {
                return false;
            }
            if (szServer.indexOf("iis") == -1 && szServer.indexOf("nginx") == -1 && szServer.indexOf("gsoap") == -1) {
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getWifiIP(Context context) {
        WifiInfo wifiInfo = ((WifiManager) context.getSystemService("wifi")).getConnectionInfo();
        String szResult = "";
        if (wifiInfo != null) {
            szResult = Formatter.formatIpAddress(wifiInfo.getIpAddress());
        } else {
            szResult = "";
        }
        return szResult;
    }

    public static String getWifiSSID(Context context) {
        WifiInfo wifiInfo = ((WifiManager) context.getSystemService("wifi")).getConnectionInfo();
        String szResult = "";
        if (wifiInfo != null) {
            szResult = wifiInfo.getSSID();
            if (szResult.startsWith("\"")) {
                szResult = szResult.substring(1);
            }
            if (szResult.endsWith("\"")) {
                szResult = szResult.substring(0, szResult.length() - 1);
            }
        } else {
            szResult = "";
        }
        return szResult;
    }

    public static Bitmap getBase64Bitmap(String szBase64) {
        Bitmap bitmap = null;
        if (szBase64 != null) {
            bitmap = null;
            try {
                byte[] BitmapData = Base64.decode(szBase64, 0);
                bitmap = BitmapFactory.decodeByteArray(BitmapData, 0, BitmapData.length);
                BitmapData = null;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public static String getBase64FileContent(String szFileName) {
        String encodedResult = null;
        try {
            encodedResult = Base64.encodeToString(readFile(szFileName), 0);
            byte[] fileContent = null;
            return encodedResult;
        } catch (IOException e) {
            e.printStackTrace();
            return encodedResult;
        }
    }

    public static byte[] intArrayToBytes(int[] values) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        for (int writeInt : values) {
            try {
                dos.writeInt(writeInt);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return baos.toByteArray();
    }

    public static boolean writeByteArrayToFile(byte[] data, String szFileName) {
        boolean bResult = false;
        byte[] buf = data;
        try {
            FileOutputStream out = new FileOutputStream(new File(szFileName));
            out.write(buf, 0, buf.length);
            out.flush();
            out.close();
            bResult = true;
            buf = null;
            return true;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return bResult;
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            return bResult;
        } catch (IOException e3) {
            e3.printStackTrace();
            return bResult;
        }
    }

    public static boolean writeBase64ToFile(String szBase64, String szFileName) {
        boolean bResult = false;
        try {
            byte[] buf = Base64.decode(szBase64, 0);
            FileOutputStream out = new FileOutputStream(new File(szFileName));
            out.write(buf, 0, buf.length);
            out.flush();
            out.close();
            bResult = true;
            buf = null;
            return true;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return bResult;
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            return bResult;
        } catch (IOException e3) {
            e3.printStackTrace();
            return bResult;
        }
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                reader.close();
                return sb.toString();
            }
            sb.append(line).append("\n");
        }
    }

    public static String readTextFile(String filePath) {
        FileNotFoundException e;
        Exception e2;
        String szResult = "";
        FileInputStream fin = null;
        try {
            FileInputStream fin2 = new FileInputStream(new File(filePath));
            try {
                szResult = convertStreamToString(fin2);
                fin2.close();
                fin = fin2;
            } catch (FileNotFoundException e3) {
                e = e3;
                fin = fin2;
                e.printStackTrace();
                if (fin != null) {
                    try {
                        fin.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
                return szResult;
            } catch (Exception e5) {
                e2 = e5;
                fin = fin2;
                e2.printStackTrace();
                if (fin != null) {
                    fin.close();
                }
                return szResult;
            }
        } catch (FileNotFoundException e6) {
            e = e6;
            e.printStackTrace();
            if (fin != null) {
                fin.close();
            }
            return szResult;
        } catch (Exception e7) {
            e2 = e7;
            e2.printStackTrace();
            if (fin != null) {
                fin.close();
            }
            return szResult;
        }
        if (fin != null) {
            fin.close();
        }
        return szResult;
    }

    public static byte[] readFile(String file) throws IOException {
        return readFile(new File(file));
    }

    public static byte[] readFile(File file) throws IOException {
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            long longlength = f.length();
            int length = (int) longlength;
            if (((long) length) != longlength) {
                throw new IOException("File size >= 2 GB");
            }
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }

    private static String getDefaultPassword() {
        try {
            return md5(new String(new byte[]{(byte) 109, (byte) 121, (byte) 105, (byte) 112, (byte) 97, (byte) 100, (byte) 49, (byte) 50, (byte) 51, (byte) 49, (byte) 50, (byte) 51, (byte) 49, (byte) 50, (byte) 51, (byte) 35, (byte) 64, (byte) 33, (byte) 35, (byte) 64, (byte) 33, (byte) 33, (byte) 64, (byte) 35, (byte) 36, (byte) 37, (byte) 97, (byte) 115, (byte) 100, (byte) 102, (byte) 108, (byte) 107, (byte) 106, (byte) 97, (byte) 115, (byte) 100, (byte) 108, (byte) 107, (byte) 102, (byte) 106, (byte) 32, (byte) 59}, "GB2312"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String readTextFileFromAssertPackage(Context context, String szFileName) {
        Exception e;
        Throwable th;
        String szPackageFileName = "package.dat";
        StringBuilder returnString = new StringBuilder();
        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;
        try {
            InputStream zipInputStream = new ZipInputStream(new ZipDecryptInputStream(context.getResources().getAssets().open(szPackageFileName, 1), getDefaultPassword()));
            ZipEntry ze;
            do {
                ze = zipInputStream.getNextEntry();
                if (ze == null) {
                    break;
                }
            } while (!ze.getName().equalsIgnoreCase(szFileName));
            fIn = zipInputStream;
            InputStreamReader isr2 = new InputStreamReader(fIn, "GBK");
            try {
                BufferedReader input2 = new BufferedReader(isr2);
                try {
                    String str = "";
                    while (true) {
                        str = input2.readLine();
                        if (str == null) {
                            break;
                        }
                        returnString.append(str);
                        returnString.append("\r\n");
                    }
                    input = input2;
                    isr = isr2;
                    if (isr != null) {
                        try {
                            isr.close();
                        } catch (Exception e2) {
                            e2.getMessage();
                        }
                    }
                    if (fIn != null) {
                        fIn.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                } catch (Exception e3) {
                    e = e3;
                    input = input2;
                    isr = isr2;
                    try {
                        e.getMessage();
                        if (isr != null) {
                            try {
                                isr.close();
                            } catch (Exception e22) {
                                e22.getMessage();
                            }
                        }
                        if (fIn != null) {
                            fIn.close();
                        }
                        if (input != null) {
                            input.close();
                        }
                        return returnString.toString();
                    } catch (Throwable th2) {
                        th = th2;
                        if (isr != null) {
                            try {
                                isr.close();
                            } catch (Exception e222) {
                                e222.getMessage();
                                throw th;
                            }
                        }
                        if (fIn != null) {
                            fIn.close();
                        }
                        if (input != null) {
                            input.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    input = input2;
                    isr = isr2;
                    if (isr != null) {
                        isr.close();
                    }
                    if (fIn != null) {
                        fIn.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e = e4;
                isr = isr2;
                e.getMessage();
                if (isr != null) {
                    isr.close();
                }
                if (fIn != null) {
                    fIn.close();
                }
                if (input != null) {
                    input.close();
                }
                return returnString.toString();
            } catch (Throwable th4) {
                th = th4;
                isr = isr2;
                if (isr != null) {
                    isr.close();
                }
                if (fIn != null) {
                    fIn.close();
                }
                if (input != null) {
                    input.close();
                }
                throw th;
            }
        } catch (Exception e5) {
            e = e5;
        }
        return returnString.toString();
    }

    public static String readTextFileFromAssert(Context context, String szFileName) {
        Exception e;
        Throwable th;
        StringBuilder returnString = new StringBuilder();
        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;
        try {
            fIn = context.getResources().getAssets().open(szFileName, 1);
            InputStreamReader isr2 = new InputStreamReader(fIn, "GBK");
            try {
                BufferedReader input2 = new BufferedReader(isr2);
                try {
                    String str = "";
                    while (true) {
                        str = input2.readLine();
                        if (str == null) {
                            break;
                        }
                        returnString.append(str);
                        returnString.append("\r\n");
                    }
                    if (isr2 != null) {
                        try {
                            isr2.close();
                        } catch (Exception e2) {
                            e2.getMessage();
                        }
                    }
                    if (fIn != null) {
                        fIn.close();
                    }
                    if (input2 != null) {
                        input2.close();
                        input = input2;
                        isr = isr2;
                        return returnString.toString();
                    }
                    input = input2;
                    isr = isr2;
                } catch (Exception e3) {
                    e = e3;
                    input = input2;
                    isr = isr2;
                } catch (Throwable th2) {
                    th = th2;
                    input = input2;
                    isr = isr2;
                }
            } catch (Exception e4) {
                e = e4;
                isr = isr2;
                try {
                    e.getMessage();
                    if (isr != null) {
                        try {
                            isr.close();
                        } catch (Exception e22) {
                            e22.getMessage();
                        }
                    }
                    if (fIn != null) {
                        fIn.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                    return returnString.toString();
                } catch (Throwable th3) {
                    th = th3;
                    if (isr != null) {
                        try {
                            isr.close();
                        } catch (Exception e222) {
                            e222.getMessage();
                            throw th;
                        }
                    }
                    if (fIn != null) {
                        fIn.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                isr = isr2;
                if (isr != null) {
                    isr.close();
                }
                if (fIn != null) {
                    fIn.close();
                }
                if (input != null) {
                    input.close();
                }
                throw th;
            }
        } catch (Exception e5) {
            e = e5;
            e.getMessage();
            if (isr != null) {
                isr.close();
            }
            if (fIn != null) {
                fIn.close();
            }
            if (input != null) {
                input.close();
            }
            return returnString.toString();
        }
        return returnString.toString();
    }

    public static String readUTF8TextFileFromAssert(Context Context, String szFileName) {
        Exception e;
        Throwable th;
        StringBuilder returnString = new StringBuilder();
        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;
        try {
            fIn = Context.getResources().getAssets().open(szFileName, 1);
            InputStreamReader isr2 = new InputStreamReader(fIn, "UTF8");
            try {
                BufferedReader input2 = new BufferedReader(isr2);
                try {
                    String str = "";
                    while (true) {
                        str = input2.readLine();
                        if (str == null) {
                            break;
                        }
                        returnString.append(str);
                        returnString.append("\r\n");
                    }
                    if (isr2 != null) {
                        try {
                            isr2.close();
                        } catch (Exception e2) {
                            e2.getMessage();
                        }
                    }
                    if (fIn != null) {
                        fIn.close();
                    }
                    if (input2 != null) {
                        input2.close();
                        input = input2;
                        isr = isr2;
                        return returnString.toString();
                    }
                    input = input2;
                    isr = isr2;
                } catch (Exception e3) {
                    e = e3;
                    input = input2;
                    isr = isr2;
                } catch (Throwable th2) {
                    th = th2;
                    input = input2;
                    isr = isr2;
                }
            } catch (Exception e4) {
                e = e4;
                isr = isr2;
                try {
                    e.getMessage();
                    if (isr != null) {
                        try {
                            isr.close();
                        } catch (Exception e22) {
                            e22.getMessage();
                        }
                    }
                    if (fIn != null) {
                        fIn.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                    return returnString.toString();
                } catch (Throwable th3) {
                    th = th3;
                    if (isr != null) {
                        try {
                            isr.close();
                        } catch (Exception e222) {
                            e222.getMessage();
                            throw th;
                        }
                    }
                    if (fIn != null) {
                        fIn.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                isr = isr2;
                if (isr != null) {
                    isr.close();
                }
                if (fIn != null) {
                    fIn.close();
                }
                if (input != null) {
                    input.close();
                }
                throw th;
            }
        } catch (Exception e5) {
            e = e5;
            e.getMessage();
            if (isr != null) {
                isr.close();
            }
            if (fIn != null) {
                fIn.close();
            }
            if (input != null) {
                input.close();
            }
            return returnString.toString();
        }
        return returnString.toString();
    }

    public static boolean appendUTF8File(File src, File dst) {
        try {
            InputStream in = new FileInputStream(src);
            FileWriter f = new FileWriter(dst.getAbsolutePath(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(in, "UTF8"));
            String str = "";
            while (true) {
                str = input.readLine();
                if (str == null) {
                    f.flush();
                    f.close();
                    return true;
                }
                f.write(new StringBuilder(String.valueOf(str)).append("\r\n").toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean copyFile(File src, File dst) {
        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);
            byte[] buf = new byte[1024];
            while (true) {
                int len = in.read(buf);
                if (len <= 0) {
                    in.close();
                    out.close();
                    return true;
                }
                out.write(buf, 0, len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e2) {
            e2.printStackTrace();
            return false;
        }
    }

    public static String XMLToString(Document RootDocument, boolean bRemoveXMLHead) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(RootDocument);
            StringWriter writer = new StringWriter();
            transformer.transform(source, new StreamResult(writer));
            String output = writer.toString();
            return (bRemoveXMLHead && output.startsWith("<?")) ? output.substring(output.indexOf(">") + 1) : output;
        } catch (TransformerException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String XMLToString(Document RootDocument) {
        return XMLToString(RootDocument, false);
    }

    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String getTemplateContent(Context Context, String szTemplateName) {
        Exception e;
        Throwable th;
        BufferedReader br = null;
        String szTemplateContent = "";
        String szOneline = "";
        try {
            BufferedReader br2 = new BufferedReader(new InputStreamReader(Context.getAssets().open(szTemplateName)));
            while (true) {
                try {
                    szOneline = br2.readLine();
                    if (szOneline == null) {
                        try {
                            break;
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            br = br2;
                        }
                    } else {
                        szTemplateContent = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(szTemplateContent)).append(szOneline).toString())).append("\r\n").toString();
                    }
                } catch (Exception e2) {
                    e = e2;
                    br = br2;
                } catch (Throwable th2) {
                    th = th2;
                    br = br2;
                }
            }
            br2.close();
            br = br2;
        } catch (Exception e3) {
            e = e3;
            try {
                e.printStackTrace();
                try {
                    br.close();
                } catch (IOException ex2) {
                    ex2.printStackTrace();
                }
                return szTemplateContent;
            } catch (Throwable th3) {
                th = th3;
                try {
                    br.close();
                } catch (IOException ex22) {
                    ex22.printStackTrace();
                }
                throw th;
            }
        }
        return szTemplateContent;
    }

    public static String getTextFromFile(String szFileName) {
        String fileContents = "";
        try {
            InputStream inputStream = new FileInputStream(szFileName);
            if (inputStream == null) {
                return fileContents;
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String receiveString = "";
            StringBuilder stringBuilder = new StringBuilder();
            while (true) {
                receiveString = bufferedReader.readLine();
                if (receiveString == null) {
                    inputStream.close();
                    return stringBuilder.toString();
                }
                stringBuilder.append(receiveString);
                stringBuilder.append("\r\n");
            }
        } catch (FileNotFoundException e) {
            Log.e("exception", "File not found: " + e.toString());
            return fileContents;
        } catch (IOException e2) {
            Log.e("exception", "Can not read file: " + e2.toString());
            return fileContents;
        }
    }

    public static String getTextFromStream(InputStream inputStream) {
        String fileContents = "";
        if (inputStream == null) {
            return fileContents;
        }
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String receiveString = "";
            StringBuilder stringBuilder = new StringBuilder();
            while (true) {
                receiveString = bufferedReader.readLine();
                if (receiveString == null) {
                    inputStream.close();
                    return stringBuilder.toString();
                }
                stringBuilder.append(receiveString);
                stringBuilder.append("\r\n");
            }
        } catch (FileNotFoundException e) {
            Log.e("exception", "File not found: " + e.toString());
            return fileContents;
        } catch (IOException e2) {
            Log.e("exception", "Can not read file: " + e2.toString());
            return fileContents;
        }
    }

    public static boolean writeTextToFile(String szFileName, String szText) {
        try {
            File myFile = new File(szFileName);
            if (myFile.exists()) {
                myFile.delete();
            }
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(szText);
            myOutWriter.close();
            fOut.close();
            return true;
        } catch (Exception e) {
            Log.e("ERRR", "Could not create file", e);
            return false;
        }
    }

    public static boolean writeTextToStream(OutputStream outputStream, String szText) {
        try {
            OutputStreamWriter myOutWriter = new OutputStreamWriter(outputStream);
            myOutWriter.append(szText);
            myOutWriter.close();
            return true;
        } catch (Exception e) {
            Log.e("ERRR", "Could not create file", e);
            return false;
        }
    }

    public static void showStaticTextInWebView(WebView WebView, String szText) {
        String mimeType = NanoHTTPD.MIME_HTML;
        String encoding = "utf-8";
        WebView.loadDataWithBaseURL("", szText, NanoHTTPD.MIME_HTML, "utf-8", "");
        WebView.invalidate();
    }

    public static void showStaticTextInWebView2(WebView WebView, String szText) {
        String mimeType = NanoHTTPD.MIME_HTML;
        String encoding = "utf-8";
        WebView.loadData(szText, NanoHTTPD.MIME_HTML, "utf-8");
        WebView.invalidate();
    }

    public static int getScreenWidth(Window Window) {
        DisplayMetrics metrics = new DisplayMetrics();
        Window.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    public static int getScreenHeight(Window Window) {
        DisplayMetrics metrics = new DisplayMetrics();
        Window.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }

    public static int getScreenWidth(Context context) {
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.widthPixels;
    }

    public static int getScreenWidth() {
        Display display = ((WindowManager) MyiBaseApplication.getBaseAppContext().getSystemService("window")).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.heightPixels;
    }

    public static int getScreenHeight() {
        Display display = ((WindowManager) MyiBaseApplication.getBaseAppContext().getSystemService("window")).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.heightPixels;
    }

    public static float dpToPixel(float dp, Context context) {
        return dp * (((float) context.getResources().getDisplayMetrics().densityDpi) / 160.0f);
    }

    public static float dpToPixel(float dp) {
        return dpToPixel(dp, MyiBaseApplication.getBaseAppContext());
    }

    public static int dpToPixel(int dp, Context context) {
        return (int) dpToPixel((float) dp, context);
    }

    public static float dpToPixel(int dp) {
        return (float) dpToPixel(dp, MyiBaseApplication.getBaseAppContext());
    }

    public static float pixelToDp(float px, Context context) {
        return px / (((float) context.getResources().getDisplayMetrics().densityDpi) / 160.0f);
    }

    public static float pixelToDp(float px) {
        return pixelToDp(px, MyiBaseApplication.getBaseAppContext());
    }

    public static int pixelToDp(int px, Context context) {
        return (int) pixelToDp((float) px, context);
    }

    public static int pixelToDp(int px) {
        return pixelToDp(px, MyiBaseApplication.getBaseAppContext());
    }

    public static float getDisplayScale() {
        return getDisplayScale(MyiBaseApplication.getBaseAppContext());
    }

    public static float getDisplayScale(Context context) {
        int nDisplayDPI = 0;
        if (null == null) {
            nDisplayDPI = context.getResources().getDisplayMetrics().densityDpi;
        }
        return ((float) nDisplayDPI) / 160.0f;
    }

    public static float getDisplayScaleX() {
        return getDisplayScaleX(MyiBaseApplication.getBaseAppContext());
    }

    public static float getDisplayScaleX(Context context) {
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return ((float) metrics.widthPixels) / 1024.0f;
    }

    public static float getDisplayScaleY() {
        return getDisplayScaleY(MyiBaseApplication.getBaseAppContext());
    }

    public static float getDisplayScaleY(Context context) {
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return ((float) metrics.heightPixels) / 730.0f;
    }

    public static String getNow() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Calendar.getInstance().getTime());
    }

    public static String getNowDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Calendar.getInstance().getTime());
    }

    public static String getNowDateChinese() {
        return new SimpleDateFormat("yyyy年MM月dd日", Locale.ENGLISH).format(Calendar.getInstance().getTime());
    }

    public static String getNowMillsecond() {
        Calendar c = Calendar.getInstance();
        return new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(c.getTime()))).append(".").toString())).append(String.valueOf(c.get(14))).toString();
    }

    public static void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            Drawable Drawable = imageView.getDrawable();
            imageView.setImageBitmap(null);
            if (imageView.getBackground() != null) {
                Drawable backgroundDrawable = imageView.getBackground();
                imageView.setBackgroundDrawable(null);
            }
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (i = 0; i < viewGroup.getChildCount(); i++) {
                unbindDrawables(viewGroup.getChildAt(i));
            }
            if (!(view instanceof AdapterView)) {
                viewGroup.removeAllViews();
            }
        } else if (view instanceof GridView) {
            GridView gridView = (GridView) view;
            int count = gridView.getCount();
            for (i = 0; i < count; i++) {
                View v = gridView.getChildAt(i);
                if (v != null) {
                    unbindDrawables(v);
                }
            }
        }
    }

    public static String createGUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String getVersionName(Context Context) {
        String szVersion = "";
        try {
            return Context.getPackageManager().getPackageInfo(Context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return szVersion;
        }
    }

    public static String getSubjectName(int nSubjectID) {
        ArrayList<Subject> arrSubjects = MyiBaseApplication.getCommonVariables().ServerInfo.ServerConfiguration.getSubjects();
        for (int i = 0; i < arrSubjects.size(); i++) {
            Subject oneSubject = (Subject) arrSubjects.get(i);
            if (oneSubject.nID == nSubjectID) {
                return oneSubject.szName;
            }
        }
        return null;
    }

    public static int getSubjectID(String szSubjectName) {
        ArrayList<Subject> arrSubjects = MyiBaseApplication.getCommonVariables().ServerInfo.ServerConfiguration.getSubjects();
        for (int i = 0; i < arrSubjects.size(); i++) {
            Subject oneSubject = (Subject) arrSubjects.get(i);
            if (oneSubject.szName.equalsIgnoreCase(szSubjectName)) {
                return oneSubject.nID;
            }
        }
        return -1;
    }

    public static void getAllSubjectInfo(ArrayList<String> arrSubjectNames, ArrayList<Integer> arrSubjectIDs) {
        ArrayList<Subject> arrSubjects = MyiBaseApplication.getCommonVariables().ServerInfo.ServerConfiguration.getSubjects();
        for (int i = 0; i < arrSubjects.size(); i++) {
            Subject oneSubject = (Subject) arrSubjects.get(i);
            arrSubjectIDs.add(Integer.valueOf(oneSubject.nID));
            arrSubjectNames.add(oneSubject.szName);
        }
    }

    public static int getSubjectColor(int nSubjectID) {
        int[] arrSubjectColor = new int[]{-13388315, -5609780, -6697984, -17613, -48060, -16737844, -6736948, -10053376, -30720, -3407872};
        int[] arrSubjectID = new int[10];
        arrSubjectID[1] = 1;
        arrSubjectID[2] = 2;
        arrSubjectID[3] = 3;
        arrSubjectID[4] = 4;
        arrSubjectID[5] = 5;
        arrSubjectID[6] = 6;
        arrSubjectID[7] = 7;
        arrSubjectID[8] = 8;
        arrSubjectID[9] = 9;
        for (int i = 0; i < arrSubjectID.length; i++) {
            if (arrSubjectID[i] == nSubjectID) {
                return arrSubjectColor[i];
            }
        }
        return -956647;
    }

    public static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", Platform.ANDROID);
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static void launchIntent(Intent intent) {
        Activity activity = UI.getCurrentActivity();
        if (activity != null) {
            activity.startActivity(intent);
            return;
        }
        intent.addFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
        MyiBaseApplication.getBaseAppContext().startActivity(intent);
    }

    public static void launchEventIntent(Intent intent, Object event) {
        writeObjectToIntent(event, intent);
        intent.addFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
        MyiBaseApplication.getBaseAppContext().getApplicationContext().startActivity(intent);
    }

    public static Bitmap cloneBitmap(Bitmap source, int x, int y, int width, int height) {
        source.getWidth();
        source.getHeight();
        int neww = width;
        int newh = height;
        Canvas canvas = new Canvas();
        Rect srcR = new Rect(x, y, source.getWidth() + x, source.getHeight() + y);
        RectF dstR = new RectF(0.0f, 0.0f, (float) width, (float) height);
        Config newConfig = Config.ARGB_8888;
        Config config = source.getConfig();
        if (config != null) {
            switch ($SWITCH_TABLE$android$graphics$Bitmap$Config()[config.ordinal()]) {
                case 1:
                    newConfig = Config.ALPHA_8;
                    break;
                case 4:
                    newConfig = Config.RGB_565;
                    break;
                default:
                    newConfig = Config.ARGB_8888;
                    break;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(neww, newh, newConfig);
        bitmap.setDensity(source.getDensity());
        canvas.setBitmap(bitmap);
        canvas.drawBitmap(source, srcR, dstR, null);
        canvas.setBitmap(null);
        return bitmap;
    }

    public static void fadeOutView(View View, int nTime) {
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration((long) nTime);
        View.setVisibility(4);
        View.startAnimation(fadeIn);
        View.setVisibility(0);
    }

    public static void fadeInView(View View, int nTime) {
        AlphaAnimation fadeIn = new AlphaAnimation(1.0f, 0.0f);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration((long) nTime);
        View.setVisibility(0);
        View.startAnimation(fadeIn);
        View.setVisibility(4);
    }

    public static void sliderAndFadeView(View view) {
        view.setVisibility(0);
        view.setAlpha(0.0f);
        view.animate().translationY((float) view.getHeight()).alpha(1.0f);
    }

    public static void sliderFromLeftToRight(View View, int nTime) {
        TranslateAnimation SliderIn = new TranslateAnimation(-100.0f, 0.0f, 0.0f, 0.0f);
        SliderIn.setInterpolator(new DecelerateInterpolator());
        SliderIn.setDuration((long) nTime);
        View.setVisibility(4);
        View.startAnimation(SliderIn);
        View.setVisibility(0);
    }

    public static void sliderFromRightToLeft(View View, int nTime) {
        TranslateAnimation SliderIn = new TranslateAnimation(100.0f, 0.0f, 0.0f, 0.0f);
        SliderIn.setInterpolator(new DecelerateInterpolator());
        SliderIn.setDuration((long) nTime);
        View.setVisibility(4);
        View.startAnimation(SliderIn);
        View.setVisibility(0);
    }

    public static void sliderAndHideFromTopToBottom(View View, int nTime) {
        TranslateAnimation SliderIn = new TranslateAnimation(0.0f, 0.0f, 0.0f, 100.0f);
        SliderIn.setInterpolator(new DecelerateInterpolator());
        SliderIn.setDuration((long) nTime);
        View.setVisibility(0);
        View.startAnimation(SliderIn);
        View.setVisibility(4);
    }

    public static void sliderAndHideFromBottomToTop(View View, int nTime) {
        TranslateAnimation SliderIn = new TranslateAnimation(0.0f, 0.0f, 0.0f, -100.0f);
        SliderIn.setInterpolator(new DecelerateInterpolator());
        SliderIn.setDuration((long) nTime);
        View.setVisibility(0);
        View.startAnimation(SliderIn);
        View.setVisibility(4);
    }

    public static void sliderFromTopToBottom(View View, int nTime) {
        TranslateAnimation SliderIn = new TranslateAnimation(0.0f, 0.0f, -100.0f, 0.0f);
        SliderIn.setInterpolator(new DecelerateInterpolator());
        SliderIn.setDuration((long) nTime);
        View.setVisibility(4);
        View.startAnimation(SliderIn);
        View.setVisibility(0);
    }

    public static void sliderFromBottomToTop(View View, int nTime) {
        TranslateAnimation SliderIn = new TranslateAnimation(0.0f, 0.0f, 100.0f, 0.0f);
        SliderIn.setInterpolator(new DecelerateInterpolator());
        SliderIn.setDuration((long) nTime);
        View.setVisibility(4);
        View.startAnimation(SliderIn);
        View.setVisibility(0);
    }

    public static synchronized void logContent(String szContent) {
        synchronized (Utilities.class) {
            Runtime info = Runtime.getRuntime();
            long usedSize = info.totalMemory() - info.freeMemory();
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String szThreadInfo = String.format("(%s - %d)", new Object[]{Thread.currentThread().getName(), Long.valueOf(Thread.currentThread().getId())});
            try {
                FileWriter f = new FileWriter("/sdcard/myiapp_runtime_log.txt", true);
                f.write("[" + timestamp + "](" + szThreadInfo + ") " + szContent + " (Memory used " + formatFileSize((double) usedSize) + ")\r\n");
                f.flush();
                f.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void logUsage(String szContent) {
        synchronized (Utilities.class) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String szModuleName = MyiBaseApplication.getBaseAppContext().getPackageName();
            szContent = "[" + timestamp + "]" + String.format("(%s)", new Object[]{szModuleName}) + " " + szContent + "\r\n";
            try {
                FileWriter f = new FileWriter("/sdcard/myiapp_usage.txt", true);
                f.write(szContent);
                f.flush();
                f.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void logException(Exception e) {
        logException(e, null);
    }

    public static void logInfo(String szText) {
        Log.d("LogInfo", szText);
        JSONObject json = new JSONObject();
        logBasicInfo(json);
        try {
            json.put(TestHandler.ACTION, "info");
            StringBuilder stringBuilder = new StringBuilder();
            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                stringBuilder.append(element.toString()).append("\n");
            }
            json.put("intent", stringBuilder.toString());
            if (szText != null) {
                json.put(VideoColumns.DESCRIPTION, szText);
            }
            logUsage(json.toString());
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    public static void logException(Exception e, String szDescription) {
        JSONObject json = new JSONObject();
        logBasicInfo(json);
        try {
            json.put(TestHandler.ACTION, "exception");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            json.put("intent", sw.toString());
            if (szDescription != null) {
                json.put(VideoColumns.DESCRIPTION, szDescription);
            } else {
                json.put(VideoColumns.DESCRIPTION, e.getMessage());
            }
            logUsage(json.toString());
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    public static void logActivity(Activity activity, String szAction) {
        JSONObject json = new JSONObject();
        logBasicInfo(json);
        try {
            json.put(TestHandler.ACTION, szAction);
            if (activity != null) {
                json.put("activity", activity.getClass().getName());
                if (activity.getIntent() != null) {
                    json.put("intent", activity.getIntent().toUri(0));
                }
            }
            logUsage(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void logWindow(Service window, String szAction) {
        JSONObject json = new JSONObject();
        logBasicInfo(json);
        try {
            json.put(TestHandler.ACTION, szAction);
            if (window != null) {
                json.put("window", window.getClass().getName());
            }
            logUsage(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void logID(int nID, String szDescription, String szData, View v, String szVerb) {
        String szName;
        Activity activity = UI.getCurrentActivity();
        JSONObject json = new JSONObject();
        logBasicInfo(json);
        try {
            szName = MyiBaseApplication.getBaseAppContext().getResources().getResourceEntryName(nID);
        } catch (NotFoundException e) {
            szName = "Invalid ID";
        }
        if (activity != null) {
            try {
                json.put("activity", activity.getClass().getName());
                if (activity.getIntent() != null) {
                    json.put("intent", activity.getIntent().toUri(0));
                }
            } catch (JSONException e2) {
                e2.printStackTrace();
                return;
            }
        }
        json.put("id", szName);
        json.put(VideoColumns.DESCRIPTION, szDescription);
        if (szData != null) {
            json.put("data", szData);
        }
        if (v != null) {
            json.put("view", v.getClass().getName());
        }
        if (szVerb != null) {
            json.put("verb", szVerb);
        }
        logUsage(json.toString());
    }

    public static void logClick(View v) {
        logID(v.getId(), v.toString(), null, v, null);
    }

    public static void logClick(View v, String szData) {
        logID(v.getId(), v.toString(), szData, v, null);
    }

    public static void logClick(View v, String szData, String szVerb) {
        logID(v.getId(), v.toString(), szData, v, szVerb);
    }

    public static void logMenuClick(MenuItem v) {
        logID(v.getItemId(), v.toString(), null, null, null);
    }

    public static void logMenuClick(MenuItem v, String szData) {
        logID(v.getItemId(), v.toString(), szData, null, null);
    }

    private static void logBasicInfo(JSONObject json) {
        String szUserGUID = MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID;
        String szUserName = MyiBaseApplication.getCommonVariables().UserInfo.szUserName;
        String szRealName = MyiBaseApplication.getCommonVariables().UserInfo.szRealName;
        try {
            json.put("guid", createGUID());
            json.put("userguid", szUserGUID);
            json.put(UserHonourFragment.USERNAME, szUserName);
            json.put("realname", szRealName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void putACRAData(String szData) {
        Runtime info = Runtime.getRuntime();
        long freeSize = info.freeMemory();
        long totalSize = info.totalMemory();
        long usedSize = totalSize - freeSize;
        try {
            Crasheye.addExtraData(new StringBuilder(String.valueOf(new StringBuilder(String.valueOf("[" + String.format("(%s - %d)", new Object[]{Thread.currentThread().getName(), Long.valueOf(Thread.currentThread().getId())}) + getNow())).append("(").append(SystemClock.uptimeMillis() / 1000).append("s,").toString())).append("Mem:").append(String.valueOf((usedSize / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID)).append("MB/").append(String.valueOf((totalSize / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID)).append("MB)]").toString(), szData);
        } catch (IllegalStateException e) {
        }
    }

    public static boolean isInSameSubnet(String szIP1, String szIP2) {
        String[] arrIP1 = szIP1.split("\\.");
        String[] arrIP2 = szIP2.split("\\.");
        if (arrIP1.length != 4 || arrIP2.length != 4) {
            return true;
        }
        if (arrIP1.length == arrIP2.length && arrIP1[0].equalsIgnoreCase(arrIP2[0]) && arrIP1[1].equalsIgnoreCase(arrIP2[1])) {
            return true;
        }
        return false;
    }

    public static boolean isUseRelayServer(String szLanIP1, String szLanIP2, String szWanIP1, String szWanIP2) {
        if (szWanIP1.equalsIgnoreCase(szWanIP2) && isInSameSubnet(szLanIP1, szLanIP2)) {
            return false;
        }
        return true;
    }

    public static void showToastMessage(Context context, String szMessage) {
        String szFinalMessage = szMessage;
        new Handler(context.getMainLooper()).post(new AnonymousClass2(context.getApplicationContext(), szFinalMessage));
    }

    public static void showToastMessage(String szMessage, int duration) {
        String szFinalMessage = szMessage;
        Context finalContext = MyiBaseApplication.getBaseAppContext();
        new Handler(finalContext.getMainLooper()).post(new AnonymousClass3(finalContext, szFinalMessage, duration));
    }

    public static void showToastMessage(String szMessage) {
        showToastMessage(szMessage, 0);
    }

    public static void runOnUIThread(Context context, Runnable Runnable) {
        new Handler(context.getMainLooper()).post(Runnable);
    }

    public static void runOnUIThreadFirst(Context context, Runnable Runnable) {
        new Handler(context.getMainLooper()).postAtFrontOfQueue(Runnable);
    }

    public static void runOnUIThreadDelay(Context context, Runnable Runnable, long delayinms) {
        new Handler(context.getMainLooper()).postDelayed(Runnable, delayinms);
    }

    public static void clearRunnable(Context context, Runnable Runnable) {
        new Handler(context.getMainLooper()).removeCallbacks(Runnable);
    }

    public static void showAlertMessage(Builder builder) {
        try {
            builder.create().show();
        } catch (BadTokenException e) {
            Log.e(TAG, "showAlertMessage failed with BadTokenException. Will try UI.getCurrentActivity.");
            AlertDialog alert = builder.create();
            alert.getWindow().setType(2003);
            alert.show();
        }
    }

    public static void showAlertMessage(Context context, String szTitle, String szMessage) {
        if (context == null) {
            context = UI.getCurrentActivity();
            Log.e(TAG, "showAlertMessage with a null context. Trying UI.getCurrentActivity().");
            if (context == null) {
                Log.e(TAG, "showAlertMessage still with a null context. Ignored this messge silence. szMessage=" + szMessage);
                return;
            }
        }
        String szFinalTitle = szTitle;
        Context finalContext = context;
        new Handler(context.getMainLooper()).post(new AnonymousClass4(finalContext, szFinalTitle, szMessage));
    }

    public static void showAlertMessage(Context context, String szTitle, String szMessage, OnClickListener onClickListener) {
        if (context == null) {
            context = UI.getCurrentActivity();
            Log.e(TAG, "showAlertMessage with a null context. Trying UI.getCurrentActivity().");
            if (context == null) {
                Log.e(TAG, "showAlertMessage still with a null context. Ignored this messge silence. szMessage=" + szMessage);
                return;
            }
        }
        String szFinalTitle = szTitle;
        Context finalContext = context;
        new Handler(context.getMainLooper()).post(new AnonymousClass5(finalContext, szFinalTitle, szMessage, onClickListener));
    }

    public static void showAlertMessage(Context context, String szTitle, String szMessage, OnClickListener onClickYesListener, OnClickListener onClickNoListener) {
        if (context == null) {
            context = UI.getCurrentActivity();
            Log.e(TAG, "showAlertMessage with a null context. Trying UI.getCurrentActivity().");
            if (context == null) {
                Log.e(TAG, "showAlertMessage still with a null context. Ignored this messge silence. szMessage=" + szMessage);
                return;
            }
        }
        String szFinalMessage = szMessage;
        String szFinalTitle = szTitle;
        new Handler(context.getMainLooper()).post(new AnonymousClass6(new ContextThemeWrapper(context, R.style.ComponentTheme), szFinalTitle, szFinalMessage, onClickYesListener, onClickNoListener));
    }

    public static void showAlertMessageWithNotDisplayAgain(Context context, String szTitle, String szMessage) {
        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("AlertDialog_" + szTitle + "_NeverShow", false)) {
            String szFinalTitle = szTitle;
            Context finalContext = context;
            new Handler(context.getMainLooper()).post(new AnonymousClass7(finalContext, szFinalTitle, szMessage));
        }
    }

    public static void setFullScreenWindow(Window window) {
        window.addFlags(1024);
        if (VERSION.SDK_INT >= 19) {
            window.getDecorView().setSystemUiVisibility(4356);
        }
    }

    public static void setKeepScreenOn(Window window) {
        window.addFlags(128);
    }

    public static String getTimePart(String szTime) {
        try {
            Date StartDate = new Date();
            String szResult = "";
            return new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(szTime));
        } catch (ParseException e1) {
            e1.printStackTrace();
            return "";
        }
    }

    public static long getTimeDifference(String szTimeOld, String szTimeNew) {
        try {
            Date StartDate = new Date();
            StartDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(szTimeOld);
            Date EndDate = new Date();
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(szTimeNew).getTime() - StartDate.getTime();
        } catch (ParseException e1) {
            e1.printStackTrace();
            return 0;
        }
    }

    public static String getTimeOffsetInHourMinutesSeconds(long nTimeDiffinms) {
        long diffInSec = TimeUnit.MILLISECONDS.toSeconds(nTimeDiffinms);
        int seconds = (int) (diffInSec % 60);
        diffInSec /= 60;
        int minutes = (int) (diffInSec % 60);
        diffInSec /= 60;
        int hours = (int) (diffInSec % 24);
        diffInSec /= 24;
        return String.format("%02d:%02d:%02d", new Object[]{Integer.valueOf(hours), Integer.valueOf(minutes), Integer.valueOf(seconds)});
    }

    public static String getTimeOffsetInSeconds(long nTimeDiffinms) {
        return String.valueOf((int) (TimeUnit.MILLISECONDS.toSeconds(nTimeDiffinms) % 60));
    }

    public static Bitmap loadBitmapFromFile(String szFileName) {
        return BitmapFactory.decodeFile(szFileName);
    }

    public static boolean saveBitmapToJpeg(String szFileName, Bitmap bmp) {
        Exception e;
        Throwable th;
        FileOutputStream out = null;
        boolean bResult = false;
        try {
            FileOutputStream out2 = new FileOutputStream(szFileName);
            try {
                bmp.compress(CompressFormat.JPEG, 90, out2);
                bResult = true;
                if (out2 != null) {
                    try {
                        out2.close();
                        out = out2;
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    return bResult;
                }
                out = out2;
            } catch (Exception e3) {
                e = e3;
                out = out2;
                try {
                    e.printStackTrace();
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    return bResult;
                } catch (Throwable th2) {
                    th = th2;
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                out = out2;
                if (out != null) {
                    out.close();
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            e.printStackTrace();
            if (out != null) {
                out.close();
            }
            return bResult;
        }
        return bResult;
    }

    public static boolean saveBitmapToPng(String szFileName, Bitmap bmp) {
        Exception e;
        Throwable th;
        FileOutputStream out = null;
        boolean bResult = false;
        try {
            FileOutputStream out2 = new FileOutputStream(szFileName);
            try {
                bmp.compress(CompressFormat.PNG, 90, out2);
                bResult = true;
                if (out2 != null) {
                    try {
                        out2.close();
                        out = out2;
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    return bResult;
                }
                out = out2;
            } catch (Exception e3) {
                e = e3;
                out = out2;
                try {
                    e.printStackTrace();
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    return bResult;
                } catch (Throwable th2) {
                    th = th2;
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                out = out2;
                if (out != null) {
                    out.close();
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            e.printStackTrace();
            if (out != null) {
                out.close();
            }
            return bResult;
        }
        return bResult;
    }

    public static String getExtensionName(String filename) {
        if (filename == null || filename.length() <= 0) {
            return filename;
        }
        int dot = filename.lastIndexOf(46);
        if (dot <= -1 || dot >= filename.length() - 1) {
            return filename;
        }
        return filename.substring(dot + 1);
    }

    public static String saveBitmapToBase64String(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(CompressFormat.JPEG, 75, baos);
        String encodedImage = Base64.encodeToString(baos.toByteArray(), 0);
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] byteArrayImage = null;
        return encodedImage;
    }

    public static String saveBitmapToBase64String(Bitmap bmp, int nQuanlity) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(CompressFormat.JPEG, nQuanlity, baos);
        String encodedImage = Base64.encodeToString(baos.toByteArray(), 0);
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] byteArrayImage = null;
        return encodedImage;
    }

    public static ArrayList<Integer> StringArrayToIntArray(ArrayList<String> arrSource) {
        ArrayList<Integer> arrResult = new ArrayList();
        for (int i = 0; i < arrSource.size(); i++) {
            int nValue = 0;
            try {
                nValue = Integer.valueOf((String) arrSource.get(i)).intValue();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            arrResult.add(Integer.valueOf(nValue));
        }
        return arrResult;
    }

    public static int toInt(String szValue) {
        int nResult = 0;
        try {
            nResult = Integer.valueOf(szValue).intValue();
        } catch (NumberFormatException e) {
        }
        return nResult;
    }

    public static int getThemeCustomColor(int nColorResID) {
        if (mmapThemeCustomColor.containsKey(Integer.valueOf(nColorResID))) {
            return ((Integer) mmapThemeCustomColor.get(Integer.valueOf(nColorResID))).intValue();
        }
        int[] customColorAttr = new int[]{nColorResID};
        Context context = MyiBaseApplication.getBaseAppContext();
        if (UI.getCurrentActivity() != null) {
            context = UI.getCurrentActivity();
        }
        TypedArray attributes = context.obtainStyledAttributes(customColorAttr);
        int nColor = attributes.getColor(0, -16777216);
        attributes.recycle();
        mmapThemeCustomColor.put(Integer.valueOf(nColorResID), Integer.valueOf(nColor));
        return nColor;
    }

    public static Drawable getThemeCustomDrawable(int nStyleAttrID) {
        int[] customColorAttr = new int[]{nStyleAttrID};
        Context context = MyiBaseApplication.getBaseAppContext();
        if (UI.getCurrentActivity() != null) {
            context = UI.getCurrentActivity();
        }
        TypedArray attributes = context.obtainStyledAttributes(customColorAttr);
        Drawable drawable = attributes.getDrawable(0);
        attributes.recycle();
        return drawable;
    }

    public static Drawable getThemeCustomDrawable(int nStyleAttrID, Context context) {
        TypedArray attributes = context.obtainStyledAttributes(new int[]{nStyleAttrID});
        Drawable drawable = attributes.getDrawable(0);
        attributes.recycle();
        return drawable;
    }

    public static int getThemeCustomColor(int nColorResID, Activity activity) {
        TypedArray attributes = activity.obtainStyledAttributes(new int[]{nColorResID});
        int nColor = attributes.getColor(0, -16777216);
        attributes.recycle();
        return nColor;
    }

    public static int getThemeCustomStyle(int nStyleAttributeID, Context activity) {
        TypedArray attributes = activity.obtainStyledAttributes(new int[]{nStyleAttributeID});
        int nThemeID = attributes.getResourceId(0, -16777216);
        attributes.recycle();
        return nThemeID;
    }

    public static int getThemeCustomResID(int nStyleAttributeID, Context activity) {
        TypedArray attributes = activity.obtainStyledAttributes(new int[]{nStyleAttributeID});
        int nThemeID = attributes.getResourceId(0, -16777216);
        attributes.recycle();
        return nThemeID;
    }

    public static Button createThemedButton(Context activity) {
        return new Button(new ContextThemeWrapper(activity, getThemeCustomStyle(R.attr.flat_button, activity)), null, 0);
    }

    public static void setSpinnerData(Context Context, Spinner Spinner, ArrayList<String> arrData) {
        Spinner.setAdapter(new ArrayAdapter(Context, 17367043, 16908308, (String[]) arrData.toArray(new String[0])));
    }

    public static Calendar getDatePart(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        cal.set(14, 0);
        return cal;
    }

    public static long daysBetween(Date startDate, Date endDate) {
        Calendar sDate = getDatePart(startDate);
        Calendar eDate = getDatePart(endDate);
        long daysBetween = 0;
        while (sDate.before(eDate)) {
            sDate.add(5, 1);
            daysBetween++;
        }
        return daysBetween;
    }

    public static boolean isCurrentUserOwner() {
        try {
            if (((Integer) UserManager.class.getMethod("getUserHandle", new Class[0]).invoke(MyiBaseApplication.getBaseAppContext().getSystemService("user"), new Object[0])).intValue() == 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static String encryptString(String szSource, String szPassword) {
        while (szPassword.length() < 16) {
            szPassword = new StringBuilder(String.valueOf(szPassword)).append(".").toString();
        }
        SecretKey secret = new SecretKeySpec(szPassword.getBytes(), "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(1, secret);
            return toHex(cipher.doFinal(szSource.getBytes(HTTP.UTF_8)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encryptStringZeroPadding(String szSource, String szPassword) {
        while (szPassword.length() < 16) {
            szPassword = new StringBuilder(String.valueOf(szPassword)).append(".").toString();
        }
        SecretKey secret = new SecretKeySpec(szPassword.getBytes(), "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/ZeroBytePadding");
            cipher.init(1, secret);
            return toHex(cipher.doFinal(szSource.getBytes(HTTP.UTF_8)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encryptStringZeroPaddingToBase64(String szSource, String szPassword) {
        while (szPassword.length() < 16) {
            szPassword = new StringBuilder(String.valueOf(szPassword)).append(".").toString();
        }
        SecretKey secret = new SecretKeySpec(szPassword.getBytes(), "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/ZeroBytePadding");
            cipher.init(1, secret);
            return Base64.encodeToString(cipher.doFinal(szSource.getBytes(HTTP.UTF_8)), 2);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decryptString(String szEncryptText, String szPassword) {
        while (szPassword.length() < 16) {
            szPassword = new StringBuilder(String.valueOf(szPassword)).append(".").toString();
        }
        SecretKey secret = new SecretKeySpec(szPassword.getBytes(), "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(2, secret);
            return new String(cipher.doFinal(toByte(szEncryptText)), HTTP.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decryptStringZeroPadding(String szEncryptText, String szPassword) {
        while (szPassword.length() < 16) {
            szPassword = new StringBuilder(String.valueOf(szPassword)).append(".").toString();
        }
        SecretKey secret = new SecretKeySpec(szPassword.getBytes(), "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/ZeroBytePadding");
            cipher.init(2, secret);
            return new String(cipher.doFinal(toByte(szEncryptText)), HTTP.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decryptBase64StringZeroPadding(String szBase64EncryptText, String szPassword) {
        while (szPassword.length() < 16) {
            szPassword = new StringBuilder(String.valueOf(szPassword)).append(".").toString();
        }
        if (szPassword.length() > 16) {
            szPassword = szPassword.substring(0, 16);
        }
        SecretKey secret = new SecretKeySpec(szPassword.getBytes(), "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/ZeroBytePadding");
            cipher.init(2, secret);
            return new String(cipher.doFinal(Base64.decode(szBase64EncryptText, 2)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void vibrator(int nTimeinms) {
        ((Vibrator) MyiBaseApplication.getBaseAppContext().getSystemService("vibrator")).vibrate((long) nTimeinms);
    }

    public static void beep(int nVolume, int nTimeinms) {
        if (System.currentTimeMillis() - mnLastBeepStartTime > 2000) {
            new ToneGenerator(4, nVolume).startTone(93, nTimeinms);
            mnLastBeepStartTime = System.currentTimeMillis();
        }
    }

    public static boolean dumpLogcatToFile(String szFileNameOnSdCard) {
        try {
            File myFile = new File("/sdcard/" + szFileNameOnSdCard);
            if (myFile.exists()) {
                myFile.delete();
            }
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(new String[]{"logcat", "-d", "-v", "time"}).getInputStream()));
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    myOutWriter.close();
                    fOut.close();
                    return true;
                }
                myOutWriter.append(line);
                myOutWriter.append("\r\n");
            }
        } catch (IOException ex) {
            Log.e(TAG, "getLog failed", ex);
            return false;
        }
    }

    public static boolean savePngToJpeg(String szPngFileName, String szJpgFileName) {
        File targetFile = new File(szPngFileName);
        if (targetFile.length() > 0) {
            Bitmap myBitmap = BitmapFactory.decodeFile(szPngFileName);
            try {
                BufferedOutputStream bufOutStr = new BufferedOutputStream(new FileOutputStream(szJpgFileName));
                myBitmap.compress(CompressFormat.JPEG, 100, bufOutStr);
                bufOutStr.flush();
                bufOutStr.close();
                targetFile.delete();
                return true;
            } catch (FileNotFoundException exception) {
                exception.printStackTrace();
            } catch (IOException exception2) {
                exception2.printStackTrace();
            }
        }
        return false;
    }

    public static void randomWait(int nMaxWaitTime) {
        try {
            Thread.sleep((long) ThreadLocalRandom.current().nextInt(0, nMaxWaitTime + 1));
        } catch (InterruptedException e) {
        }
    }

    public static void setViewBackground(View view, Drawable drawable) {
        if (view != null) {
            if (VERSION.SDK_INT < 16) {
                view.setBackgroundDrawable(drawable);
            } else {
                view.setBackground(drawable);
            }
        }
    }

    public static boolean isHoneycombOrLater() {
        return VERSION.SDK_INT >= 11;
    }

    public static boolean isICSOrLater() {
        return VERSION.SDK_INT >= 14;
    }

    public static boolean isJellyBeanOrLater() {
        return VERSION.SDK_INT >= 16;
    }
}
