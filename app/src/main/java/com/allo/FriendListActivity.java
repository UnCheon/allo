package com.allo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class FriendListActivity extends Activity {
    ImageView iv_back;
    ListView lv_friend;

    Button btn_select;

    ArrayList<Friend> ar_friend = new ArrayList<Friend>();
    ImageLoader imageLoader;
    DisplayImageOptions options;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        setLayout();

        setInstance();
        getFriendList();
    }

    private void setInstance() {

        options = new DisplayImageOptions.Builder()
//                        .showImageOnLoading(R.drawable.ic_stub)
//                        .showImageForEmptyUri(R.drawable.ic_empty)
//                        .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(20)).build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));
    }


    private void setLayout() {
        iv_back = (ImageView) findViewById(R.id.iv_back);
        lv_friend = (ListView) findViewById(R.id.lv_friend);
    }




    public void getFriendList() {

        for (int i = 0 ; i < 10 ; i++ ){
            Friend friend = new Friend();
            friend.setNickname("김은영");
            friend.setPhoneNumber("01072207802");
            ar_friend.add(friend);
        }

        FriendListAdapter adapter = new FriendListAdapter(this, R.layout.layout_friend_item, ar_friend);
        lv_friend.setAdapter(adapter);
        /*
        AsyncHttpClient myClient;

        myClient = new AsyncHttpClient();
        myClient.setTimeout(30000);
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        myClient.setCookieStore(myCookieStore);

        String url = this.getString(R.string.url_friend_list);
        RequestParams params = new RequestParams();
        final SingleToneData singleToneData = SingleToneData.getInstance();
        params.put("token", singleToneData.getToken());

        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("HTTP RESPONSE......getFriendList", new String(responseBody));
                getFriendListFinish(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
        */
    }

    private void getFriendListFinish(String st_result) {

        try {
            JSONObject jo_result = new JSONObject(st_result);
            JSONObject jo_response = jo_result.getJSONObject("response");
            JSONArray ja_friend_list = jo_response.getJSONArray("my_friend_list");
            String status = jo_result.getString("status");
            if (status.equals("success")) {

                for (int i = 0; i < ja_friend_list.length(); i++) {
                    JSONObject jo_friend = ja_friend_list.getJSONObject(i);

                    Friend friend = new Friend();
                    friend.setNickname(jo_friend.getString("nickname"));
                    friend.setPhoneNumber(jo_friend.getString("phone_number"));
                    friend.setId(jo_friend.getString("id"));

                    ar_friend.add(friend);
                }

            } else if (status.equals("fail")) {
                String st_error = jo_response.getString("error");
                if (st_error.equals("not login")) {
                    LoginUtils loginUtils = new LoginUtils(this);
                    loginUtils.onLoginRequired();
                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        FriendListAdapter adapter = new FriendListAdapter(this, R.layout.layout_friend_item, ar_friend);
        lv_friend.setAdapter(adapter);
    }
}
