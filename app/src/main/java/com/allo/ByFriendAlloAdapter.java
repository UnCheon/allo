package com.allo;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;



public class ByFriendAlloAdapter extends ArrayAdapter<Friend> {

    private int resId;
    private ArrayList<Friend> friend_list;


    private LayoutInflater Inflater;
    private Context context;
    public int listCount = 0;


    public ByFriendAlloAdapter(Context context, int textViewResourceId, List<Friend> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        resId = textViewResourceId;
        friend_list = (ArrayList<Friend>) objects;
        listCount = friend_list.size();
        Inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount(){
        return super.getCount();
    }

    @Override
    public Friend getItem(int arg0) {
        return super.getItem(arg0);
    }

    @Override
    public long getItemId(int position){
        return super.getItemId(position);
    }



    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        ViewHolder holder;
        if (v == null) {
            v = Inflater.inflate(resId, null);
            holder = new ViewHolder();
            holder.tv_friend_name= (TextView) v.findViewById(R.id.tv_friend_name);
            holder.tv_friend_phone_number= (TextView) v.findViewById(R.id.tv_friend_phone_number);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        Friend mFriend = friend_list.get(position);

        if (mFriend != null) {
            holder.tv_friend_name.setText(mFriend.getNickname());
            holder.tv_friend_phone_number.setText(mFriend.getPhoneNumber());
        }
        return v;
    }



    private class ViewHolder {
        TextView tv_friend_name;
        TextView tv_friend_phone_number;
    }

}