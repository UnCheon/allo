package com.allo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class ChangeAlloActivity extends Activity {
    ImageView iv_back;
    TextView tv_title;
    ListView lv_allo;

    Button btn_time;
    Button btn_friend;

    ArrayList<Allo> ar_allo;
    ImageLoader imageLoader;
    DisplayImageOptions options;

    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_allo);

        setLayout();
        setHeaderView();

        setInstance();
        getAlloList();
    }

    private void setInstance() {
        context = this;

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("allo-state"));

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
        tv_title = (TextView) findViewById(R.id.tv_title);
        lv_allo = (ListView) findViewById(R.id.lv_allo);

    }

    private void setHeaderView() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = null;

        String st_category = getIntent().getStringExtra("category");

        switch (st_category) {
            case "basic":
                view = inflater.inflate(R.layout.layout_change_basic_header, null, false);
                break;
            case "time":
                view = inflater.inflate(R.layout.layout_change_time_header, null, false);
                btn_time = (Button) view.findViewById(R.id.btn_time);
                btn_time.setOnClickListener(timeClickListener);
                break;
            case "friend":
                view = inflater.inflate(R.layout.layout_change_friend_header, null, false);
                btn_friend = (Button) view.findViewById(R.id.btn_friend);
                btn_friend.setOnClickListener(friendClickListener);
                break;
        }
        lv_allo.addHeaderView(view);
    }

    View.OnClickListener timeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CustomTimePickerDialog customTimePickerDialog = new CustomTimePickerDialog(context,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    },
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });
            customTimePickerDialog.show();
        }
    };

    View.OnClickListener friendClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, FriendListActivity.class);
            startActivity(intent);
        }
    };


    public void getAlloList() {
        AsyncHttpClient myClient;

        myClient = new AsyncHttpClient();
        myClient.setTimeout(30000);
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        myClient.setCookieStore(myCookieStore);

        String url = this.getString(R.string.url_my_allo_list);
        RequestParams params = new RequestParams();
        final SingleToneData singleToneData = SingleToneData.getInstance();
        params.put("token", singleToneData.getToken());

        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("HTTP RESPONSE......getAlloList", new String(responseBody));
                getAlloListFinish(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private void getAlloListFinish(String st_result) {
        ar_allo = new ArrayList<Allo>();

        try {
            JSONObject jo_result = new JSONObject(st_result);
            JSONObject jo_response = jo_result.getJSONObject("response");
            JSONArray ja_allo_list = jo_response.getJSONArray("my_allo_list");
            String status = jo_result.getString("status");
            if (status.equals("success")) {

                for (int i = 0; i < ja_allo_list.length(); i++) {
                    JSONObject jo_allo = ja_allo_list.getJSONObject(i);

                    Allo allo = new Allo();
                    allo.setTitle(jo_allo.getString("title"));
                    allo.setArtist(jo_allo.getString("artist"));
                    allo.setURL(jo_allo.getString("url"));
                    if (jo_allo.has("thumbs"))
                        allo.setThumbs(jo_allo.getString("thumbs"));
                    if (jo_allo.has("image"))
                        allo.setImage(jo_allo.getString("image"));
                    if (jo_allo.has("uid"))
                        allo.setId(jo_allo.getString("uid"));
                    ar_allo.add(allo);
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

        ChangeAlloAdapter adapter = new ChangeAlloAdapter(this, R.layout.layout_change_allo_item, ar_allo);
        lv_allo.setAdapter(adapter);
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Log.i("receiver", "stop stop stop");
            String message = intent.getStringExtra("message");
            if (message.equals("stop")) {
//                playBarUIInit();
            }
        }
    };
}
