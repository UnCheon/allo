package com.allo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;


public class RequestSongActivity extends Activity {
    ImageView iv_back;

    EditText et_title;
    EditText et_artist;

    Button btn_confirm;

    ProgressDialog pd = null;

    Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_song);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("RequestSongActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());


        setLayout();
        setListener();


    }

    private void setLayout() {
        iv_back = (ImageView) findViewById(R.id.iv_back);

        et_title = (EditText) findViewById(R.id.et_title);
        et_artist = (EditText) findViewById(R.id.et_artist);

        btn_confirm = (Button) findViewById(R.id.btn_confirm);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et_title, InputMethodManager.SHOW_FORCED);

    }

    private void setListener() {


        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("request").setAction("request_btn click").build());
                confirm();
            }
        });
    }

    private void confirm() {
        String st_title = et_title.getText().toString();
        String st_artist = et_artist.getText().toString();


        if (st_title.equals("")) {
            Toast.makeText(this, "노래 제목을 입력하여 주십시오.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (st_artist.equals("")) {
            Toast.makeText(this, "가수이름을 입력하여 주십시오.", Toast.LENGTH_SHORT).show();
            return;
        }


        AsyncHttpClient myClient;

        myClient = new AsyncHttpClient();
        myClient.setTimeout(SingleToneData.getInstance().getTimeOutValue());
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        myClient.setCookieStore(myCookieStore);

        String url = getString(R.string.url_request);

        RequestParams params = new RequestParams();
        LoginUtils loginUtils = new LoginUtils(this);

        params.put("id", loginUtils.getId());
        params.put("pw", loginUtils.getPw());
        params.put("title", st_title);
        params.put("artist", st_artist);


        pd = ProgressDialog.show(RequestSongActivity.this, "", RequestSongActivity.this.getString(R.string.wait_request), true);
        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pd.dismiss();
                Log.i("(request activity)", new String(responseBody));
                confirmFinish(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pd.dismiss();
                Toast.makeText(RequestSongActivity.this, getResources().getString(R.string.on_failure), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmFinish(String st_response_body) {
        try {
            JSONObject jo_result = new JSONObject(st_response_body);
            String st_status = jo_result.getString("status");
            if (st_status.equals("success")) {
                String msg = "\n곡 요청이 등록되었습니다.\n";

                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(RequestSongActivity.this);
                alert_confirm.setTitle("곡 요청하기").setMessage(msg).setCancelable(false).setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RequestSongActivity.this.finish();
                                return;
                            }
                        });
                AlertDialog alert = alert_confirm.create();
                alert.show();


            } else {
                ErrorHandler errorHandler = new ErrorHandler(RequestSongActivity.this);
                errorHandler.handleErrorCode(jo_result);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
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
