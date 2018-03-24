package com.foxit.uiextensions.modules;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.PDFError;
import com.foxit.sdk.common.PDFException;
import com.foxit.sdk.common.Pause;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.sdk.pdf.PDFTextSearch;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.utils.AppDisplay;
import com.foxit.uiextensions.utils.AppUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SearchView {
    private static long sLastTimeMillis;
    private boolean bCancelSearchText = true;
    private SearchAdapter mAdapterSearch;
    private AppDisplay mAppDisplay;
    private ImageView mBottom_iv_next;
    private ImageView mBottom_iv_prev;
    private ImageView mBottom_iv_result;
    public LinearLayout mBottom_ll_shadow;
    public ListView mCenter_lv_result_list;
    private TextView mCenter_tv_total_number;
    private Context mContext = null;
    private float mCurrentPageX;
    private float mCurrentPageY;
    private int mCurrentPosition = -1;
    private float mCurrentSearchB;
    private float mCurrentSearchR;
    private LayoutInflater mInflater;
    private boolean mIsBlank = true;
    protected boolean mIsCancel = true;
    private DisplayMetrics mMetrics;
    OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (!SearchView.isFastDoubleClick()) {
                SearchView.this.OnPreItemClick();
                RectF rectF;
                RectF canvasRectF;
                boolean transSuccess;
                int screenWidth;
                int screenHeight;
                if (SearchView.this.mTagResultList.contains(SearchView.this.mShowResultList.get(position))) {
                    SearchView.this.mCurrentPosition = position + 1;
                    SearchView.this.setCurrentPageX();
                    rectF = new RectF(SearchView.this.mCurrentPageX, SearchView.this.mCurrentPageY, SearchView.this.mCurrentSearchR, SearchView.this.mCurrentSearchB);
                    canvasRectF = new RectF();
                    transSuccess = SearchView.this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, canvasRectF, ((SearchResult) SearchView.this.mShowResultList.get(SearchView.this.mCurrentPosition)).mPageIndex);
                    screenWidth = SearchView.this.getVisibleWidth().width();
                    screenHeight = SearchView.this.getVisibleWidth().height();
                    if (!transSuccess || canvasRectF.left < 0.0f || canvasRectF.right > ((float) screenWidth) || canvasRectF.top < 0.0f || canvasRectF.bottom > ((float) screenHeight)) {
                        SearchView.this.mPdfViewCtrl.gotoPage(((SearchResult) SearchView.this.mShowResultList.get(SearchView.this.mCurrentPosition)).mPageIndex, (float) ((int) (SearchView.this.mCurrentPageX - ((float) (SearchView.this.getScreenWidth() / 4)))), (float) ((int) (SearchView.this.mCurrentPageY - ((float) (SearchView.this.getScreenHeight() / 4)))));
                    }
                    SearchView.this.mPageIndex = ((SearchResult) SearchView.this.mShowResultList.get(SearchView.this.mCurrentPosition)).mPageIndex;
                    SearchView.this.mRect = ((SearchResult) SearchView.this.mShowResultList.get(SearchView.this.mCurrentPosition)).mRects;
                    SearchView.this.setToolbarIcon();
                    SearchView.this.mPdfViewCtrl.invalidate();
                    return;
                }
                SearchView.this.mCurrentPosition = position;
                SearchView.this.setCurrentPageX();
                rectF = new RectF(SearchView.this.mCurrentPageX, SearchView.this.mCurrentPageY, SearchView.this.mCurrentSearchR, SearchView.this.mCurrentSearchB);
                canvasRectF = new RectF();
                transSuccess = SearchView.this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, canvasRectF, ((SearchResult) SearchView.this.mShowResultList.get(SearchView.this.mCurrentPosition)).mPageIndex);
                screenWidth = SearchView.this.getVisibleWidth().width();
                screenHeight = SearchView.this.getVisibleWidth().height();
                if (!transSuccess || canvasRectF.left < 0.0f || canvasRectF.right > ((float) screenWidth) || canvasRectF.top < 0.0f || canvasRectF.bottom > ((float) screenHeight)) {
                    SearchView.this.mPdfViewCtrl.gotoPage(((SearchResult) SearchView.this.mShowResultList.get(SearchView.this.mCurrentPosition)).mPageIndex, (float) ((int) (SearchView.this.mCurrentPageX - ((float) (SearchView.this.getScreenWidth() / 4)))), (float) ((int) (SearchView.this.mCurrentPageY - ((float) (SearchView.this.getScreenHeight() / 4)))));
                }
                SearchView.this.mPageIndex = ((SearchResult) SearchView.this.mShowResultList.get(SearchView.this.mCurrentPosition)).mPageIndex;
                SearchView.this.mRect = ((SearchResult) SearchView.this.mShowResultList.get(SearchView.this.mCurrentPosition)).mRects;
                SearchView.this.setToolbarIcon();
                SearchView.this.mPdfViewCtrl.invalidate();
            }
        }
    };
    protected int mPageIndex = -1;
    private ViewGroup mParent = null;
    private PDFViewCtrl mPdfViewCtrl = null;
    public LinearLayout mRd_search_ll_bottom;
    public LinearLayout mRd_search_ll_center;
    public LinearLayout mRd_search_ll_top;
    protected List<RectF> mRect = new ArrayList();
    private SearchCancelListener mSearchCancelListener = null;
    private long mSearchId = 0;
    private String mSearchText = null;
    private View mSearchView = null;
    public String mSearch_content;
    private ArrayList<SearchResult> mShowResultList = new ArrayList();
    private ArrayList<SearchResult> mTagResultList = new ArrayList();
    private Button mTop_bt_cancel;
    public EditText mTop_et_content;
    public ImageView mTop_iv_clear;
    public LinearLayout mTop_ll_shadow;
    private ArrayList<SearchResult> mValueResultList = new ArrayList();
    public View mViewCenterLeft;
    public LinearLayout mViewCenterRight;
    private OnKeyListener mySearchListener = new OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (66 != keyCode || event.getAction() != 0) {
                return false;
            }
            if (!SearchView.this.mIsBlank) {
                SearchView.this.mSearch_content = SearchView.this.mTop_et_content.getText().toString();
                AppUtil.dismissInputSoft(SearchView.this.mTop_et_content);
                if (!(SearchView.this.mSearch_content == null || "".equals(SearchView.this.mSearch_content.trim()))) {
                    if (SearchView.this.mRd_search_ll_bottom.getVisibility() == 0) {
                        SearchView.this.mRd_search_ll_bottom.setVisibility(8);
                        SearchView.this.mBottom_ll_shadow.setVisibility(8);
                    }
                    if (SearchView.this.mViewCenterRight.getVisibility() == 0) {
                        SearchView.this.mIsCancel = false;
                        SearchView.this.searchText(SearchView.this.mSearch_content, 0);
                    } else {
                        SearchView.this.mRd_search_ll_center.setBackgroundResource(R.color.ux_color_mask_background);
                        SearchView.this.mViewCenterLeft.setVisibility(0);
                        SearchView.this.mViewCenterLeft.setClickable(false);
                        SearchView.this.mViewCenterRight.setVisibility(0);
                        Animation animationR2L = AnimationUtils.loadAnimation(SearchView.this.mContext, R.anim.view_anim_rtol_show);
                        animationR2L.setAnimationListener(new AnimationListener() {
                            public void onAnimationStart(Animation animation) {
                            }

                            public void onAnimationEnd(Animation animation) {
                                SearchView.this.mViewCenterLeft.setClickable(true);
                                SearchView.this.mIsCancel = false;
                                SearchView.this.searchText(SearchView.this.mSearch_content, 0);
                            }

                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        animationR2L.setStartOffset(300);
                        SearchView.this.mViewCenterRight.startAnimation(animationR2L);
                    }
                }
            }
            return true;
        }
    };
    private OnClickListener searchModelListener = new OnClickListener() {
        public void onClick(View v) {
            if (v.getId() == R.id.top_iv_clear) {
                SearchView.this.searchCancel();
            } else if (v.getId() == R.id.top_bt_cancel) {
                SearchView.this.cancel();
            } else if (v.getId() != R.id.rd_search_ll_top && v.getId() != R.id.rd_search_center_right) {
                if (v.getId() == R.id.rd_search_center_left) {
                    if (!SearchView.isFastDoubleClick()) {
                        AppUtil.dismissInputSoft(SearchView.this.mTop_et_content);
                        Animation animationL2R = AnimationUtils.loadAnimation(SearchView.this.mContext, R.anim.view_anim_rtol_hide);
                        animationL2R.setAnimationListener(new AnimationListener() {
                            public void onAnimationStart(Animation animation) {
                            }

                            public void onAnimationEnd(Animation animation) {
                                SearchView.this.mRd_search_ll_center.setBackgroundResource(R.color.ux_color_translucent);
                                SearchView.this.mViewCenterLeft.setVisibility(8);
                                SearchView.this.mViewCenterRight.setVisibility(8);
                                SearchView.this.mRd_search_ll_bottom.setVisibility(0);
                                SearchView.this.mBottom_ll_shadow.setVisibility(0);
                            }

                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        animationL2R.setStartOffset(0);
                        SearchView.this.mViewCenterRight.startAnimation(animationL2R);
                    }
                } else if (v.getId() == R.id.bottom_iv_result) {
                    SearchView.this.mRd_search_ll_bottom.setVisibility(8);
                    SearchView.this.mBottom_ll_shadow.setVisibility(8);
                    SearchView.this.mRd_search_ll_center.setBackgroundResource(R.color.ux_color_mask_background);
                    SearchView.this.mViewCenterLeft.setVisibility(0);
                    SearchView.this.mViewCenterRight.setVisibility(0);
                    Animation animationR2L = AnimationUtils.loadAnimation(SearchView.this.mContext, R.anim.view_anim_rtol_show);
                    animationR2L.setAnimationListener(new AnimationListener() {
                        public void onAnimationStart(Animation animation) {
                        }

                        public void onAnimationEnd(Animation animation) {
                        }

                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    animationR2L.setStartOffset(0);
                    SearchView.this.mViewCenterRight.startAnimation(animationR2L);
                } else if (v.getId() == R.id.bottom_iv_prev) {
                    SearchView.this.searchPre();
                } else if (v.getId() == R.id.bottom_iv_next) {
                    SearchView.this.searchNext();
                }
            }
        }
    };

    class SearchAdapter extends BaseAdapter {
        SearchAdapter() {
        }

        public int getCount() {
            return SearchView.this.mShowResultList == null ? 0 : SearchView.this.mShowResultList.size();
        }

        public Object getItem(int position) {
            return SearchView.this.mShowResultList.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout container;
            SpannableString searchContent;
            String matchText;
            View viewTag;
            SearchItemTag mItemTag;
            SearchItemView mItemView;
            View viewContent;
            String mContent;
            Matcher matcher;
            if (convertView == null) {
                LayoutParams params = new LayoutParams(-1, -2);
                container = new LinearLayout(SearchView.this.mContext);
                if (SearchView.this.mTagResultList.contains(SearchView.this.mShowResultList.get(position))) {
                    viewTag = SearchView.this.mInflater.inflate(R.layout.search_item_tag, null);
                    mItemTag = new SearchItemTag();
                    mItemTag.search_pageIndex = (TextView) viewTag.findViewById(R.id.search_page_tv);
                    mItemTag.search_pageCount = (TextView) viewTag.findViewById(R.id.search_curpage_count);
                    mItemTag.search_pageIndex.setText(String.format(SearchView.this.mContext.getResources().getString(R.string.search_page_number), new Object[]{new StringBuilder(String.valueOf(((SearchResult) SearchView.this.mShowResultList.get(position)).mPageIndex + 1)).toString()}));
                    mItemTag.search_pageCount.setText(new StringBuilder(String.valueOf(((SearchResult) SearchView.this.mShowResultList.get(position)).mPatternStart)).toString());
                    container.addView(viewTag, params);
                } else {
                    mItemView = new SearchItemView();
                    viewContent = SearchView.this.mInflater.inflate(R.layout.search_item_content, null);
                    mItemView.search_content = (TextView) viewContent.findViewById(R.id.search_content_tv);
                    mContent = ((SearchResult) SearchView.this.mShowResultList.get(position)).mSentence;
                    searchContent = new SpannableString(mContent);
                    matchText = SearchView.this.mSearchText.replaceAll("\r", " ").replaceAll("\n", " ").replaceAll("\\s+", " ");
                    if (matchText.length() > mContent.length()) {
                        matchText = mContent.substring(((SearchResult) SearchView.this.mShowResultList.get(position)).mPatternStart);
                    }
                    try {
                        matcher = Pattern.compile(matchText, 2).matcher(searchContent);
                        while (matcher.find()) {
                            searchContent.setSpan(new ForegroundColorSpan(SearchView.this.mContext.getResources().getColor(R.color.ux_text_color_subhead_colour)), ((SearchResult) SearchView.this.mShowResultList.get(position)).mPatternStart, ((SearchResult) SearchView.this.mShowResultList.get(position)).mPatternStart + matchText.length(), 33);
                        }
                        mItemView.search_content.setText(searchContent);
                        container.addView(viewContent, params);
                    } catch (PatternSyntaxException e) {
                        if (searchContent.subSequence(((SearchResult) SearchView.this.mShowResultList.get(position)).mPatternStart, ((SearchResult) SearchView.this.mShowResultList.get(position)).mPatternStart + matchText.length()).toString().equalsIgnoreCase(matchText)) {
                            searchContent.setSpan(new ForegroundColorSpan(SearchView.this.mContext.getResources().getColor(R.color.ux_text_color_subhead_colour)), ((SearchResult) SearchView.this.mShowResultList.get(position)).mPatternStart, ((SearchResult) SearchView.this.mShowResultList.get(position)).mPatternStart + matchText.length(), 33);
                        }
                        mItemView.search_content.setText(searchContent);
                        container.addView(viewContent, params);
                    }
                }
            } else {
                container = (LinearLayout) convertView;
                container.removeAllViews();
                if (SearchView.this.mTagResultList.contains(SearchView.this.mShowResultList.get(position))) {
                    viewTag = SearchView.this.mInflater.inflate(R.layout.search_item_tag, null);
                    mItemTag = new SearchItemTag();
                    mItemTag.search_pageIndex = (TextView) viewTag.findViewById(R.id.search_page_tv);
                    mItemTag.search_pageCount = (TextView) viewTag.findViewById(R.id.search_curpage_count);
                    mItemTag.search_pageIndex.setText(String.format(SearchView.this.mContext.getResources().getString(R.string.search_page_number), new Object[]{new StringBuilder(String.valueOf(((SearchResult) SearchView.this.mShowResultList.get(position)).mPageIndex + 1)).toString()}));
                    mItemTag.search_pageCount.setText(new StringBuilder(String.valueOf(((SearchResult) SearchView.this.mShowResultList.get(position)).mPatternStart)).toString());
                    container.addView(viewTag, new LayoutParams(-1, -2));
                } else {
                    mItemView = new SearchItemView();
                    viewContent = SearchView.this.mInflater.inflate(R.layout.search_item_content, null);
                    mItemView.search_content = (TextView) viewContent.findViewById(R.id.search_content_tv);
                    mContent = ((SearchResult) SearchView.this.mShowResultList.get(position)).mSentence;
                    searchContent = new SpannableString(mContent);
                    matchText = SearchView.this.mSearchText.replaceAll("\r", " ").replaceAll("\n", " ").replaceAll("\\s+", " ");
                    if (matchText.length() > mContent.length()) {
                        matchText = mContent.substring(((SearchResult) SearchView.this.mShowResultList.get(position)).mPatternStart);
                    }
                    try {
                        matcher = Pattern.compile(matchText, 2).matcher(searchContent);
                        while (matcher.find()) {
                            searchContent.setSpan(new ForegroundColorSpan(SearchView.this.mContext.getResources().getColor(R.color.ux_text_color_subhead_colour)), ((SearchResult) SearchView.this.mShowResultList.get(position)).mPatternStart, ((SearchResult) SearchView.this.mShowResultList.get(position)).mPatternStart + matchText.length(), 33);
                        }
                        mItemView.search_content.setText(searchContent);
                        container.addView(viewContent);
                    } catch (PatternSyntaxException e2) {
                        if (searchContent.subSequence(((SearchResult) SearchView.this.mShowResultList.get(position)).mPatternStart, ((SearchResult) SearchView.this.mShowResultList.get(position)).mPatternStart + matchText.length()).toString().equalsIgnoreCase(matchText)) {
                            searchContent.setSpan(new ForegroundColorSpan(SearchView.this.mContext.getResources().getColor(R.color.ux_text_color_subhead_colour)), ((SearchResult) SearchView.this.mShowResultList.get(position)).mPatternStart, ((SearchResult) SearchView.this.mShowResultList.get(position)).mPatternStart + matchText.length(), 33);
                        }
                        mItemView.search_content.setText(searchContent);
                        container.addView(viewContent);
                    }
                }
            }
            return container;
        }
    }

    public interface SearchCancelListener {
        void onSearchCancel();
    }

    private class SearchItemTag {
        public TextView search_pageCount;
        public TextView search_pageIndex;

        private SearchItemTag() {
        }
    }

    private class SearchItemView {
        public TextView search_content;

        private SearchItemView() {
        }
    }

    class SearchPageTask implements Runnable {
        protected int mFlag;
        protected int mPageIndex;
        protected String mPattern;
        protected PDFViewCtrl mPdfView;
        protected ArrayList<SearchResult> mSearchResults;
        protected TaskResult mTaskResult;

        public SearchPageTask(PDFViewCtrl pdfView, int pageIndex, String pattern, int flag, TaskResult<Integer, String, ArrayList<SearchResult>> taskResult) {
            this.mPdfView = pdfView;
            this.mPageIndex = pageIndex;
            this.mPattern = pattern;
            this.mFlag = flag;
            this.mTaskResult = taskResult;
        }

        public void run() {
            if (this.mSearchResults == null) {
                this.mSearchResults = new ArrayList();
            }
            int err = searchPage();
            if (this.mTaskResult != null) {
                this.mTaskResult.onResult(err, Integer.valueOf(this.mPageIndex), this.mPattern, this.mSearchResults);
            }
        }

        private int searchPage() {
            int errCode = PDFError.NO_ERROR.getCode();
            PDFDoc document = this.mPdfView.getDoc();
            try {
                Pause pause = new Pause() {
                    public boolean needPauseNow() {
                        return true;
                    }
                };
                PDFTextSearch textSearch = PDFTextSearch.create(document, pause);
                textSearch.setStartPage(this.mPageIndex);
                textSearch.setKeyWords(this.mPattern);
                for (boolean bRet = textSearch.findNext(); bRet && textSearch.getMatchPageIndex() == this.mPageIndex; bRet = textSearch.findNext()) {
                    String sentence = textSearch.getMatchSentence();
                    if (sentence == null) {
                        sentence = "";
                    }
                    SearchResult searchResult = new SearchResult(this.mPageIndex, sentence, textSearch.getMatchSentenceStartIndex());
                    int count = textSearch.getMatchRectCount();
                    for (int i = 0; i < count; i++) {
                        searchResult.mRects.add(textSearch.getMatchRect(i));
                    }
                    this.mSearchResults.add(searchResult);
                }
                textSearch.release();
                pause.release();
                return errCode;
            } catch (PDFException e) {
                return e.getLastError();
            }
        }
    }

    public static class SearchResult {
        public int mPageIndex;
        public int mPatternStart;
        public ArrayList<RectF> mRects = new ArrayList();
        public String mSentence;

        public SearchResult(int pageIndex, String sentence, int patternStart) {
            this.mPageIndex = pageIndex;
            this.mSentence = sentence;
            this.mPatternStart = patternStart;
        }
    }

    public interface TaskResult<T1, T2, T3> {
        long getTag();

        void onResult(int i, T1 t1, T2 t2, T3 t3);

        void setTag(long j);
    }

    public class myTextWatcher implements TextWatcher {
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void afterTextChanged(Editable s) {
            if (s.length() > 0) {
                SearchView.this.mTop_iv_clear.setVisibility(0);
                SearchView.this.mIsBlank = false;
                return;
            }
            SearchView.this.mTop_iv_clear.setVisibility(4);
            SearchView.this.mIsBlank = true;
        }
    }

    public SearchView(Context context, ViewGroup parent, PDFViewCtrl pdfViewCtrl) {
        this.mContext = context;
        this.mParent = parent;
        this.mPdfViewCtrl = pdfViewCtrl;
        this.mAppDisplay = AppDisplay.getInstance(context);
        this.mMetrics = context.getResources().getDisplayMetrics();
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mAdapterSearch = new SearchAdapter();
        this.mSearchView = LayoutInflater.from(context).inflate(R.layout.search_layout, null, false);
        this.mSearchView.setVisibility(8);
        this.mParent = parent;
        this.mParent.addView(this.mSearchView);
        initView();
        bindEvent();
    }

    public void initView() {
        this.mRd_search_ll_top = (LinearLayout) this.mSearchView.findViewById(R.id.rd_search_ll_top);
        this.mTop_et_content = (EditText) this.mSearchView.findViewById(R.id.top_et_content);
        this.mTop_iv_clear = (ImageView) this.mSearchView.findViewById(R.id.top_iv_clear);
        this.mTop_bt_cancel = (Button) this.mSearchView.findViewById(R.id.top_bt_cancel);
        this.mTop_ll_shadow = (LinearLayout) this.mSearchView.findViewById(R.id.top_ll_shadow);
        this.mRd_search_ll_center = (LinearLayout) this.mSearchView.findViewById(R.id.rd_search_ll_center);
        this.mViewCenterLeft = this.mSearchView.findViewById(R.id.rd_search_center_left);
        this.mViewCenterRight = (LinearLayout) this.mSearchView.findViewById(R.id.rd_search_center_right);
        this.mCenter_tv_total_number = (TextView) this.mSearchView.findViewById(R.id.center_tv_total_number);
        this.mCenter_lv_result_list = (ListView) this.mSearchView.findViewById(R.id.center_lv_result_list);
        this.mRd_search_ll_bottom = (LinearLayout) this.mSearchView.findViewById(R.id.rd_search_ll_bottom);
        this.mBottom_iv_prev = (ImageView) this.mSearchView.findViewById(R.id.bottom_iv_prev);
        this.mBottom_iv_next = (ImageView) this.mSearchView.findViewById(R.id.bottom_iv_next);
        this.mBottom_iv_result = (ImageView) this.mSearchView.findViewById(R.id.bottom_iv_result);
        this.mBottom_ll_shadow = (LinearLayout) this.mSearchView.findViewById(R.id.bottom_ll_shadow);
        RelativeLayout.LayoutParams topParams = (RelativeLayout.LayoutParams) this.mRd_search_ll_top.getLayoutParams();
        RelativeLayout.LayoutParams bottomParams = (RelativeLayout.LayoutParams) this.mRd_search_ll_bottom.getLayoutParams();
        if (this.mAppDisplay.isPad()) {
            topParams.height = (int) this.mContext.getResources().getDimension(R.dimen.ux_toolbar_height_pad);
            bottomParams.height = (int) this.mContext.getResources().getDimension(R.dimen.ux_toolbar_height_pad);
        } else {
            topParams.height = (int) this.mContext.getResources().getDimension(R.dimen.ux_toolbar_height_phone);
            bottomParams.height = (int) this.mContext.getResources().getDimension(R.dimen.ux_toolbar_height_phone);
        }
        this.mRd_search_ll_top.setLayoutParams(topParams);
        this.mRd_search_ll_bottom.setLayoutParams(bottomParams);
        this.mTop_et_content.setFocusable(true);
        this.mTop_et_content.requestFocus();
        this.mRd_search_ll_center.setVisibility(0);
        this.mRd_search_ll_center.setBackgroundResource(R.color.ux_color_translucent);
        this.mViewCenterLeft.setVisibility(8);
        this.mViewCenterRight.setVisibility(8);
        this.mRd_search_ll_bottom.setVisibility(8);
        this.mBottom_ll_shadow.setVisibility(8);
        setSearchResultWidth();
    }

    protected View getView() {
        return this.mSearchView;
    }

    public void bindEvent() {
        AppUtil.dismissInputSoft(this.mTop_et_content);
        this.mTop_et_content.addTextChangedListener(new myTextWatcher());
        this.mTop_et_content.setOnKeyListener(this.mySearchListener);
        this.mTop_iv_clear.setOnClickListener(this.searchModelListener);
        this.mTop_bt_cancel.setOnClickListener(this.searchModelListener);
        this.mRd_search_ll_top.setOnClickListener(this.searchModelListener);
        this.mViewCenterLeft.setOnClickListener(this.searchModelListener);
        this.mViewCenterRight.setOnClickListener(this.searchModelListener);
        this.mBottom_iv_result.setOnClickListener(this.searchModelListener);
        this.mBottom_iv_prev.setOnClickListener(this.searchModelListener);
        this.mBottom_iv_next.setOnClickListener(this.searchModelListener);
        this.mBottom_iv_prev.setEnabled(false);
        this.mBottom_iv_next.setEnabled(false);
        this.mRd_search_ll_bottom.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        this.mCenter_lv_result_list.setAdapter(this.mAdapterSearch);
        this.mCenter_lv_result_list.setOnItemClickListener(this.mOnItemClickListener);
    }

    protected void setToolbarIcon() {
        this.mBottom_iv_prev.setImageDrawable(this.mSearchView.getResources().getDrawable(R.drawable.search_previous));
        this.mBottom_iv_next.setImageDrawable(this.mSearchView.getResources().getDrawable(R.drawable.search_next));
        this.mBottom_iv_prev.setEnabled(true);
        this.mBottom_iv_next.setEnabled(true);
        if (isFirstSearchResult()) {
            this.mBottom_iv_prev.setImageDrawable(this.mSearchView.getResources().getDrawable(R.drawable.search_previous_pressed));
            this.mBottom_iv_prev.setEnabled(false);
        }
        if (isLastSearchResult()) {
            this.mBottom_iv_next.setImageDrawable(this.mSearchView.getResources().getDrawable(R.drawable.search_next_pressed));
            this.mBottom_iv_next.setEnabled(false);
        }
    }

    protected void setTotalNumber(int count) {
        this.mCenter_tv_total_number.setText(String.format(this.mContext.getResources().getString(R.string.search_find_number), new Object[]{new StringBuilder(String.valueOf(count)).toString()}));
    }

    private void setSearchResultWidth() {
        LayoutParams leftParams = (LayoutParams) this.mViewCenterLeft.getLayoutParams();
        LayoutParams rightParams = (LayoutParams) this.mViewCenterRight.getLayoutParams();
        if (this.mAppDisplay.isPad()) {
            if (this.mAppDisplay.isLandscape()) {
                leftParams.width = 0;
                leftParams.height = -1;
                leftParams.weight = 2.0f;
                rightParams.width = 0;
                rightParams.height = -1;
                rightParams.weight = 1.0f;
            } else {
                leftParams.width = 0;
                leftParams.height = -1;
                leftParams.weight = 1.0f;
                rightParams.width = 0;
                rightParams.height = -1;
                rightParams.weight = 1.0f;
            }
        } else if (this.mAppDisplay.isLandscape()) {
            leftParams.width = 0;
            leftParams.height = -1;
            leftParams.weight = 1.0f;
            rightParams.width = 0;
            rightParams.height = -1;
            rightParams.weight = 4.0f;
        } else {
            leftParams.width = 0;
            leftParams.height = -1;
            leftParams.weight = 1.0f;
            rightParams.width = 0;
            rightParams.height = -1;
            rightParams.weight = 4.0f;
        }
        this.mViewCenterLeft.setLayoutParams(leftParams);
        this.mViewCenterRight.setLayoutParams(rightParams);
    }

    public void show() {
        if (this.mSearchView != null) {
            this.mSearchView.setVisibility(0);
        }
    }

    public void dismiss() {
        if (this.mSearchView != null) {
            this.mSearchView.setVisibility(8);
        }
    }

    protected void OnPreItemClick() {
        AppUtil.dismissInputSoft(this.mTop_et_content);
        Animation animationL2R = AnimationUtils.loadAnimation(this.mContext, R.anim.view_anim_rtol_hide);
        animationL2R.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                SearchView.this.mViewCenterRight.setVisibility(8);
                SearchView.this.mViewCenterLeft.setVisibility(8);
                SearchView.this.mRd_search_ll_center.setBackgroundResource(R.color.ux_color_translucent);
                SearchView.this.mRd_search_ll_bottom.setVisibility(0);
                SearchView.this.mBottom_ll_shadow.setVisibility(0);
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        animationL2R.setStartOffset(0);
        this.mViewCenterRight.startAnimation(animationL2R);
    }

    public void setSearchCancelListener(SearchCancelListener listener) {
        this.mSearchCancelListener = listener;
    }

    protected void cancel() {
        this.mIsCancel = true;
        this.mIsBlank = true;
        searchCancel();
        this.mSearchView.setVisibility(4);
        cancelSearchText();
        AppUtil.dismissInputSoft(this.mTop_et_content);
        if (this.mSearchCancelListener != null) {
            this.mSearchCancelListener.onSearchCancel();
        }
    }

    public void launchSearchView() {
        this.mIsCancel = false;
        this.bCancelSearchText = false;
        this.mRect = null;
        if (this.mTop_et_content.getText().length() > 0) {
            this.mTop_et_content.selectAll();
            this.mTop_iv_clear.setVisibility(0);
        }
        this.mTop_et_content.requestFocus();
        this.mTop_et_content.setFocusable(true);
        AppUtil.showSoftInput(this.mTop_et_content);
    }

    private void searchCancel() {
        this.mTop_et_content.setText("");
        this.mTop_iv_clear.setVisibility(4);
    }

    public static boolean isFastDoubleClick() {
        long currentTimeMillis = System.currentTimeMillis();
        if (Math.abs(currentTimeMillis - sLastTimeMillis) < 500) {
            return true;
        }
        sLastTimeMillis = currentTimeMillis;
        return false;
    }

    protected int getScreenWidth() {
        return this.mMetrics.widthPixels;
    }

    protected int getScreenHeight() {
        return this.mMetrics.heightPixels;
    }

    private void searchPage(int pageIndex, String pattern, int flag, TaskResult<Integer, String, ArrayList<SearchResult>> result) {
        new Handler().post(new SearchPageTask(this.mPdfViewCtrl, pageIndex, pattern, flag, result));
    }

    private void _searchText(final String pattern, final int flag, final int pageIndex) {
        this.mSearchText = pattern.trim();
        String trim = pattern.trim();
        TaskResult<Integer, String, ArrayList<SearchResult>> taskResult = new TaskResult<Integer, String, ArrayList<SearchResult>>() {
            private long mTaskId;

            public void onResult(int errCode, Integer p1, String p2, ArrayList<SearchResult> p3) {
                if (errCode == 10) {
                    SearchView.this.mPdfViewCtrl.recoverForOOM();
                } else if (this.mTaskId == SearchView.this.mSearchId && p3 != null) {
                    if (p3.size() > 0) {
                        SearchResult searchResult = new SearchResult(p1.intValue(), "tag", p3.size());
                        SearchView.this.mTagResultList.add(searchResult);
                        SearchView.this.mShowResultList.add(searchResult);
                    }
                    SearchView.this.mValueResultList.addAll(p3);
                    SearchView.this.mShowResultList.addAll(p3);
                    if (p3.size() > 0) {
                        SearchView.this.notifyDataSetChangedSearchAdapter();
                    }
                    SearchView.this.setTotalNumber(SearchView.this.mValueResultList.size());
                    if (pageIndex != SearchView.this.mPdfViewCtrl.getPageCount() - 1) {
                        if (p1.intValue() >= SearchView.this.mPdfViewCtrl.getCurrentPage() && SearchView.this.mCurrentPosition == -1 && p3.size() > 0) {
                            SearchView.this.mCurrentPosition = SearchView.this.mShowResultList.size() - p3.size();
                            SearchView.this.mPageIndex = ((SearchResult) SearchView.this.mShowResultList.get(SearchView.this.mCurrentPosition)).mPageIndex;
                            SearchView.this.mRect = ((SearchResult) SearchView.this.mShowResultList.get(SearchView.this.mCurrentPosition)).mRects;
                            SearchView.this.setToolbarIcon();
                            SearchView.this.mPdfViewCtrl.invalidate();
                        }
                        SearchView.this.setToolbarIcon();
                        if (SearchView.this.bCancelSearchText) {
                            SearchView.this.bCancelSearchText = false;
                        } else {
                            SearchView.this._searchText(pattern, flag, pageIndex + 1);
                        }
                    } else if (SearchView.this.mCurrentPosition == -1 && SearchView.this.mShowResultList.size() > 0) {
                        SearchView.this.mCurrentPosition = SearchView.this.mShowResultList.size() - 1;
                        if (SearchView.this.mCurrentPosition != -1) {
                            SearchView.this.mPageIndex = ((SearchResult) SearchView.this.mShowResultList.get(SearchView.this.mCurrentPosition)).mPageIndex;
                            SearchView.this.mRect = ((SearchResult) SearchView.this.mShowResultList.get(SearchView.this.mCurrentPosition)).mRects;
                            SearchView.this.setToolbarIcon();
                            SearchView.this.mPdfViewCtrl.invalidate();
                        }
                    }
                }
            }

            public void setTag(long taskId) {
                this.mTaskId = taskId;
            }

            public long getTag() {
                return this.mTaskId;
            }
        };
        searchPage(pageIndex, trim, flag, taskResult);
        taskResult.setTag(this.mSearchId);
    }

    public void searchText(String pattern, int flag) {
        cancelSearchText();
        clearSearchResult();
        this.mCurrentPosition = -1;
        this.mSearchId++;
        this.mRect = null;
        this.mIsCancel = false;
        this.mSearchText = null;
        synchronized (this) {
            this.bCancelSearchText = false;
        }
        _searchText(pattern, flag, 0);
    }

    public void cancelSearchText() {
        synchronized (this) {
            if (!this.bCancelSearchText) {
                this.bCancelSearchText = true;
                onCancelSearchText();
            }
        }
    }

    private void notifyDataSetChangedSearchAdapter() {
        if (this.mAdapterSearch != null) {
            this.mAdapterSearch.notifyDataSetChanged();
        }
    }

    private void clearSearchResult() {
        if (!(this.mShowResultList == null && this.mTagResultList == null && this.mValueResultList == null)) {
            this.mTagResultList.clear();
            this.mValueResultList.clear();
            this.mShowResultList.clear();
        }
        notifyDataSetChangedSearchAdapter();
    }

    private void onCancelSearchText() {
        this.mRd_search_ll_bottom.setVisibility(8);
        this.mPdfViewCtrl.invalidate();
    }

    private Rect getVisibleWidth() {
        Rect rect = new Rect();
        this.mPdfViewCtrl.getGlobalVisibleRect(rect);
        return rect;
    }

    public void searchPre() {
        if (this.mSearchText != null && !this.bCancelSearchText) {
            if (this.mCurrentPosition <= 1) {
                this.mPageIndex = ((SearchResult) this.mShowResultList.get(this.mCurrentPosition)).mPageIndex;
                this.mRect = ((SearchResult) this.mShowResultList.get(this.mCurrentPosition)).mRects;
                setToolbarIcon();
                this.mPdfViewCtrl.invalidate();
                return;
            }
            this.mCurrentPosition--;
            if (((SearchResult) this.mShowResultList.get(this.mCurrentPosition)).mSentence.endsWith("tag")) {
                this.mCurrentPosition--;
            }
            setCurrentPageX();
            RectF rectF = new RectF(this.mCurrentPageX, this.mCurrentPageY, this.mCurrentSearchR, this.mCurrentSearchB);
            RectF canvasRectF = new RectF();
            boolean transSuccess = this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, canvasRectF, ((SearchResult) this.mShowResultList.get(this.mCurrentPosition)).mPageIndex);
            int screenWidth = getVisibleWidth().width();
            int screenHeight = getVisibleWidth().height();
            if (!transSuccess || canvasRectF.left < 0.0f || canvasRectF.right > ((float) screenWidth) || canvasRectF.top < 0.0f || canvasRectF.bottom > ((float) screenHeight)) {
                this.mPdfViewCtrl.gotoPage(((SearchResult) this.mShowResultList.get(this.mCurrentPosition)).mPageIndex, (float) ((int) (this.mCurrentPageX - ((float) (getScreenWidth() / 4)))), (float) ((int) (this.mCurrentPageY - ((float) (getScreenHeight() / 4)))));
            }
            this.mPageIndex = ((SearchResult) this.mShowResultList.get(this.mCurrentPosition)).mPageIndex;
            this.mRect = ((SearchResult) this.mShowResultList.get(this.mCurrentPosition)).mRects;
            setToolbarIcon();
            this.mPdfViewCtrl.invalidate();
        }
    }

    public void searchNext() {
        if (this.mSearchText != null && !this.bCancelSearchText) {
            if (this.mCurrentPosition >= this.mShowResultList.size() - 1) {
                this.mPageIndex = ((SearchResult) this.mShowResultList.get(this.mCurrentPosition)).mPageIndex;
                this.mRect = ((SearchResult) this.mShowResultList.get(this.mCurrentPosition)).mRects;
                setToolbarIcon();
                this.mPdfViewCtrl.invalidate();
                return;
            }
            this.mCurrentPosition++;
            if (((SearchResult) this.mShowResultList.get(this.mCurrentPosition)).mSentence.endsWith("tag")) {
                this.mCurrentPosition++;
            }
            setCurrentPageX();
            RectF rectF = new RectF(this.mCurrentPageX, this.mCurrentPageY, this.mCurrentSearchR, this.mCurrentSearchB);
            RectF canvasRectF = new RectF();
            boolean transSuccess = this.mPdfViewCtrl.convertPageViewRectToDisplayViewRect(rectF, canvasRectF, ((SearchResult) this.mShowResultList.get(this.mCurrentPosition)).mPageIndex);
            int screenWidth = getVisibleWidth().width();
            int screenHeight = getVisibleWidth().height();
            if (!transSuccess || canvasRectF.left < 0.0f || canvasRectF.right > ((float) screenWidth) || canvasRectF.top < 0.0f || canvasRectF.bottom > ((float) screenHeight)) {
                this.mPdfViewCtrl.gotoPage(((SearchResult) this.mShowResultList.get(this.mCurrentPosition)).mPageIndex, (float) ((int) (this.mCurrentPageX - ((float) (getScreenWidth() / 4)))), (float) ((int) (this.mCurrentPageY - ((float) (getScreenHeight() / 4)))));
            }
            this.mPageIndex = ((SearchResult) this.mShowResultList.get(this.mCurrentPosition)).mPageIndex;
            this.mRect = ((SearchResult) this.mShowResultList.get(this.mCurrentPosition)).mRects;
            setToolbarIcon();
            this.mPdfViewCtrl.invalidate();
        }
    }

    public boolean isFirstSearchResult() {
        return this.mCurrentPosition <= 1;
    }

    public boolean isLastSearchResult() {
        return this.mCurrentPosition < 1 || this.mCurrentPosition >= this.mShowResultList.size() - 1;
    }

    private void setCurrentPageX() {
        float x = 0.0f;
        float y = 0.0f;
        float r = 0.0f;
        float b = 0.0f;
        for (int i = 0; i < ((SearchResult) this.mShowResultList.get(this.mCurrentPosition)).mRects.size(); i++) {
            RectF pageRect = new RectF((RectF) ((SearchResult) this.mShowResultList.get(this.mCurrentPosition)).mRects.get(i));
            RectF pageViewRect = new RectF();
            if (this.mPdfViewCtrl.convertPdfRectToPageViewRect(pageRect, pageViewRect, ((SearchResult) this.mShowResultList.get(this.mCurrentPosition)).mPageIndex)) {
                if (i == 0) {
                    x = pageViewRect.left;
                    y = pageViewRect.top;
                    r = pageViewRect.right;
                    b = pageViewRect.bottom;
                } else {
                    if (pageViewRect.left < x) {
                        x = pageViewRect.left;
                    }
                    if (pageViewRect.top < y) {
                        y = pageViewRect.top;
                    }
                    if (pageViewRect.right > r) {
                        r = pageViewRect.right;
                    }
                    if (pageViewRect.bottom > b) {
                        b = pageViewRect.bottom;
                    }
                }
            }
        }
        this.mCurrentPageX = x;
        this.mCurrentPageY = y;
        this.mCurrentSearchR = r;
        this.mCurrentSearchB = b;
    }

    public void onDocumentClosed() {
        this.mTop_et_content.setText("");
        clearSearchResult();
        this.mCenter_tv_total_number.setText("");
    }
}
