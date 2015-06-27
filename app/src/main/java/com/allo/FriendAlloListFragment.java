package com.allo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class FriendAlloListFragment extends Fragment {
    ImageView iv_side_munu;
    ListView lv_friend;

    ArrayList<Friend> ar_friend = new ArrayList<Friend>();
    ImageLoader imageLoader;
    DisplayImageOptions options;
    Context context;


    public void setContext(Context context){this.context = context;}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("onCreateView", "view");
        View view = inflater.inflate(R.layout.fragment_friend_list, container, false);


        setLayout(view);
        setInstance();
        setListener();
        getFriendList();



        return view;
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
        imageLoader.init(ImageLoaderConfiguration.createDefault(context));
    }


    private void setLayout(View view) {
        iv_side_munu = (ImageView) view.findViewById(R.id.iv_side_munu);
        lv_friend = (ListView) view.findViewById(R.id.lv_friend);
    }

    private void setListener() {
        iv_side_munu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)context).openDrawerLayout();
            }
        });
    }




    public void getFriendList() {

        for (int i = 0 ; i < 10 ; i++ ){
            Friend friend = new Friend();
            friend.setNickname("김은영");
            friend.setPhoneNumber("01072207802");


            Allo allo = new Allo();
            allo.setTitle("the blower's daughter");
            allo.setArtist("damien rice");
            allo.setURL("adsf");
            allo.setThumbs("asdf");

            friend.setAllo(allo);

            ar_friend.add(friend);
        }

        FriendAlloAdapter adapter = new FriendAlloAdapter(context, R.layout.layout_friend_allo_item_3, ar_friend);
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
                    LoginUtils loginUtils = new LoginUtils(context);
                    loginUtils.onLoginRequired();
                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }

        FriendListAdapter adapter = new FriendListAdapter(context, R.layout.layout_friend_allo_item_3, ar_friend);
        lv_friend.setAdapter(adapter);
    }
}
