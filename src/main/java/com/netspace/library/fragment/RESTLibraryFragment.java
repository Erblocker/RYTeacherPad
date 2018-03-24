package com.netspace.library.fragment;

import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.activity.ResourceDetailActivity;
import com.netspace.library.activity.ResourcesViewActivity2;
import com.netspace.library.adapter.SimpleRecyclerViewAdapterWrapper;
import com.netspace.library.adapter.SimpleRecyclerViewAdapterWrapper.RecyclerAdapterWrapperCallBack;
import com.netspace.library.adapter.SimpleRecyclerViewAdapterWrapper.ViewHolder;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.CommentComponent;
import com.netspace.library.error.ErrorCode;
import com.netspace.library.parser.ResourceParser;
import com.netspace.library.service.StudentAnswerImageService;
import com.netspace.library.struct.ResourceItemData;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.virtualnetworkobject.HttpItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject;
import com.netspace.library.virtualnetworkobject.ItemObject.OnFailureListener;
import com.netspace.library.virtualnetworkobject.ItemObject.OnSuccessListener;
import com.netspace.library.virtualnetworkobject.QuestionItemObject;
import com.netspace.library.virtualnetworkobject.ResourceItemObject;
import com.netspace.library.virtualnetworkobject.VirtualNetworkObject;
import com.netspace.pad.library.R;
import com.squareup.picasso.Picasso;
import io.vov.vitamio.MediaMetadataRetriever;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RESTLibraryFragment extends Fragment implements OnClickListener, RecyclerAdapterWrapperCallBack {
    public static final String ARGUMENT_ALLOW_ADD_BOOKMARK = "allowAddBookMark";
    public static final String ARGUMENT_ALLOW_ADD_TO_QUESTION_BOOK = "allowAddToQuestionBook";
    public static final String ARGUMENT_ALLOW_ADD_TO_RESOURCELIBRARY = "allowAddToResourceLibrary";
    public static final String ARGUMENT_MORE_SUFFIX = "";
    public static final String ARGUMENT_NAME_SUFFIX = "name";
    public static final String ARGUMENT_PARAM_SUFFIX = "szParamSuffix";
    public static final String ARGUMENT_PATH_SUFFIX = "szPathSuffix";
    public static final String ARGUMENT_QUESTION_SUFFIX = "szQuestionSuffix";
    private static final String TAG = "RESTLibraryFragment";
    private static int TIMEOUT = 2000;
    private SimpleRecyclerViewAdapterWrapper mAdapter;
    private OnFailureListener mFailureListener = new OnFailureListener() {
        public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
            if (nReturnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
                Utilities.showAlertMessage(RESTLibraryFragment.this.getContext(), "尚未实现", "很抱歉，此功能目前尚未完成。");
            } else {
                Utilities.showAlertMessage(RESTLibraryFragment.this.getContext(), "出现错误", "很抱歉，通讯过程中出现错误。错误信息：" + ItemObject.getErrorText());
            }
        }
    };
    private String mGetQuestionSuffix = "";
    private HashMap<Integer, String> mMapFavoriteItemNames = new HashMap();
    private HashMap<Integer, String> mMapFavoriteItems = new HashMap();
    private String mModuleName;
    private String mMoreSuffix = "startPage";
    private TextView mNoDataView;
    private String mPathSuffix = "";
    private RecyclerView mRecyclerView;
    private String mRestBaseURL;
    private OnRestSuccessListener mRestMoreSuccessListener = new OnRestSuccessListener() {
        public void onJsonObject(JSONObject jsonData) {
            int i;
            JSONArray folder;
            for (i = 0; i < RESTLibraryFragment.this.marrData.size(); i++) {
                if (((ResourceItemData) RESTLibraryFragment.this.marrData.get(i)).szGUID == "more") {
                    RESTLibraryFragment.this.marrData.remove(i);
                    break;
                }
            }
            int i2 = 0;
            try {
                folder = jsonData.getJSONArray("folders");
                for (i = 0; i < folder.length(); i++) {
                    RESTLibraryFragment.this.parserJsonObject(folder.getJSONObject(i));
                }
            } catch (JSONException e) {
            }
            try {
                folder = jsonData.getJSONArray("questions");
                i2 = 0 + folder.length();
                for (i = 0; i < folder.length(); i++) {
                    RESTLibraryFragment.this.parserJsonObject(folder.getJSONObject(i));
                }
            } catch (JSONException e2) {
            }
            try {
                folder = jsonData.getJSONArray("resources");
                i2 += folder.length();
                for (i = 0; i < folder.length(); i++) {
                    RESTLibraryFragment.this.parserJsonObject(folder.getJSONObject(i));
                }
            } catch (JSONException e3) {
            }
            if (i2 > 0) {
                RESTLibraryFragment.this.addMore();
            }
            RESTLibraryFragment.this.mAdapter.notifyDataSetChanged();
            ActivityCompat.invalidateOptionsMenu(RESTLibraryFragment.this.getActivity());
        }

        public void onJsonArray(JSONArray jsonArray) {
            int i;
            for (i = 0; i < RESTLibraryFragment.this.marrData.size(); i++) {
                if (((ResourceItemData) RESTLibraryFragment.this.marrData.get(i)).szGUID == "more") {
                    RESTLibraryFragment.this.marrData.remove(i);
                    break;
                }
            }
            for (i = 0; i < jsonArray.length(); i++) {
                try {
                    RESTLibraryFragment.this.parserJsonObject(jsonArray.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (jsonArray.length() > 0) {
                RESTLibraryFragment.this.addMore();
            }
            RESTLibraryFragment.this.mAdapter.notifyDataSetChanged();
            ActivityCompat.invalidateOptionsMenu(RESTLibraryFragment.this.getActivity());
        }
    };
    private OnRestSuccessListener mRestSuccessListener = new OnRestSuccessListener() {
        public void onJsonObject(JSONObject jsonData) {
            JSONArray folder;
            int i;
            RESTLibraryFragment.this.marrData.clear();
            RESTLibraryFragment.this.addBack();
            int nCount = 0;
            try {
                folder = jsonData.getJSONArray("folders");
                for (i = 0; i < folder.length(); i++) {
                    RESTLibraryFragment.this.parserJsonObject(folder.getJSONObject(i));
                }
            } catch (JSONException e) {
            }
            try {
                folder = jsonData.getJSONArray("questions");
                nCount = 0 + folder.length();
                for (i = 0; i < folder.length(); i++) {
                    RESTLibraryFragment.this.parserJsonObject(folder.getJSONObject(i));
                }
            } catch (JSONException e2) {
            }
            try {
                folder = jsonData.getJSONArray("resources");
                nCount += folder.length();
                for (i = 0; i < folder.length(); i++) {
                    RESTLibraryFragment.this.parserJsonObject(folder.getJSONObject(i));
                }
            } catch (JSONException e3) {
            }
            if (nCount > 0) {
                RESTLibraryFragment.this.addMore();
            }
            RESTLibraryFragment.this.mAdapter.notifyDataSetChanged();
            if (RESTLibraryFragment.this.marrData.size() > 0) {
                RESTLibraryFragment.this.mRecyclerView.scrollToPosition(0);
            }
            ActivityCompat.invalidateOptionsMenu(RESTLibraryFragment.this.getActivity());
        }

        public void onJsonArray(JSONArray jsonArray) {
            RESTLibraryFragment.this.marrData.clear();
            RESTLibraryFragment.this.addBack();
            int nCount = jsonArray.length();
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    RESTLibraryFragment.this.parserJsonObject(jsonArray.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (nCount > 0) {
                RESTLibraryFragment.this.addMore();
            }
            RESTLibraryFragment.this.mAdapter.notifyDataSetChanged();
            if (RESTLibraryFragment.this.marrData.size() > 0) {
                RESTLibraryFragment.this.mRecyclerView.scrollToPosition(0);
            }
            ActivityCompat.invalidateOptionsMenu(RESTLibraryFragment.this.getActivity());
        }
    };
    private View mRootView;
    private String mSuffix = "";
    private ArrayList<ResourceItemData> marrData = new ArrayList();
    private ArrayList<String> marrFavoriteItems = new ArrayList();
    private ArrayList<String> marrFavoriteURLs = new ArrayList();
    private ArrayList<ResourceItemData> marrPath = new ArrayList();
    private boolean mbAllowAddToQuestionBook = false;
    private boolean mbAllowAddToResourceLibrary = false;
    private boolean mbAllowBookMark = true;
    private int mnPageIndex = 1;
    private String mszData;
    private String mszSelectedGUID = "";

    private interface OnRestSuccessListener {
        void onJsonArray(JSONArray jSONArray);

        void onJsonObject(JSONObject jSONObject);
    }

    private void doCacheOperation(String szGUID, OnSuccessListener successListener) {
        String szURL = this.mRestBaseURL + this.mPathSuffix;
        if (this.mGetQuestionSuffix.isEmpty()) {
            for (int i = 0; i < this.marrPath.size(); i++) {
                szURL = new StringBuilder(String.valueOf(szURL)).append(((ResourceItemData) this.marrPath.get(i)).szGUID).append("/").toString();
            }
        } else {
            szURL = new StringBuilder(String.valueOf(szURL)).append(this.mGetQuestionSuffix).toString();
        }
        szURL = new StringBuilder(String.valueOf(szURL)).append(szGUID).append(".xml").toString();
        if (!this.mSuffix.isEmpty()) {
            szURL = new StringBuilder(String.valueOf(szURL)).append("?").append(this.mSuffix).toString();
        }
        HttpItemObject httpItem = new HttpItemObject(szURL, getActivity());
        httpItem.setSuccessListener(successListener);
        httpItem.setFailureListener(this.mFailureListener);
        httpItem.setNeedAuthenticate(true);
        VirtualNetworkObject.addToQueue(httpItem);
    }

    private String getPathText() {
        String szResult = "";
        for (int i = 0; i < this.marrPath.size(); i++) {
            szResult = new StringBuilder(String.valueOf(szResult)).append(((ResourceItemData) this.marrPath.get(i)).szTitle).append("/").toString();
        }
        return szResult;
    }

    private String getURL() {
        String szURL = "";
        for (int i = 0; i < this.marrPath.size(); i++) {
            szURL = new StringBuilder(String.valueOf(szURL)).append(((ResourceItemData) this.marrPath.get(i)).szGUID).append("/").toString();
        }
        return szURL;
    }

    private void doRestCall(String szTargetPath, String szPostData, int nPageIndex, final OnRestSuccessListener SuccessListener) {
        String szURL = this.mRestBaseURL + this.mPathSuffix;
        for (int i = 0; i < this.marrPath.size(); i++) {
            szURL = new StringBuilder(String.valueOf(szURL)).append(((ResourceItemData) this.marrPath.get(i)).szGUID).append("/").toString();
        }
        szURL = new StringBuilder(String.valueOf(szURL)).append(szTargetPath).toString();
        if (!this.mSuffix.isEmpty()) {
            szURL = new StringBuilder(String.valueOf(szURL)).append("?").append(this.mSuffix).toString();
        }
        if (nPageIndex > 1 && !this.mMoreSuffix.isEmpty()) {
            if (szURL.indexOf("?") == -1) {
                szURL = new StringBuilder(String.valueOf(szURL)).append("?").toString();
            } else {
                szURL = new StringBuilder(String.valueOf(szURL)).append("&").toString();
            }
            szURL = new StringBuilder(String.valueOf(szURL)).append(this.mMoreSuffix).append("=").append(String.valueOf(nPageIndex)).toString();
        }
        this.mNoDataView.setVisibility(8);
        HttpItemObject httpItem = new HttpItemObject(szURL, getActivity());
        httpItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                String szData = ItemObject.readTextData();
                if (szData != null && !szData.isEmpty()) {
                    boolean z = false;
                    try {
                        z = true;
                        SuccessListener.onJsonObject(new JSONObject(szData));
                    } catch (JSONException e) {
                    }
                    if (!z) {
                        try {
                            z = true;
                            SuccessListener.onJsonArray(new JSONArray(szData));
                        } catch (JSONException e2) {
                        }
                    }
                    if (!z) {
                        RESTLibraryFragment.this.marrData.clear();
                        RESTLibraryFragment.this.addBack();
                        RESTLibraryFragment.this.mAdapter.notifyDataSetChanged();
                        RESTLibraryFragment.this.reportMessage("当前没有数据，请选择别的目录。");
                    }
                }
            }
        });
        httpItem.setFailureListener(this.mFailureListener);
        if (szPostData != null) {
            httpItem.writeTextData(szPostData);
            httpItem.setReadOperation(false);
        }
        httpItem.setNeedAuthenticate(true);
        VirtualNetworkObject.addToQueue(httpItem);
    }

    private void doSearchRestCall(final String szKeywords, final int nPageIndex, final OnRestSuccessListener SuccessListener) {
        String szURL = new StringBuilder(String.valueOf(this.mRestBaseURL + this.mPathSuffix)).append("search?keywords=").append(szKeywords).toString();
        if (!this.mSuffix.isEmpty()) {
            szURL = new StringBuilder(String.valueOf(szURL)).append("&").append(this.mSuffix).toString();
        }
        if (!this.mMoreSuffix.isEmpty()) {
            szURL = new StringBuilder(String.valueOf(szURL)).append("&").append(this.mMoreSuffix).append("=").append(String.valueOf(nPageIndex)).toString();
        }
        HttpItemObject httpItem = new HttpItemObject(szURL, getActivity());
        httpItem.setSuccessListener(new OnSuccessListener() {
            public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                String szData = ItemObject.readTextData();
                if (nPageIndex <= 1) {
                    ResourceItemData searchFolder = new ResourceItemData();
                    searchFolder.szGUID = "search";
                    searchFolder.szResourceGUID = szKeywords;
                    searchFolder.bReadOnly = true;
                    RESTLibraryFragment.this.marrPath.add(searchFolder);
                }
                if (szData != null && !szData.isEmpty()) {
                    boolean z = false;
                    try {
                        z = true;
                        SuccessListener.onJsonObject(new JSONObject(szData));
                    } catch (JSONException e) {
                    }
                    if (!z) {
                        try {
                            SuccessListener.onJsonArray(new JSONArray(szData));
                        } catch (JSONException e2) {
                        }
                    }
                }
            }
        });
        httpItem.setFailureListener(this.mFailureListener);
        httpItem.setNeedAuthenticate(true);
        VirtualNetworkObject.addToQueue(httpItem);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        if (this.mRootView != null) {
            return this.mRootView;
        }
        if (getArguments() != null) {
            this.mModuleName = getArguments().getString(ARGUMENT_NAME_SUFFIX, "");
            this.mPathSuffix = getArguments().getString(ARGUMENT_PATH_SUFFIX, "");
            this.mSuffix = getArguments().getString(ARGUMENT_PARAM_SUFFIX, "");
            this.mGetQuestionSuffix = getArguments().getString(ARGUMENT_QUESTION_SUFFIX, "");
            this.mMoreSuffix = getArguments().getString("", this.mMoreSuffix);
            this.mbAllowAddToQuestionBook = getArguments().getBoolean(ARGUMENT_ALLOW_ADD_TO_QUESTION_BOOK, false);
            this.mbAllowBookMark = getArguments().getBoolean(ARGUMENT_ALLOW_ADD_BOOKMARK, true);
            this.mbAllowAddToResourceLibrary = getArguments().getBoolean(ARGUMENT_ALLOW_ADD_TO_RESOURCELIBRARY, false);
        }
        this.mRootView = inflater.inflate(R.layout.fragment_myilibrary, null);
        this.mRecyclerView = (RecyclerView) this.mRootView.findViewById(R.id.recentView);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        this.mNoDataView = (TextView) this.mRootView.findViewById(R.id.textViewNoData);
        this.mNoDataView.setVisibility(4);
        this.mRestBaseURL = MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/restfuldatasource/";
        this.mAdapter = new SimpleRecyclerViewAdapterWrapper(getActivity(), R.layout.layout_resourceitem, this);
        this.mRecyclerView.setAdapter(this.mAdapter);
        setHasOptionsMenu(true);
        refresh();
        return this.mRootView;
    }

    public boolean goBack() {
        if (this.marrPath.size() <= 0) {
            return false;
        }
        this.mszSelectedGUID = ((ResourceItemData) this.marrPath.get(this.marrPath.size() - 1)).szGUID;
        this.marrPath.remove(this.marrPath.size() - 1);
        refresh();
        return true;
    }

    private void reportMessage(String szMessage) {
        this.mNoDataView.setVisibility(0);
        this.mNoDataView.setText(szMessage);
    }

    public void onClick(View v) {
        final ResourceItemData data = (ResourceItemData) v.getTag();
        if (v.getId() == R.id.cardViewResource) {
            if (!data.bFolder) {
                this.mszSelectedGUID = data.szGUID;
                this.mAdapter.notifyDataSetChanged();
                final OnSuccessListener successListener;
                if (data.nType != 0) {
                    successListener = new OnSuccessListener() {
                        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                            String szGUID = data.szGUID;
                            ResourceParser parser = new ResourceParser();
                            if (parser.initialize(RESTLibraryFragment.this.getActivity(), ItemObject.readTextData())) {
                                szGUID = parser.getGUID();
                            }
                            Intent Intent;
                            if (data.nType < 1000 || data.nType >= 2000) {
                                Intent = new Intent(RESTLibraryFragment.this.getActivity(), ResourceDetailActivity.class);
                                Intent.putExtra(CommentComponent.RESOURCEGUID, szGUID);
                                Intent.putExtra("title", data.szTitle);
                                Intent.putExtra("isquestion", false);
                                Intent.putExtra("resourcetype", data.nType);
                                if (RESTLibraryFragment.this.mbAllowAddToResourceLibrary) {
                                    Intent.putExtra(RESTLibraryFragment.ARGUMENT_ALLOW_ADD_TO_RESOURCELIBRARY, true);
                                }
                                RESTLibraryFragment.this.startActivity(Intent);
                                return;
                            }
                            Intent = new Intent(RESTLibraryFragment.this.getActivity(), ResourcesViewActivity2.class);
                            Intent.putExtra(CommentComponent.RESOURCEGUID, szGUID);
                            Intent.putExtra("scheduleguid", szGUID);
                            Intent.putExtra("displayanswers", true);
                            Intent.putExtra("enableunlock", true);
                            Intent.putExtra(UserHonourFragment.USERCLASSGUID, "");
                            Intent.putExtra("userclassname", "");
                            Intent.putExtra("resourcetype", data.nType);
                            RESTLibraryFragment.this.startActivity(Intent);
                        }
                    };
                    ResourceItemObject ResourceObject = new ResourceItemObject(data.szGUID, getActivity());
                    ResourceObject.setSuccessListener(new OnSuccessListener() {
                        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                            if (nReturnCode == ErrorCode.ERROR_NO_DATA) {
                                RESTLibraryFragment.this.doCacheOperation(data.szGUID, successListener);
                            } else {
                                successListener.OnDataSuccess(ItemObject, nReturnCode);
                            }
                        }
                    });
                    ResourceObject.setFailureListener(new OnFailureListener() {
                        public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                            if (nReturnCode == 2) {
                                RESTLibraryFragment.this.doCacheOperation(data.szGUID, successListener);
                            }
                        }
                    });
                    ResourceObject.setAlwaysActiveCallbacks(true);
                    VirtualNetworkObject.addToQueue(ResourceObject);
                } else {
                    final String szOriginalGUID = data.szGUID;
                    String szFixedGUID = data.szGUID;
                    if (szFixedGUID.indexOf("/") != -1) {
                        szFixedGUID = szFixedGUID.substring(szFixedGUID.indexOf("/") + 1);
                    }
                    final String szPureGUID = szFixedGUID;
                    successListener = new OnSuccessListener() {
                        public void OnDataSuccess(ItemObject ItemObject, int nReturnCode) {
                            Intent Intent = new Intent(RESTLibraryFragment.this.getActivity(), ResourceDetailActivity.class);
                            Intent.putExtra(CommentComponent.RESOURCEGUID, szPureGUID);
                            Intent.putExtra("title", data.szTitle);
                            Intent.putExtra("isquestion", true);
                            Intent.putExtra("resourcetype", data.nType);
                            if (MyiBaseApplication.getCommonVariables().UserInfo.nUserType == 0) {
                                Intent.putExtra("allowSwitchAnswers", true);
                            }
                            if (RESTLibraryFragment.this.mbAllowAddToQuestionBook) {
                                Intent.putExtra(RESTLibraryFragment.ARGUMENT_ALLOW_ADD_TO_QUESTION_BOOK, true);
                            }
                            if (RESTLibraryFragment.this.mbAllowAddToResourceLibrary) {
                                Intent.putExtra(RESTLibraryFragment.ARGUMENT_ALLOW_ADD_TO_RESOURCELIBRARY, true);
                            }
                            RESTLibraryFragment.this.startActivity(Intent);
                        }
                    };
                    QuestionItemObject ResourceObject2 = new QuestionItemObject(szPureGUID, getActivity());
                    ResourceObject2.setSuccessListener(successListener);
                    ResourceObject2.setFailureListener(new OnFailureListener() {
                        public void OnDataFailure(ItemObject ItemObject, int nReturnCode) {
                            if (nReturnCode == 2) {
                                RESTLibraryFragment.this.doCacheOperation(szOriginalGUID, successListener);
                            }
                        }
                    });
                    ResourceObject2.setAlwaysActiveCallbacks(true);
                    VirtualNetworkObject.addToQueue(ResourceObject2);
                }
            } else if (data.szGUID.equalsIgnoreCase("back")) {
                goBack();
            } else if (data.szGUID.equalsIgnoreCase("more")) {
                loadMore();
            } else {
                this.marrPath.add(data);
                refresh();
            }
        }
        if (v.getId() == R.id.imageViewRename) {
            boolean z = data.bFolder;
        }
        v.getId();
        int i = R.id.imageViewShare;
        v.getId();
        i = R.id.imageViewDelete;
    }

    private void refresh() {
        this.mnPageIndex = 1;
        doRestCall("", null, this.mnPageIndex, this.mRestSuccessListener);
    }

    private void loadMore() {
        ResourceItemData lastPath = new ResourceItemData();
        if (this.marrPath.size() > 0) {
            lastPath = (ResourceItemData) this.marrPath.get(this.marrPath.size() - 1);
        }
        if (lastPath.szGUID.equalsIgnoreCase("search")) {
            this.mnPageIndex++;
            doSearchRestCall(lastPath.szResourceGUID, this.mnPageIndex, this.mRestMoreSuccessListener);
            return;
        }
        this.mnPageIndex++;
        doRestCall("", null, this.mnPageIndex, this.mRestMoreSuccessListener);
    }

    private void addBack() {
        if (this.marrPath.size() > 0) {
            ResourceItemData lastPath = new ResourceItemData();
            String szLastPath = "上一级";
            if (this.marrPath.size() > 0) {
                szLastPath = ((ResourceItemData) this.marrPath.get(this.marrPath.size() - 1)).szTitle;
            }
            if (szLastPath.isEmpty()) {
                szLastPath = "上一级";
            }
            ResourceItemData data = new ResourceItemData();
            data.bFolder = true;
            data.szGUID = "back";
            data.szTitle = "返回到" + szLastPath;
            this.marrData.add(0, data);
        }
    }

    private void addMore() {
        if (this.marrPath.size() > 0 && !this.mMoreSuffix.isEmpty()) {
            int i = 0;
            while (i < this.marrData.size()) {
                if (((ResourceItemData) this.marrData.get(i)).bFolder) {
                    i++;
                } else {
                    ResourceItemData data = new ResourceItemData();
                    data.bFolder = true;
                    data.szGUID = "more";
                    data.szTitle = "点击加载更多内容...";
                    this.marrData.add(data);
                    return;
                }
            }
        }
    }

    private boolean parserJsonObject(JSONObject oneObject) {
        ResourceItemData data = new ResourceItemData();
        boolean bResult = false;
        try {
            if (oneObject.has("mountPoint") && oneObject.getBoolean("mountPoint")) {
                data.bFolder = true;
                data.szGUID = oneObject.getString("guid");
                data.szTitle = oneObject.getString("title");
                data.bReadOnly = true;
                this.marrData.add(data);
                return true;
            }
            String szNodeType = oneObject.getString("nodeType");
            data.szGUID = oneObject.getString("guid");
            data.szTitle = oneObject.getString("title");
            data.szDateTime = oneObject.optString(MediaMetadataRetriever.METADATA_KEY_DATE);
            if (oneObject.has("readOnly")) {
                data.bReadOnly = oneObject.getBoolean("readOnly");
            } else {
                data.bReadOnly = true;
            }
            if (szNodeType.equalsIgnoreCase("kp")) {
                data.bFolder = true;
                this.marrData.add(data);
            } else if (szNodeType.equalsIgnoreCase("question")) {
                data.bFolder = false;
                data.szAuthor = oneObject.optString(MediaMetadataRetriever.METADATA_KEY_AUTHOR);
                this.marrData.add(data);
                bResult = true;
            } else if (szNodeType.equalsIgnoreCase("resource")) {
                data.bFolder = false;
                data.szAuthor = oneObject.optString(MediaMetadataRetriever.METADATA_KEY_AUTHOR);
                data.nType = oneObject.optInt("type");
                if (data.nType == 0) {
                    data.nType = 1;
                }
                this.marrData.add(data);
                bResult = true;
            } else {
                bResult = false;
            }
            return bResult;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int nItemID = item.getItemId();
        int i;
        if (nItemID == R.id.action_addbookmark) {
            String szURL = getURL();
            String szName = getPathText();
            ArrayList arrURLs = new ArrayList(this.marrFavoriteURLs);
            ArrayList<String> arrNames = new ArrayList(this.marrFavoriteItems);
            Integer[] pos = new Integer[1];
            if (Utilities.isInArray(arrURLs, szURL, pos)) {
                arrNames.remove(pos[0].intValue());
                arrURLs.remove(pos[0].intValue());
            } else {
                arrNames.add(0, szName);
                arrURLs.add(0, szURL);
            }
            Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
            JSONArray jsonArray = new JSONArray();
            for (i = 0; i < arrNames.size(); i++) {
                JSONObject one = new JSONObject();
                try {
                    one.put(StudentAnswerImageService.LISTURL, arrURLs.get(i));
                    one.put(ARGUMENT_NAME_SUFFIX, arrNames.get(i));
                    jsonArray.put(i, one);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            editor.putString("RESTFavoriteItems_" + this.mModuleName, jsonArray.toString());
            editor.commit();
            ActivityCompat.invalidateOptionsMenu(getActivity());
            return true;
        } else if (!this.mMapFavoriteItems.containsKey(Integer.valueOf(nItemID))) {
            return super.onOptionsItemSelected(item);
        } else {
            String szTitle = (String) this.mMapFavoriteItemNames.get(Integer.valueOf(nItemID));
            String[] arrURL = ((String) this.mMapFavoriteItems.get(Integer.valueOf(nItemID))).split("/");
            String[] arrTitles = szTitle.split("/");
            this.marrPath.clear();
            for (i = 0; i < arrURL.length; i++) {
                ResourceItemData oneItem = new ResourceItemData();
                oneItem.szGUID = arrURL[i];
                oneItem.szTitle = arrTitles[i];
                oneItem.bReadOnly = true;
                oneItem.bFolder = true;
                this.marrPath.add(oneItem);
            }
            refresh();
            return true;
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.mbAllowBookMark) {
            inflater.inflate(R.menu.menu_fragment_restlibrary, menu);
            this.mMapFavoriteItems.clear();
            this.mMapFavoriteItemNames.clear();
            this.marrFavoriteItems.clear();
            this.marrFavoriteURLs.clear();
            this.mszData = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("RESTFavoriteItems_" + this.mModuleName, "");
            SubMenu submenu;
            if (this.mszData.isEmpty()) {
                submenu = menu.findItem(R.id.action_bookmarklist).getSubMenu();
                if (submenu != null) {
                    submenu.add("当前没有收藏任何内容，请先收藏一些目录。");
                }
            } else {
                int i;
                try {
                    JSONArray jsonArray = new JSONArray(this.mszData);
                    for (i = 0; i < jsonArray.length(); i++) {
                        JSONObject one = jsonArray.getJSONObject(i);
                        this.marrFavoriteItems.add(one.getString(ARGUMENT_NAME_SUFFIX));
                        this.marrFavoriteURLs.add(one.getString(StudentAnswerImageService.LISTURL));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                submenu = menu.findItem(R.id.action_bookmarklist).getSubMenu();
                if (submenu != null) {
                    i = 10000;
                    Iterator<String> it = this.marrFavoriteItems.iterator();
                    Iterator<String> it2 = this.marrFavoriteURLs.iterator();
                    while (it.hasNext() && it2.hasNext()) {
                        String szTitle = (String) it.next();
                        String szURL = (String) it2.next();
                        submenu.add(1, R.id.action_bookmarklist + i, 0, szTitle);
                        this.mMapFavoriteItems.put(Integer.valueOf(R.id.action_bookmarklist + i), szURL);
                        this.mMapFavoriteItemNames.put(Integer.valueOf(R.id.action_bookmarklist + i), szTitle);
                        i++;
                    }
                }
            }
            if (this.mMapFavoriteItems.containsValue(getURL())) {
                menu.findItem(R.id.action_addbookmark).setIcon(new IconDrawable(getActivity(), FontAwesomeIcons.fa_star).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
                return;
            }
            menu.findItem(R.id.action_addbookmark).setIcon(new IconDrawable(getActivity(), FontAwesomeIcons.fa_star_o).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        }
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void startSearch(String szKeyword) {
        this.mnPageIndex = 1;
        doSearchRestCall(szKeyword, this.mnPageIndex, this.mRestSuccessListener);
    }

    public int getCount() {
        return this.marrData.size();
    }

    private boolean isReadOnly() {
        if (this.marrPath.size() > 0) {
            return ((ResourceItemData) this.marrPath.get(this.marrPath.size() - 1)).bReadOnly;
        }
        return true;
    }

    public void getView(int position, ViewHolder viewHolder) {
        ResourceItemData data = (ResourceItemData) this.marrData.get(position);
        TextView Title = (TextView) viewHolder.findViewById(R.id.textViewTitle);
        TextView Date = (TextView) viewHolder.findViewById(R.id.textViewTime);
        TextView Description = (TextView) viewHolder.findViewById(R.id.textViewContent);
        TextView FolderName = (TextView) viewHolder.findViewById(R.id.textViewFolderName);
        TextView ItemInfo = (TextView) viewHolder.findViewById(R.id.textViewItemInfo);
        ImageView Thumbnail = (ImageView) viewHolder.findViewById(R.id.imageViewThumbnail);
        LinearLayout CardView = (LinearLayout) viewHolder.findViewById(R.id.cardViewResource);
        CardView.setTag(data);
        CardView.setOnClickListener(this);
        if (data.szGUID.equalsIgnoreCase(this.mszSelectedGUID)) {
            CardView.setSelected(true);
        } else {
            CardView.setSelected(false);
        }
        ImageView ImageShareIcon = (ImageView) viewHolder.findViewById(R.id.imageViewShare);
        ImageView ImageDeleteIcon = (ImageView) viewHolder.findViewById(R.id.imageViewDelete);
        ImageView ImageRenameIcon = (ImageView) viewHolder.findViewById(R.id.imageViewRename);
        ImageShareIcon.setImageDrawable(new IconDrawable(getActivity(), FontAwesomeIcons.fa_share_alt).colorRes(17170432).actionBarSize());
        ImageDeleteIcon.setImageDrawable(new IconDrawable(getActivity(), FontAwesomeIcons.fa_trash_o).colorRes(17170432).actionBarSize());
        ImageRenameIcon.setImageDrawable(new IconDrawable(getActivity(), FontAwesomeIcons.fa_pencil).colorRes(17170432).actionBarSize());
        ImageShareIcon.setOnClickListener(this);
        ImageDeleteIcon.setOnClickListener(this);
        ImageRenameIcon.setOnClickListener(this);
        Thumbnail.setVisibility(0);
        ItemInfo.setVisibility(8);
        if (data.bFolder) {
            FolderName.setText(data.szTitle);
            FolderName.setVisibility(0);
            Title.setVisibility(4);
            Date.setVisibility(4);
            Description.setVisibility(8);
            ImageShareIcon.setVisibility(8);
            ImageRenameIcon.setVisibility(0);
            ImageDeleteIcon.setVisibility(0);
            if (data.szGUID.equalsIgnoreCase("back")) {
                Thumbnail.setImageResource(R.drawable.folder_back);
                ImageShareIcon.setVisibility(8);
                ImageRenameIcon.setVisibility(8);
                ImageDeleteIcon.setVisibility(8);
            } else if (data.szGUID.equalsIgnoreCase("more")) {
                FolderName.setVisibility(8);
                ItemInfo.setVisibility(0);
                ItemInfo.setText(data.szTitle);
                Thumbnail.setVisibility(4);
                ImageShareIcon.setVisibility(8);
                ImageRenameIcon.setVisibility(8);
                ImageDeleteIcon.setVisibility(8);
            } else {
                Thumbnail.setImageResource(R.drawable.folder);
            }
        } else {
            FolderName.setVisibility(4);
            ImageShareIcon.setVisibility(0);
            ImageRenameIcon.setVisibility(8);
            ImageDeleteIcon.setVisibility(0);
            Title.setVisibility(0);
            Date.setVisibility(0);
            Description.setVisibility(0);
            if (data.nType == 0) {
                Title.setVisibility(8);
                Date.setVisibility(8);
                Description.setText(data.szTitle);
                Thumbnail.setImageResource(R.drawable.ic_question);
            } else {
                Title.setText("资源");
                Picasso.with(getContext()).load(MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/getresourcethumbnail?guid=" + data.szGUID).error(R.drawable.ic_placehold_small_gray).into(Thumbnail);
                if (data.nType >= 1000 && data.nType <= 1999) {
                    Title.setText("备课资源");
                }
                if (data.nType == 11) {
                    Title.setText("视频资源");
                }
                if (data.nType == 21) {
                    Title.setText("音频资源");
                }
                if (data.nType == 41) {
                    Title.setText("图片资源");
                }
                if (data.nType >= 4000 && data.nType <= 4999) {
                    Title.setText("课堂实录");
                }
                Description.setText(data.szTitle);
            }
            if (data.szAuthor == null || data.szAuthor.isEmpty()) {
                Date.setText(data.szDateTime);
            } else {
                Date.setText(data.szAuthor + "，" + data.szDateTime);
            }
            ImageShareIcon.setVisibility(0);
            ImageRenameIcon.setVisibility(8);
        }
        if (isReadOnly() || data.bReadOnly) {
            ImageRenameIcon.setVisibility(8);
            ImageDeleteIcon.setVisibility(8);
        }
        ImageShareIcon.setVisibility(8);
    }
}
