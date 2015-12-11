package com.allo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.adapter.RecyclerViewMaterialAdapter;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by florentchampigny on 24/04/15.
 */
public class StoreFragment extends Fragment {


    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    MainFragment mainFragment;
    Context context;
    String st_type;
    ArrayList<Allo> ar_allo;
    boolean is_scrolled = false;


    private int scrollY = 0;

    public static StoreFragment newInstance() {
        return new StoreFragment();
    }

    public void setMainFragment(MainFragment mainFragment) {
        this.mainFragment = mainFragment;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setType(String st_type) {
        this.st_type = st_type;
    }

    public void setScrollY(int scrollY_){
        Log.i("srolled set : ",scrollY_+"  zzz");
        if (scrollY_ > 380){
            scrollY = 380;
        }else{
            scrollY = scrollY_;
        }
    }

    public int getScrollY() { return this.scrollY; }







    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View v = mainFragment.getView();
        final LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.ll_cash);
        final ViewPager viewPager = (ViewPager) v.findViewById(R.id.vp_noti);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);


        Log.i("rec", "start  star tat ");

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                scrollY += dy;
                if (scrollY < 0) scrollY = 0;

                if (scrollY < 190) {
//                    Log.i("baekun", "true : " + scrollY);
                    viewPager.setVisibility(View.VISIBLE);
                    linearLayout.setVisibility(View.VISIBLE);
                } else if (scrollY > 190) {
//                    Log.i("baekun", "false : " + scrollY);
                    viewPager.setVisibility(View.GONE);
                    linearLayout.setVisibility(View.GONE);
                }

                SingleToneData.getInstance().setIsScrolled(true);



                Log.i("scroll y", scrollY+"");
            }
        });

        getStoreAlloList();


    }

    private void getStoreAlloList() {
        AsyncHttpClient myClient;

        myClient = new AsyncHttpClient();
        myClient.setTimeout(SingleToneData.getInstance().getTimeOutValue());
        PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
        myClient.setCookieStore(myCookieStore);

        String url = "";

        switch (st_type) {
            case "popular_ucc":
                url = context.getString(R.string.url_popular_ucc);
                break;
            case "new_ucc":
                url = context.getString(R.string.url_new_ucc);
                break;
            case "popular_music":
                url = context.getString(R.string.url_popular_music);
                break;
            case "new_music":
                url = context.getString(R.string.url_new_music);
                break;
            default:
                url = context.getString(R.string.url_popular_ucc);
                break;
        }

        RequestParams params = new RequestParams();
        LoginUtils loginUtils = new LoginUtils(context);

        params.put("id", loginUtils.getId());
        params.put("pw", loginUtils.getPw());


        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("(Store fragment)", new String(responseBody));
                getStoreFirstListFinish(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, getResources().getString(R.string.on_failure), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getStoreFirstListFinish(String st_result) {
        ar_allo = new ArrayList<Allo>();

        try {
            JSONObject jo_result = new JSONObject(st_result);
            JSONObject jo_response = jo_result.getJSONObject("response");
            JSONArray ja_allo_list = jo_response.getJSONArray("allo_list");
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
                    if (jo_allo.has("duration"))
                        allo.setDuration(jo_allo.getInt("duration"));
                    if (jo_allo.has("is_ucc"))
                        allo.setIsUcc(jo_allo.getBoolean("is_ucc"));
                    if (jo_allo.has("uploader_id"))
                        allo.setUploader(jo_allo.getString("uploader_id"));
                    if (jo_allo.has("desc"))
                        allo.setContent(jo_allo.getString("desc"));

                    ar_allo.add(allo);
                }

            } else if (status.equals("fail")) {
                ErrorHandler errorHandler = new ErrorHandler(context);
                errorHandler.handleErrorCode(jo_result);
            }

        } catch (Exception e) {
            Toast.makeText(context, "Json Error", Toast.LENGTH_SHORT).show();
        }

        mAdapter = new RecyclerViewMaterialAdapter(new StoreRecyclerViewAdapter(context, ar_allo));
        mRecyclerView.setAdapter(mAdapter);
        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView, null);
    }

}