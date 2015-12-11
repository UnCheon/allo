package com.allo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by baek_uncheon on 2015. 7. 27..
 */
public class ConfigFragment extends Fragment {
    Context context;

    ImageView iv_back;

    LinearLayout ll_help;
    LinearLayout ll_q;
    LinearLayout ll_password;
    LinearLayout ll_secede;


    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_config, container, false);

        AnalyticsApplication application = (AnalyticsApplication) ((Activity)context).getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName("ConfigFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        setLayout(view);
        setListener();

        return view;
    }

    private void setLayout(View view) {

        iv_back = (ImageView) view.findViewById(R.id.iv_back);

        ll_help = (LinearLayout) view.findViewById(R.id.ll_help);
        ll_q = (LinearLayout) view.findViewById(R.id.ll_q);
        ll_password = (LinearLayout) view.findViewById(R.id.ll_password);
        ll_secede = (LinearLayout) view.findViewById(R.id.ll_secede);

    }

    private void setListener() {


        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).openDrawerLayout();
            }
        });

        ll_help.setOnClickListener(configClickListener);
        ll_q.setOnClickListener(configClickListener);
        ll_password.setOnClickListener(configClickListener);
        ll_secede.setOnClickListener(configClickListener);

    }

    View.OnClickListener configClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = null;

            switch (view.getId()) {
                case R.id.ll_help:
                    intent = new Intent(context, HelpFragmentActivity.class);
                    intent.putExtra("index", "basic");
                    break;
                case R.id.ll_q:
                    intent = new Intent(context, ConfigQuestionActivity.class);
                    break;
                case R.id.ll_password:
                    intent = new Intent(context, ConfigPwActivity.class);
                    break;
                case R.id.ll_secede:
                    intent = new Intent(context, ConfigSecedeActivity.class);
                    break;
            }

            if (intent != null)
                startActivity(intent);
        }
    };

    @Override
    public void onStart(){
        super.onStart();
        GoogleAnalytics.getInstance(context).reportActivityStart((Activity)context);
    }

    @Override
    public void onStop(){
        super.onStop();
        GoogleAnalytics.getInstance(context).reportActivityStop((Activity) context);
    }
}
