package com.netspace.library.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import java.util.HashMap;

public class FaceSelectDialog extends Dialog implements OnItemClickListener {
    private Context mContext;
    private OnFaceSelectListener mListener;

    public interface OnFaceSelectListener {
        void OnFaceSelected(int i, int i2);
    }

    public FaceSelectDialog(Context context, OnFaceSelectListener OnFaceSelectListener) {
        super(context, 16974130);
        this.mContext = context;
        this.mListener = OnFaceSelectListener;
        setTitle("表情");
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_faceselect);
        GridView gridview = (GridView) findViewById(R.id.gridView1);
        ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList();
        for (int i = 0; i < 90; i++) {
            HashMap<String, Object> map = new HashMap();
            map.put("ItemImage", Integer.valueOf(this.mContext.getResources().getIdentifier("smiley_" + String.valueOf(i), "drawable", this.mContext.getPackageName())));
            lstImageItem.add(map);
        }
        gridview.setAdapter(new SimpleAdapter(getContext(), lstImageItem, R.layout.listitem_face, new String[]{"ItemImage"}, new int[]{R.id.imageFace}));
        gridview.setOnItemClickListener(this);
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (this.mListener != null) {
            this.mListener.OnFaceSelected(position, this.mContext.getResources().getIdentifier("smiley_" + String.valueOf(position), "drawable", this.mContext.getPackageName()));
        }
        dismiss();
    }
}
