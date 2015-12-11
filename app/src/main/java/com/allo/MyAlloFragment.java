package com.allo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
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

/**
 * Created by baek_uncheon on 2015. 7. 27..
 */
public class MyAlloFragment extends Fragment {
    Context context;

    ArrayList<RotationAlloSet> al_rotation_allo_set;
    ArrayList<Allo> al_my_allo;
    ArrayList<Allo> al_my_allo_list;
    ArrayList<Friend> al_friend;
    ArrayList<Friend> al_friend_list;
    ArrayList<Friend> al_friend_total;

    ImageView iv_back;

    ListView lv_my_allo;


    LinearLayout ll_basic_q;
    LinearLayout ll_friend_q;
    ImageView ib_basic_q;
    ImageView ib_friend_q;

    View view;

    View header_view;
    MyAlloAdapter adapter;

    AlloCacheAsyncTask alloCacheThread = null;
    ProgressDialog dialog = null;


    ProgressDialog pd_loading = null;

    String st_type = "";
    Friend select_friend = null;
    Allo select_allo = null;
    Tracker mTracker;


    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_allo, container, false);

        AnalyticsApplication application = (AnalyticsApplication) ((Activity)context).getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("MyAlloFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        dialog = new ProgressDialog(context);
        dialog.setTitle("");
        dialog.setMessage(context.getString(R.string.wait_cache_file));


        setLayout(view);
        setListener();

        getMyAllo();


        return view;
    }

    private void setLayout(View view) {


        iv_back = (ImageView) view.findViewById(R.id.iv_back);

        lv_my_allo = (ListView) view.findViewById(R.id.lv_my_allo);

        //        header view set Layout
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        header_view = inflater.inflate(R.layout.layout_my_allo_header_1, null, false);

        lv_my_allo.addHeaderView(header_view);

        ib_basic_q = (ImageView) header_view.findViewById(R.id.ib_basic_q);
        ib_friend_q = (ImageView) header_view.findViewById(R.id.ib_friend_q);

        ll_basic_q = (LinearLayout) header_view.findViewById(R.id.ll_basic_q);
        ll_friend_q = (LinearLayout) header_view.findViewById(R.id.ll_friend_q);

        ImageButton ib_add_1 = (ImageButton) header_view.findViewById(R.id.ib_add_1);
        ImageButton ib_add_2 = (ImageButton) header_view.findViewById(R.id.ib_add_2);
        ImageButton ib_add_3 = (ImageButton) header_view.findViewById(R.id.ib_add_3);
        ImageButton ib_add_4 = (ImageButton) header_view.findViewById(R.id.ib_add_4);

        TextView tv_title_1 = (TextView) header_view.findViewById(R.id.tv_title_1);
        TextView tv_title_2 = (TextView) header_view.findViewById(R.id.tv_title_2);
        TextView tv_title_3 = (TextView) header_view.findViewById(R.id.tv_title_3);
        TextView tv_title_4 = (TextView) header_view.findViewById(R.id.tv_title_4);

        TextView tv_artist_1 = (TextView) header_view.findViewById(R.id.tv_artist_1);
        TextView tv_artist_2 = (TextView) header_view.findViewById(R.id.tv_artist_2);
        TextView tv_artist_3 = (TextView) header_view.findViewById(R.id.tv_artist_3);
        TextView tv_artist_4 = (TextView) header_view.findViewById(R.id.tv_artist_4);

        RotationAlloSet rotationAlloSet_1 = new RotationAlloSet();
        rotationAlloSet_1.setIbAdd(ib_add_1);
        rotationAlloSet_1.setTvTitle(tv_title_1);
        rotationAlloSet_1.setTvArtist(tv_artist_1);

        RotationAlloSet rotationAlloSet_2 = new RotationAlloSet();
        rotationAlloSet_2.setIbAdd(ib_add_2);
        rotationAlloSet_2.setTvTitle(tv_title_2);
        rotationAlloSet_2.setTvArtist(tv_artist_2);

        RotationAlloSet rotationAlloSet_3 = new RotationAlloSet();
        rotationAlloSet_3.setIbAdd(ib_add_3);
        rotationAlloSet_3.setTvTitle(tv_title_3);
        rotationAlloSet_3.setTvArtist(tv_artist_3);

        RotationAlloSet rotationAlloSet_4 = new RotationAlloSet();
        rotationAlloSet_4.setIbAdd(ib_add_4);
        rotationAlloSet_4.setTvTitle(tv_title_4);
        rotationAlloSet_4.setTvArtist(tv_artist_4);

        al_rotation_allo_set = new ArrayList<>();

        al_rotation_allo_set.add(rotationAlloSet_1);
        al_rotation_allo_set.add(rotationAlloSet_2);
        al_rotation_allo_set.add(rotationAlloSet_3);
        al_rotation_allo_set.add(rotationAlloSet_4);

    }

    private void setListener() {


        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).openDrawerLayout();
            }
        });

        ll_basic_q.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("Help").setAction("basic_ll click").build());
                Intent intent = new Intent(context, HelpFragmentActivity.class);
                intent.putExtra("index", "basic");
                startActivity(intent);
            }
        });

        ll_friend_q.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("Help").setAction("friend_ll click").build());
                Intent intent = new Intent(context, HelpFragmentActivity.class);
                intent.putExtra("index", "friend");
                startActivity(intent);
            }
        });


        for (int i = 0; i < al_rotation_allo_set.size(); i++) {
            ImageButton ib_add = al_rotation_allo_set.get(i).getIbAdd();
            ib_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTracker.send(new HitBuilders.EventBuilder().setCategory("Allo").setAction("basic_allo click").build());
                    setEnabled(false);

                    Allo allo = null;
                    try {
                        switch (v.getId()) {
                            case R.id.ib_add_1:
                                allo = al_my_allo.get(0);
                                break;
                            case R.id.ib_add_2:
                                allo = al_my_allo.get(1);
                                break;
                            case R.id.ib_add_3:
                                allo = al_my_allo.get(2);
                                break;
                            case R.id.ib_add_4:
                                allo = al_my_allo.get(3);
                                break;
                        }
                    } catch (IndexOutOfBoundsException e) {
                        allo = null;
                    }

                    if (allo == null) {
                        Intent intent = new Intent(context, SelectMyAlloActivity.class);
                        intent.putExtra("type", "my_allo");
                        startActivity(intent);
                    } else {


                        dialog.show();

                        select_allo = new Allo();

                        select_allo.setTitle(allo.getTitle());
                        select_allo.setArtist(allo.getArtist());
                        select_allo.setURL(allo.getURL());
                        select_allo.setThumbs(allo.getThumbs());
                        select_allo.setImage(allo.getImage());
                        select_allo.setStartPoint(allo.getStartPoint());
                        select_allo.setEndPoint(allo.getEndPoint());
                        select_allo.setDuration(allo.getDuration());
                        select_allo.setId(allo.getId());

//
//                        AlloCacheServer alloCacheServer = new AlloCacheServer(context, select_allo.getURL()) {
//                            @Override
//                            public void onFinish(String st_cache_path, long l_time) {
//                                Log.i("CacheServer onFinish", l_time+"");
//                                select_allo.setURL(st_cache_path);
//                                st_type = "MY";
//                                PlayAllo.getInstance().setType("MY");
//                                PlayAllo.getInstance().setMyAlloFragment(MyAlloFragment.this);
//                                PlayAllo.getInstance().setAlloPrepare(select_allo);
//                            }
//
//                            @Override
//                            public void onFailed() {
//                                dialog.dismiss();
//                                setEnabled(true);
//                            }
//                        };
//                        try {
//                            alloCacheServer.setCachePathFromUrl();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }


                        alloCacheThread = new AlloCacheAsyncTask(context, select_allo.getURL()) {
                            @Override
                            public void onFinish(String st_cache_path, long l_time) {
                                select_allo.setURL(st_cache_path);
                                st_type = "MY";
                                PlayAllo.getInstance().setType("MY");
                                PlayAllo.getInstance().setMyAlloFragment(MyAlloFragment.this);
                                PlayAllo.getInstance().setAlloPrepare(select_allo);
                            }

                            @Override
                            public void onFailed() {
                                dialog.dismiss();
                                setEnabled(true);
                            }
                        };
                        alloCacheThread.execute("a", "a", "a");


                    }
                }
            });


        }
    }

    private void setEnabled(boolean flag) {
        for (int i = 0; i < al_rotation_allo_set.size(); i++) {
            ImageButton ib_add = al_rotation_allo_set.get(i).getIbAdd();
            ib_add.setEnabled(flag);
        }
        lv_my_allo.setEnabled(flag);
    }


    private void setHeaderContents() {
        al_my_allo = SingleToneData.getInstance().getMyAllo();

        if (al_my_allo == null) {
            ((MainActivity) context).moveFragment(0);
            return;
        }
        Log.i("myallofragment", String.valueOf(al_my_allo.size()));

        for (int j = 0; j < al_rotation_allo_set.size(); j++) {
            RotationAlloSet rotationAlloSet = al_rotation_allo_set.get(j);
            ImageButton ib_add = rotationAlloSet.getIbAdd();
            TextView tv_title = rotationAlloSet.getTvTitle();
            TextView tv_artist = rotationAlloSet.getTvArtist();
            ib_add.setBackgroundResource(R.drawable.selector_add);
            ib_add.setImageResource(android.R.color.transparent);

            if (j == 0) {
                tv_title.setText("알로선택");
                tv_artist.setText("기본알로");
            } else {
                tv_title.setText("");
                tv_artist.setText("");
            }
        }

        for (int i = 0; i < al_my_allo.size(); i++) {
            RotationAlloSet rotationAlloSet = al_rotation_allo_set.get(i);
            Allo allo = al_my_allo.get(i);

            ImageButton ib_add = rotationAlloSet.getIbAdd();
            TextView tv_title = rotationAlloSet.getTvTitle();
            TextView tv_artist = rotationAlloSet.getTvArtist();
            ib_add.setBackgroundResource(0);

            tv_title.setText(allo.getTitle());
            tv_artist.setText(allo.getArtist());

            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .displayer(new RoundedBitmapDisplayer(100)).build();
            ImageLoader imageLoader = ImageLoader.getInstance();
            if (!imageLoader.isInited())
                imageLoader.init(ImageLoaderConfiguration.createDefault(context));

            imageLoader.displayImage(allo.getThumbs(), ib_add, options);


        }
    }


    private class RotationAlloSet {
        ImageButton ib_add;
        TextView tv_title;
        TextView tv_artist;

        public void setIbAdd(ImageButton ib_add) {
            this.ib_add = ib_add;
        }

        public ImageButton getIbAdd() {
            return this.ib_add;
        }

        public void setTvTitle(TextView tv_title) {
            this.tv_title = tv_title;
        }

        public TextView getTvTitle() {
            return this.tv_title;
        }

        public void setTvArtist(TextView tv_artist) {
            this.tv_artist = tv_artist;
        }

        public TextView getTvArtist() {
            return this.tv_artist;
        }
    }

    private void getMyAllo() {
        AsyncHttpClient myClient;

        myClient = new AsyncHttpClient();
        myClient.setTimeout(SingleToneData.getInstance().getTimeOutValue());
        PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
        myClient.setCookieStore(myCookieStore);

        String url = context.getString(R.string.url_my_allo_list);

        RequestParams params = new RequestParams();
        LoginUtils loginUtils = new LoginUtils(context);

        params.put("id", loginUtils.getId());
        params.put("pw", loginUtils.getPw());

        pd_loading = ProgressDialog.show(context, "", "로딩중입니다.", true);
        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pd_loading.dismiss();
                pd_loading = null;
                Log.i("(MyAllo fragment)", new String(responseBody));
                getAlloFinish(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pd_loading.dismiss();
                pd_loading = null;
                Toast.makeText(context, getResources().getString(R.string.on_failure), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAlloFinish(String st_response_body) {
        al_my_allo = new ArrayList<>();
        al_my_allo_list = new ArrayList<>();
        al_friend_list = new ArrayList<>();
        al_friend = new ArrayList<>();
        al_friend_total = new ArrayList<>();

        try {

            JSONObject jo_result = new JSONObject(st_response_body);
            String st_status = jo_result.getString("status");
            if (st_status.equals("success")) {
                JSONObject jo_response = jo_result.getJSONObject("response");

                JSONArray ja_my_allo = jo_response.getJSONArray("my_allo");
                JSONArray ja_my_allo_list = jo_response.getJSONArray("my_allo_list");
                JSONArray ja_friend_allo_list = jo_response.getJSONArray("friend_allo_list");
                JSONArray ja_friend_list = jo_response.getJSONArray("friend_list");

                SharedPreferences pref = context.getSharedPreferences("my_allo", context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();


                for (int i = 0; i < ja_my_allo.length(); i++) {
                    JSONObject jo_allo = ja_my_allo.getJSONObject(i);
                    Allo allo = new Allo();
                    if (jo_allo.has("title"))
                        allo.setTitle(jo_allo.getString("title"));
                    if (jo_allo.has("artist"))
                        allo.setArtist(jo_allo.getString("artist"));
                    if (jo_allo.has("image"))
                        allo.setImage(jo_allo.getString("image"));
                    if (jo_allo.has("thumbs"))
                        allo.setThumbs(jo_allo.getString("thumbs"));
                    if (jo_allo.has("uid"))
                        allo.setId(jo_allo.getString("uid"));
                    if (jo_allo.has("url"))
                        allo.setURL(jo_allo.getString("url"));
                    if (jo_allo.has("duration"))
                        allo.setDuration(jo_allo.getInt("duration"));
                    if (jo_allo.has("start_point"))
                        allo.setStartPoint(jo_allo.getInt("start_point"));
                    if (jo_allo.has("end_point"))
                        allo.setEndPoint(jo_allo.getInt("end_point"));


                    al_my_allo.add(allo);


                    String st_url_key = i + "url";
                    String st_start_key = i + "key";
                    String st_title_key = i + "title";
                    String st_artist_key = i + "artist";
                    String st_image_key = i + "image";
                    String st_thumb_key = i +"thumb";

                    editor.putString(st_title_key, allo.getTitle());
                    editor.putString(st_artist_key, allo.getArtist());
                    editor.putString(st_url_key, allo.getURL());
                    editor.putInt(st_start_key, allo.getStartPoint());
                    editor.putString(st_image_key, allo.getImage());
                    editor.putString(st_thumb_key, allo.getThumbs());

                }

                editor.putInt("count", ja_my_allo.length());
                editor.commit();


                for (int i = 0; i < ja_my_allo_list.length(); i++) {
                    JSONObject jo_allo = ja_my_allo_list.getJSONObject(i);
                    Allo allo = new Allo();
                    if (jo_allo.has("title"))
                        allo.setTitle(jo_allo.getString("title"));
                    if (jo_allo.has("artist"))
                        allo.setArtist(jo_allo.getString("artist"));
                    if (jo_allo.has("image"))
                        allo.setImage(jo_allo.getString("image"));
                    if (jo_allo.has("thumbs"))
                        allo.setThumbs(jo_allo.getString("thumbs"));
                    if (jo_allo.has("uid"))
                        allo.setId(jo_allo.getString("uid"));
                    if (jo_allo.has("url"))
                        allo.setURL(jo_allo.getString("url"));
                    if (jo_allo.has("duration"))
                        allo.setDuration(jo_allo.getInt("duration"));


                    al_my_allo_list.add(allo);
                }


                for (int i = 0; i < ja_friend_allo_list.length(); i++) {
                    JSONObject jo_friend = ja_friend_allo_list.getJSONObject(i);
                    Friend friend = new Friend();
                    if (jo_friend.has("nickname"))
                        friend.setNickname(jo_friend.getString("nickname"));
                    if (jo_friend.has("phone_number"))
                        friend.setPhoneNumber(jo_friend.getString("phone_number"));
                    if (jo_friend.has("title"))
                        friend.setTitle(jo_friend.getString("title"));
                    if (jo_friend.has("artist"))
                        friend.setArtist(jo_friend.getString("artist"));
                    if (jo_friend.has("image"))
                        friend.setImage(jo_friend.getString("image"));
                    if (jo_friend.has("thumbs"))
                        friend.setThumbs(jo_friend.getString("thumbs"));
                    if (jo_friend.has("uid"))
                        friend.setId(jo_friend.getString("uid"));
                    if (jo_friend.has("url"))
                        friend.setURL(jo_friend.getString("url"));
                    if (jo_friend.has("duration"))
                        friend.setDuration(jo_friend.getInt("duration"));
                    if (jo_friend.has("start_point"))
                        friend.setStartPoint(jo_friend.getInt("start_point"));
                    if (jo_friend.has("end_point"))
                        friend.setEndPoint(jo_friend.getInt("end_point"));

                    al_friend_list.add(friend);

                }


                for (int i = 0; i < ja_friend_list.length(); i++) {
                    JSONObject jo_friend = ja_friend_list.getJSONObject(i);
                    Friend friend = new Friend();
                    if (jo_friend.has("nickname"))
                        friend.setNickname(jo_friend.getString("nickname"));
                    if (jo_friend.has("phone_number"))
                        friend.setPhoneNumber(jo_friend.getString("phone_number"));

                    al_friend.add(friend);

                }

                SingleToneData.getInstance().setMyAlloList(al_my_allo_list);
                SingleToneData.getInstance().setMyAllo(al_my_allo);
                SingleToneData.getInstance().setFriendAlloList(al_friend_list);
                SingleToneData.getInstance().setFriend(al_friend);

                setFriendTotal();


            } else {
                ErrorHandler errorHandler = new ErrorHandler(context);
                errorHandler.handleErrorCode(jo_result);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("my allo size", String.valueOf(al_my_allo.size()));
        Log.i("my allo list size", String.valueOf(al_my_allo_list.size()));
        Log.i("friend size", String.valueOf(al_friend.size()));
        Log.i("friend allo size", String.valueOf(al_friend_list.size()));


        if (al_friend_total.size() == 0) {
            al_friend_total.add(new Friend());
            MyAlloNoFriendAdapter _adapter = new MyAlloNoFriendAdapter(context, R.layout.activity_friend_intro, al_friend_total);
            lv_my_allo.setAdapter(_adapter);
        } else {
            adapter = new MyAlloAdapter(context, R.layout.my_allo_friends_list_item, al_friend_total);
            lv_my_allo.setAdapter(adapter);

            lv_my_allo.setOnItemClickListener(itemClickListener);
        }


        setHeaderContents();
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            mTracker.send(new HitBuilders.EventBuilder().setCategory("Allo").setAction("friend_allo click").build());
            setEnabled(false);
            Friend friend = al_friend_total.get(i - 1);

            select_friend = new Friend();
            select_friend.setFriendId(friend.getFriendId());
            select_friend.setPhoneNumber(friend.getPhoneNumber());
            select_friend.setNickname(friend.getNickname());
            select_friend.setTitle(friend.getTitle());
            select_friend.setArtist(friend.getArtist());
            select_friend.setURL(friend.getURL());
            select_friend.setThumbs(friend.getThumbs());
            select_friend.setImage(friend.getImage());
            select_friend.setStartPoint(friend.getStartPoint());
            select_friend.setEndPoint(friend.getEndPoint());
            select_friend.setDuration(friend.getDuration());
            select_friend.setId(friend.getId());

            if (select_friend.getTitle() == null) {
                Intent intent = new Intent(context, SelectMyAlloActivity.class);
                intent.putExtra("friend", select_friend);
                intent.putExtra("type", "friend");
                startActivity(intent);
            } else {

                dialog.show();

                alloCacheThread = new AlloCacheAsyncTask(context, select_friend.getURL()) {
                    @Override
                    public void onFinish(String st_cache_path, long l_time) {
                        select_friend.setURL(st_cache_path);
                        st_type = "MY_FRIEND";
                        PlayAllo.getInstance().setType("MY");
                        PlayAllo.getInstance().setMyAlloFragment(MyAlloFragment.this);
                        PlayAllo.getInstance().setAlloPrepare(select_friend);
                    }

                    @Override
                    public void onFailed() {
                        dialog.dismiss();
                        setEnabled(true);
                    }
                };
                alloCacheThread.execute("a", "a", "a");
            }
        }
    };

    private void setFriendTotal() {
        al_friend_total.clear();

        al_friend = SingleToneData.getInstance().getFriend();
        al_friend_list = SingleToneData.getInstance().getFriendAlloList();

        al_friend_total.addAll(al_friend_list);

        if (al_friend_list.size() == 0) {
            al_friend_total.addAll(al_friend);
        } else {
            for (int i = 0; i < al_friend.size(); i++) {
                Friend friend = al_friend.get(i);
                boolean is_exist = false;
                for (int j = 0; j < al_friend_list.size(); j++) {
                    Friend friend_allo = al_friend_list.get(j);
                    if (
                            (friend_allo.getPhoneNumber().equals(friend.getPhoneNumber())))
                        is_exist = true;
                }
                if (!is_exist)
                    al_friend_total.add(friend);
            }
        }
    }


    //    call back
    public void onReload() {
        if (SingleToneData.getInstance().getMyAllo() != null)
            setHeaderContents();
    }

    public void onReloadListView() {
        setFriendTotal();
        Log.i("total", String.valueOf(al_friend_total.size()) + ", " + String.valueOf(al_friend_list.size()) + ", " + String.valueOf(al_friend.size()));

        adapter = new MyAlloAdapter(context, R.layout.my_allo_friends_list_item, al_friend_total);
        lv_my_allo.setAdapter(adapter);

        lv_my_allo.setOnItemClickListener(itemClickListener);


    }

    public void onPrepared(Allo allo) {
        Log.i("MyAlloFragment", "onPrepared");
        AlloMyDialog alloMyDialog = new AlloMyDialog(context);
        alloMyDialog.setType(st_type);
        alloMyDialog.setAllo(allo);
        if (st_type.equals("MY_FRIEND"))
            alloMyDialog.setFriend(select_friend);
        alloMyDialog.setMyAlloFragment(MyAlloFragment.this);
        alloMyDialog.show();
        dialog.dismiss();
        setEnabled(true);
    }

    public void onPrepareFailed() {
        dialog.dismiss();
        setEnabled(true);

        Toast.makeText(context, context.getResources().getString(R.string.prepare_fail), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        Log.i("on resoume", "resume");
        setEnabled(true);
        super.onResume();
        if (SingleToneData.getInstance().getMyAllo() != null) {
            setHeaderContents();
            onReloadListView();
        }
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
