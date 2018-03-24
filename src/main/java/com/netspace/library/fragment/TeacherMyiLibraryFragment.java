package com.netspace.library.fragment;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.activity.ResourceDetailActivity;
import com.netspace.library.activity.ResourcesViewActivity2;
import com.netspace.library.adapter.SubjectLearnAdapter.SubjectItem;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.ChatComponent;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.dialog.ShareToDialog;
import com.netspace.library.dialog.ShareToDialog.ShareDialogCallBack;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.library.virtualnetworkobject.WebServiceCallItemObject;
import com.netspace.pad.library.R;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sqlcipher.database.SQLiteDatabase;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TeacherMyiLibraryFragment extends MyiLibraryFragment implements OnClickListener {
    private static final String TAG = "TeacherMyiLibraryFragment";
    private OnSuccessListener mRootKPLoadCompleteListener = new OnSuccessListener() {
        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
            String szXML = (String) ItemObject.getParam("1");
            ArrayList<SubjectItem> arrData = new ArrayList();
            try {
                NodeList arrPadDirectory = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(szXML.getBytes(HTTP.UTF_8))).getDocumentElement().getChildNodes();
                for (int i = 0; i < arrPadDirectory.getLength(); i++) {
                    Node OneDirectory = arrPadDirectory.item(i);
                    String szClassGUID = "";
                    String szKPName = OneDirectory.getAttributes().getNamedItem(RESTLibraryFragment.ARGUMENT_NAME_SUFFIX).getNodeValue();
                    String szKPGUID = OneDirectory.getAttributes().getNamedItem("guid").getNodeValue();
                    int nPos = szKPName.indexOf(":Class_");
                    if (nPos != -1) {
                        szKPName = szKPName.substring(0, nPos);
                    }
                    ResourceItemData OneItem = new ResourceItemData();
                    OneItem.szTitle = szKPName;
                    OneItem.szGUID = szKPGUID;
                    OneItem.bFolder = true;
                    if (OneItem.szGUID != null) {
                        TeacherMyiLibraryFragment.this.marrData.add(OneItem);
                    }
                }
                TeacherMyiLibraryFragment.this.mAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    protected void setKPPermission(String szKPGUID) {
    }

    protected void buildStudentPersonalKP() {
    }

    protected void getSystemPersonalKP() {
        this.mbLoadPersonalDataOnly = true;
        WebServiceCallItemObject CallItem = new WebServiceCallItemObject("GetPrivateKP", getActivity());
        CallItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                ArrayList<String> arrGUID = (ArrayList) ItemObject.getParam("0");
                WebServiceCallItemObject CallItem2 = new WebServiceCallItemObject("GetCatalogsByGUID", TeacherMyiLibraryFragment.this.getActivity(), TeacherMyiLibraryFragment.this.mRootKPLoadCompleteListener);
                CallItem2.setParam("arrKPGUIDs", arrGUID);
                CallItem2.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
                VirtualNetworkObject.addToQueue(CallItem2);
            }
        });
        CallItem.setParam("nGrade", Integer.valueOf(-1));
        CallItem.setParam("nSubject", Integer.valueOf(-1));
        CallItem.setParam("szKeyWords", "");
        CallItem.setUserGUID(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
        VirtualNetworkObject.addToQueue(CallItem);
        this.mCurrentKPGUID = "";
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.mCurrentKPGUID != "" || item.getItemId() == R.id.action_add) {
            return super.onOptionsItemSelected(item);
        }
        Utilities.showAlertMessage(getActivity(), "无法添加内容", "无法在当前目录下增加内容。");
        return true;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_myilibrary, menu);
        menu.findItem(R.id.action_add).setIcon(new IconDrawable(getActivity(), FontAwesomeIcons.fa_plus).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
    }

    protected void initRootKP() {
        this.marrData.clear();
        getSystemPersonalKP();
    }

    public void onClick(final View v) {
        if (v.getId() != R.id.imageViewShare) {
            super.onClick(v);
        } else if (getChildFragmentManager().findFragmentByTag("shareDialog") == null) {
            ShareToDialog shareDialog = new ShareToDialog();
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            shareDialog.setCancelable(true);
            shareDialog.setCallBack(new ShareDialogCallBack() {
                public void onShare(ArrayList<String> arrSelectedUserJIDs) {
                    ResourceItemData itemData = (ResourceItemData) v.getTag();
                    String szTextToSend = "INTENT=";
                    Intent intent;
                    if (itemData.nType < 1000 || itemData.nType > 1999) {
                        intent = new Intent(TeacherMyiLibraryFragment.this.getActivity(), ResourceDetailActivity.class);
                        intent.putExtra(CommentComponent.RESOURCEGUID, itemData.szGUID);
                        if (itemData.nType == 0) {
                            intent.putExtra("isquestion", true);
                        } else {
                            intent.putExtra("isquestion", false);
                        }
                        intent.putExtra("resourcetype", itemData.nType);
                        intent.putExtra("title", itemData.szTitle);
                        intent.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
                        szTextToSend = new StringBuilder(String.valueOf(szTextToSend)).append(intent.toUri(0)).toString();
                    } else {
                        intent = new Intent(TeacherMyiLibraryFragment.this.getActivity(), ResourcesViewActivity2.class);
                        intent.putExtra(CommentComponent.RESOURCEGUID, itemData.szGUID);
                        intent.putExtra("scheduleguid", itemData.szGUID);
                        intent.putExtra("displayanswers", true);
                        intent.putExtra("enableunlock", true);
                        intent.putExtra(UserHonourFragment.USERCLASSGUID, "");
                        intent.putExtra("userclassname", "");
                        intent.putExtra("title", itemData.szTitle);
                        intent.putExtra("resourcetype", itemData.nType);
                        intent.setFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
                        szTextToSend = new StringBuilder(String.valueOf(szTextToSend)).append(intent.toUri(0)).toString();
                    }
                    for (int i = 0; i < arrSelectedUserJIDs.size(); i++) {
                        ChatComponent.sendMessage((String) arrSelectedUserJIDs.get(i), szTextToSend);
                    }
                    Utilities.showAlertMessage(TeacherMyiLibraryFragment.this.getActivity(), "已发出", "该内容已成功分享给所选学生。");
                }
            });
            shareDialog.show(ft, "shareDialog");
        }
    }
}
