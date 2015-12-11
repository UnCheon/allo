package com.allo;

import android.app.Activity;
import android.app.ProgressDialog;
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


public class ConfigQuestionActivity extends Activity {
    ImageView iv_back;

    EditText et_title;
    EditText et_desc;

    Button btn_confirm;

    ProgressDialog pd = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName("ConfigQuestionActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        setLayout();
        setListener();


    }

    private void setLayout() {
        iv_back = (ImageView) findViewById(R.id.iv_back);

        et_title = (EditText) findViewById(R.id.et_title);
        et_desc = (EditText) findViewById(R.id.et_desc);

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
        String st_title = et_title.getText().toString();
        String st_desc = et_desc.getText().toString();


        if (st_title.equals("")) {
            Toast.makeText(this, "제목을 입력하여 주십시오.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (st_desc.equals("")) {
            Toast.makeText(this, "내용을 입력하여 주십시오.", Toast.LENGTH_SHORT).show();
            return;
        }


        AsyncHttpClient myClient;

        myClient = new AsyncHttpClient();
        myClient.setTimeout(SingleToneData.getInstance().getTimeOutValue());
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        myClient.setCookieStore(myCookieStore);

        String url = getString(R.string.url_question);

        RequestParams params = new RequestParams();
        LoginUtils loginUtils = new LoginUtils(this);

        params.put("id", loginUtils.getId());
        params.put("pw", loginUtils.getPw());
        params.put("title", st_title);
        params.put("desc", st_desc);


        pd = ProgressDialog.show(ConfigQuestionActivity.this, "", ConfigQuestionActivity.this.getString(R.string.wait_request), true);
        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pd.dismiss();
                Log.i("(question activity)", new String(responseBody));
                confirmFinish(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pd.dismiss();
                Toast.makeText(ConfigQuestionActivity.this, getResources().getString(R.string.on_failure), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmFinish(String st_response_body) {
        try {
            JSONObject jo_result = new JSONObject(st_response_body);
            String st_status = jo_result.getString("status");
            if (st_status.equals("success")) {
                Toast.makeText(this, "접수되었습니다.\n빠른시일내에 답변드리겠습니다. ", Toast.LENGTH_LONG).show();
                finish();

            } else {
                ErrorHandler errorHandler = new ErrorHandler(ConfigQuestionActivity.this);
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
