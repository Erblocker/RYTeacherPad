package wei.mark.standout.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import java.util.LinkedList;
import java.util.Queue;
import wei.mark.standout.R;
import wei.mark.standout.StandOutWindow;
import wei.mark.standout.StandOutWindow.StandOutLayoutParams;
import wei.mark.standout.Utils;
import wei.mark.standout.constants.StandOutFlags;

public class Window extends FrameLayout {
    static final String TAG = "Window";
    public static final int VISIBILITY_GONE = 0;
    public static final int VISIBILITY_TRANSITION = 2;
    public static final int VISIBILITY_VISIBLE = 1;
    public Class<? extends StandOutWindow> cls;
    public Bundle data;
    int displayHeight;
    int displayWidth;
    public int flags;
    public boolean focused;
    public int id;
    private final StandOutWindow mContext;
    private LayoutInflater mLayoutInflater;
    public StandOutLayoutParams originalParams;
    public TouchInfo touchInfo;
    public int visibility;

    public class Editor {
        public static final int UNCHANGED = Integer.MIN_VALUE;
        float anchorX = 0.0f;
        float anchorY = 0.0f;
        StandOutLayoutParams mParams;

        public Editor() {
            this.mParams = Window.this.getLayoutParams();
        }

        public Editor setAnchorPoint(float x, float y) {
            if (x < 0.0f || x > 1.0f || y < 0.0f || y > 1.0f) {
                throw new IllegalArgumentException("Anchor point must be between 0 and 1, inclusive.");
            }
            this.anchorX = x;
            this.anchorY = y;
            return this;
        }

        public Editor setSize(float percentWidth, float percentHeight) {
            return setSize((int) (((float) Window.this.displayWidth) * percentWidth), (int) (((float) Window.this.displayHeight) * percentHeight));
        }

        public Editor setSize(int width, int height) {
            return setSize(width, height, false);
        }

        private Editor setSize(int width, int height, boolean skip) {
            if (this.mParams != null) {
                if (this.anchorX < 0.0f || this.anchorX > 1.0f || this.anchorY < 0.0f || this.anchorY > 1.0f) {
                    throw new IllegalStateException("Anchor point must be between 0 and 1, inclusive.");
                }
                int lastWidth = this.mParams.width;
                int lastHeight = this.mParams.height;
                if (width != Integer.MIN_VALUE) {
                    this.mParams.width = width;
                }
                if (height != Integer.MIN_VALUE) {
                    this.mParams.height = height;
                }
                int maxWidth = this.mParams.maxWidth;
                int maxHeight = this.mParams.maxHeight;
                if (Utils.isSet(Window.this.flags, StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE)) {
                    maxWidth = Math.min(maxWidth, Window.this.displayWidth);
                    maxHeight = Math.min(maxHeight, Window.this.displayHeight);
                }
                this.mParams.width = Math.min(Math.max(this.mParams.width, this.mParams.minWidth), maxWidth);
                this.mParams.height = Math.min(Math.max(this.mParams.height, this.mParams.minHeight), maxHeight);
                if (Utils.isSet(Window.this.flags, StandOutFlags.FLAG_WINDOW_ASPECT_RATIO_ENABLE)) {
                    int ratioWidth = (int) (((float) this.mParams.height) * Window.this.touchInfo.ratio);
                    int ratioHeight = (int) (((float) this.mParams.width) / Window.this.touchInfo.ratio);
                    if (ratioHeight < this.mParams.minHeight || ratioHeight > this.mParams.maxHeight) {
                        this.mParams.width = ratioWidth;
                    } else {
                        this.mParams.height = ratioHeight;
                    }
                }
                if (!skip) {
                    setPosition((int) (((float) this.mParams.x) + (((float) lastWidth) * this.anchorX)), (int) (((float) this.mParams.y) + (((float) lastHeight) * this.anchorY)));
                }
            }
            return this;
        }

        public Editor setPosition(float percentWidth, float percentHeight) {
            return setPosition((int) (((float) Window.this.displayWidth) * percentWidth), (int) (((float) Window.this.displayHeight) * percentHeight));
        }

        public Editor setPosition(int x, int y) {
            return setPosition(x, y, false);
        }

