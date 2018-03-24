package com.netspace.teacherpad.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.adapter.ClassResourcesAdapter;
import com.netspace.teacherpad.dialog.ResourceSeekDialog;
import com.netspace.teacherpad.dialog.ResourceSeekDialog.OnSeekCallBack;
import com.netspace.teacherpad.dialog.StartClassControlUnit;

public class ResourcesListFragment extends Fragment implements OnClickListener {
    private static ClassResourcesAdapter mResourceAdapter;
    private static int mnLastViewPosition = -1;
    private ClassResourcesAdapter mAdapter;
    private Handler mHandler = new Handler();
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecycleView;
    private View mRootView;
    private TextView mTextViewMessage;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(R.layout.fragment_resourceslist, null);
        this.mRecycleView = (RecyclerView) this.mRootView.findViewById(R.id.studentAnswerView);
        this.mLayoutManager = new LinearLayoutManager(getActivity(), 1, false);
        this.mRecycleView.setLayoutManager(this.mLayoutManager);
        this.mRecycleView.setItemAnimator(new DefaultItemAnimator());
        this.mAdapter = new ClassResourcesAdapter(getActivity(), TeacherPadApplication.arrResourceData);
        this.mRecycleView.setAdapter(this.mAdapter);
        this.mAdapter.setOnClickListener(this);
        mResourceAdapter = this.mAdapter;
        this.mTextViewMessage = (TextView) this.mRootView.findViewById(R.id.textViewNoData);
        this.mTextViewMessage.setVisibility(4);
        if (this.mAdapter.getItemCount() == 0) {
            reportMessage("当前没有数据");
        } else if (!(mnLastViewPosition == -1 || mnLastViewPosition == -1 || mnLastViewPosition >= this.mAdapter.getItemCount())) {
            this.mLayoutManager.scrollToPosition(mnLastViewPosition);
        }
        return this.mRootView;
    }

    private void reportMessage(String szMessage) {
        if (this.mTextViewMessage != null) {
            this.mTextViewMessage.setVisibility(0);
            this.mTextViewMessage.setText(szMessage);
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.cardViewClassResourceItem) {
            if (TeacherPadApplication.marrMonitors.size() == 1) {
                StartClassControlUnit.switchToResource(((ResourceItemData) v.getTag()).szGUID, 0);
                ((DialogFragment) getParentFragment()).dismiss();
            }
        } else if (v.getId() == R.id.buttonScreen) {
            Item = (ResourceItemData) v.getTag(R.id.buttonScreen);
            TextView textView = (TextView) v;
            int nScreenID = Utilities.toInt(textView.getText().toString());
            if (nScreenID > 0) {
                StartClassControlUnit.switchToResource(Item.szGUID, nScreenID - 1);
                textView.setSelected(true);
            }
        } else if (v.getId() == R.id.buttonPlay || v.getId() == R.id.buttonStop || v.getId() == R.id.buttonPrev || v.getId() == R.id.buttonNext) {
            Item = (ResourceItemData) v.getTag(R.id.buttonScreen);
            for (int i = 0; i < TeacherPadApplication.marrMonitors.size(); i++) {
                if (StartClassControlUnit.isResourceInScreen(Item.szGUID, i)) {
                    StartClassControlUnit.processCommandWithScreen(v, i);
                }
            }
        } else if (v.getId() == R.id.buttonSeek) {
            Item = (ResourceItemData) v.getTag(R.id.buttonScreen);
            if (Item.arrThumbnailUrls != null && Item.arrThumbnailUrls.size() > 0) {
                ResourceSeekDialog seekDialog = new ResourceSeekDialog();
                seekDialog.setImageListUrls(Item.arrThumbnailUrls);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                seekDialog.setCallBack(new OnSeekCallBack() {
                    public void onSeek(int nPosition, ResourceItemData resourceItemData) {
                        for (int i = 0; i < TeacherPadApplication.marrMonitors.size(); i++) {
                            if (StartClassControlUnit.isResourceOnTop(resourceItemData.szGUID, i)) {
                                TeacherPadApplication.IMThread.SendMessage("SetPlayPos " + String.valueOf(nPosition + 1) + " " + String.valueOf(i));
                            }
                        }
                    }
                });
                seekDialog.setResourceItem(Item);
                seekDialog.setCancelable(true);
                seekDialog.show(ft, "seekDialog");
            }
        }
    }

    public void onDestroyView() {
        mnLastViewPosition = this.mLayoutManager.findFirstCompletelyVisibleItemPosition();
        mResourceAdapter = null;
        super.onDestroyView();
    }

    public static void refrehResourceAdapter() {
        if (mResourceAdapter != null) {
            mResourceAdapter.notifyDataSetChanged();
        }
    }
}
