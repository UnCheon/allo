package com.allo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by baek_uncheon on 2015. 3. 25..
 */
public class AgreeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);

    }

    public void agreeBtn(View v){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);

        finish();
    }
}
