package io.vov.vitamio.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.lang.ref.WeakReference;

public class InitActivity extends Activity {
    public static final String FROM_ME = "fromVitamioInitActivity";
    private ProgressDialog mPD;
    private UIHandler uiHandler;

    private static class UIHandler extends Handler {
        private WeakReference<Context> mContext;

        public UIHandler(Context c) {
            this.mContext = new WeakReference(c);
        }

        public void handleMessage(Message msg) {
            InitActivity ctx = (InitActivity) this.mContext.get();
            switch (msg.what) {
                case 0:
                    ctx.mPD.dismiss();
                    Intent src = ctx.getIntent();
                    Intent i = new Intent();
                    i.setClassName(src.getStringExtra("package"), src.getStringExtra("className"));
                    i.setData(src.getData());
                    i.putExtras(src);
                    i.putExtra(InitActivity.FROM_ME, true);
                    ctx.startActivity(i);
                    ctx.finish();
                    return;
                default:
                    return;
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(128);
        this.uiHandler = new UIHandler(this);
        new AsyncTask<Object, Object, Boolean>() {
            protected void onPreExecute() {
                InitActivity.this.mPD = new ProgressDialog(InitActivity.this);
                InitActivity.this.mPD.setCancelable(false);
                InitActivity.this.mPD.setMessage(InitActivity.this.getString(InitActivity.this.getResources().getIdentifier("vitamio_init_decoders", "string", InitActivity.this.getPackageName())));
                InitActivity.this.mPD.show();
            }

            protected void onPostExecute(Boolean inited) {
                if (inited.booleanValue()) {
                    InitActivity.this.uiHandler.sendEmptyMessage(0);
                }
            }

            protected Boolean doInBackground(Object... arg0) {
                return null;
            }
        }.execute(new Object[0]);
    }
}
