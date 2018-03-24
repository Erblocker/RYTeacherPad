package net.sourceforge.opencamera;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FolderChooserDialog extends DialogFragment {
    private static final String TAG = "FolderChooserFragment";
    private File current_folder = null;
    private AlertDialog folder_dialog = null;
    private ListView list = null;

    private class FileWrapper implements Comparable<FileWrapper> {
        private File file = null;
        private boolean is_parent = false;

        FileWrapper(File file, boolean is_parent) {
            this.file = file;
            this.is_parent = is_parent;
        }

        public String toString() {
            if (this.is_parent) {
                return FolderChooserDialog.this.getResources().getString(R.string.parent_folder);
            }
            return this.file.getName();
        }

        @SuppressLint({"DefaultLocale"})
        public int compareTo(FileWrapper o) {
            if (this.is_parent) {
                return -1;
            }
            if (o.isParent()) {
                return 1;
            }
            return this.file.getName().toLowerCase().compareTo(o.getFile().getName().toLowerCase());
        }

        File getFile() {
            return this.file;
        }

        private boolean isParent() {
            return this.is_parent;
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        File new_folder = MainActivity.getImageFolder(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("preference_save_location", "OpenCamera"));
        this.list = new ListView(getActivity());
        this.list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FolderChooserDialog.this.refreshList(((FileWrapper) parent.getItemAtPosition(position)).getFile());
            }
        });
        this.folder_dialog = new Builder(getActivity()).setView(this.list).setPositiveButton(R.string.use_folder, null).setNeutralButton(R.string.new_folder, null).setNegativeButton(17039360, null).create();
        this.folder_dialog.setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialog_interface) {
                FolderChooserDialog.this.folder_dialog.getButton(-1).setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        if (FolderChooserDialog.this.useFolder()) {
                            FolderChooserDialog.this.folder_dialog.dismiss();
                        }
                    }
                });
                FolderChooserDialog.this.folder_dialog.getButton(-3).setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        FolderChooserDialog.this.newFolder();
                    }
                });
            }
        });
        if (new_folder.exists() || !new_folder.mkdirs()) {
            refreshList(new_folder);
        } else {
            refreshList(new_folder);
        }
        if (this.current_folder == null) {
            refreshList(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
            if (this.current_folder == null) {
                refreshList(new File("/"));
            }
        }
        return this.folder_dialog;
    }

    private void refreshList(File new_folder) {
        if (new_folder != null) {
            File[] files = (File[]) null;
            try {
                files = new_folder.listFiles();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (files == null) {
                Toast.makeText(getActivity(), new StringBuilder(String.valueOf(getResources().getString(R.string.cant_access_folder))).append(":\n").append(new_folder.getAbsolutePath()).toString(), 0).show();
                return;
            }
            List<FileWrapper> listed_files = new ArrayList();
            if (new_folder.getParentFile() != null) {
                listed_files.add(new FileWrapper(new_folder.getParentFile(), true));
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    listed_files.add(new FileWrapper(file, false));
                }
            }
            Collections.sort(listed_files);
            this.list.setAdapter(new ArrayAdapter(getActivity(), 17367043, listed_files));
            this.current_folder = new_folder;
            this.folder_dialog.setTitle(this.current_folder.getAbsolutePath());
        }
    }

    private boolean canWrite() {
        try {
            if (this.current_folder != null && this.current_folder.canWrite()) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    private boolean useFolder() {
        if (this.current_folder == null) {
            return false;
        }
        if (canWrite()) {
            File base_folder = MainActivity.getBaseFolder();
            String new_save_location = this.current_folder.getAbsolutePath();
            if (this.current_folder.getParentFile() != null && this.current_folder.getParentFile().equals(base_folder)) {
                new_save_location = this.current_folder.getName();
            }
            Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
            editor.putString(MainActivity.getSaveLocationPreferenceKey(), new_save_location);
            editor.apply();
            return true;
        }
        Toast.makeText(getActivity(), R.string.cant_write_folder, 0).show();
        return false;
    }

    private void newFolder() {
        if (this.current_folder != null) {
            if (canWrite()) {
                final EditText edit_text = new EditText(getActivity());
                edit_text.setSingleLine();
                edit_text.setFilters(new InputFilter[]{new InputFilter() {
                    String disallowed = "|\\?*<\":>";

                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            if (this.disallowed.indexOf(source.charAt(i)) != -1) {
                                return "";
                            }
                        }
                        return null;
                    }
                }});
                new Builder(getActivity()).setTitle(R.string.enter_new_folder).setView(edit_text).setPositiveButton(17039370, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (edit_text.getText().length() != 0) {
                            try {
                                File new_folder = new File(new StringBuilder(String.valueOf(FolderChooserDialog.this.current_folder.getAbsolutePath())).append(File.separator).append(edit_text.getText().toString()).toString());
                                if (new_folder.exists()) {
                                    Toast.makeText(FolderChooserDialog.this.getActivity(), R.string.folder_exists, 0).show();
                                } else if (new_folder.mkdirs()) {
                                    FolderChooserDialog.this.refreshList(FolderChooserDialog.this.current_folder);
                                } else {
                                    Toast.makeText(FolderChooserDialog.this.getActivity(), R.string.failed_create_folder, 0).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(FolderChooserDialog.this.getActivity(), R.string.failed_create_folder, 0).show();
                            }
                        }
                    }
                }).setNegativeButton(17039360, null).create().show();
                return;
            }
            Toast.makeText(getActivity(), R.string.cant_write_folder, 0).show();
        }
    }

    public void onResume() {
        super.onResume();
        refreshList(this.current_folder);
    }

    public File getCurrentFolder() {
        return this.current_folder;
    }
}
