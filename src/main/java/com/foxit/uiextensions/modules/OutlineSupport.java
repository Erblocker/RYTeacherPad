package com.foxit.uiextensions.modules;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.Task;
import com.foxit.sdk.Task.CallBack;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.pdf.Bookmark;
import com.foxit.sdk.pdf.action.Destination;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.modules.OutlineModule.OutlineItem;
import com.foxit.uiextensions.utils.AppDisplay;
import java.util.ArrayList;

public abstract class OutlineSupport {
    public static final int STATE_LOADING = 1;
    public static final int STATE_LOAD_FINISH = 2;
    public static final int STATE_NORMAL = 0;
    private static final int UPDATEUI = 100;
    private OutlineAdapter mAdapter;
    private ImageView mBack;
    private Context mContext;
    private int mCurrentState = 0;
    private AppDisplay mDisplay;
    private MyHandler mHandler;
    private int mLevel = 0;
    private OutlineItem mOutlineItem = new OutlineItem();
    private ArrayList<OutlineItem> mOutlineList = new ArrayList();
    private PDFViewCtrl mPDFViewCtrl;
    private PopupWindow mPanelPopupWindow;
    private ArrayList<OutlineItem> mParents = new ArrayList();
    private int mPosition = -1;
    private ArrayList<OutlineItem> mShowOutlineList = new ArrayList();

    interface ITaskResult<T1, T2, T3> {
        void onResult(boolean z, T1 t1, T2 t2, T3 t3);
    }

