package net.sourceforge.opencamera;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import net.sourceforge.opencamera.CameraController.Size;

public class PopupView extends LinearLayout {
    private static final String TAG = "PopupView";
    private int burst_mode_index = -1;
    private int picture_size_index = -1;
    private Map<String, View> popup_buttons = new Hashtable();
    private int timer_index = -1;
    private int video_size_index = -1;

    private abstract class ArrayOptionsPopupListener {
        public abstract int onClickNext();

        public abstract int onClickPrev();

        private ArrayOptionsPopupListener() {
        }
    }

    private abstract class ButtonOptionsPopupListener {
        public abstract void onClick(String str);

        private ButtonOptionsPopupListener() {
        }
    }

    /* renamed from: net.sourceforge.opencamera.PopupView$5 */
    class AnonymousClass5 extends ArrayOptionsPopupListener {
        final Handler handler = new Handler();
        Runnable update_runnable;
        private final /* synthetic */ MainActivity val$main_activity;
        private final /* synthetic */ List val$picture_sizes;
        private final /* synthetic */ Preview val$preview;

        AnonymousClass5(List list, final MainActivity mainActivity, Preview preview) {
            this.val$picture_sizes = list;
            this.val$main_activity = mainActivity;
            this.val$preview = preview;
            super();
            this.update_runnable = new Runnable() {
                public void run() {
                    mainActivity.updateForSettings("");
                }
            };
        }

        private void update() {
            if (PopupView.this.picture_size_index != -1) {
                Size new_size = (Size) this.val$picture_sizes.get(PopupView.this.picture_size_index);
                String resolution_string = new_size.width + " " + new_size.height;
                Editor editor = PreferenceManager.getDefaultSharedPreferences(this.val$main_activity).edit();
                editor.putString(MainActivity.getResolutionPreferenceKey(this.val$preview.getCameraId()), resolution_string);
                editor.apply();
                this.handler.removeCallbacks(this.update_runnable);
                this.handler.postDelayed(this.update_runnable, 400);
            }
        }

        public int onClickPrev() {
            if (PopupView.this.picture_size_index == -1 || PopupView.this.picture_size_index <= 0) {
                return -1;
            }
            PopupView popupView = PopupView.this;
            popupView.picture_size_index = popupView.picture_size_index - 1;
            update();
            return PopupView.this.picture_size_index;
        }

        public int onClickNext() {
            if (PopupView.this.picture_size_index == -1 || PopupView.this.picture_size_index >= this.val$picture_sizes.size() - 1) {
                return -1;
            }
            PopupView popupView = PopupView.this;
            popupView.picture_size_index = popupView.picture_size_index + 1;
            update();
            return PopupView.this.picture_size_index;
        }
    }

    /* renamed from: net.sourceforge.opencamera.PopupView$6 */
    class AnonymousClass6 extends ArrayOptionsPopupListener {
        final Handler handler = new Handler();
        Runnable update_runnable;
        private final /* synthetic */ MainActivity val$main_activity;
        private final /* synthetic */ Preview val$preview;
        private final /* synthetic */ List val$video_sizes;

        AnonymousClass6(List list, final MainActivity mainActivity, Preview preview) {
            this.val$video_sizes = list;
            this.val$main_activity = mainActivity;
            this.val$preview = preview;
            super();
            this.update_runnable = new Runnable() {
                public void run() {
                    mainActivity.updateForSettings("");
                }
            };
        }

        private void update() {
            if (PopupView.this.video_size_index != -1) {
                String quality = (String) this.val$video_sizes.get(PopupView.this.video_size_index);
                Editor editor = PreferenceManager.getDefaultSharedPreferences(this.val$main_activity).edit();
                editor.putString(MainActivity.getVideoQualityPreferenceKey(this.val$preview.getCameraId()), quality);
                editor.apply();
                this.handler.removeCallbacks(this.update_runnable);
                this.handler.postDelayed(this.update_runnable, 400);
            }
        }

        public int onClickPrev() {
            if (PopupView.this.video_size_index == -1 || PopupView.this.video_size_index <= 0) {
                return -1;
            }
            PopupView popupView = PopupView.this;
            popupView.video_size_index = popupView.video_size_index - 1;
            update();
            return PopupView.this.video_size_index;
        }

        public int onClickNext() {
            if (PopupView.this.video_size_index == -1 || PopupView.this.video_size_index >= this.val$video_sizes.size() - 1) {
                return -1;
            }
            PopupView popupView = PopupView.this;
            popupView.video_size_index = popupView.video_size_index + 1;
            update();
            return PopupView.this.video_size_index;
        }
    }

