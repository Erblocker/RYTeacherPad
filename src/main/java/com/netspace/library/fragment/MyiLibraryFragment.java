package com.netspace.library.fragment;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.media.TransportMediator;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.activity.ExecuteCommandActivity;
import com.netspace.library.activity.ExecuteCommandActivity.ExecuteCommandInterface;
import com.netspace.library.activity.FingerDrawActivity;
import com.netspace.library.activity.FingerDrawActivity.FingerDrawCallbackInterface;
import com.netspace.library.activity.ResourceDetailActivity;
import com.netspace.library.activity.ResourcesViewActivity2;
import com.netspace.library.adapter.MyiLibraryAdapter;
import com.netspace.library.adapter.SubjectLearnAdapter.SubjectItem;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.ChatComponent;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.consts.Features;
import com.netspace.library.dialog.SelectFolderDialog;
import com.netspace.library.dialog.SelectFolderDialog.SelectFolderDialogCallBack;
import com.netspace.library.dialog.ShareToDialog;
import com.netspace.library.dialog.ShareToDialog.ShareDialogCallBack;
import com.netspace.library.parser.ResourceParser;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.struct.UserClassInfo;
import com.netspace.library.utilities.ResourceUploadProcess;
import com.netspace.library.utilities.ResourceUploadProcess.ResourceUploadInterface;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.PrivateDataItemObject;
import com.netspace.library.virtualnetworkobject.ResourceItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.library.wrapper.CameraRecordActivity;
import com.netspace.library.wrapper.CameraRecordActivity.CameraRecordCallBack;
import com.netspace.pad.library.R;
import io.vov.vitamio.utils.Log;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sqlcipher.database.SQLiteDatabase;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MyiLibraryFragment extends Fragment implements OnClickListener {
    private static final String TAG = "MyiLibraryFragment";
    protected static String mszClientID = "";
    protected static String mszRealName = "";
    protected MyiLibraryAdapter mAdapter;
    protected MyiLibraryCallBackListener mCallBack;
    protected HashMap<String, String> mClassRootKPPath = new HashMap();
    protected String mCurrentKPGUID = "";
    protected String mCurrentKPPath = "";
    protected FingerDrawCallbackInterface mDrawCallBackInterface = new FingerDrawCallbackInterface() {
        public void OnFingerDrawCreate(Activity Activity) {
        }

        public boolean HasMJpegClients() {
            return false;
        }

        public void OnUpdateMJpegImage(Bitmap bitmap, Activity Activity) {
        }

        public void OnProject(Activity Activity) {
        }

        public void OnDestroy(Activity Activity) {
        }

        public void OnBroadcast(Activity Activity) {
        }

        public void OnPenAction(String szAction, float fX, float fY, int nWidth, int nHeight, Activity Activity) {
        }

        public void OnOK(Bitmap bitmap, String szUploadName, final Activity Activity) {
            String szFileName = new StringBuilder(String.valueOf(MyiLibraryFragment.this.mszUploadPath)).append("DrawPicture_").append(Utilities.createGUID()).append(".jpg").toString();
            Utilities.saveBitmapToJpeg(szFileName, bitmap);
            ResourceUploadProcess UploadProcess = new ResourceUploadProcess(MyiLibraryFragment.this.getActivity(), MyiLibraryFragment.this.mszUploadPath, MyiLibraryFragment.mszRealName, MyiLibraryFragment.this.mCurrentKPPath, MyiLibraryFragment.this.mCurrentKPGUID);
            UploadProcess.setType("绘图");
            UploadProcess.setCallBack(new ResourceUploadInterface() {
                public void onBeginUpload() {
                    Activity.finish();
                }

                public void onUploadComplete() {
                    MyiLibraryFragment.this.refresh();
                }

                public void onCancel() {
                }
            });
            UploadProcess.startUploadProcess(szFileName);
        }
    };
    protected TextView mNoDataView;
    protected OnSuccessListener mOnGetCatalogsByGUIDCallBack = new OnSuccessListener() {
        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            try {
                Element RootElement = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(((String) ItemObject.getParam("1")).getBytes(HTTP.UTF_8))).getDocumentElement();
                if (RootElement.getChildNodes().getLength() > 0) {
                    NodeList arrPadDirectory = RootElement.getChildNodes();
                    for (int i = 0; i < arrPadDirectory.getLength(); i++) {
                        Node OneDirectory = arrPadDirectory.item(i);
                        String szKPName = OneDirectory.getAttributes().getNamedItem(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX).getNodeValue();
                        String szKPGUID = OneDirectory.getAttributes().getNamedItem("guid").getNodeValue();
                        ResourceItemData OneItem = new ResourceItemData();
                        OneItem.szTitle = szKPName;
                        OneItem.szGUID = szKPGUID;
                        OneItem.bFolder = true;
                        MyiLibraryFragment.this.marrData.add(OneItem);
                    }
                    MyiLibraryFragment.this.mAdapter.notifyDataSetChanged();
                    return;
                }
                MyiLibraryFragment.this.reportMessage("很抱歉，数据解析出现错误。");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    protected OnSuccessListener mOnKPQuestionDataComplete = new OnSuccessListener() {
        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            if (ItemObject.getParam("0") != null && ItemObject.getParam("1") != null) {
                ArrayList<String> arrResourceGUID = (ArrayList) ItemObject.getParam("0");
                ArrayList<String> arrResourceName = (ArrayList) ItemObject.getParam("1");
                for (int i = 0; i < arrResourceGUID.size(); i++) {
                    ResourceItemData OneItem = new ResourceItemData();
                    OneItem.szTitle = (String) arrResourceName.get(i);
                    OneItem.szGUID = (String) arrResourceGUID.get(i);
                    OneItem.nType = 0;
                    MyiLibraryFragment.this.marrData.add(OneItem);
                }
                MyiLibraryFragment.this.mAdapter.notifyDataSetChanged();
            }
        }
    };
    protected OnSuccessListener mOnKPResourceDataComplete = new OnSuccessListener() {
        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            if (ItemObject.getParam("0") != null && ItemObject.getParam("1") != null && ItemObject.getParam("2") != null && ItemObject.getParam("3") != null && ItemObject.getParam("4") != null && ItemObject.getParam("5") != null) {
                ArrayList<String> arrResourceGUID = (ArrayList) ItemObject.getParam("0");
                ArrayList<String> arrResourceTitle = (ArrayList) ItemObject.getParam("1");
                ArrayList<String> arrResourceExt = (ArrayList) ItemObject.getParam("2");
                ArrayList<String> arrResourceType = (ArrayList) ItemObject.getParam("3");
                ArrayList<String> arrResourceDate = (ArrayList) ItemObject.getParam("4");
                ArrayList<String> arrResourceAuthor = (ArrayList) ItemObject.getParam("5");
                for (int i = 0; i < arrResourceGUID.size(); i++) {
                    String szGUID = (String) arrResourceGUID.get(i);
                    ResourceItemData OneItem = new ResourceItemData();
                    OneItem.szFileType = (String) arrResourceExt.get(i);
                    OneItem.nType = Integer.valueOf((String) arrResourceType.get(i)).intValue();
                    OneItem.szGUID = szGUID;
                    OneItem.szTitle = (String) arrResourceTitle.get(i);
                    OneItem.szDateTime = (String) arrResourceDate.get(i);
                    OneItem.szAuthor = (String) arrResourceAuthor.get(i);
                    MyiLibraryFragment.this.marrData.add(OneItem);
                }
                MyiLibraryFragment.this.mAdapter.notifyDataSetChanged();
            }
        }
    };
    protected OnSuccessListener mOnSubKPDataComplete = new OnSuccessListener() {
        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            try {
                NodeList arrNodeList = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(ItemObject.readTextData().getBytes(HTTP.UTF_8))).getDocumentElement().getElementsByTagName("Node");
                String szParentKPGUID = (String) ItemObject.getParam("lpszParentKPGUID");
                MyiLibraryFragment.this.mCurrentKPGUID = szParentKPGUID;
                MyiLibraryFragment.this.marrData.clear();
                if (!szParentKPGUID.equalsIgnoreCase(MyiLibraryFragment.this.mRootKPGUID)) {
                    ResourceItemData backItem = new ResourceItemData();
                    backItem.bFolder = true;
                    backItem.szGUID = "back";
                    backItem.szTitle = "返回上一级";
                    MyiLibraryFragment.this.marrData.add(backItem);
                } else if (!MyiLibraryFragment.this.mbClassShareFolderOnly) {
                    Iterator it = MyiBaseApplication.getCommonVariables().UserInfo.arrClasses.iterator();
                    while (it.hasNext()) {
                        UserClassInfo oneClass = (UserClassInfo) it.next();
                        ResourceItemData shareItem = new ResourceItemData();
                        shareItem.bLocked = true;
                        shareItem.bFolder = true;
                        shareItem.szGUID = (String) MyiLibraryFragment.this.mClassRootKPPath.get(oneClass.szClassGUID);
                        shareItem.szTitle = oneClass.szClassName + "专区";
                        if (shareItem.szGUID != null) {
                            MyiLibraryFragment.this.marrData.add(shareItem);
                        }
                    }
                }
                if (MyiLibraryFragment.this.mShareKPGUID.containsValue(MyiLibraryFragment.this.mCurrentKPGUID) || MyiLibraryFragment.this.mShowcaseKPGUID.containsValue(MyiLibraryFragment.this.mCurrentKPGUID) || MyiLibraryFragment.this.mClassRootKPPath.containsValue(MyiLibraryFragment.this.mCurrentKPGUID)) {
                    MyiLibraryFragment.this.mAdapter.setReadOnly(true);
                } else {
                    MyiLibraryFragment.this.mAdapter.setReadOnly(false);
                    if (!(MyiLibraryFragment.this.mCurrentKPPath == null || MyiLibraryFragment.this.mCurrentKPPath.indexOf(":Class_") == -1 || MyiLibraryFragment.this.mCurrentKPGUID.equalsIgnoreCase(MyiLibraryFragment.this.mRootKPGUID))) {
                        MyiLibraryFragment.this.mAdapter.setReadOnly(true);
                    }
                }
                if (MyiLibraryFragment.this.mbReadOnly) {
                    MyiLibraryFragment.this.mAdapter.setReadOnly(true);
                }
                if (false && MyiLibraryFragment.this.mbClassShareFolderOnly) {
                    MyiLibraryFragment.this.mAdapter.setReadOnly(true);
                } else {
                    for (int i = 0; i < arrNodeList.getLength(); i++) {
                        Node OneNode = arrNodeList.item(i);
                        boolean bAddThisFolder = true;
                        String szKPName = OneNode.getAttributes().getNamedItem(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX).getNodeValue();
                        String szKPName2 = szKPName;
                        String szKPGUID = OneNode.getAttributes().getNamedItem("guid").getNodeValue();
                        int nPos = szKPName.indexOf(":Class_");
                        if (nPos != -1) {
                            szKPName = szKPName.substring(0, nPos);
                        }
                        if (nPos != -1 && MyiLibraryFragment.this.mbLoadPersonalDataOnly) {
                            bAddThisFolder = MyiBaseApplication.getCommonVariables().UserInfo.isInClass(szKPName2.substring(nPos + 7, szKPName2.indexOf("_", nPos + 7)));
                        }
                        if (bAddThisFolder) {
                            ResourceItemData OneItem = new ResourceItemData();
                            OneItem.szTitle = szKPName;
                            OneItem.szGUID = szKPGUID;
                            OneItem.bFolder = true;
                            if (OneItem.szGUID != null) {
                                MyiLibraryFragment.this.marrData.add(OneItem);
                            }
                        }
                    }
                }
                MyiLibraryFragment.this.mAdapter.notifyDataSetChanged();
                MyiLibraryFragment.this.mCurrentKPPath = "";
                WebServiceCallItemObject CallItem3 = new WebServiceCallItemObject("GetKPPathText", MyiLibraryFragment.this.getActivity(), new OnSuccessListener() {
                    public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                        MyiLibraryFragment.this.mCurrentKPPath = ItemObject.readTextData();
                    }
                });
                CallItem3.setParam("lpszKPNodeGUID", MyiLibraryFragment.this.mCurrentKPGUID);
                VirtualNetworkObject.addToQueue(CallItem3);
                if (!MyiLibraryFragment.this.mbClassShareFolderOnly) {
                    WebServiceCallItemObject CallItem = new WebServiceCallItemObject("GetKPResources3", MyiLibraryFragment.this.getActivity(), MyiLibraryFragment.this.mOnKPResourceDataComplete);
                    CallItem.setParam("lpszKPGUID", MyiLibraryFragment.this.mCurrentKPGUID);
                    CallItem.setParam("nResourceType", Integer.valueOf(-1));
                    CallItem.setPrivateObject((String) ItemObject.getParam("lpszParentKPGUID"));
                    if (MyiLibraryFragment.this.mbLoadPersonalDataOnly) {
                        CallItem.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
                    }
                    VirtualNetworkObject.addToQueue(CallItem);
                    WebServiceCallItemObject CallItem2 = new WebServiceCallItemObject("GetKPQuestions2", MyiLibraryFragment.this.getActivity(), MyiLibraryFragment.this.mOnKPQuestionDataComplete);
                    CallItem2.setParam("lpszKPGUID", MyiLibraryFragment.this.mCurrentKPGUID);
                    CallItem2.setParam("nQuestionType", Integer.valueOf(-1));
                    CallItem2.setParam("bPrivateOnly", Integer.valueOf(0));
                    CallItem2.setPrivateObject((String) ItemObject.getParam("lpszParentKPGUID"));
                    if (MyiLibraryFragment.this.mbLoadPersonalDataOnly) {
                        CallItem2.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
                    }
                    VirtualNetworkObject.addToQueue(CallItem2);
                }
                MyiLibraryFragment.this.getActivity().invalidateOptionsMenu();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    protected String mPersonalKPGUID;
    protected String mPersonalKPPath;
    protected RecyclerView mRecyclerView;
    protected String mRootKPGUID = "";
    protected String mRootKPPath = "";
    protected View mRootView;
    protected HashMap<String, String> mShareKPGUID = new HashMap();
    protected HashMap<String, String> mShowcaseKPGUID = new HashMap();
    protected HashMap<String, String> mShowcaseKPPath = new HashMap();
    protected ArrayList<ResourceItemData> marrData = new ArrayList();
    protected ArrayList<String> marrKPPath = new ArrayList();
    protected boolean mbClassShareFolderOnly = false;
    protected boolean mbLoadPersonalDataOnly = false;
    protected boolean mbReadOnly = false;
    protected String mszOwnerGUID = "";
    protected String mszUploadPath;

    public interface MyiLibraryCallBackListener {
        boolean OnQuestionClick(ResourceItemData resourceItemData);

        boolean OnResourceClick(ResourceItemData resourceItemData);
    }

    public static void setBasicInfo(String szRealName, String szClientID) {
        mszClientID = szClientID;
        mszRealName = szRealName;
        ResourceUploadProcess.setClientID(mszClientID);
    }

    private static void setResourcePermission(String szResourceGUID) {
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("SetObjectAccessRights", null, new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            }
        });
        CallItem.setParam("nObjectType", Integer.valueOf(4));
        CallItem.setParam("lpszSearchSQL", " where guid='" + szResourceGUID + "'");
        CallItem.setParam("nRights", Integer.valueOf(TransportMediator.KEYCODE_MEDIA_PAUSE));
        CallItem.setParam("szGroupGUID", mszClientID);
        CallItem.setParam("szOwnerGUID", mszClientID);
        CallItem.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(CallItem);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        if (this.mRootView != null) {
            return this.mRootView;
        }
        this.mRootView = inflater.inflate(R.layout.fragment_myilibrary, null);
        this.mRecyclerView = (RecyclerView) this.mRootView.findViewById(R.id.recentView);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        this.mNoDataView = (TextView) this.mRootView.findViewById(R.id.textViewNoData);
        this.mNoDataView.setVisibility(4);
        this.mAdapter = new MyiLibraryAdapter(getActivity(), this.marrData);
        this.mAdapter.setOnClickListener(this);
        this.mRecyclerView.setAdapter(this.mAdapter);
        this.mAdapter.setReadOnly(this.mbReadOnly);
        this.mszUploadPath = Utilities.getUploadPath(getActivity());
        getSystemPersonalKP();
        ExecuteCommandActivity.setCallBack(new ExecuteCommandInterface() {
            public void executeCommand(Intent intent) {
                if (intent.getAction().equalsIgnoreCase("startscreenrecordupload")) {
                    String szRealName = intent.getStringExtra("realname");
                    String szKPPath = intent.getStringExtra("kppath");
                    String szKPGUID = intent.getStringExtra("kpguid");
                    new ResourceUploadProcess(MyiLibraryFragment.this.getActivity(), MyiLibraryFragment.this.mszUploadPath, szRealName, szKPPath, szKPGUID).startUpload(intent.getStringExtra("uploadfilename"), intent.getStringExtra("xmlfilename"));
                }
            }
        });
        return this.mRootView;
    }

    public void setReadOnly(boolean bReadOnly) {
        this.mbReadOnly = bReadOnly;
        if (this.mAdapter != null) {
            this.mAdapter.setReadOnly(this.mbReadOnly);
        }
    }

    protected void setKPPermission(String szKPGUID) {
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("SetObjectAccessRights", getActivity(), new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            }
        });
        CallItem.setParam("nObjectType", Integer.valueOf(0));
        if (szKPGUID == null) {
            CallItem.setParam("lpszSearchSQL", " where guid='" + this.mPersonalKPGUID + "'");
        } else if (!szKPGUID.isEmpty()) {
            CallItem.setParam("lpszSearchSQL", " where guid='" + szKPGUID + "'");
        }
        CallItem.setParam("nRights", Integer.valueOf(TransportMediator.KEYCODE_MEDIA_PAUSE));
        CallItem.setParam("szGroupGUID", mszClientID);
        CallItem.setParam("szOwnerGUID", mszClientID);
        VirtualNetworkObject.addToQueue(CallItem);
    }

    protected void buildStudentPersonalKP() {
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("SearchAndCreateKPFromCurrent", getActivity(), new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                if (!MyiLibraryFragment.this.mbClassShareFolderOnly) {
                    MyiLibraryFragment.this.mPersonalKPGUID = ItemObject.readTextData();
                    MyiLibraryFragment.this.mPersonalKPPath = new StringBuilder(String.valueOf(MyiLibraryFragment.this.mRootKPPath)).append("\\").append(MyiLibraryFragment.mszRealName).append(":").append(MyiLibraryFragment.mszClientID).toString();
                }
                MyiLibraryFragment.this.mRootKPGUID = MyiLibraryFragment.this.mPersonalKPGUID;
                MyiLibraryFragment.this.setKPPermission(null);
                MyiLibraryFragment.this.initRootKP();
            }
        });
        CallItem.setParam("lpszKPName", mszRealName + ":" + mszClientID);
        CallItem.setParam("lpszParentKPGUID", this.mRootKPGUID);
        VirtualNetworkObject.addToQueue(CallItem);
    }

    public void setCallBack(MyiLibraryCallBackListener callBack) {
        this.mCallBack = callBack;
    }

    private void addKP(String szKPName) {
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("AddKnowledgePoint", getActivity(), new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                MyiLibraryFragment.this.setKPPermission(ItemObject.readTextData());
                MyiLibraryFragment.this.refresh();
            }
        });
        CallItem.setParam("lpszKPName", szKPName);
        CallItem.setParam("nKPType", Integer.valueOf(3));
        CallItem.setParam("nGrade", Integer.valueOf(-1));
        CallItem.setParam("nSubject", Integer.valueOf(-1));
        CallItem.setParam("nBookVersion", Integer.valueOf(-1));
        CallItem.setParam("nDisplayIndex", Integer.valueOf(0));
        CallItem.setParam("lpszParentKPGUID", this.mCurrentKPGUID);
        CallItem.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
        VirtualNetworkObject.addToQueue(CallItem);
    }

    public String getCurrentKPGUID() {
        return this.mCurrentKPGUID;
    }

    public String getCurrentKPPath() {
        return this.mCurrentKPPath;
    }

    private void renameKP(String szKPGUID, final String szKPName, final ResourceItemData data) {
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("RenameKnowledegePoint", getActivity(), new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                data.szTitle = szKPName;
                MyiLibraryFragment.this.mAdapter.notifyDataSetChanged();
            }
        });
        CallItem.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                Utilities.showAlertMessage(MyiLibraryFragment.this.getActivity(), "出现错误", "无法重命名选定的内容，请检查您是否有权限。");
            }
        });
        CallItem.setParam("lpszNewName", szKPName);
        CallItem.setParam("lpszKPGUID", szKPGUID);
        CallItem.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
        VirtualNetworkObject.addToQueue(CallItem);
    }

    private void addPaint() {
        int nImageWidth = Utilities.getScreenWidth(getActivity());
        int nImageHeight = Utilities.getScreenHeight(getActivity());
        String szDrawPadKey = "DrawPad_" + Utilities.createGUID() + ".jpg";
        Intent DrawActivity = new Intent(getActivity(), FingerDrawActivity.class);
        FingerDrawActivity.SetCallbackInterface(this.mDrawCallBackInterface);
        DrawActivity.putExtra("imageWidth", nImageWidth);
        DrawActivity.putExtra("imageHeight", nImageHeight);
        DrawActivity.putExtra("imageData", "");
        DrawActivity.putExtra("allowUpload", true);
        DrawActivity.putExtra("allowCamera", true);
        DrawActivity.putExtra("uploadName", szDrawPadKey);
        DrawActivity.putExtra("enableBackButton", true);
        DrawActivity.setFlags(335544320);
        startActivity(DrawActivity);
    }

    protected void getSystemPersonalKP() {
        VirtualNetworkObject.addToQueue(new PrivateDataItemObject("StudentPad", getActivity(), new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                String szXML = ItemObject.readTextData();
                ArrayList<SubjectItem> arrData = new ArrayList();
                try {
                    NodeList arrPadDirectory = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(szXML.getBytes(HTTP.UTF_8))).getDocumentElement().getElementsByTagName("PadDirectory");
                    for (int i = 0; i < arrPadDirectory.getLength(); i++) {
                        Node OneDirectory = arrPadDirectory.item(i);
                        String szClassGUID = "";
                        String szKPName = OneDirectory.getAttributes().getNamedItem(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX).getNodeValue();
                        String szKPGUID = OneDirectory.getAttributes().getNamedItem("kpGUID").getNodeValue();
                        String szKPFullPath = OneDirectory.getAttributes().getNamedItem("kpFullPath").getNodeValue();
                        if (szKPName.equalsIgnoreCase("学生平板")) {
                            MyiLibraryFragment.this.mRootKPGUID = szKPGUID;
                        }
                        int nPos = szKPName.indexOf("Class_");
                        if (nPos != -1) {
                            nPos += 6;
                            szClassGUID = szKPName.substring(nPos, szKPName.indexOf("_", nPos));
                            Iterator it = MyiBaseApplication.getCommonVariables().UserInfo.arrClasses.iterator();
                            while (it.hasNext()) {
                                UserClassInfo oneClass = (UserClassInfo) it.next();
                                if (oneClass.szClassGUID.equalsIgnoreCase(szClassGUID)) {
                                    if (szKPName.indexOf("_Root") != -1) {
                                        MyiLibraryFragment.this.mClassRootKPPath.put(oneClass.szClassGUID, szKPGUID);
                                    } else if (szKPName.indexOf("_Resources") != -1) {
                                        MyiLibraryFragment.this.mShareKPGUID.put(oneClass.szClassGUID, szKPGUID);
                                    } else if (szKPName.indexOf("_Showcase") != -1) {
                                        MyiLibraryFragment.this.mShowcaseKPGUID.put(oneClass.szClassGUID, szKPGUID);
                                        MyiLibraryFragment.this.mShowcaseKPPath.put(oneClass.szClassGUID, szKPFullPath);
                                    }
                                }
                            }
                            continue;
                        }
                    }
                    MyiLibraryFragment.this.buildStudentPersonalKP();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    public boolean goBack() {
        if (this.marrKPPath.size() <= 0) {
            return false;
        }
        String szLastKPGUID = (String) this.marrKPPath.get(this.marrKPPath.size() - 1);
        this.marrKPPath.remove(this.marrKPPath.size() - 1);
        if (szLastKPGUID.isEmpty()) {
            initRootKP();
        } else {
            WebServiceCallItemObject CallItem = new WebServiceCallItemObject("GetOneLevelCatalogs", getActivity(), this.mOnSubKPDataComplete);
            CallItem.setParam("lpszParentKPGUID", szLastKPGUID);
            if (this.mbLoadPersonalDataOnly) {
                CallItem.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
            }
            VirtualNetworkObject.addToQueue(CallItem);
        }
        return true;
    }

    protected void initRootKP() {
        this.marrData.clear();
        Log.d(TAG, "initRootKP");
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("GetOneLevelCatalogs", getActivity(), this.mOnSubKPDataComplete);
        CallItem.setParam("lpszParentKPGUID", this.mPersonalKPGUID);
        if (this.mbLoadPersonalDataOnly) {
            CallItem.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
        }
        VirtualNetworkObject.addToQueue(CallItem);
    }

    private void reportMessage(String szMessage) {
        this.mNoDataView.setVisibility(0);
        this.mNoDataView.setText(szMessage);
    }

    public void onClick(View v) {
        final ResourceItemData data = (ResourceItemData) v.getTag();
        if (v.getId() == R.id.cardViewResource) {
            if (!data.bFolder) {
                if (this.mCallBack != null) {
                    if (data.nType != 0) {
                        if (this.mCallBack.OnResourceClick(data)) {
                            return;
                        }
                    } else if (this.mCallBack.OnQuestionClick(data)) {
                        return;
                    }
                }
                Intent Intent;
                if (data.nType == 0) {
                    Intent = new Intent(getActivity(), ResourceDetailActivity.class);
                    Intent.putExtra(CommentComponent.RESOURCEGUID, data.szGUID);
                    Intent.putExtra("title", data.szTitle);
                    Intent.putExtra("isquestion", true);
                    Intent.putExtra("resourcetype", data.nType);
                    startActivity(Intent);
                    return;
                } else if (data.nType < 1000 || data.nType >= 2000) {
                    Intent = new Intent(getActivity(), ResourceDetailActivity.class);
                    Intent.putExtra(CommentComponent.RESOURCEGUID, data.szGUID);
                    Intent.putExtra("title", data.szTitle);
                    Intent.putExtra("isquestion", false);
                    Intent.putExtra("resourcetype", data.nType);
                    startActivity(Intent);
                    return;
                } else {
                    Intent = new Intent(getActivity(), ResourcesViewActivity2.class);
                    Intent.putExtra(CommentComponent.RESOURCEGUID, data.szGUID);
                    Intent.putExtra("scheduleguid", data.szGUID);
                    Intent.putExtra("displayanswers", true);
                    Intent.putExtra("enableunlock", true);
                    Intent.putExtra(UserHonourFragment.USERCLASSGUID, "");
                    Intent.putExtra("userclassname", "");
                    Intent.putExtra("resourcetype", data.nType);
                    startActivity(Intent);
                    return;
                }
            } else if (!data.szGUID.equalsIgnoreCase("back")) {
                CallItem = new WebServiceCallItemObject("GetOneLevelCatalogs", getActivity(), this.mOnSubKPDataComplete);
                CallItem.setParam("lpszParentKPGUID", data.szGUID);
                this.marrKPPath.add(this.mCurrentKPGUID);
                if (this.mbLoadPersonalDataOnly) {
                    CallItem.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
                }
                VirtualNetworkObject.addToQueue(CallItem);
            } else if (this.marrKPPath.size() > 0) {
                String szLastKPGUID = (String) this.marrKPPath.get(this.marrKPPath.size() - 1);
                this.marrKPPath.remove(this.marrKPPath.size() - 1);
                if (szLastKPGUID.isEmpty()) {
                    initRootKP();
                } else {
                    CallItem = new WebServiceCallItemObject("GetOneLevelCatalogs", getActivity(), this.mOnSubKPDataComplete);
                    CallItem.setParam("lpszParentKPGUID", szLastKPGUID);
                    if (this.mbLoadPersonalDataOnly) {
                        CallItem.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
                    }
                    VirtualNetworkObject.addToQueue(CallItem);
                }
            } else {
                initRootKP();
            }
        }
        if (v.getId() == R.id.imageViewRename && data.bFolder) {
            final EditText txtName = new EditText(getActivity());
            LinearLayout Layout = new LinearLayout(getActivity());
            Layout.addView(txtName);
            LayoutParams Params = (LayoutParams) txtName.getLayoutParams();
            Params.leftMargin = Utilities.dpToPixel(12, getActivity());
            Params.rightMargin = Utilities.dpToPixel(12, getActivity());
            Params.topMargin = Utilities.dpToPixel(5, getActivity());
            Params.width = -1;
            txtName.setHint("请输入文件夹的名称");
            txtName.setText(data.szTitle);
            new Builder(getActivity()).setTitle("重命名文件夹").setView(Layout).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String szKPName = txtName.getText().toString();
                    if (szKPName.isEmpty()) {
                        Utilities.showAlertMessage(MyiLibraryFragment.this.getActivity(), "请输入名称", "请输入文件夹的标题");
                        return;
                    }
                    MyiLibraryFragment.this.renameKP(data.szGUID, szKPName, data);
                    dialog.dismiss();
                }
            }).setNegativeButton("取消", null).show();
        }
        if (v.getId() == R.id.imageViewShare) {
            if (this.mShowcaseKPGUID == null || this.mShowcaseKPGUID.isEmpty()) {
                Utilities.showAlertMessage(getActivity(), "无法执行分享动作", "缺少展示区目录的相关信息，无法进行分享操作。");
                return;
            } else if (!this.mShareKPGUID.containsValue(this.mCurrentKPGUID)) {
                PopupMenu popup = new PopupMenu(getActivity(), v);
                popup.getMenuInflater().inflate(R.menu.menu_fragment_myilibrary_share, popup.getMenu());
                popup.getMenu().removeItem(R.id.action_shareto_classshowcase);
                int i = 0;
                final ArrayList<String> arrUserClassGUID = new ArrayList();
                Iterator it = MyiBaseApplication.getCommonVariables().UserInfo.arrClasses.iterator();
                while (it.hasNext()) {
                    UserClassInfo oneClass = (UserClassInfo) it.next();
                    popup.getMenu().add(0, R.id.action_shareto_classshowcase + i, 0, "分享到" + oneClass.szClassName + "的展示区...");
                    arrUserClassGUID.add(oneClass.szClassGUID);
                    i++;
                }
                popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.action_shareto_chattarget) {
                            if (MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_MYILIBRARY_SHARETOIM)) {
                                ChatComponent ActiveComponent = ChatComponent.getCurrentChatComponent();
                                if (ActiveComponent != null) {
                                    boolean z;
                                    Intent intent = new Intent(MyiLibraryFragment.this.getActivity(), ResourceDetailActivity.class);
                                    intent.putExtra(CommentComponent.RESOURCEGUID, data.szGUID);
                                    intent.putExtra("title", data.szTitle);
                                    String str = "isquestion";
                                    if (data.nType == 0) {
                                        z = true;
                                    } else {
                                        z = false;
                                    }
                                    intent.putExtra(str, z);
                                    str = "isresource";
                                    if (data.nType != 0) {
                                        z = true;
                                    } else {
                                        z = false;
                                    }
                                    intent.putExtra(str, z);
                                    intent.putExtra("resourcetype", data.nType);
                                    if (!ActiveComponent.sendMessage("INTENT=" + intent.toUri(0))) {
                                        Utilities.showAlertMessage(MyiLibraryFragment.this.getActivity(), "无法发送", "请检查当前是否没有选择在线答疑对象。");
                                    }
                                } else {
                                    Utilities.showAlertMessage(MyiLibraryFragment.this.getActivity(), "无法发送", "没有检测到当前打开了聊天窗口。");
                                }
                            } else {
                                Utilities.showAlertMessage(MyiLibraryFragment.this.getActivity(), "无法分享", "您当前没有权限。");
                                return false;
                            }
                        } else if (item.getItemId() == R.id.action_select_user) {
                            if (!MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_MYILIBRARY_SHARETOSTUDENTS)) {
                                Utilities.showAlertMessage(MyiLibraryFragment.this.getActivity(), "无法分享", "您当前没有权限。");
                                return false;
                            } else if (MyiLibraryFragment.this.getActivity().getSupportFragmentManager().findFragmentByTag("shareDialog") == null) {
                                ShareToDialog shareDialog = new ShareToDialog();
                                ft = MyiLibraryFragment.this.getActivity().getSupportFragmentManager().beginTransaction();
                                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                shareDialog.setCancelable(true);
                                r9 = data;
                                shareDialog.setCallBack(new ShareDialogCallBack() {
                                    public void onShare(ArrayList<String> arrSelectedUserJIDs) {
                                        boolean z;
                                        boolean z2 = true;
                                        String szTextToSend = "INTENT=";
                                        Intent intent = new Intent(MyiLibraryFragment.this.getActivity(), ResourceDetailActivity.class);
                                        intent.putExtra(CommentComponent.RESOURCEGUID, r9.szGUID);
                                        intent.putExtra("title", r9.szTitle);
                                        String str = "isquestion";
                                        if (r9.nType == 0) {
                                            z = true;
                                        } else {
                                            z = false;
                                        }
                                        intent.putExtra(str, z);
                                        String str2 = "isresource";
                                        if (r9.nType == 0) {
                                            z2 = false;
                                        }
                                        intent.putExtra(str2, z2);
                                        intent.putExtra("resourcetype", r9.nType);
                                        szTextToSend = new StringBuilder(String.valueOf(szTextToSend)).append(intent.toUri(0)).toString();
                                        for (int i = 0; i < arrSelectedUserJIDs.size(); i++) {
                                            ChatComponent.sendMessage((String) arrSelectedUserJIDs.get(i), szTextToSend);
                                        }
                                        Utilities.showAlertMessage(MyiLibraryFragment.this.getActivity(), "已发出", "该内容已成功分享。");
                                    }
                                });
                                shareDialog.show(ft, "shareDialog");
                            }
                        } else if (item.getItemId() >= R.id.action_shareto_classshowcase) {
                            if (!MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_MYILIBRARY_SHARETOSTUDENTS)) {
                                Utilities.showAlertMessage(MyiLibraryFragment.this.getActivity(), "无法分享", "您当前没有权限。");
                                return false;
                            } else if (data.nType == 0) {
                                Utilities.showAlertMessage(MyiLibraryFragment.this.getActivity(), "无法分享", "目前暂不支持试题分享到展示区。");
                                return false;
                            } else {
                                SelectFolderDialog selectFolderDialog = new SelectFolderDialog();
                                String szTargetUserClassGUID = (String) arrUserClassGUID.get(item.getItemId() - R.id.action_shareto_classshowcase);
                                ft = MyiLibraryFragment.this.getActivity().getSupportFragmentManager().beginTransaction();
                                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                selectFolderDialog.setCancelable(true);
                                selectFolderDialog.setTargetClassKPGUID((String) MyiLibraryFragment.this.mShowcaseKPGUID.get(szTargetUserClassGUID));
                                r9 = data;
                                selectFolderDialog.setCallBack(new SelectFolderDialogCallBack() {
                                    public void onSelected(String szTargetFolderGUID, String szTargetFolderPath) {
                                        MyiLibraryFragment.this.shareResource(r9, szTargetFolderGUID, szTargetFolderPath);
                                    }
                                });
                                selectFolderDialog.show(ft, "selectFolderDialog");
                            }
                        }
                        return true;
                    }
                });
                popup.show();
            }
        }
        if (v.getId() != R.id.imageViewDelete) {
            return;
        }
        if (data.bFolder) {
            new Builder(getActivity()).setTitle("删除确认").setMessage("确实删除吗？删除该目录后，这个目录中的所有资源都将无法被访问").setPositiveButton("是", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Activity activity = MyiLibraryFragment.this.getActivity();
                    final ResourceItemData resourceItemData = data;
                    WebServiceCallItemObject CallItem = new WebServiceCallItemObject("DeleteKnowledgePoint", activity, new OnSuccessListener() {
                        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                            if (nReturnCode == 1) {
                                MyiLibraryFragment.this.marrData.remove(resourceItemData);
                                MyiLibraryFragment.this.mAdapter.notifyDataSetChanged();
                                return;
                            }
                            Utilities.showAlertMessage(MyiLibraryFragment.this.getActivity(), "无法删除", "无法删除该目录。");
                        }
                    });
                    CallItem.setParam("lpszKPGUID", data.szGUID);
                    CallItem.setAllowReturnCodes(1);
                    CallItem.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
                    VirtualNetworkObject.addToQueue(CallItem);
                }
            }).setNegativeButton("否", null).show();
            return;
        }
        String szPromptText = "确实删除该资源吗？如果这个资源已经被分享了，执行删除操作将同时删除公共区中的对应内容。";
        if (this.mShowcaseKPGUID.size() == 0) {
            szPromptText = "确实删除该资源吗？";
        }
        new Builder(getActivity()).setTitle("删除确认").setMessage(szPromptText).setPositiveButton("是", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String szMethodName = "DeleteResource";
                if (data.nType == 0) {
                    szMethodName = "DeleteQuestion";
                } else {
                    szMethodName = "DeleteResource";
                }
                WebServiceCallItemObject CallItem = new WebServiceCallItemObject(szMethodName, MyiLibraryFragment.this.getActivity(), new OnSuccessListener() {
                    public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                        MyiLibraryFragment.this.refresh();
                    }
                });
                CallItem.setFailureListener(new OnFailureListener() {
                    public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                        Utilities.showAlertMessage(MyiLibraryFragment.this.getActivity(), "删除出现错误", "无法删除选定的内容，请检查您是否有权限。");
                    }
                });
                CallItem.setParam("lpszQuestionGUID", data.szGUID);
                CallItem.setParam("lpszGUID", data.szGUID);
                CallItem.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
                VirtualNetworkObject.addToQueue(CallItem);
            }
        }).setNegativeButton("否", null).show();
    }

    private void shareResource(ResourceItemData data, final String szTargetFolderGUID, final String szTargetFolderPath) {
        ResourceItemObject ResourceObject = new ResourceItemObject(data.szGUID, getActivity());
        ResourceObject.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                String szXMLText = ItemObject.readTextData();
                ResourceParser ResourceParser = new ResourceParser();
                if (ResourceParser.initialize(MyiLibraryFragment.this.getActivity(), szXMLText)) {
                    ArrayList arrKPGUIDs = new ArrayList();
                    ArrayList<String> arrKPPaths = new ArrayList();
                    final String szGUID = ResourceParser.getGUID();
                    String szShowcaseGUID = szTargetFolderGUID;
                    if (ResourceParser.getKnowledgePoints(arrKPPaths, arrKPGUIDs) && Utilities.isInArray(arrKPGUIDs, szShowcaseGUID)) {
                        Utilities.showAlertMessage(MyiLibraryFragment.this.getActivity(), "无法分享", "此资源已经在展示区被分享过了。");
                    } else if (ResourceParser.addKnowledgePoints(szTargetFolderPath, szShowcaseGUID)) {
                        WebServiceCallItemObject WebServiceCallItem = new WebServiceCallItemObject("AddResourceByXML", MyiLibraryFragment.this.getActivity());
                        WebServiceCallItem.setSuccessListener(new OnSuccessListener() {
                            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                                MyiLibraryFragment.setResourcePermission(szGUID);
                                Utilities.showAlertMessage(MyiLibraryFragment.this.getActivity(), "分享成功", "所选资源成功分享到展示区");
                            }
                        });
                        WebServiceCallItem.setFailureListener(new OnFailureListener() {
                            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                                Utilities.showAlertMessage(MyiLibraryFragment.this.getActivity(), "分享失败", "无法分享所选资源到展示区");
                            }
                        });
                        WebServiceCallItem.setParam("lpszResourceXML", ResourceParser.getXML());
                        VirtualNetworkObject.addToQueue(WebServiceCallItem);
                    }
                }
            }
        });
        ResourceObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(ResourceObject);
    }

    public void setShareFolderOnly(boolean bEnable, String szRootKPGUID) {
        this.mbClassShareFolderOnly = bEnable;
        this.mPersonalKPGUID = szRootKPGUID;
    }

    private void refresh() {
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("GetOneLevelCatalogs", getActivity(), this.mOnSubKPDataComplete);
        CallItem.setParam("lpszParentKPGUID", this.mCurrentKPGUID);
        if (this.mbLoadPersonalDataOnly) {
            CallItem.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
        }
        VirtualNetworkObject.addToQueue(CallItem);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != R.id.action_add_picture) {
            if (item.getItemId() == R.id.action_add_video) {
                if (MyiBaseApplication.getCommonVariables().UserInfo.nUserType == 0 && !MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_MYILIBRARY_ADDVIDEO)) {
                    Utilities.showAlertMessage(getActivity(), "无法添加内容", "您当前没有权限。");
                    return false;
                } else if (this.mShowcaseKPGUID.containsValue(this.mCurrentKPGUID)) {
                    Utilities.showAlertMessage(getActivity(), "无法添加内容", "无法在展示区中添加内容，请将内容加入“我的资源库”然后分享到展示区。");
                    return false;
                } else {
                    if (MyiBaseApplication.getCommonVariables().UserInfo.nUserType == 0) {
                        Utilities.showAlertMessageWithNotDisplayAgain(getActivity(), "添加视频内容注意", "注意，您所添加的内容可以被您的任课老师看到且可以被删除，请勿添加老师不允许的内容。");
                    }
                    Intent newIntent = new Intent(getActivity(), CameraRecordActivity.class);
                    CameraRecordActivity.setCallBack(new CameraRecordCallBack() {
                        public void onRecordComplete(String szVideoUri) {
                            String szFilePath = Uri.parse(szVideoUri).getPath();
                            ResourceUploadProcess UploadProcess = new ResourceUploadProcess(MyiLibraryFragment.this.getActivity(), MyiLibraryFragment.this.mszUploadPath, MyiLibraryFragment.mszRealName, MyiLibraryFragment.this.mCurrentKPPath, MyiLibraryFragment.this.mCurrentKPGUID);
                            UploadProcess.setType("短视频");
                            UploadProcess.setCallBack(new ResourceUploadInterface() {
                                public void onBeginUpload() {
                                }

                                public void onUploadComplete() {
                                    MyiLibraryFragment.this.refresh();
                                }

                                public void onCancel() {
                                }
                            });
                            UploadProcess.startUploadProcess(szFilePath);
                        }
                    });
                    newIntent.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
                    startActivity(newIntent);
                }
            }
            if (item.getItemId() == R.id.action_add_folder) {
                if (MyiBaseApplication.getCommonVariables().UserInfo.nUserType == 0 && !MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_MYILIBRARY_ADDFOLDER)) {
                    Utilities.showAlertMessage(getActivity(), "无法添加内容", "您当前没有权限。");
                    return false;
                } else if (this.mShowcaseKPGUID.containsValue(this.mCurrentKPGUID)) {
                    Utilities.showAlertMessage(getActivity(), "无法添加内容", "无法在展示区中添加内容，请将内容加入“我的资源库”然后分享到展示区。");
                    return false;
                } else {
                    final EditText txtName = new EditText(getActivity());
                    LinearLayout Layout = new LinearLayout(getActivity());
                    Layout.addView(txtName);
                    LayoutParams Params = (LayoutParams) txtName.getLayoutParams();
                    Params.leftMargin = Utilities.dpToPixel(12, getActivity());
                    Params.rightMargin = Utilities.dpToPixel(12, getActivity());
                    Params.topMargin = Utilities.dpToPixel(5, getActivity());
                    Params.width = -1;
                    txtName.setHint("请输入目录的名称");
                    new Builder(getActivity()).setTitle("新建目录").setView(Layout).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String szKPName = txtName.getText().toString();
                            if (szKPName.isEmpty()) {
                                Utilities.showAlertMessage(MyiLibraryFragment.this.getActivity(), "请输入名称", "请输入文件夹的标题");
                                return;
                            }
                            MyiLibraryFragment.this.addKP(szKPName);
                            dialog.dismiss();
                        }
                    }).setNegativeButton("取消", null).show();
                }
            }
            return super.onOptionsItemSelected(item);
        } else if (MyiBaseApplication.getCommonVariables().UserInfo.nUserType == 0 && !MyiBaseApplication.getCommonVariables().UserInfo.checkPermission(Features.PERMISSION_MYILIBRARY_ADDPICTURE)) {
            Utilities.showAlertMessage(getActivity(), "无法添加内容", "您当前没有权限。");
            return false;
        } else if (this.mShowcaseKPGUID.containsValue(this.mCurrentKPGUID)) {
            Utilities.showAlertMessage(getActivity(), "无法添加内容", "无法在展示区中添加内容，请将内容加入“我的资源库”然后分享到展示区。");
            return false;
        } else {
            if (MyiBaseApplication.getCommonVariables().UserInfo.nUserType == 0) {
                Utilities.showAlertMessageWithNotDisplayAgain(getActivity(), "添加绘画内容注意", "注意，您所添加的内容可以被您的任课老师看到且可以被删除，请勿添加老师不允许的内容。");
            }
            addPaint();
            return true;
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.mAdapter.getReadOnly()) {
            super.onCreateOptionsMenu(menu, inflater);
            return;
        }
        inflater.inflate(R.menu.menu_fragment_myilibrary, menu);
        menu.findItem(R.id.action_add).setIcon(new IconDrawable(getActivity(), FontAwesomeIcons.fa_plus).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onDestroy() {
        super.onDestroy();
        ExecuteCommandActivity.setCallBack(null);
    }
}
