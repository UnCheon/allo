package com.allo;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.ArrayList;

/**
 * Created by florentchampigny on 24/04/15.
 */
public class StoreRecyclerViewAdapter extends RecyclerView.Adapter<StoreRecyclerViewAdapter.ListItemViewHolder> {

    ArrayList<Allo> contents;

    Context context;

    static final int TYPE_HEADER = 0;
    static final int TYPE_CELL = 1;

    ProgressDialog pd = null;


    ListItemViewHolder holder_current;

    public StoreRecyclerViewAdapter(Context context, ArrayList<Allo> contents) {
        this.context = context;
        this.contents = contents;
    }


    @Override
    public int getItemCount() {
        return contents.size();
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_store_item, parent, false);
        return new ListItemViewHolder(itemView);
    }

    public void onPrepared(Allo allo) {

        holder_current.ll_store_item.setEnabled(true);

        AlloStoreDialog alloDialog = new AlloStoreDialog(context);
        alloDialog.setAllo(allo);
//        alloDialog.is_allo_prepared = true;

        if (allo.is_ucc) {
            alloDialog.setType("UCC");
            Log.i("is_ucc", "is_ucc");
        } else {
            alloDialog.setType("STORE");
            Log.i("is_ucc", "is_ucc no ");
        }
        pd.dismiss();

        alloDialog.show();
    }

    public void onPrepareFailed(){
        pd.dismiss();
        Toast.makeText(context, context.getResources().getString(R.string.prepare_fail), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBindViewHolder(final ListItemViewHolder holder, int position) {

        if (pd == null) {
            pd = new ProgressDialog(context);
            pd.setTitle("");
            pd.setMessage(context.getString(R.string.wait_prepare));
        }


        final Allo allo = contents.get(position);
        holder.tv_rank.setText(String.valueOf(position + 1));
        holder.tv_title.setText(allo.getTitle());
        holder.tv_artist.setText(allo.getArtist());


        holder.tv_time.setText(AlloUtils.getInstance().millisecondToTimeString(allo.getDuration()));


        holder.ll_store_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder_current = holder;
                holder.ll_store_item.setEnabled(false);

                pd.show();
                PlayAllo.getInstance().setStoreRecyclerViewAdapter(StoreRecyclerViewAdapter.this);
                PlayAllo.getInstance().setType("STORE");
                PlayAllo.getInstance().setAlloPrepare(allo);
            }
        });


        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.allo_logo)
                .showImageForEmptyUri(R.drawable.allo_logo)
                .showImageOnFail(R.drawable.allo_logo)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(0)).build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited())
            imageLoader.init(ImageLoaderConfiguration.createDefault(context));

        imageLoader.displayImage(allo.getThumbs(), holder.iv_allo, options);

    }

    public final static class ListItemViewHolder extends RecyclerView.ViewHolder {
        LinearLayout ll_store_item;
        TextView tv_rank;
        TextView tv_title;
        TextView tv_artist;
        TextView tv_time;

        ImageView iv_allo;

        public ListItemViewHolder(View itemView) {
            super(itemView);
            ll_store_item = (LinearLayout) itemView.findViewById(R.id.ll_store_item);
            tv_rank = (TextView) itemView.findViewById(R.id.tv_rank);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            tv_artist = (TextView) itemView.findViewById(R.id.tv_artist);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);

            iv_allo = (ImageView) itemView.findViewById(R.id.iv_allo);
        }
    }
}