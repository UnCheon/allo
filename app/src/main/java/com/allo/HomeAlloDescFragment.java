package com.allo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by baek_uncheon on 2015. 6. 11..
 */
public class HomeAlloDescFragment extends Fragment {
    Context context;
    int i_turn;

    public void setContext(Context context) {
        this.context = context;
    }
    public void setTurn(int i_turn) {this.i_turn = i_turn; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        int i_resource = 0;

        switch (i_turn){
            case 0:
                i_resource = R.layout.fragment_home_allo_desc_0;
                break;
            case 1:
                i_resource = R.layout.fragment_home_allo_desc_1;
                break;
            case 2:
                i_resource = R.layout.fragment_home_allo_desc_2;
                break;
        }

        View view = inflater.inflate(i_resource, container, false);


        return view;
    }
}


