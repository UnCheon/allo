package com.allo;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import java.util.List;


public class MyAlloNoFriendAdapter extends ArrayAdapter<Friend> {

    private int resId;

    private LayoutInflater Inflater;
    private Context context;
    public int listCount = 1;


    public MyAlloNoFriendAdapter(Context context, int textViewResourceId, List<Friend> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        resId = textViewResourceId;

        Inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }


    @Override
    public View getView(int position, View v, ViewGroup parent) {
        ViewHolder holder;
        if (v == null) {
            v = Inflater.inflate(resId, null);
            holder = new ViewHolder();
            holder.btn_invite = (Button) v.findViewById(R.id.btn_invite);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        holder.btn_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KaKaoInvite kaKaoInvite = new KaKaoInvite(context);
                kaKaoInvite.sendMessage();

            }
        });

        return v;
    }


    private class ViewHolder {
        Button btn_invite;
    }
}