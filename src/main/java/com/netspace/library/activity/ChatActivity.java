package com.netspace.library.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.components.ChatComponent;
import com.netspace.library.dialog.UserInfoDialog;
import com.netspace.library.ui.BaseActivity;
import com.netspace.library.ui.UI;
import com.netspace.library.utilities.Utilities;
import com.netspace.library.window.ChatWindow;
import com.netspace.pad.library.R;
import wei.mark.standout.StandOutWindow;

public class ChatActivity extends BaseActivity implements OnQueryTextListener {
    private static ChatComponent mChatComponent;
    private LinearLayout mContentLayout;
    private ChatComponent mCurrentChatComponent;

    public /* bridge */ /* synthetic */ View onCreateView(View view, String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(view, str, context, attributeSet);
    }

    public /* bridge */ /* synthetic */ View onCreateView(String str, Context context, AttributeSet attributeSet) {
        return super.onCreateView(str, context, attributeSet);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDoublePressReturn(true);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_bars).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setTitle("在线答疑");
        this.mContentLayout = (LinearLayout) findViewById(R.id.layoutContent);
        if (mChatComponent == null) {
            ChatComponent RootView = new ChatComponent(this);
            this.mContentLayout.addView(RootView, new LayoutParams(-1, -1));
            this.mCurrentChatComponent = RootView;
            return;
        }
        this.mContentLayout.addView(mChatComponent, new LayoutParams(-1, -1));
        this.mCurrentChatComponent = mChatComponent;
    }

    public static void setChatComponent(ChatComponent component) {
        mChatComponent = component;
    }

    protected void onDestroy() {
        if (mChatComponent != null) {
            ((ViewGroup) mChatComponent.getParent()).removeView(mChatComponent);
            mChatComponent = null;
        }
        super.onDestroy();
    }

    protected void onResume() {
        this.mCurrentChatComponent.setLocked(ChatComponent.getLocked());
        StandOutWindow.closeAll(this, ChatWindow.class);
        super.onResume();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        menu.findItem(R.id.action_airplay).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_tv).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        if (UI.mOnAirplayButton == null) {
            menu.findItem(R.id.action_airplay).setVisible(false);
        }
        menu.findItem(R.id.action_search).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_search).colorRes(R.color.toolbar).actionBarSize());
        if (menu.findItem(R.id.action_search) != null) {
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
            if (searchView != null) {
                searchView.setOnQueryTextListener(this);
                changeSearchViewTextColor(searchView);
            }
        }
        if (MyiBaseApplication.getCommonVariables().UserInfo.nUserType != 0) {
            menu.findItem(R.id.action_properties).setIcon(new IconDrawable((Context) this, FontAwesomeIcons.fa_address_card_o).color(Utilities.getThemeCustomColor(R.attr.toolbar_textcolor)).actionBarSize());
        } else {
            menu.removeItem(R.id.action_properties);
        }
        return true;
    }

    private void changeSearchViewTextColor(View view) {
        if (view == null) {
            return;
        }
        if (view instanceof TextView) {
            ((TextView) view).setHint("在当前聊天中搜索...");
            ((TextView) view).setBackgroundResource(R.drawable.apptheme_edit_text_holo_light);
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                changeSearchViewTextColor(viewGroup.getChildAt(i));
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Utilities.logMenuClick(item);
        if (item.getItemId() == R.id.action_airplay && UI.mOnAirplayButton != null) {
            UI.mOnAirplayButton.onClick(null, 0);
        }
        if (item.getItemId() == R.id.action_properties) {
            String szUserName = this.mCurrentChatComponent.getTargetJID().replace("myipad_", "").replace("_teacherpad", "");
            if (szUserName.isEmpty()) {
                Utilities.showAlertMessage(this, "没有属性", "请先选择一个学生后再点击。");
                return false;
            } else if (szUserName.indexOf("*") != -1) {
                Utilities.showAlertMessage(this, "没有属性", "当前没有针对班级聊天室的属性");
            } else if (UI.getCurrentActivity() != null && (UI.getCurrentActivity() instanceof AppCompatActivity)) {
                UserInfoDialog.showDialog((AppCompatActivity) UI.getCurrentActivity(), szUserName);
            }
        }
        if (item.getItemId() == 16908332) {
            finish();
        }
        return true;
    }

    public boolean onQueryTextChange(String arg0) {
        return false;
    }

    public boolean onQueryTextSubmit(String arg0) {
        this.mCurrentChatComponent.search(arg0);
        return true;
    }
}
