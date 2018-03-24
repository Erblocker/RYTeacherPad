package com.foxit.uiextensions.modules;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.Task;
import com.foxit.sdk.Task.CallBack;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.DocumentManager.AnnotEventListener;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.controls.dialog.UITextEditDialog;
import com.foxit.uiextensions.modules.AnnotAdapter.CheckBoxChangeListener;
import com.foxit.uiextensions.modules.AnnotAdapter.DeleteCallback;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.UIToast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class AnnotPanel implements OnClickListener, CheckBoxChangeListener {
    private static final int DELETE_CAN = 0;
    private static final int DELETE_SRCAN = 1;
    private static final int DELETE_UNCAN = 2;
    public static final int STATUS_CANCEL = 2;
    public static final int STATUS_DELETING = 6;
    public static final int STATUS_DONE = 4;
    public static final int STATUS_FAILED = 5;
    public static final int STATUS_LOADING = 1;
    public static final int STATUS_PAUSED = 3;
    private final AnnotAdapter mAdapter;
    private AnnotEventListener mAnnotEventListener = new AnnotEventListener() {
        public void onAnnotAdded(PDFPage page, Annot annot) {
            if (page != null && annot != null && !AppAnnotUtil.isSupportGroupElement(annot)) {
                try {
                    if (AppAnnotUtil.isSupportEditAnnot(annot) && (annot.getFlags() & 2) == 0 && AnnotPanel.this.isPageLoaded(page)) {
                        if (AnnotPanel.this.mAdapter.getAnnotNode(page, annot.getUniqueID()) == null) {
                            String replyTo = "";
                            Annot replyToAnnot = AppAnnotUtil.getReplyToAnnot(annot);
                            if (replyToAnnot != null) {
                                replyTo = replyToAnnot.getUniqueID();
                            }
                            AnnotNode node = new AnnotNode(page.getIndex(), annot.getUniqueID(), replyTo);
                            if (annot.isMarkup()) {
                                node.setAuthor(((Markup) annot).getTitle());
                            }
                            node.setType(AppAnnotUtil.getTypeString(annot));
                            node.setContent(annot.getContent());
                            String modifiedDate = AppDmUtil.getLocalDateString(annot.getModifiedDateTime());
                            String createdDate = AppDmUtil.dateOriValue;
                            if (annot.isMarkup()) {
                                createdDate = AppDmUtil.getLocalDateString(((Markup) annot).getCreationDateTime());
                            }
                            if (modifiedDate == null || modifiedDate.equals(AppDmUtil.dateOriValue)) {
                                modifiedDate = createdDate;
                            }
                            node.setModifiedDate(modifiedDate);
                            node.setCreationDate(createdDate);
                            node.setDeletable(true);
                            node.setCanReply(AppAnnotUtil.isSupportReply(annot));
                            if (annot.isMarkup()) {
                                node.setIntent(((Markup) annot).getIntent());
                            }
                            AnnotPanel.this.mAdapter.addNode(node);
                        }
                        AnnotPanel.this.mAdapter.establishNodeList();
                        AnnotPanel.this.mAdapter.notifyDataSetChanged();
                        AnnotPanel.this.mPanel.hideNoAnnotsView();
                    }
                } catch (PDFException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onAnnotDeleted(PDFPage page, Annot annot) {
            if (page != null && annot != null && !AppAnnotUtil.isSupportGroupElement(annot)) {
                try {
                    if (AppAnnotUtil.isSupportEditAnnot(annot) && (annot.getFlags() & 2) == 0 && AnnotPanel.this.isPageLoaded(page)) {
                        for (int i = AnnotPanel.this.mCheckedNodes.size() - 1; i > -1; i--) {
                            AnnotNode node = (AnnotNode) AnnotPanel.this.mCheckedNodes.get(i);
                            if (node.getUID().equals(annot.getUniqueID())) {
                                node.setChecked(false);
                                AnnotPanel.this.onChecked(false, node);
                            }
                            for (AnnotNode parent = node.getParent(); parent != null; parent = parent.getParent()) {
                                if (parent.getUID().equals(annot.getUniqueID())) {
                                    node.setChecked(false);
                                    AnnotPanel.this.onChecked(false, node);
                                    break;
                                }
                            }
                        }
                        AnnotPanel.this.mAdapter.removeNode(page, annot.getUniqueID());
                        AnnotPanel.this.mAdapter.establishNodeList();
                        Activity activity = (Activity) AnnotPanel.this.mContext;
                        if (AnnotPanel.this.mLoadedState == 4 && AnnotPanel.this.mAdapter.getCount() == 0) {
                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    AnnotPanel.this.mPanel.showNoAnnotsView();
                                }
                            });
                        }
                    }
                } catch (PDFException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onAnnotModified(PDFPage page, Annot annot) {
            if (page != null && annot != null && !AppAnnotUtil.isSupportGroupElement(annot)) {
                try {
                    if (AppAnnotUtil.isSupportEditAnnot(annot) && (annot.getFlags() & 2) == 0 && AnnotPanel.this.isPageLoaded(page)) {
                        AnnotPanel.this.mAdapter.updateNode(annot);
                    }
                } catch (PDFException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onAnnotChanged(Annot lastAnnot, Annot currentAnnot) {
        }
    };
    private TextView mChangedTextView;
    private final List<AnnotNode> mCheckedNodes;
    private final Context mContext;
    private ProgressDialog mDeleteDialog;
    private final List<AnnotNode> mDeleteTemps;
    private int mLoadedIndex;
    private int mLoadedState;
    private View mMainLayout;
    private AnnotPanelModule mPanel;
    private final ViewGroup mParent;
    private boolean mPause;
    private final PDFViewCtrl mPdfViewCtrl;
    private UITextEditDialog mSRDeleteDialog;

    public interface OnSearchPageEndListener {
        void onResult(boolean z, ArrayList<AnnotNode> arrayList);
    }

    class SearchPageTask extends Task {
        private int mPageIndex;
        private PDFViewCtrl mPdfView;
        private ArrayList<AnnotNode> mSearchResults;
        private boolean mSearchRet;

        /* renamed from: com.foxit.uiextensions.modules.AnnotPanel$SearchPageTask$1 */
        class AnonymousClass1 implements CallBack {
            private final /* synthetic */ OnSearchPageEndListener val$onSearchPageEndListener;
            private final /* synthetic */ AnnotPanel val$this$0;

            AnonymousClass1(AnnotPanel annotPanel, OnSearchPageEndListener onSearchPageEndListener) {
                this.val$this$0 = annotPanel;
                this.val$onSearchPageEndListener = onSearchPageEndListener;
            }

            public void result(Task task) {
                SearchPageTask task1 = (SearchPageTask) task;
                this.val$onSearchPageEndListener.onResult(task1.mSearchRet, task1.mSearchResults);
            }
        }

        public SearchPageTask(PDFViewCtrl pdfView, int pageIndex, OnSearchPageEndListener onSearchPageEndListener) {
            super(new AnonymousClass1(AnnotPanel.this, onSearchPageEndListener));
            this.mPdfView = pdfView;
            this.mPageIndex = pageIndex;
        }

        protected void execute() {
            if (this.mSearchResults == null) {
                this.mSearchResults = new ArrayList();
            }
            AnnotPanel.this.mLoadedState = 1;
            this.mSearchRet = searchPage();
        }

        private boolean searchPage() {
            try {
                PDFPage page = this.mPdfView.getDoc().getPage(this.mPageIndex);
                if (page == null) {
                    return false;
                }
                int nCount = page.getAnnotCount();
                if (nCount > 0) {
                    for (int i = 0; i < nCount; i++) {
                        Annot annot = page.getAnnot(i);
                        if (annot != null && (annot.getFlags() & 2) == 0 && AppAnnotUtil.isSupportEditAnnot(annot)) {
                            String replyTo = "";
                            Annot replyToAnnot = AppAnnotUtil.getReplyToAnnot(annot);
                            if (replyToAnnot != null) {
                                if (replyToAnnot.getUniqueID() == null) {
                                    replyToAnnot.setUniqueID(AppDmUtil.randomUUID(null));
                                }
                                replyTo = replyToAnnot.getUniqueID();
                            }
                            if (annot.getUniqueID() == null) {
                                annot.setUniqueID(AppDmUtil.randomUUID(null));
                            }
                            AnnotNode node = new AnnotNode(this.mPageIndex, annot.getUniqueID(), replyTo);
                            if (annot.isMarkup()) {
                                node.setAuthor(((Markup) annot).getTitle());
                            }
                            node.setType(AppAnnotUtil.getTypeString(annot));
                            node.setContent(annot.getContent());
                            String modifiedDate = AppDmUtil.getLocalDateString(annot.getModifiedDateTime());
                            String creationDate = AppDmUtil.dateOriValue;
                            if (annot.isMarkup()) {
                                creationDate = AppDmUtil.getLocalDateString(((Markup) annot).getCreationDateTime());
                            }
                            if (modifiedDate == null || modifiedDate.equals(AppDmUtil.dateOriValue)) {
                                modifiedDate = creationDate;
                            }
                            node.setModifiedDate(modifiedDate);
                            node.setCreationDate(creationDate);
                            node.setDeletable(true);
                            node.setCanReply(AppAnnotUtil.isSupportReply(annot));
                            if (annot.isMarkup()) {
                                node.setIntent(((Markup) annot).getIntent());
                            }
                            this.mSearchResults.add(node);
                        }
                    }
                    page.getDocument().closePage(this.mPageIndex);
                }
                return true;
            } catch (PDFException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    AnnotPanel(AnnotPanelModule panelModule, Context context, PDFViewCtrl pdfViewCtrl, ViewGroup parent, View layout, ArrayList<Boolean> itemMoreViewShow) {
        this.mPanel = panelModule;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mParent = parent;
        this.mContext = context;
        this.mMainLayout = layout;
        this.mCheckedNodes = new ArrayList();
        this.mDeleteTemps = new ArrayList();
        this.mAdapter = new AnnotAdapter(layout.getContext(), this.mPdfViewCtrl, this.mParent, itemMoreViewShow);
        this.mAdapter.setPopupWindow(this.mPanel.getPopupWindow());
        this.mAdapter.setCheckBoxChangeListener(this);
        this.mDeleteDialog = new ProgressDialog(this.mContext);
        this.mDeleteDialog.setCancelable(false);
        init();
    }

    void prepareDeleteNodeList() {
        this.mDeleteTemps.clear();
        for (AnnotNode node : this.mCheckedNodes) {
            if (node.getParent() == null || !this.mCheckedNodes.contains(node.getParent())) {
                this.mDeleteTemps.add(node);
            }
        }
    }

    void clearAllNodes() {
        DocumentManager.getInstance(this.mPdfViewCtrl).setCurrentAnnot(null);
        if (this.mAdapter.selectAll()) {
            final Activity context = this.mContext;
            resetDeleteDialog();
            Collections.sort(this.mCheckedNodes);
            if (checkDeleteStatus() == 1) {
                if (this.mSRDeleteDialog == null || this.mSRDeleteDialog.getDialog().getOwnerActivity() == null) {
                    this.mSRDeleteDialog = new UITextEditDialog(context);
                    this.mSRDeleteDialog.getPromptTextView().setText(R.string.rv_panel_annot_delete_tips);
                    this.mSRDeleteDialog.setTitle(R.string.cloud_delete_tv);
                    this.mSRDeleteDialog.getInputEditText().setVisibility(8);
                }
                this.mSRDeleteDialog.getOKButton().setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        AnnotPanel.this.mSRDeleteDialog.dismiss();
                        AnnotPanel.this.mDeleteDialog.setMessage(context.getString(R.string.rv_panel_annot_deleting));
                        AnnotPanel.this.mDeleteDialog.show();
                        AnnotPanel.this.prepareDeleteNodeList();
                        AnnotPanel.this.deleteItems();
                    }
                });
                this.mSRDeleteDialog.show();
            } else if (checkDeleteStatus() == 2) {
                UIToast.getInstance(this.mContext).show((CharSequence) "Failed...");
            } else {
                this.mDeleteDialog.setMessage(context.getString(R.string.rv_panel_annot_deleting));
                this.mDeleteDialog.show();
                prepareDeleteNodeList();
                deleteItems();
            }
            this.mAdapter.notifyDataSetChanged();
        }
    }

    private boolean isPageLoaded(PDFPage page) {
        if (page == null) {
            return false;
        }
        try {
            if (page.getIndex() < this.mLoadedIndex || this.mLoadedState == 4) {
                return true;
            }
            return false;
        } catch (PDFException e) {
            return false;
        }
    }

    public void setStatusPause(boolean status) {
        this.mPause = status;
    }

    public AnnotEventListener getAnnotEventListener() {
        return this.mAnnotEventListener;
    }

    private void init() {
        this.mMainLayout.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        this.mChangedTextView = (TextView) this.mMainLayout.findViewById(R.id.rv_panel_annot_notify);
        this.mChangedTextView.setVisibility(8);
    }

    public void onClick(View v) {
    }

    private void resetDeleteDialog() {
        if (this.mDeleteDialog != null) {
            this.mDeleteDialog.dismiss();
        }
    }

    private void deleteItems() {
        int size = this.mDeleteTemps.size();
        if (size == 0) {
            if (this.mLoadedState == 6) {
                startSearch(this.mLoadedIndex);
            }
            resetDeleteDialog();
            if (this.mAdapter.getAnnotCount() == 0) {
                this.mPanel.showNoAnnotsView();
            }
            this.mAdapter.notifyDataSetChanged();
            return;
        }
        if (this.mLoadedState != 4) {
            this.mLoadedState = 6;
        }
        if (this.mPdfViewCtrl.getDoc() != null) {
            if (((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler() != null) {
                ((UIExtensionsManager) this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
            }
            AnnotNode node = (AnnotNode) this.mDeleteTemps.get(size - 1);
            if (node == null || node.isPageDivider()) {
                this.mDeleteTemps.remove(node);
                deleteItems();
            } else if (node.canDelete()) {
                this.mAdapter.removeNode(node, new DeleteCallback() {
                    public void result(boolean success, AnnotNode n) {
                        if (success) {
                            AnnotPanel.this.mDeleteTemps.remove(n);
                            AnnotPanel.this.onChecked(false, n);
                            AnnotPanel.this.deleteItems();
                            return;
                        }
                        AnnotPanel.this.resetDeleteDialog();
                    }
                });
            } else {
                node.setChecked(false);
                onChecked(false, node);
                this.mDeleteTemps.remove(node);
                deleteItems();
            }
        }
    }

    private int checkDeleteStatus() {
        int status = 0;
        for (AnnotNode node : this.mCheckedNodes) {
            if (!node.canDelete()) {
                status = 2;
                AnnotNode parent = node.getParent();
                while (parent != null) {
                    if (parent.isChecked() && parent.canDelete()) {
                        status = 1;
                        break;
                    }
                    parent = parent.getParent();
                }
                if (status == 2) {
                    break;
                }
            }
        }
        return status;
    }

    public void onChecked(boolean isChecked, AnnotNode node) {
        if (!isChecked) {
            this.mCheckedNodes.remove(node);
        } else if (!this.mCheckedNodes.contains(node)) {
            this.mCheckedNodes.add(node);
        }
    }

    public boolean jumpToPage(int position) {
        final AnnotNode node = (AnnotNode) this.mAdapter.getItem(position);
        if (node == null || node.isPageDivider() || !node.isRootNode() || AppUtil.isEmpty(node.getUID())) {
            return false;
        }
        this.mPdfViewCtrl.addTask(new Task(new CallBack() {
            public void result(Task task) {
                try {
                    Annot annot = AppAnnotUtil.getAnnot(AnnotPanel.this.mPdfViewCtrl.getDoc().getPage(node.getPageIndex()), node.getUID());
                    RectF rect = annot.getRect();
                    RectF rectPageView = new RectF();
                    if (AnnotPanel.this.mPdfViewCtrl.convertPdfRectToPageViewRect(rect, rectPageView, node.getPageIndex())) {
                        AnnotPanel.this.mPdfViewCtrl.gotoPage(node.getPageIndex(), rectPageView.left - ((((float) AnnotPanel.this.mPdfViewCtrl.getWidth()) - rectPageView.width()) / 2.0f), rectPageView.top - ((((float) AnnotPanel.this.mPdfViewCtrl.getHeight()) - rectPageView.height()) / 2.0f));
                    } else {
                        AnnotPanel.this.mPdfViewCtrl.gotoPage(node.getPageIndex(), new PointF(rect.left, rect.top));
                    }
                    if (((UIExtensionsManager) AnnotPanel.this.mPdfViewCtrl.getUIExtensionsManager()).getCurrentToolHandler() != null) {
                        ((UIExtensionsManager) AnnotPanel.this.mPdfViewCtrl.getUIExtensionsManager()).setCurrentToolHandler(null);
                    }
                    DocumentManager.getInstance(AnnotPanel.this.mPdfViewCtrl).setCurrentAnnot(annot);
                } catch (PDFException e) {
                    e.printStackTrace();
                }
            }
        }) {
            protected void execute() {
            }
        });
        return true;
    }

    public int getCount() {
        return this.mAdapter.getCount();
    }

    public int getCurrentStatus() {
        return this.mLoadedState;
    }

    public AnnotAdapter getAdapter() {
        return this.mAdapter;
    }

    public void onDocOpened() {
        this.mAdapter.preparePageNodes();
        reset();
    }

    public void onDocWillClose() {
        reset();
        resetDeleteDialog();
        this.mSRDeleteDialog = null;
    }

    private void reset() {
        this.mChangedTextView.setVisibility(8);
        this.mPanel.hideNoAnnotsView();
        this.mLoadedState = 2;
        this.mPause = false;
        this.mLoadedIndex = 0;
        this.mCheckedNodes.clear();
        this.mAdapter.clearNodes();
        this.mAdapter.notifyDataSetChanged();
    }

    public void startSearch(final int pageIndex) {
        this.mLoadedIndex = pageIndex;
        searchPage(pageIndex, new OnSearchPageEndListener() {
            public void onResult(boolean success, ArrayList<AnnotNode> nodeList) {
                int pageCount = AnnotPanel.this.mPdfViewCtrl.getPageCount();
                if (!success) {
                    AnnotPanel.this.mLoadedState = 5;
                    AnnotPanel.this.mPanel.updateLoadedPage(pageIndex + 1, pageCount);
                } else if (AnnotPanel.this.mPause) {
                    AnnotPanel.this.mPanel.pauseSearch(pageIndex);
                    AnnotPanel.this.mLoadedState = 3;
                } else {
                    AnnotPanel.this.mPanel.updateLoadedPage(pageIndex + 1, pageCount);
                    int nCount = nodeList.size();
                    for (int i = 0; i < nCount; i++) {
                        AnnotPanel.this.mAdapter.addNode((AnnotNode) nodeList.get(i));
                    }
                    AnnotPanel.this.mAdapter.establishNodeList();
                    AnnotPanel.this.mAdapter.notifyDataSetChanged();
                    if (pageIndex >= pageCount - 1) {
                        AnnotPanel.this.mLoadedState = 4;
                        AnnotPanel.this.mPanel.updateLoadedPage(0, 0);
                    } else if (AnnotPanel.this.mLoadedState != 2) {
                        AnnotPanel.this.startSearch(pageIndex + 1);
                    }
                }
            }
        });
    }

    private void searchPage(int pageIndex, OnSearchPageEndListener result) {
        this.mPdfViewCtrl.addTask(new SearchPageTask(this.mPdfViewCtrl, pageIndex, result));
    }
}
