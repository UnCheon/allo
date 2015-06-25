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



public class NoFriendAdapter extends ArrayAdapter<Friend> {

    private int resId;
    private ArrayList<Friend> friend_list;


    private LayoutInflater Inflater;
    private Context context;
    public int listCount = 0;



    public NoFriendAdapter(Context context, int textViewResourceId, List<Friend> objects) {
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

        if (v == null) {
            v = Inflater.inflate(resId, null);

        }
        return v;
    }
}