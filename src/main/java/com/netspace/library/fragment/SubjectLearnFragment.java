package com.netspace.library.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.netspace.library.activity.ResourceDetailActivity;
import com.netspace.library.activity.ResourcesViewActivity2;
import com.netspace.library.adapter.SubjectLearnAdapter;
import com.netspace.library.adapter.SubjectLearnAdapter.SubjectItem;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.PrivateDataItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SubjectLearnFragment extends Fragment implements OnItemClickListener {
    private boolean mDisplayMyLibrary = false;
    private ListView mDummyView;
    private OnSuccessListener mOnKPQuestionDataComplete = new OnSuccessListener() {
        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            if (ItemObject.getParam("0") != null && ItemObject.getParam("1") != null) {
                ArrayList<String> arrResourceGUID = (ArrayList) ItemObject.getParam("0");
                ArrayList<String> arrResourceName = (ArrayList) ItemObject.getParam("1");
                for (int i = 0; i < arrResourceGUID.size(); i++) {
                    SubjectItem OneItem = new SubjectItem();
                    OneItem.szItemName = (String) arrResourceName.get(i);
                    OneItem.szItemGUID = (String) arrResourceGUID.get(i);
                    OneItem.szParentGUID = (String) ItemObject.getPrivateObject();
                    OneItem.bIsQuestion = true;
                    SubjectLearnFragment.this.m_arrLastListViewData.add(OneItem);
                }
            }
        }
    };
    private OnSuccessListener mOnKPResourceDataComplete = new OnSuccessListener() {
        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            if (ItemObject.getParam("0") != null && ItemObject.getParam("1") != null) {
                ArrayList<String> arrResourceGUID = (ArrayList) ItemObject.getParam("0");
                ArrayList<String> arrResourceName = (ArrayList) ItemObject.getParam("1");
                for (int i = 0; i < arrResourceGUID.size(); i++) {
                    SubjectItem OneItem = new SubjectItem();
                    OneItem.szItemName = (String) arrResourceName.get(i);
                    OneItem.szItemGUID = (String) arrResourceGUID.get(i);
                    OneItem.szParentGUID = (String) ItemObject.getPrivateObject();
                    OneItem.bIsResource = true;
                    SubjectLearnFragment.this.m_arrLastListViewData.add(OneItem);
                }
                if (SubjectLearnFragment.this.mDisplayMyLibrary) {
                    WebServiceCallItemObject CallItem2 = new WebServiceCallItemObject("GetKPQuestions2", SubjectLearnFragment.this.getActivity(), SubjectLearnFragment.this.mOnKPQuestionDataComplete);
                    CallItem2.setParam("lpszKPGUID", (String) ItemObject.getPrivateObject());
                    CallItem2.setParam("nQuestionType", Integer.valueOf(-1));
                    CallItem2.setParam("bPrivateOnly", Integer.valueOf(0));
                    CallItem2.setPrivateObject((String) ItemObject.getParam("lpszParentKPGUID"));
                    if (SubjectLearnFragment.this.mDisplayMyLibrary) {
                        CallItem2.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
                    }
                    VirtualNetworkObject.addToQueue(CallItem2);
                    WebServiceCallItemObject CallItem = new WebServiceCallItemObject("GetKPResources2", SubjectLearnFragment.this.getActivity(), SubjectLearnFragment.this.mOnKPResourceDataComplete2);
                    CallItem.setParam("lpszKPGUID", (String) ItemObject.getPrivateObject());
                    CallItem.setParam("nResourceType", Integer.valueOf(-1));
                    CallItem.setPrivateObject((String) ItemObject.getParam("lpszParentKPGUID"));
                    if (SubjectLearnFragment.this.mDisplayMyLibrary) {
                        CallItem.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
                    }
                    VirtualNetworkObject.addToQueue(CallItem);
                    return;
                }
                SubjectLearnFragment.this.addOneListView(SubjectLearnFragment.this.m_arrLastListViewData);
            }
        }
    };
    private OnSuccessListener mOnKPResourceDataComplete2 = new OnSuccessListener() {
        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            if (ItemObject.getParam("0") != null && ItemObject.getParam("1") != null && ItemObject.getParam("2") != null) {
                ArrayList<String> arrResourceGUID = (ArrayList) ItemObject.getParam("0");
                ArrayList<String> arrResourceExt = (ArrayList) ItemObject.getParam("1");
                ArrayList<String> arrResourceType = (ArrayList) ItemObject.getParam("2");
                for (int i = 0; i < arrResourceGUID.size(); i++) {
                    String szGUID = (String) arrResourceGUID.get(i);
                    int j = 0;
                    while (j < SubjectLearnFragment.this.m_arrLastListViewData.size()) {
                        SubjectItem OneItem = (SubjectItem) SubjectLearnFragment.this.m_arrLastListViewData.get(j);
                        if (OneItem.szItemGUID.equalsIgnoreCase(szGUID)) {
                            OneItem.szItemExt = (String) arrResourceExt.get(i);
                            OneItem.nItemType = Integer.valueOf((String) arrResourceType.get(i)).intValue();
                            if (OneItem.nItemType == 0) {
                                OneItem.bIsQuestion = true;
                                OneItem.bIsResource = false;
                            } else {
                                OneItem.bIsQuestion = false;
                                OneItem.bIsResource = true;
                            }
                        } else {
                            j++;
                        }
                    }
                }
                SubjectLearnFragment.this.addOneListView(SubjectLearnFragment.this.m_arrLastListViewData);
            }
        }
    };
    private OnSuccessListener mOnMainDataComplete = new OnSuccessListener() {
        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            String szXML;
            if (SubjectLearnFragment.this.mDisplayMyLibrary) {
                szXML = (String) ItemObject.getParam("1");
            } else {
                szXML = ItemObject.readTextData();
            }
            ArrayList<SubjectItem> arrData = new ArrayList();
            try {
                NodeList arrPadDirectory;
                Element RootElement = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(szXML.getBytes(HTTP.UTF_8))).getDocumentElement();
                if (!SubjectLearnFragment.this.mDisplayMyLibrary) {
                    arrPadDirectory = RootElement.getElementsByTagName("PadDirectory");
                } else if (RootElement.getChildNodes().getLength() > 0) {
                    arrPadDirectory = RootElement.getChildNodes();
                } else {
                    SubjectLearnFragment.this.reportMessage("很抱歉，数据解析出现错误。");
                    return;
                }
                for (int i = 0; i < arrPadDirectory.getLength(); i++) {
                    String szKPGUID;
                    Node OneDirectory = arrPadDirectory.item(i);
                    String szKPName = OneDirectory.getAttributes().getNamedItem(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX).getNodeValue();
                    if (SubjectLearnFragment.this.mDisplayMyLibrary) {
                        szKPGUID = OneDirectory.getAttributes().getNamedItem("guid").getNodeValue();
                    } else {
                        szKPGUID = OneDirectory.getAttributes().getNamedItem("kpGUID").getNodeValue();
                    }
                    SubjectItem OneItem = new SubjectItem();
                    OneItem.szItemName = szKPName;
                    OneItem.szItemGUID = szKPGUID;
                    arrData.add(OneItem);
                }
                if (arrData.size() == 0) {
                    SubjectLearnFragment.this.reportMessage("很抱歉，当前并没有给您安排专题学习的内容");
                } else {
                    SubjectLearnFragment.this.addOneListView(arrData);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private OnSuccessListener mOnSubKPDataComplete = new OnSuccessListener() {
        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            String szXML = ItemObject.readTextData();
            SubjectLearnFragment.this.m_arrLastListViewData = new ArrayList();
            try {
                NodeList arrNodeList = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(szXML.getBytes(HTTP.UTF_8))).getDocumentElement().getElementsByTagName("Node");
                for (int i = 0; i < arrNodeList.getLength(); i++) {
                    Node OneNode = arrNodeList.item(i);
                    String szKPName = OneNode.getAttributes().getNamedItem(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX).getNodeValue();
                    String szKPGUID = OneNode.getAttributes().getNamedItem("guid").getNodeValue();
                    SubjectItem OneItem = new SubjectItem();
                    OneItem.szItemName = szKPName;
                    OneItem.szItemGUID = szKPGUID;
                    SubjectLearnFragment.this.m_arrLastListViewData.add(OneItem);
                }
                WebServiceCallItemObject CallItem = new WebServiceCallItemObject("GetKPResources", SubjectLearnFragment.this.getActivity(), SubjectLearnFragment.this.mOnKPResourceDataComplete);
                CallItem.setParam("lpszKPGUID", (String) ItemObject.getParam("lpszParentKPGUID"));
                if (SubjectLearnFragment.this.mDisplayMyLibrary) {
                    CallItem.setParam("nResourceType", Integer.valueOf(-1));
                } else {
                    CallItem.setParam("nResourceType", Integer.valueOf(1000));
                }
                CallItem.setPrivateObject((String) ItemObject.getParam("lpszParentKPGUID"));
                if (SubjectLearnFragment.this.mDisplayMyLibrary) {
                    CallItem.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
                }
                VirtualNetworkObject.addToQueue(CallItem);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private View mRootView;
    private NestedScrollView mScrollView;
    private TextView mTextViewMessage;
    private LinearLayout m_RootLayout;
    private ArrayList<SubjectItem> m_arrLastListViewData;
    private String mszOwnerGUID = "";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mRootView != null) {
            return this.mRootView;
        }
        this.mRootView = inflater.inflate(R.layout.fragment_subjectlearn, container, false);
        this.m_RootLayout = (LinearLayout) this.mRootView.findViewById(R.id.LinearLayout1);
        this.mScrollView = (NestedScrollView) this.mRootView.findViewById(R.id.ScrollView1);
        this.mTextViewMessage = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        this.mTextViewMessage.setVisibility(4);
        if (VirtualNetworkObject.getOfflineMode()) {
            reportMessage("很抱歉，此功能当前无法在离线模式下使用。");
        } else if (this.mDisplayMyLibrary) {
            WebServiceCallItemObject CallItem = new WebServiceCallItemObject("GetPrivateKP", getActivity());
            CallItem.setSuccessListener(new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    ArrayList<String> arrGUID = (ArrayList) ItemObject.getParam("0");
                    WebServiceCallItemObject CallItem2 = new WebServiceCallItemObject("GetCatalogsByGUID", SubjectLearnFragment.this.getActivity(), SubjectLearnFragment.this.mOnMainDataComplete);
                    CallItem2.setParam("arrKPGUIDs", arrGUID);
                    if (SubjectLearnFragment.this.mDisplayMyLibrary) {
                        CallItem2.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
                    }
                    VirtualNetworkObject.addToQueue(CallItem2);
                }
            });
            CallItem.setParam("nGrade", Integer.valueOf(-1));
            CallItem.setParam("nSubject", Integer.valueOf(-1));
            CallItem.setParam("szKeyWords", "");
            if (this.mDisplayMyLibrary) {
                CallItem.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
            }
            VirtualNetworkObject.addToQueue(CallItem);
        } else {
            VirtualNetworkObject.addToQueue(new PrivateDataItemObject("StudentPadDirectory_SubjectLearn", getActivity(), this.mOnMainDataComplete));
        }
        return this.mRootView;
    }

    public void setOpenOwnerLibrary(boolean bEnable, String szOwnerGUID) {
        this.mDisplayMyLibrary = bEnable;
        this.mszOwnerGUID = szOwnerGUID;
    }

    private void reportMessage(String szMessage) {
        this.mTextViewMessage.setVisibility(0);
        this.mTextViewMessage.setText(szMessage);
        this.mScrollView.setVisibility(4);
    }

    public void addOneListView(ArrayList<SubjectItem> arrData) {
        this.m_RootLayout.removeView(this.mDummyView);
        ListView ListView = new ListView(getActivity());
        SubjectLearnAdapter Adapter = new SubjectLearnAdapter(getActivity(), arrData);
        ListView.setChoiceMode(1);
        ListView.setSelector(R.drawable.listview_default_selector);
        ListView.setVisibility(4);
        this.m_RootLayout.addView(ListView);
        ListView.setAdapter(Adapter);
        ListView.getLayoutParams().width = Utilities.dpToPixel(350, getActivity());
        ListView.setOnItemClickListener(this);
        View View = new View(getActivity());
        View.setBackgroundColor(-5197646);
        this.m_RootLayout.addView(View);
        LayoutParams Param = View.getLayoutParams();
        Param.width = 1;
        Param.height = -1;
        Utilities.sliderFromLeftToRight(ListView, HttpStatus.SC_MULTIPLE_CHOICES);
        this.mScrollView.postDelayed(new Runnable() {
            public void run() {
                SubjectLearnFragment.this.mScrollView.scrollTo(SubjectLearnFragment.this.mScrollView.getRight(), 0);
            }
        }, 100);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListView ListView = (ListView) parent;
        SubjectItem SelectedItem = (SubjectItem) ListView.getAdapter().getItem(position);
        ListView.setItemChecked(position, true);
        Intent Intent;
        if (SelectedItem.bIsResource) {
            if (SelectedItem.nItemType == 0 || (SelectedItem.nItemType >= 1000 && SelectedItem.nItemType < 2000)) {
                Intent = new Intent(getActivity(), ResourcesViewActivity2.class);
                Intent.putExtra(CommentComponent.RESOURCEGUID, SelectedItem.szItemGUID);
                Intent.putExtra("scheduleguid", SelectedItem.szItemGUID);
                Intent.putExtra("displayanswers", true);
                Intent.putExtra("enableunlock", true);
                Intent.putExtra(UserHonourFragment.USERCLASSGUID, MyiBaseApplication.getCommonVariables().UserInfo.getFirstClassGUID());
                Intent.putExtra("userclassname", MyiBaseApplication.getCommonVariables().UserInfo.getFirstClassName());
                startActivity(Intent);
                return;
            }
            Intent = new Intent(getActivity(), ResourceDetailActivity.class);
            Intent.putExtra(CommentComponent.RESOURCEGUID, SelectedItem.szItemGUID);
            Intent.putExtra("title", SelectedItem.szItemName);
            Intent.putExtra("isquestion", false);
            Intent.putExtra("resourcetype", SelectedItem.nItemType);
            startActivity(Intent);
        } else if (SelectedItem.bIsQuestion) {
            Intent = new Intent(getActivity(), ResourceDetailActivity.class);
            Intent.putExtra(CommentComponent.RESOURCEGUID, SelectedItem.szItemGUID);
            Intent.putExtra("title", SelectedItem.szItemName);
            Intent.putExtra("isquestion", true);
            Intent.putExtra("resourcetype", SelectedItem.nItemType);
            startActivity(Intent);
        } else {
            int nDelStartIndex = -1;
            for (int i = 0; i < this.m_RootLayout.getChildCount(); i++) {
                if (this.m_RootLayout.getChildAt(i).equals(parent)) {
                    nDelStartIndex = i + 2;
                    break;
                }
            }
            if (nDelStartIndex != -1) {
                while (this.m_RootLayout.getChildAt(nDelStartIndex) != null) {
                    this.m_RootLayout.removeViewAt(nDelStartIndex);
                }
            }
            this.mDummyView = new ListView(getActivity());
            this.m_RootLayout.addView(this.mDummyView);
            this.mDummyView.getLayoutParams().width = Utilities.dpToPixel(350, getActivity());
            WebServiceCallItemObject CallItem = new WebServiceCallItemObject("GetOneLevelCatalogs", getActivity(), this.mOnSubKPDataComplete);
            CallItem.setParam("lpszParentKPGUID", SelectedItem.szItemGUID);
            if (this.mDisplayMyLibrary) {
                CallItem.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
            }
            VirtualNetworkObject.addToQueue(CallItem);
        }
    }
}
