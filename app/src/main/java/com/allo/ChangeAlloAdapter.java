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


public class ChangeAlloAdapter extends ArrayAdapter<Allo> {

    private int resId;
    private ArrayList<Allo> ar_allo;


    private LayoutInflater Inflater;
    private Context context;


    public ChangeAlloAdapter(Context context, int textViewResourceId, List<Allo> objects) {
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
            holder.iv_allo = (ImageView) v.findViewById(R.id.iv_allo);
            holder.tv_title = (TextView) v.findViewById(R.id.tv_title);
            holder.tv_artist = (TextView) v.findViewById(R.id.tv_artist);
            holder.iv_more = (ImageView) v.findViewById(R.id.iv_more);
            holder.ll_more = (LinearLayout) v.findViewById(R.id.ll_more);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        Allo mAllo= ar_allo.get(position);

        if (mAllo != null) {
            Log.i("allo tag", mAllo.getURL());

            holder.tv_title.setText(mAllo.getTitle());
            if (mAllo.getArtist().equals("null"))
                holder.tv_artist.setText("unknown");
            else
                holder.tv_artist.setText(mAllo.getArtist()+" ");

            if (mAllo.getThumbs() != null){
                DisplayImageOptions options = new DisplayImageOptions.Builder()
//                        .showImageOnLoading(R.drawable.ic_stub)
//                        .showImageForEmptyUri(R.drawable.ic_empty)
//                        .showImageOnFail(R.drawable.ic_error)
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .considerExifParams(true)
                        .displayer(new RoundedBitmapDisplayer(20)).build();
                ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.init(ImageLoaderConfiguration.createDefault(context));
                imageLoader.displayImage(mAllo.getThumbs(), holder.iv_allo, options);
            }else{
                holder.iv_allo.setImageResource(R.drawable.allo_logo);
            }

            final int play_position = position;

            holder.ll_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openMoreDialog(play_position);

                }
            });
        }
        return v;
    }
    private void openMoreDialog(final int position)
    {
        Allo allo = ar_allo.get(position);

        final CharSequence[] items = {"앨범 정보 보기", "현재 알로로 설정", "재생"};

        new AlertDialog.Builder(context)
                .setTitle(allo.getTitle())
                .setItems(items,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                                switch (i) {
                                    case 0:
                                        Log.i("Main List Click", "0");
                                        break;
                                    case 1:
                                        setMainAllo(position);
                                        break;
                                    case 2:
                                        Log.i("Main List Click", "2");
                                        break;
                                }
                            }
                        })
                .show();
    }

    private void setMainAllo(int position){
        Allo allo = ar_allo.get(position);
//        AlloHttpUtils alloHttpUtils = new AlloHttpUtils(context);
//        alloHttpUtils.setMainAllo(allo, mMyAlloFragment);
    }



    private class ViewHolder {

        TextView tv_title;
        TextView tv_artist;
        ImageView iv_allo;
        ImageView iv_more;
        LinearLayout ll_more;
    }
}