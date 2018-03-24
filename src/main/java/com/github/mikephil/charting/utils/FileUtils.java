package com.github.mikephil.charting.utils;

import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.protocol.HTTP;

public class FileUtils {
    private static final String LOG = "MPChart-FileUtils";

    public static List<Entry> loadEntriesFromFile(String path) {
        File file = new File(Environment.getExternalStorageDirectory(), path);
        List<Entry> entries = new ArrayList();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                String[] split = line.split("#");
                if (split.length <= 2) {
                    entries.add(new Entry(Float.parseFloat(split[0]), Integer.parseInt(split[1])));
                } else {
                    float[] vals = new float[(split.length - 1)];
                    for (int i = 0; i < vals.length; i++) {
                        vals[i] = Float.parseFloat(split[i]);
                    }
                    entries.add(new BarEntry(vals, Integer.parseInt(split[split.length - 1])));
                }
            }
        } catch (IOException e) {
            Log.e(LOG, e.toString());
        }
        return entries;
    }

    public static List<Entry> loadEntriesFromAssets(AssetManager am, String path) {
        IOException e;
        Throwable th;
        List<Entry> entries = new ArrayList();
        BufferedReader bufferedReader = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(am.open(path), HTTP.UTF_8));
            try {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    String[] split = line.split("#");
                    if (split.length <= 2) {
                        entries.add(new Entry(Float.parseFloat(split[0]), Integer.parseInt(split[1])));
                    } else {
                        float[] vals = new float[(split.length - 1)];
                        for (int i = 0; i < vals.length; i++) {
                            vals[i] = Float.parseFloat(split[i]);
                        }
                        entries.add(new BarEntry(vals, Integer.parseInt(split[split.length - 1])));
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                        bufferedReader = reader;
                    } catch (IOException e2) {
                        Log.e(LOG, e2.toString());
                    }
                    return entries;
                }
                bufferedReader = reader;
            } catch (IOException e3) {
                e2 = e3;
                bufferedReader = reader;
            } catch (Throwable th2) {
                th = th2;
                bufferedReader = reader;
            }
        } catch (IOException e4) {
            e2 = e4;
            try {
                Log.e(LOG, e2.toString());
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e22) {
                        Log.e(LOG, e22.toString());
                    }
                }
                return entries;
            } catch (Throwable th3) {
                th = th3;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e222) {
                        Log.e(LOG, e222.toString());
                    }
                }
                throw th;
            }
        }
        return entries;
    }

    public static void saveToSdCard(List<Entry> entries, String path) {
        File saved = new File(Environment.getExternalStorageDirectory(), path);
        if (!saved.exists()) {
            try {
                saved.createNewFile();
            } catch (IOException e) {
                Log.e(LOG, e.toString());
            }
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(saved, true));
            for (Entry e2 : entries) {
                buf.append(e2.getVal() + "#" + e2.getXIndex());
                buf.newLine();
            }
            buf.close();
        } catch (IOException e3) {
            Log.e(LOG, e3.toString());
        }
    }

    public static List<BarEntry> loadBarEntriesFromAssets(AssetManager am, String path) {
        IOException e;
        Throwable th;
        List<BarEntry> entries = new ArrayList();
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(am.open(path), HTTP.UTF_8));
            try {
                for (String line = reader2.readLine(); line != null; line = reader2.readLine()) {
                    String[] split = line.split("#");
                    entries.add(new BarEntry(Float.parseFloat(split[0]), Integer.parseInt(split[1])));
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                        reader = reader2;
                    } catch (IOException e2) {
                        Log.e(LOG, e2.toString());
                    }
                    return entries;
                }
                reader = reader2;
            } catch (IOException e3) {
                e2 = e3;
                reader = reader2;
            } catch (Throwable th2) {
                th = th2;
                reader = reader2;
            }
        } catch (IOException e4) {
            e2 = e4;
            try {
                Log.e(LOG, e2.toString());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e22) {
                        Log.e(LOG, e22.toString());
                    }
                }
                return entries;
            } catch (Throwable th3) {
                th = th3;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e222) {
                        Log.e(LOG, e222.toString());
                    }
                }
                throw th;
            }
        }
        return entries;
    }
}
