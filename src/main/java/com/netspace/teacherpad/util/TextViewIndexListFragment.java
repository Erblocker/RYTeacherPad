package com.netspace.teacherpad.util;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.netspace.library.controls.LockableScrollView;
import com.netspace.teacherpad.R;

public class TextViewIndexListFragment extends ListFragment {
    private int mTextViewID = -1;
    private ViewGroup mViewGroup;

    public class TextViewIndexAdapter extends ArrayAdapter<TextViewIndexItem> {
        public TextViewIndexAdapter(Context context) {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_basicsingleline, null);
            }
            ((TextView) convertView.findViewById(R.id.textTitle)).setText(((TextViewIndexItem) getItem(position)).szTitle);
            return convertView;
        }
    }

    private class TextViewIndexItem {
        public View TargetView;
        public String szTitle;

        public TextViewIndexItem(String szTitle, View View) {
            this.szTitle = szTitle;
            this.TargetView = View;
        }
    }

    public TextViewIndexListFragment(ViewGroup ListViewGroup) {
        this.mViewGroup = ListViewGroup;
    }

    public TextViewIndexListFragment(ViewGroup ListViewGroup, int nViewID) {
        this.mViewGroup = ListViewGroup;
        this.mTextViewID = nViewID;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mViewGroup == null) {
            this.mViewGroup = container;
        }
        return inflater.inflate(R.layout.list, null);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshDataSet();
    }

    public void refreshDataSet() {
        int i;
        TextViewIndexAdapter adapter = new TextViewIndexAdapter(getActivity());
        for (i = 0; i < this.mViewGroup.getChildCount(); i++) {
            View OneView = this.mViewGroup.getChildAt(i);
            if (OneView instanceof TextView) {
                if (this.mTextViewID == -1 || this.mTextViewID == OneView.getId()) {
                    TextView textView = (TextView) OneView;
                    adapter.add(new TextViewIndexItem(textView.getText().toString(), textView));
                }
            } else if ((OneView instanceof ViewGroup) && this.mTextViewID != -1) {
                View TempView = ((ViewGroup) OneView).findViewById(this.mTextViewID);
                if (TempView != null && (TempView instanceof TextView)) {
                    adapter.add(new TextViewIndexItem(((TextView) TempView).getText().toString(), OneView));
                }
            }
        }
        LinearLayout Layout = (LinearLayout) this.mViewGroup.findViewById(R.id.LayoutStudentImage);
        for (i = 0; i < Layout.getChildCount(); i++) {
            OneView = Layout.getChildAt(i);
            if (OneView instanceof TextView) {
                textView = (TextView) OneView;
                adapter.add(new TextViewIndexItem(textView.getText().toString(), textView));
            }
        }
        setListAdapter(adapter);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        int nTopOffset = 0;
        TextViewIndexItem SelectedItem = (TextViewIndexItem) getListAdapter().getItem(position);
        if (SelectedItem.TargetView.getParent() instanceof LinearLayout) {
            LinearLayout LinearLayout = (LinearLayout) SelectedItem.TargetView.getParent();
            if (LinearLayout.getId() == R.id.LayoutStudentImage) {
                nTopOffset = LinearLayout.getTop();
            }
        }
        for (View ParentView = this.mViewGroup; ParentView != null; ParentView = (View) ParentView.getParent()) {
            if (ParentView instanceof LockableScrollView) {
                ((LockableScrollView) ParentView).scrollTo(0, SelectedItem.TargetView.getTop() + nTopOffset);
                return;
            }
        }
    }
}
