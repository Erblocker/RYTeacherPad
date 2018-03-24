package com.netspace.library.controls;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.netspace.pad.library.R;
import java.util.ArrayList;

public class CustomSelfLearnItem extends LinearLayout implements OnScrollChangedListener {
    private OnVisibleListener mCallBack = null;
    private boolean mDataLoaded = false;
    private Context m_Context;
    private ListView m_ListView;
    private String m_szResourceGUID;
    private String m_szScheduleGUID;
    private String m_szUserClassGUID;
    private String m_szUserClassName;
    private boolean mbAttachedToWindow = false;

    public interface OnVisibleListener {
        void OnVisible(CustomSelfLearnItem customSelfLearnItem);
    }

    public CustomSelfLearnItem(Context context) {
        super(context);
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.layout_customselflearnitemview, this);
        this.m_ListView = (ListView) findViewById(R.id.listView1);
        this.m_ListView.setVisibility(8);
        this.m_ListView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case 0:
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                    case 1:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                v.onTouchEvent(event);
                return true;
            }
        });
        this.m_Context = context;
    }

    public void setTitle(String szTitle) {
        TextView TextView = (TextView) findViewById(R.id.TextViewTitle);
        if (TextView != null) {
            TextView.setText(szTitle);
        }
    }

    public String getTitle() {
        TextView TextView = (TextView) findViewById(R.id.TextViewTitle);
        if (TextView != null) {
            return TextView.getText().toString();
        }
        return "";
    }

    public boolean isDataLoaded() {
        return this.mDataLoaded;
    }

    public void setUnread(boolean bUnread) {
        ImageView view = (ImageView) findViewById(R.id.imageUnread);
        if (view == null) {
            return;
        }
        if (bUnread) {
            view.setVisibility(0);
        } else {
            view.setVisibility(8);
        }
    }

    public void setSummery(String szSummery) {
        TextView TextView = (TextView) findViewById(R.id.TextViewSubTitle);
        if (TextView != null) {
            TextView.setText(szSummery);
        }
    }

    public void setBlockColor(int nColor) {
        View BlockView = findViewById(R.id.ViewColorBlock);
        if (BlockView != null) {
            BlockView.setBackgroundColor(nColor);
        }
    }

    public void setBlockVisible(boolean bVisible) {
        View BlockView = findViewById(R.id.ViewColorBlock);
        if (BlockView == null) {
            return;
        }
        if (bVisible) {
            BlockView.setVisibility(0);
        } else {
            BlockView.setVisibility(8);
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        ImageView LinkImage = (ImageView) findViewById(R.id.imageView1);
        ((LinearLayout) findViewById(R.id.LayoutTitle)).setEnabled(enabled);
        if (!enabled && LinkImage != null) {
            LinkImage.setImageResource(R.drawable.ic_right);
        }
    }

    public void setListItems(ArrayList<String> arrData) {
        String[] arrStringObjects = new String[arrData.size()];
        arrData.toArray(arrStringObjects);
        this.m_ListView.setAdapter(new ArrayAdapter(this.m_Context, R.layout.listitem_selflearntasklist, R.id.textView1, arrStringObjects));
        this.m_ListView.setVisibility(0);
        setListViewHeightBasedOnItems(this.m_ListView);
    }

    private boolean setListViewHeightBasedOnItems(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return false;
        }
        int numberOfItems = listAdapter.getCount();
        int totalItemsHeight = 0;
        for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
            View item = listAdapter.getView(itemPos, null, listView);
            item.measure(0, 0);
            totalItemsHeight += item.getMeasuredHeight();
        }
        int totalDividersHeight = listView.getDividerHeight() * (numberOfItems - 1);
        LayoutParams params = listView.getLayoutParams();
        params.height = Math.min(totalItemsHeight + totalDividersHeight, 500);
        listView.setLayoutParams(params);
        listView.requestLayout();
        return true;
    }

    public void setListItems(ListAdapter Adapter) {
        this.m_ListView.setAdapter(Adapter);
        this.m_ListView.setVisibility(0);
        setListViewHeightBasedOnItems(this.m_ListView);
    }

    public void setListItemOnItemClickListener(OnItemClickListener OnItemClickListener) {
        this.m_ListView.setOnItemClickListener(OnItemClickListener);
    }

    public String getScheduleGUID() {
        return this.m_szScheduleGUID;
    }

    public void setScheduleGUID(String szScheduleGUID) {
        this.m_szScheduleGUID = szScheduleGUID;
    }

    public String getUserClassName() {
        return this.m_szUserClassName;
    }

    public String getUserClassGUID() {
        return this.m_szUserClassGUID;
    }

    public void setUserClassName(String szUserClassName) {
        this.m_szUserClassName = szUserClassName;
    }

    public void setUserClassGUID(String szUserClassGUID) {
        this.m_szUserClassGUID = szUserClassGUID;
    }

    public String getResourceGUID() {
        return this.m_szResourceGUID;
    }

    public void clearBackground() {
        findViewById(R.id.LayoutTitle).setBackgroundResource(R.drawable.background_selectitem);
    }

    public void setResourceGUID(String szResourceGUID) {
        this.m_szResourceGUID = szResourceGUID;
    }

    public void setCallBack(OnVisibleListener CallBack) {
        this.mCallBack = CallBack;
    }

    protected void onAttachedToWindow() {
        this.mbAttachedToWindow = true;
        ViewTreeObserver vto = getViewTreeObserver();
        if (vto != null) {
            vto.addOnScrollChangedListener(this);
        }
        super.onAttachedToWindow();
        if (getLocalVisibleRect(new Rect())) {
            this.mDataLoaded = true;
            loadData();
        }
    }

    protected void onDetachedFromWindow() {
        this.mbAttachedToWindow = false;
        ViewTreeObserver vto = getViewTreeObserver();
        if (vto != null) {
            vto.removeOnScrollChangedListener(this);
        }
        super.onDetachedFromWindow();
    }

    public void onScrollChanged() {
        if (getLocalVisibleRect(new Rect()) && !this.mDataLoaded) {
            loadData();
            ViewTreeObserver vto = getViewTreeObserver();
            if (vto != null) {
                vto.removeOnScrollChangedListener(this);
            }
        }
    }

    public void loadData() {
        this.mDataLoaded = true;
        if (this.mCallBack != null) {
            this.mCallBack.OnVisible(this);
        }
    }
}