    private class MyHandler extends Handler {
        private MyHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    OutlineSupport.this.updateUI(OutlineSupport.this.mLevel, msg.arg1);
                    OutlineSupport.this.mAdapter.notifyDataSetChanged();
                    return;
                default:
                    return;
            }
        }
    }

    class Outline {
        ImageView ivMore = null;
        LinearLayout layout;
        LinearLayout sd_outline_layout_ll;
        TextView tvChapter = null;

        Outline() {
        }
    }

    private class OutlineAdapter extends BaseAdapter {
        private OutlineAdapter() {
        }

        public int getCount() {
            return OutlineSupport.this.mShowOutlineList != null ? OutlineSupport.this.mShowOutlineList.size() : 0;
        }

        public Object getItem(int position) {
            return OutlineSupport.this.mShowOutlineList.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            Outline outline;
            if (convertView == null) {
                convertView = LayoutInflater.from(OutlineSupport.this.mContext).inflate(R.layout.rv_panel_outline_item, null, false);
                outline = new Outline();
                outline.sd_outline_layout_ll = (LinearLayout) convertView.findViewById(R.id.sd_outline_layout_ll);
                outline.tvChapter = (TextView) convertView.findViewById(R.id.sd_outline_chapter);
                outline.ivMore = (ImageView) convertView.findViewById(R.id.sd_outline_more);
                outline.layout = (LinearLayout) convertView.findViewById(R.id.sd_outline_layout_more);
                LayoutParams layoutParams = (LayoutParams) outline.sd_outline_layout_ll.getLayoutParams();
                if (OutlineSupport.this.mDisplay.isPad()) {
                    convertView.setLayoutParams(new AbsListView.LayoutParams(-1, (int) OutlineSupport.this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_1l_pad)));
                    convertView.setPadding((int) OutlineSupport.this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_pad), 0, 0, 0);
                    layoutParams.height = (int) OutlineSupport.this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_1l_pad);
                    outline.ivMore.setPadding(outline.ivMore.getPaddingLeft(), outline.ivMore.getPaddingTop(), (int) OutlineSupport.this.mContext.getResources().getDimension(R.dimen.ux_horz_right_margin_pad), outline.ivMore.getPaddingBottom());
                } else {
                    convertView.setLayoutParams(new AbsListView.LayoutParams(-1, (int) OutlineSupport.this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_1l_phone)));
                    convertView.setPadding((int) OutlineSupport.this.mContext.getResources().getDimension(R.dimen.ux_horz_left_margin_phone), 0, 0, 0);
                    layoutParams.height = (int) OutlineSupport.this.mContext.getResources().getDimension(R.dimen.ux_list_item_height_1l_phone);
                    outline.ivMore.setPadding(outline.ivMore.getPaddingLeft(), outline.ivMore.getPaddingTop(), (int) OutlineSupport.this.mContext.getResources().getDimension(R.dimen.ux_horz_right_margin_phone), outline.ivMore.getPaddingBottom());
                }
                outline.sd_outline_layout_ll.setLayoutParams(layoutParams);
                convertView.setTag(outline);
            } else {
                outline = (Outline) convertView.getTag();
            }
            outline.ivMore.setVisibility(((OutlineItem) OutlineSupport.this.mShowOutlineList.get(position)).mHaveChild ? 0 : 4);
            outline.tvChapter.setText(((OutlineItem) OutlineSupport.this.mShowOutlineList.get(position)).mTitle);
            outline.ivMore.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    OutlineItem currentNode = (OutlineItem) OutlineSupport.this.mShowOutlineList.get(position);
                    OutlineSupport.this.mLevel = currentNode.mLevel + 1;
                    OutlineSupport.this.mPosition = OutlineSupport.this.mOutlineList.indexOf(currentNode);
                    ((OutlineItem) OutlineSupport.this.mOutlineList.get(OutlineSupport.this.mPosition)).mIsExpanded = !((OutlineItem) OutlineSupport.this.mOutlineList.get(OutlineSupport.this.mPosition)).mIsExpanded;
                    OutlineSupport.this.mShowOutlineList.clear();
                    OutlineSupport.this.mCurrentState = 1;
                    OutlineSupport.this.getOutList(currentNode, OutlineSupport.this.mPosition);
                }
            });
            convertView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    OutlineSupport.this.mPDFViewCtrl.gotoPage(((OutlineItem) OutlineSupport.this.mShowOutlineList.get(position)).mPageIndex, ((OutlineItem) OutlineSupport.this.mShowOutlineList.get(position)).mX, ((OutlineItem) OutlineSupport.this.mShowOutlineList.get(position)).mY);
                    if (OutlineSupport.this.mPanelPopupWindow.isShowing()) {
                        OutlineSupport.this.mPanelPopupWindow.dismiss();
                    }
                }
            });
            return convertView;
        }
    }

    class OutlineTask extends Task {
        boolean bSuccess;
        Bookmark mBookmark;
        int mIdx;
        int mLevel;
        OutlineItem mOutlineItem;

        public OutlineTask(Bookmark bookmark, int idx, int level, CallBack callBack) {
            super(callBack);
            this.mBookmark = bookmark;
            this.mIdx = idx;
            this.mLevel = level;
        }

        protected void execute() {
            try {
                this.mOutlineItem = new OutlineItem();
                Bookmark current = this.mBookmark.getFirstChild();
                while (current != null) {
                    OutlineItem childItem = new OutlineItem();
                    childItem.mHaveChild = current.hasChild();
                    childItem.mParentPos = this.mIdx;
                    childItem.mTitle = current.getTitle();
                    childItem.mBookmark = current;
                    childItem.mLevel = this.mLevel;
                    Destination dest = current.getDestination();
                    if (dest != null) {
                        childItem.mPageIndex = dest.getPageIndex();
                    }
                    current = current.getNextSibling();
                    this.mOutlineItem.mChildren.add(childItem);
                }
                this.mOutlineItem.mLevel = this.mLevel - 1;
                this.bSuccess = true;
            } catch (PDFException e) {
                if (e.getLastError() == PDFError.OOM.getCode()) {
                    OutlineSupport.this.mPDFViewCtrl.recoverForOOM();
                } else {
                    this.bSuccess = false;
                }
            }
        }
    }

    public abstract void OutlineBindingListView(BaseAdapter baseAdapter);

    public abstract void getShowOutline(ArrayList<OutlineItem> arrayList);

    public abstract void updateUI(int i, int i2);

    public int getCurrentState() {
        return this.mCurrentState;
    }

    public OutlineSupport(Context context, PDFViewCtrl pdfViewCtrl, AppDisplay display, PopupWindow popup, ImageView back) {
        this.mContext = context;
        this.mDisplay = display;
        this.mPanelPopupWindow = popup;
        this.mPDFViewCtrl = pdfViewCtrl;
        this.mBack = back;
        this.mHandler = new MyHandler();
        this.mAdapter = new OutlineAdapter();
        this.mCurrentState = 1;
        updateUI(this.mLevel, this.mCurrentState);
        OutlineBindingListView(this.mAdapter);
        this.mBack.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                OutlineSupport outlineSupport = OutlineSupport.this;
                outlineSupport.mLevel = outlineSupport.mLevel - 1;
                OutlineSupport.this.mShowOutlineList.clear();
                OutlineSupport.this.mShowOutlineList.addAll(((OutlineItem) OutlineSupport.this.mParents.get(OutlineSupport.this.mPosition)).mChildren);
                OutlineSupport.this.getShowOutline(OutlineSupport.this.mShowOutlineList);
                OutlineSupport.this.updateUI(OutlineSupport.this.mLevel, 0);
                OutlineSupport.this.mAdapter.notifyDataSetChanged();
                OutlineSupport.this.mPosition = ((OutlineItem) OutlineSupport.this.mShowOutlineList.get(0)).mParentPos;
            }
        });
    }

    public void init() {
        try {
            this.mOutlineItem.mBookmark = this.mPDFViewCtrl.getDoc().getFirstBookmark();
        } catch (PDFException e) {
            e.printStackTrace();
        }
        if (this.mOutlineItem.mBookmark != null) {
            init(this.mOutlineItem.mBookmark, 0, 0);
        }
    }

    private void getOutlineInfo(Bookmark bookmark, int idx, int level, final ITaskResult<OutlineItem, Integer, Integer> result) {
        this.mPDFViewCtrl.addTask(new OutlineTask(bookmark, idx, level, new CallBack() {
            public void result(Task task) {
                OutlineTask task1 = (OutlineTask) task;
                if (result != null) {
                    result.onResult(task1.bSuccess, task1.mOutlineItem, Integer.valueOf(task1.mIdx), Integer.valueOf(task1.mLevel));
                }
            }
        }));
    }

    private void init(Bookmark bookmark, int idx, int level) {
        if (bookmark != null) {
            getOutlineInfo(bookmark, idx, level, new ITaskResult<OutlineItem, Integer, Integer>() {
                public void onResult(boolean success, OutlineItem outlineItem, Integer idx, Integer level) {
                    if (!success) {
                        return;
                    }
                    int i;
                    Message msg;
                    if (OutlineSupport.this.mOutlineList.size() == 0) {
                        OutlineSupport.this.mOutlineList.addAll(outlineItem.mChildren);
                        for (i = 0; i < outlineItem.mChildren.size(); i++) {
                            OutlineSupport.this.mParents.add(outlineItem);
                        }
                        OutlineSupport.this.mShowOutlineList.clear();
                        OutlineSupport.this.mShowOutlineList.addAll(OutlineSupport.this.mOutlineList);
                        OutlineSupport.this.mCurrentState = 2;
                        OutlineSupport.this.getShowOutline(OutlineSupport.this.mShowOutlineList);
                        if (OutlineSupport.this.mAdapter != null) {
                            msg = new Message();
                            msg.arg1 = OutlineSupport.this.mCurrentState;
                            msg.what = 100;
                            OutlineSupport.this.mHandler.sendMessage(msg);
                        }
                    } else if (idx.intValue() >= 0) {
                        OutlineSupport.this.mOutlineList.addAll(idx.intValue() + 1, outlineItem.mChildren);
                        for (i = 0; i < outlineItem.mChildren.size(); i++) {
                            OutlineSupport.this.mParents.add((idx.intValue() + 1) + i, outlineItem);
                        }
                        OutlineSupport.this.mShowOutlineList.addAll(outlineItem.mChildren);
                        OutlineSupport.this.mCurrentState = 0;
                        OutlineSupport.this.getShowOutline(OutlineSupport.this.mShowOutlineList);
                        if (OutlineSupport.this.mAdapter != null) {
                            msg = new Message();
                            msg.arg1 = OutlineSupport.this.mCurrentState;
                            msg.what = 100;
                            OutlineSupport.this.mHandler.sendMessage(msg);
                        }
                    }
                }
            });
        }
    }

    private void getOutList(OutlineItem outlineItem, int pos) {
        init(outlineItem.mBookmark, pos, this.mLevel);
    }
}
