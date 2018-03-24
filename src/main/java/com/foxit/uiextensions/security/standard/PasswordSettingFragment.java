package com.foxit.uiextensions.security.standard;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.dialog.AppDialogManager;
import com.foxit.uiextensions.controls.dialog.AppDialogManager.CancelListener;
import com.foxit.uiextensions.controls.dialog.MatchDialog.DialogListener;
import com.foxit.uiextensions.controls.dialog.UIEncryptionDialogFragment;
import com.foxit.uiextensions.controls.dialog.UIEncryptionDialogFragment.UIEncryptionDialogEventListener;
import com.foxit.uiextensions.controls.dialog.UIMatchDialog;
import com.foxit.uiextensions.controls.dialog.fileselect.UISaveAsDialog;
import com.foxit.uiextensions.controls.dialog.fileselect.UISaveAsDialog.ISaveAsOnOKClickCallBack;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.UIToast;
import java.util.ArrayList;

public class PasswordSettingFragment extends UIMatchDialog {
    private static final String DIALOGTAG = "FOXIT_SAVEDOC_ENCRYPT_STANDARD";
    public static int EYES_TAG_OWNER = 11;
    public static int EYES_TAG_USER = 10;
    public static int SWITCH_TAG_ANNOT = 5;
    public static int SWITCH_TAG_COPY = 9;
    public static int SWITCH_TAG_FILLFORM = 4;
    public static int SWITCH_TAG_MODIFYDOC = 7;
    public static int SWITCH_TAG_OWNER = 2;
    public static int SWITCH_TAG_PAGE = 6;
    public static int SWITCH_TAG_PRINT = 3;
    public static int SWITCH_TAG_TEXTACCESS = 8;
    public static int SWITCH_TAG_USER = 1;
    public static final String TAG = "PasswordSettingTag";
    private Boolean mFillForm = Boolean.valueOf(true);
    private ArrayList<ImageView> mImageList = null;
    private Boolean mIsAddAnnot = Boolean.valueOf(true);
    private Boolean mIsCopy = Boolean.valueOf(true);
    private Boolean mIsManagePage = Boolean.valueOf(true);
    private Boolean mIsModifyDoc = Boolean.valueOf(true);
    private Boolean mIsPrint = Boolean.valueOf(true);
    private LinearLayout mLinearLayout;
    private OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            boolean z = true;
            boolean z2 = false;
            int tag = ((Integer) v.getTag()).intValue();
            if (tag == PasswordSettingFragment.SWITCH_TAG_USER) {
                boolean z3;
                PasswordSettingFragment passwordSettingFragment = PasswordSettingFragment.this;
                if (PasswordSettingFragment.this.mSettingUserPassword.booleanValue()) {
                    z3 = false;
                } else {
                    z3 = true;
                }
                passwordSettingFragment.mSettingUserPassword = Boolean.valueOf(z3);
            } else if (tag == PasswordSettingFragment.SWITCH_TAG_OWNER) {
                r1 = PasswordSettingFragment.this;
                if (!PasswordSettingFragment.this.mSettingOwnerPassword.booleanValue()) {
                    z2 = true;
                }
                r1.mSettingOwnerPassword = Boolean.valueOf(z2);
            } else if (tag == PasswordSettingFragment.SWITCH_TAG_PRINT) {
                r1 = PasswordSettingFragment.this;
                if (!PasswordSettingFragment.this.mIsPrint.booleanValue()) {
                    z2 = true;
                }
                r1.mIsPrint = Boolean.valueOf(z2);
            } else if (tag == PasswordSettingFragment.SWITCH_TAG_FILLFORM) {
                r1 = PasswordSettingFragment.this;
                if (PasswordSettingFragment.this.mFillForm.booleanValue()) {
                    z = false;
                }
                r1.mFillForm = Boolean.valueOf(z);
                if (!PasswordSettingFragment.this.mFillForm.booleanValue()) {
                    PasswordSettingFragment.this.mIsAddAnnot = Boolean.valueOf(false);
                    PasswordSettingFragment.this.mIsModifyDoc = Boolean.valueOf(false);
                }
            } else if (tag == PasswordSettingFragment.SWITCH_TAG_ANNOT) {
                r1 = PasswordSettingFragment.this;
                if (!PasswordSettingFragment.this.mIsAddAnnot.booleanValue()) {
                    z2 = true;
                }
                r1.mIsAddAnnot = Boolean.valueOf(z2);
                if (PasswordSettingFragment.this.mIsAddAnnot.booleanValue()) {
                    PasswordSettingFragment.this.mFillForm = Boolean.valueOf(true);
                }
            } else if (tag == PasswordSettingFragment.SWITCH_TAG_PAGE) {
                r1 = PasswordSettingFragment.this;
                if (PasswordSettingFragment.this.mIsManagePage.booleanValue()) {
                    z = false;
                }
                r1.mIsManagePage = Boolean.valueOf(z);
                if (!PasswordSettingFragment.this.mIsManagePage.booleanValue()) {
                    PasswordSettingFragment.this.mIsModifyDoc = Boolean.valueOf(false);
                }
            } else if (tag == PasswordSettingFragment.SWITCH_TAG_MODIFYDOC) {
                r1 = PasswordSettingFragment.this;
                if (!PasswordSettingFragment.this.mIsModifyDoc.booleanValue()) {
                    z2 = true;
                }
                r1.mIsModifyDoc = Boolean.valueOf(z2);
                if (PasswordSettingFragment.this.mIsModifyDoc.booleanValue()) {
                    PasswordSettingFragment.this.mFillForm = Boolean.valueOf(true);
                    PasswordSettingFragment.this.mIsManagePage = Boolean.valueOf(true);
                }
            } else if (tag == PasswordSettingFragment.SWITCH_TAG_COPY) {
                r1 = PasswordSettingFragment.this;
                if (!PasswordSettingFragment.this.mIsCopy.booleanValue()) {
                    z2 = true;
                }
                r1.mIsCopy = Boolean.valueOf(z2);
                if (PasswordSettingFragment.this.mIsCopy.booleanValue()) {
                    PasswordSettingFragment.this.mTextAccess = Boolean.valueOf(true);
                }
            } else if (tag == PasswordSettingFragment.SWITCH_TAG_TEXTACCESS) {
                r1 = PasswordSettingFragment.this;
                if (PasswordSettingFragment.this.mTextAccess.booleanValue()) {
                    z = false;
                }
                r1.mTextAccess = Boolean.valueOf(z);
                if (!PasswordSettingFragment.this.mTextAccess.booleanValue()) {
                    PasswordSettingFragment.this.mIsCopy = Boolean.valueOf(false);
                }
            }
            PasswordSettingFragment.this.refreshButton(PasswordSettingFragment.EYES_TAG_USER);
            PasswordSettingFragment.this.refreshButton(PasswordSettingFragment.EYES_TAG_OWNER);
            PasswordSettingFragment.this.refreshAllSwitchView();
            PasswordSettingFragment.this.refreshViewList(PasswordSettingFragment.this.mSettingUserPassword.booleanValue(), PasswordSettingFragment.this.mSettingOwnerPassword.booleanValue());
        }
    };
    private OnTouchListener mOnTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            EditText editText = null;
            int tag = ((Integer) v.getTag()).intValue();
            if (tag == PasswordSettingFragment.EYES_TAG_USER) {
                editText = PasswordSettingFragment.this.mUserEditText;
            }
            if (tag == PasswordSettingFragment.EYES_TAG_OWNER) {
                editText = PasswordSettingFragment.this.mOwnerEditText;
            }
            CharSequence text;
            switch (event.getAction()) {
                case 0:
                    ((ImageView) v).setImageResource(R.drawable.rv_password_check_eye_pressed);
                    editText.setInputType(144);
                    editText.setHeight(AppDisplay.getInstance(PasswordSettingFragment.this.mContext).dp2px(30.0f));
                    text = editText.getText();
                    if (text instanceof Spannable) {
                        Selection.setSelection((Spannable) text, text.length());
                        break;
                    }
                    break;
                case 1:
                case 3:
                    ((ImageView) v).setImageResource(R.drawable.rv_password_check_eye_normal);
                    editText.setInputType(129);
                    editText.setKeyListener(new NumberKeyListener() {
                        public int getInputType() {
                            return 129;
                        }

                        protected char[] getAcceptedChars() {
                            return PasswordConstants.mAcceptChars;
                        }
                    });
                    text = editText.getText();
                    if (text instanceof Spannable) {
                        Selection.setSelection((Spannable) text, text.length());
                        break;
                    }
                    break;
            }
            return true;
        }
    };
    private EditText mOwnerEditText = null;
    private ImageView mOwnerEye;
    private PDFViewCtrl mPdfViewCtrl;
    private Boolean mSettingOwnerPassword = Boolean.valueOf(false);
    private Boolean mSettingUserPassword = Boolean.valueOf(false);
    private PasswordStandardSupport mSupport;
    private Boolean mTextAccess = Boolean.valueOf(true);
    private EditText mUserEditText = null;
    private ImageView mUserEye;
    private ArrayList<View> mViewList = null;
    private String ownerpassword = null;
    private String userpassword = null;

    public PasswordSettingFragment(Context context) {
        super(context);
    }

    public void init(PasswordStandardSupport support, PDFViewCtrl pdfViewCtrl) {
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mSupport = support;
        createView();
    }

    private View createView() {
        View view = View.inflate(this.mContext, R.layout.rv_password_setting, null);
        view.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        this.mSettingUserPassword = Boolean.valueOf(false);
        this.mSettingOwnerPassword = Boolean.valueOf(false);
        this.mIsAddAnnot = Boolean.valueOf(true);
        this.mIsCopy = Boolean.valueOf(true);
        this.mIsManagePage = Boolean.valueOf(true);
        this.mIsPrint = Boolean.valueOf(true);
        this.mLinearLayout = (LinearLayout) view.findViewById(R.id.settinglist);
        this.mImageList = new ArrayList();
        this.mViewList = new ArrayList();
        this.mViewList.add(getSwitchItem(this.mContext.getString(R.string.rv_doc_encrpty_standard_openfile), SWITCH_TAG_USER));
        this.mViewList.add(getPasswordInputItem(this.mContext.getString(R.string.rv_doc_encrpty_standard_password), EYES_TAG_USER));
        this.mViewList.add(getSwitchItem(this.mContext.getString(R.string.rv_doc_encrpty_standard_owner_permission), SWITCH_TAG_OWNER));
        this.mViewList.add(getSwitchItem(this.mContext.getString(R.string.rv_doc_info_permission_print), SWITCH_TAG_PRINT));
        this.mViewList.add(getSwitchItem(this.mContext.getString(R.string.rv_doc_info_permission_fillform), SWITCH_TAG_FILLFORM));
        this.mViewList.add(getSwitchItem(this.mContext.getString(R.string.rv_doc_info_permission_annotform), SWITCH_TAG_ANNOT));
        this.mViewList.add(getSwitchItem(this.mContext.getString(R.string.rv_doc_info_permission_assemble), SWITCH_TAG_PAGE));
        this.mViewList.add(getSwitchItem(this.mContext.getString(R.string.rv_doc_info_permission_modify), SWITCH_TAG_MODIFYDOC));
        this.mViewList.add(getSwitchItem(this.mContext.getString(R.string.rv_doc_info_permission_extractaccess), SWITCH_TAG_TEXTACCESS));
        this.mViewList.add(getSwitchItem(this.mContext.getString(R.string.rv_doc_info_permission_extract), SWITCH_TAG_COPY));
        this.mViewList.add(getPasswordInputItem(this.mContext.getString(R.string.rv_doc_encrpty_standard_password), EYES_TAG_OWNER));
        View tip = getOnlyTextItem(new StringBuilder(String.valueOf(this.mContext.getString(R.string.rv_doc_encrpty_standard_bottom_tip1))).append("\r\n\r\n").append(this.mContext.getString(R.string.rv_doc_encrpty_standard_bottom_tip2)).toString());
        ((TextView) tip.findViewById(R.id.rv_password_item_textview)).setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_body2_gray));
        ((ImageView) tip.findViewById(R.id.rv_password_item_divide)).setVisibility(8);
        this.mViewList.add(tip);
        refreshViewList(this.mSettingUserPassword.booleanValue(), this.mSettingOwnerPassword.booleanValue());
        setContentView(view);
        setTitle(this.mContext.getString(R.string.rv_doc_info_security_standard));
        setButton(5);
        setButtonEnable(false, 4);
        setBackButtonVisible(8);
        setListener(new DialogListener() {
            public void onResult(long btType) {
                if (btType == 4) {
                    if (PasswordSettingFragment.this.mSettingUserPassword.booleanValue()) {
                        PasswordSettingFragment.this.userpassword = PasswordSettingFragment.this.mUserEditText.getText().toString();
                    } else {
                        PasswordSettingFragment.this.userpassword = "";
                    }
                    if (PasswordSettingFragment.this.mSettingOwnerPassword.booleanValue()) {
                        PasswordSettingFragment.this.ownerpassword = PasswordSettingFragment.this.mOwnerEditText.getText().toString();
                    } else {
                        PasswordSettingFragment.this.ownerpassword = "";
                    }
                    if (PasswordSettingFragment.this.userpassword.equals(PasswordSettingFragment.this.ownerpassword)) {
                        UIToast.getInstance(PasswordSettingFragment.this.mContext).show(PasswordSettingFragment.this.mContext.getString(R.string.rv_doc_encrpty_standard_same_password));
                        return;
                    }
                    PasswordSettingFragment.this.dismiss();
                    if (PasswordSettingFragment.this.mPdfViewCtrl != null) {
                        boolean bModified = false;
                        try {
                            bModified = PasswordSettingFragment.this.mPdfViewCtrl.getDoc().isModified();
                        } catch (PDFException e) {
                            e.printStackTrace();
                        }
                        if (bModified || (DocumentManager.getInstance(PasswordSettingFragment.this.mPdfViewCtrl).canSaveAsFile() && !DocumentManager.getInstance(PasswordSettingFragment.this.mPdfViewCtrl).canModifyFile())) {
                            FragmentManager fm = ((FragmentActivity) PasswordSettingFragment.this.mContext).getSupportFragmentManager();
                            UIEncryptionDialogFragment newFragment = (UIEncryptionDialogFragment) fm.findFragmentByTag(PasswordSettingFragment.DIALOGTAG);
                            if (newFragment == null) {
                                newFragment = UIEncryptionDialogFragment.newInstance(true);
                            }
                            AppDialogManager.getInstance().showAllowManager(newFragment, fm, PasswordSettingFragment.DIALOGTAG, new CancelListener() {
                                public void cancel() {
                                }
                            });
                            newFragment.setEncryptionDialogEventListener(new UIEncryptionDialogEventListener() {
                                public void onConfirmed(boolean encrypt) {
                                    if (PasswordSettingFragment.this.mSettingUserPassword.booleanValue()) {
                                        PasswordSettingFragment.this.userpassword = PasswordSettingFragment.this.mUserEditText.getText().toString();
                                    } else {
                                        PasswordSettingFragment.this.userpassword = "";
                                    }
                                    if (PasswordSettingFragment.this.mSettingOwnerPassword.booleanValue()) {
                                        PasswordSettingFragment.this.ownerpassword = PasswordSettingFragment.this.mOwnerEditText.getText().toString();
                                    } else {
                                        PasswordSettingFragment.this.ownerpassword = "";
                                    }
                                    if (!DocumentManager.getInstance(PasswordSettingFragment.this.mPdfViewCtrl).canSaveAsFile() || DocumentManager.getInstance(PasswordSettingFragment.this.mPdfViewCtrl).canModifyFile()) {
                                        PasswordSettingFragment.this.mSupport.addPassword(PasswordSettingFragment.this.userpassword, PasswordSettingFragment.this.ownerpassword, PasswordSettingFragment.this.mIsAddAnnot.booleanValue(), PasswordSettingFragment.this.mIsCopy.booleanValue(), PasswordSettingFragment.this.mIsManagePage.booleanValue(), PasswordSettingFragment.this.mIsPrint.booleanValue(), PasswordSettingFragment.this.mFillForm.booleanValue(), PasswordSettingFragment.this.mIsModifyDoc.booleanValue(), PasswordSettingFragment.this.mTextAccess.booleanValue(), null);
                                    } else {
                                        new UISaveAsDialog(PasswordSettingFragment.this.mContext, PasswordSettingFragment.this.mSupport.getFilePath(), "pdf", new ISaveAsOnOKClickCallBack() {
                                            public void onOkClick(String newFilePath) {
                                                PasswordSettingFragment.this.mSupport.addPassword(PasswordSettingFragment.this.userpassword, PasswordSettingFragment.this.ownerpassword, PasswordSettingFragment.this.mIsAddAnnot.booleanValue(), PasswordSettingFragment.this.mIsCopy.booleanValue(), PasswordSettingFragment.this.mIsManagePage.booleanValue(), PasswordSettingFragment.this.mIsPrint.booleanValue(), PasswordSettingFragment.this.mFillForm.booleanValue(), PasswordSettingFragment.this.mIsModifyDoc.booleanValue(), PasswordSettingFragment.this.mTextAccess.booleanValue(), newFilePath);
                                            }

                                            public void onCancelClick() {
                                            }
                                        }).showDialog();
                                    }
                                }

                                public void onCancel() {
                                }
                            });
                            return;
                        }
                        PasswordSettingFragment.this.mSupport.addPassword(PasswordSettingFragment.this.userpassword, PasswordSettingFragment.this.ownerpassword, PasswordSettingFragment.this.mIsAddAnnot.booleanValue(), PasswordSettingFragment.this.mIsCopy.booleanValue(), PasswordSettingFragment.this.mIsManagePage.booleanValue(), PasswordSettingFragment.this.mIsPrint.booleanValue(), PasswordSettingFragment.this.mFillForm.booleanValue(), PasswordSettingFragment.this.mIsModifyDoc.booleanValue(), PasswordSettingFragment.this.mTextAccess.booleanValue(), null);
                    }
                } else if (btType == 1) {
                    PasswordSettingFragment.this.dismiss();
                }
            }

            public void onBackClick() {
            }
        });
        return view;
    }

    private View getOnlyTextItem(String tip) {
        tip = "\r\n" + tip;
        View item = View.inflate(this.mContext, R.layout.rv_password_setting_item, null);
        TextView tv = (TextView) item.findViewById(R.id.rv_password_item_textview);
        tv.setTextSize(10.0f);
        tv.setText(tip);
        ((ImageView) item.findViewById(R.id.rv_password_item_imagebutton)).setVisibility(8);
        ((EditText) item.findViewById(R.id.rv_password_item_edittext)).setVisibility(8);
        return item;
    }

    private View getSwitchItem(String tip, int tag) {
        RelativeLayout item = (RelativeLayout) View.inflate(this.mContext, R.layout.rv_password_setting_item, null);
        if (AppDisplay.getInstance(this.mContext).isPad()) {
            item.setLayoutParams(new LayoutParams(-1, (int) this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_1l_pad)));
        } else {
            item.setLayoutParams(new LayoutParams(-1, (int) this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_1l_phone)));
        }
        ImageView iv = (ImageView) item.findViewById(R.id.rv_password_item_imagebutton);
        iv.setTag(Integer.valueOf(tag));
        iv.setImageResource(R.drawable.setting_on);
        iv.setOnClickListener(this.mOnClickListener);
        this.mImageList.add(iv);
        TextView tv = (TextView) item.findViewById(R.id.rv_password_item_textview);
        tv.setText(tip);
        tv.setPadding(0, 0, AppDisplay.getInstance(this.mContext).dp2px(60.0f), 0);
        ((EditText) item.findViewById(R.id.rv_password_item_edittext)).setVisibility(8);
        refreshSwitchView(tag, iv);
        return item;
    }

    private View getPasswordInputItem(String tip, final int tag) {
        RelativeLayout item = (RelativeLayout) View.inflate(this.mContext, R.layout.rv_password_setting_item, null);
        if (AppDisplay.getInstance(this.mContext).isPad()) {
            item.setLayoutParams(new LayoutParams(-1, (int) this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_1l_pad)));
        } else {
            item.setLayoutParams(new LayoutParams(-1, (int) this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_1l_phone)));
        }
        ImageView iv = (ImageView) item.findViewById(R.id.rv_password_item_imagebutton);
        iv.setImageResource(R.drawable.rv_password_check_eye_normal);
        iv.setTag(Integer.valueOf(tag));
        iv.setOnTouchListener(this.mOnTouchListener);
        iv.setVisibility(4);
        if (tag == EYES_TAG_USER) {
            this.mUserEye = iv;
        }
        if (tag == EYES_TAG_OWNER) {
            this.mOwnerEye = iv;
        }
        ((TextView) item.findViewById(R.id.rv_password_item_textview)).setText(tip);
        final EditText et = (EditText) item.findViewById(R.id.rv_password_item_edittext);
        et.setHeight(AppDisplay.getInstance(this.mContext).dp2px(30.0f));
        et.setKeyListener(new NumberKeyListener() {
            public int getInputType() {
                return 129;
            }

            protected char[] getAcceptedChars() {
                return PasswordConstants.mAcceptChars;
            }
        });
        et.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (et.getText().length() == 0 || et.getText().length() > 32) {
                    PasswordSettingFragment.this.setButtonEnable(false, 4);
                    if (tag == PasswordSettingFragment.EYES_TAG_USER) {
                        PasswordSettingFragment.this.mUserEye.setVisibility(4);
                    }
                    if (tag == PasswordSettingFragment.EYES_TAG_OWNER) {
                        PasswordSettingFragment.this.mOwnerEye.setVisibility(4);
                    }
                } else {
                    PasswordSettingFragment.this.setButtonEnable(true, 4);
                    if (tag == PasswordSettingFragment.EYES_TAG_USER) {
                        PasswordSettingFragment.this.mUserEye.setVisibility(0);
                    }
                    if (tag == PasswordSettingFragment.EYES_TAG_OWNER) {
                        PasswordSettingFragment.this.mOwnerEye.setVisibility(0);
                    }
                }
                PasswordSettingFragment.this.refreshButton(tag);
            }
        });
        et.setFilters(new InputFilter[]{new LengthFilter(32)});
        et.setHint(this.mContext.getString(R.string.rv_doc_encrpty_standard_must_input));
        if (tag == EYES_TAG_USER) {
            this.mUserEditText = et;
        }
        if (tag == EYES_TAG_OWNER) {
            this.mOwnerEditText = et;
        }
        return item;
    }

    private void refreshViewList(boolean user, boolean owner) {
        this.mLinearLayout.removeAllViews();
        int i;
        if (user && owner) {
            for (i = 0; i < this.mViewList.size(); i++) {
                this.mLinearLayout.addView((View) this.mViewList.get(i));
            }
        } else if (user && !owner) {
            i = 0;
            while (i < this.mViewList.size()) {
                if ((i >= 0 && i <= 2) || i == 11) {
                    this.mLinearLayout.addView((View) this.mViewList.get(i));
                }
                i++;
            }
        } else if (!user && owner) {
            i = 0;
            while (i < this.mViewList.size()) {
                if (i == 0 || (i >= 2 && i <= 11)) {
                    this.mLinearLayout.addView((View) this.mViewList.get(i));
                }
                i++;
            }
        } else if (!user && !owner) {
            i = 0;
            while (i < this.mViewList.size()) {
                if (i == 0 || i == 2 || i == 11) {
                    this.mLinearLayout.addView((View) this.mViewList.get(i));
                }
                i++;
            }
        }
    }

    private void refreshSwitchView(int tag, ImageView iv) {
        if (tag == SWITCH_TAG_USER) {
            if (this.mSettingUserPassword.booleanValue()) {
                iv.setImageResource(R.drawable.setting_on);
            } else {
                iv.setImageResource(R.drawable.setting_off);
            }
        } else if (tag == SWITCH_TAG_OWNER) {
            if (this.mSettingOwnerPassword.booleanValue()) {
                iv.setImageResource(R.drawable.setting_on);
            } else {
                iv.setImageResource(R.drawable.setting_off);
            }
        } else if (tag == SWITCH_TAG_ANNOT) {
            if (this.mIsAddAnnot.booleanValue()) {
                iv.setImageResource(R.drawable.setting_on);
            } else {
                iv.setImageResource(R.drawable.setting_off);
            }
        } else if (tag == SWITCH_TAG_COPY) {
            if (this.mIsCopy.booleanValue()) {
                iv.setImageResource(R.drawable.setting_on);
            } else {
                iv.setImageResource(R.drawable.setting_off);
            }
        } else if (tag == SWITCH_TAG_PAGE) {
            if (this.mIsManagePage.booleanValue()) {
                iv.setImageResource(R.drawable.setting_on);
            } else {
                iv.setImageResource(R.drawable.setting_off);
            }
        } else if (tag == SWITCH_TAG_PRINT) {
            if (this.mIsPrint.booleanValue()) {
                iv.setImageResource(R.drawable.setting_on);
            } else {
                iv.setImageResource(R.drawable.setting_off);
            }
        } else if (tag == SWITCH_TAG_FILLFORM) {
            if (this.mFillForm.booleanValue()) {
                iv.setImageResource(R.drawable.setting_on);
            } else {
                iv.setImageResource(R.drawable.setting_off);
            }
        } else if (tag == SWITCH_TAG_MODIFYDOC) {
            if (this.mIsModifyDoc.booleanValue()) {
                iv.setImageResource(R.drawable.setting_on);
            } else {
                iv.setImageResource(R.drawable.setting_off);
            }
        } else if (tag != SWITCH_TAG_TEXTACCESS) {
        } else {
            if (this.mTextAccess.booleanValue()) {
                iv.setImageResource(R.drawable.setting_on);
            } else {
                iv.setImageResource(R.drawable.setting_off);
            }
        }
    }

    private void refreshAllSwitchView() {
        if (this.mSettingUserPassword.booleanValue()) {
            ((ImageView) this.mImageList.get(0)).setImageResource(R.drawable.setting_on);
        } else {
            ((ImageView) this.mImageList.get(0)).setImageResource(R.drawable.setting_off);
        }
        if (this.mSettingOwnerPassword.booleanValue()) {
            ((ImageView) this.mImageList.get(1)).setImageResource(R.drawable.setting_on);
        } else {
            ((ImageView) this.mImageList.get(1)).setImageResource(R.drawable.setting_off);
        }
        if (this.mIsPrint.booleanValue()) {
            ((ImageView) this.mImageList.get(2)).setImageResource(R.drawable.setting_on);
        } else {
            ((ImageView) this.mImageList.get(2)).setImageResource(R.drawable.setting_off);
        }
        if (this.mFillForm.booleanValue()) {
            ((ImageView) this.mImageList.get(3)).setImageResource(R.drawable.setting_on);
        } else {
            ((ImageView) this.mImageList.get(3)).setImageResource(R.drawable.setting_off);
        }
        if (this.mIsAddAnnot.booleanValue()) {
            ((ImageView) this.mImageList.get(4)).setImageResource(R.drawable.setting_on);
        } else {
            ((ImageView) this.mImageList.get(4)).setImageResource(R.drawable.setting_off);
        }
        if (this.mIsManagePage.booleanValue()) {
            ((ImageView) this.mImageList.get(5)).setImageResource(R.drawable.setting_on);
        } else {
            ((ImageView) this.mImageList.get(5)).setImageResource(R.drawable.setting_off);
        }
        if (this.mIsModifyDoc.booleanValue()) {
            ((ImageView) this.mImageList.get(6)).setImageResource(R.drawable.setting_on);
        } else {
            ((ImageView) this.mImageList.get(6)).setImageResource(R.drawable.setting_off);
        }
        if (this.mTextAccess.booleanValue()) {
            ((ImageView) this.mImageList.get(7)).setImageResource(R.drawable.setting_on);
        } else {
            ((ImageView) this.mImageList.get(7)).setImageResource(R.drawable.setting_off);
        }
        if (this.mIsCopy.booleanValue()) {
            ((ImageView) this.mImageList.get(8)).setImageResource(R.drawable.setting_on);
        } else {
            ((ImageView) this.mImageList.get(8)).setImageResource(R.drawable.setting_off);
        }
    }

    private void refreshButton(int tag) {
        if (tag == EYES_TAG_USER && this.mSettingUserPassword.booleanValue()) {
            if (this.mUserEditText.getText().toString().length() == 0) {
                setButtonEnable(false, 4);
            } else {
                setButtonEnable(true, 4);
            }
        }
        if (tag == EYES_TAG_OWNER && this.mSettingOwnerPassword.booleanValue()) {
            if (this.mOwnerEditText.getText().toString().length() == 0) {
                setButtonEnable(false, 4);
            } else {
                setButtonEnable(true, 4);
            }
        }
        if (!this.mSettingOwnerPassword.booleanValue() && !this.mSettingUserPassword.booleanValue()) {
            setButtonEnable(false, 4);
        }
    }
}
