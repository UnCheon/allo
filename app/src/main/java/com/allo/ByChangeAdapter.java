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

import java.util.ArrayList;
import java.util.List;



public class ByChangeAdapter extends ArrayAdapter<Allo> {

    private int resId;
    private ArrayList<Allo> allo_list;


    private LayoutInflater Inflater;
    private Context context;
    public int listCount = 0;




    public ByChangeAdapter(Context context, int textViewResourceId, List<Allo> objects) {
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
            holder.tv_allo_title = (TextView) v.findViewById(R.id.tv_allo_title);
            holder.tv_allo_artist = (TextView) v.findViewById(R.id.tv_allo_artist);

            holder.ll_more = (LinearLayout) v.findViewById(R.id.ll_more);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        Allo mAllo= allo_list.get(position);

        if (mAllo != null) {
            Log.i("allo tag", mAllo.getURL());

            holder.tv_allo_title.setText(mAllo.getTitle());
            if (mAllo.getArtist().equals("null"))
                holder.tv_allo_artist.setText("");
            else
                holder.tv_allo_artist.setText(mAllo.getArtist()+" ");

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
        Allo allo = allo_list.get(position);


        final CharSequence[] items = {"앨범 정보 보기", "친구별 알로로 설정", "재생"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(allo.getTitle())
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                break;
                            case 1:
                                setFriendAllo(position);
                                break;
                            case 2:
                                break;
                        }
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();


    }

    private void setFriendAllo(int position){
        Allo allo = allo_list.get(position);
        AlloHttpUtils alloHttpUtils = new AlloHttpUtils(context);
        alloHttpUtils.setByFriendAllo(allo, context);
    }



    private class ViewHolder {

        TextView tv_allo_title;
        TextView tv_allo_artist;

        LinearLayout ll_more;
    }
}