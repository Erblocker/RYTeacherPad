package com.foxit.uiextensions.security.certificate;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.foxit.uiextensions.R;
import com.foxit.uiextensions.controls.dialog.AppDialogManager;
import com.foxit.uiextensions.controls.dialog.MatchDialog.DialogListener;
import com.foxit.uiextensions.controls.dialog.UIMatchDialog;
import com.foxit.uiextensions.utils.AppDisplay;

public class CertificateDetailDialog extends UIMatchDialog {
    private static final int CERTDETAILITEMSCOUNT = 5;
    public static final int PERMDLG_TYPE_DECRYPT = 1;
    public static final int PERMDLG_TYPE_ENCRYPT = 0;
    private InfoAdapter mCertDetailAdapter = new InfoAdapter();
    private CertificateFileInfo mCertInfo;
    private Context mContext;
    private int mPermDialogType;
    private View mView;

    class InfoAdapter extends BaseAdapter {

        class TextViewHolder {
            TextView name;
            TextView value;

            TextViewHolder() {
            }
        }

        InfoAdapter() {
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            TextViewHolder holder;
            if (convertView == null) {
                holder = new TextViewHolder();
                convertView = View.inflate(CertificateDetailDialog.this.mContext, R.layout.rv_security_information_certlist_item, null);
                holder.name = (TextView) convertView.findViewById(R.id.rv_security_information_certlist_name);
                holder.value = (TextView) convertView.findViewById(R.id.rv_security_information_certlist_value);
                convertView.setTag(holder);
                if (AppDisplay.getInstance(CertificateDetailDialog.this.mContext).isPad()) {
                    LayoutParams LP = new LayoutParams(-1, -1);
                    LP.height = CertificateDetailDialog.this.mContext.getResources().getDimensionPixelSize(R.dimen.ux_list_item_height_1l_pad);
                    convertView.setLayoutParams(LP);
                }
            } else {
                holder = (TextViewHolder) convertView.getTag();
            }
            switch (position) {
                case 0:
                    holder.name.setText(R.string.rv_security_information_certlist_serialnumber);
                    holder.value.setText(CertificateDetailDialog.this.mCertInfo.certificateInfo.serialNumber);
                    break;
                case 1:
                    holder.name.setText(R.string.rv_security_information_certlist_publisher);
                    holder.value.setText(CertificateDetailDialog.this.mCertInfo.certificateInfo.publisher);
                    break;
                case 2:
                    holder.name.setText(R.string.rv_security_information_certlist_startdate);
                    holder.value.setText(CertificateDetailDialog.this.mCertInfo.certificateInfo.startDate);
                    break;
                case 3:
                    holder.name.setText(R.string.rv_security_information_certlist_expiringdate);
                    holder.value.setText(CertificateDetailDialog.this.mCertInfo.certificateInfo.expiringDate);
                    break;
                case 4:
                    holder.name.setText(R.string.rv_security_information_certlist_email);
                    holder.value.setText(CertificateDetailDialog.this.mCertInfo.certificateInfo.emailAddress);
                    break;
            }
            return convertView;
        }

        public long getItemId(int position) {
            return 0;
        }

        public Object getItem(int position) {
            return null;
        }

        public int getCount() {
            return 5;
        }
    }

    public CertificateDetailDialog(Context context, boolean showTopBarShadow) {
        super(context, showTopBarShadow);
        this.mContext = context;
    }

    public void init(int DlgType, CertificateFileInfo info) {
        this.mPermDialogType = DlgType;
        this.mCertInfo = info;
        initPermissions(info.permCode);
        createView();
    }

    private View createView() {
        this.mView = View.inflate(this.mContext, R.layout.rv_security_information, null);
        RelativeLayout permly = (RelativeLayout) this.mView.findViewById(R.id.rv_security_information_prm_ly);
        LinearLayout tably = (LinearLayout) this.mView.findViewById(R.id.rv_security_information_tab_ly);
        TextView permLabel = (TextView) this.mView.findViewById(R.id.rv_security_information_prmtitle);
        TextView certLabel = (TextView) this.mView.findViewById(R.id.rv_security_information_detailtitle);
        final ListView permListView = (ListView) this.mView.findViewById(R.id.rv_security_information_listp);
        final ListView certListView = (ListView) this.mView.findViewById(R.id.rv_security_information_listc);
        final View permLine = this.mView.findViewById(R.id.rv_security_information_prmline);
        final View detailLine = this.mView.findViewById(R.id.rv_security_information_detailline);
        certListView.setAdapter(this.mCertDetailAdapter);
        if (this.mPermDialogType == 1) {
            permly.setVisibility(8);
            tably.setVisibility(8);
            permListView.setVisibility(8);
            certListView.setVisibility(0);
        } else {
            permly.setVisibility(0);
            tably.setVisibility(0);
            certListView.setVisibility(8);
            detailLine.setVisibility(4);
        }
        if (AppDisplay.getInstance(this.mContext).isPad()) {
            permLabel.setTextColor(this.mView.getResources().getColor(R.color.ux_bg_color_toolbar_colour));
            certLabel.setTextColor(this.mView.getResources().getColor(R.color.ux_bg_color_toolbar_colour));
            permLine.setBackgroundColor(this.mView.getResources().getColor(R.color.ux_bg_color_toolbar_colour));
            detailLine.setBackgroundColor(this.mView.getResources().getColor(R.color.ux_bg_color_toolbar_colour));
            tably.setBackgroundColor(this.mView.getResources().getColor(R.color.ux_color_white));
        } else {
            permLabel.setTextColor(this.mView.getResources().getColor(R.color.ux_bg_color_toolbar_light));
            certLabel.setTextColor(this.mView.getResources().getColor(R.color.ux_bg_color_toolbar_light));
            permLine.setBackgroundColor(this.mView.getResources().getColor(R.color.ux_bg_color_toolbar_light));
            detailLine.setBackgroundColor(this.mView.getResources().getColor(R.color.ux_bg_color_toolbar_light));
            tably.setBackgroundColor(this.mView.getResources().getColor(R.color.ux_bg_color_toolbar_colour));
        }
        permLabel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!permListView.isShown()) {
                    certListView.setVisibility(8);
                    permListView.setVisibility(0);
                    detailLine.setVisibility(4);
                    permLine.setVisibility(0);
                }
            }
        });
        final ListView listView = certListView;
        final ListView listView2 = permListView;
        final View view = permLine;
        final View view2 = detailLine;
        certLabel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!listView.isShown()) {
                    listView2.setVisibility(8);
                    listView.setVisibility(0);
                    view.setVisibility(4);
                    view2.setVisibility(0);
                }
            }
        });
        setContentView(this.mView);
        if (this.mPermDialogType == 1) {
            setTitle(this.mContext.getString(R.string.rv_security_information_certlist_title));
        } else {
            setTitle(this.mContext.getString(R.string.rv_certlist_note));
        }
        setBackButtonVisible(0);
        setButton(4);
        setListener(new DialogListener() {
            public void onResult(long btType) {
                if (btType == 4) {
                    if (CertificateDetailDialog.this.mPermDialogType == 0) {
                        CertificateDetailDialog.this.mCertInfo.permCode = CertificateDetailDialog.this.getCustomPermission();
                    }
                    AppDialogManager.getInstance().dismiss(CertificateDetailDialog.this);
                }
            }

            public void onBackClick() {
            }
        });
        return this.mView;
    }

    private void initPermissions(int permission) {
    }

    private int getCustomPermission() {
        return 0;
    }
}
