package com.allo;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.ArrayList;
import java.util.List;


public class FriendListAdapter extends ArrayAdapter<Friend> {

    private int resId;
    private ArrayList<Friend> ar_friend;


    private LayoutInflater Inflater;
    private Context context;


    public FriendListAdapter(Context context, int textViewResourceId, List<Friend> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        resId = textViewResourceId;
        ar_friend = (ArrayList<Friend>) objects;
        Inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public View getView(int position, View v, ViewGroup parent) {
        ViewHolder holder;
        if (v == null) {
            v = Inflater.inflate(resId, null);
            holder = new ViewHolder();
            holder.tv_nickname = (TextView)v.findViewById(R.id.tv_nickname);
            holder.tv_phone_number= (TextView)v.findViewById(R.id.tv_phone_number);
            holder.btn_select = (Button)v.findViewById(R.id.btn_select);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        Friend friend= ar_friend.get(position);

        if (friend != null) {
            holder.tv_nickname.setText(friend.getNickname());
            holder.tv_phone_number.setText(friend.getPhoneNumber());

            final int play_position = position;

            holder.btn_select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

        }
        return v;
    }





    private class ViewHolder {
        TextView tv_nickname;
        TextView tv_phone_number;
        Button btn_select;
    }
}