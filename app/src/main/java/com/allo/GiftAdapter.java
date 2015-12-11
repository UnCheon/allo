package com.allo;


import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class GiftAdapter extends ArrayAdapter<Gift> {

    private int resId;
    private ArrayList<Gift> gift_list;

    private LayoutInflater Inflater;
    private Context context;
    public int listCount = 0;
    GiftFragment giftFragment;

    ProgressDialog pd = null;


    public GiftAdapter(Context context, int textViewResourceId, List<Gift> objects, GiftFragment giftFragment) {
        super(context, textViewResourceId, objects);
        this.context = context;
        resId = textViewResourceId;
        gift_list = (ArrayList<Gift>) objects;
        listCount = gift_list.size();
        Inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.giftFragment = giftFragment;

    }


    @Override
    public View getView(int position, View v, ViewGroup parent) {
        ViewHolder holder;
        if (v == null) {
            v = Inflater.inflate(resId, null);
            holder = new ViewHolder();
            holder.tv_sender_name = (TextView) v.findViewById(R.id.tv_sender_name);
            holder.tv_sent_time = (TextView) v.findViewById(R.id.tv_sent_time);
            holder.tv_title = (TextView) v.findViewById(R.id.tv_title);
            holder.tv_artist = (TextView) v.findViewById(R.id.tv_artist);
            holder.iv_allo = (ImageView) v.findViewById(R.id.iv_allo);
            holder.btn_get = (Button) v.findViewById(R.id.btn_get);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        final Gift gift = gift_list.get(position);


        if (gift != null) {
//            String time = "";
//            Log.i("created time", String.valueOf(gift.getSentTime()));
//            Log.i("current time", String.valueOf(System.currentTimeMillis()));
//            long d_time_mil = System.currentTimeMillis() - (gift.getSentTime());
//            long d_time_s = d_time_mil / 1000000;
//
//            int i_hour = (int)d_time_s/3600;
//
//            if (i_hour < 1){
//                i_hour = (int)d_time_s / 60;
//                time = String.valueOf(i_hour) + "분전";
//           }else{
//                time = String.valueOf(i_hour)+"시간전";
//            }


            holder.tv_sender_name.setText(gift.getSenderNickname());
            holder.tv_sent_time.setText(gift.getSentTime());
            holder.tv_title.setText(gift.getTitle());
            holder.tv_artist.setText(gift.getArtist());

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

            imageLoader.displayImage(gift.getThumbs(), holder.iv_allo, options);

            holder.btn_get.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getGift(gift);
                }
            });
        }
        return v;
    }


    private class ViewHolder {
        ImageView iv_allo;
        TextView tv_sender_name;
        TextView tv_sent_time;
        TextView tv_title;
        TextView tv_artist;
        Button btn_get;

    }


    private void getGift(Gift gift) {
        AsyncHttpClient myClient;

        myClient = new AsyncHttpClient();
        myClient.setTimeout(SingleToneData.getInstance().getTimeOutValue());
        PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
        myClient.setCookieStore(myCookieStore);

        String url = context.getString(R.string.url_get_gift);

        RequestParams params = new RequestParams();
        LoginUtils loginUtils = new LoginUtils(context);

        params.put("id", loginUtils.getId());
        params.put("pw", loginUtils.getPw());
        params.put("allo_id", gift.getId());


        pd = ProgressDialog.show(context, "", context.getString(R.string.wait_get), true);
        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pd.dismiss();
                Log.i("(get gift )", new String(responseBody));
                getGiftFinish(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pd.dismiss();
                Toast.makeText(context, context.getResources().getString(R.string.on_failure), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getGiftFinish(String st_response_body) {
        try {
            JSONObject jo_result = new JSONObject(st_response_body);
            String st_status = jo_result.getString("status");
            if (st_status.equals("success")) {
                giftFragment.onGetGiftSuccess();

            } else {
                ErrorHandler errorHandler = new ErrorHandler(context);
                errorHandler.handleErrorCode(jo_result);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}