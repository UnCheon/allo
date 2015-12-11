package com.allo;


import android.content.Context;
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
            holder.tv_time = (TextView) v.findViewById(R.id.tv_time);

            holder.moreLayout = (LinearLayout) v.findViewById(R.id.moreLayout);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        Allo mAllo = allo_list.get(position);

        if (mAllo != null) {
            holder.fileTV.setText(mAllo.getTitle());
            holder.tv_time.setText(AlloUtils.getInstance().millisecondToTimeString(mAllo.getDuration()));

        }
        return v;
    }


    private class ViewHolder {

        ImageView fileIV;
        TextView fileTV;
        LinearLayout moreLayout;
        TextView tv_time;
    }
}