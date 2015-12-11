package com.allo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class FriendListActivity extends Activity {
    LinearLayout ll_back;
    ImageView iv_back;
    ListView lv_friend;

    Allo allo;

    ArrayList<Friend> al_friend = new ArrayList<Friend>();
    ImageLoader imageLoader;
    DisplayImageOptions options;

    ProgressDialog pd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName("FriendListActivity(Gift)");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());


        allo = (Allo) getIntent().getSerializableExtra("allo");
        setLayout();

        setInstance();
        getFriend();
    }

    private void setInstance() {

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.allo_logo)
                .showImageForEmptyUri(R.drawable.allo_logo)
                .showImageOnFail(R.drawable.allo_logo)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(0)).build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));
    }


    private void setLayout() {
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        lv_friend = (ListView) findViewById(R.id.lv_friend);
    }


    private void getFriend() {
        AsyncHttpClient myClient;

        myClient = new AsyncHttpClient();
        myClient.setTimeout(SingleToneData.getInstance().getTimeOutValue());
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        myClient.setCookieStore(myCookieStore);

        String url = getString(R.string.url_allo_friend);

        RequestParams params = new RequestParams();
        LoginUtils loginUtils = new LoginUtils(this);

        params.put("id", loginUtils.getId());
        params.put("pw", loginUtils.getPw());


//        pd = ProgressDialog.show(FriendListActivity.this, "", FriendListActivity.this.getString(R.string.wait_friend), true);
        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                pd.dismiss();
                Log.i("(friend fragment)", new String(responseBody));
                getFriendFinish(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                pd.dismiss();
                Toast.makeText(FriendListActivity.this, getString(R.string.on_failure), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getFriendFinish(String st_response_body) {
        try {
            JSONObject jo_result = new JSONObject(st_response_body);
            String st_status = jo_result.getString("status");
            if (st_status.equals("success")) {
                JSONObject jo_response = jo_result.getJSONObject("response");

                JSONArray ja_friend_list = jo_response.getJSONArray("friend_list");


                for (int i = 0; i < ja_friend_list.length(); i++) {
                    JSONObject jo_friend = ja_friend_list.getJSONObject(i);
                    Friend friend = new Friend();
                    if (jo_friend.has("nickname"))
                        friend.setNickname(jo_friend.getString("nickname"));
                    if (jo_friend.has("phone_number"))
                        friend.setPhoneNumber(jo_friend.getString("phone_number"));

                    /*
                    try{
                        JSONArray ja_my_allo = jo_response.getJSONArray("my_allo");
                        JSONObject jo_allo = ja_my_allo.getJSONObject(i);

                        if (jo_allo.has("title"))
                            friend.setTitle(jo_friend.getString("title"));
                        if (jo_allo.has("artist"))
                            friend.setArtist(jo_friend.getString("artist"));
                        if (jo_allo.has("image"))
                            friend.setImage(jo_friend.getString("image"));
                        if (jo_allo.has("thumbs"))
                            friend.setThumbs(jo_friend.getString("thumbs"));
                        if (jo_allo.has("uid"))
                            friend.setId(jo_friend.getString("uid"));
                        if (jo_allo.has("url"))
                            friend.setURL(jo_friend.getString("url"));
                        if (jo_allo.has("duration"))
                            friend.setDuration(jo_friend.getInt("duration"));
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    */

                    al_friend.add(friend);
                }

            } else {
                ErrorHandler errorHandler = new ErrorHandler(this);
                errorHandler.handleErrorCode(jo_result);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (al_friend.size() == 0) {
            ArrayList<Friend> al_friend = new ArrayList<>();
            al_friend.add(new Friend());
            MyAlloNoFriendAdapter adapter = new MyAlloNoFriendAdapter(this, R.layout.activity_friend_intro, al_friend);
            lv_friend.setAdapter(adapter);
            lv_friend.setDividerHeight(0);
        } else {
            FriendList2Adapter adapter = new FriendList2Adapter(this, R.layout.friend_list_item, al_friend, allo);
            lv_friend.setAdapter(adapter);
        }

        Log.i("size", String.valueOf(al_friend.size()));

    }

    @Override
    protected void onStart(){
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop(){
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
}
