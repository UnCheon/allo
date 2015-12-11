package com.allo;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class InviteAdapter extends ArrayAdapter<Contact> {

    private int resId;
    private ArrayList<Contact> ar_contact;

    private LayoutInflater Inflater;
    private Context context;
    public int listCount = 0;


    public InviteAdapter(Context context, int textViewResourceId, List<Contact> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        resId = textViewResourceId;
        ar_contact = (ArrayList<Contact>) objects;
        listCount = ar_contact.size();
        Inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }


    @Override
    public View getView(int position, View v, ViewGroup parent) {
        final ViewHolder holder;
        if (v == null) {
            v = Inflater.inflate(resId, null);
            holder = new ViewHolder();
            holder.tv_friend_name = (TextView) v.findViewById(R.id.tv_friend_name);
            holder.tv_phone_number = (TextView) v.findViewById(R.id.tv_phone_number);
            holder.btn_invite = (Button) v.findViewById(R.id.btn_invite);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        final Contact contact = ar_contact.get(position);
        LoginUtils loginUtils = new LoginUtils(context);

        final String msg = "기다림을 즐겨라! 무료 컬러링 알로를 사용해보세요!" +
                "'" + loginUtils.getNickname() + "'" +
                "님이 자신이 설정한 통화연결음 " +
                "'" + loginUtils.getMyAlloTitle() + "'" +
                "를 들어보라고 초대하셨습니다. " +
                "market //details id=com.allo";


        if (contact != null) {
            holder.tv_friend_name.setText(contact.getNickname());
            holder.tv_phone_number.setText(contact.getPhonenum());
            holder.btn_invite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    SmsManager mSmsManager = SmsManager.getDefault();
//                    mSmsManager.sendTextMessage(contact.getPhonenum(), null, msg, null, null);

                }
            });
        }
        return v;
    }


    private class ViewHolder {
        TextView tv_friend_name;
        TextView tv_phone_number;
        Button btn_invite;

    }
}