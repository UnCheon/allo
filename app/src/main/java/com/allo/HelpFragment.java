package com.allo;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Created by baek_uncheon on 2015. 3. 26..
 */
public class HelpFragment extends Fragment {
    TextView tv_desc;
    TextView tv_title;
    String st_index;

    ViewPager vp_question;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question, container, false);

        setLayout(view);
        setDesc();

        return view;
    }

    public void setIndex(String st_index) {
        this.st_index = st_index;
    }

    public void setViewPager(ViewPager vp) {
        this.vp_question = vp;
    }


    private void setLayout(View view) {
        tv_desc = (TextView) view.findViewById(R.id.tv_desc);
        tv_title = (TextView) view.findViewById(R.id.tv_title);
    }

    private void setDesc() {
        String st_desc = "cash";
        switch (st_index) {
            case "cash":
                tv_title.setText("알로 이용권이란?");
                st_desc = "알로 이용권이란 알로 스토어에 있는 음악 중 1곡을 구매하거나 선물할 수 있는 이용권입니다.\n\n보유한 알로는 기본 알로나 친구별 알로를 통해 통화연결음으로 사용할 수 있습니다. UCC의 경우 알로 이용권 없이도 이용하실 수 있습니다.\n" +
                        "\n" +
                        "* 현재는 이벤트 기간 중으로 3일마다 무료로 알로 이용권이 충전됩니다.";
                break;
            case "basic":
                tv_title.setText("기본 알로란?");
                st_desc = "기본 알로는 통화 상대방이 알로 유저가 아니거나 알로 유저가 전화를 걸 경우에 재생되는 통화연결음입니다.\n\n최대 4개의 기본 알로를 설정할 수 있으며 설정된 기본 알로 중 1곡이 랜덤하게 재생됩니다.";
                break;
            case "friend":
                tv_title.setText("친구별 알로란?");
                st_desc = "친구별 알로는 특정한 알로 친구에게 들려주고 싶은 알로를 설정할 수 있는 기능입니다.\n\n친구 A에게 알로 B를 친구별 알로로 설정하시면 친구 A가 전화를 걸어오면 기본 알로와 상관없이 알로 B를 재생해주는 기능입니다.";
                break;
        }
        tv_desc.setText(st_desc);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
