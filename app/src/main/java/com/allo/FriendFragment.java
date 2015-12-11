package com.allo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
public class FriendFragment extends Fragment {
    Context context;


    ArrayList<Friend> al_friend;


    ImageView iv_back;


    ListView lv_my_allo;
    AlloCacheAsyncTask alloCacheThread = null;
    ProgressDialog dialog;
    ProgressDialog pd_loading;

    Allo select_allo;

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend, container, false);

        AnalyticsApplication application = (AnalyticsApplication) ((Activity)context).getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName("FriendFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        setLayout(view);
        setListener();

        dialog = new ProgressDialog(context);
        dialog.setTitle("");
        dialog.setMessage(context.getString(R.string.wait_cache_file));

        getFriend();

        return view;
    }

    private void setLayout(View view) {


        iv_back = (ImageView) view.findViewById(R.id.iv_back);

        lv_my_allo = (ListView) view.findViewById(R.id.lv_my_allo);

    }

    private void setListener() {


        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).openDrawerLayout();
            }
        });

        lv_my_allo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

        String url = context.getString(R.string.url_allo_friend);

        RequestParams params = new RequestParams();
        LoginUtils loginUtils = new LoginUtils(context);

        params.put("id", loginUtils.getId());
        params.put("pw", loginUtils.getPw());

        pd_loading = ProgressDialog.show(context, "", "로딩중입니다.", true);
        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("(friend fragment)", new String(responseBody));
                pd_loading.dismiss();
                getFriendFinish(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pd_loading.dismiss();
                Toast.makeText(context, getResources().getString(R.string.on_failure), Toast.LENGTH_SHORT).show();
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

                al_friend = new ArrayList<>();

                for (int i = 0; i < ja_friend_list.length(); i++) {
                    JSONObject jo_friend = ja_friend_list.getJSONObject(i);
                    Friend friend = new Friend();
                    if (jo_friend.has("nickname"))
                        friend.setNickname(jo_friend.getString("nickname"));
                    if (jo_friend.has("phone_number"))
                        friend.setPhoneNumber(jo_friend.getString("phone_number"));

                    if (jo_friend.has("my_allo")) {
                        JSONArray ja_my_allo = jo_friend.getJSONArray("my_allo");
                        if (ja_my_allo.length() != 0) {
                            JSONObject jo_allo = ja_my_allo.getJSONObject(0);
                            if (jo_allo.has("title"))
                                friend.setTitle(jo_allo.getString("title"));
                            if (jo_allo.has("artist"))
                                friend.setArtist(jo_allo.getString("artist"));
                            if (jo_allo.has("image"))
                                friend.setImage(jo_allo.getString("image"));
                            if (jo_allo.has("thumbs"))
                                friend.setThumbs(jo_allo.getString("thumbs"));
                            if (jo_allo.has("uid"))
                                friend.setId(jo_allo.getString("uid"));
                            if (jo_allo.has("url"))
                                friend.setURL(jo_allo.getString("url"));
                            if (jo_allo.has("duration"))
                                friend.setDuration(jo_allo.getInt("duration"));
                            if (jo_allo.has("start_point"))
                                friend.setStartPoint(jo_allo.getInt("start_point"));
                            if (jo_allo.has("end_point"))
                                friend.setEndPoint(jo_allo.getInt("end_point"));
                        }
                    }


                    al_friend.add(friend);
                }

            } else {
                ErrorHandler errorHandler = new ErrorHandler(context);
                errorHandler.handleErrorCode(jo_result);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (al_friend.size() == 0) {
            ArrayList<Friend> al_friend = new ArrayList<>();
            al_friend.add(new Friend());
            MyAlloNoFriendAdapter adapter = new MyAlloNoFriendAdapter(context, R.layout.activity_friend_intro, al_friend);
            lv_my_allo.setAdapter(adapter);
            lv_my_allo.setDividerHeight(0);
        } else {
            FriendAdapter adapter = new FriendAdapter(context, R.layout.friends_allo_list_item, al_friend);
            lv_my_allo.setAdapter(adapter);

            lv_my_allo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    lv_my_allo.setEnabled(false);
                    Friend friend = al_friend.get(i);
                    if (friend.getTitle() == null || friend.getTitle().equals("")) {
                        Toast.makeText(context, "알로를 설정하지 않은 친구입니다.", Toast.LENGTH_SHORT).show();

                    } else {
                        dialog.show();

                        select_allo = new Allo();

                        select_allo.setTitle(friend.getTitle());
                        select_allo.setArtist(friend.getArtist());
                        select_allo.setURL(friend.getURL());
                        select_allo.setThumbs(friend.getThumbs());
                        select_allo.setImage(friend.getImage());
                        select_allo.setStartPoint(friend.getStartPoint());
                        select_allo.setEndPoint(friend.getEndPoint());
                        select_allo.setDuration(friend.getDuration());
                        select_allo.setId(friend.getId());


                        alloCacheThread = new AlloCacheAsyncTask(context, friend.getURL()) {
                            @Override
                            public void onFinish(String st_cache_path, long l_time) {
                                select_allo.setURL(st_cache_path);
                                PlayAllo.getInstance().setType("FRIEND");
                                PlayAllo.getInstance().setFriendFragment(FriendFragment.this);
                                PlayAllo.getInstance().setAlloPrepare(select_allo);
                            }

                            @Override
                            public void onFailed() {
                                lv_my_allo.setEnabled(true);
                            }
                        };
                        alloCacheThread.execute("a", "a", "a");
                    }
                }
            });
        }
        Log.i("size", String.valueOf(al_friend.size()));
    }

    public void onPrepared(Allo allo) {
        AlloFriendDialog alloFriendDialog = new AlloFriendDialog(context);
        alloFriendDialog.setAllo(allo);
        alloFriendDialog.show();
        dialog.dismiss();
        lv_my_allo.setEnabled(true);
    }

    public void onPrepareFailed() {
        dialog.dismiss();
        lv_my_allo.setEnabled(true);
        Toast.makeText(context, context.getResources().getString(R.string.prepare_fail), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStop() {
        Log.i("on stop", "stop");
        super.onStop();
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();

        if (alloCacheThread != null) {
            if (!alloCacheThread.isCancelled())
                alloCacheThread.cancel(true);
        }
        GoogleAnalytics.getInstance(context).reportActivityStop((Activity) context);
    }


    @Override
    public void onStart(){
        super.onStart();
        GoogleAnalytics.getInstance(context).reportActivityStart((Activity) context);
    }
}
