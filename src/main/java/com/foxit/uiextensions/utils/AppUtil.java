package com.foxit.uiextensions.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.dialog.UITextEditDialog;
import java.math.BigDecimal;
import java.util.regex.Pattern;
import net.sqlcipher.database.SQLiteDatabase;

public class AppUtil {
    private static long sLastTimeMillis;

    /* renamed from: com.foxit.uiextensions.utils.AppUtil$1 */
    class AnonymousClass1 implements Runnable {
        private final /* synthetic */ View val$editText;

        AnonymousClass1(View view) {
            this.val$editText = view;
        }

        public void run() {
            ((InputMethodManager) this.val$editText.getContext().getSystemService("input_method")).showSoftInput(this.val$editText, 0);
        }
    }

    /* renamed from: com.foxit.uiextensions.utils.AppUtil$2 */
    class AnonymousClass2 implements OnClickListener {
        private final /* synthetic */ Activity val$act;
        private final /* synthetic */ UITextEditDialog val$dialog;
        private final /* synthetic */ String val$myurl;

        AnonymousClass2(String str, Activity activity, UITextEditDialog uITextEditDialog) {
            this.val$myurl = str;
            this.val$act = activity;
            this.val$dialog = uITextEditDialog;
        }

        public void onClick(View v) {
            Uri uri;
            if (this.val$myurl.toLowerCase().startsWith("http://") || this.val$myurl.toLowerCase().startsWith("https://")) {
                uri = Uri.parse(this.val$myurl);
            } else {
                uri = Uri.parse("http://" + this.val$myurl);
            }
            this.val$act.startActivity(new Intent("android.intent.action.VIEW", uri));
            this.val$dialog.dismiss();
        }
    }

    /* renamed from: com.foxit.uiextensions.utils.AppUtil$3 */
    class AnonymousClass3 implements OnClickListener {
        private final /* synthetic */ UITextEditDialog val$dialog;

        AnonymousClass3(UITextEditDialog uITextEditDialog) {
            this.val$dialog = uITextEditDialog;
        }

        public void onClick(View v) {
            this.val$dialog.dismiss();
        }
    }

    public static boolean isEmailFormatForRMS(String userId) {
        return Pattern.compile("[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?").matcher(userId).find();
    }

    public static boolean isFastDoubleClick() {
        long currentTimeMillis = System.currentTimeMillis();
        if (Math.abs(currentTimeMillis - sLastTimeMillis) < 500) {
            return true;
        }
        sLastTimeMillis = currentTimeMillis;
        return false;
    }

    public static void showSoftInput(View editText) {
        if (editText != null) {
            editText.requestFocus();
            editText.post(new AnonymousClass1(editText));
        }
    }

    public static void dismissInputSoft(View editText) {
        if (editText != null) {
            ((InputMethodManager) editText.getContext().getSystemService("input_method")).hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    public static void fixBackgroundRepeat(View view) {
        Drawable bg = view.getBackground();
        if (bg != null && (bg instanceof BitmapDrawable)) {
            BitmapDrawable bmp = (BitmapDrawable) bg;
            bmp.mutate();
            bmp.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        }
    }

    public static void openUrl(Activity act, String url) {
        String myurl = url;
        UITextEditDialog dialog = new UITextEditDialog(act);
        dialog.getInputEditText().setVisibility(8);
        dialog.setTitle(R.string.rv_url_dialog_title);
        dialog.getPromptTextView().setText(new StringBuilder(String.valueOf(act.getResources().getString(R.string.rv_urldialog_title))).append(url).append(act.getResources().getString(R.string.rv_urldialog_title_ko)).append("?").toString());
        dialog.getOKButton().setOnClickListener(new AnonymousClass2(myurl, act, dialog));
        dialog.getCancelButton().setOnClickListener(new AnonymousClass3(dialog));
        dialog.show();
    }

    public static void mailTo(Activity act, String uri) {
        if (!isEmpty(uri) && !isFastDoubleClick()) {
            Intent intent = new Intent("android.intent.action.SENDTO");
            if (uri.startsWith("mailto:")) {
                intent.setData(Uri.parse(uri));
            } else {
                intent.setData(Uri.parse("mailto:" + uri));
            }
            intent.addFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
            act.startActivity(Intent.createChooser(intent, ""));
        }
    }

    public static boolean isEmpty(CharSequence str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        return false;
    }

    public static String getEntryName(String allData, String entry) {
        if (allData == null || entry == null) {
            return null;
        }
        int startPos = allData.indexOf(entry);
        if (startPos < 0) {
            return "";
        }
        int endPos;
        if (entry.contentEquals("C=")) {
            endPos = allData.length();
        } else {
            endPos = allData.indexOf(",", startPos);
        }
        return allData.substring(entry.length() + startPos, endPos);
    }

    public static String getFileName(String filePath) {
        int index = filePath.lastIndexOf(47);
        return index < 0 ? filePath : filePath.substring(index + 1, filePath.length());
    }

    public static String fileSizeToString(long size) {
        float fsize = (float) size;
        char[] unit = new char[]{'B', 'K', 'M'};
        int i = 0;
        while (i < unit.length) {
            if (fsize < 1024.0f || i == unit.length - 1) {
                return new StringBuilder(String.valueOf(String.valueOf(new BigDecimal((double) fsize).setScale(2, 4).floatValue()))).append(unit[i]).toString();
            }
            fsize /= 1024.0f;
            i++;
        }
        return "";
    }

    public static String getFileFolder(String filePath) {
        int index = filePath.lastIndexOf(47);
        if (index < 0) {
            return "";
        }
        return filePath.substring(0, index);
    }
}
