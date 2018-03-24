package com.netspace.library.fragment;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.netspace.library.struct.UserHonourData;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.PrivateDataItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.pad.library.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;
import io.vov.vitamio.provider.MediaStore.Video.VideoColumns;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UserHonourFragmentForStudent extends Fragment implements OnItemClickListener, ListAdapterWrapperCallBack {
    private OnHonourChangeCallBack mCallBack;
    private Target mImageSaveTarget = new Target() {
        public void onBitmapFailed(Drawable arg0) {
            Utilities.showAlertMessage(UserHonourFragmentForStudent.this.getActivity(), "获取荣誉图片失败", "获取图片数据失败。");
        }

        public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
            if (Utilities.saveBitmapToPng(UserHonourFragmentForStudent.this.mszTargetFileName, arg0)) {
                UserHonourFragmentForStudent.this.mListAdapterWrapper.notifyDataSetChanged();
                if (UserHonourFragmentForStudent.this.mCallBack != null) {
                    UserHonourFragmentForStudent.this.mCallBack.onHonourChanged();
                }
            }
        }

        public void onPrepareLoad(Drawable arg0) {
        }
    };
    private LinearLayout mLayout;
    private SimpleListAdapterWrapper mListAdapterWrapper;
    private ListView mListView;
    private View mRootView;
    private TextView mTextViewInfo;
    private TextView mTextViewMessage;
    private ArrayList<UserHonourData> marrData = new ArrayList();
    private boolean mbChanged = false;
    private String mszTargetFileName;
    private String mszUserGUID;
    private String mszUserName;

    public interface OnHonourChangeCallBack {
        void onHonourChanged();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mRootView != null) {
            return this.mRootView;
        }
        this.mRootView = inflater.inflate(R.layout.fragment_userhonour, null);
        this.mLayout = (LinearLayout) this.mRootView.findViewById(R.id.layoutContent);
        this.mLayout.setVisibility(4);
        this.mListAdapterWrapper = new SimpleListAdapterWrapper(getContext(), this, R.layout.listitem_userhonour);
        this.mTextViewMessage = (TextView) this.mRootView.findViewById(R.id.textViewMessage);
        this.mTextViewMessage.setVisibility(4);
        this.mTextViewInfo = (TextView) this.mRootView.findViewById(R.id.textViewInfo);
        this.mTextViewInfo.setText("下面列出了您当前收到的奖励，点击可以选择放到桌面或者收到仓库里。");
        this.mListView = (ListView) this.mRootView.findViewById(R.id.listView1);
        this.mListView.setAdapter(this.mListAdapterWrapper);
        this.mListView.setOnItemClickListener(this);
        if (this.mszUserName == null) {
            this.mRootView.findViewById(R.id.textViewInfo).setVisibility(8);
        }
        loadData();
        return this.mRootView;
    }

    public void setCallBack(OnHonourChangeCallBack CallBack) {
        this.mCallBack = CallBack;
    }

    private void reportMessage(String szMessage) {
        this.mTextViewMessage.setVisibility(0);
        this.mTextViewMessage.setText(szMessage);
        this.mLayout.setVisibility(4);
    }

    private void showContent() {
        this.mTextViewMessage.setVisibility(4);
        this.mLayout.setVisibility(0);
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

    public void setUserName(String szUserName) {
        this.mszUserName = szUserName;
    }

    private void loadData() {
        reportMessage("稍候...");
        PrivateDataItemObject PrivateDataObject = new PrivateDataItemObject(this.mszUserName + "_HonourData", null, new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                UserHonourFragmentForStudent.this.decodeHonourData(ItemObject.readTextData(), true);
                UserHonourFragmentForStudent.this.showContent();
            }
        });
        PrivateDataObject.setFailureListener(new OnFailureListener() {
            public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                if (nReturnCode == 0 || nReturnCode == ErrorCode.ERROR_NO_DATA) {
                    UserHonourFragmentForStudent.this.reportMessage("目前您还没有收到任何奖励呢，要努力哦。");
                } else {
                    UserHonourFragmentForStudent.this.reportMessage("获取数据时出现错误，错误信息：" + Utilities.getErrorMessage(nReturnCode));
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

    private void decodeHonourData(String szXML, boolean bFromStudent) {
        try {
            NodeList arrNodeList = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(szXML.getBytes(HTTP.UTF_8))).getDocumentElement().getElementsByTagName("Honour");
            for (int i = 0; i < arrNodeList.getLength(); i++) {
                Node OneNode = arrNodeList.item(i);
                UserHonourData NewData = new UserHonourData();
                if (!false) {
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
                    this.marrData.add(0, NewData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mListAdapterWrapper.notifyDataSetChanged();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String[] arrNames = new String[]{"放在桌面"};
        final UserHonourData data = (UserHonourData) this.marrData.get(position);
        if (data.bGiven) {
            arrNames[0] = "收到仓库里";
        } else {
            arrNames[0] = "放在桌面";
        }
        Builder dialogBuilder = new Builder(getActivity());
        dialogBuilder.setItems(arrNames, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (!data.bGiven) {
                    UserHonourFragmentForStudent.this.putToDesk(data);
                } else if (UserHonourFragmentForStudent.this.removeFromDesk(data) && UserHonourFragmentForStudent.this.mCallBack != null) {
                    UserHonourFragmentForStudent.this.mCallBack.onHonourChanged();
                }
                UserHonourFragmentForStudent.this.mListAdapterWrapper.notifyDataSetChanged();
            }
        });
        dialogBuilder.setTitle("选择动作");
        dialogBuilder.setCancelable(true);
        dialogBuilder.show();
    }

    private File getLocalFile(UserHonourData data) {
        return new File(new StringBuilder(String.valueOf(getContext().getExternalCacheDir().getAbsolutePath())).append("/").append(new StringBuilder(String.valueOf(this.mszUserName + "_")).append(data.szKey).append(".png").toString()).toString());
    }

    public boolean IsOnDesk(UserHonourData data) {
        return getLocalFile(data).exists();
    }

    public boolean putToDesk(UserHonourData data) {
        String szURL = MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/GetTemporaryStorage?filename=" + data.szImageDataGUID;
        this.mszTargetFileName = getLocalFile(data).getAbsolutePath();
        Picasso.with(getContext()).load(szURL).into(this.mImageSaveTarget);
        return true;
    }

    public boolean removeFromDesk(UserHonourData data) {
        return getLocalFile(data).delete();
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
        honourData.bGiven = IsOnDesk(honourData);
        if (honourData.bGiven) {
            imageView.setImageDrawable(new IconDrawable(getContext(), FontAwesomeIcons.fa_floppy_o).color(-6381922).actionBarSize());
        } else {
            imageView.setImageDrawable(null);
        }
        Picasso.with(getContext()).load(szURL).into(imageViewThumbnail);
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
