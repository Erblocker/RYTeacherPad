package com.foxit.uiextensions.annots.common;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue.IdleHandler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MotionEventCompat;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.DateTime;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFPage;
import com.foxit.sdk.pdf.annots.Annot;
import com.foxit.sdk.pdf.annots.Markup;
import com.foxit.uiextensions.DocumentManager;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.annots.AnnotContent;
import com.foxit.uiextensions.annots.note.NoteAnnotContent;
import com.foxit.uiextensions.controls.dialog.AppDialogManager;
import com.foxit.uiextensions.controls.dialog.UITextEditDialog;
import com.foxit.uiextensions.utils.AppAnnotUtil;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.Event;
import com.foxit.uiextensions.utils.Event.Callback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class UIAnnotReply {
    public static final int TITLE_COMMENT_ID = R.string.fx_string_reply;
    public static final int TITLE_EDIT_ID = R.string.fx_string_comment;

    public interface ReplyCallback {
        String getContent();

        void result(String str);
    }

    /* renamed from: com.foxit.uiextensions.annots.common.UIAnnotReply$1 */
    class AnonymousClass1 implements ReplyCallback {
        private final /* synthetic */ Annot val$annot;
        private final /* synthetic */ Context val$context;
        private final /* synthetic */ ViewGroup val$parent;
        private final /* synthetic */ PDFViewCtrl val$pdfViewCtrl;

        AnonymousClass1(Annot annot, PDFViewCtrl pDFViewCtrl, Context context, ViewGroup viewGroup) {
            this.val$annot = annot;
            this.val$pdfViewCtrl = pDFViewCtrl;
            this.val$context = context;
            this.val$parent = viewGroup;
        }

        public void result(String content) {
            PDFPage page = null;
            try {
                page = this.val$annot.getPage();
            } catch (PDFException e) {
                e.printStackTrace();
            }
            PDFViewCtrl pDFViewCtrl = this.val$pdfViewCtrl;
            Annot annot = this.val$annot;
            String randomUUID = AppDmUtil.randomUUID(null);
            final Context context = this.val$context;
            final PDFViewCtrl pDFViewCtrl2 = this.val$pdfViewCtrl;
            final ViewGroup viewGroup = this.val$parent;
            final Annot annot2 = this.val$annot;
            UIAnnotReply.addReplyAnnot(pDFViewCtrl, annot, page, randomUUID, content, new Callback() {
                public void result(Event event, boolean success) {
                    UIAnnotReply.showComments(context, pDFViewCtrl2, viewGroup, annot2);
                }
            });
        }

        public String getContent() {
            return null;
        }
    }

    public static class CommentContent implements AnnotContent {
        Annot annot;
        String content;

        public CommentContent(Annot annot, String content) {
            this.annot = annot;
            this.content = content;
        }

        public int getPageIndex() {
            try {
                return this.annot.getPage().getIndex();
            } catch (PDFException e) {
                e.printStackTrace();
                return 0;
            }
        }

        public int getType() {
            try {
                return this.annot.getType();
            } catch (PDFException e) {
                e.printStackTrace();
                return 0;
            }
        }

        public String getNM() {
            try {
                return this.annot.getUniqueID();
            } catch (PDFException e) {
                e.printStackTrace();
                return null;
            }
        }

        public RectF getBBox() {
            try {
                return this.annot.getRect();
            } catch (PDFException e) {
                e.printStackTrace();
                return null;
            }
        }

        public int getColor() {
            try {
                return (int) this.annot.getBorderColor();
            } catch (PDFException e) {
                e.printStackTrace();
                return 0;
            }
        }

        public int getOpacity() {
            try {
                return (int) ((((Markup) this.annot).getOpacity() * 255.0f) + 0.5f);
            } catch (PDFException e) {
                e.printStackTrace();
                return 0;
            }
        }

        public float getLineWidth() {
            try {
                if (this.annot.getBorderInfo() != null) {
                    return this.annot.getBorderInfo().getWidth();
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
            return 0.0f;
        }

        public String getSubject() {
            try {
                return ((Markup) this.annot).getSubject();
            } catch (PDFException e) {
                e.printStackTrace();
                return null;
            }
        }

        public DateTime getModifiedDate() {
            return AppDmUtil.currentDateToDocumentDate();
        }

        public String getContents() {
            return this.content;
        }

        public String getIntent() {
            try {
                return ((Markup) this.annot).getIntent();
            } catch (PDFException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static class CommentsFragment extends DialogFragment {
        private static final int DELETE_CAN = 0;
        private static final int DELETE_SRCAN = 1;
        private boolean isTouchHold;
        private CommentsAdapter mAdapter = new CommentsAdapter(this.mPDFViewCtrl, this.mItemMoreViewShow);
        private Markup mAnnot;
        private final List<AnnotNode> mCheckedNodes = new ArrayList();
        private UITextEditDialog mClearDialog;
        private Context mContext = null;
        private ProgressDialog mDeleteDialog;
        private View mDialogContentView;
        private AppDisplay mDisplay;
        private ArrayList<Boolean> mItemMoreViewShow = new ArrayList();
        private PDFViewCtrl mPDFViewCtrl;
        private ViewGroup mParent;
        private TextView mReplyClear;
        private AnnotNode mRootNode;
        private UITextEditDialog mSRDeleteDialog;

        class AnnotNode implements Comparable<AnnotNode> {
            private String author;
            private List<AnnotNode> childNodes;
            private CharSequence content;
            private String date;
            private boolean editEnable;
            private boolean isChecked;
            private boolean isExpanded;
            private String mCreationDate;
            private String mModifyDate;
            private int pageIndex;
            private AnnotNode parent;
            private Annot replyAnnot;
            private Annot replyToAnnot;
            private int type;

            AnnotNode(int pageIndex, Annot annot, Annot replyTo) {
                this.pageIndex = pageIndex;
                this.replyAnnot = annot;
                this.replyToAnnot = replyTo;
            }

            public void clearChildren() {
                if (this.childNodes != null) {
                    this.childNodes.clear();
                }
            }

            void addChildNode(AnnotNode node) {
                if (this.childNodes == null) {
                    this.childNodes = new ArrayList();
                }
                if (!this.childNodes.contains(node)) {
                    this.childNodes.add(node);
                }
            }

            void removeChildNode(AnnotNode node) {
                if (this.childNodes != null) {
                    this.childNodes.remove(node);
                }
            }

            public int getPageIndex() {
                return this.pageIndex;
            }

            public boolean isEditEnable() {
                return this.editEnable;
            }

            public void setEditEnable(boolean editEnable) {
                this.editEnable = editEnable;
            }

            public List<AnnotNode> getChildren() {
                return this.childNodes;
            }

            public int getType() {
                return this.type;
            }

            public void setType(int type) {
                this.type = type;
            }

            public String getAuthor() {
                return this.author == null ? "" : this.author;
            }

            public void setAuthor(String author) {
                this.author = author;
            }

            public void setContent(CharSequence content) {
                this.content = content;
            }

            public CharSequence getContent() {
                return this.content == null ? "" : this.content;
            }

            public void setDate(String date) {
                this.date = date;
            }

            public String getDate() {
                return this.date == null ? AppDmUtil.dateOriValue : this.date;
            }

            public void setModifyDate(String modifyDate) {
                this.mModifyDate = modifyDate;
            }

            public String getModifyDate() {
                return this.mModifyDate == null ? AppDmUtil.dateOriValue : this.mModifyDate;
            }

            public void setCreationDate(String creationDate) {
                this.mCreationDate = creationDate;
            }

            public String getCreationDate() {
                return this.mCreationDate == null ? AppDmUtil.dateOriValue : this.mCreationDate;
            }

            public void setChecked(boolean isChecked) {
                this.isChecked = isChecked;
            }

            public boolean isChecked() {
                return this.isChecked;
            }

            public void setParent(AnnotNode parent) {
                this.parent = parent;
            }

            public AnnotNode getParent() {
                return this.parent;
            }

            public boolean isRoot() {
                return this.parent == null;
            }

            public boolean isLeafNode() {
                return this.childNodes == null || this.childNodes.size() == 0;
            }

            public int getLevel() {
                return this.parent == null ? 0 : this.parent.getLevel() + 1;
            }

            public boolean isExpanded() {
                return this.isExpanded || this.parent == null || getLevel() != 1;
            }

            public void setExpanded(boolean isExpanded) {
                this.isExpanded = isExpanded;
            }

            public int compareTo(AnnotNode another) {
                if (another == null) {
                    return 0;
                }
                if (getLevel() != another.getLevel()) {
                    return getLevel() - another.getLevel();
                }
                try {
                    String lCreationDate = getCreationDate();
                    if (lCreationDate == null || AppDmUtil.dateOriValue.equals(lCreationDate)) {
                        lCreationDate = getModifyDate();
                    }
                    String rCreationDate = another.getCreationDate();
                    if (rCreationDate == null || AppDmUtil.dateOriValue.equals(rCreationDate)) {
                        rCreationDate = another.getModifyDate();
                    }
                    Date lDate = AppDmUtil.documentDateToJavaDate(AppDmUtil.parseDocumentDate(lCreationDate));
                    Date rDate = AppDmUtil.documentDateToJavaDate(AppDmUtil.parseDocumentDate(rCreationDate));
                    if (lDate == null && rDate == null) {
                        return 0;
                    }
                    if (lDate.before(rDate)) {
                        return -1;
                    }
                    if (lDate.after(rDate)) {
                        return 1;
                    }
                    return 0;
                } catch (Exception e) {
                    return 0;
                }
            }
        }

        class CommentsAdapter extends BaseAdapter {
            private ArrayList<Boolean> mMoreViewShow;
            private final List<AnnotNode> mNodes = new ArrayList();
            private final List<AnnotNode> mNodesTmp = new ArrayList();
            private PDFViewCtrl mPdfViewCtrl;

            final class ViewHolder {
                public TextView mAuthorTextView;
                public TextView mContentsTextView;
                public TextView mDateTextView;
                public ImageView mExpandImageView;
                public ImageView mIconImageView;
                public ImageView mIv_comment;
                public ImageView mIv_delete;
                public ImageView mIv_relist_more;
                public ImageView mIv_reply;
                public LinearLayout mLl_relist_comment;
                public LinearLayout mLl_relist_delete;
                public LinearLayout mLl_relist_moreview;
                public LinearLayout mLl_relist_reply;
                public RelativeLayout mReplyListRL;
                public LinearLayout mReplyRoot;
                public TextView mTv_comment;
                public TextView mTv_delete;
                public TextView mTv_reply;

                ViewHolder() {
                }
            }

            public CommentsAdapter(PDFViewCtrl pdfViewCtrl, ArrayList<Boolean> moreViewShow) {
                this.mPdfViewCtrl = pdfViewCtrl;
                this.mMoreViewShow = moreViewShow;
            }

            public void removeNode(AnnotNode node) {
                if (node.getChildren() != null) {
                    node.clearChildren();
                }
                try {
                    DocumentManager.getInstance(CommentsFragment.this.mPDFViewCtrl).removeAnnot(node.replyAnnot, true, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.mNodesTmp.remove(node);
                if (node.getParent() != null) {
                    node.getParent().removeChildNode(node);
                }
                CommentsFragment.this.mCheckedNodes.remove(node);
                CommentsFragment.this.notifyCounter();
                CommentsFragment.this.deleteItems();
                establishReplyList(CommentsFragment.this.mRootNode);
                notifyDataSetChanged();
                if (node.equals(CommentsFragment.this.mRootNode)) {
                    AppDialogManager.getInstance().dismiss(CommentsFragment.this);
                }
            }

            public void resetCheckedNodes() {
                CommentsFragment.this.mCheckedNodes.clear();
                for (int i = 0; i < this.mNodes.size(); i++) {
                    AnnotNode node = (AnnotNode) this.mNodes.get(i);
                    if (!CommentsFragment.this.mCheckedNodes.contains(node)) {
                        CommentsFragment.this.mCheckedNodes.add(node);
                    }
                }
                CommentsFragment.this.notifyCounter();
            }

            void clearNodes() {
                this.mNodes.clear();
                this.mNodesTmp.clear();
            }

            public void establishReplyList(AnnotNode node) {
                this.mNodes.clear();
                int index = this.mNodesTmp.indexOf(node);
                if (index != -1) {
                    AnnotNode n = (AnnotNode) this.mNodesTmp.get(index);
                    this.mNodes.add(n);
                    establishNodeRoot(n);
                    this.mMoreViewShow.clear();
                    for (int i = 0; i < this.mNodes.size(); i++) {
                        this.mMoreViewShow.add(Boolean.valueOf(false));
                    }
                }
            }

            public void addNode(AnnotNode node) {
                if (!this.mNodesTmp.contains(node)) {
                    if (node.replyAnnot != null || node.replyToAnnot != null) {
                        boolean needFind = node.replyToAnnot != null;
                        try {
                            for (AnnotNode an : this.mNodesTmp) {
                                if (needFind && an.replyAnnot.getUniqueID().equals(node.replyToAnnot.getUniqueID())) {
                                    node.setParent(an);
                                    an.addChildNode(node);
                                    needFind = false;
                                } else if (an.replyToAnnot != null && node.replyAnnot.getUniqueID().equals(an.replyToAnnot.getUniqueID())) {
                                    an.setParent(node);
                                    node.addChildNode(an);
                                }
                            }
                        } catch (PDFException e) {
                            e.printStackTrace();
                        }
                        this.mNodesTmp.add(node);
                    }
                }
            }

            private void establishNodeRoot(AnnotNode parent) {
                if (parent != null && !parent.isLeafNode() && parent.isExpanded() && parent.getChildren() != null) {
                    if (parent.getChildren().size() > 1) {
                        Collections.sort(parent.getChildren());
                    }
                    for (AnnotNode child : parent.getChildren()) {
                        this.mNodes.add(child);
                        establishNodeRoot(child);
                    }
                }
            }

            public int getCount() {
                return this.mNodes.size();
            }

            public Object getItem(int position) {
                return this.mNodes.get(position);
            }

            public long getItemId(int position) {
                return (long) position;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                if (convertView == null) {
                    holder = new ViewHolder();
                    convertView = View.inflate(CommentsFragment.this.getActivity(), R.layout.annot_reply_item, null);
                    holder.mReplyRoot = (LinearLayout) convertView.findViewById(R.id.annot_reply_top_layout);
                    holder.mReplyListRL = (RelativeLayout) convertView.findViewById(R.id.annot_reply_list_rl);
                    holder.mAuthorTextView = (TextView) convertView.findViewById(R.id.annot_reply_author_tv);
                    holder.mContentsTextView = (TextView) convertView.findViewById(R.id.annot_reply_contents_tv);
                    holder.mDateTextView = (TextView) convertView.findViewById(R.id.annot_reply_date_tv);
                    holder.mIconImageView = (ImageView) convertView.findViewById(R.id.annot_iv_reply_icon);
                    holder.mExpandImageView = (ImageView) convertView.findViewById(R.id.annot_reply_expand_iv);
                    holder.mIv_relist_more = (ImageView) convertView.findViewById(R.id.rd_annot_relist_item_more);
                    holder.mLl_relist_moreview = (LinearLayout) convertView.findViewById(R.id.rd_annot_relist_item_moreview);
                    holder.mLl_relist_reply = (LinearLayout) convertView.findViewById(R.id.rd_annot_relist_item_ll_reply);
                    holder.mIv_reply = (ImageView) convertView.findViewById(R.id.rd_annot_relist_item_reply);
                    holder.mTv_reply = (TextView) convertView.findViewById(R.id.rd_annot_item_tv_reply);
                    holder.mLl_relist_comment = (LinearLayout) convertView.findViewById(R.id.rd_annot_item_ll_comment);
                    holder.mIv_comment = (ImageView) convertView.findViewById(R.id.rd_annot_item_comment);
                    holder.mTv_comment = (TextView) convertView.findViewById(R.id.rd_annot_item_tv_comment);
                    holder.mLl_relist_delete = (LinearLayout) convertView.findViewById(R.id.rd_annot_item_ll_delete);
                    holder.mIv_delete = (ImageView) convertView.findViewById(R.id.rd_annot_item_delete);
                    holder.mTv_delete = (TextView) convertView.findViewById(R.id.rd_annot_item_tv_delete);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                final AnnotNode node = (AnnotNode) this.mNodes.get(position);
                LayoutParams params = (LinearLayout.LayoutParams) holder.mContentsTextView.getLayoutParams();
                if (node.isRoot()) {
                    holder.mIconImageView.setImageResource(AppAnnotUtil.getIconId(AppAnnotUtil.getTypeString(CommentsFragment.this.mAnnot)));
                } else {
                    holder.mIconImageView.setImageResource(R.drawable.annot_reply_selector);
                }
                if (node.isRoot()) {
                    holder.mExpandImageView.setVisibility(8);
                    params.leftMargin = CommentsFragment.this.mDisplay.dp2px(0.0f);
                } else if (node.getLevel() != 1 || node.isLeafNode()) {
                    holder.mExpandImageView.setVisibility(8);
                    holder.mIconImageView.setVisibility(0);
                    params.leftMargin = CommentsFragment.this.mDisplay.dp2px(29.0f);
                } else {
                    holder.mExpandImageView.setVisibility(0);
                    params.leftMargin = CommentsFragment.this.mDisplay.dp2px(53.0f);
                    if (node.isExpanded()) {
                        holder.mExpandImageView.setImageResource(R.drawable.annot_reply_item_minus_selector);
                    } else {
                        holder.mExpandImageView.setImageResource(R.drawable.annot_reply_item_add_selector);
                    }
                    holder.mExpandImageView.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            node.setExpanded(!node.isExpanded());
                            CommentsAdapter.this.establishReplyList(CommentsFragment.this.mRootNode);
                            CommentsAdapter.this.notifyDataSetChanged();
                        }
                    });
                }
                holder.mContentsTextView.setLayoutParams(params);
                int level = node.getLevel() > 2 ? 2 : node.getLevel();
                int ax = CommentsFragment.this.mDisplay.dp2px(32.0f);
                int px = CommentsFragment.this.mDisplay.dp2px(5.0f);
                int ex = CommentsFragment.this.mDisplay.dp2px(21.0f);
                int dx = CommentsFragment.this.mDisplay.dp2px(24.0f);
                if (level == 0) {
                    convertView.setPadding(0, 0, 0, 0);
                } else if (level != 1) {
                    convertView.setPadding((ax + px) + ((dx + px) * (level - 1)), 0, 0, 0);
                } else if (node.isLeafNode()) {
                    convertView.setPadding((ax + px) + ((dx + px) * (level - 1)), 0, 0, 0);
                } else {
                    convertView.setPadding(((ax + px) - ex) + ((dx + px) * (level - 1)), 0, 0, 0);
                }
                String date = node.getDate();
                if (date == null) {
                    date = AppDmUtil.dateOriValue;
                }
                holder.mDateTextView.setText(date);
                holder.mAuthorTextView.setText(node.getAuthor());
                holder.mContentsTextView.setText(node.getContent());
                holder.mIv_relist_more.setTag(Integer.valueOf(position));
                holder.mIv_relist_more.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (!AppUtil.isFastDoubleClick()) {
                            int tag = ((Integer) v.getTag()).intValue();
                            for (int i = 0; i < CommentsAdapter.this.mMoreViewShow.size(); i++) {
                                if (i == tag) {
                                    CommentsAdapter.this.mMoreViewShow.set(i, Boolean.valueOf(true));
                                } else {
                                    CommentsAdapter.this.mMoreViewShow.set(i, Boolean.valueOf(false));
                                }
                            }
                            CommentsAdapter.this.notifyDataSetChanged();
                        }
                    }
                });
                if (DocumentManager.getInstance(CommentsFragment.this.mPDFViewCtrl).canAddAnnot()) {
                    holder.mLl_relist_reply.setVisibility(0);
                    holder.mLl_relist_reply.setTag(Integer.valueOf(position));
                    holder.mLl_relist_reply.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            if (!AppUtil.isFastDoubleClick()) {
                                ((LinearLayout) v.getParent()).setVisibility(8);
                                CommentsAdapter.this.mMoreViewShow.set(((Integer) v.getTag()).intValue(), Boolean.valueOf(false));
                                if (node != null) {
                                    Context access$7 = CommentsFragment.this.mContext;
                                    PDFViewCtrl access$0 = CommentsFragment.this.mPDFViewCtrl;
                                    ViewGroup access$8 = CommentsFragment.this.mParent;
                                    int i = UIAnnotReply.TITLE_COMMENT_ID;
                                    final AnnotNode annotNode = node;
                                    UIAnnotReply.replyToAnnot(access$7, access$0, access$8, true, i, new ReplyCallback() {
                                        public void result(String Content) {
                                            final int pageIndex = annotNode.getPageIndex();
                                            final String uid = AppDmUtil.randomUUID(null);
                                            try {
                                                final PDFPage page = CommentsFragment.this.mPDFViewCtrl.getDoc().getPage(pageIndex);
                                                PDFViewCtrl access$1 = CommentsAdapter.this.mPdfViewCtrl;
                                                Annot access$12 = annotNode.replyAnnot;
                                                final AnnotNode annotNode = annotNode;
                                                final String str = Content;
                                                UIAnnotReply.addReplyAnnot(access$1, access$12, page, uid, Content, new Callback() {
                                                    public void result(Event event, boolean success) {
                                                        AnnotNode annotNode = new AnnotNode(pageIndex, AppAnnotUtil.getAnnot(page, uid), annotNode.replyAnnot);
                                                        annotNode.setAuthor(AppDmUtil.getAnnotAuthor());
                                                        String dateTemp = AppDmUtil.getLocalDateString(AppDmUtil.currentDateToDocumentDate());
                                                        annotNode.setDate(dateTemp);
                                                        annotNode.setCreationDate(dateTemp);
                                                        annotNode.setModifyDate(dateTemp);
                                                        annotNode.setContent(str);
                                                        annotNode.setType(1);
                                                        annotNode.setEditEnable(true);
                                                        CommentsFragment.this.mAdapter.addNode(annotNode);
                                                        CommentsFragment.this.mAdapter.establishReplyList(CommentsFragment.this.mRootNode);
                                                        annotNode.setChecked(false);
                                                        CommentsFragment.this.mCheckedNodes.clear();
                                                        CommentsFragment.this.mAdapter.notifyDataSetChanged();
                                                    }
                                                });
                                            } catch (PDFException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        public String getContent() {
                                            return null;
                                        }
                                    });
                                }
                            }
                        }
                    });
                    holder.mLl_relist_comment.setVisibility(0);
                    holder.mLl_relist_comment.setTag(Integer.valueOf(position));
                    holder.mLl_relist_comment.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            if (!AppUtil.isFastDoubleClick()) {
                                ((LinearLayout) v.getParent()).setVisibility(8);
                                CommentsAdapter.this.mMoreViewShow.set(((Integer) v.getTag()).intValue(), Boolean.valueOf(false));
                                Context access$7 = CommentsFragment.this.mContext;
                                PDFViewCtrl access$0 = CommentsFragment.this.mPDFViewCtrl;
                                ViewGroup access$8 = CommentsFragment.this.mParent;
                                int i = UIAnnotReply.TITLE_EDIT_ID;
                                final AnnotNode annotNode = node;
                                UIAnnotReply.replyToAnnot(access$7, access$0, access$8, true, i, new ReplyCallback() {
                                    public void result(String content) {
                                        final Annot annot = annotNode.replyAnnot;
                                        if (annot != null) {
                                            DocumentManager instance = DocumentManager.getInstance(CommentsAdapter.this.mPdfViewCtrl);
                                            Annot access$1 = annotNode.replyAnnot;
                                            AnnotContent commentContent = new CommentContent(annotNode.replyAnnot, content);
                                            final AnnotNode annotNode = annotNode;
                                            instance.modifyAnnot(access$1, commentContent, true, new Callback() {
                                                public void result(Event event, boolean success) {
                                                    if (success) {
                                                        try {
                                                            annotNode.setAuthor(((Markup) annot).getTitle());
                                                            annotNode.setDate(AppDmUtil.getLocalDateString(annot.getModifiedDateTime()));
                                                            annotNode.setModifyDate(AppDmUtil.getLocalDateString(annot.getModifiedDateTime()));
                                                            annotNode.setContent(annot.getContent());
                                                            CommentsFragment.this.mAdapter.notifyDataSetChanged();
                                                        } catch (PDFException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    }

                                    public String getContent() {
                                        return (String) annotNode.getContent();
                                    }
                                });
                            }
                        }
                    });
                    if (node.isEditEnable()) {
                        holder.mLl_relist_delete.setVisibility(0);
                        holder.mLl_relist_delete.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                if (!AppUtil.isFastDoubleClick()) {
                                    CommentsFragment.this.mCheckedNodes.clear();
                                    if (!CommentsFragment.this.mCheckedNodes.contains(node)) {
                                        CommentsFragment.this.mCheckedNodes.add(node);
                                    }
                                    Collections.sort(CommentsFragment.this.mCheckedNodes);
                                    CommentsFragment.this.beginToDelete();
                                }
                            }
                        });
                    } else {
                        holder.mLl_relist_delete.setVisibility(8);
                    }
                } else {
                    holder.mLl_relist_comment.setVisibility(0);
                    holder.mLl_relist_reply.setVisibility(8);
                    holder.mLl_relist_delete.setVisibility(8);
                    holder.mLl_relist_comment.setTag(Integer.valueOf(position));
                    holder.mLl_relist_comment.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            if (!AppUtil.isFastDoubleClick()) {
                                ((LinearLayout) v.getParent()).setVisibility(8);
                                CommentsAdapter.this.mMoreViewShow.set(((Integer) v.getTag()).intValue(), Boolean.valueOf(false));
                                if (node != null) {
                                    Context access$7 = CommentsFragment.this.mContext;
                                    PDFViewCtrl access$0 = CommentsFragment.this.mPDFViewCtrl;
                                    ViewGroup access$8 = CommentsFragment.this.mParent;
                                    int i = UIAnnotReply.TITLE_EDIT_ID;
                                    final AnnotNode annotNode = node;
                                    UIAnnotReply.replyToAnnot(access$7, access$0, access$8, false, i, new ReplyCallback() {
                                        public void result(String Content) {
                                        }

                                        public String getContent() {
                                            return (String) annotNode.getContent();
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
                holder.mContentsTextView.setTag(Integer.valueOf(position));
                holder.mContentsTextView.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (!AppUtil.isFastDoubleClick()) {
                            int tag = ((Integer) v.getTag()).intValue();
                            boolean hasShow = false;
                            int index = 0;
                            for (int i = 0; i < CommentsAdapter.this.mMoreViewShow.size(); i++) {
                                if (((Boolean) CommentsAdapter.this.mMoreViewShow.get(i)).booleanValue()) {
                                    hasShow = true;
                                    index = i;
                                    break;
                                }
                            }
                            if (hasShow) {
                                CommentsAdapter.this.mMoreViewShow.set(index, Boolean.valueOf(false));
                                CommentsFragment.this.mAdapter.notifyDataSetChanged();
                                return;
                            }
                            boolean editable;
                            if (DocumentManager.getInstance(CommentsFragment.this.mPDFViewCtrl).canAddAnnot()) {
                                editable = true;
                            } else {
                                editable = false;
                            }
                            Context access$7 = CommentsFragment.this.mContext;
                            PDFViewCtrl access$0 = CommentsFragment.this.mPDFViewCtrl;
                            ViewGroup access$8 = CommentsFragment.this.mParent;
                            int i2 = UIAnnotReply.TITLE_EDIT_ID;
                            final AnnotNode annotNode = node;
                            UIAnnotReply.replyToAnnot(access$7, access$0, access$8, editable, i2, new ReplyCallback() {
                                public void result(String content) {
                                    final Annot annot = annotNode.replyAnnot;
                                    if (annot != null) {
                                        DocumentManager instance = DocumentManager.getInstance(CommentsAdapter.this.mPdfViewCtrl);
                                        Annot access$1 = annotNode.replyAnnot;
                                        AnnotContent commentContent = new CommentContent(annot, content);
                                        final AnnotNode annotNode = annotNode;
                                        instance.modifyAnnot(access$1, commentContent, true, new Callback() {
                                            public void result(Event event, boolean success) {
                                                if (success) {
                                                    try {
                                                        annotNode.setAuthor(((Markup) annot).getTitle());
                                                        annotNode.setDate(AppDmUtil.getLocalDateString(annot.getModifiedDateTime()));
                                                        annotNode.setModifyDate(AppDmUtil.getLocalDateString(annot.getModifiedDateTime()));
                                                        annotNode.setContent(annot.getContent());
                                                        CommentsFragment.this.mAdapter.notifyDataSetChanged();
                                                    } catch (PDFException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }

                                public String getContent() {
                                    return (String) annotNode.getContent();
                                }
                            });
                        }
                    }
                });
                LayoutParams replyListRLLayoutParams = (LinearLayout.LayoutParams) holder.mReplyListRL.getLayoutParams();
                LayoutParams replyMoreLayoutParams = (RelativeLayout.LayoutParams) holder.mIv_relist_more.getLayoutParams();
                LinearLayout.LayoutParams contentLayoutParams = (LinearLayout.LayoutParams) holder.mContentsTextView.getLayoutParams();
                if (AppDisplay.getInstance(CommentsFragment.this.mContext).isPad()) {
                    replyListRLLayoutParams.leftMargin = (int) CommentsFragment.this.getActivity().getApplicationContext().getResources().getDimension(R.dimen.ux_horz_left_margin_pad);
                    replyMoreLayoutParams.rightMargin = (int) CommentsFragment.this.getActivity().getApplicationContext().getResources().getDimension(R.dimen.ux_horz_right_margin_pad);
                    contentLayoutParams.rightMargin = (int) CommentsFragment.this.getActivity().getApplicationContext().getResources().getDimension(R.dimen.ux_horz_left_margin_pad);
                } else {
                    replyListRLLayoutParams.leftMargin = (int) CommentsFragment.this.getActivity().getApplicationContext().getResources().getDimension(R.dimen.ux_horz_left_margin_phone);
                    replyMoreLayoutParams.rightMargin = (int) CommentsFragment.this.getActivity().getApplicationContext().getResources().getDimension(R.dimen.ux_horz_right_margin_phone);
                    contentLayoutParams.rightMargin = (int) CommentsFragment.this.getActivity().getApplicationContext().getResources().getDimension(R.dimen.ux_horz_left_margin_phone);
                }
                holder.mReplyListRL.setLayoutParams(replyListRLLayoutParams);
                holder.mIv_relist_more.setLayoutParams(replyMoreLayoutParams);
                holder.mContentsTextView.setLayoutParams(contentLayoutParams);
                LayoutParams paramsMoreView = (RelativeLayout.LayoutParams) holder.mLl_relist_moreview.getLayoutParams();
                paramsMoreView.height = holder.mReplyRoot.getMeasuredHeight();
                holder.mLl_relist_moreview.setLayoutParams(paramsMoreView);
                if (((Boolean) this.mMoreViewShow.get(position)).booleanValue()) {
                    holder.mLl_relist_moreview.setVisibility(0);
                } else {
                    holder.mLl_relist_moreview.setVisibility(8);
                }
                return convertView;
            }
        }

        public void onAttach(Activity activity) {
            super.onAttach(activity);
            if (this.mAdapter == null) {
                dismiss();
            } else {
                Looper.myQueue().addIdleHandler(new IdleHandler() {
                    public boolean queueIdle() {
                        CommentsFragment.this.init();
                        return false;
                    }
                });
            }
        }

        private void init() {
            if (this.mAnnot == null) {
                dismiss();
                return;
            }
            this.mAdapter.clearNodes();
            this.mCheckedNodes.clear();
            try {
                String date;
                PDFPage page = this.mAnnot.getPage();
                int pageIndex = page.getIndex();
                int count = page.getAnnotCount();
                for (int i = 0; i < count; i++) {
                    Annot annot = page.getAnnot(i);
                    if (!(annot == null || AppAnnotUtil.isSupportGroupElement(annot))) {
                        String name = annot.getUniqueID();
                        if (name == null) {
                            annot.setUniqueID(AppDmUtil.randomUUID(null));
                        } else {
                            boolean isMatch = name.equals(this.mAnnot.getUniqueID());
                            if (AppAnnotUtil.isSupportEditAnnot(annot) && !isMatch) {
                                Markup dmAnnot = (Markup) annot;
                                AnnotNode node = new AnnotNode(pageIndex, dmAnnot, AppAnnotUtil.getReplyToAnnot(dmAnnot));
                                node.setAuthor(dmAnnot.getTitle());
                                node.setContent(dmAnnot.getContent());
                                date = AppDmUtil.getLocalDateString(dmAnnot.getModifiedDateTime());
                                if (date == null || date.equals(AppDmUtil.dateOriValue)) {
                                    date = AppDmUtil.getLocalDateString(dmAnnot.getCreationDateTime());
                                }
                                node.setDate(date);
                                node.setCreationDate(AppDmUtil.getLocalDateString(dmAnnot.getCreationDateTime()));
                                node.setModifyDate(AppDmUtil.getLocalDateString(dmAnnot.getModifiedDateTime()));
                                node.setType(dmAnnot.getType());
                                node.setEditEnable(DocumentManager.getInstance(this.mPDFViewCtrl).canAddAnnot());
                                this.mAdapter.addNode(node);
                            }
                        }
                    }
                }
                this.mRootNode = new AnnotNode(pageIndex, this.mAnnot, AppAnnotUtil.getReplyToAnnot(this.mAnnot));
                this.mRootNode.setAuthor(this.mAnnot.getTitle());
                this.mRootNode.setContent(this.mAnnot.getContent());
                date = AppDmUtil.getLocalDateString(this.mAnnot.getModifiedDateTime());
                if (date == null || date.equals(AppDmUtil.dateOriValue)) {
                    date = AppDmUtil.getLocalDateString(this.mAnnot.getCreationDateTime());
                }
                this.mRootNode.setDate(date);
                this.mRootNode.setCreationDate(AppDmUtil.getLocalDateString(this.mAnnot.getCreationDateTime()));
                this.mRootNode.setModifyDate(AppDmUtil.getLocalDateString(this.mAnnot.getModifiedDateTime()));
                this.mRootNode.setEditEnable(DocumentManager.getInstance(this.mPDFViewCtrl).canAddAnnot());
                this.mRootNode.setType(this.mAnnot.getType());
                this.mAdapter.addNode(this.mRootNode);
                this.mAdapter.establishReplyList(this.mRootNode);
                if (DocumentManager.getInstance(this.mPDFViewCtrl).canAddAnnot()) {
                    this.mReplyClear.setEnabled(true);
                } else {
                    this.mReplyClear.setEnabled(false);
                }
                this.mAdapter.notifyDataSetChanged();
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }

        public void init(PDFViewCtrl pdfViewCtrl, ViewGroup parent, Annot dmAnnot) {
            this.mPDFViewCtrl = pdfViewCtrl;
            this.mParent = parent;
            this.mAnnot = (Markup) dmAnnot;
            this.mAdapter.clearNodes();
            this.mCheckedNodes.clear();
        }

        @SuppressLint({"NewApi"})
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.mContext = getActivity();
            this.mDisplay = AppDisplay.getInstance(this.mContext);
            if (!AppDisplay.getInstance(this.mContext).isPad()) {
                int theme;
                if (VERSION.SDK_INT >= 21) {
                    theme = 16974065;
                } else if (VERSION.SDK_INT >= 14) {
                    theme = 16974125;
                } else if (VERSION.SDK_INT >= 11) {
                    theme = 16974065;
                } else {
                    theme = 16973838;
                }
                setStyle(1, theme);
            }
        }

        @SuppressLint({"NewApi"})
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (this.mAdapter == null) {
                return super.onCreateDialog(savedInstanceState);
            }
            if (!AppDisplay.getInstance(this.mContext).isPad()) {
                return super.onCreateDialog(savedInstanceState);
            }
            int theme;
            if (VERSION.SDK_INT >= 21) {
                theme = 16973941;
            } else if (VERSION.SDK_INT >= 14) {
                theme = 16974132;
            } else if (VERSION.SDK_INT >= 11) {
                theme = 16973941;
            } else {
                theme = R.style.rv_dialog_style;
            }
            Dialog dialog = new Dialog(getActivity(), theme);
            int width = this.mDisplay.getDialogWidth();
            int height = this.mDisplay.getDialogHeight();
            dialog.setContentView(createView());
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = width;
            params.height = height;
            dialog.getWindow().setAttributes(params);
            dialog.setCanceledOnTouchOutside(true);
            dialog.getWindow().setFlags(1024, 1024);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dlg_title_bg_4circle_corner_white);
            return dialog;
        }

        private View createView() {
            this.mDialogContentView = View.inflate(getActivity(), R.layout.annot_reply_main, null);
            RelativeLayout replyTop = (RelativeLayout) this.mDialogContentView.findViewById(R.id.annot_reply_top);
            ImageView replyBack = (ImageView) this.mDialogContentView.findViewById(R.id.annot_reply_back);
            TextView replyTitle = (TextView) this.mDialogContentView.findViewById(R.id.annot_reply_list_title);
            ListView listView = (ListView) this.mDialogContentView.findViewById(R.id.annot_reply_list);
            this.mReplyClear = (TextView) this.mDialogContentView.findViewById(R.id.annot_reply_clear);
            this.mReplyClear.setEnabled(false);
            LinearLayout replyContent = (LinearLayout) this.mDialogContentView.findViewById(R.id.annot_reply_list_ll_content);
            RelativeLayout.LayoutParams replyTopLayoutParams = (RelativeLayout.LayoutParams) replyTop.getLayoutParams();
            RelativeLayout.LayoutParams replyBackLayoutParams = (RelativeLayout.LayoutParams) replyBack.getLayoutParams();
            RelativeLayout.LayoutParams replyTitleLayoutParams = (RelativeLayout.LayoutParams) replyTitle.getLayoutParams();
            RelativeLayout.LayoutParams replyClearLayoutParams = (RelativeLayout.LayoutParams) this.mReplyClear.getLayoutParams();
            if (this.mDisplay.isPad()) {
                replyTop.setBackgroundResource(R.drawable.dlg_title_bg_circle_corner_gray);
                replyTopLayoutParams.height = (int) getActivity().getApplicationContext().getResources().getDimension(R.dimen.ux_toolbar_height_pad);
                replyBackLayoutParams.leftMargin = (int) getActivity().getApplicationContext().getResources().getDimension(R.dimen.ux_horz_left_margin_pad);
                replyTitleLayoutParams.leftMargin = 0;
                replyTitleLayoutParams.addRule(13);
                replyClearLayoutParams.rightMargin = (int) getActivity().getApplicationContext().getResources().getDimension(R.dimen.ux_horz_left_margin_pad);
                replyContent.setBackgroundResource(R.drawable.dlg_title_bg_cc_bottom_yellow);
                replyBack.setVisibility(8);
            } else {
                replyTop.setBackgroundResource(R.color.ux_bg_color_toolbar_light);
                replyTopLayoutParams.height = (int) getActivity().getApplicationContext().getResources().getDimension(R.dimen.ux_toolbar_height_phone);
                replyBackLayoutParams.leftMargin = (int) getActivity().getApplicationContext().getResources().getDimension(R.dimen.ux_horz_left_margin_phone);
                replyTitleLayoutParams.leftMargin = this.mDisplay.dp2px(70.0f);
                replyTitleLayoutParams.addRule(13, 0);
                replyClearLayoutParams.rightMargin = (int) getActivity().getApplicationContext().getResources().getDimension(R.dimen.ux_horz_left_margin_phone);
                replyContent.setBackgroundResource(R.color.ux_color_yellow);
                replyBack.setVisibility(0);
            }
            replyTop.setLayoutParams(replyTopLayoutParams);
            replyBack.setLayoutParams(replyBackLayoutParams);
            replyTitle.setLayoutParams(replyTitleLayoutParams);
            this.mReplyClear.setLayoutParams(replyClearLayoutParams);
            this.mDialogContentView.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
            this.mReplyClear.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!AppUtil.isFastDoubleClick()) {
                        CommentsFragment.this.mClearDialog = new UITextEditDialog(CommentsFragment.this.mContext);
                        CommentsFragment.this.mClearDialog.setTitle(CommentsFragment.this.mContext.getResources().getString(R.string.hm_clear));
                        CommentsFragment.this.mClearDialog.getPromptTextView().setText(CommentsFragment.this.mContext.getResources().getString(R.string.rv_panel_annot_delete_tips));
                        CommentsFragment.this.mClearDialog.getInputEditText().setVisibility(8);
                        CommentsFragment.this.mClearDialog.getOKButton().setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                if (!AppUtil.isFastDoubleClick()) {
                                    CommentsFragment.this.mAdapter.resetCheckedNodes();
                                    Collections.sort(CommentsFragment.this.mCheckedNodes);
                                    CommentsFragment.this.mClearDialog.dismiss();
                                    CommentsFragment.this.mDeleteDialog = new ProgressDialog(CommentsFragment.this.getActivity());
                                    CommentsFragment.this.mDeleteDialog.setProgressStyle(0);
                                    CommentsFragment.this.mDeleteDialog.setCancelable(false);
                                    CommentsFragment.this.mDeleteDialog.setIndeterminate(false);
                                    CommentsFragment.this.mDeleteDialog.setMessage(CommentsFragment.this.getActivity().getString(R.string.rv_panel_annot_deleting));
                                    CommentsFragment.this.mDeleteDialog.show();
                                    CommentsFragment.this.deleteItems();
                                }
                            }
                        });
                        CommentsFragment.this.mClearDialog.getCancelButton().setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                CommentsFragment.this.mClearDialog.dismiss();
                            }
                        });
                        CommentsFragment.this.mClearDialog.show();
                    }
                }
            });
            listView.setAdapter(this.mAdapter);
            listView.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    switch (MotionEventCompat.getActionMasked(event)) {
                        case 0:
                            boolean show = false;
                            int position = 0;
                            for (int i = 0; i < CommentsFragment.this.mAdapter.getCount(); i++) {
                                if (((Boolean) CommentsFragment.this.mItemMoreViewShow.get(i)).booleanValue()) {
                                    show = true;
                                    position = i;
                                    if (show) {
                                        CommentsFragment.this.mItemMoreViewShow.set(position, Boolean.valueOf(false));
                                        CommentsFragment.this.mAdapter.notifyDataSetChanged();
                                        CommentsFragment.this.isTouchHold = true;
                                        return true;
                                    }
                                }
                            }
                            if (show) {
                                CommentsFragment.this.mItemMoreViewShow.set(position, Boolean.valueOf(false));
                                CommentsFragment.this.mAdapter.notifyDataSetChanged();
                                CommentsFragment.this.isTouchHold = true;
                                return true;
                            }
                            break;
                        case 1:
                        case 3:
                            break;
                    }
                    if (CommentsFragment.this.isTouchHold) {
                        CommentsFragment.this.isTouchHold = false;
                        return true;
                    }
                    return false;
                }
            });
            this.mAdapter.notifyDataSetChanged();
            replyBack.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    AppDialogManager.getInstance().dismiss(CommentsFragment.this);
                }
            });
            return this.mDialogContentView;
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            if (this.mDisplay.isPad()) {
                return super.onCreateView(inflater, container, savedInstanceState);
            }
            return createView();
        }

        public void onDetach() {
            super.onDetach();
            if (this.mCheckedNodes != null) {
                this.mCheckedNodes.clear();
            }
            if (this.mAdapter != null) {
                this.mAdapter.clearNodes();
            }
            if (this.mSRDeleteDialog != null && this.mSRDeleteDialog.getDialog().isShowing()) {
                this.mSRDeleteDialog.dismiss();
            }
            this.mSRDeleteDialog = null;
            resetDeleteDialog();
        }

        private void beginToDelete() {
            if (checkDeleteStatus() == 1) {
                if (this.mSRDeleteDialog == null || this.mSRDeleteDialog.getDialog().getOwnerActivity() == null) {
                    this.mSRDeleteDialog = new UITextEditDialog(getActivity());
                    this.mSRDeleteDialog.getPromptTextView().setText(R.string.rv_panel_annot_delete_tips);
                    this.mSRDeleteDialog.setTitle(R.string.cloud_delete_tv);
                    this.mSRDeleteDialog.getInputEditText().setVisibility(8);
                }
                this.mSRDeleteDialog.getOKButton().setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        CommentsFragment.this.mSRDeleteDialog.dismiss();
                        CommentsFragment.this.mDeleteDialog = new ProgressDialog(CommentsFragment.this.getActivity());
                        CommentsFragment.this.mDeleteDialog.setProgressStyle(0);
                        CommentsFragment.this.mDeleteDialog.setCancelable(false);
                        CommentsFragment.this.mDeleteDialog.setIndeterminate(false);
                        CommentsFragment.this.mDeleteDialog.setMessage(CommentsFragment.this.getActivity().getString(R.string.rv_panel_annot_deleting));
                        CommentsFragment.this.mDeleteDialog.show();
                        CommentsFragment.this.deleteItems();
                    }
                });
                this.mSRDeleteDialog.show();
                return;
            }
            this.mDeleteDialog = new ProgressDialog(getActivity());
            this.mDeleteDialog.setProgressStyle(0);
            this.mDeleteDialog.setCancelable(false);
            this.mDeleteDialog.setIndeterminate(false);
            this.mDeleteDialog.setMessage(getActivity().getString(R.string.rv_panel_annot_deleting));
            this.mDeleteDialog.show();
            deleteItems();
        }

        private void resetDeleteDialog() {
            if (this.mDeleteDialog != null) {
                if (this.mDeleteDialog.isShowing()) {
                    AppDialogManager.getInstance().dismiss(this.mDeleteDialog);
                }
                this.mDeleteDialog = null;
            }
        }

        private void deleteItems() {
            int size = this.mCheckedNodes.size();
            if (size == 0) {
                resetDeleteDialog();
                notifyCounter();
                this.mAdapter.notifyDataSetChanged();
                return;
            }
            AnnotNode node = (AnnotNode) this.mCheckedNodes.get(size - 1);
            if (node == null) {
                this.mCheckedNodes.remove(node);
                deleteItems();
            } else if (node.isEditEnable()) {
                this.mAdapter.removeNode(node);
            } else {
                node.setChecked(false);
                this.mCheckedNodes.remove(node);
                notifyCounter();
                deleteItems();
            }
        }

        private int checkDeleteStatus() {
            return 0;
        }

        private void notifyCounter() {
            if (this.mAdapter.getCount() > 0) {
                this.mReplyClear.setVisibility(0);
            } else {
                this.mReplyClear.setEnabled(false);
            }
        }
    }

    static class ReplyContent implements NoteAnnotContent {
        String content;
        String nm;
        int pageIndex;
        String parentNM;

        public ReplyContent(int pageIndex, String nm, String content, String parentNM) {
            this.pageIndex = pageIndex;
            this.nm = nm;
            this.content = content;
            this.parentNM = parentNM;
        }

        public String getIcon() {
            return "";
        }

        public String getFromType() {
            return Module.MODULE_NAME_REPLY;
        }

        public String getParentNM() {
            return this.parentNM;
        }

        public int getPageIndex() {
            return this.pageIndex;
        }

        public int getType() {
            return 1;
        }

        public String getNM() {
            return this.nm;
        }

        public RectF getBBox() {
            return new RectF();
        }

        public int getColor() {
            return 0;
        }

        public int getOpacity() {
            return 0;
        }

        public float getLineWidth() {
            return 0.0f;
        }

        public String getSubject() {
            return null;
        }

        public DateTime getModifiedDate() {
            return AppDmUtil.currentDateToDocumentDate();
        }

        public String getContents() {
            return this.content;
        }

        public String getIntent() {
            return null;
        }
    }

    public static class ReplyDialog extends DialogFragment {
        private ReplyCallback mCallback;
        private Context mContext;
        private boolean mDialogEditable = false;
        private AppDisplay mDisplay;
        private EditText mEditText;
        private PDFViewCtrl mPDFViewerCtrl;
        private ViewGroup mParent;
        private int mTitleID;

        public void init(PDFViewCtrl pdfViewCtrl, ViewGroup parent, boolean dialogEditable, int titleId, ReplyCallback callback) {
            this.mPDFViewerCtrl = pdfViewCtrl;
            this.mParent = parent;
            this.mDialogEditable = dialogEditable;
            this.mTitleID = titleId;
            this.mCallback = callback;
        }

        public void onCreate(Bundle savedInstanceState) {
            this.mContext = getActivity();
            this.mDisplay = AppDisplay.getInstance(this.mContext);
            super.onCreate(savedInstanceState);
        }

        public void onAttach(Activity activity) {
            super.onAttach(activity);
            if (this.mCallback == null || activity == null || this.mTitleID == 0) {
                dismiss();
            } else {
                setCancelable(true);
            }
        }

        public void dismiss() {
            super.dismiss();
        }

        public void onDetach() {
            super.onDetach();
            if (this.mEditText != null) {
                AppUtil.dismissInputSoft(this.mEditText);
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (this.mCallback == null || getActivity() == null || this.mTitleID == 0) {
                return super.onCreateDialog(savedInstanceState);
            }
            Dialog dialog = new Dialog(getActivity(), R.style.rv_dialog_style);
            dialog.setContentView(createView());
            dialog.getWindow().setFlags(1024, 1024);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dlg_title_bg_4circle_corner_white);
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = this.mDisplay.getDialogWidth();
            dialog.getWindow().setAttributes(params);
            dialog.setCanceledOnTouchOutside(true);
            return dialog;
        }

        public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            if (this.mContext.getResources().getConfiguration().orientation == 2) {
                this.mEditText.setMaxLines(5);
            } else {
                this.mEditText.setMaxLines(10);
            }
            WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
            if (params.height > (this.mParent.getHeight() * 4) / 5) {
                params.height = (this.mParent.getHeight() * 4) / 5;
            }
            getDialog().getWindow().setAttributes(params);
        }

        public void onActivityCreated(Bundle arg0) {
            super.onActivityCreated(arg0);
        }

        @TargetApi(11)
        public View createView() {
            String content;
            View view = View.inflate(getActivity(), R.layout.rd_note_dialog_edit, null);
            ((TextView) view.findViewById(R.id.rd_note_dialog_edit_title)).setText(this.mTitleID);
            this.mEditText = (EditText) view.findViewById(R.id.rd_note_dialog_edit);
            Button bt_close = (Button) view.findViewById(R.id.rd_note_dialog_edit_cancel);
            Button bt_ok = (Button) view.findViewById(R.id.rd_note_dialog_edit_ok);
            if (this.mCallback.getContent() == null) {
                content = "";
            } else {
                content = this.mCallback.getContent();
            }
            this.mEditText.setText(content);
            if (this.mDialogEditable) {
                this.mEditText.setEnabled(true);
                bt_ok.setEnabled(true);
                bt_ok.setTextColor(getActivity().getApplicationContext().getResources().getColor(R.color.dlg_bt_text_selector));
                this.mEditText.setCursorVisible(true);
                this.mEditText.setFocusable(true);
                this.mEditText.setSelection(content.length());
                AppUtil.showSoftInput(this.mEditText);
            } else {
                bt_ok.setEnabled(false);
                bt_ok.setTextColor(getActivity().getApplicationContext().getResources().getColor(R.color.ux_bg_color_dialog_button_disabled));
                this.mEditText.setFocusable(false);
                this.mEditText.setLongClickable(false);
                if (VERSION.SDK_INT >= 11) {
                    this.mEditText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                            return false;
                        }

                        public void onDestroyActionMode(ActionMode mode) {
                        }

                        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                            return false;
                        }

                        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                            return false;
                        }
                    });
                } else {
                    this.mEditText.setEnabled(false);
                }
            }
            bt_close.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (DocumentManager.getInstance(ReplyDialog.this.mPDFViewerCtrl).canAddAnnot()) {
                        AppUtil.dismissInputSoft(ReplyDialog.this.mEditText);
                    }
                    AppDialogManager.getInstance().dismiss(ReplyDialog.this);
                }
            });
            bt_ok.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    AppUtil.dismissInputSoft(ReplyDialog.this.mEditText);
                    ReplyDialog.this.dismiss();
                    if (ReplyDialog.this.mCallback != null && ReplyDialog.this.mEditText.getText() != null) {
                        String Content = ReplyDialog.this.mEditText.getText().toString().trim();
                        if (!Content.equals(ReplyDialog.this.mCallback.getContent())) {
                            ReplyDialog.this.mCallback.result(Content);
                        }
                    }
                }
            });
            if (this.mContext.getResources().getConfiguration().orientation == 2) {
                this.mEditText.setMaxLines(5);
            } else {
                this.mEditText.setMaxLines(10);
            }
            RelativeLayout.LayoutParams editParams = (RelativeLayout.LayoutParams) this.mEditText.getLayoutParams();
            editParams.height = -2;
            this.mEditText.setLayoutParams(editParams);
            view.setLayoutParams(new LayoutParams(this.mDisplay.getDialogWidth(), -2));
            return view;
        }
    }

    public static void replyToAnnot(Context context, PDFViewCtrl pdfViewCtrl, ViewGroup parent, Annot annot) {
        if (annot != null) {
            FragmentActivity act = (FragmentActivity) context;
            ReplyDialog fragment = (ReplyDialog) act.getSupportFragmentManager().findFragmentByTag("ReplyDialog");
            if (fragment == null) {
                fragment = new ReplyDialog();
            }
            fragment.init(pdfViewCtrl, parent, true, TITLE_COMMENT_ID, new AnonymousClass1(annot, pdfViewCtrl, context, parent));
            AppDialogManager.getInstance().showAllowManager(fragment, act.getSupportFragmentManager(), "ReplyDialog", null);
        }
    }

    public static void replyToAnnot(Context context, PDFViewCtrl pdfViewCtrl, ViewGroup parent, boolean editable, int titleId, ReplyCallback callback) {
        if (callback != null) {
            FragmentActivity act = (FragmentActivity) context;
            ReplyDialog fragment = (ReplyDialog) act.getSupportFragmentManager().findFragmentByTag("ReplyDialog");
            if (fragment == null) {
                fragment = new ReplyDialog();
            }
            fragment.init(pdfViewCtrl, parent, editable, titleId, callback);
            AppDialogManager.getInstance().showAllowManager(fragment, act.getSupportFragmentManager(), "ReplyDialog", null);
        }
    }

    public static void showComments(Context context, PDFViewCtrl pdfViewCtrl, ViewGroup parent, Annot dmAnnot) {
        if (dmAnnot != null) {
            FragmentActivity act = (FragmentActivity) context;
            CommentsFragment fragment = (CommentsFragment) act.getSupportFragmentManager().findFragmentByTag("CommentsFragment");
            if (fragment == null) {
                fragment = new CommentsFragment();
            }
            fragment.init(pdfViewCtrl, parent, dmAnnot);
            AppDialogManager.getInstance().showAllowManager(fragment, act.getSupportFragmentManager(), "CommentsFragment", null);
        }
    }

    public static void addReplyAnnot(PDFViewCtrl pdfViewCtrl, Annot annot, PDFPage page, String nm, String content, Callback callback) {
        try {
            DocumentManager.getInstance(pdfViewCtrl).addAnnot(page, new ReplyContent(page.getIndex(), nm, content, annot.getUniqueID()), true, callback);
        } catch (PDFException e) {
            e.printStackTrace();
        }
    }
}
