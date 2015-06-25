package com.allo;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by baek_uncheon on 2015. 3. 30..
 */
public class AlloHttpUtils {
    Context mContext;
    AlloJSONUtils alloJSONUtils;

    public AlloHttpUtils(Context mContext){
        this.mContext = mContext;
        alloJSONUtils = new AlloJSONUtils(mContext);
    }

    public void getAlloStoreList(final StoreFragment storeFragment, String st_url){
        AsyncHttpClient myClient = new AsyncHttpClient();
        myClient.setTimeout(30000);
        PersistentCookieStore myCookieStore = new PersistentCookieStore(mContext);
        myClient.setCookieStore(myCookieStore);

        RequestParams params = new RequestParams();
        SingleToneData singleToneData = SingleToneData.getInstance();

        params.put("token", singleToneData.getToken());

        myClient.post(st_url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("HTTP RESPONSE......get allo store list", new String(responseBody));
                parseStoreJson(storeFragment, new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private void parseStoreJson(StoreFragment storeFragment, String resultString){
        ArrayList<Allo> allo_array = new ArrayList<>();

        try{
            JSONObject jo_result = new JSONObject(resultString);
            JSONObject jo_response = jo_result.getJSONObject("response");
            JSONArray ja_allo_list = jo_response.getJSONArray("allo_list");
            String status = jo_result.getString("status");
            if (status.equals("success")){

                for (int i = 0; i < ja_allo_list.length(); i++) {
                    JSONObject jo_allo = ja_allo_list.getJSONObject(i);

                    Allo allo = new Allo();
                    allo.setTitle(jo_allo.getString("title"));
                    allo.setArtist(jo_allo.getString("artist"));
                    allo.setURL(jo_allo.getString("url"));
//                    allo.setThumbs(jo_allo.getString("thumbs"));
//                    allo.setImage(jo_allo.getString("image"));
//                    allo.setId(jo_allo.getString("id"));
//                    allo.setRank(jo_allo.getString("rank"));


                    allo_array.add(allo);
                }

            } else if (status.equals("fail")) {
                String st_error = jo_response.getString("error");
                if (st_error.equals("not login")) {
                    LoginUtils loginUtils = new LoginUtils(mContext);
                    loginUtils.onLoginRequired();
                }
            }

        }catch (Exception e){
            System.out.println(e);
        }
        storeFragment.setRankAdapter(allo_array);
    }




    public void buyRing(Allo allo){
        AsyncHttpClient myClient = new AsyncHttpClient();
        myClient.setTimeout(30000);

        String url = mContext.getString(R.string.url_store_buy);

        PersistentCookieStore myCookieStore = new PersistentCookieStore(mContext);
        myClient.setCookieStore(myCookieStore);

        RequestParams params = new RequestParams();
        params.put("title", allo.getTitle());
        params.put("singer", allo.getArtist());
        params.put("url", allo.getURL());
        params.put("token", "token");
        // id & 이용권/가격

        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("HTTP RESPONSE......", new String(responseBody));
                try {
                    JSONObject jo_result = new JSONObject(new String(responseBody));
                    String st_status = jo_result.getString("status");
                    if (st_status.equals("success")) {
                        Toast.makeText(mContext, "구매가 완료되었습니다. 내 정보에서 확인하세요.", Toast.LENGTH_SHORT).show();
                    } else if (st_status.equals("fail")) {
                        JSONObject jo_response = jo_result.getJSONObject("response");
                        String st_error = jo_response.getString("error");
                        if (st_error.equals("not login")) {
                            LoginUtils loginUtils = new LoginUtils(mContext);
                            loginUtils.onLoginRequired();
                        }
                    }

                } catch (JSONException e) {

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    public void setMainAllo(final Allo allo, final MyAlloFragment myAlloFragment){
        AsyncHttpClient myClient = new AsyncHttpClient();
        myClient.setTimeout(30000);

        String url = mContext.getString(R.string.url_set_my_allo);

        PersistentCookieStore myCookieStore = new PersistentCookieStore(mContext);
        myClient.setCookieStore(myCookieStore);

        RequestParams params = new RequestParams();
        SingleToneData singleToneData= SingleToneData.getInstance();
        params.put("token", singleToneData.getToken());
        params.put("allo_uid", allo.getId());


        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("HTTP RESPONSE......", new String(responseBody));
                try {
                    JSONObject response_object = new JSONObject(new String(responseBody));
                    String status = response_object.getString("status");
                    if (status.equals("success")) {
                        myAlloFragment.completeSetMainAllo(allo);
                    } else {

                    }

                } catch (JSONException e) {

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    public void getMyAlloList(final MyAlloFragment myAlloFragment){
        AsyncHttpClient myClient;

        myClient = new AsyncHttpClient();
        myClient.setTimeout(30000);
        PersistentCookieStore myCookieStore = new PersistentCookieStore(mContext);
        myClient.setCookieStore(myCookieStore);

        String url = mContext.getString(R.string.url_my_allo_list);
        RequestParams params = new RequestParams();
        SingleToneData singleToneData = SingleToneData.getInstance();
        params.put("token", singleToneData.getToken());

        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("HTTP RESPONSE......getAlloList", new String(responseBody));
                alloJSONUtils.parseMyAlloList(new String(responseBody));
                myAlloFragment.setUI();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    public void getMyAlloList(final ByChangeActivity byChangeActivity){
        AsyncHttpClient myClient;

        myClient = new AsyncHttpClient();
        myClient.setTimeout(30000);
        PersistentCookieStore myCookieStore = new PersistentCookieStore(mContext);
        myClient.setCookieStore(myCookieStore);

        String url = mContext.getString(R.string.url_my_allo_list);
        RequestParams params = new RequestParams();
        SingleToneData singleToneData = SingleToneData.getInstance();
        params.put("token", singleToneData.getToken());

        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("HTTP RESPONSE......getAlloList", new String(responseBody));
                alloJSONUtils.parseMyAlloList(new String(responseBody));
                byChangeActivity.setUI();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }


    public void getByFriendAllo(final ByFriendAlloFragment byFriendAlloFragment){
        AsyncHttpClient myClient;

        myClient = new AsyncHttpClient();
        myClient.setTimeout(30000);
        PersistentCookieStore myCookieStore = new PersistentCookieStore(mContext);
        myClient.setCookieStore(myCookieStore);

        String url = mContext.getString(R.string.url_by_friend_allo);
        RequestParams params = new RequestParams();
        SingleToneData singleToneData = SingleToneData.getInstance();
        params.put("token", singleToneData.getToken());

        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("HTTP RESPONSE......getAlloList", new String(responseBody));
                alloJSONUtils.parseByFriendAlloList(new String(responseBody));
                byFriendAlloFragment.setUI();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    public void setByFriendAllo(final Allo allo, final Context context) {
        AsyncHttpClient myClient;

        myClient = new AsyncHttpClient();
        myClient.setTimeout(30000);
        PersistentCookieStore myCookieStore = new PersistentCookieStore(mContext);
        myClient.setCookieStore(myCookieStore);

        String url = mContext.getString(R.string.url_set_by_friend_allo);
        RequestParams params = new RequestParams();
        final SingleToneData singleToneData = SingleToneData.getInstance();
        params.put("token", singleToneData.getToken());
        params.put("friend_id", singleToneData.getCurrentByFriend().getId());
        params.put("allo_uid", allo.getId());

        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("HTTP RESPONSE......getAlloList", new String(responseBody));
                alloJSONUtils.parseSetByFriendAllo(new String(responseBody));
                Friend currentFriend = singleToneData.getCurrentByFriend();
                currentFriend.setAllo(allo);
                singleToneData.setCurrentByFriend(currentFriend);
                ((ByChangeActivity)context).setUI();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

    }


}
