package com.allo;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


/**
 * A simple {@link Fragment} subclass.
 */
public class UCCFragment extends Fragment {

    Context context;
    ImageView iv_side_menu;
    Button btn_start_ucc;

    Tracker mTracker;

    public UCCFragment() {
        // Required empty public constructor
    }

    public void setContext(Context context) {
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ucc, container, false);

        AnalyticsApplication application = (AnalyticsApplication) ((Activity)context).getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("UCCFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        // Inflate the layout for this fragment

        setLayout(view);
        setListener();

        return view;

    }

    private void setLayout(View v) {

        iv_side_menu = (ImageView) v.findViewById(R.id.iv_side_menu);
        btn_start_ucc = (Button) v.findViewById(R.id.btn_start_ucc);
    }

    private void setListener() {
        iv_side_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).openDrawerLayout();
            }
        });
        btn_start_ucc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("UCC").setAction("start_ucc click").build());
                btnStartUcc();
            }
        });
    }

    private void btnStartUcc() {
        Intent intent = new Intent(context, RecordActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onStart(){
        super.onStart();
        GoogleAnalytics.getInstance(context).reportActivityStart((Activity) context);
    }

    @Override
    public void onStop(){
        super.onStop();
        GoogleAnalytics.getInstance(context).reportActivityStop((Activity) context);
    }
}
