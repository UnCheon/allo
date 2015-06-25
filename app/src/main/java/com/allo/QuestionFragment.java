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
public class QuestionFragment extends Fragment {
    TextView tv_desc;
    String st_index;

    ViewPager vp_question;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question, container, false);

        setLayout(view);
        setDesc();

        return view;
    }

    public void setIndex(String st_index){ this.st_index = st_index; }
    public void setViewPager(ViewPager vp){ this.vp_question = vp; }


    private void setLayout(View view){
        tv_desc = (TextView) view.findViewById(R.id.tv_desc);
    }

    private void setDesc() {
        String st_desc = "basic";
        switch (st_index){
            case "basic":
                st_desc = "기본알로 설정은 이런것 입니다.";
                break;
            case "time":
                st_desc = "시간알로 설정은 이런것 입니다.";
                break;
            case "friend":
                st_desc = "친구별알로 설정은 이런것 입니다.";
                break;
        }
        tv_desc.setText(st_desc);
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
