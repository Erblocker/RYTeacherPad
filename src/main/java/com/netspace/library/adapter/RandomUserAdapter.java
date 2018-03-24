package com.netspace.library.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.struct.UserClassInfo;
import com.netspace.library.struct.UserInfo;
import com.netspace.library.utilities.Utilities;
import com.netspace.pad.library.R;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUserAdapter extends BaseAdapter {
    private Context m_Context;
    private LayoutInflater m_LayoutInflater;
    private ArrayList<UserItem> m_arrData = new ArrayList();
    private ArrayList<Integer> marrRandStudentIDs = new ArrayList();

    public class UserItem {
        public boolean bChecked = false;
        public String szName;
        public String szUID;
    }

    public RandomUserAdapter(Context context) {
        this.m_Context = context;
        this.m_LayoutInflater = (LayoutInflater) this.m_Context.getSystemService("layout_inflater");
    }

    public void addRandomUser(String szUserClassGUID) {
        if (MyiBaseApplication.getCommonVariables().UserInfo.arrClasses.size() > 0) {
            Iterator it = MyiBaseApplication.getCommonVariables().UserInfo.arrClasses.iterator();
            while (it.hasNext()) {
                UserClassInfo UserClassInfo = (UserClassInfo) it.next();
                if (UserClassInfo.szClassGUID.equalsIgnoreCase(szUserClassGUID)) {
                    int nStudentID = -1;
                    if (this.marrRandStudentIDs.size() >= UserClassInfo.arrStudents.size()) {
                        Utilities.showAlertMessage(this.m_Context, "无法添加学生", "已经添加的全班的学生。");
                        return;
                    }
                    while (true) {
                        if (nStudentID != -1 && !Utilities.isInArray(this.marrRandStudentIDs, nStudentID)) {
                            break;
                        }
                        nStudentID = ThreadLocalRandom.current().nextInt(0, UserClassInfo.arrStudents.size());
                    }
                    this.marrRandStudentIDs.add(Integer.valueOf(nStudentID));
                    UserInfo userinfo = (UserInfo) UserClassInfo.arrStudents.get(nStudentID);
                    UserItem UserItem = new UserItem();
                    UserItem.szName = userinfo.szRealName;
                    UserItem.szUID = "myipad_" + userinfo.szUserName;
                    UserItem.bChecked = true;
                    this.m_arrData.add(UserItem);
                }
            }
            notifyDataSetChanged();
        }
    }

    public int getCount() {
        return this.m_arrData.size();
    }

    public Object getItem(int position) {
        return this.m_arrData.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final UserItem Data = (UserItem) this.m_arrData.get(position);
        if (convertView == null) {
            convertView = this.m_LayoutInflater.inflate(R.layout.listitem_recentuser, null);
        }
        TextView textTitle = (TextView) convertView.findViewById(R.id.textViewTitle);
        TextView textContent = (TextView) convertView.findViewById(R.id.textViewContent);
        CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.checkBox1);
        checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Data.bChecked = isChecked;
            }
        });
        checkbox.setChecked(Data.bChecked);
        textContent.setText(Data.szName);
        return convertView;
    }

    public ArrayList<UserItem> getData() {
        return this.m_arrData;
    }
}
