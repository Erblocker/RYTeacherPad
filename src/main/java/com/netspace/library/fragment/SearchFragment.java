package com.netspace.library.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import com.netspace.library.activity.ResourceDetailActivity;
import com.netspace.library.adapter.SearchResultListAdapter;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.pad.library.R;
import java.util.ArrayList;

public class SearchFragment extends Fragment implements OnItemClickListener {
    private SearchResultListAdapter mAdapter;
    private ListView mListView;
    private int mMaxType = 0;
    private int mMinType = 0;
    private View mRootView;
    private TextView mTextViewMessage;
    private ArrayList<ResourceItemData> marrData;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mRootView != null) {
            return this.mRootView;
        }
        this.mRootView = inflater.inflate(R.layout.fragment_searchresult, null);
        this.mListView = (ListView) this.mRootView.findViewById(R.id.listView1);
        this.mListView.setOnItemClickListener(this);
        this.mTextViewMessage = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        this.mTextViewMessage.setVisibility(4);
        if (this.marrData == null) {
            reportMessage("请输入搜索关键词");
        } else {
            this.mAdapter = new SearchResultListAdapter(getActivity(), this.marrData);
            this.mListView.setAdapter(this.mAdapter);
            if (this.marrData.isEmpty()) {
                reportMessage("没有搜索到任何内容，请尝试换个关键字。");
            } else {
                this.mTextViewMessage.setVisibility(4);
                this.mListView.setVisibility(0);
            }
        }
        return this.mRootView;
    }

    private void reportMessage(String szMessage) {
        this.mTextViewMessage.setVisibility(0);
        this.mTextViewMessage.setText(szMessage);
        this.mListView.setVisibility(4);
    }

    public void setType(int nMin, int nMax) {
        this.mMinType = nMin;
        this.mMaxType = nMax;
    }

    public void handleSearchResult(ArrayList<ResourceItemData> arrData) {
        this.marrData = new ArrayList();
        for (int i = 0; i < arrData.size(); i++) {
            ResourceItemData newItem = (ResourceItemData) arrData.get(i);
            if (newItem.nType >= this.mMinType && newItem.nType < this.mMaxType) {
                this.marrData.add(newItem);
            }
        }
        if (this.mListView != null) {
            this.mAdapter = new SearchResultListAdapter(getActivity(), this.marrData);
            this.mListView.setAdapter(this.mAdapter);
            if (this.marrData.isEmpty()) {
                reportMessage("没有搜索到任何内容，请尝试换个关键字。");
                return;
            }
            this.mTextViewMessage.setVisibility(4);
            this.mListView.setVisibility(0);
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        ResourceItemData itemData = (ResourceItemData) this.marrData.get(position);
        Intent intent = new Intent(getActivity(), ResourceDetailActivity.class);
        if (itemData.nType == 1) {
            intent.putExtra("isquestion", true);
        } else {
            intent.putExtra("isquestion", false);
        }
        intent.putExtra("resourcetype", itemData.nType);
        intent.putExtra("title", itemData.szTitle);
        intent.putExtra(CommentComponent.RESOURCEGUID, itemData.szGUID);
        startActivity(intent);
    }
}
