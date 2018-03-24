package com.netspace.teacherpad.fragments;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import com.netspace.library.adapter.SimpleListAdapterWrapper;
import com.netspace.library.adapter.SimpleListAdapterWrapper.ListAdapterWrapperCallBack;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.im.IMService;
import com.netspace.library.utilities.TextViewLinkHandler;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.HttpItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.teacherpad.R;
import java.util.ArrayList;
import java.util.Iterator;
import me.maxwin.view.XListView;
import me.maxwin.view.XListView.IXListViewListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SelectPCFragment extends Fragment implements OnItemClickListener, ListAdapterWrapperCallBack, OnItemLongClickListener, IXListViewListener {
    private LinearLayout mLayout;
    private SimpleListAdapterWrapper mListAdapterWrapper;
    private XListView mListView;
    private View mRootView;
    private TextView mTextViewInfo;
    private TextView mTextViewMessage;
    private ArrayList<PCData> marrData = new ArrayList();
    private int mnSelectedIndex = -1;
    private String mszCurrentScheduleGUID = "";

    public static class PCData {
        public boolean bCanSelect;
        public String szIP;
        public String szLocation;
        public String szSessionID;
        public String szStatus;
        public String szVersion;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(R.layout.fragment_selectpc, null);
        this.mLayout = (LinearLayout) this.mRootView.findViewById(R.id.layoutContent);
        this.mLayout.setVisibility(4);
        this.mListAdapterWrapper = new SimpleListAdapterWrapper(getContext(), this, R.layout.listitem_selectpc);
        this.mTextViewMessage = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        this.mTextViewMessage.setVisibility(4);
        this.mTextViewInfo = (TextView) this.mRootView.findViewById(R.id.textViewInfo);
        this.mListView = (XListView) this.mRootView.findViewById(R.id.listView1);
        this.mListView.setAdapter(this.mListAdapterWrapper);
        this.mListView.setOnItemClickListener(this);
        this.mListView.setOnItemLongClickListener(this);
        this.mListView.setPullLoadEnable(false);
        this.mListView.setPullRefreshEnable(true);
        this.mListView.setXListViewListener(this);
        this.mTextViewMessage.setMovementMethod(new TextViewLinkHandler() {
            public void onLinkClick(String url) {
                SelectPCFragment.this.loadData(true);
            }
        });
        loadData(true);
        return this.mRootView;
    }

    public PCData getSelectedItem() {
        if (this.mnSelectedIndex < 0 || this.mnSelectedIndex >= this.marrData.size()) {
            return null;
        }
        return (PCData) this.marrData.get(this.mnSelectedIndex);
    }

    private void reportMessage(String szMessage) {
        this.mTextViewMessage.setVisibility(0);
        this.mTextViewMessage.setText(szMessage);
        this.mLayout.setVisibility(4);
    }

    private void showContent() {
        if (this.marrData.size() > 0) {
            this.mTextViewMessage.setVisibility(4);
            this.mLayout.setVisibility(0);
            return;
        }
        this.mTextViewMessage.setText(Html.fromHtml("没有检测到已登录您账号的睿易通程序。<a href='reload'>点击这里</a>刷新。"));
        this.mTextViewMessage.setVisibility(0);
        this.mLayout.setVisibility(4);
        Utilities.runOnUIThreadDelay(getContext(), new Runnable() {
            public void run() {
                SelectPCFragment.this.loadData(false);
            }
        }, 1000);
    }

    private void loadData(boolean bShowLoadingMessage) {
        if (bShowLoadingMessage && this.marrData.size() == 0) {
            reportMessage("稍候...");
        }
        HttpItemObject itemObject = new HttpItemObject(MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/clients.json?filter=" + MyiBaseApplication.getCommonVariables().UserInfo.szUserName + ";", null);
        itemObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                if (ItemObject.readTextData() != null) {
                    SelectPCFragment.this.analysisJsonData(ItemObject.readTextData());
                }
                SelectPCFragment.this.showContent();
                SelectPCFragment.this.mListView.stopRefresh();
                SelectPCFragment.this.mListView.stopLoadMore();
                SelectPCFragment.this.mListView.setRefreshTime("刚刚");
            }
        });
        itemObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                SelectPCFragment.this.mTextViewMessage.setText(Html.fromHtml("加载数据时出现错误，错误信息：" + ItemObject.getErrorText() + "，<br><a href='reload'>点击这里</a>重试。"));
                SelectPCFragment.this.mTextViewMessage.setVisibility(0);
                SelectPCFragment.this.mLayout.setVisibility(4);
            }
        });
        itemObject.setReadOperation(true);
        itemObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(itemObject);
    }

    private void analysisJsonData(String szJsonData) {
        if (szJsonData.startsWith("null")) {
            this.marrData.clear();
            this.mListAdapterWrapper.notifyDataSetChanged();
            return;
        }
        try {
            JSONObject json = new JSONObject(szJsonData);
            ArrayList<PCData> arrNewData = new ArrayList();
            int nLastAvailableItem = -1;
            Iterator<String> iter = json.keys();
            while (iter.hasNext()) {
                String szJID = (String) iter.next();
                JSONArray oneStudent = json.getJSONArray(szJID);
                if (szJID.equalsIgnoreCase(MyiBaseApplication.getCommonVariables().UserInfo.szUserName)) {
                    for (int i = 0; i < oneStudent.length(); i++) {
                        JSONObject data = oneStudent.getJSONObject(i);
                        String szIP = data.getString("ip");
                        String szStatus = data.getString("status");
                        String szVersion = data.getString("client");
                        String szSessionID = data.getString("sessionid");
                        PCData oneData = new PCData();
                        oneData.szIP = szIP;
                        oneData.szSessionID = szSessionID;
                        oneData.szVersion = szVersion;
                        oneData.szStatus = szStatus;
                        if (oneData.szStatus.startsWith("idle")) {
                            oneData.bCanSelect = true;
                        } else if (oneData.szStatus.indexOf("inclass") != -1) {
                            if (checkStatus(oneData.szStatus, this.mszCurrentScheduleGUID)) {
                                oneData.bCanSelect = true;
                            } else {
                                oneData.bCanSelect = false;
                            }
                        }
                        if (oneData.bCanSelect) {
                            nLastAvailableItem = i;
                        }
                        if (data.has("Location")) {
                            oneData.szLocation = data.getString("Location");
                        }
                        arrNewData.add(oneData);
                    }
                }
            }
            this.marrData.clear();
            this.marrData.addAll(arrNewData);
            if (this.mnSelectedIndex == -1) {
                this.mnSelectedIndex = nLastAvailableItem;
            }
            this.mListAdapterWrapper.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getCount() {
        return this.marrData.size();
    }

    public Object getItem(int position) {
        return this.marrData.get(position);
    }

    public void setScheduleGUID(String szScheduleGUID) {
        this.mszCurrentScheduleGUID = szScheduleGUID;
    }

    public static boolean checkStatus(String szStatus, String szScheduleGUID) {
        if (szStatus.startsWith("idle")) {
            return true;
        }
        if (szStatus.indexOf("inclass") != -1) {
            String[] arrStatus = szStatus.split(" ");
            if (arrStatus.length > 2 && arrStatus[1].equalsIgnoreCase(szScheduleGUID)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkinClassStatus(String szStatus, String szScheduleGUID) {
        if (szStatus.indexOf("inclass") != -1) {
            String[] arrStatus = szStatus.split(" ");
            if (arrStatus.length > 2 && arrStatus[1].equalsIgnoreCase(szScheduleGUID)) {
                return true;
            }
        }
        return false;
    }

    public void getView(int position, View convertView) {
        PCData honourData = (PCData) this.marrData.get(position);
        TextView textViewTitle = (TextView) convertView.findViewById(R.id.TextViewTitle);
        TextView textViewDescription = (TextView) convertView.findViewById(R.id.textViewDescription);
        TextView textViewIP = (TextView) convertView.findViewById(R.id.textViewIP);
        ImageView imageViewThumbnail = (ImageView) convertView.findViewById(R.id.imageViewThumbnail);
        RadioButton radioButton = (RadioButton) convertView.findViewById(R.id.radioButtonSelected);
        boolean bCanUse = true;
        String szDescription = "";
        textViewTitle.setText(honourData.szVersion);
        imageViewThumbnail.setVisibility(8);
        if (honourData.szStatus.startsWith("idle")) {
            szDescription = "空闲，可以使用";
        } else if (honourData.szStatus.indexOf("inclass") != -1) {
            szDescription = "上课中";
            if (checkStatus(honourData.szStatus, this.mszCurrentScheduleGUID)) {
                szDescription = new StringBuilder(String.valueOf(szDescription)).append("，可以使用").toString();
            } else {
                szDescription = new StringBuilder(String.valueOf(szDescription)).append("，不能使用").toString();
                bCanUse = false;
            }
        }
        textViewDescription.setText(szDescription);
        textViewIP.setText(honourData.szVersion);
        if (honourData.szLocation != null) {
            textViewTitle.setText(honourData.szLocation);
        } else {
            textViewTitle.setText(honourData.szIP);
        }
        if (bCanUse) {
            radioButton.setEnabled(true);
            if (position == this.mnSelectedIndex) {
                radioButton.setChecked(true);
                return;
            } else {
                radioButton.setChecked(false);
                return;
            }
        }
        radioButton.setEnabled(false);
        radioButton.setChecked(false);
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        this.mnSelectedIndex = position - 1;
        this.mListAdapterWrapper.notifyDataSetChanged();
    }

    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        final PCData pcdata = (PCData) this.marrData.get(position - 1);
        String[] arrNames = new String[]{"退出该机器上的睿易通程序", "结束上课"};
        Builder dialogBuilder = new Builder(getActivity());
        dialogBuilder.setItems(arrNames, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final PCData pCData;
                if (which == 0) {
                    pCData = pcdata;
                    Utilities.showAlertMessage(SelectPCFragment.this.getContext(), "退出确认", "将要退出所选机器上的睿易通程序。\n\n是否继续？", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            IMService.getIMService().sendMessage(Utilities.getNow() + " " + IMService.getIMUserName() + ": " + String.format("Quit", new Object[0]), pCData.szSessionID);
                            Utilities.showAlertMessage(SelectPCFragment.this.getContext(), "操作成功", "退出命令已发出。");
                        }
                    }, null);
                } else if (which == 1) {
                    pCData = pcdata;
                    Utilities.showAlertMessage(SelectPCFragment.this.getContext(), "退出确认", "将要结束所选机器上的上课动作。\n\n是否继续？", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            IMService.getIMService().sendMessage(Utilities.getNow() + " " + IMService.getIMUserName() + ": " + String.format("EndClass", new Object[0]), pCData.szSessionID);
                            Utilities.showAlertMessage(SelectPCFragment.this.getContext(), "操作成功", "结束上课命令已发出。");
                        }
                    }, null);
                }
            }
        });
        dialogBuilder.setTitle("选择动作");
        dialogBuilder.setCancelable(true);
        dialogBuilder.show();
        return false;
    }

    public void onRefresh() {
        loadData(true);
    }

    public void onLoadMore() {
    }
}
