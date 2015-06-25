package com.allo;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class StoreAdapter extends ArrayAdapter<Allo> {

    private int resId;
    private ArrayList<Allo> allo_list;



    private LayoutInflater Inflater;
    private Context context;
    public int listCount = 0;
    public boolean check = false;



    public StoreAdapter(Context context, int textViewResourceId, List<Allo> objects ) {
        super(context, textViewResourceId, objects);
        this.context = context;
        resId = textViewResourceId;
        allo_list = (ArrayList<Allo>) objects;
        listCount = allo_list.size();
        Inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }


    @Override
    public View getView(int position, View v, ViewGroup parent) {
        ViewHolder holder;
        if (v == null) {
            v = Inflater.inflate(resId, null);
            holder = new ViewHolder();
            holder.imageBtn = (ImageView) v.findViewById(R.id.iv_allo_image);
            holder.rankTV = (TextView) v.findViewById(R.id.rankTV);
            holder.songTV = (TextView) v.findViewById(R.id.songTV);
            holder.artistTV = (TextView) v.findViewById(R.id.artistTV);
            holder.moreLayout = (LinearLayout) v.findViewById(R.id.moreLayout);
            holder.moreBtn = (ImageView) v.findViewById(R.id.moreBtn);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        Allo mAllo= allo_list.get(position);

        if (mAllo != null){
//            holder.rankTV.setText(mAllo.getRingRank());
            holder.songTV.setText(mAllo.getTitle());
            holder.artistTV.setText(mAllo.getArtist());

//            imageLoader.DisplayImage(mAllo.getRingCount(), holder.imageBtn);

            final int play_position = position;
            holder.moreLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openMoreDialog(play_position);


                }
            });
        }
/*
        if (mAllo != null && !holder.songTV.getTag().equals("wc")) {
            String path = mAllo.getRingCount()+".png";
            Log.i("path path", path);
            try{
                holder.songTV.setTag("wc");
                InputStream ims = context.getAssets().open(path);
                Log.i("path path", path);
                // load image as Drawable
                Drawable d = Drawable.createFromStream(ims, null);
                // set image to ImageView
                holder.imageBtn.setImageDrawable(d);
            }catch (Exception e){
                System.out.println(e);
            }
                    }
*/
        return v;
    }


    private void openMoreDialog(int position)
    {
        final Allo allo = allo_list.get(position);
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
        ImageView imageBtn;
        TextView rankTV;
        TextView songTV;
        TextView artistTV;
        LinearLayout moreLayout;
        ImageView moreBtn;
    }
}