        private Editor setPosition(int x, int y, boolean skip) {
            if (this.mParams != null) {
                if (this.anchorX < 0.0f || this.anchorX > 1.0f || this.anchorY < 0.0f || this.anchorY > 1.0f) {
                    throw new IllegalStateException("Anchor point must be between 0 and 1, inclusive.");
                }
                if (x != Integer.MIN_VALUE) {
                    this.mParams.x = (int) (((float) x) - (((float) this.mParams.width) * this.anchorX));
                }
                if (y != Integer.MIN_VALUE) {
                    this.mParams.y = (int) (((float) y) - (((float) this.mParams.height) * this.anchorY));
                }
                if (Utils.isSet(Window.this.flags, StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE)) {
                    if (this.mParams.gravity != 51) {
                        throw new IllegalStateException("The window " + Window.this.id + " gravity must be TOP|LEFT if FLAG_WINDOW_EDGE_LIMITS_ENABLE or FLAG_WINDOW_EDGE_TILE_ENABLE is set.");
                    }
                    this.mParams.x = Math.min(Math.max(this.mParams.x, 0), Window.this.displayWidth - this.mParams.width);
                    this.mParams.y = Math.min(Math.max(this.mParams.y, 0), Window.this.displayHeight - this.mParams.height);
                }
            }
            return this;
        }

        public void commit() {
            if (this.mParams != null) {
                Window.this.mContext.updateViewLayout(Window.this.id, this.mParams);
                this.mParams = null;
            }
        }
    }

    public static class WindowDataKeys {
        public static final String HEIGHT_BEFORE_MAXIMIZE = "heightBeforeMaximize";
        public static final String IS_MAXIMIZED = "isMaximized";
        public static final String WIDTH_BEFORE_MAXIMIZE = "widthBeforeMaximize";
        public static final String X_BEFORE_MAXIMIZE = "xBeforeMaximize";
        public static final String Y_BEFORE_MAXIMIZE = "yBeforeMaximize";
    }

    public Window(Context context) {
        super(context);
        this.mContext = null;
    }

