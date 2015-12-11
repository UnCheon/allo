package com.allo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.florent37.materialviewpager.adapter.RecyclerViewMaterialAdapter;
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


public class SearchActivity extends Activity {

    ImageView iv_back;
    ImageView iv_search;
    ImageView iv_request;
    EditText et_search;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    ArrayList<Allo> ar_allo = null;

    ProgressDialog pd = null;

    Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("SearchActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

//        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        getWindow().getAttributes().windowAnimations = R.style.AlloClickDialogAnimation;
        setLayout();
        setListener();
    }

    private void setLayout() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_request = (ImageView) findViewById(R.id.iv_request);
        iv_search = (ImageView) findViewById(R.id.iv_search);
        et_search = (EditText) findViewById(R.id.et_search);
    }

    private void setListener() {
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("search").setAction("search_btn click").build());
                search();
            }
        });

        iv_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, RequestSongActivity.class);
                startActivity(intent);
            }
        });

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et_search, InputMethodManager.SHOW_FORCED);

        et_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    if (i == KeyEvent.KEYCODE_ENTER) {
                        search();
                        Log.i("search", "enter key click");
                    }
                }
                return false;
            }
        });

    }

    private void search() {

        if (et_search.getText().toString().equals("")) {
            Toast.makeText(this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        AsyncHttpClient myClient;

        myClient = new AsyncHttpClient();
        myClient.setTimeout(SingleToneData.getInstance().getTimeOutValue());
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        myClient.setCookieStore(myCookieStore);

        String url = getString(R.string.url_search);
        RequestParams params = new RequestParams();

        LoginUtils loginUtils = new LoginUtils(this);

        params.put("id", loginUtils.getId());
        params.put("pw", loginUtils.getPw());
        params.put("title", et_search.getText().toString());


        pd = ProgressDialog.show(SearchActivity.this, "", SearchActivity.this.getString(R.string.wait_search), true);
        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pd.dismiss();
                Log.i("HTTP RESPONSE......", new String(responseBody));
                searchSuccess(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pd.dismiss();
                Toast.makeText(SearchActivity.this, getText(R.string.on_failure), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void searchSuccess(String st_response_body) {
        try {
            JSONObject jo_result = new JSONObject(st_response_body);
            JSONObject jo_response = jo_result.getJSONObject("response");
            JSONArray ja_allo_list = jo_response.getJSONArray("allo_list");
            String status = jo_result.getString("status");
            if (status.equals("success")) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);

                ar_allo = new ArrayList<>();
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

                    ar_allo.add(allo);
                }

            } else if (status.equals("fail")) {
                ErrorHandler errorHandler = new ErrorHandler(SearchActivity.this);
                errorHandler.handleErrorCode(jo_result);
            }

        } catch (JSONException e) {
            Toast.makeText(SearchActivity.this, "Json Error", Toast.LENGTH_SHORT).show();
        }

        if (ar_allo.size() == 0) {
            String msg = "\n검색결과가 없습니다.\n곡 요청을 원하시면 곡 요청을 눌러주세요.\n";

            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(SearchActivity.this);
            alert_confirm.setTitle("알로 검색").setMessage(msg).setCancelable(false).setPositiveButton("확인",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                        }
                    });
            AlertDialog alert = alert_confirm.create();
            alert.show();
        }

        mAdapter = new RecyclerViewMaterialAdapter(new StoreRecyclerViewAdapter(SearchActivity.this, ar_allo));
        mRecyclerView.setAdapter(mAdapter);
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
