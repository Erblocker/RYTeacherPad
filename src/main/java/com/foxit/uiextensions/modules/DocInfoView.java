package com.foxit.uiextensions.modules;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.dialog.MatchDialog.DialogListener;
import com.foxit.uiextensions.controls.dialog.UIMatchDialog;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.UIMarqueeTextView;
import java.io.File;

public class DocInfoView {
    private Context mContext = null;
    private String mFilePath = null;
    private boolean mIsPad = false;
    private PDFViewCtrl mPdfViewCtrl = null;
    private SummaryInfo mSummaryInfo = null;

    abstract class DocInfo {
        protected String mCaption = null;
        protected UIMatchDialog mDialog = null;
        protected View mRootLayout = null;

        abstract void init();

        abstract void show();

        DocInfo() {
        }
    }

    class PermissionInfo extends DocInfo {
        PermissionInfo() {
            super();
            this.mCaption = AppResource.getString(DocInfoView.this.mContext, R.string.rv_doc_info_permission);
        }

        void init() {
            this.mRootLayout = View.inflate(DocInfoView.this.mContext, R.layout.rv_doc_info_permissioin, null);
            initPadDimens();
            PDFDoc doc = DocInfoView.this.mPdfViewCtrl.getDoc();
            if (doc != null) {
                String str;
                ((TextView) this.mRootLayout.findViewById(R.id.rv_doc_info_permission_title)).setText(AppResource.getString(DocInfoView.this.mContext, R.string.rv_doc_info_permission_summary));
                ((TextView) this.mRootLayout.findViewById(R.id.rv_doc_info_permission_print)).setText(AppResource.getString(DocInfoView.this.mContext, R.string.rv_doc_info_permission_print));
                ((TextView) this.mRootLayout.findViewById(R.id.rv_doc_info_permission_fillform)).setText(AppResource.getString(DocInfoView.this.mContext, R.string.rv_doc_info_permission_fillform));
                ((TextView) this.mRootLayout.findViewById(R.id.rv_doc_info_permission_annotform)).setText(AppResource.getString(DocInfoView.this.mContext, R.string.rv_doc_info_permission_annotform));
                ((TextView) this.mRootLayout.findViewById(R.id.rv_doc_info_permission_assemble)).setText(AppResource.getString(DocInfoView.this.mContext, R.string.rv_doc_info_permission_assemble));
                ((TextView) this.mRootLayout.findViewById(R.id.rv_doc_info_permission_modify)).setText(AppResource.getString(DocInfoView.this.mContext, R.string.rv_doc_info_permission_modify));
                ((TextView) this.mRootLayout.findViewById(R.id.rv_doc_info_permission_extractaccess)).setText(AppResource.getString(DocInfoView.this.mContext, R.string.rv_doc_info_permission_extractaccess));
                ((TextView) this.mRootLayout.findViewById(R.id.rv_doc_info_permission_extract)).setText(AppResource.getString(DocInfoView.this.mContext, R.string.rv_doc_info_permission_extract));
                ((TextView) this.mRootLayout.findViewById(R.id.rv_doc_info_permission_signing)).setText(AppResource.getString(DocInfoView.this.mContext, R.string.rv_doc_info_permission_signing));
                TextView tvPrint = (TextView) this.mRootLayout.findViewById(R.id.rv_doc_info_permission_print_of);
                TextView tvFillForm = (TextView) this.mRootLayout.findViewById(R.id.rv_doc_info_permission_fillform_of);
                TextView tvAnnotForm = (TextView) this.mRootLayout.findViewById(R.id.rv_doc_info_permission_annotform_of);
                TextView tvAssemble = (TextView) this.mRootLayout.findViewById(R.id.rv_doc_info_permission_assemble_of);
                TextView tvModify = (TextView) this.mRootLayout.findViewById(R.id.rv_doc_info_permission_modify_of);
                TextView tvExtractAccess = (TextView) this.mRootLayout.findViewById(R.id.rv_doc_info_permission_extractaccess_of);
                TextView tvExtract = (TextView) this.mRootLayout.findViewById(R.id.rv_doc_info_permission_extract_of);
                TextView tvSigning = (TextView) this.mRootLayout.findViewById(R.id.rv_doc_info_permission_signing_of);
                String allowed = AppResource.getString(DocInfoView.this.mContext, R.string.fx_string_allowed);
                String notAllowed = AppResource.getString(DocInfoView.this.mContext, R.string.fx_string_notallowed);
                long userPermission = 0;
                try {
                    userPermission = doc.getUserPermissions();
                } catch (PDFException e) {
                    e.printStackTrace();
                }
                if ((4 & userPermission) != 0) {
                    str = allowed;
                } else {
                    str = notAllowed;
                }
                tvPrint.setText(str);
                if (DocumentManager.getInstance(DocInfoView.this.mPdfViewCtrl).canFillForm()) {
                    str = allowed;
                } else {
                    str = notAllowed;
                }
                tvFillForm.setText(str);
                if (DocumentManager.getInstance(DocInfoView.this.mPdfViewCtrl).canAddAnnot()) {
                    str = allowed;
                } else {
                    str = notAllowed;
                }
                tvAnnotForm.setText(str);
                if (DocumentManager.getInstance(DocInfoView.this.mPdfViewCtrl).canAssemble()) {
                    str = allowed;
                } else {
                    str = notAllowed;
                }
                tvAssemble.setText(str);
                if (DocumentManager.getInstance(DocInfoView.this.mPdfViewCtrl).canModifyContents()) {
                    str = allowed;
                } else {
                    str = notAllowed;
                }
                tvModify.setText(str);
                if (DocumentManager.getInstance(DocInfoView.this.mPdfViewCtrl).canCopyForAssess()) {
                    str = allowed;
                } else {
                    str = notAllowed;
                }
                tvExtractAccess.setText(str);
                if (DocumentManager.getInstance(DocInfoView.this.mPdfViewCtrl).canCopy()) {
                    str = allowed;
                } else {
                    str = notAllowed;
                }
                tvExtract.setText(str);
                if (!DocumentManager.getInstance(DocInfoView.this.mPdfViewCtrl).canSigning()) {
                    allowed = notAllowed;
                }
                tvSigning.setText(allowed);
            }
        }