    public PopupView(Context context) {
        super(context);
        setOrientation(1);
        MainActivity main_activity = (MainActivity) getContext();
        Preview preview = main_activity.getPreview();
        final Preview preview2 = preview;
        final MainActivity mainActivity = main_activity;
        addButtonOptionsToPopup(preview.getSupportedFlashValues(), R.array.flash_icons, R.array.flash_values, getResources().getString(R.string.flash_mode), preview.getCurrentFlashValue(), "TEST_FLASH", new ButtonOptionsPopupListener() {
            public void onClick(String option) {
                preview2.updateFlash(option);
                mainActivity.closePopup();
            }
        });
        if (!preview.isVideo() || !preview.isTakingPhoto()) {
            preview2 = preview;
            mainActivity = main_activity;
            addButtonOptionsToPopup(preview.getSupportedFocusValues(), R.array.focus_mode_icons, R.array.focus_mode_values, getResources().getString(R.string.focus_mode), preview.getCurrentFocusValue(), "TEST_FOCUS", new ButtonOptionsPopupListener() {
                public void onClick(String option) {
                    preview2.updateFocus(option, false, true);
                    mainActivity.closePopup();
                }
            });
            List<String> supported_isos = preview.getSupportedISOs();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
            String str = "ISO";
            final MainActivity mainActivity2 = main_activity;
            addButtonOptionsToPopup(supported_isos, -1, -1, str, sharedPreferences.getString(MainActivity.getISOPreferenceKey(), "auto"), "TEST_ISO", new ButtonOptionsPopupListener() {
                public void onClick(String option) {
                    Editor editor = PreferenceManager.getDefaultSharedPreferences(mainActivity2).edit();
                    editor.putString(MainActivity.getISOPreferenceKey(), option);
                    editor.apply();
                    mainActivity2.updateForSettings("ISO: " + option);
                    mainActivity2.closePopup();
                }
            });
            if (preview.getCameraController() != null) {
                addRadioOptionsToPopup(preview.getSupportedWhiteBalances(), getResources().getString(R.string.white_balance), MainActivity.getWhiteBalancePreferenceKey(), preview.getCameraController().getDefaultWhiteBalance(), "TEST_WHITE_BALANCE");
                addRadioOptionsToPopup(preview.getSupportedSceneModes(), getResources().getString(R.string.scene_mode), MainActivity.getSceneModePreferenceKey(), preview.getCameraController().getDefaultSceneMode(), "TEST_SCENE_MODE");
                addRadioOptionsToPopup(preview.getSupportedColorEffects(), getResources().getString(R.string.color_effect), MainActivity.getColorEffectPreferenceKey(), preview.getCameraController().getDefaultColorEffect(), "TEST_COLOR_EFFECT");
            }
            if (main_activity.supportsAutoStabilise()) {
                View checkBox = new CheckBox(main_activity);
                checkBox.setText(getResources().getString(R.string.preference_auto_stabilise));
                checkBox.setTextColor(-1);
                checkBox.setChecked(sharedPreferences.getBoolean(MainActivity.getAutoStabilisePreferenceKey(), false));
                mainActivity2 = main_activity;
                final Preview preview3 = preview;
                checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Editor editor = PreferenceManager.getDefaultSharedPreferences(mainActivity2).edit();
                        editor.putBoolean(MainActivity.getAutoStabilisePreferenceKey(), isChecked);
                        editor.apply();
                        preview3.showToast(mainActivity2.changed_auto_stabilise_toast, new StringBuilder(String.valueOf(PopupView.this.getResources().getString(R.string.preference_auto_stabilise))).append(": ").append(PopupView.this.getResources().getString(isChecked ? R.string.on : R.string.off)).toString());
                        mainActivity2.closePopup();
                    }
                });
                addView(checkBox);
            }
            List<Size> picture_sizes = preview.getSupportedPictureSizes();
            this.picture_size_index = preview.getCurrentPictureSizeIndex();
            List<String> picture_size_strings = new ArrayList();
            for (Size picture_size : picture_sizes) {
                picture_size_strings.add(picture_size.width + " x " + picture_size.height + " " + Preview.getMPString(picture_size.width, picture_size.height));
            }
            addArrayOptionsToPopup(picture_size_strings, getResources().getString(R.string.preference_resolution), this.picture_size_index, new AnonymousClass5(picture_sizes, main_activity, preview));
            List<String> video_sizes = preview.getSupportedVideoQuality();
            this.video_size_index = preview.getCurrentVideoQualityIndex();
            List<String> video_size_strings = new ArrayList();
            for (String video_size : video_sizes) {
                video_size_strings.add(preview.getCamcorderProfileDescriptionShort(video_size));
            }
            addArrayOptionsToPopup(video_size_strings, getResources().getString(R.string.video_quality), this.video_size_index, new AnonymousClass6(video_sizes, main_activity, preview));
            String[] timer_values = getResources().getStringArray(R.array.preference_timer_values);
            String[] timer_entries = getResources().getStringArray(R.array.preference_timer_entries);
            this.timer_index = Arrays.asList(timer_values).indexOf(sharedPreferences.getString(MainActivity.getTimerPreferenceKey(), "0"));
            if (this.timer_index == -1) {
                this.timer_index = 0;
            }
            final String[] strArr = timer_values;
            mainActivity = main_activity;
            addArrayOptionsToPopup(Arrays.asList(timer_entries), getResources().getString(R.string.preference_timer), this.timer_index, new ArrayOptionsPopupListener() {
                private void update() {
                    if (PopupView.this.timer_index != -1) {
                        String new_timer_value = strArr[PopupView.this.timer_index];
                        Editor editor = PreferenceManager.getDefaultSharedPreferences(mainActivity).edit();
                        editor.putString(MainActivity.getTimerPreferenceKey(), new_timer_value);
                        editor.apply();
                    }
                }

                public int onClickPrev() {
                    if (PopupView.this.timer_index == -1 || PopupView.this.timer_index <= 0) {
                        return -1;
                    }
                    PopupView popupView = PopupView.this;
                    popupView.timer_index = popupView.timer_index - 1;
                    update();
                    return PopupView.this.timer_index;
                }

                public int onClickNext() {
                    if (PopupView.this.timer_index == -1 || PopupView.this.timer_index >= strArr.length - 1) {
                        return -1;
                    }
                    PopupView popupView = PopupView.this;
                    popupView.timer_index = popupView.timer_index + 1;
                    update();
                    return PopupView.this.timer_index;
                }
            });
            String[] burst_mode_values = getResources().getStringArray(R.array.preference_burst_mode_values);
            String[] burst_mode_entries = getResources().getStringArray(R.array.preference_burst_mode_entries);
            this.burst_mode_index = Arrays.asList(burst_mode_values).indexOf(sharedPreferences.getString(MainActivity.getBurstModePreferenceKey(), "1"));
            if (this.burst_mode_index == -1) {
                this.burst_mode_index = 0;
            }
            strArr = burst_mode_values;
            mainActivity = main_activity;
            addArrayOptionsToPopup(Arrays.asList(burst_mode_entries), getResources().getString(R.string.preference_burst_mode), this.burst_mode_index, new ArrayOptionsPopupListener() {
                private void update() {
                    if (PopupView.this.burst_mode_index != -1) {
                        String new_burst_mode_value = strArr[PopupView.this.burst_mode_index];
                        Editor editor = PreferenceManager.getDefaultSharedPreferences(mainActivity).edit();
                        editor.putString(MainActivity.getBurstModePreferenceKey(), new_burst_mode_value);
                        editor.apply();
                    }
                }

                public int onClickPrev() {
                    if (PopupView.this.burst_mode_index == -1 || PopupView.this.burst_mode_index <= 0) {
                        return -1;
                    }
                    PopupView popupView = PopupView.this;
                    popupView.burst_mode_index = popupView.burst_mode_index - 1;
                    update();
                    return PopupView.this.burst_mode_index;
                }

                public int onClickNext() {
                    if (PopupView.this.burst_mode_index == -1 || PopupView.this.burst_mode_index >= strArr.length - 1) {
                        return -1;
                    }
                    PopupView popupView = PopupView.this;
                    popupView.burst_mode_index = popupView.burst_mode_index + 1;
                    update();
                    return PopupView.this.burst_mode_index;
                }
            });
        }
    }

    private void addButtonOptionsToPopup(List<String> supported_options, int icons_id, int values_id, String string, String current_value, String test_key, ButtonOptionsPopupListener listener) {
        if (supported_options != null) {
            long time_s = System.currentTimeMillis();
            View linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(0);
            String[] icons = icons_id != -1 ? getResources().getStringArray(icons_id) : null;
            String[] values = values_id != -1 ? getResources().getStringArray(values_id) : null;
            float scale = getResources().getDisplayMetrics().density;
            int total_width = 280;
            Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);
            int dpHeight = ((int) (((float) outMetrics.heightPixels) / scale)) - 50;
            if (280 > dpHeight) {
                total_width = dpHeight;
            }
            int button_width_dp = total_width / supported_options.size();
            boolean use_scrollview = false;
            if (button_width_dp < 40) {
                button_width_dp = 40;
                use_scrollview = true;
            }
            View current_view = null;
            for (String supported_option : supported_options) {
                View view;
                int resource = -1;
                if (!(icons == null || values == null)) {
                    int index = -1;
                    for (int i = 0; i < values.length && index == -1; i++) {
                        if (values[i].equals(supported_option)) {
                            index = i;
                        }
                    }
                    if (index != -1) {
                        resource = getResources().getIdentifier(icons[index], null, getContext().getApplicationContext().getPackageName());
                    }
                }
                int padding;
                if (resource != -1) {
                    linearLayout = new ImageButton(getContext());
                    view = linearLayout;
                    linearLayout.addView(view);
                    Bitmap bm = ((MainActivity) getContext()).getPreloadedBitmap(resource);
                    if (bm != null) {
                        linearLayout.setImageBitmap(bm);
                    }
                    linearLayout.setScaleType(ScaleType.FIT_CENTER);
                    padding = (int) ((10.0f * scale) + 0.5f);
                    view.setPadding(padding, padding, padding, padding);
                } else {
                    View button = new Button(getContext());
                    view = button;
                    linearLayout.addView(view);
                    if (string.equalsIgnoreCase("ISO") && supported_option.length() >= 4 && supported_option.substring(0, 4).equalsIgnoreCase("ISO_")) {
                        button.setText(new StringBuilder(String.valueOf(string)).append("\n").append(supported_option.substring(4)).toString());
                    } else if (string.equalsIgnoreCase("ISO") && supported_option.length() >= 3 && supported_option.substring(0, 3).equalsIgnoreCase("ISO")) {
                        button.setText(new StringBuilder(String.valueOf(string)).append("\n").append(supported_option.substring(3)).toString());
                    } else {
                        button.setText(new StringBuilder(String.valueOf(string)).append("\n").append(supported_option).toString());
                    }
                    button.setTextSize(1, 12.0f);
                    button.setTextColor(-1);
                    padding = (int) ((0.0f * scale) + 0.5f);
                    view.setPadding(padding, padding, padding, padding);
                }
                LayoutParams params = view.getLayoutParams();
                params.width = (int) ((((float) button_width_dp) * scale) + 0.5f);
                params.height = (int) ((50.0f * scale) + 0.5f);
                view.setLayoutParams(params);
                view.setContentDescription(string);
                if (supported_option.equals(current_value)) {
                    view.setAlpha(1.0f);
                    current_view = view;
                } else {
                    view.setAlpha(0.6f);
                }
                final String str = supported_option;
                final ButtonOptionsPopupListener buttonOptionsPopupListener = listener;
                view.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        buttonOptionsPopupListener.onClick(str);
                    }
                });
                this.popup_buttons.put(new StringBuilder(String.valueOf(test_key)).append("_").append(supported_option).toString(), view);
            }
            if (use_scrollview) {
                linearLayout = new HorizontalScrollView(getContext());
                linearLayout.addView(linearLayout);
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams((int) ((((float) total_width) * scale) + 0.5f), -2));
                addView(linearLayout);
                if (current_view != null) {
                    final View final_current_view = current_view;
                    final View view2 = linearLayout;
                    getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                        public void onGlobalLayout() {
                            view2.scrollTo(final_current_view.getLeft(), 0);
                        }
                    });
                    return;
                }
                return;
            }
            addView(linearLayout);
        }
    }

    private void addRadioOptionsToPopup(List<String> supported_options, String title, String preference_key, String default_option, String test_key) {
        if (supported_options != null) {
            final MainActivity main_activity = (MainActivity) getContext();
            TextView text_view = new TextView(getContext());
            text_view.setText(title);
            text_view.setTextColor(-1);
            text_view.setGravity(17);
            text_view.setTextSize(1, 8.0f);
            addView(text_view);
            RadioGroup rg = new RadioGroup(getContext());
            rg.setOrientation(1);
            this.popup_buttons.put(test_key, rg);
            String current_option = PreferenceManager.getDefaultSharedPreferences(main_activity).getString(preference_key, default_option);
            for (final String supported_option : supported_options) {
                RadioButton button = new RadioButton(getContext());
                button.setText(supported_option);
                button.setTextColor(-1);
                if (supported_option.equals(current_option)) {
                    button.setChecked(true);
                } else {
                    button.setChecked(false);
                }
                rg.addView(button);
                button.setContentDescription(supported_option);
                final String str = preference_key;
                final String str2 = title;
                button.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Editor editor = PreferenceManager.getDefaultSharedPreferences(main_activity).edit();
                        editor.putString(str, supported_option);
                        editor.apply();
                        main_activity.updateForSettings(str2 + ": " + supported_option);
                        main_activity.closePopup();
                    }
                });
                this.popup_buttons.put(new StringBuilder(String.valueOf(test_key)).append("_").append(supported_option).toString(), button);
            }
            addView(rg);
        }
    }

    private void addArrayOptionsToPopup(List<String> supported_options, String title, int current_index, ArrayOptionsPopupListener listener) {
        if (supported_options != null && current_index != -1) {
            TextView text_view = new TextView(getContext());
            text_view.setText(title);
            text_view.setTextColor(-1);
            text_view.setGravity(17);
            text_view.setTextSize(1, 8.0f);
            addView(text_view);
            LinearLayout ll2 = new LinearLayout(getContext());
            ll2.setOrientation(0);
            final TextView resolution_text_view = new TextView(getContext());
            resolution_text_view.setText((CharSequence) supported_options.get(current_index));
            resolution_text_view.setTextColor(-1);
            resolution_text_view.setGravity(17);
            resolution_text_view.setLayoutParams(new LinearLayout.LayoutParams(-2, -2, 1.0f));
            float scale = getResources().getDisplayMetrics().density;
            final Button prev_button = new Button(getContext());
            ll2.addView(prev_button);
            prev_button.setText("<");
            prev_button.setTextSize(1, 12.0f);
            int padding = (int) ((0.0f * scale) + 0.5f);
            prev_button.setPadding(padding, padding, padding, padding);
            LayoutParams vg_params = prev_button.getLayoutParams();
            vg_params.width = (int) ((60.0f * scale) + 0.5f);
            vg_params.height = (int) ((50.0f * scale) + 0.5f);
            prev_button.setLayoutParams(vg_params);
            prev_button.setVisibility(current_index > 0 ? 0 : 4);
            ll2.addView(resolution_text_view);
            final Button next_button = new Button(getContext());
            ll2.addView(next_button);
            next_button.setText(">");
            next_button.setTextSize(1, 12.0f);
            next_button.setPadding(padding, padding, padding, padding);
            vg_params = next_button.getLayoutParams();
            vg_params.width = (int) ((60.0f * scale) + 0.5f);
            vg_params.height = (int) ((50.0f * scale) + 0.5f);
            next_button.setLayoutParams(vg_params);
            next_button.setVisibility(current_index < supported_options.size() + -1 ? 0 : 4);
            final ArrayOptionsPopupListener arrayOptionsPopupListener = listener;
            final List<String> list = supported_options;
            prev_button.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    int i = 0;
                    int new_index = arrayOptionsPopupListener.onClickPrev();
                    if (new_index != -1) {
                        int i2;
                        resolution_text_view.setText((CharSequence) list.get(new_index));
                        Button button = prev_button;
                        if (new_index > 0) {
                            i2 = 0;
                        } else {
                            i2 = 4;
                        }
                        button.setVisibility(i2);
                        Button button2 = next_button;
                        if (new_index >= list.size() - 1) {
                            i = 4;
                        }
                        button2.setVisibility(i);
                    }
                }
            });
            arrayOptionsPopupListener = listener;
            list = supported_options;
            next_button.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    int i = 0;
                    int new_index = arrayOptionsPopupListener.onClickNext();
                    if (new_index != -1) {
                        int i2;
                        resolution_text_view.setText((CharSequence) list.get(new_index));
                        Button button = prev_button;
                        if (new_index > 0) {
                            i2 = 0;
                        } else {
                            i2 = 4;
                        }
                        button.setVisibility(i2);
                        Button button2 = next_button;
                        if (new_index >= list.size() - 1) {
                            i = 4;
                        }
                        button2.setVisibility(i);
                    }
                }
            });
            addView(ll2);
        }
    }

    void close() {
        this.popup_buttons.clear();
    }

    View getPopupButton(String key) {
        return (View) this.popup_buttons.get(key);
    }
}
