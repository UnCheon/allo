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


public class ConfigSecedeActivity extends Activity {
    ImageView iv_back;

    EditText et_pw;


    Button btn_confirm;

    ProgressDialog pd = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secede);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName("ConfigSecedeActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());


        setLayout();
        setListener();


    }

    private void setLayout() {
        iv_back = (ImageView) findViewById(R.id.iv_back);

        et_pw = (EditText) findViewById(R.id.et_pw);

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
        String st_pw = et_pw.getText().toString();

        if (st_pw.equals("")) {
            Toast.makeText(this, "비밀번호를 입력하여 주십시오.", Toast.LENGTH_SHORT).show();
            return;
        }


        AsyncHttpClient myClient;

        myClient = new AsyncHttpClient();
        myClient.setTimeout(SingleToneData.getInstance().getTimeOutValue());
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        myClient.setCookieStore(myCookieStore);

        String url = getString(R.string.url_secede);

        RequestParams params = new RequestParams();
        LoginUtils loginUtils = new LoginUtils(this);

        params.put("id", loginUtils.getId());
        params.put("pw", st_pw);

        pd = ProgressDialog.show(ConfigSecedeActivity.this, "", ConfigSecedeActivity.this.getString(R.string.wait_secede), true);
        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pd.dismiss();
                Log.i("(secede fragment)", new String(responseBody));
                confirmFinish(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pd.dismiss();
                Toast.makeText(ConfigSecedeActivity.this, getResources().getString(R.string.on_failure), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmFinish(String st_response_body) {
        try {
            JSONObject jo_result = new JSONObject(st_response_body);
            String st_status = jo_result.getString("status");
            if (st_status.equals("success")) {
                Toast.makeText(this, "탈퇴되었습니다.\n이용해주셔서 감사합니다", Toast.LENGTH_LONG).show();
                SharedPreferences pref = getSharedPreferences("userInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                editor.commit();

                ContactDBOpenHelper contactDBOpenHelper = new ContactDBOpenHelper(ConfigSecedeActivity.this);
                contactDBOpenHelper.open_writableDatabase();
                contactDBOpenHelper.deleteAllContacts();
                contactDBOpenHelper.close();

                moveTaskToBack(true);
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());

            } else {
                ErrorHandler errorHandler = new ErrorHandler(ConfigSecedeActivity.this);
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
