package com.netspace.teacherpad.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.adapter.SimpleListAdapterWrapper;
import com.netspace.library.adapter.SimpleListAdapterWrapper.ListAdapterWrapperCallBack;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.ScreenDisplayActivity;
import com.netspace.teacherpad.TeacherPadApplication;
import com.netspace.teacherpad.dialog.StartClassControlUnit;
import com.netspace.teacherpad.structure.MultiScreen;
import com.netspace.teacherpad.structure.PlayPos;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Map.Entry;

public class ScreenResourcesHistoryListFragment extends ListFragment implements ListAdapterWrapperCallBack, OnClickListener {
    private SimpleListAdapterWrapper mListAdapterWrapper;
    private ArrayList<HistoryItem> marrData = new ArrayList();

    private static class HistoryItem {
        public boolean bEmptyPrompt;
        public boolean bScreenCategory;
        public IconDrawable icon;
        public int nIndex;
        public int nScreenID;
        public String szGUID;
        public String szStatus;
        public String szTitle;

        private HistoryItem() {
            this.szTitle = "";
            this.bScreenCategory = false;
            this.bEmptyPrompt = false;
            this.szGUID = "";
            this.szStatus = "";
            this.nScreenID = 0;
            this.nIndex = 0;
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mListAdapterWrapper = new SimpleListAdapterWrapper(getContext(), this, R.layout.listitem_screenresourcehistory);
        setListAdapter(this.mListAdapterWrapper);
        return inflater.inflate(R.layout.list, null);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        HistoryItem item = (HistoryItem) this.marrData.get(position);
        int nScreenID = item.nScreenID;
        int nIndex = item.nIndex;
        boolean bNeedRefresh = false;
        if (nIndex == -1 && nScreenID >= 0 && nScreenID < TeacherPadApplication.marrMonitors.size()) {
            if (((MultiScreen) TeacherPadApplication.marrMonitors.get(nScreenID)).bMaximized) {
                TeacherPadApplication.IMThread.SendMessage("RestoreScreen " + String.valueOf(nScreenID), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                TeacherPadApplication.IMThread.SendMessage("GetScreenLayout", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            } else {
                TeacherPadApplication.mActiveScreenID = nScreenID;
                TeacherPadApplication.IMThread.SendMessage("MaxScreen " + String.valueOf(nScreenID), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
                TeacherPadApplication.IMThread.SendMessage("GetScreenLayout", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
            }
            bNeedRefresh = true;
        } else if (nScreenID >= 0 && nScreenID < TeacherPadApplication.marrMonitors.size()) {
            MultiScreen screen = (MultiScreen) TeacherPadApplication.marrMonitors.get(nScreenID);
            String szGUID = "";
            if (nIndex < screen.arrPlayStack.size() && StartClassControlUnit.switchToResource((String) screen.arrPlayStack.get(nIndex), nScreenID)) {
                bNeedRefresh = true;
            }
        }
        if (bNeedRefresh) {
            Utilities.runOnUIThreadDelay(getContext(), new Runnable() {
                public void run() {
                    Activity activity = UI.getCurrentActivity();
                    if (activity instanceof ScreenDisplayActivity) {
                        ((ScreenDisplayActivity) activity).updateSideMenu();
                    }
                }
            }, 1000);
        }
    }

    public void notifyDataSetChanged() {
        if (this.mListAdapterWrapper != null) {
            this.mListAdapterWrapper.notifyDataSetChanged();
        }
    }

    public void refresh() {
        if (this.mListAdapterWrapper != null) {
            getListView().setDivider(null);
            this.marrData.clear();
            for (int i = 0; i < TeacherPadApplication.marrMonitors.size(); i++) {
                MultiScreen oneScreen = (MultiScreen) TeacherPadApplication.marrMonitors.get(i);
                HistoryItem screenItem = new HistoryItem();
                screenItem.bScreenCategory = true;
                screenItem.szTitle = "第" + String.valueOf(i + 1) + "屏";
                screenItem.nScreenID = i;
                screenItem.nIndex = -1;
                if (oneScreen.bMaximized) {
                    screenItem.icon = new IconDrawable(MyiBaseApplication.getBaseAppContext(), FontAwesomeIcons.fa_window_maximize).color(-9605779).sizeDp(16);
                } else {
                    screenItem.icon = new IconDrawable(MyiBaseApplication.getBaseAppContext(), FontAwesomeIcons.fa_window_restore).color(-9605779).sizeDp(16);
                }
                this.marrData.add(screenItem);
                if (oneScreen.arrPlayStack.size() > 0) {
                    for (int j = 0; j < oneScreen.arrPlayStack.size(); j++) {
                        HistoryItem childItem = new HistoryItem();
                        int nFlag = ((Integer) oneScreen.arrPlayStackFlags.get(j)).intValue();
                        PlayPos playPos = (PlayPos) oneScreen.arrPlayPos.get(j);
                        String szGUID = (String) oneScreen.arrPlayStack.get(j);
                        childItem.nScreenID = i;
                        childItem.nIndex = j;
                        childItem.szTitle = getResourceTitle((String) oneScreen.arrPlayStack.get(j));
                        if (szGUID.indexOf("/") == -1) {
                            childItem.szGUID = szGUID;
                        }
                        childItem.icon = null;
                        childItem.szStatus = "当前隐藏";
                        if (j == 0) {
                            childItem.szStatus = "正在显示";
                            if (playPos.nLength != 0 && playPos.nLength < 100) {
                                childItem.szStatus += "(" + playPos.nPos + "/" + playPos.nLength + ")";
                            }
                        }
                        if ((StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_PLAYING & nFlag) == StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_PLAYING) {
                            childItem.szStatus = "正在播放";
                        }
                        if ((StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_STOPPED & nFlag) == StartClassControlUnit.WM_CLASSMEDIA_CONTROL_FLAG_STOPPED) {
                            childItem.szStatus = "已停止播放";
                        }
                        this.marrData.add(childItem);
                    }
                } else {
                    HistoryItem emptyItem = new HistoryItem();
                    emptyItem.szTitle = "<当前没有播放任何内容>";
                    emptyItem.nScreenID = i;
                    emptyItem.bEmptyPrompt = true;
                    this.marrData.add(emptyItem);
                }
            }
            notifyDataSetChanged();
        }
    }

    private String getResourceTitle(String szGUIDOrUrl) {
        for (int i = 0; i < TeacherPadApplication.arrResourceData.size(); i++) {
            ResourceItemData item = (ResourceItemData) TeacherPadApplication.arrResourceData.get(i);
            if (item.szGUID.equalsIgnoreCase(szGUIDOrUrl)) {
                return item.szTitle;
            }
        }
        if (szGUIDOrUrl.indexOf(":") != -1) {
            String szIP = szGUIDOrUrl.substring(0, szGUIDOrUrl.indexOf(":"));
            String szStudentName = "";
            String szStudentID = "";
            if (szIP.equalsIgnoreCase(Utilities.getWifiIP(getContext()))) {
                return "当前平板投屏";
            }
            for (Entry<String, String> e : TeacherPadApplication.mapStudentIP.entrySet()) {
                String key = (String) e.getKey();
                if (((String) e.getValue()).equalsIgnoreCase(szIP)) {
                    szStudentName = (String) TeacherPadApplication.mapStudentName.get(key);
                    break;
                }
            }
            if (!(szStudentName == null || szStudentName.isEmpty())) {
                return "学生" + szStudentName + "的平板投屏";
            }
        }
        String szTitle = (String) TeacherPadApplication.mapResourceTitles.get(szGUIDOrUrl);
        if (!(szTitle == null || szTitle.isEmpty())) {
            return szTitle;
        }
        return szGUIDOrUrl;
    }

    public int getCount() {
        return this.marrData.size();
    }

    public Object getItem(int position) {
        return this.marrData.get(position);
    }

    public void getView(int position, View convertView) {
        HistoryItem item = (HistoryItem) this.marrData.get(position);
        TextView textViewLargeTitle = (TextView) convertView.findViewById(R.id.textViewLargeTitle);
        TextView textViewTitle = (TextView) convertView.findViewById(R.id.TextViewTitle);
        TextView textViewDescription = (TextView) convertView.findViewById(R.id.textViewDescription);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.ImageViewState);
        ImageView imageViewThumbnail = (ImageView) convertView.findViewById(R.id.imageViewThumbnail);
        ImageView imageViewWindowState = (ImageView) convertView.findViewById(R.id.ImageViewWindowState);
        if (item.bEmptyPrompt || item.bScreenCategory) {
            imageViewThumbnail.setVisibility(8);
            imageView.setVisibility(8);
        } else {
            imageViewThumbnail.setVisibility(0);
            imageView.setVisibility(0);
        }
        if (item.bScreenCategory) {
            textViewLargeTitle.setText(item.szTitle);
            textViewLargeTitle.setVisibility(0);
            textViewTitle.setVisibility(4);
            imageViewWindowState.setVisibility(0);
            imageViewWindowState.setImageDrawable(item.icon);
            imageViewWindowState.setOnClickListener(null);
            imageViewWindowState.setTag(null);
        } else {
            textViewTitle.setText(item.szTitle);
            textViewTitle.setVisibility(0);
            textViewLargeTitle.setVisibility(4);
            imageViewWindowState.setVisibility(4);
            if (item.bEmptyPrompt) {
                imageView.setVisibility(8);
            } else {
                imageView.setVisibility(0);
                imageView.setImageDrawable(new IconDrawable(getContext(), FontAwesomeIcons.fa_close).color(-14606047).sizeDp(22));
                imageView.setOnClickListener(this);
                imageView.setTag(item);
            }
        }
        textViewDescription.setText(item.szStatus);
        if (item.szGUID.isEmpty()) {
            imageViewThumbnail.setImageDrawable(null);
            return;
        }
        Picasso.with(getContext()).load(MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/getresourcethumbnail?guid=" + item.szGUID).error((int) R.drawable.ic_placehold_small_gray).into(imageViewThumbnail);
    }

    public void onClick(View v) {
        HistoryItem item = (HistoryItem) v.getTag();
        TeacherPadApplication.IMThread.SendMessage("CloseResourceFromScreen " + item.szGUID + " " + String.valueOf(item.nScreenID), MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        TeacherPadApplication.IMThread.SendMessage("GetScreenPlayStack", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        TeacherPadApplication.IMThread.SendMessage("GetScreenLayout", MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
    }
}