        void initPadDimens() {
            if (DocInfoView.this.mIsPad) {
                int[] idArray = new int[]{R.id.rv_doc_info_permission_title, R.id.rv_doc_info_permisson_print_rl, R.id.rv_doc_info_permission_fillform_rl, R.id.rv_doc_info_permission_annotform_rl, R.id.rv_doc_info_permission_assemble_rl, R.id.rv_doc_info_permission_modify_rl, R.id.rv_doc_info_permission_extractaccess_rl, R.id.rv_doc_info_permission_extract_rl, R.id.rv_doc_info_permission_signing_rl};
                for (int findViewById : idArray) {
                    View view = this.mRootLayout.findViewById(findViewById);
                    int leftPadding = AppResource.getDimensionPixelSize(DocInfoView.this.mContext, R.dimen.ux_horz_left_margin_pad);
                    view.setPadding(leftPadding, 0, leftPadding, 0);
                    view.getLayoutParams().height = AppResource.getDimensionPixelSize(DocInfoView.this.mContext, R.dimen.ux_list_item_height_1l_pad);
                }
            }
        }

        void show() {
            this.mDialog = new UIMatchDialog(DocInfoView.this.mContext);
            this.mDialog.setTitle(this.mCaption);
            this.mDialog.setContentView(this.mRootLayout);
            this.mDialog.setBackButtonVisible(0);
            this.mDialog.setListener(new DialogListener() {
                public void onResult(long btType) {
                }

                public void onBackClick() {
                    PermissionInfo.this.mDialog.dismiss();
                }
            });
            this.mDialog.showDialog();
        }
    }

    class SummaryInfo extends DocInfo {

        public class DocumentInfo {
            public String mAuthor = null;
            public String mCreateTime = null;
            public String mFileName = null;
            public String mFilePath = null;
            public long mFileSize = 0;
            public String mModTime = null;
            public String mSubject = null;
        }

