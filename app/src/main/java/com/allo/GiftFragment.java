package com.allo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by baek_uncheon on 2015. 7. 27..
 */
public class GiftFragment extends Fragment {
    Context context;


    ArrayList<Gift> al_gift;


    ImageView iv_back;

    ListView lv_gift;

    Tracker mTracker;


    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gift, container, false);

        AnalyticsApplication application = (AnalyticsApplication) ((Activity)context).getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("GiftFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        setLayout(view);
        setListener();

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        getFriend();

        return view;
    }

    private void setLayout(View view) {


        iv_back = (ImageView) view.findViewById(R.id.iv_back);

        lv_gift = (ListView) view.findViewById(R.id.lv_gift);

    }

    private void setListener() {


        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).openDrawerLayout();
            }
        });

        lv_gift.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
    }


    private void getFriend() {
        AsyncHttpClient myClient;

        myClient = new AsyncHttpClient();
        myClient.setTimeout(SingleToneData.getInstance().getTimeOutValue());
        PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
        myClient.setCookieStore(myCookieStore);

        String url = context.getString(R.string.url_gift_list);

        RequestParams params = new RequestParams();
        LoginUtils loginUtils = new LoginUtils(context);

        params.put("id", loginUtils.getId());
        params.put("pw", loginUtils.getPw());


        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("(friend fragment)", new String(responseBody));
                getGiftFinish(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, getResources().getString(R.string.on_failure), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getGiftFinish(String st_response_body) {
        try {
            JSONObject jo_result = new JSONObject(st_response_body);
            String st_status = jo_result.getString("status");
            if (st_status.equals("success")) {
                JSONObject jo_response = jo_result.getJSONObject("response");

                JSONArray ja_gift_list = jo_response.getJSONArray("gift_list");

                al_gift = new ArrayList<>();

                for (int i = 0; i < ja_gift_list.length(); i++) {
                    JSONObject jo_gift = ja_gift_list.getJSONObject(i);
                    Gift gift = new Gift();
                    if (jo_gift.has("sender_nickname"))
                        gift.setSenderNickname(jo_gift.getString("sender_nickname"));
                    if (jo_gift.has("sender_id"))
                        gift.setSenderId(jo_gift.getString("sender_id"));
                    if (jo_gift.has("msg"))
                        gift.setMsg(jo_gift.getString("msg"));
                    if (jo_gift.has("created"))
                        gift.setSentTime(jo_gift.getString("created"));
                    if (jo_gift.has("title"))
                        gift.setTitle(jo_gift.getString("title"));
                    if (jo_gift.has("artist"))
                        gift.setArtist(jo_gift.getString("artist"));
                    if (jo_gift.has("image"))
                        gift.setImage(jo_gift.getString("image"));
                    if (jo_gift.has("thumbs"))
                        gift.setThumbs(jo_gift.getString("thumbs"));
                    if (jo_gift.has("url"))
                        gift.setURL(jo_gift.getString("url"));
                    if (jo_gift.has("uid"))
                        gift.setId(jo_gift.getString("uid"));
                    if (jo_gift.has("duration"))
                        gift.setDuration(jo_gift.getInt("duration"));

                    al_gift.add(gift);
                }

            } else {
                ErrorHandler errorHandler = new ErrorHandler(context);
                errorHandler.handleErrorCode(jo_result);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        Log.i("size", String.valueOf(al_gift.size()));
        if (al_gift.size() == 0) {
            ArrayList<Friend> al_friend = new ArrayList<>();
            al_friend.add(new Friend());
            MyAlloNoFriendAdapter adapter = new MyAlloNoFriendAdapter(context, R.layout.activity_gift_intro, al_friend);
            lv_gift.setAdapter(adapter);
            lv_gift.setDividerHeight(0);
        } else {
            GiftAdapter adapter = new GiftAdapter(context, R.layout.gift_list_item, al_gift, GiftFragment.this);
            lv_gift.setAdapter(adapter);

            lv_gift.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Gift gift = al_gift.get(i);
//                AlloDialog alloDialog = new AlloDialog(context);
//                alloDialog.setType("gift");
//                alloDialog.setAllo(gift);
//                alloDialog.show();
                }
            });
        }


    }

    //    Callback
    public void onGetGiftSuccess() {


        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(context);
        alert_confirm.setTitle("선물 받기").setMessage("\n알로설정에서 선물 받은 알로를 사용할 수 있습니다.\n").setCancelable(false).setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getFriend();

                    }
                });
        AlertDialog alert = alert_confirm.create();
        alert.show();
    }

    @Override
    public void onStart(){
        super.onStart();
        GoogleAnalytics.getInstance(context).reportActivityStart((Activity) context);
    }

    @Override
    public void onStop(){
        super.onStop();
        GoogleAnalytics.getInstance(context).reportActivityStop((Activity) context);
    }
}
