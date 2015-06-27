package com.allo;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.ArrayList;
import java.util.List;


public class FriendAlloAdapter extends ArrayAdapter<Friend> {

    private int resId;
    private ArrayList<Friend> ar_friend;


    private LayoutInflater Inflater;
    private Context context;
    public int listCount = 0;

    MyAlloFragment mMyAlloFragment;



    public FriendAlloAdapter(Context context, int textViewResourceId, List<Friend> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        resId = textViewResourceId;
        ar_friend = (ArrayList<Friend>) objects;
        listCount = ar_friend.size();
        Inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


    }


    @Override
    public View getView(int position, View v, ViewGroup parent) {
        ViewHolder holder;
        if (v == null) {
            v = Inflater.inflate(resId, null);
            holder = new ViewHolder();

            holder.iv_allo = (ImageView) v.findViewById(R.id.iv_allo);
            holder.tv_nickname = (TextView) v.findViewById(R.id.tv_nickname);
            holder.tv_title = (TextView) v.findViewById(R.id.tv_title);
            holder.tv_artist = (TextView) v.findViewById(R.id.tv_artist);

            holder.moreBtn = (ImageView) v.findViewById(R.id.moreBtn);
            holder.moreLayout = (LinearLayout) v.findViewById(R.id.moreLayout);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        Friend friend= ar_friend.get(position);

        if (friend != null) {

            holder.tv_nickname.setText(friend.getNickname());

            Allo allo = friend.getAllo();
            if (allo.getThumbs() != null){
                holder.tv_title.setText(allo.getTitle());
                holder.tv_artist.setText(allo.getArtist());

                DisplayImageOptions options = new DisplayImageOptions.Builder()
//                        .showImageOnLoading(R.drawable.ic_stub)
                        .showImageForEmptyUri(R.drawable.damienrice)
                        .showImageOnFail(R.drawable.damienrice)
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .considerExifParams(true)
                        .displayer(new RoundedBitmapDisplayer(20)).build();
                ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.init(ImageLoaderConfiguration.createDefault(context));
                imageLoader.displayImage(allo.getThumbs(), holder.iv_allo, options);
            }else{
                holder.iv_allo.setImageResource(R.drawable.allo_logo);
            }

        }
        return v;
    }



    private class ViewHolder {

        TextView tv_nickname;
        TextView tv_title;
        TextView tv_artist;
        ImageView iv_allo;

        ImageView moreBtn;
        LinearLayout moreLayout;
    }
}