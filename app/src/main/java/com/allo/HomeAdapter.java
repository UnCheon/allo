package com.allo;


import android.app.Activity;
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

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;



public class HomeAdapter extends ArrayAdapter<Friend> {

    private int resId;
    private ArrayList<Friend> friend_list;


    private LayoutInflater Inflater;
    private Context context;
    public int listCount = 0;
    ImageLoader imageLoader;

    private HomeFragment homeFragment;

    public HomeAdapter(Context context, int textViewResourceId, List<Friend> objects, HomeFragment homeFragment) {
        super(context, textViewResourceId, objects);
        this.homeFragment = homeFragment;
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
            holder.fName = (TextView) v.findViewById(R.id.fName);

            holder.fSongTV = (TextView) v.findViewById(R.id.fSongTV);
            holder.fSongInfoBtn = (ImageView) v.findViewById(R.id.fSongInfoBtn);
            holder.fSongInfoLayout = (LinearLayout) v.findViewById(R.id.fSongInfoLayout);
            holder.moreLayout = (LinearLayout) v.findViewById(R.id.moreLayout);

//            holder.fInfoLayout = (LinearLayout) v.findViewById(R.id.fInfoLayout);
//            holder.fTel = (TextView) v.findViewById(R.id.fTel);
//            holder.fSongArtistTV = (TextView) v.findViewById(R.id.fSongArtistTV);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        Friend mFriend = friend_list.get(position);

        if (mFriend != null) {
            holder.fName.setText(mFriend.getNickname());
//            holder.fTel.setText(mFriend.getPhoneNumber());

            if (mFriend.getAllo() != null){
                if(mFriend.getAllo().getArtist().length() < 1)
                    holder.fSongTV.setText(mFriend.getAllo().getTitle());
                else
                    holder.fSongTV.setText(mFriend.getAllo().getTitle() + " - " + mFriend.getAllo().getArtist());
            }

//            holder.fSongArtistTV.setText(mFriend.getRingSinger());




            final int play_position = position;
            holder.fSongInfoLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Friend friend = friend_list.get(play_position);
                    Allo allo;
                    allo = friend.getAllo();
                    homeFragment.onItemClickPlay(allo);

                }
            });

            holder.moreLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // morebtn
                    openMoreDialog(play_position);
                }
            });
        }
        return v;
    }

    private void callFriendDialog(int position){

        final Friend friend = friend_list.get(position);

        new AlertDialog.Builder(context)
                .setTitle(friend.getNickname()+"에게 전화를 거시겠습니까?")
                .setMessage(friend.getPhoneNumber())
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    // 확인 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+friend.getPhoneNumber()));
                        context.startActivity(intent);

                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    // 확인 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                })
                .show();
    }


    private void openMoreDialog(int position)
    {
        Friend friend = friend_list.get(position);

        final CharSequence[] items = {"전화 걸기", "앨범 정보 보기", "재생"};
        final int play_position = position;

        new AlertDialog.Builder(context)
                .setTitle(friend.getNickname())
                .setItems(items,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                                switch (i) {
                                    case 0:
                                        callFriendDialog(play_position);
                                        break;
                                    case 1:
                                        Log.i("Main List Click", "1");
                                        break;
                                    case 2:
                                        Log.i("Main List Click", "2");
                                        break;
                                }
                            }
                        })
                .show();
    }



    private class ViewHolder {
        TextView fName;
        //        TextView fTel;
        TextView fSongTV;
        //        TextView fSongArtistTV;
        ImageView fSongInfoBtn;
        ImageView moreBtn;
        LinearLayout fSongInfoLayout;
        //        LinearLayout fInfoLayout;
        LinearLayout moreLayout;
    }

}