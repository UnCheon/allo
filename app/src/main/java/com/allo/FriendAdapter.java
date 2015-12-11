package com.allo;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.ArrayList;
import java.util.List;


public class FriendAdapter extends ArrayAdapter<Friend> {

    private int resId;
    private ArrayList<Friend> friend_list;

    private LayoutInflater Inflater;
    private Context context;
    public int listCount = 0;


    public FriendAdapter(Context context, int textViewResourceId, List<Friend> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        resId = textViewResourceId;
        friend_list = (ArrayList<Friend>) objects;
        listCount = friend_list.size();
        Inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }


    @Override
    public View getView(int position, View v, ViewGroup parent) {
        ViewHolder holder;
        if (v == null) {
            v = Inflater.inflate(resId, null);
            holder = new ViewHolder();
            holder.tv_friend_name = (TextView) v.findViewById(R.id.tv_friend_name);
            holder.tv_title = (TextView) v.findViewById(R.id.tv_title);
            holder.tv_artist = (TextView) v.findViewById(R.id.tv_artist);
            holder.iv_allo = (ImageView) v.findViewById(R.id.iv_allo);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        Friend friend = friend_list.get(position);


        if (friend != null) {

            holder.tv_friend_name.setText(friend.getNickname());
            try {
                if (!(friend.getTitle().equals(""))) {
                    holder.tv_title.setText(friend.getTitle());
                    holder.tv_artist.setText(friend.getArtist());

                    DisplayImageOptions options = new DisplayImageOptions.Builder()
//                        .showImageOnLoading(R.drawable.ic_stub)
                            .showImageForEmptyUri(R.drawable.damienrice)
                            .showImageOnFail(R.drawable.damienrice)
                            .cacheInMemory(true)
                            .cacheOnDisk(true)
                            .considerExifParams(true)
                            .displayer(new RoundedBitmapDisplayer(0)).build();
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    if (!imageLoader.isInited())
                        imageLoader.init(ImageLoaderConfiguration.createDefault(context));

                    imageLoader.displayImage(friend.getThumbs(), holder.iv_allo, options);
                }
            } catch (Exception e) {

            }

        }
        return v;
    }


    private class ViewHolder {
        ImageView iv_allo;
        TextView tv_friend_name;
        TextView tv_title;
        TextView tv_artist;

    }
}