    public Window(final StandOutWindow context, final int id) {
        View content;
        FrameLayout body;
        super(context);
        context.setTheme(context.getThemeStyle());
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.cls = context.getClass();
        this.id = id;
        this.originalParams = context.getParams(id, this);
        this.flags = context.getFlags(id);
        this.touchInfo = new TouchInfo();
        this.touchInfo.ratio = ((float) this.originalParams.width) / ((float) this.originalParams.height);
        this.data = new Bundle();
        DisplayMetrics metrics = this.mContext.getResources().getDisplayMetrics();
        this.displayWidth = metrics.widthPixels;
        this.displayHeight = (int) (((float) metrics.heightPixels) - (25.0f * metrics.density));
        if (Utils.isSet(this.flags, StandOutFlags.FLAG_DECORATION_SYSTEM)) {
            content = getSystemDecorations();
            body = (FrameLayout) content.findViewById(R.id.body);
        } else {
            content = new FrameLayout(context);
            content.setId(R.id.content);
            body = (FrameLayout) content;
        }
        addView(content);
        body.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                boolean consumed = context.onTouchHandleMove(id, Window.this, v, event) || false;
                if (context.onTouchBody(id, Window.this, v, event) || consumed) {
                    return true;
                }
                return false;
            }
        });
        context.createAndAttachView(id, body);
        if (body.getChildCount() == 0) {
            throw new RuntimeException("You must attach your view to the given frame in createAndAttachView()");
        }
        if (!Utils.isSet(this.flags, StandOutFlags.FLAG_FIX_COMPATIBILITY_ALL_DISABLE)) {
            fixCompatibility(body);
        }
        if (!Utils.isSet(this.flags, StandOutFlags.FLAG_ADD_FUNCTIONALITY_ALL_DISABLE)) {
            addFunctionality(body);
        }
        setTag(body.getTag());
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        StandOutLayoutParams params = getLayoutParams();
        if (event.getAction() == 0 && this.mContext.getFocusedWindow() != this) {
            this.mContext.focus(this.id);
        }
        if (event.getPointerCount() < 2 || !Utils.isSet(this.flags, StandOutFlags.FLAG_WINDOW_PINCH_RESIZE_ENABLE) || (event.getAction() & 255) != 5) {
            return false;
        }
        this.touchInfo.scale = 1.0d;
        this.touchInfo.dist = -1.0d;
        this.touchInfo.firstWidth = (double) params.width;
        this.touchInfo.firstHeight = (double) params.height;
        return true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case 4:
                if (this.mContext.getFocusedWindow() == this) {
                    this.mContext.unfocus(this);
                }
                this.mContext.onTouchBody(this.id, this, this, event);
                break;
        }
        if (event.getPointerCount() >= 2 && Utils.isSet(this.flags, StandOutFlags.FLAG_WINDOW_PINCH_RESIZE_ENABLE)) {
            float x0 = event.getX(0);
            float y0 = event.getY(0);
            double dist = Math.sqrt(Math.pow((double) (x0 - event.getX(1)), 2.0d) + Math.pow((double) (y0 - event.getY(1)), 2.0d));
            switch (event.getAction() & 255) {
                case 2:
                    if (this.touchInfo.dist == -1.0d) {
                        this.touchInfo.dist = dist;
                    }
                    TouchInfo touchInfo = this.touchInfo;
                    touchInfo.scale *= dist / this.touchInfo.dist;
                    this.touchInfo.dist = dist;
                    edit().setAnchorPoint(0.5f, 0.5f).setSize((int) (this.touchInfo.firstWidth * this.touchInfo.scale), (int) (this.touchInfo.firstHeight * this.touchInfo.scale)).commit();
                    break;
            }
            this.mContext.onResize(this.id, this, this, event);
        }
        return true;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (this.mContext.onKeyEvent(this.id, this, event)) {
            Log.d(TAG, "Window " + this.id + " key event " + event + " cancelled by implementation.");
            return false;
        }
        if (event.getAction() == 1) {
            switch (event.getKeyCode()) {
                case 4:
                    this.mContext.unfocus(this);
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public boolean onFocus(boolean focus) {
        if (Utils.isSet(this.flags, StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE) || focus == this.focused) {
            return false;
        }
        this.focused = focus;
        if (this.mContext.onFocusChange(this.id, this, focus)) {
            boolean z;
            Log.d(TAG, "Window " + this.id + " focus change " + (focus ? "(true)" : "(false)") + " cancelled by implementation.");
            if (focus) {
                z = false;
            } else {
                z = true;
            }
            this.focused = z;
            return false;
        }
        if (!Utils.isSet(this.flags, StandOutFlags.FLAG_WINDOW_FOCUS_INDICATOR_DISABLE)) {
            View content = findViewById(R.id.content);
            if (focus) {
                content.setBackgroundResource(R.drawable.bg_focused_window);
            } else if (Utils.isSet(this.flags, StandOutFlags.FLAG_DECORATION_SYSTEM)) {
                content.setBackgroundResource(R.drawable.bg_window);
            } else {
                content.setBackgroundResource(0);
            }
        }
        StandOutLayoutParams params = getLayoutParams();
        params.setFocusFlag(focus);
        this.mContext.updateViewLayout(this.id, params);
        if (focus) {
            this.mContext.setFocusedWindow(this);
        } else if (this.mContext.getFocusedWindow() == this) {
            this.mContext.setFocusedWindow(null);
        }
        return true;
    }

    public void setLayoutParams(LayoutParams params) {
        if (params instanceof StandOutLayoutParams) {
            super.setLayoutParams(params);
            return;
        }
        throw new IllegalArgumentException(new StringBuilder(TAG).append(this.id).append(": LayoutParams must be an instance of StandOutLayoutParams.").toString());
    }

    public Editor edit() {
        return new Editor();
    }

    public StandOutLayoutParams getLayoutParams() {
        StandOutLayoutParams params = (StandOutLayoutParams) super.getLayoutParams();
        if (params == null) {
            return this.originalParams;
        }
        return params;
    }

    private View getSystemDecorations() {
        View decorations = this.mLayoutInflater.inflate(R.layout.system_window_decorators, null);
        ((ImageView) decorations.findViewById(R.id.window_icon)).setImageResource(this.mContext.getAppIcon());
        ((TextView) decorations.findViewById(R.id.title)).setText(this.mContext.getTitle(this.id));
        View hide = decorations.findViewById(R.id.hide);
        hide.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Window.this.mContext.hide(Window.this.id);
            }
        });
        hide.setVisibility(8);
        View maximize = decorations.findViewById(R.id.maximize);
        maximize.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                StandOutLayoutParams params = Window.this.getLayoutParams();
                boolean isMaximized = Window.this.data.getBoolean(WindowDataKeys.IS_MAXIMIZED);
                if (isMaximized && params.width == Window.this.displayWidth && params.height == Window.this.displayHeight && params.x == 0 && params.y == 0) {
                    Window.this.data.putBoolean(WindowDataKeys.IS_MAXIMIZED, false);
                    int oldWidth = Window.this.data.getInt(WindowDataKeys.WIDTH_BEFORE_MAXIMIZE, -1);
                    int oldHeight = Window.this.data.getInt(WindowDataKeys.HEIGHT_BEFORE_MAXIMIZE, -1);
                    Window.this.edit().setSize(oldWidth, oldHeight).setPosition(Window.this.data.getInt(WindowDataKeys.X_BEFORE_MAXIMIZE, -1), Window.this.data.getInt(WindowDataKeys.Y_BEFORE_MAXIMIZE, -1)).commit();
                } else {
                    Window.this.data.putBoolean(WindowDataKeys.IS_MAXIMIZED, true);
                    Window.this.data.putInt(WindowDataKeys.WIDTH_BEFORE_MAXIMIZE, params.width);
                    Window.this.data.putInt(WindowDataKeys.HEIGHT_BEFORE_MAXIMIZE, params.height);
                    Window.this.data.putInt(WindowDataKeys.X_BEFORE_MAXIMIZE, params.x);
                    Window.this.data.putInt(WindowDataKeys.Y_BEFORE_MAXIMIZE, params.y);
                    Window.this.edit().setSize(1.0f, 1.0f).setPosition(0, 0).commit();
                }
                if (isMaximized) {
                    Window.this.mContext.onTouchWindowButton(R.id.restore, Window.this.id, Window.this, v);
                } else {
                    Window.this.mContext.onTouchWindowButton(R.id.maximize, Window.this.id, Window.this, v);
                }
                Window.this.mContext.onResize(Window.this.id, Window.this, v, null);
            }
        });
        View close = decorations.findViewById(R.id.close);
        close.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Window.this.mContext.onTouchWindowButton(R.id.close, Window.this.id, Window.this, v);
                Window.this.mContext.close(Window.this.id);
            }
        });
        View titlebar = decorations.findViewById(R.id.titlebar);
        titlebar.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return Window.this.mContext.onTouchHandleMove(Window.this.id, Window.this, v, event);
            }
        });
        View corner = decorations.findViewById(R.id.corner);
        corner.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return Window.this.mContext.onTouchHandleResize(Window.this.id, Window.this, v, event);
            }
        });
        if (Utils.isSet(this.flags, StandOutFlags.FLAG_WINDOW_HIDE_ENABLE)) {
            hide.setVisibility(0);
        }
        if (Utils.isSet(this.flags, StandOutFlags.FLAG_DECORATION_MAXIMIZE_DISABLE)) {
            maximize.setVisibility(8);
        }
        if (Utils.isSet(this.flags, StandOutFlags.FLAG_DECORATION_CLOSE_DISABLE)) {
            close.setVisibility(8);
        }
        if (Utils.isSet(this.flags, StandOutFlags.FLAG_DECORATION_MOVE_DISABLE)) {
            titlebar.setOnTouchListener(null);
        }
        if (Utils.isSet(this.flags, StandOutFlags.FLAG_DECORATION_RESIZE_DISABLE)) {
            corner.setVisibility(8);
        }
        return decorations;
    }

    void addFunctionality(View root) {
        if (!Utils.isSet(this.flags, StandOutFlags.FLAG_ADD_FUNCTIONALITY_RESIZE_DISABLE)) {
            View corner = root.findViewById(R.id.corner);
            if (corner != null) {
                corner.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        return Window.this.mContext.onTouchHandleResize(Window.this.id, Window.this, v, event);
                    }
                });
            }
        }
        if (!Utils.isSet(this.flags, StandOutFlags.FLAG_ADD_FUNCTIONALITY_DROP_DOWN_DISABLE)) {
            final View icon = root.findViewById(R.id.window_icon);
            if (icon != null) {
                icon.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        PopupWindow dropDown = Window.this.mContext.getDropDown(Window.this.id);
                        if (dropDown != null) {
                            dropDown.showAsDropDown(icon);
                        }
                    }
                });
            }
        }
    }

    void fixCompatibility(View root) {
        Queue<View> queue = new LinkedList();
        queue.add(root);
        while (true) {
            View view = (View) queue.poll();
            if (view != null) {
                if (view instanceof ViewGroup) {
                    ViewGroup group = (ViewGroup) view;
                    for (int i = 0; i < group.getChildCount(); i++) {
                        queue.add(group.getChildAt(i));
                    }
                }
            } else {
                return;
            }
        }
    }
}
