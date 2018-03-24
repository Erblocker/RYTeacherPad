package net.sourceforge.opencamera;

import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;

public class MyPreferenceFragment extends PreferenceFragment {
    private static final String TAG = "MyPreferenceActivity";

    public void onCreate(Bundle savedInstanceState) {
        CharSequence[] entries;
        CharSequence[] values;
        int i;
        ListPreference lp;
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.opencamera_preferences);
        Bundle bundle = getArguments();
        int cameraId = bundle.getInt("cameraId");
        String camera_api = bundle.getString("camera_api");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean supports_auto_stabilise = bundle.getBoolean("supports_auto_stabilise");
        if (!bundle.getBoolean("supports_face_detection")) {
            ((PreferenceGroup) findPreference("preference_category_camera_effects")).removePreference(findPreference("preference_face_detection"));
        }
        int[] preview_widths = bundle.getIntArray("preview_widths");
        int[] preview_heights = bundle.getIntArray("preview_heights");
        int[] video_widths = bundle.getIntArray("video_widths");
        int[] video_heights = bundle.getIntArray("video_heights");
        int[] widths = bundle.getIntArray("resolution_widths");
        int[] heights = bundle.getIntArray("resolution_heights");
        if (widths == null || heights == null) {
            ((PreferenceGroup) findPreference("preference_screen_photo_settings")).removePreference(findPreference("preference_resolution"));
        } else {
            entries = new CharSequence[widths.length];
            values = new CharSequence[widths.length];
            for (i = 0; i < widths.length; i++) {
                entries[i] = widths[i] + " x " + heights[i] + " " + Preview.getAspectRatioMPString(widths[i], heights[i]);
                values[i] = widths[i] + " " + heights[i];
            }
            lp = (ListPreference) findPreference("preference_resolution");
            lp.setEntries(entries);
            lp.setEntryValues(values);
            String resolution_preference_key = MainActivity.getResolutionPreferenceKey(cameraId);
            lp.setValue(sharedPreferences.getString(resolution_preference_key, ""));
            lp.setKey(resolution_preference_key);
        }
        entries = new CharSequence[100];
        values = new CharSequence[100];
        for (i = 0; i < 100; i++) {
            entries[i] = (i + 1) + "%";
            values[i] = (i + 1);
        }
        lp = (ListPreference) findPreference("preference_quality");
        lp.setEntries(entries);
        lp.setEntryValues(values);
        String[] video_quality = bundle.getStringArray("video_quality");
        String[] video_quality_string = bundle.getStringArray("video_quality_string");
        if (video_quality == null || video_quality_string == null) {
            ((PreferenceGroup) findPreference("preference_screen_video_settings")).removePreference(findPreference("preference_video_quality"));
        } else {
            entries = new CharSequence[video_quality.length];
            values = new CharSequence[video_quality.length];
            for (i = 0; i < video_quality.length; i++) {
                entries[i] = video_quality_string[i];
                values[i] = video_quality[i];
            }
            lp = (ListPreference) findPreference("preference_video_quality");
            lp.setEntries(entries);
            lp.setEntryValues(values);
            String video_quality_preference_key = MainActivity.getVideoQualityPreferenceKey(cameraId);
            lp.setValue(sharedPreferences.getString(video_quality_preference_key, ""));
            lp.setKey(video_quality_preference_key);
        }
        if (!bundle.getBoolean("supports_force_video_4k") || video_quality == null || video_quality_string == null) {
            ((PreferenceGroup) findPreference("preference_screen_video_settings")).removePreference(findPreference("preference_force_video_4k"));
        }
        if (!bundle.getBoolean("supports_video_stabilization")) {
            ((PreferenceGroup) findPreference("preference_screen_video_settings")).removePreference(findPreference("preference_video_stabilization"));
        }
        boolean can_disable_shutter_sound = bundle.getBoolean("can_disable_shutter_sound");
        if (VERSION.SDK_INT < 17 || !can_disable_shutter_sound) {
            ((PreferenceGroup) findPreference("preference_screen_camera_controls_more")).removePreference(findPreference("preference_shutter_sound"));
        }
        if (VERSION.SDK_INT < 19) {
            ((PreferenceGroup) findPreference("preference_screen_gui")).removePreference(findPreference("preference_immersive_mode"));
        }
    }

    public void onResume() {
        super.onResume();
        TypedArray array = getActivity().getTheme().obtainStyledAttributes(new int[]{16842801});
        getView().setBackgroundColor(array.getColor(0, -16777216));
        array.recycle();
    }
}
