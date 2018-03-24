package com.netspace.teacherpad.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.netspace.library.adapter.SimpleListAdapterWrapper;
import com.netspace.library.adapter.SimpleListAdapterWrapper.ListAdapterWrapperCallBack;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.dialog.StartClassControlUnit;
import com.netspace.teacherpad.structure.PlayPos;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class PDFResourcesListFragment extends ListFragment implements ListAdapterWrapperCallBack {
    private SimpleListAdapterWrapper mListAdapterWrapper;
    private ResourceItemData mResourceItemData;
    private ArrayList<String> mThumbnailImages;

    public void setImageListUrls(ArrayList<String> arrImageURLs) {
        this.mThumbnailImages = arrImageURLs;
        setListAdapter(this.mListAdapterWrapper);
        refresh();
    }

    public void setResourceItem(ResourceItemData resourceItem) {
        this.mResourceItemData = resourceItem;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        for (int i = 0; i < TeacherPadApplication.marrMonitors.size(); i++) {
            if (StartClassControlUnit.isResourceOnTop(this.mResourceItemData.szGUID, i)) {
                TeacherPadApplication.IMThread.SendMessage("SetPlayPos " + String.valueOf(position + 1) + " " + String.valueOf(i));
            }
        }
    }

    public void notifyDataSetChanged() {
        this.mListAdapterWrapper.notifyDataSetChanged();
    }

    private void refresh() {
        if (this.mResourceItemData != null) {
            PlayPos playPos = StartClassControlUnit.getResourcePlayPos(this.mResourceItemData.szGUID);
            int nFlags = StartClassControlUnit.getResourcePlayFlags(this.mResourceItemData.szGUID);
            if (playPos == null) {
                return;
            }
            if ((playPos == null || playPos.nPos != 0 || playPos.nLength != 0) && (StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTPLAYSTOP & nFlags) != StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_SUPPORTPLAYSTOP) {
                getListView().setSelection(playPos.nPos - 1);
            }
        }
    }

    public int getCount() {
        if (this.mThumbnailImages != null) {
            return this.mThumbnailImages.size();
        }
        return 0;
    }

    public Object getItem(int position) {
        if (this.mThumbnailImages != null) {
            return this.mThumbnailImages.get(position);
        }
        return null;
    }

    public void getView(int position, View convertView) {
        TextView textView = (TextView) convertView.findViewById(R.id.textViewIndex);
        Picasso.with(getContext()).load((String) this.mThumbnailImages.get(position)).error((int) R.drawable.ic_placehold_small_gray).into((ImageView) convertView.findViewById(R.id.imageThumbnail));
        textView.setText("第" + String.valueOf(position + 1) + "页");
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mListAdapterWrapper = new SimpleListAdapterWrapper(getContext(), this, R.layout.listitem_sidebar_seekthumbnail);
        setListAdapter(this.mListAdapterWrapper);
        setEmptyText("当前资源没有缩略图");
    }
}
