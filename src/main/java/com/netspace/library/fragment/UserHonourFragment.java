package com.netspace.library.fragment;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.adapter.SimpleListAdapterWrapper;
import com.netspace.library.adapter.SimpleListAdapterWrapper.ListAdapterWrapperCallBack;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.error.ErrorCode;
import com.netspace.library.im.IMService;
import com.netspace.library.struct.UserClassInfo;
import com.netspace.library.struct.UserHonourData;
import com.netspace.library.struct.UserInfo;
import com.netspace.library.utilities.TextViewLinkHandler;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.PrivateDataItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.pad.library.R;
import com.squareup.picasso.Picasso;
import io.vov.vitamio.provider.MediaStore.Video.VideoColumns;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UserHonourFragment extends Fragment implements OnItemClickListener, ListAdapterWrapperCallBack {
    public static final String ALLOWMULTISELECT = "allowMultiSelect";
    private static final String TAG = "UserHonourFragment";
    public static final String USERCLASSGUID = "userclassguid";
    public static final String USERNAME = "username";
    private UserHonourCallBack mCallBack;
    private JSONObject mJsonObject;
    private LinearLayout mLayout;
    private SimpleListAdapterWrapper mListAdapterWrapper;
    private ListView mListView;
    private View mRootView;
    private TextView mTextViewInfo;
    private TextView mTextViewMessage;
    private UserInfo mUserInfo;
    private ArrayList<UserHonourData> marrData = new ArrayList();
    private boolean mbAllowMultiSelect = false;
    private boolean mbChanged = false;
    private String mszDefaultStudentHonourXML = "<wmStudy/>";
    private String mszUserClassGUID = "";
    private String mszUserGUID;
    private String mszUserName;

    public interface UserHonourCallBack {
        void onHonourDeselected(UserHonourData userHonourData);

        void onHonourSelected(UserHonourData userHonourData);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        if (this.mRootView != null) {
            return this.mRootView;
        }
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            this.mbAllowMultiSelect = bundle.getBoolean(ALLOWMULTISELECT, this.mbAllowMultiSelect);
            this.mszUserClassGUID = bundle.getString(USERCLASSGUID, this.mszUserClassGUID);
            this.mszUserName = bundle.getString(USERNAME, this.mszUserName);
        }
        this.mRootView = inflater.inflate(R.layout.fragment_userhonour, null);
        this.mLayout = (LinearLayout) this.mRootView.findViewById(R.id.layoutContent);
        this.mLayout.setVisibility(4);
        this.mListAdapterWrapper = new SimpleListAdapterWrapper(getContext(), this, R.layout.listitem_userhonour);
        this.mTextViewMessage = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        this.mTextViewMessage.setVisibility(4);
        this.mTextViewInfo = (TextView) this.mRootView.findViewById(R.id.textViewInfo);
        this.mListView = (ListView) this.mRootView.findViewById(R.id.listView1);
        this.mListView.setAdapter(this.mListAdapterWrapper);
        this.mListView.setOnItemClickListener(this);
        if (this.mszUserName == null) {
            this.mRootView.findViewById(R.id.textViewInfo).setVisibility(8);
        }
        loadData();
        return this.mRootView;
    }

    public void setCallBack(UserHonourCallBack callBack) {
        this.mCallBack = callBack;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
    }

    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
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
        } else if (this.mszUserClassGUID != null) {
            reportMessage("当前没有任何奖励，点击这里导入系统的预制奖励。");
            this.mTextViewMessage.setText(Html.fromHtml("当前没有任何奖励，<a href='importdefault'>点击这里</a>导入系统的预制奖励。"));
            this.mTextViewMessage.setMovementMethod(new TextViewLinkHandler() {
                public void onLinkClick(String url) {
                    UserHonourFragment.this.doImportDefault();
                }
            });
            this.mTextViewMessage.setVisibility(0);
            this.mLayout.setVisibility(4);
        }
    }

    private void doImportDefault() {
        PrivateDataItemObject PrivateDataObject = new PrivateDataItemObject("DefaultHonour", null, new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                String szData = ItemObject.readTextData();
                if (szData != null && !szData.isEmpty()) {
                    PrivateDataItemObject newDataObject = new PrivateDataItemObject(new StringBuilder(String.valueOf(MyiBaseApplication.getCommonVariables().UserInfo.szUserName)).append("_HonourData").toString(), null, new OnSuccessListener() {
                        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                            Utilities.showAlertMessage(UserHonourFragment.this.getContext(), "操作成功", "默认数据已复制到当前班级。");
                            UserHonourFragment.this.loadData();
                            UserHonourFragment.this.showContent();
                        }
                    });
                    newDataObject.setFailureListener(new OnFailureListener() {
                        public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                            Utilities.showAlertMessage(UserHonourFragment.this.getContext(), "错误", "保存数据时出现错误，错误信息：" + Utilities.getErrorMessage(nReturnCode));
                        }
                    });
                    newDataObject.setReadOperation(false);
                    newDataObject.setAlwaysActiveCallbacks(true);
                    newDataObject.writeTextData(szData);
                    VirtualNetworkObject.addToQueue(newDataObject);
                }
            }
        });
        PrivateDataObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                UserHonourFragment.this.reportMessage("获取数据时出现错误，错误信息：" + Utilities.getErrorMessage(nReturnCode));
            }
        });
        PrivateDataObject.setAlwaysActiveCallbacks(true);
        PrivateDataObject.setReadOperation(true);
        VirtualNetworkObject.addToQueue(PrivateDataObject);
    }

    public ArrayList<UserHonourData> getSelectedHonourData() {
        ArrayList<UserHonourData> arrResult = new ArrayList();
        Iterator it = this.marrData.iterator();
        while (it.hasNext()) {
            UserHonourData oneData = (UserHonourData) it.next();
            if (oneData.bSelected) {
                arrResult.add(oneData);
            }
        }
        return arrResult;
    }

    public void setData(String szUserGUID) {
        this.mszUserGUID = szUserGUID;
        if (this.mRootView != null) {
            loadData();
        }
    }

    public void setAllowMultiSelect(boolean bAllow) {
        this.mbAllowMultiSelect = bAllow;
        if (this.mRootView != null) {
            loadData();
        }
    }

    public void setUserName(String szUserName) {
        this.mszUserName = szUserName;
    }

    public void setUserClassGUID(String szUserClassGUID) {
        this.mszUserClassGUID = szUserClassGUID;
    }

    private void loadData() {
        reportMessage("稍候...");
        String szFirstClassGUID = "";
        if (this.mszUserGUID != null) {
            this.mUserInfo = MyiBaseApplication.getCommonVariables().UserInfo.findUserByGUID(this.mszUserGUID);
            if (this.mUserInfo == null || this.mUserInfo.arrClasses.size() == 0) {
                reportMessage("没有找到这个用户的数据，他可能不在您所管理的班级内。");
                return;
            }
            szFirstClassGUID = ((UserClassInfo) this.mUserInfo.arrClasses.get(0)).szClassGUID;
        } else {
            szFirstClassGUID = this.mszUserClassGUID;
        }
        PrivateDataItemObject PrivateDataObject = new PrivateDataItemObject(new StringBuilder(String.valueOf(MyiBaseApplication.getCommonVariables().UserInfo.szUserName)).append("_HonourData").toString(), null, new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                UserHonourFragment.this.decodeHonourData(ItemObject.readTextData(), false);
                if (UserHonourFragment.this.mszUserGUID != null) {
                    UserHonourFragment.this.loadStudentHonourList();
                } else {
                    UserHonourFragment.this.showContent();
                }
            }
        });
        PrivateDataObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                if (nReturnCode != 0 && nReturnCode != ErrorCode.ERROR_NO_DATA) {
                    UserHonourFragment.this.reportMessage("获取数据时出现错误，错误信息：" + Utilities.getErrorMessage(nReturnCode));
                } else if (UserHonourFragment.this.mszUserGUID != null) {
                    UserHonourFragment.this.loadStudentHonourList();
                } else {
                    UserHonourFragment.this.showContent();
                }
            }
        });
        PrivateDataObject.setAlwaysActiveCallbacks(true);
        VirtualNetworkObject.addToQueue(PrivateDataObject);
    }

    public void save() {
        if (!this.mbChanged) {
        }
    }

    private void loadStudentHonourList() {
        if (this.mszUserName != null) {
            PrivateDataItemObject PrivateDataObject = new PrivateDataItemObject(this.mszUserName + "_HonourData", null, new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    UserHonourFragment.this.decodeHonourData(ItemObject.readTextData(), true);
                    UserHonourFragment.this.showContent();
                }
            });
            PrivateDataObject.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    if (nReturnCode == 0 || nReturnCode == ErrorCode.ERROR_NO_DATA) {
                        UserHonourFragment.this.showContent();
                    } else {
                        UserHonourFragment.this.reportMessage("获取数据时出现错误，错误信息：" + Utilities.getErrorMessage(nReturnCode));
                    }
                }
            });
            PrivateDataObject.setAlwaysActiveCallbacks(true);
            VirtualNetworkObject.addToQueue(PrivateDataObject);
        }
    }

    private void saveStudentHonourData() {
        try {
            Document RootDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(this.mszDefaultStudentHonourXML.getBytes(HTTP.UTF_8)));
            Element RootElement = RootDocument.getDocumentElement();
            for (int i = 0; i < this.marrData.size(); i++) {
                UserHonourData oneData = (UserHonourData) this.marrData.get(i);
                if (oneData.bGiven) {
                    Element NewElement = RootDocument.createElement("Honour");
                    NewElement.setAttribute("title", oneData.szTitle);
                    NewElement.setAttribute("guid", oneData.szImageDataGUID);
                    if (oneData.szKey != null) {
                        NewElement.setAttribute("key", oneData.szKey);
                    }
                    NewElement.setAttribute(VideoColumns.DESCRIPTION, oneData.szDescription);
                    RootElement.appendChild(NewElement);
                }
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(RootDocument);
            Writer writer = new StringWriter();
            transformer.transform(source, new StreamResult(writer));
            String output = writer.toString();
            PrivateDataItemObject PrivateDataObject = new PrivateDataItemObject(this.mszUserName + "_HonourData", null, new OnSuccessListener() {
                public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                    Utilities.showAlertMessage(UserHonourFragment.this.getContext(), "操作成功", "该学生的荣誉数据已成功保存。");
                }
            });
            PrivateDataObject.setFailureListener(new OnFailureListener() {
                public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                    Utilities.showAlertMessage(UserHonourFragment.this.getContext(), "错误", "保存数据时出现错误，错误信息：" + Utilities.getErrorMessage(nReturnCode));
                }
            });
            PrivateDataObject.setReadOperation(false);
            PrivateDataObject.setAlwaysActiveCallbacks(true);
            PrivateDataObject.writeTextData(output);
            PrivateDataObject.setParam("nRights", Integer.valueOf(-1));
            VirtualNetworkObject.addToQueue(PrivateDataObject);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Utilities.showAlertMessage(getContext(), "出现错误", "生成用户荣誉数据时出现错误。");
        } catch (ParserConfigurationException e2) {
            e2.printStackTrace();
            Utilities.showAlertMessage(getContext(), "出现错误", "生成用户荣誉数据时出现错误。");
        } catch (SAXException e3) {
            e3.printStackTrace();
            Utilities.showAlertMessage(getContext(), "出现错误", "生成用户荣誉数据时出现错误。");
        } catch (IOException e4) {
            e4.printStackTrace();
            Utilities.showAlertMessage(getContext(), "出现错误", "生成用户荣誉数据时出现错误。");
        } catch (TransformerException e5) {
            e5.printStackTrace();
            Utilities.showAlertMessage(getContext(), "出现错误", "生成用户荣誉数据时出现错误。");
        }
    }

    private void decodeHonourData(String szXML, boolean bFromStudent) {
        Log.d(TAG, "decodeHonourData");
        try {
            NodeList arrNodeList = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(szXML.getBytes(HTTP.UTF_8))).getDocumentElement().getElementsByTagName("Honour");
            for (int i = 0; i < arrNodeList.getLength(); i++) {
                Node OneNode = arrNodeList.item(i);
                UserHonourData NewData = new UserHonourData();
                String szImageGUID = OneNode.getAttributes().getNamedItem("guid").getNodeValue();
                boolean bFound = false;
                int j = 0;
                while (j < this.marrData.size()) {
                    if (((UserHonourData) this.marrData.get(j)).szImageDataGUID.equalsIgnoreCase(szImageGUID) && ((UserHonourData) this.marrData.get(j)).bGiven == bFromStudent) {
                        bFound = true;
                        break;
                    }
                    j++;
                }
                if (!bFound) {
                    if (bFromStudent) {
                        NewData.szTitle = OneNode.getAttributes().getNamedItem("title").getNodeValue();
                    } else {
                        NewData.szTitle = OneNode.getAttributes().getNamedItem(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX).getNodeValue();
                    }
                    NewData.szImageDataGUID = OneNode.getAttributes().getNamedItem("guid").getNodeValue();
                    NewData.szDescription = OneNode.getAttributes().getNamedItem(VideoColumns.DESCRIPTION).getNodeValue();
                    if (OneNode.getAttributes().getNamedItem("key") != null) {
                        NewData.szKey = OneNode.getAttributes().getNamedItem("key").getNodeValue();
                    }
                    if (bFromStudent) {
                        NewData.bGiven = true;
                    }
                    this.marrData.add(NewData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mListAdapterWrapper.notifyDataSetChanged();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        boolean z = false;
        String[] arrNames = new String[]{"发出"};
        final UserHonourData data = (UserHonourData) this.marrData.get(position);
        if (this.mbAllowMultiSelect) {
            if (!data.bSelected) {
                z = true;
            }
            data.bSelected = z;
            if (this.mCallBack != null) {
                if (data.bSelected) {
                    this.mCallBack.onHonourSelected(data);
                } else {
                    this.mCallBack.onHonourDeselected(data);
                }
            }
            ((CheckBox) view.findViewById(R.id.checkBoxSelected)).setChecked(data.bSelected);
            return;
        }
        if (data.bGiven) {
            arrNames[0] = "撤回";
        } else {
            arrNames[0] = "发出";
        }
        Builder dialogBuilder = new Builder(getActivity());
        dialogBuilder.setItems(arrNames, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (data.bGiven) {
                    final UserHonourData userHonourData = data;
                    Utilities.showAlertMessage(UserHonourFragment.this.getContext(), "撤回确认", "将要撤回此奖励，学生将不会收到撤回通知。如果学生已经把这个奖励放到桌面上了，将在下次登录时自动从桌面上撤回。\n\n是否继续？", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            userHonourData.bGiven = false;
                            UserHonourFragment.this.saveStudentHonourData();
                        }
                    }, null);
                    return;
                }
                userHonourData = data;
                Utilities.showAlertMessage(UserHonourFragment.this.getContext(), "发出确认", "将要发出此奖励，学生将收到通知。下面的列表将在学生确认收到奖励后才会更新。\n\n是否继续？", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        IMService.getIMService().sendMessage(IMService.buildMessage(String.format("GiveHonour %s %s %s %s %s", new Object[]{userHonourData.szTitle, userHonourData.szDescription, userHonourData.szImageDataGUID, MyiBaseApplication.getCommonVariables().UserInfo.szRealName, UserHonourFragment.this.mUserInfo.szUserName}), Utilities.createGUID(), MyiBaseApplication.getCommonVariables().MyiApplication.getClientID(), UserHonourFragment.this.mUserInfo.szUserJID, 0), UserHonourFragment.this.mUserInfo.szUserJID);
                        Utilities.showAlertMessage(UserHonourFragment.this.getContext(), "操作成功", "奖励数据已发出，等学生平板在线收到后下面列表才会更新。");
                    }
                }, null);
            }
        });
        dialogBuilder.setTitle("选择动作");
        dialogBuilder.setCancelable(true);
        dialogBuilder.show();
    }

    public int getCount() {
        return this.marrData.size();
    }

    public Object getItem(int position) {
        return this.marrData.get(position);
    }

    public void getView(int position, View convertView) {
        UserHonourData honourData = (UserHonourData) this.marrData.get(position);
        TextView textViewTitle = (TextView) convertView.findViewById(R.id.TextViewTitle);
        TextView textViewDescription = (TextView) convertView.findViewById(R.id.textViewDescription);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.ImageViewState);
        ImageView imageViewThumbnail = (ImageView) convertView.findViewById(R.id.imageViewThumbnail);
        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBoxSelected);
        String szURL = MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/GetTemporaryStorage?filename=" + honourData.szImageDataGUID;
        textViewTitle.setText(honourData.szTitle);
        textViewDescription.setText(honourData.szDescription);
        if (honourData.bGiven) {
            imageView.setImageDrawable(new IconDrawable(getContext(), FontAwesomeIcons.fa_check).color(-16716288).actionBarSize());
        } else {
            imageView.setImageDrawable(null);
        }
        Picasso.with(getContext()).load(szURL).into(imageViewThumbnail);
        if (this.mbAllowMultiSelect) {
            checkBox.setVisibility(0);
            imageView.setVisibility(8);
            checkBox.setChecked(honourData.bSelected);
            return;
        }
        checkBox.setVisibility(8);
        imageView.setVisibility(0);
    }

    public void select(boolean bAll) {
        Iterator it = this.marrData.iterator();
        while (it.hasNext()) {
            ((UserHonourData) it.next()).bSelected = bAll;
        }
        this.mListAdapterWrapper.notifyDataSetChanged();
    }
}
