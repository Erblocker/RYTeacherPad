package com.netspace.teacherpad.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.netspace.teacherpad.R;
import java.util.ArrayList;

public class ScreenSelectAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private LayoutInflater mInflater;
    private int mResID = 0;
    private ArrayList<ScreenSpinner> marrData = new ArrayList();

    private class ScreenSpinner {
        public Bitmap bitmap;
        public int[] nData;
        public String szText;

        private ScreenSpinner() {
        }
    }

    public ScreenSelectAdapter(Context context, int resource) {
        super(context, resource);
        this.mResID = resource;
        this.mContext = context;
        this.mInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public void addScreen(String szText, Bitmap bmp, int[] nData) {
        ScreenSpinner ScreenSpinner = new ScreenSpinner();
        ScreenSpinner.szText = szText;
        ScreenSpinner.bitmap = bmp;
        ScreenSpinner.nData = nData;
        this.marrData.add(ScreenSpinner);
    }

    public int[] getScreenData(int position) {
        return ((ScreenSpinner) this.marrData.get(position)).nData;
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        View row = this.mInflater.inflate(this.mResID, parent, false);
        ImageView icon = (ImageView) row.findViewById(R.id.image);
        ScreenSpinner data = (ScreenSpinner) this.marrData.get(position);
        ((TextView) row.findViewById(R.id.label)).setText(data.szText);
        icon.setImageBitmap(data.bitmap);
        return row;
    }

    public int getCount() {
        return this.marrData.size();
    }
}