        SummaryInfo() {
            super();
            this.mCaption = AppResource.getString(DocInfoView.this.mContext, R.string.rv_doc_info);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        void init() {
            String content = null;
            this.mRootLayout = View.inflate(DocInfoView.this.mContext, R.layout.rv_doc_info, null);
            initPadDimens();
            PDFDoc doc = DocInfoView.this.mPdfViewCtrl.getDoc();
            if (doc != null) {
                DocumentInfo info = getDocumentInfo();
                ((TextView) this.mRootLayout.findViewById(R.id.rv_doc_info_fileinfo_title)).setText(AppResource.getString(DocInfoView.this.mContext, R.string.rv_doc_info_fileinfo));
                ((UIMarqueeTextView) this.mRootLayout.findViewById(R.id.rv_doc_info_fileinfo_name_value)).setText(info.mFileName);
                ((UIMarqueeTextView) this.mRootLayout.findViewById(R.id.rv_doc_info_fileinfo_path_value)).setText(AppUtil.getFileFolder(info.mFilePath));
                ((UIMarqueeTextView) this.mRootLayout.findViewById(R.id.rv_doc_info_fileinfo_size_value)).setText(AppUtil.fileSizeToString(info.mFileSize));
                ((UIMarqueeTextView) this.mRootLayout.findViewById(R.id.rv_doc_info_fileinfo_author_value)).setText(info.mAuthor);
                ((UIMarqueeTextView) this.mRootLayout.findViewById(R.id.rv_doc_info_fileinfo_subject_value)).setText(info.mSubject);
                ((UIMarqueeTextView) this.mRootLayout.findViewById(R.id.rv_doc_info_fileinfo_createdate_value)).setText(info.mCreateTime);
                ((UIMarqueeTextView) this.mRootLayout.findViewById(R.id.rv_doc_info_fileinfo_moddate_value)).setText(info.mModTime);
                ((LinearLayout) this.mRootLayout.findViewById(R.id.rv_doc_info_security)).setVisibility(0);
                ((TextView) this.mRootLayout.findViewById(R.id.rv_doc_info_security_title)).setText(AppResource.getString(DocInfoView.this.mContext, R.string.rv_doc_info_security));
                TextView tvContent = (TextView) this.mRootLayout.findViewById(R.id.rv_doc_info_security_content);
                this.mRootLayout.findViewById(R.id.rv_doc_info_security).setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        PermissionInfo permInfo = new PermissionInfo();
                        permInfo.init();
                        permInfo.show();
                    }
                });
                try {
                    switch (doc.getEncryptionType()) {
                        case 1:
                            content = AppResource.getString(DocInfoView.this.mContext, R.string.rv_doc_info_security_standard);
                            break;
                        case 2:
                            content = AppResource.getString(DocInfoView.this.mContext, R.string.rv_doc_info_security_pubkey);
                            break;
                        case 3:
                        case 5:
                            content = AppResource.getString(DocInfoView.this.mContext, R.string.rv_doc_info_security_rms);
                            break;
                        case 4:
                            content = AppResource.getString(DocInfoView.this.mContext, R.string.rv_doc_info_security_custom);
                            break;
                        default:
                            content = AppResource.getString(DocInfoView.this.mContext, R.string.rv_doc_info_security_no);
                            break;
                    }
                } catch (PDFException e) {
                    e.printStackTrace();
                }
                tvContent.setText(content);
            }
        }

        void show() {
            this.mDialog = new UIMatchDialog(DocInfoView.this.mContext);
            this.mDialog.setTitle(this.mCaption);
            this.mDialog.setContentView(this.mRootLayout);
            if (DocInfoView.this.mIsPad) {
                this.mDialog.setBackButtonVisible(8);
            } else {
                this.mDialog.setBackButtonVisible(0);
            }
            this.mDialog.setListener(new DialogListener() {
                public void onResult(long btType) {
                    SummaryInfo.this.mDialog.dismiss();
                }

                public void onBackClick() {
                }
            });
            this.mDialog.showDialog(true);
        }

        DocumentInfo getDocumentInfo() {
            DocumentInfo info = new DocumentInfo();
            PDFDoc doc = DocInfoView.this.mPdfViewCtrl.getDoc();
            info.mFilePath = DocInfoView.this.mFilePath;
            if (DocInfoView.this.mFilePath != null) {
                info.mFileName = AppUtil.getFileName(DocInfoView.this.mFilePath);
                info.mFileSize = new File(DocInfoView.this.mFilePath).length();
            }
            try {
                info.mAuthor = doc.getMetadataValue("Author");
                info.mSubject = doc.getMetadataValue("Subject");
                info.mCreateTime = AppDmUtil.getLocalDateString(doc.getCreationDateTime());
                info.mModTime = AppDmUtil.getLocalDateString(doc.getModifiedDateTime());
            } catch (Exception e) {
            }
            return info;
        }

        void initPadDimens() {
            int leftPadding;
            int rightPadding;
            if (DocInfoView.this.mIsPad) {
                leftPadding = AppResource.getDimensionPixelSize(DocInfoView.this.mContext, R.dimen.ux_horz_left_margin_pad);
                rightPadding = AppResource.getDimensionPixelSize(DocInfoView.this.mContext, R.dimen.ux_horz_right_margin_pad);
            } else {
                leftPadding = AppResource.getDimensionPixelSize(DocInfoView.this.mContext, R.dimen.ux_horz_left_margin_phone);
                rightPadding = AppResource.getDimensionPixelSize(DocInfoView.this.mContext, R.dimen.ux_horz_right_margin_phone);
            }
            ((UIMarqueeTextView) this.mRootLayout.findViewById(R.id.rv_doc_info_fileinfo_name_value)).setPadding(0, 0, leftPadding + rightPadding, 0);
            ((UIMarqueeTextView) this.mRootLayout.findViewById(R.id.rv_doc_info_fileinfo_path_value)).setPadding(0, 0, leftPadding + rightPadding, 0);
            if (DocInfoView.this.mIsPad) {
                int[] arrIDForPadding = new int[]{R.id.rv_doc_info_fileinfo_title, R.id.table_row_file_name, R.id.table_row_file_path, R.id.table_row_file_size, R.id.table_row_file_author, R.id.table_row_file_subject, R.id.table_row_create_date, R.id.table_row_modify_date, R.id.rv_doc_info_security};
                for (int findViewById : arrIDForPadding) {
                    this.mRootLayout.findViewById(findViewById).setPadding(leftPadding, 0, rightPadding, 0);
                }
                int[] arrIDForLayout = new int[]{R.id.rv_doc_info_fileinfo_title, R.id.rv_doc_info_fileinfo_name, R.id.rv_doc_info_fileinfo_name_value, R.id.rv_doc_info_fileinfo_path, R.id.rv_doc_info_fileinfo_path_value, R.id.rv_doc_info_fileinfo_size, R.id.rv_doc_info_fileinfo_size_value, R.id.rv_doc_info_fileinfo_author, R.id.rv_doc_info_fileinfo_author_value, R.id.rv_doc_info_fileinfo_subject, R.id.rv_doc_info_fileinfo_subject_value, R.id.rv_doc_info_fileinfo_createdate, R.id.rv_doc_info_fileinfo_createdate_value, R.id.rv_doc_info_fileinfo_moddate, R.id.rv_doc_info_fileinfo_moddate_value, R.id.rv_doc_info_security_title, R.id.rv_doc_info_security_content};
                for (int findViewById2 : arrIDForLayout) {
                    this.mRootLayout.findViewById(findViewById2).getLayoutParams().height = AppResource.getDimensionPixelSize(DocInfoView.this.mContext, R.dimen.ux_list_item_height_1l_pad);
                }
            }
        }
    }

    DocInfoView(Context context, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mIsPad = AppDisplay.getInstance(context).isPad();
    }

    public void init(String filePath) {
        setFilePath(filePath);
        this.mSummaryInfo = new SummaryInfo();
    }

    public void setFilePath(String path) {
        this.mFilePath = path;
    }

    public void show() {
        if (this.mSummaryInfo != null) {
            this.mSummaryInfo.init();
            this.mSummaryInfo.show();
        }
    }
}
