package com.foxit.app.thread;

import android.os.AsyncTask;

public class AppAsyncTask extends AsyncTask<Object, Object, Object> {
    public void onPreExecute() {
        super.onPreExecute();
    }

    public Object doInBackground(Object... params) {
        return null;
    }

    public void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
    }

    public void onPostExecute(Object result) {
        super.onPostExecute(result);
    }
}
