package com.netspace.teacherpad.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.teacherpad.R;
import com.netspace.teacherpad.structure.StudentAnswerImage;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class StudentAnswerImageAdapter extends ArrayAdapter<StudentAnswerImage> {
    private OnLongClickListener mLongClickListener;
    private OnClickListener mOnClickListener;
    private LayoutInflater m_LayoutInflater;
    private ArrayList<StudentAnswerImage> marrAnswerImages;

    public StudentAnswerImageAdapter(Context context, ArrayList<StudentAnswerImage> images, OnLongClickListener OnLongClickListener, OnClickListener OnClickListener) {
        super(context, 0, images);
        this.marrAnswerImages = images;
        this.mLongClickListener = OnLongClickListener;
        this.mOnClickListener = OnClickListener;
        this.m_LayoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        StudentAnswerImage info = (StudentAnswerImage) this.marrAnswerImages.get(position);
        if (convertView == null) {
            convertView = this.m_LayoutInflater.inflate(R.layout.listitem_studentanswerimage, parent, false);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.textViewStudentName);
        ImageView answerImage = (ImageView) convertView.findViewById(R.id.imageViewAnswerImage);
        String szURL = MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/GetTemporaryStorage?filename=" + info.szImageKey;
        textView.setText(info.szStudentName);
        Picasso.with(getContext()).load(szURL).resize(0, 500).into(answerImage);
        answerImage.setTag(info);
        answerImage.setOnClickListener(this.mOnClickListener);
        answerImage.setOnLongClickListener(this.mLongClickListener);
        return convertView;
    }
}
