package com.farproc.wifi.connecter;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TwoLineListItem;
import java.util.List;

public class TestWifiScan extends ListActivity {
    private OnItemClickListener mItemOnClick = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            TestWifiScan.launchWifiConnecter(TestWifiScan.this, (ScanResult) TestWifiScan.this.mScanResults.get(position));
        }
    };
    private BaseAdapter mListAdapter = new BaseAdapter() {
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null || !(convertView instanceof TwoLineListItem)) {
                convertView = View.inflate(TestWifiScan.this.getApplicationContext(), 17367044, null);
            }
            ((TwoLineListItem) convertView).getText1().setText(((ScanResult) TestWifiScan.this.mScanResults.get(position)).SSID);
            ((TwoLineListItem) convertView).getText1().setTextColor(-16777216);
            ((TwoLineListItem) convertView).getText2().setText(String.format("%s  %d", new Object[]{result.BSSID, Integer.valueOf(result.level)}));
            return convertView;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public Object getItem(int position) {
            return null;
        }

        public int getCount() {
            return TestWifiScan.this.mScanResults == null ? 0 : TestWifiScan.this.mScanResults.size();
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.net.wifi.SCAN_RESULTS")) {
                TestWifiScan.this.mScanResults = TestWifiScan.this.mWifiManager.getScanResults();
                TestWifiScan.this.mListAdapter.notifyDataSetChanged();
                TestWifiScan.this.mWifiManager.startScan();
            }
        }
    };
    private List<ScanResult> mScanResults;
    private WifiManager mWifiManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mWifiManager = (WifiManager) getSystemService("wifi");
        setListAdapter(this.mListAdapter);
        setTitle("Wifi 设置");
        getListView().setOnItemClickListener(this.mItemOnClick);
    }

    public void onResume() {
        super.onResume();
        registerReceiver(this.mReceiver, new IntentFilter("android.net.wifi.SCAN_RESULTS"));
        this.mWifiManager.startScan();
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(this.mReceiver);
    }

    private static void launchWifiConnecter(Activity activity, ScanResult hotspot) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_HOTSPOT, hotspot);
        try {
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }
}
