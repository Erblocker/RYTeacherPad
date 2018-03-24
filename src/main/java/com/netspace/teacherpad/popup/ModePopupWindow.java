package com.netspace.teacherpad.popup;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.NovaIcons;
import com.netspace.library.utilities.Utilities;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.ScreenDisplayActivity;
import com.netspace.teacherpad.dialog.StartClassControlUnit;

public class ModePopupWindow extends BasicPopupWindow implements OnClickListener {
    private ImageButton mButtonEmptyAll;
    private ImageButton mButtonErase;
    private ImageButton mButtonMouse;
    private ImageButton mButtonWhiteboard;
    private View mContentView;

    public ModePopupWindow(Context context) {
        super(context);
        initView();
    }

    public void initView() {
        super.initView();
        setWidth(Utilities.dpToPixel(220, this.mContext));
        setHeight(Utilities.dpToPixel(140, this.mContext));
        this.mContentView = this.mLayoutInflater.inflate(R.layout.popup_mode, null);
        this.mButtonWhiteboard = (ImageButton) this.mContentView.findViewById(R.id.buttonWhiteBoard);
        this.mButtonMouse = (ImageButton) this.mContentView.findViewById(R.id.buttonMouse);
        this.mButtonErase = (ImageButton) this.mContentView.findViewById(R.id.buttonErase);
        this.mButtonEmptyAll = (ImageButton) this.mContentView.findViewById(R.id.buttonClearWhiteBoard);
        this.mButtonWhiteboard.setOnClickListener(this);
        this.mButtonMouse.setOnClickListener(this);
        this.mButtonErase.setOnClickListener(this);
        this.mButtonEmptyAll.setOnClickListener(this);
        this.mButtonWhiteboard.setImageDrawable(new IconDrawable(this.mContext, FontAwesomeIcons.fa_pencil_square_o).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        this.mButtonMouse.setImageDrawable(new IconDrawable(this.mContext, FontAwesomeIcons.fa_mouse_pointer).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        this.mButtonErase.setImageDrawable(new IconDrawable(this.mContext, NovaIcons.nova_icon_eraser).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        this.mButtonEmptyAll.setImageDrawable(new IconDrawable(this.mContext, FontAwesomeIcons.fa_trash_o).color(Utilities.getThemeCustomColor(R.attr.float_button_border_color)).actionBarSize());
        this.mContentLayout.addView(this.mContentView, -1, -1);
        if (ScreenDisplayActivity.getCursorMode()) {
            this.mButtonMouse.setSelected(true);
        } else if (ScreenDisplayActivity.getPenMode()) {
            this.mButtonWhiteboard.setSelected(true);
        } else {
            this.mButtonErase.setSelected(true);
        }
    }

    public void onClick(View v) {
        new StartClassControlUnit(this.mContext).onClick(v);
        if (v.getId() != R.id.buttonClearWhiteBoard) {
            this.mButtonWhiteboard.setSelected(false);
            this.mButtonMouse.setSelected(false);
            this.mButtonErase.setSelected(false);
            v.setSelected(true);
        }
    }
}
