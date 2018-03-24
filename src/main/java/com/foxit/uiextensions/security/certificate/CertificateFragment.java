package com.foxit.uiextensions.security.certificate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.dialog.AppDialogManager;
import com.foxit.uiextensions.controls.dialog.MatchDialog.DialogListener;
import com.foxit.uiextensions.controls.dialog.UIMatchDialog;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppFileUtil;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.UIToast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CertificateFragment extends UIMatchDialog {
    private static final int BITMAP_1 = 1;
    private static final int BITMAP_2 = 2;
    public static final int CERLIST_TYPE_DECRYPT = 2;
    public static final int CERLIST_TYPE_ENCRYPT = 1;
    public static final int CERLIST_TYPE_SIGNATURE = 3;
    public static final int MESSAGE_FINISH = 18;
    public static final int MESSAGE_UPDATE = 17;
    private static final int TEMPLATE = 0;
    private BaseAdapter mAdapter = new BaseAdapter() {
        public int getCount() {
            return CertificateFragment.this.mItems.size();
        }

        public Object getItem(int position) {
            return CertificateFragment.this.mItems.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(CertificateFragment.this.mContext, R.layout.rv_security_certlist_item, null);
                holder.nameTextView = (TextView) convertView.findViewById(R.id.rv_security_certlist_item_tv);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.rv_security_certlist_item_cb);
                holder.infoBtn = (ImageView) convertView.findViewById(R.id.rv_security_certlist_item_info_iv);
                holder.templateLayout = (LinearLayout) convertView.findViewById(R.id.rv_security_certlist_item_sigshape);
                holder.templateImageView = (ImageView) convertView.findViewById(R.id.rv_security_certlist_item_sigshape_info_iv);
                holder.inkSignBmp1ImageView = (ImageView) convertView.findViewById(R.id.rv_security_certlist_item_sigshape_last1_iv);
                holder.inkSignBmp2ImageView = (ImageView) convertView.findViewById(R.id.rv_security_certlist_item_sigshape_last2_iv);
                holder.templateRadioButton = (RadioButton) convertView.findViewById(R.id.rv_security_certlist_item_sigshape_info_rb);
                holder.inkSignBmp1RadioButton = (RadioButton) convertView.findViewById(R.id.rv_security_certlist_item_sigshape_last1_rb);
                holder.inkSignBmp2RadioButton = (RadioButton) convertView.findViewById(R.id.rv_security_certlist_item_sigshape_last2_rb);
                convertView.setTag(holder);
                if (AppDisplay.getInstance(CertificateFragment.this.mContext).isPad()) {
                    LayoutParams LP = new LayoutParams(-1, -1);
                    LP.height = CertificateFragment.this.mContext.getResources().getDimensionPixelSize(R.dimen.ux_list_item_height_1l_pad);
                    ((RelativeLayout) convertView.findViewById(R.id.rv_security_certlist_item_ly)).setLayoutParams(LP);
                }
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final CertificateFileInfo info = (CertificateFileInfo) CertificateFragment.this.mItems.get(position);
            holder.nameTextView.setText(info.fileName);
            if (!CertificateFragment.this.mSignature) {
                holder.checkBox.setChecked(info.selected);
            } else if (position == CertificateFragment.this.sigPostion && info.selected) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }
            final ViewHolder fHolder = holder;
            if (!CertificateFragment.this.mSignature) {
                holder.templateLayout.setVisibility(8);
            } else if (holder.checkBox.isChecked()) {
                fHolder.templateImageView.setImageBitmap(CertificateFragment.this.mTemplateBmp);
                if (CertificateFragment.this.mInkSignBmp1 != null) {
                    fHolder.inkSignBmp1ImageView.setImageBitmap(CertificateFragment.this.mInkSignBmp1);
                    fHolder.inkSignBmp1ImageView.setVisibility(0);
                    fHolder.inkSignBmp1RadioButton.setVisibility(0);
                } else {
                    fHolder.inkSignBmp1ImageView.setVisibility(8);
                    fHolder.inkSignBmp1RadioButton.setVisibility(8);
                }
                if (CertificateFragment.this.mInkSignBmp2 != null) {
                    fHolder.inkSignBmp2ImageView.setImageBitmap(CertificateFragment.this.mInkSignBmp2);
                    fHolder.inkSignBmp2ImageView.setVisibility(0);
                    fHolder.inkSignBmp2RadioButton.setVisibility(0);
                } else {
                    fHolder.inkSignBmp2ImageView.setVisibility(8);
                    fHolder.inkSignBmp2RadioButton.setVisibility(8);
                }
            } else {
                holder.templateLayout.setVisibility(8);
            }
            holder.infoBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (info == null) {
                        return;
                    }
                    if (info.isCertFile) {
                        if (info.certificateInfo == null && CertificateFragment.this.mViewSupport != null) {
                            info.certificateInfo = CertificateFragment.this.mViewSupport.getCertSupport().getCertificateInfo(info.filePath);
                        }
                        if (info.certificateInfo == null) {
                            UIToast.getInstance(CertificateFragment.this.mContext).show(R.string.rv_security_certfrompfx_failed);
                        } else if (CertificateFragment.this.mViewSupport != null) {
                            CertificateFragment.this.mViewSupport.showPermissionDialog(info);
                        }
                    } else if (AppUtil.isEmpty(info.password) || info.certificateInfo == null) {
                        if (CertificateFragment.this.mViewSupport != null) {
                            CertificateFragment.this.mViewSupport.showPasswordDialog(info, null);
                        }
                    } else if (CertificateFragment.this.mViewSupport != null) {
                        CertificateFragment.this.mViewSupport.showPermissionDialog(info);
                    }
                }
            });
            holder.checkBox.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    CertificateFragment.this._onCheckboxClicked(info, position, fHolder);
                }
            });
            holder.templateRadioButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    AnonymousClass2.this.setRadioButtonStatus(fHolder, 0);
                    info.radioButtonID = 0;
                }
            });
            holder.inkSignBmp1RadioButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    AnonymousClass2.this.setRadioButtonStatus(fHolder, 1);
                    info.radioButtonID = 1;
                }
            });
            holder.inkSignBmp2RadioButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    AnonymousClass2.this.setRadioButtonStatus(fHolder, 2);
                    info.radioButtonID = 2;
                }
            });
            return convertView;
        }

        private void setRadioButtonStatus(ViewHolder holder, int state) {
            boolean z;
            boolean z2 = true;
            RadioButton radioButton = holder.templateRadioButton;
            if (state == 0) {
                z = true;
            } else {
                z = false;
            }
            radioButton.setChecked(z);
            radioButton = holder.inkSignBmp1RadioButton;
            if (state == 1) {
                z = true;
            } else {
                z = false;
            }
            radioButton.setChecked(z);
            RadioButton radioButton2 = holder.inkSignBmp2RadioButton;
            if (state != 2) {
                z2 = false;
            }
            radioButton2.setChecked(z2);
        }
    };
    private ICertDialogCallback mCertCallback;
    private Context mContext;
    private boolean mDoEncrypt;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 17) {
                File file = msg.obj;
                if (file != null && file.exists() && file.canRead() && CertificateFragment.this.addInfo(file.getName(), file.getPath(), file.getName().endsWith(".cer"))) {
                    CertificateFragment.this.mAdapter.notifyDataSetChanged();
                }
            } else if (msg.what == 18) {
                TextView note = (TextView) CertificateFragment.this.mView.findViewById(R.id.rv_security_certlist_listtitle_tv);
                if (CertificateFragment.this.mItems.size() <= 0) {
                    note.setText(R.string.rv_security_certlist_nocerificatefile);
                } else {
                    note.setText(R.string.rv_certlist_note);
                }
            }
        }
    };
    private Bitmap mInkSignBmp1;
    private Bitmap mInkSignBmp2;
    private List<CertificateFileInfo> mItems = new ArrayList();
    private CertificateSearchRunnable mSearchRunnable;
    private SparseArray<CertificateFileInfo> mSelectedItems = new SparseArray();
    private boolean mSignature;
    private Bitmap mTemplateBmp;
    private View mView;
    private CertificateViewSupport mViewSupport;
    private int sigPostion = -1;

    public interface ICertDialogCallback {
        void result(boolean z, Object obj, Bitmap bitmap);
    }

    final class ViewHolder {
        public CheckBox checkBox;
        public ImageView infoBtn;
        public ImageView inkSignBmp1ImageView;
        public RadioButton inkSignBmp1RadioButton;
        public ImageView inkSignBmp2ImageView;
        public RadioButton inkSignBmp2RadioButton;
        public TextView nameTextView;
        public ImageView templateImageView;
        public LinearLayout templateLayout;
        public RadioButton templateRadioButton;

        ViewHolder() {
        }
    }

    public CertificateFragment(Context context) {
        super(context);
        this.mContext = context;
        this.mSearchRunnable = new CertificateSearchRunnable(this.mContext);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        cleanup();
        if (this.mCertCallback != null) {
            this.mCertCallback.result(false, null, null);
        }
        return false;
    }

    public void init(CertificateViewSupport support, ICertDialogCallback callback, int type) {
        boolean z = false;
        this.mViewSupport = support;
        this.mCertCallback = callback;
        this.mSignature = false;
        if (type == 1) {
            this.mDoEncrypt = true;
        } else if (type == 2) {
            this.mDoEncrypt = false;
        } else if (type == 3) {
            this.mDoEncrypt = false;
            this.mSignature = true;
        }
        createView();
        if (!this.mDoEncrypt) {
            z = true;
        }
        searchCertificateFile(z);
    }

    private void searchCertificateFile(boolean isOnlyFindPfx) {
        this.mItems.clear();
        this.mSelectedItems.clear();
        getDataBySQLite(isOnlyFindPfx);
        ((TextView) this.mView.findViewById(R.id.rv_security_certlist_listtitle_tv)).setText(R.string.rv_certlist_note_searching);
        this.mSearchRunnable.init(this.mHandler, isOnlyFindPfx);
        new Thread(this.mSearchRunnable).start();
    }

    private boolean addInfo(String fileName, String filePath, boolean isCert) {
        CertificateFileInfo info = new CertificateFileInfo();
        info.permCode = 3900;
        info.isCertFile = isCert;
        info.fileName = fileName;
        info.filePath = filePath;
        if (this.mItems.contains(info)) {
            return false;
        }
        if (isCert) {
            updateInfo(info);
        }
        return this.mItems.add(info);
    }

    private void getDataBySQLite(boolean isOnlyFindPfx) {
        int i;
        CertificateFileInfo info;
        if (isOnlyFindPfx) {
            this.mViewSupport.getDataSupport().getAllPfxs(this.mItems);
            for (i = this.mItems.size() - 1; i >= 0; i--) {
                info = (CertificateFileInfo) this.mItems.get(i);
                File file = new File(info.filePath);
                if (!AppFileUtil.isSDAvailable() || file.exists()) {
                    info.certificateInfo = this.mViewSupport.getCertSupport().verifyPassword(info.filePath, info.password);
                    if (info.certificateInfo == null) {
                        info.password = null;
                    }
                } else {
                    this.mViewSupport.getDataSupport().removePfx(info.filePath);
                    this.mItems.remove(i);
                }
            }
            if (this.mItems.size() > 0) {
                this.mHandler.obtainMessage(17).sendToTarget();
                return;
            }
            return;
        }
        this.mViewSupport.getDataSupport().getAllPfxs(this.mItems);
        this.mViewSupport.getDataSupport().getAllCerts(this.mItems);
        for (i = this.mItems.size() - 1; i >= 0; i--) {
            info = (CertificateFileInfo) this.mItems.get(i);
            file = new File(info.filePath);
            if (AppFileUtil.isSDAvailable() && !file.exists()) {
                if (info.isCertFile) {
                    this.mViewSupport.getDataSupport().removeCert(info.filePath);
                } else {
                    this.mViewSupport.getDataSupport().removePfx(info.filePath);
                }
                this.mItems.remove(i);
            } else if (!info.isCertFile) {
                info.certificateInfo = this.mViewSupport.getCertSupport().verifyPassword(info.filePath, info.password);
                if (info.certificateInfo == null) {
                    info.password = null;
                }
            }
        }
        if (this.mItems.size() > 0) {
            this.mHandler.obtainMessage(17).sendToTarget();
        }
    }

    private void updateInfo(CertificateFileInfo info) {
        if (info.isCertFile) {
            this.mViewSupport.getDataSupport().insertCert(info.issuer, info.publisher, info.serialNumber, info.filePath, info.fileName);
        } else {
            this.mViewSupport.getDataSupport().insertPfx(info.issuer, info.publisher, info.serialNumber, info.filePath, info.fileName, info.password);
        }
    }

    private View createView() {
        if (this.mSignature) {
            int width = (AppDisplay.getInstance(this.mContext).getDialogWidth() * 4) / 7;
            int height = (width * 10) / 16;
            recycleBmp();
            createBitmap(width, height);
        }
        this.mView = View.inflate(this.mContext, R.layout.rv_security_certlist, null);
        ListView listView = (ListView) this.mView.findViewById(R.id.rv_security_certlist_lv);
        listView.setAdapter(this.mAdapter);
        TextView listTitle = (TextView) this.mView.findViewById(R.id.rv_security_certlist_listtitle_tv);
        setContentView(this.mView);
        setTitle(this.mContext.getString(R.string.rv_certlist_note));
        setButton(5);
        setBackButtonVisible(8);
        setButtonEnable(false, 4);
        setListener(new DialogListener() {
            public void onResult(long btType) {
                if (btType == 4) {
                    AppDialogManager.getInstance().dismiss(CertificateFragment.this);
                    if (CertificateFragment.this.mCertCallback != null) {
                        CertificateFileInfo info;
                        if (CertificateFragment.this.mSignature) {
                            CertificateFragment.this.cleanup();
                            CertificateFragment.this.recycleBmp();
                            info = (CertificateFileInfo) CertificateFragment.this.mSelectedItems.valueAt(0);
                            switch (info.radioButtonID) {
                                case 1:
                                    CertificateFragment.this.mCertCallback.result(true, info, CertificateFragment.this.getBmpByInkSignName((String) CertificateFragment.this.getInkSignNames().get(0)));
                                    return;
                                case 2:
                                    CertificateFragment.this.mCertCallback.result(true, info, CertificateFragment.this.getBmpByInkSignName((String) CertificateFragment.this.getInkSignNames().get(1)));
                                    return;
                                default:
                                    CertificateFragment.this.mCertCallback.result(true, info, null);
                                    return;
                            }
                        }
                        CertificateFragment.this.cleanup();
                        SparseArray<CertificateFileInfo> array = CertificateFragment.this.mSelectedItems;
                        int size = array.size();
                        List<CertificateFileInfo> infos = new ArrayList(size);
                        for (int i = 0; i < size; i++) {
                            info = (CertificateFileInfo) array.get(array.keyAt(i));
                            if (info.permCode == 3900) {
                                info.permCode |= 2;
                            }
                            infos.add(info);
                        }
                        array.clear();
                        CertificateFragment.this.mCertCallback.result(true, infos, null);
                    }
                } else if (btType == 1) {
                    CertificateFragment.this.cleanup();
                    if (CertificateFragment.this.mCertCallback != null) {
                        CertificateFragment.this.mCertCallback.result(false, null, null);
                    }
                }
            }

            public void onBackClick() {
            }
        });
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ViewHolder holder = (ViewHolder) view.getTag();
                CertificateFileInfo info = (CertificateFileInfo) CertificateFragment.this.mItems.get(position);
                holder.checkBox.setChecked(!info.selected);
                CertificateFragment.this._onCheckboxClicked(info, position, holder);
            }
        });
        return this.mView;
    }

    private void createBitmap(int width, int height) {
        this.mTemplateBmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        List<String> signNames = getInkSignNames();
        if (signNames == null) {
            this.mInkSignBmp2 = null;
            this.mInkSignBmp1 = null;
            return;
        }
        int size = signNames.size();
        if (size > 1) {
            this.mInkSignBmp2 = getBmpByInkSignName((String) signNames.get(1), width, height);
        }
        if (size > 0) {
            this.mInkSignBmp1 = getBmpByInkSignName((String) signNames.get(0), width, height);
        }
    }

    private void cleanup() {
        if (!this.mSearchRunnable.isStoped()) {
            this.mSearchRunnable.stopSearch();
        }
    }

    private void recycleBmp() {
        if (this.mTemplateBmp != null) {
            if (!this.mTemplateBmp.isRecycled()) {
                this.mTemplateBmp.recycle();
            }
            this.mTemplateBmp = null;
        }
        if (this.mInkSignBmp1 != null) {
            if (!this.mInkSignBmp1.isRecycled()) {
                this.mInkSignBmp1.recycle();
            }
            this.mInkSignBmp1 = null;
        }
        if (this.mInkSignBmp2 != null) {
            if (!this.mInkSignBmp2.isRecycled()) {
                this.mInkSignBmp2.recycle();
            }
            this.mInkSignBmp2 = null;
        }
    }

    private List<String> getInkSignNames() {
        return null;
    }

    private Bitmap getBmpByInkSignName(String name, int width, int height) {
        return null;
    }

    private Bitmap getBmpByInkSignName(String name) {
        return null;
    }

    private void _onCheckboxClicked(final CertificateFileInfo info, final int position, ViewHolder fHolder) {
        this.sigPostion = position;
        if (((CertificateFileInfo) this.mSelectedItems.get(position)) == null) {
            info.selected = true;
            if (!info.isCertFile) {
                if (AppUtil.isEmpty(info.password)) {
                    if (this.mViewSupport != null) {
                        this.mViewSupport.showPasswordDialog(info, new ICertDialogCallback() {
                            public void result(boolean succeed, Object result, Bitmap forSign) {
                                if (CertificateFragment.this.mSignature) {
                                    info.selected = false;
                                }
                                if (!succeed) {
                                    info.selected = false;
                                    CertificateFragment.this.mAdapter.notifyDataSetChanged();
                                    if (CertificateFragment.this.mSignature) {
                                        CertificateFragment.this.setButtonEnable(false, 4);
                                    }
                                } else if (info.certificateInfo.keyUsage == null || !info.certificateInfo.keyUsage[3]) {
                                    if (!CertificateFragment.this.mSignature) {
                                        UIToast.getInstance(CertificateFragment.this.mContext).show(AppResource.getString(CertificateFragment.this.mContext, R.string.rv_security_certlist_pubkey_invalidtype), 0);
                                        info.selected = false;
                                        CertificateFragment.this.mAdapter.notifyDataSetChanged();
                                    } else if (info.certificateInfo.expired) {
                                        UIToast.getInstance(CertificateFragment.this.mContext).show(AppResource.getString(CertificateFragment.this.mContext, R.string.rv_security_certlist_outdate), 0);
                                        info.selected = false;
                                        CertificateFragment.this.mAdapter.notifyDataSetChanged();
                                    } else {
                                        CertificateFragment.this.mSelectedItems.clear();
                                        info.selected = true;
                                        CertificateFragment.this.mSelectedItems.put(position, info);
                                        CertificateFragment.this.setButtonEnable(true, 4);
                                    }
                                } else if (CertificateFragment.this.mDoEncrypt && info.certificateInfo.expired) {
                                    UIToast.getInstance(CertificateFragment.this.mContext).show(AppResource.getString(CertificateFragment.this.mContext, R.string.rv_security_certlist_outdate), 0);
                                    info.selected = false;
                                    CertificateFragment.this.mAdapter.notifyDataSetChanged();
                                } else {
                                    CertificateFragment.this.mSelectedItems.put(position, info);
                                    CertificateFragment.this.setButtonEnable(true, 4);
                                    if (!CertificateFragment.this.mSignature) {
                                        return;
                                    }
                                    if (info.certificateInfo.expired) {
                                        UIToast.getInstance(CertificateFragment.this.mContext).show(AppResource.getString(CertificateFragment.this.mContext, R.string.rv_security_certlist_outdate), 0);
                                        info.selected = false;
                                        CertificateFragment.this.mAdapter.notifyDataSetChanged();
                                        return;
                                    }
                                    CertificateFragment.this.mSelectedItems.clear();
                                    info.selected = true;
                                    CertificateFragment.this.mSelectedItems.put(position, info);
                                    CertificateFragment.this.setButtonEnable(true, 4);
                                }
                            }
                        });
                    }
                    this.mAdapter.notifyDataSetChanged();
                    return;
                } else if ((info.certificateInfo.keyUsage == null || !info.certificateInfo.keyUsage[3]) && !this.mSignature) {
                    UIToast.getInstance(this.mContext).show(AppResource.getString(this.mContext, R.string.rv_security_certlist_pubkey_invalidtype), 0);
                    info.selected = false;
                    this.mAdapter.notifyDataSetChanged();
                    return;
                } else if (this.mDoEncrypt && info.certificateInfo.expired) {
                    UIToast.getInstance(this.mContext).show(AppResource.getString(this.mContext, R.string.rv_security_certlist_outdate), 0);
                    info.selected = false;
                    this.mAdapter.notifyDataSetChanged();
                    return;
                }
            }
            if (this.mSignature) {
                if (info.certificateInfo.expired) {
                    UIToast.getInstance(this.mContext).show(AppResource.getString(this.mContext, R.string.rv_security_certlist_outdate), 0);
                    info.selected = false;
                    this.mAdapter.notifyDataSetChanged();
                    return;
                }
                info.selected = true;
                if (this.mSelectedItems.size() > 0) {
                    CertificateFileInfo info_tmp = (CertificateFileInfo) this.mSelectedItems.valueAt(0);
                    if (info_tmp != null) {
                        info_tmp.selected = false;
                        this.mSelectedItems.clear();
                    }
                }
            }
            if (info.certificateInfo == null && this.mViewSupport != null) {
                info.certificateInfo = this.mViewSupport.getCertSupport().getCertificateInfo(info.filePath);
            }
            if ((info.certificateInfo.keyUsage == null || !info.certificateInfo.keyUsage[3]) && !this.mSignature) {
                UIToast.getInstance(this.mContext).show(AppResource.getString(this.mContext, R.string.rv_security_certlist_pubkey_invalidtype), 0);
                info.selected = false;
                this.mAdapter.notifyDataSetChanged();
                return;
            } else if (this.mDoEncrypt && info.certificateInfo.expired) {
                UIToast.getInstance(this.mContext).show(AppResource.getString(this.mContext, R.string.rv_security_certlist_outdate), 0);
                info.selected = false;
                this.mAdapter.notifyDataSetChanged();
                return;
            } else {
                this.mSelectedItems.put(position, info);
            }
        } else {
            fHolder.templateLayout.setVisibility(8);
            this.mSelectedItems.remove(position);
            info.selected = false;
        }
        if (this.mSelectedItems.size() == 0) {
            setButtonEnable(false, 4);
        } else {
            setButtonEnable(true, 4);
        }
        if (!this.mSignature) {
            return;
        }
        if (info.certificateInfo.expired) {
            UIToast.getInstance(this.mContext).show(AppResource.getString(this.mContext, R.string.rv_security_certlist_outdate), 0);
            info.selected = false;
            this.mAdapter.notifyDataSetChanged();
            return;
        }
        this.mAdapter.notifyDataSetChanged();
    }
}
