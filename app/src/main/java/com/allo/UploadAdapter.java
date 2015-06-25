package com.allo;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class UploadAdapter extends ArrayAdapter<Allo> {

    private int resId;
    private ArrayList<Allo> allo_list;

    private LayoutInflater Inflater;
    Context mContext;
    public int listCount = 0;


    public UploadAdapter(Context context, int textViewResourceId, List<Allo> objects) {
        super(context, textViewResourceId, objects);
        mContext = context;
        resId = textViewResourceId;
        allo_list = (ArrayList<Allo>) objects;
        listCount = allo_list.size();
        Inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        ViewHolder holder;
        if (v == null) {
            v = Inflater.inflate(resId, null);
            holder = new ViewHolder();
            holder.fileIV = (ImageView) v.findViewById(R.id.fileIV);
            holder.fileTV = (TextView) v.findViewById(R.id.fileTV);
            holder.moreBtn = (ImageView) v.findViewById(R.id.moreBtn);
            holder.moreLayout= (LinearLayout) v.findViewById(R.id.moreLayout);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        Allo mAllo= allo_list.get(position);

        if (mAllo != null) {
            holder.fileTV.setText(mAllo.getTitle());
            final int play_position = position;
            holder.moreLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openMoreDialog(play_position);
                }
            });

        }
        return v;
    }


    private void openMoreDialog(final int position)
    {
        final Allo allo = allo_list.get(position);
        final CharSequence[] items = {"파일정보 보기", "파일 업로드하기", "파일 선물하기"};

        new AlertDialog.Builder(mContext)
                .setTitle(allo.getTitle())
                .setItems(items,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                                switch (i) {
                                    case 0:
                                        break;
                                    case 1:
                                        uploadFile(position);
                                        break;
                                    case 2:
                                        break;
                                }
                            }
                        })
                .show();
    }

    private void uploadFile(int position){
        Allo allo = allo_list.get(position);
        AlloHttpUtils alloHttpUtils = new AlloHttpUtils(mContext);
//        alloHttpUtils.setUploadFile(allo);

    }




    private class ViewHolder {

        ImageView fileIV;
        TextView fileTV;
        LinearLayout moreLayout;
        ImageView moreBtn;
    }
}