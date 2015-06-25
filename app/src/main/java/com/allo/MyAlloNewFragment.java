package com.allo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by baek_uncheon on 2015. 6. 11..
 */
public class MyAlloNewFragment extends Fragment {
    Context context;

    LinearLayout ll_basic;

    ImageView iv_side_menu;

    ImageView iv_basic_question;
    ImageView iv_time_question;
    ImageView iv_friend_question;

    ImageView iv_basic;

    ImageView iv_time_1;
    ImageView iv_time_2;
    ImageView iv_time_3;

    ImageView iv_friend_1;
    ImageView iv_friend_2;
    ImageView iv_friend_3;
    ImageView iv_friend_4;
    ImageView iv_friend_5;

    TextView tv_basic_title;
    TextView tv_basic_artist;

    TextView tv_time_1;
    TextView tv_time_2;
    TextView tv_time_3;

    TextView tv_friend_1;
    TextView tv_friend_2;
    TextView tv_friend_3;
    TextView tv_friend_4;
    TextView tv_friend_5;

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_allo_new, container, false);

        setLayout(view);
        setListener();

        return view;
    }

    private void setLayout(View view){

        iv_side_menu = (ImageView) view.findViewById(R.id.iv_side_menu);

        ll_basic = (LinearLayout) view.findViewById(R.id.ll_basic);

        iv_basic_question = (ImageView) view.findViewById(R.id.iv_basic_question);
        iv_time_question = (ImageView) view.findViewById(R.id.iv_time_question);
        iv_friend_question = (ImageView) view.findViewById(R.id.iv_friend_question);

        iv_basic = (ImageView) view.findViewById(R.id.iv_basic);

        iv_time_1 = (ImageView) view.findViewById(R.id.iv_time_1);
        iv_time_2 = (ImageView) view.findViewById(R.id.iv_time_2);
        iv_time_3 = (ImageView) view.findViewById(R.id.iv_time_3);

        iv_friend_1 = (ImageView) view.findViewById(R.id.iv_friend_1);
        iv_friend_2 = (ImageView) view.findViewById(R.id.iv_friend_2);
        iv_friend_3 = (ImageView) view.findViewById(R.id.iv_friend_3);
        iv_friend_4 = (ImageView) view.findViewById(R.id.iv_friend_4);
        iv_friend_5 = (ImageView) view.findViewById(R.id.iv_friend_5);

        tv_basic_title = (TextView) view.findViewById(R.id.tv_basic_title);
        tv_basic_artist = (TextView) view.findViewById(R.id.tv_basic_artist);

        tv_time_1 = (TextView) view.findViewById(R.id.tv_time_1);
        tv_time_2 = (TextView) view.findViewById(R.id.tv_time_2);
        tv_time_3 = (TextView) view.findViewById(R.id.tv_time_3);

        tv_friend_1 = (TextView) view.findViewById(R.id.tv_friend_1);
        tv_friend_2 = (TextView) view.findViewById(R.id.tv_friend_2);
        tv_friend_3 = (TextView) view.findViewById(R.id.tv_friend_3);
        tv_friend_4 = (TextView) view.findViewById(R.id.tv_friend_4);
        tv_friend_5 = (TextView) view.findViewById(R.id.tv_friend_5);
    }

    private void setListener(){
        iv_side_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)context).openDrawerLayout();
            }
        });

        iv_basic_question.setOnClickListener(questionListener);
        iv_time_question.setOnClickListener(questionListener);
        iv_friend_question.setOnClickListener(questionListener);

        ll_basic.setOnClickListener(basicListener);

        iv_time_1.setOnClickListener(timeListener);
        iv_time_2.setOnClickListener(timeListener);
        iv_time_3.setOnClickListener(timeListener);

        iv_friend_1.setOnClickListener(friendListener);
        iv_friend_2.setOnClickListener(friendListener);
        iv_friend_3.setOnClickListener(friendListener);
        iv_friend_4.setOnClickListener(friendListener);
        iv_friend_5.setOnClickListener(friendListener);

    }

    View.OnClickListener questionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, QuestionFragmentActivity.class);
            startActivity(intent);
        }
    };


    View.OnClickListener basicListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, ChangeAlloActivity.class);
            intent.putExtra("category", "basic");
            startActivity(intent);
        }
    };


    View.OnClickListener timeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, ChangeAlloActivity.class);
            intent.putExtra("category", "time");
            startActivity(intent);
        }
    };

    View.OnClickListener friendListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, ChangeAlloActivity.class);
            intent.putExtra("category", "friend");
            startActivity(intent);
        }
    };
}


