package com.foxit.uiextensions.modules;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.Task;
import com.foxit.sdk.Task.CallBack;
import com.foxit.sdk.common.DateTime;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.ReadingBookmark;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.dialog.UITextEditDialog;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class ReadingBookmarkSupport {
    private final ReadingBookmarkAdapter mAdapter;
    private boolean mNeedRelayout = false;
    private final ReadingBookmarkModule mReadingBookmarkModule;

    interface IReadingBookmarkCallback {
        void result();
    }

    interface IReadingBookmarkListener {
        void onDelete(int i);

        void onMoreClick(int i);

        void onRename(int i);
    }

    public class ReadingBookmarkAdapter extends BaseAdapter {
        private final IReadingBookmarkListener mBookmarkListener;
        private final Context mContext;
        private final ArrayList<Boolean> mItemMoreViewShow;
        private final ArrayList<ReadingBookmarkNode> mNodeList = new ArrayList();
        private PDFDoc mPdfDoc;
        private final PDFViewCtrl mPdfViewCtrl;

        class RBViewHolder {
            public LinearLayout mLlDelete;
            public LinearLayout mLlRename;
            public TextView mRMContent;
            public TextView mRMCreateTime;
            public ImageView mRMDelete;
            public ImageView mRMMore;
            public LinearLayout mRMMoreView;
            public ImageView mRMRename;
            public TextView mRMTvDelete;
            public TextView mRMTvRename;

            RBViewHolder() {
            }
        }

        class RemoveReadingBookmarkTask extends Task {

            /* renamed from: com.foxit.uiextensions.modules.ReadingBookmarkSupport$ReadingBookmarkAdapter$RemoveReadingBookmarkTask$1 */
            class AnonymousClass1 implements CallBack {
                private final /* synthetic */ IReadingBookmarkCallback val$callback;
                private final /* synthetic */ ReadingBookmarkAdapter val$this$1;

                AnonymousClass1(ReadingBookmarkAdapter readingBookmarkAdapter, IReadingBookmarkCallback iReadingBookmarkCallback) {
                    this.val$this$1 = readingBookmarkAdapter;
                    this.val$callback = iReadingBookmarkCallback;
                }

                public void result(Task task) {
                    this.val$callback.result();
                }
            }

            public RemoveReadingBookmarkTask(IReadingBookmarkCallback callback) {
                super(new AnonymousClass1(ReadingBookmarkAdapter.this, callback));
            }

            protected void execute() {
                try {
                    ArrayList<ReadingBookmark> mTmpReadingBookmark = new ArrayList();
                    for (int i = 0; i < ReadingBookmarkAdapter.this.mPdfDoc.getReadingBookmarkCount(); i++) {
                        mTmpReadingBookmark.add(ReadingBookmarkAdapter.this.mPdfDoc.getReadingBookmark(i));
                    }
                    Iterator it = mTmpReadingBookmark.iterator();
                    while (it.hasNext()) {
                        ReadingBookmarkAdapter.this.mPdfDoc.removeReadingBookmark((ReadingBookmark) it.next());
                        updateLayout();
                    }
                    mTmpReadingBookmark.clear();
                    ReadingBookmarkAdapter.this.mNodeList.clear();
                } catch (PDFException e) {
                    e.printStackTrace();
                }
            }

            private void updateLayout() {
                ((Activity) ReadingBookmarkAdapter.this.mContext).runOnUiThread(new Runnable() {
                    public void run() {
                        ReadingBookmarkAdapter.this.notifyDataSetChanged();
                    }
                });
            }
        }

        public ReadingBookmarkAdapter() {
            this.mContext = ReadingBookmarkSupport.this.mReadingBookmarkModule.mContentView.getContext();
            this.mPdfViewCtrl = ReadingBookmarkSupport.this.mReadingBookmarkModule.mPdfViewCtrl;
            this.mItemMoreViewShow = ReadingBookmarkSupport.this.mReadingBookmarkModule.mItemMoreViewShow;
            this.mBookmarkListener = new IReadingBookmarkListener() {
                public void onMoreClick(int position) {
                    for (int i = 0; i < ReadingBookmarkAdapter.this.mItemMoreViewShow.size(); i++) {
                        if (i == position) {
                            ReadingBookmarkAdapter.this.mItemMoreViewShow.set(i, Boolean.valueOf(true));
                        } else {
                            ReadingBookmarkAdapter.this.mItemMoreViewShow.set(i, Boolean.valueOf(false));
                        }
                    }
                    ReadingBookmarkAdapter.this.notifyDataSetChanged();
                }

                public void onRename(int position) {
                    if (!AppUtil.isFastDoubleClick()) {
                        final UITextEditDialog renameDlg = new UITextEditDialog(ReadingBookmarkAdapter.this.mContext);
                        renameDlg.getPromptTextView().setVisibility(8);
                        renameDlg.setTitle(ReadingBookmarkAdapter.this.mContext.getResources().getString(R.string.fx_string_rename));
                        renameDlg.getDialog().setCanceledOnTouchOutside(false);
                        final EditText renameDlgEt = renameDlg.getInputEditText();
                        final Button renameDlgOk = renameDlg.getOKButton();
                        Button renameDlgCancel = renameDlg.getCancelButton();
                        renameDlgEt.setTextSize(17.3f);
                        renameDlgEt.setFilters(new InputFilter[]{new LengthFilter(200)});
                        renameDlgEt.setTextColor(-16777216);
                        final InputMethodManager mInputManager = (InputMethodManager) renameDlgEt.getContext().getSystemService("input_method");
                        renameDlgEt.setText(((ReadingBookmarkNode) ReadingBookmarkAdapter.this.mNodeList.get(position)).getTitle());
                        renameDlgEt.selectAll();
                        renameDlgOk.setEnabled(false);
                        renameDlgOk.setTextColor(ReadingBookmarkAdapter.this.mContext.getResources().getColor(R.color.ux_bg_color_dialog_button_disabled));
                        renameDlgEt.addTextChangedListener(new TextWatcher() {
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                if (renameDlgEt.getText().toString().trim().length() > 199) {
                                    final Toast toast = Toast.makeText(ReadingBookmarkAdapter.this.mContext, R.string.rv_panel_readingbookmark_tips_limited, 1);
                                    toast.setGravity(17, 0, 0);
                                    final Timer timer = new Timer();
                                    timer.schedule(new TimerTask() {
                                        public void run() {
                                            toast.show();
                                        }
                                    }, 0, 3000);
                                    new Timer().schedule(new TimerTask() {
                                        public void run() {
                                            toast.cancel();
                                            timer.cancel();
                                        }
                                    }, 5000);
                                } else if (renameDlgEt.getText().toString().trim().length() == 0) {
                                    renameDlgOk.setEnabled(false);
                                    renameDlgOk.setTextColor(ReadingBookmarkAdapter.this.mContext.getResources().getColor(R.color.ux_bg_color_dialog_button_disabled));
                                } else {
                                    renameDlgOk.setEnabled(true);
                                    renameDlgOk.setTextColor(ReadingBookmarkAdapter.this.mContext.getResources().getColor(R.color.dlg_bt_text_selector));
                                }
                            }

                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            public void afterTextChanged(Editable s) {
                                for (int i = s.length(); i > 0; i--) {
                                    if (s.subSequence(i - 1, i).toString().equals("\n")) {
                                        s.replace(i - 1, i, "");
                                    }
                                }
                            }
                        });
                        renameDlgEt.setOnKeyListener(new OnKeyListener() {
                            public boolean onKey(View v, int keyCode, KeyEvent event) {
                                if (66 != keyCode || event.getAction() != 0) {
                                    return false;
                                }
                                mInputManager.hideSoftInputFromWindow(renameDlgEt.getWindowToken(), 0);
                                return true;
                            }
                        });
                        final int i = position;
                        renameDlgOk.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                ReadingBookmarkAdapter.this.updateBookmarkNode(i, renameDlgEt.getText().toString().trim(), AppDmUtil.currentDateToDocumentDate());
                                ReadingBookmarkAdapter.this.notifyDataSetChanged();
                                mInputManager.hideSoftInputFromWindow(renameDlgEt.getWindowToken(), 0);
                                renameDlg.dismiss();
                            }
                        });
                        renameDlgCancel.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                mInputManager.hideSoftInputFromWindow(renameDlgEt.getWindowToken(), 0);
                                renameDlg.dismiss();
                            }
                        });
                        renameDlg.show();
                        renameDlgEt.setFocusable(true);
                        renameDlgEt.setFocusableInTouchMode(true);
                        renameDlgEt.requestFocus();
                        new Timer().schedule(new TimerTask() {
                            public void run() {
                                mInputManager.showSoftInput(renameDlgEt, 0);
                            }
                        }, 500);
                    }
                }

                public void onDelete(int position) {
                    boolean z = false;
                    ReadingBookmarkNode node = (ReadingBookmarkNode) ReadingBookmarkAdapter.this.mNodeList.get(position);
                    ReadingBookmarkSupport.this.mAdapter.removeBookmarkNode(node.getIndex());
                    if (node.getIndex() == ReadingBookmarkAdapter.this.mPdfViewCtrl.getCurrentPage()) {
                        ReadingBookmarkSupport.this.mReadingBookmarkModule.changeMarkItemState(false);
                    }
                    ReadingBookmarkModule access$0 = ReadingBookmarkSupport.this.mReadingBookmarkModule;
                    if (ReadingBookmarkAdapter.this.mNodeList.size() != 0) {
                        z = true;
                    }
                    access$0.changeViewState(z);
                }
            };
        }

        public boolean isMarked(int index) {
            Iterator it = this.mNodeList.iterator();
            while (it.hasNext()) {
                if (((ReadingBookmarkNode) it.next()).getIndex() == index) {
                    return true;
                }
            }
            return false;
        }

        public void initBookmarkList() {
            try {
                this.mPdfDoc = this.mPdfViewCtrl.getDoc();
                if (this.mPdfDoc != null) {
                    this.mNodeList.clear();
                    int nCount = this.mPdfDoc.getReadingBookmarkCount();
                    for (int i = 0; i < nCount; i++) {
                        ReadingBookmark readingBookmark = this.mPdfDoc.getReadingBookmark(i);
                        if (readingBookmark != null) {
                            this.mNodeList.add(new ReadingBookmarkNode(readingBookmark));
                            this.mItemMoreViewShow.add(Boolean.valueOf(false));
                        }
                    }
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }

        public void addBookmarkNode(int pageIndex, String title) {
            try {
                ReadingBookmark readingBookmark = this.mPdfDoc.insertReadingBookmark(0, title, pageIndex);
                DateTime dateTime = AppDmUtil.currentDateToDocumentDate();
                readingBookmark.setDateTime(dateTime, true);
                readingBookmark.setDateTime(dateTime, false);
                this.mNodeList.add(0, new ReadingBookmarkNode(readingBookmark));
            } catch (PDFException e) {
                e.printStackTrace();
            }
            this.mItemMoreViewShow.add(0, Boolean.valueOf(false));
            notifyDataSetChanged();
        }

        public void removeBookmarkNode(int pageIndex) {
            for (int position = 0; position < this.mNodeList.size(); position++) {
                if (((ReadingBookmarkNode) this.mNodeList.get(position)).getIndex() == pageIndex) {
                    this.mNodeList.remove(position);
                    this.mItemMoreViewShow.remove(position);
                    break;
                }
            }
            try {
                int nCount = this.mPdfDoc.getReadingBookmarkCount();
                for (int i = 0; i < nCount; i++) {
                    ReadingBookmark readingMark = this.mPdfDoc.getReadingBookmark(i);
                    if (readingMark.getPageIndex() == pageIndex) {
                        this.mPdfDoc.removeReadingBookmark(readingMark);
                        break;
                    }
                }
            } catch (PDFException e) {
                e.printStackTrace();
            }
            notifyDataSetChanged();
        }

        public void updateBookmarkNode(int position, String title, DateTime dateTime) {
            ((ReadingBookmarkNode) this.mNodeList.get(position)).setTitle(title);
            ((ReadingBookmarkNode) this.mNodeList.get(position)).setModifiedDateTime(dateTime);
        }

        public void clearAllNodes() {
            final ProgressDialog progressDialog = new ProgressDialog(this.mContext);
            progressDialog.setMessage("deleting");
            progressDialog.setCancelable(false);
            progressDialog.show();
            this.mPdfViewCtrl.addTask(new RemoveReadingBookmarkTask(new IReadingBookmarkCallback() {
                public void result() {
                    progressDialog.dismiss();
                    ReadingBookmarkAdapter.this.notifyDataSetChanged();
                }
            }));
        }

        public void onPageRemoved(boolean success, int index) {
            if (success) {
                ReadingBookmarkNode node;
                ArrayList<ReadingBookmarkNode> invalidList = new ArrayList();
                Iterator it = this.mNodeList.iterator();
                while (it.hasNext()) {
                    node = (ReadingBookmarkNode) it.next();
                    if (node.getIndex() == index) {
                        invalidList.add(node);
                    } else if (node.getIndex() > index) {
                        node.setIndex(node.getIndex() - 1);
                    }
                }
                it = invalidList.iterator();
                while (it.hasNext()) {
                    node = (ReadingBookmarkNode) it.next();
                    this.mNodeList.remove(node);
                    try {
                        this.mPdfViewCtrl.getDoc().removeReadingBookmark(node.mBookrmak);
                    } catch (PDFException e) {
                        e.printStackTrace();
                    }
                }
                invalidList.clear();
                ReadingBookmarkSupport.this.mNeedRelayout = true;
            }
        }

        public void onPageMoved(boolean success, int index, int dstIndex) {
            if (success) {
                for (int i = 0; i < this.mNodeList.size(); i++) {
                    ReadingBookmarkNode node = (ReadingBookmarkNode) this.mNodeList.get(i);
                    if (index < dstIndex) {
                        if (node.getIndex() <= dstIndex && node.getIndex() > index) {
                            node.setIndex(node.getIndex() - 1);
                        } else if (node.getIndex() == index) {
                            node.setIndex(dstIndex);
                        }
                    } else if (node.getIndex() >= dstIndex && node.getIndex() < index) {
                        node.setIndex(node.getIndex() + 1);
                    } else if (node.getIndex() == index) {
                        node.setIndex(dstIndex);
                    }
                }
            }
        }

        protected void onPagesInsert(boolean success, int dstIndex, int[] range) {
            if (success) {
                int offsetIndex = 0;
                for (int i = 0; i < range.length / 2; i++) {
                    offsetIndex += range[(i * 2) + 1];
                }
                updateReadingBookmarkItems(dstIndex, offsetIndex);
            }
        }

        private void updateReadingBookmarkItems(int dstIndex, int offsetIndex) {
            for (int i = 0; i < this.mNodeList.size(); i++) {
                ReadingBookmarkNode node = (ReadingBookmarkNode) this.mNodeList.get(i);
                if (node.getIndex() >= dstIndex) {
                    node.setIndex(node.getIndex() + offsetIndex);
                }
            }
        }

        public int getCount() {
            return this.mNodeList.size();
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            RBViewHolder bkHolder;
            if (convertView == null) {
                bkHolder = new RBViewHolder();
                convertView = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.rd_readingmark_item, null);
                bkHolder.mRMContent = (TextView) convertView.findViewById(R.id.rd_bookmark_item_content);
                bkHolder.mRMCreateTime = (TextView) convertView.findViewById(R.id.rd_bookmark_item_date);
                bkHolder.mRMMore = (ImageView) convertView.findViewById(R.id.rd_panel_item_more);
                bkHolder.mRMMoreView = (LinearLayout) convertView.findViewById(R.id.rd_bookmark_item_moreView);
                bkHolder.mLlRename = (LinearLayout) convertView.findViewById(R.id.rd_bookmark_item_ll_rename);
                bkHolder.mRMRename = (ImageView) convertView.findViewById(R.id.rd_bookmark_item_rename);
                bkHolder.mRMTvRename = (TextView) convertView.findViewById(R.id.rd_bookmark_item_tv_rename);
                bkHolder.mLlDelete = (LinearLayout) convertView.findViewById(R.id.rd_bookmark_item_ll_delete);
                bkHolder.mRMDelete = (ImageView) convertView.findViewById(R.id.rd_bookmark_item_delete);
                bkHolder.mRMTvDelete = (TextView) convertView.findViewById(R.id.rd_bookmark_item_tv_delete);
                if (AppDisplay.getInstance(this.mContext).isPad()) {
                    convertView.setLayoutParams(new LayoutParams(-1, (int) this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_2l_pad)));
                    convertView.setPadding((int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_pad), 0, 0, 0);
                    bkHolder.mRMMore.setPadding(bkHolder.mRMMore.getPaddingLeft(), bkHolder.mRMMore.getPaddingTop(), (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_right_margin_pad), bkHolder.mRMMore.getPaddingBottom());
                } else {
                    convertView.setLayoutParams(new LayoutParams(-1, (int) this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_2l_phone)));
                    convertView.setPadding((int) this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_phone), 0, 0, 0);
                    bkHolder.mRMMore.setPadding(bkHolder.mRMMore.getPaddingLeft(), bkHolder.mRMMore.getPaddingTop(), (int) this.mContext.getResources().getDimension(R.dimen.ux_horz_right_margin_phone), bkHolder.mRMMore.getPaddingBottom());
                }
                convertView.setTag(bkHolder);
            } else {
                bkHolder = (RBViewHolder) convertView.getTag();
            }
            ReadingBookmarkNode node = (ReadingBookmarkNode) getItem(position);
            bkHolder.mRMContent.setText(node.getTitle());
            String time = AppDmUtil.dateOriValue;
            if (node.getModifiedDateTime() != null) {
                time = AppDmUtil.getLocalDateString(node.getModifiedDateTime());
            }
            bkHolder.mRMCreateTime.setText(time);
            if (((Boolean) this.mItemMoreViewShow.get(position)).booleanValue()) {
                bkHolder.mRMMoreView.setVisibility(0);
            } else {
                bkHolder.mRMMoreView.setVisibility(8);
            }
            bkHolder.mRMMore.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ReadingBookmarkAdapter.this.mBookmarkListener.onMoreClick(position);
                }
            });
            OnClickListener renameListener = new OnClickListener() {
                public void onClick(View v) {
                    ((LinearLayout) v.getParent().getParent()).setVisibility(8);
                    ReadingBookmarkAdapter.this.mItemMoreViewShow.set(position, Boolean.valueOf(false));
                    ReadingBookmarkAdapter.this.mBookmarkListener.onRename(position);
                }
            };
            bkHolder.mRMRename.setOnClickListener(renameListener);
            bkHolder.mRMTvRename.setOnClickListener(renameListener);
            OnClickListener deleteListener = new OnClickListener() {
                public void onClick(View v) {
                    ((LinearLayout) v.getParent().getParent()).setVisibility(8);
                    ReadingBookmarkAdapter.this.mItemMoreViewShow.set(position, Boolean.valueOf(false));
                    ReadingBookmarkAdapter.this.mBookmarkListener.onDelete(position);
                }
            };
            bkHolder.mRMDelete.setOnClickListener(deleteListener);
            bkHolder.mRMTvDelete.setOnClickListener(deleteListener);
            OnTouchListener listener = new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == 0) {
                        v.setPressed(true);
                        return true;
                    }
                    if (event.getAction() == 1) {
                        v.setPressed(false);
                    }
                    return false;
                }
            };
            bkHolder.mLlRename.setOnTouchListener(listener);
            bkHolder.mLlDelete.setOnTouchListener(listener);
            RelativeLayout.LayoutParams paramsMoreView = (RelativeLayout.LayoutParams) bkHolder.mRMMoreView.getLayoutParams();
            paramsMoreView.height = -1;
            bkHolder.mRMMoreView.setLayoutParams(paramsMoreView);
            return convertView;
        }

        public Object getItem(int position) {
            return this.mNodeList.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }
    }

    public class ReadingBookmarkNode {
        private ReadingBookmark mBookrmak;
        private int mIndex;
        private DateTime mModifiedDateTime;
        private String mTitle;

        public ReadingBookmarkNode(ReadingBookmark bookmark) {
            this.mBookrmak = bookmark;
            try {
                this.mTitle = this.mBookrmak.getTitle();
                this.mIndex = this.mBookrmak.getPageIndex();
                this.mModifiedDateTime = this.mBookrmak.getDateTime(false) == null ? this.mBookrmak.getDateTime(true) : this.mBookrmak.getDateTime(false);
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }

        public void setTitle(String title) {
            this.mTitle = title;
            try {
                this.mBookrmak.setTitle(title);
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }

        public void setModifiedDateTime(DateTime dateTime) {
            this.mModifiedDateTime = dateTime;
            try {
                this.mBookrmak.setDateTime(this.mModifiedDateTime, false);
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }

        public DateTime getModifiedDateTime() {
            return this.mModifiedDateTime;
        }

        public String getTitle() {
            return this.mTitle;
        }

        public int getIndex() {
            return this.mIndex;
        }

        public void setIndex(int index) {
            this.mIndex = index;
            try {
                this.mBookrmak.setPageIndex(index);
            } catch (PDFException e) {
                e.printStackTrace();
            }
        }
    }

    public ReadingBookmarkSupport(ReadingBookmarkModule panelModule) {
        this.mReadingBookmarkModule = panelModule;
        this.mAdapter = new ReadingBookmarkAdapter();
        this.mAdapter.initBookmarkList();
    }

    public ReadingBookmarkAdapter getAdapter() {
        return this.mAdapter;
    }

    public void clearAllNodes() {
        this.mAdapter.clearAllNodes();
    }

    public void addReadingBookmarkNode(int index, String title) {
        this.mAdapter.addBookmarkNode(index, title);
    }

    public boolean needRelayout() {
        return this.mNeedRelayout;
    }

    public void removeReadingBookmarkNode(int index) {
        this.mAdapter.removeBookmarkNode(index);
    }
}
