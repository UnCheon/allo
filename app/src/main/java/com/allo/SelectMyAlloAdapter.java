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


public class SelectMyAlloAdapter extends ArrayAdapter<Allo> {

    private int resId;
    private ArrayList<Allo> ar_allo;


    private LayoutInflater Inflater;
    private Context context;


    public SelectMyAlloAdapter(Context context, int textViewResourceId, List<Allo> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        resId = textViewResourceId;
        ar_allo = (ArrayList<Allo>) objects;
        Inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public View getView(int position, View v, ViewGroup parent) {
        ViewHolder holder;
        if (v == null) {
            v = Inflater.inflate(resId, null);
            holder = new ViewHolder();
            holder.tv_title = (TextView) v.findViewById(R.id.tv_title);
            holder.tv_artist = (TextView) v.findViewById(R.id.tv_artist);
            holder.tv_time = (TextView) v.findViewById(R.id.tv_time);
            holder.iv_allo = (ImageView) v.findViewById(R.id.iv_allo);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        Allo mAllo = ar_allo.get(position);

        if (mAllo != null) {

            holder.tv_title.setText(mAllo.getTitle());
            holder.tv_artist.setText(mAllo.getArtist());


            holder.tv_time.setText(AlloUtils.getInstance().millisecondToTimeString(mAllo.getDuration()));


            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.allo_logo)
                    .showImageForEmptyUri(R.drawable.allo_logo)
                    .showImageOnFail(R.drawable.allo_logo)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .displayer(new RoundedBitmapDisplayer(0)).build();
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(ImageLoaderConfiguration.createDefault(context));
            imageLoader.displayImage(mAllo.getThumbs(), holder.iv_allo, options);

        }
        return v;
    }


    private class ViewHolder {

        TextView tv_title;
        TextView tv_artist;
        TextView tv_time;
        ImageView iv_allo;


    }
}