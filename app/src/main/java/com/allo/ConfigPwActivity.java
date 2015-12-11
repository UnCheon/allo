package com.allo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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


public class ConfigPwActivity extends Activity {

    ImageView iv_back;

    EditText et_original_pw;
    EditText et_change_pw;
    EditText et_change_pw_2;

    Button btn_confirm;


    ProgressDialog pd = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName("ConfigPwActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());


        setLayout();
        setListener();


    }

    private void setLayout() {

        iv_back = (ImageView) findViewById(R.id.iv_back);

        et_original_pw = (EditText) findViewById(R.id.et_original_pw);
        et_change_pw = (EditText) findViewById(R.id.et_change_pw);
        et_change_pw_2 = (EditText) findViewById(R.id.et_change_pw_2);

        btn_confirm = (Button) findViewById(R.id.btn_confirm);

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
                confirm();
            }
        });
    }

    private void confirm() {
        String st_original_pw = et_original_pw.getText().toString();
        String st_change_pw = et_change_pw.getText().toString();
        String st_change_pw_2 = et_change_pw_2.getText().toString();

        if (st_original_pw.equals("")) {
            Toast.makeText(this, "기존 비밀번호를 입력하여 주십시오.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (st_change_pw.equals("")) {
            Toast.makeText(this, "변경하실 비밀번호를 입력하여 주십시오.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!(st_change_pw.equals(st_change_pw_2))) {
            Toast.makeText(this, "변경하실 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }


        AsyncHttpClient myClient;

        myClient = new AsyncHttpClient();
        myClient.setTimeout(SingleToneData.getInstance().getTimeOutValue());
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        myClient.setCookieStore(myCookieStore);

        String url = getString(R.string.url_change_pw);

        RequestParams params = new RequestParams();
        LoginUtils loginUtils = new LoginUtils(this);
        if (!loginUtils.getPw().equals(st_original_pw)){
            Toast.makeText(ConfigPwActivity.this, "원래 비밀번호가 맞지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }


        params.put("id", loginUtils.getId());
        params.put("pw", st_original_pw);
        params.put("new_pw", st_change_pw);

        pd = ProgressDialog.show(ConfigPwActivity.this, "", ConfigPwActivity.this.getString(R.string.wait_pw_change), true);

        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pd.dismiss();
                Log.i("(friend fragment)", new String(responseBody));
                confirmFinish(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pd.dismiss();
                Toast.makeText(ConfigPwActivity.this, getResources().getString(R.string.on_failure), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmFinish(String st_response_body) {
        try {
            JSONObject jo_result = new JSONObject(st_response_body);
            String st_status = jo_result.getString("status");
            if (st_status.equals("success")) {
                Toast.makeText(this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                SharedPreferences pref = getSharedPreferences("userInfo", MODE_PRIVATE);
                String st_id = pref.getString("id", "");
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                editor.putString("id", st_id);
                editor.putString("pw", et_change_pw.getText().toString());

                editor.commit();

                finish();

            } else {
                ErrorHandler errorHandler = new ErrorHandler(ConfigPwActivity.this);
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
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
}
