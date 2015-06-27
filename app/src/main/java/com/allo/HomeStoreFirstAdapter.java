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

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.ArrayList;
import java.util.List;


public class HomeStoreFirstAdapter extends ArrayAdapter<Allo> {

    private int resId;
    private ArrayList<Allo> ar_allo;


    private LayoutInflater Inflater;
    private Context context;
    public int listCount = 0;



    public HomeStoreFirstAdapter(Context context, int textViewResourceId, List<Allo> objects ) {
        super(context, textViewResourceId, objects);
        this.context = context;
        resId = textViewResourceId;
        ar_allo = (ArrayList<Allo>) objects;
        listCount = ar_allo.size();
        Inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


    }


    @Override
    public View getView(int position, View v, ViewGroup parent) {
        ViewHolder holder;
        if (v == null) {
            v = Inflater.inflate(resId, null);
            holder = new ViewHolder();
            holder.iv_allo = (ImageView) v.findViewById(R.id.iv_allo);
            holder.rankTV = (TextView) v.findViewById(R.id.rankTV);
            holder.songTV = (TextView) v.findViewById(R.id.songTV);
            holder.artistTV = (TextView) v.findViewById(R.id.artistTV);
            holder.moreLayout = (LinearLayout) v.findViewById(R.id.moreLayout);
            holder.moreBtn = (ImageView) v.findViewById(R.id.moreBtn);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        Allo mAllo= ar_allo.get(position);

        if (mAllo != null){
            holder.rankTV.setText(String.valueOf(position+1));
            holder.songTV.setText(mAllo.getTitle());
            holder.artistTV.setText(mAllo.getArtist());

            final int play_position = position;
            holder.moreLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openMoreDialog(play_position);


                }
            });
        }


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
        imageLoader.displayImage(mAllo.getThumbs(), holder.iv_allo, options);
//        if (mAllo.getThumbs() != null){
//            DisplayImageOptions options = new DisplayImageOptions.Builder()
////                        .showImageOnLoading(R.drawable.ic_stub)
//                        .showImageForEmptyUri(R.drawable.damienrice)
//                        .showImageOnFail(R.drawable.damienrice)
//                    .cacheInMemory(true)
//                    .cacheOnDisk(true)
//                    .considerExifParams(true)
//                    .displayer(new RoundedBitmapDisplayer(20)).build();
//            ImageLoader imageLoader = ImageLoader.getInstance();
//            imageLoader.init(ImageLoaderConfiguration.createDefault(context));
//            imageLoader.displayImage(mAllo.getThumbs(), holder.iv_allo, options);
//        }else{
//            holder.iv_allo.setImageResource(R.drawable.allo_logo);
//        }



        return v;
    }


    private void openMoreDialog(int position)
    {
        final Allo allo = ar_allo.get(position);
        final CharSequence[] items = {"앨범 정보 보기", "구매하기", "선물하기"};

        new AlertDialog.Builder(context)
                .setTitle(allo.getRingTitle())
                .setItems(items,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                                switch (i) {
                                    case 0:
                                        clickAlbumInfo(allo);
                                        break;
                                    case 1:
                                        clickBuy(allo);
                                        break;
                                    case 2:
                                        clickGift(allo);
                                        break;
                                }


                            }
                        })
                .show();
    }

    private void clickAlbumInfo(Allo allo){

    }
    private void clickBuy(Allo allo){
        AlloHttpUtils alloHttpUtils = new AlloHttpUtils(context);
        alloHttpUtils.buyRing(allo);
    }
    private void clickGift(Allo allo){

    }



    private class ViewHolder {
        ImageView iv_allo;
        TextView rankTV;
        TextView songTV;
        TextView artistTV;
        LinearLayout moreLayout;
        ImageView moreBtn;
    }

}