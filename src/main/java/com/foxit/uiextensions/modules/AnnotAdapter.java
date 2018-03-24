package com.foxit.uiextensions.modules;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.common.UIAnnotReply;
import com.foxit.uiextensions.annots.common.UIAnnotReply.CommentContent;
import com.foxit.uiextensions.annots.common.UIAnnotReply.ReplyCallback;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnnotAdapter extends BaseAdapter {
    private CheckBoxChangeListener mCheckBoxChangeListener;
    private Context mContext;
    private boolean mDateChanged = false;
    private final AppDisplay mDisplay;
    private final ArrayList<Boolean> mItemMoreViewShow;
    private List<AnnotNode> mNodeList;
    private ArrayList<List<AnnotNode>> mPageNodesList;
    private ViewGroup mParent;
    private PDFViewCtrl mPdfViewCtrl;
    private PopupWindow mPopupWindow;

    public interface CheckBoxChangeListener {
        void onChecked(boolean z, AnnotNode annotNode);
    }

    public interface DeleteCallback {
        void result(boolean z, AnnotNode annotNode);
    }

    private static final class ViewHolder {
        public TextView mAuthorTextView;
        public TextView mContentsTextView;
        public TextView mCounterTextView;
        public TextView mDateTextView;
        public ImageView mIconImageView;
        public ImageView mItemMore;
        public LinearLayout mItemMoreView;
        public LinearLayout mItem_ll_comment;
        public LinearLayout mItem_ll_delete;
        public LinearLayout mItem_ll_reply;
        public View mMainLayout;
        public TextView mPageIndexTextView;
        public View mPageLayout;
        public ImageView mRedImageView;
        public RelativeLayout mRlMain;

        private ViewHolder() {
        }
    }

    public void setCheckBoxChangeListener(CheckBoxChangeListener listener) {
        this.mCheckBoxChangeListener = listener;
    }

    protected boolean hasDataChanged() {
        return this.mDateChanged;
    }

    protected void resetDataChanged() {
        this.mDateChanged = false;
    }

    public AnnotAdapter(Context context, PDFViewCtrl pdfViewCtrl, ViewGroup parent, ArrayList<Boolean> itemMoreViewShow) {
        this.mContext = context;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mParent = parent;
        this.mDisplay = new AppDisplay(this.mContext);
        this.mItemMoreViewShow = itemMoreViewShow;
        this.mNodeList = new ArrayList();
        this.mPageNodesList = new ArrayList();
    }

    protected void preparePageNodes() {
        for (int i = 0; i < this.mPdfViewCtrl.getPageCount(); i++) {
            this.mPageNodesList.add(null);
        }
    }

    public void setPopupWindow(PopupWindow popupWindow) {
        this.mPopupWindow = popupWindow;
    }

    public void establishNodeList() {
        int i;
        this.mNodeList.clear();
        for (i = 0; i < this.mPageNodesList.size(); i++) {
            List<AnnotNode> nodes = (List) this.mPageNodesList.get(i);
            if (nodes != null && nodes.size() > 0) {
                int index = this.mNodeList.size();
                boolean addPageNode = false;
                int count = 0;
                for (AnnotNode an : nodes) {
                    if (an.isRootNode() && !an.isRedundant()) {
                        addPageNode = true;
                        this.mNodeList.add(an);
                        count++;
                        establishNodeRoot(an);
                    }
                }
                if (addPageNode) {
                    AnnotNode pageNode = new AnnotNode(i);
                    pageNode.counter = count;
                    this.mNodeList.add(index, pageNode);
                }
            }
        }
        this.mItemMoreViewShow.clear();
        for (i = 0; i < this.mNodeList.size(); i++) {
            this.mItemMoreViewShow.add(Boolean.valueOf(false));
        }
    }

    private void establishNodeRoot(AnnotNode parent) {
        if (!parent.isLeafNode()) {
            for (AnnotNode child : parent.getChildren()) {
                this.mNodeList.add(child);
                establishNodeRoot(child);
            }
        }
    }

    public void clearNodes() {
        this.mNodeList.clear();
        this.mItemMoreViewShow.clear();
        for (int i = 0; i < this.mPageNodesList.size(); i++) {
            List<AnnotNode> nodes = (List) this.mPageNodesList.get(i);
            if (nodes != null) {
                nodes.clear();
            }
            this.mPageNodesList.set(i, null);
        }
        notifyDataSetChanged();
    }

    public void addNode(AnnotNode node) {
        List<AnnotNode> nodes = (List) this.mPageNodesList.get(node.getPageIndex());
        if (nodes == null) {
            nodes = new ArrayList();
            this.mPageNodesList.set(node.getPageIndex(), nodes);
        }
        if (!nodes.contains(node)) {
            if (!node.getReplyTo().equals("") || !node.getUID().equals("")) {
                boolean needFind = !node.getReplyTo().equals("");
                for (AnnotNode an : nodes) {
                    if (needFind && an.getUID().equals(node.getReplyTo())) {
                        node.setParent(an);
                        an.addChildNode(node);
                        needFind = false;
                    } else if (!an.getReplyTo().equals("") && an.getReplyTo().equals(node.getUID())) {
                        an.setParent(node);
                        node.addChildNode(an);
                    }
                }
                nodes.add(node);
                Collections.sort(nodes);
            }
        }
    }

    public void updateNode(Annot annot) {
        if (annot != null) {
            try {
                if (AppAnnotUtil.isSupportEditAnnot(annot) && annot.getUniqueID() != null && !annot.getUniqueID().equals("")) {
                    List<AnnotNode> nodes = (List) this.mPageNodesList.get(annot.getPage().getIndex());
                    if (nodes != null) {
                        for (AnnotNode node : nodes) {
                            if (node.getUID().equals(annot.getUniqueID())) {
                                if (annot.isMarkup()) {
                                    node.setAuthor(((Markup) annot).getTitle());
                                }
                                node.setContent(annot.getContent());
                                String date = AppDmUtil.getLocalDateString(annot.getModifiedDateTime());
                                if ((date == null || date.equals(AppDmUtil.dateOriValue)) && annot.isMarkup()) {
                                    date = AppDmUtil.getLocalDateString(((Markup) annot).getCreationDateTime());
                                }
                                node.setModifiedDate(date);
                                notifyDataSetChanged();
                            }
                        }
                    }
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeNode(PDFPage page, String uid) {
        if (uid != null && !uid.equals("")) {
            try {
                List<AnnotNode> nodes = (List) this.mPageNodesList.get(page.getIndex());
                if (nodes != null) {
                    for (int i = nodes.size() - 1; i >= 0; i--) {
                        AnnotNode node = (AnnotNode) nodes.get(i);
                        if (node.getUID().equals(uid)) {
                            removeNodeFromPageNode(node, nodes);
                            if (node.getParent() != null) {
                                node.getParent().removeChild(node);
                                return;
                            } else {
                                node.removeChildren();
                                return;
                            }
                        }
                    }
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeNodeFromPageNode(AnnotNode node, List<AnnotNode> pageNodes) {
        if (pageNodes != null && node != null && pageNodes.contains(node)) {
            List<AnnotNode> children = node.getChildren();
            if (!(children == null || children.size() == 0)) {
                for (AnnotNode child : children) {
                    removeNodeFromPageNode(child, pageNodes);
                }
            }
            pageNodes.remove(node);
        }
    }

    public void removeNode(final AnnotNode node, final DeleteCallback callback) {
        final List<AnnotNode> nodes = (List) this.mPageNodesList.get(node.getPageIndex());
        if (nodes != null && nodes.contains(node)) {
            PDFPage page = getAnnotNodePage(node);
            if (page != null) {
                Annot annot = AppAnnotUtil.getAnnot(page, node.getUID());
                if (annot != null) {
                    DocumentManager.getInstance(this.mPdfViewCtrl).removeAnnot(annot, true, new Callback() {
                        public void result(Event event, boolean success) {
                            if (success) {
                                AnnotAdapter.this.removeNodeFromPageNode(node, nodes);
                                node.removeChildren();
                                callback.result(true, node);
                                AnnotAdapter.this.establishNodeList();
                                AnnotAdapter.this.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        } else if (callback != null) {
            callback.result(true, node);
        }
    }

    public void notifyDataSetChanged() {
        this.mContext.runOnUiThread(new Runnable() {
            public void run() {
                super.notifyDataSetChanged();
            }
        });
    }

    public PDFPage getAnnotNodePage(AnnotNode node) {
        try {
            return this.mPdfViewCtrl.getDoc().getPage(node.getPageIndex());
        } catch (PDFException e) {
            return null;
        }
    }

    public int getCount() {
        return this.mNodeList.size();
    }

    public Object getItem(int position) {
        if (position < 0 || position >= this.mNodeList.size()) {
            return null;
        }
        return this.mNodeList.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final AnnotNode node = (AnnotNode) this.mNodeList.get(position);
        if (node == null) {
            return null;
        }
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(this.mContext, R.layout.panel_annot_item, null);
            holder.mPageLayout = convertView.findViewById(R.id.rv_panel_annot_item_page_layout);
            holder.mPageIndexTextView = (TextView) holder.mPageLayout.findViewById(R.id.rv_panel_annot_item_page_tv);
            holder.mCounterTextView = (TextView) convertView.findViewById(R.id.rv_panel_annot_item_page_count_tv);
            holder.mRlMain = (RelativeLayout) convertView.findViewById(R.id.rd_panel_rl_main);
            holder.mMainLayout = convertView.findViewById(R.id.rv_panel_annot_item_main_layout);
            holder.mAuthorTextView = (TextView) holder.mMainLayout.findViewById(R.id.rv_panel_annot_item_author_tv);
            holder.mContentsTextView = (TextView) holder.mMainLayout.findViewById(R.id.rv_panel_annot_item_contents_tv);
            holder.mDateTextView = (TextView) holder.mMainLayout.findViewById(R.id.rv_panel_annot_item_date_tv);
            holder.mIconImageView = (ImageView) holder.mMainLayout.findViewById(R.id.rv_panel_annot_item_icon_iv);
            holder.mRedImageView = (ImageView) holder.mMainLayout.findViewById(R.id.rv_panel_annot_item_icon_red);
            holder.mItemMore = (ImageView) holder.mMainLayout.findViewById(R.id.rd_panel_annot_item_more);
            holder.mItemMoreView = (LinearLayout) convertView.findViewById(R.id.rd_annot_item_moreview);
            holder.mItem_ll_reply = (LinearLayout) convertView.findViewById(R.id.rd_annot_item_ll_reply);
            holder.mItem_ll_comment = (LinearLayout) convertView.findViewById(R.id.rd_annot_item_ll_comment);
            holder.mItem_ll_delete = (LinearLayout) convertView.findViewById(R.id.rd_annot_item_ll_delete);
            LayoutParams rlMainLayoutParams = (LinearLayout.LayoutParams) holder.mRlMain.getLayoutParams();
            RelativeLayout.LayoutParams mainLayoutParams = (RelativeLayout.LayoutParams) holder.mMainLayout.getLayoutParams();
            LinearLayout.LayoutParams contentLayoutParams = (LinearLayout.LayoutParams) holder.mContentsTextView.getLayoutParams();
            int marginLeft;
            int marginRight;
            int paddingRightSmall;
            if (this.mDisplay.isPad()) {
                rlMainLayoutParams.height = (int) this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_2l_pad);
                marginLeft = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_pad);
                marginRight = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_pad);
                holder.mPageLayout.setPadding(marginLeft, holder.mPageLayout.getPaddingTop(), marginRight, holder.mPageLayout.getPaddingBottom());
                mainLayoutParams.leftMargin = marginLeft;
                contentLayoutParams.rightMargin = marginRight;
                paddingRightSmall = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_right_margin_pad);
                holder.mItemMore.setPadding(holder.mItemMore.getPaddingLeft(), holder.mItemMore.getPaddingTop(), paddingRightSmall, holder.mItemMore.getPaddingBottom());
            } else {
                rlMainLayoutParams.height = (int) this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_2l_phone);
                marginLeft = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_phone);
                marginRight = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_right_margin_phone);
                holder.mPageLayout.setPadding(marginLeft, holder.mPageLayout.getPaddingTop(), marginRight, holder.mPageLayout.getPaddingBottom());
                mainLayoutParams.leftMargin = marginLeft;
                contentLayoutParams.rightMargin = marginRight;
                paddingRightSmall = (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_right_margin_phone);
                holder.mItemMore.setPadding(holder.mItemMore.getPaddingLeft(), holder.mItemMore.getPaddingTop(), paddingRightSmall, holder.mItemMore.getPaddingBottom());
            }
            holder.mRlMain.setLayoutParams(rlMainLayoutParams);
            holder.mMainLayout.setLayoutParams(mainLayoutParams);
            holder.mContentsTextView.setLayoutParams(contentLayoutParams);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (node.isPageDivider()) {
            holder.mRlMain.setVisibility(8);
            holder.mItemMoreView.setVisibility(8);
            holder.mPageLayout.setVisibility(0);
            holder.mCounterTextView.setText(node.counter);
            holder.mPageIndexTextView.setText(new StringBuilder(String.valueOf(AppResource.getString(this.mContext, R.string.rv_panel_annot_item_pagenum))).append(" ").append(node.getPageIndex() + 1).toString());
            return convertView;
        }
        holder.mRlMain.setVisibility(0);
        holder.mPageLayout.setVisibility(8);
        int level = node.getLevel();
        holder.mDateTextView.setText(node.getModifiedDate());
        holder.mAuthorTextView.setText(node.getAuthor());
        holder.mContentsTextView.setText(node.getContent());
        if (node.isRootNode()) {
            holder.mIconImageView.setImageResource(AppAnnotUtil.getIconId(node.getType()));
        } else {
            holder.mIconImageView.setImageResource(R.drawable.annot_reply_selector);
        }
        holder.mItemMore.setVisibility(0);
        if (this.mPdfViewCtrl.getDoc() != null) {
            if (DocumentManager.getInstance(this.mPdfViewCtrl).canAddAnnot()) {
                if (AppAnnotUtil.contentsModifiable(node.getType())) {
                    holder.mItem_ll_reply.setVisibility(0);
                    holder.mItem_ll_comment.setVisibility(0);
                    holder.mItem_ll_delete.setVisibility(0);
                    holder.mItem_ll_reply.setTag(Integer.valueOf(position));
                    holder.mItem_ll_reply.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            if (!AppUtil.isFastDoubleClick()) {
                                ((LinearLayout) v.getParent()).setVisibility(8);
                                AnnotAdapter.this.mItemMoreViewShow.set(((Integer) v.getTag()).intValue(), Boolean.valueOf(false));
                                Context access$3 = AnnotAdapter.this.mContext;
                                PDFViewCtrl access$4 = AnnotAdapter.this.mPdfViewCtrl;
                                ViewGroup access$5 = AnnotAdapter.this.mParent;
                                int i = UIAnnotReply.TITLE_COMMENT_ID;
                                final AnnotNode annotNode = node;
                                UIAnnotReply.replyToAnnot(access$3, access$4, access$5, true, i, new ReplyCallback() {
                                    public void result(String contents) {
                                        PDFPage page = AnnotAdapter.this.getAnnotNodePage(annotNode);
                                        if (page != null) {
                                            Annot annot = AppAnnotUtil.getAnnot(page, annotNode.getUID());
                                            if (annot != null) {
                                                UIAnnotReply.addReplyAnnot(AnnotAdapter.this.mPdfViewCtrl, annot, page, AppDmUtil.randomUUID(null), contents, null);
                                            }
                                        }
                                    }

                                    public String getContent() {
                                        return null;
                                    }
                                });
                            }
                        }
                    });
                    holder.mItem_ll_comment.setTag(Integer.valueOf(position));
                    holder.mItem_ll_comment.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            if (!AppUtil.isFastDoubleClick()) {
                                ((LinearLayout) v.getParent()).setVisibility(8);
                                AnnotAdapter.this.mItemMoreViewShow.set(((Integer) v.getTag()).intValue(), Boolean.valueOf(false));
                                Context access$3 = AnnotAdapter.this.mContext;
                                PDFViewCtrl access$4 = AnnotAdapter.this.mPdfViewCtrl;
                                ViewGroup access$5 = AnnotAdapter.this.mParent;
                                int i = UIAnnotReply.TITLE_EDIT_ID;
                                final AnnotNode annotNode = node;
                                UIAnnotReply.replyToAnnot(access$3, access$4, access$5, true, i, new ReplyCallback() {
                                    public void result(final String content) {
                                        PDFPage page = AnnotAdapter.this.getAnnotNodePage(annotNode);
                                        if (page != null) {
                                            final Annot annot = AppAnnotUtil.getAnnot(page, annotNode.getUID());
                                            if (annot != null && AppAnnotUtil.isSupportEditAnnot(annot)) {
                                                DocumentManager instance = DocumentManager.getInstance(AnnotAdapter.this.mPdfViewCtrl);
                                                AnnotContent commentContent = new CommentContent(annot, content);
                                                final AnnotNode annotNode = annotNode;
                                                instance.modifyAnnot(annot, commentContent, true, new Callback() {
                                                    public void result(Event event, boolean success) {
                                                        if (success) {
                                                            annotNode.setContent(content);
                                                            try {
                                                                annotNode.setModifiedDate(AppDmUtil.getLocalDateString(annot.getModifiedDateTime()));
                                                            } catch (PDFException e) {
                                                                e.printStackTrace();
                                                            }
                                                            AnnotAdapter.this.notifyDataSetInvalidated();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }

                                    public String getContent() {
                                        return annotNode.getContent().toString();
                                    }
                                });
                            }
                        }
                    });
                } else {
                    holder.mItem_ll_reply.setVisibility(8);
                    holder.mItem_ll_comment.setVisibility(8);
                    holder.mItem_ll_delete.setVisibility(0);
                }
                holder.mItem_ll_delete.setTag(Integer.valueOf(position));
                holder.mItem_ll_delete.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (!AppUtil.isFastDoubleClick()) {
                            ((LinearLayout) v.getParent()).setVisibility(8);
                            AnnotAdapter.this.mItemMoreViewShow.set(((Integer) v.getTag()).intValue(), Boolean.valueOf(false));
                            AnnotAdapter.this.removeNode(node, new DeleteCallback() {
                                public void result(boolean success, AnnotNode node) {
                                    if (success) {
                                        AnnotAdapter.this.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                });
            } else if (AppAnnotUtil.contentsModifiable(node.getType())) {
                holder.mItem_ll_comment.setVisibility(0);
                holder.mItem_ll_reply.setVisibility(8);
                holder.mItem_ll_delete.setVisibility(8);
                holder.mItem_ll_comment.setTag(Integer.valueOf(position));
                holder.mItem_ll_comment.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (!AppUtil.isFastDoubleClick()) {
                            ((LinearLayout) v.getParent()).setVisibility(8);
                            AnnotAdapter.this.mItemMoreViewShow.set(((Integer) v.getTag()).intValue(), Boolean.valueOf(false));
                            Context access$3 = AnnotAdapter.this.mContext;
                            PDFViewCtrl access$4 = AnnotAdapter.this.mPdfViewCtrl;
                            ViewGroup access$5 = AnnotAdapter.this.mParent;
                            int i = UIAnnotReply.TITLE_EDIT_ID;
                            final AnnotNode annotNode = node;
                            UIAnnotReply.replyToAnnot(access$3, access$4, access$5, false, i, new ReplyCallback() {
                                public void result(String contents) {
                                }

                                public String getContent() {
                                    return (String) annotNode.getContent();
                                }
                            });
                        }
                    }
                });
            } else {
                holder.mItemMore.setVisibility(8);
            }
            holder.mContentsTextView.setBackgroundResource(R.color.ux_color_translucent);
            if (AppAnnotUtil.contentsModifiable(node.getType())) {
                holder.mContentsTextView.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (!AppUtil.isFastDoubleClick()) {
                            int i;
                            boolean show = false;
                            for (i = 0; i < AnnotAdapter.this.mNodeList.size(); i++) {
                                if (((Boolean) AnnotAdapter.this.mItemMoreViewShow.get(i)).booleanValue()) {
                                    show = true;
                                    break;
                                }
                            }
                            if (show) {
                                for (i = 0; i < AnnotAdapter.this.mItemMoreViewShow.size(); i++) {
                                    AnnotAdapter.this.mItemMoreViewShow.set(i, Boolean.valueOf(false));
                                }
                                AnnotAdapter.this.notifyDataSetChanged();
                            } else if (node == null) {
                            } else {
                                if (node.canReply()) {
                                    Context access$3 = AnnotAdapter.this.mContext;
                                    PDFViewCtrl access$4 = AnnotAdapter.this.mPdfViewCtrl;
                                    ViewGroup access$5 = AnnotAdapter.this.mParent;
                                    boolean canAddAnnot = DocumentManager.getInstance(AnnotAdapter.this.mPdfViewCtrl).canAddAnnot();
                                    int i2 = UIAnnotReply.TITLE_EDIT_ID;
                                    final AnnotNode annotNode = node;
                                    UIAnnotReply.replyToAnnot(access$3, access$4, access$5, canAddAnnot, i2, new ReplyCallback() {
                                        public void result(final String content) {
                                            PDFPage page = AnnotAdapter.this.getAnnotNodePage(annotNode);
                                            if (page != null) {
                                                final Annot annot = AppAnnotUtil.getAnnot(page, annotNode.getUID());
                                                if (annot != null && AppAnnotUtil.isSupportEditAnnot(annot)) {
                                                    DocumentManager instance = DocumentManager.getInstance(AnnotAdapter.this.mPdfViewCtrl);
                                                    AnnotContent commentContent = new CommentContent(annot, content);
                                                    final AnnotNode annotNode = annotNode;
                                                    instance.modifyAnnot(annot, commentContent, true, new Callback() {
                                                        public void result(Event event, boolean success) {
                                                            if (success) {
                                                                annotNode.setContent(content);
                                                                try {
                                                                    annotNode.setModifiedDate(AppDmUtil.getLocalDateString(annot.getModifiedDateTime()));
                                                                } catch (PDFException e) {
                                                                    e.printStackTrace();
                                                                }
                                                                AnnotAdapter.this.notifyDataSetChanged();
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }

                                        public String getContent() {
                                            return annotNode.getContent().toString();
                                        }
                                    });
                                } else if (!node.isPageDivider() && node.isRootNode() && !AppUtil.isEmpty(node.getUID())) {
                                    AnnotAdapter.this.mPdfViewCtrl.gotoPage(node.getPageIndex(), 0.0f, 0.0f);
                                    if (AnnotAdapter.this.mPopupWindow != null && AnnotAdapter.this.mPopupWindow.isShowing()) {
                                        AnnotAdapter.this.mPopupWindow.dismiss();
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
        holder.mContentsTextView.setTextColor(this.mContext.getResources().getColor(R.color.ux_text_color_body2_dark));
        holder.mRedImageView.setVisibility(8);
        int dx = this.mDisplay.dp2px(37.0f);
        if (level > 0) {
            holder.mMainLayout.setPadding(dx * Math.min(level, 2), 0, 0, 0);
        } else {
            holder.mMainLayout.setPadding(0, 0, 0, 0);
        }
        LayoutParams params = (RelativeLayout.LayoutParams) holder.mItemMoreView.getLayoutParams();
        params.height = -1;
        holder.mItemMoreView.setLayoutParams(params);
        holder.mItemMore.setTag(Integer.valueOf(position));
        holder.mItemMore.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                int tag = ((Integer) v.getTag()).intValue();
                for (int i = 0; i < AnnotAdapter.this.mItemMoreViewShow.size(); i++) {
                    if (i == tag) {
                        AnnotAdapter.this.mItemMoreViewShow.set(i, Boolean.valueOf(true));
                    } else {
                        AnnotAdapter.this.mItemMoreViewShow.set(i, Boolean.valueOf(false));
                    }
                }
                AnnotAdapter.this.notifyDataSetChanged();
            }
        });
        if (((Boolean) this.mItemMoreViewShow.get(position)).booleanValue()) {
            holder.mItemMoreView.setVisibility(0);
        } else {
            holder.mItemMoreView.setVisibility(8);
        }
        return convertView;
    }

    public boolean selectAll() {
        int i;
        List<AnnotNode> nodes;
        if (getSelectedCount() == getAnnotCount()) {
            for (i = 0; i < this.mPageNodesList.size(); i++) {
                nodes = (List) this.mPageNodesList.get(i);
                if (nodes != null) {
                    for (AnnotNode node : nodes) {
                        if (!node.isRedundant() && node.isChecked()) {
                            node.setChecked(false);
                            if (this.mCheckBoxChangeListener != null) {
                                this.mCheckBoxChangeListener.onChecked(false, node);
                            }
                        }
                    }
                }
            }
            return false;
        }
        for (i = 0; i < this.mPageNodesList.size(); i++) {
            nodes = (List) this.mPageNodesList.get(i);
            if (nodes != null) {
                for (AnnotNode node2 : nodes) {
                    if (!(node2.isRedundant() || node2.isChecked())) {
                        node2.setChecked(true);
                        if (this.mCheckBoxChangeListener != null) {
                            this.mCheckBoxChangeListener.onChecked(true, node2);
                        }
                    }
                }
            }
        }
        return true;
    }

    protected void onPageRemoved(boolean success, int index) {
        if (success) {
            this.mPageNodesList.remove(index);
            updateNodePageIndex(index, this.mPageNodesList.size());
            establishNodeList();
            this.mDateChanged = true;
        }
    }

    protected void updateNodePageIndex(int start, int end) {
        for (int i = start; i < end; i++) {
            List<AnnotNode> nodes = (List) this.mPageNodesList.get(i);
            if (nodes != null) {
                for (AnnotNode node : nodes) {
                    node.setPageIndex(i);
                }
            }
        }
    }

    protected void onPageMoved(boolean success, int index, int dstIndex) {
        if (success) {
            int i;
            if (index < dstIndex) {
                for (i = index; i < dstIndex; i++) {
                    Collections.swap(this.mPageNodesList, i, i + 1);
                }
            } else {
                for (i = index; i > dstIndex; i--) {
                    Collections.swap(this.mPageNodesList, i, i - 1);
                }
            }
            updateNodePageIndex(Math.min(index, dstIndex), Math.max(index, dstIndex) + 1);
            establishNodeList();
            this.mDateChanged = true;
        }
    }

    protected void onPagesInsert(boolean success, int dstIndex, int[] range) {
        if (success) {
            for (int i = 0; i < range.length / 2; i++) {
                for (int index = 0; index < range[(i * 2) + 1]; index++) {
                    this.mPageNodesList.add(dstIndex, null);
                    dstIndex++;
                }
            }
            updateNodePageIndex(dstIndex, this.mPageNodesList.size());
            establishNodeList();
            this.mDateChanged = true;
        }
    }

    private int getSelectedCount() {
        int count = 0;
        for (int i = 0; i < this.mPageNodesList.size(); i++) {
            List<AnnotNode> nodes = (List) this.mPageNodesList.get(i);
            if (nodes != null) {
                for (AnnotNode node : nodes) {
                    if (node.isChecked()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public int getAnnotCount() {
        int count = 0;
        for (int i = 0; i < this.mPageNodesList.size(); i++) {
            List<AnnotNode> nodes = (List) this.mPageNodesList.get(i);
            if (nodes != null) {
                for (AnnotNode node : nodes) {
                    if (!node.isRedundant()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public AnnotNode getAnnotNode(PDFPage page, String uid) {
        if (uid == null || uid.equals("")) {
            return null;
        }
        try {
            List<AnnotNode> nodes = (List) this.mPageNodesList.get(page.getIndex());
            if (nodes == null) {
                return null;
            }
            for (AnnotNode node : nodes) {
                if (node.getUID().equals(uid)) {
                    return node;
                }
            }
            return null;
        } catch (PDFException e) {
        }
    }
}
