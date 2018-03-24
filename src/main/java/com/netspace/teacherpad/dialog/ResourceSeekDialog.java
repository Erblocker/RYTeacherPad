package com.netspace.teacherpad.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.netspace.library.adapter.SimpleListAdapterWrapper;
import com.netspace.library.adapter.SimpleListAdapterWrapper.ListAdapterWrapperCallBack;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class ResourceSeekDialog extends DialogFragment implements ListAdapterWrapperCallBack, OnItemClickListener {
    private OnSeekCallBack mCallBack;
    private SimpleListAdapterWrapper mListAdapterWrapper;
    private ResourceItemData mResourceItemData;
    private View mRootView;
    private ArrayList<String> mThumbnailImages;
    private ListView mThumbnailList;

    public interface OnSeekCallBack {
        void onSeek(int i, ResourceItemData resourceItemData);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mListAdapterWrapper = new SimpleListAdapterWrapper(getContext(), this, R.layout.listitem_seekthumbnail);
        getDialog().getWindow().requestFeature(1);
        this.mRootView = inflater.inflate(R.layout.dialog_seekbar2, container, false);
        this.mThumbnailList = (ListView) this.mRootView.findViewById(R.id.listViewImages);
        this.mThumbnailList.setOnItemClickListener(this);
        ((Toolbar) this.mRootView.findViewById(R.id.toolbar)).setTitle((CharSequence) "资源跳转");
        this.mThumbnailList.setAdapter(this.mListAdapterWrapper);
        return this.mRootView;
    }

    public void setImageListUrls(ArrayList<String> arrImageURLs) {
        this.mThumbnailImages = arrImageURLs;
    }

    public void setResourceItem(ResourceItemData resourceItem) {
        this.mResourceItemData = resourceItem;
    }

    public void setCallBack(OnSeekCallBack CallBack) {
        this.mCallBack = CallBack;
    }

    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(Utilities.dpToPixel(600, getContext()), -1);
        }
    }

    public int getCount() {
        return this.mThumbnailImages.size();
    }

    public Object getItem(int position) {
        return this.mThumbnailImages.get(position);
    }

    public void getView(int position, View convertView) {
        TextView textView = (TextView) convertView.findViewById(R.id.textViewIndex);
        Picasso.with(getContext()).load((String) this.mThumbnailImages.get(position)).error((int) R.drawable.ic_placehold_small_gray).into((ImageView) convertView.findViewById(R.id.imageThumbnail));
        textView.setText("第" + String.valueOf(position + 1) + "页");
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (this.mCallBack != null) {
            this.mCallBack.onSeek(position, this.mResourceItemData);
        }
    }
}
