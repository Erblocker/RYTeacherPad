package com.netspace.library.components;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import com.netspace.library.controls.CustomFrameLayout;
import com.netspace.library.interfaces.IComponents;
import com.netspace.library.interfaces.IComponents.ComponentCallBack;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import java.util.HashMap;

public class FaceComponent extends CustomFrameLayout implements IComponents, OnItemClickListener {
    private ComponentCallBack mCallBack;
    private Context mContextThemeWrapper;
    private GridView mGridView;
    private View mRootView;
    private int mSelectImageResID = 0;
    private String mszData;

    public FaceComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public FaceComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public FaceComponent(Context context) {
        super(context);
        initView();
    }

    public void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        this.mContextThemeWrapper = new ContextThemeWrapper(getContext(), R.style.ComponentTheme);
        this.mRootView = inflater.cloneInContext(this.mContextThemeWrapper).inflate(R.layout.component_face, this, true);
        this.mGridView = (GridView) this.mRootView.findViewById(R.id.gridView1);
        ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList();
        for (int i = 0; i < 90; i++) {
            HashMap<String, Object> map = new HashMap();
            map.put("ItemImage", Integer.valueOf(getContext().getResources().getIdentifier("smiley_" + String.valueOf(i), "drawable", getContext().getPackageName())));
            lstImageItem.add(map);
        }
        this.mGridView.setAdapter(new SimpleAdapter(this.mContextThemeWrapper, lstImageItem, R.layout.listitem_face, new String[]{"ItemImage"}, new int[]{R.id.imageFace}));
        this.mGridView.setOnItemClickListener(this);
    }

    public void setData(String szData) {
    }

    public String getData() {
        return String.valueOf(this.mSelectImageResID);
    }

    public void setCallBack(ComponentCallBack ComponentCallBack) {
        this.mCallBack = ComponentCallBack;
    }

    public void intentComplete(Intent intent) {
    }

    public void setLocked(boolean bLock) {
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        this.mSelectImageResID = getContext().getResources().getIdentifier("smiley_" + String.valueOf(position), "drawable", getContext().getPackageName());
        if (this.mCallBack != null) {
            this.mCallBack.OnDataUploaded(String.valueOf(position), this);
        }
    }
}
