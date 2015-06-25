package com.allo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by baek_uncheon on 2015. 6. 11..
 */
public class ByFriendAlloFragment extends Fragment {
    Context context;

    ImageView iv_side_munu;
    TextView tv_up;
    ListView lv_by_friend_allo;

    View view_header;

    LinearLayout ll_header_container;

    ArrayList<Friend> ar_by_friend_list;
    ArrayList<Friend> ar_friend_list;

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_by_friend_allo, container, false);

        setLayout(view);
        setListener();

        return view;
    }

    private void setLayout(View v) {
        iv_side_munu = (ImageView) v.findViewById(R.id.iv_side_menu);
        tv_up = (TextView) v.findViewById(R.id.tv_up);
        lv_by_friend_allo = (ListView) v.findViewById(R.id.lv_by_friend_allo);
    }

    private void setListener(){
        iv_side_munu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)context).openDrawerLayout();
            }
        });
        lv_by_friend_allo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clickListViewItem(position);
            }
        });
    }


    private void getByFriendAlloList() {
        AlloHttpUtils alloHttpUtils = new AlloHttpUtils(context);
        alloHttpUtils.getByFriendAllo(this);
    }

    public void setUI() {

        if (view_header != null){
            ll_header_container.removeAllViews();
            lv_by_friend_allo.removeHeaderView(view_header);
        }



        SingleToneData singleToneData = SingleToneData.getInstance();
        ar_by_friend_list = singleToneData.getByFriendAlloList();
        ar_friend_list = new ArrayList<>(singleToneData.getFriendList());

        subFriendList();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (ar_friend_list.size() == 0 && ar_by_friend_list.size() == 0){
            NoFriendAdapter adapter = new NoFriendAdapter(context, R.layout.layout_main_no_friend_item, ar_friend_list);
            lv_by_friend_allo.setAdapter(adapter);
        }else{
            view_header = inflater.inflate(R.layout.layout_main_no_friend_item, null, false);
            if (ar_by_friend_list.size() == 0){

            }else {
                view_header = inflater.inflate(R.layout.layout_by_friend_allo_header, null, false);
                ll_header_container = (LinearLayout) view_header.findViewById(R.id.ll_header_container);

                for (int i = 0; i < ar_by_friend_list.size(); i++) {
                    final Friend byFriend = ar_by_friend_list.get(i);
                    Allo byAllo = byFriend.getAllo();

                    View view_header_info = inflater.inflate(R.layout.layout_by_friend_allo_header_info, null, false);
                    TextView tv_friend_title = (TextView) view_header_info.findViewById(R.id.tv_friend_title);
                    TextView tv_friend_artist = (TextView) view_header_info.findViewById(R.id.tv_friend_artist);
                    TextView tv_friend_nick = (TextView) view_header_info.findViewById(R.id.tv_friend_nick);

                    tv_friend_title.setText(byAllo.getTitle());
                    tv_friend_artist.setText(byAllo.getArtist());
                    tv_friend_nick.setText(byFriend.getNickname());


                    view_header_info.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SingleToneData singleToneData = SingleToneData.getInstance();
                            singleToneData.setCurrentByFriend(byFriend);
                            Intent intent = new Intent(context, ByChangeActivity.class);
                            startActivity(intent);
                        }
                    });



                    ll_header_container.addView(view_header_info);
                }
            }
            lv_by_friend_allo.addHeaderView(view_header);
            ByFriendAlloAdapter adapter = new ByFriendAlloAdapter(context, R.layout.layout_by_friend_allo_item, ar_friend_list);
            lv_by_friend_allo.setAdapter(adapter);
        }
    }



    private void subFriendList(){
        for (int i = 0 ; i < ar_friend_list.size() ; i++){
            Friend friend = ar_friend_list.get(i);
            for (int j = 0 ; j < ar_by_friend_list.size() ; j++ ){
                Friend by_friend = ar_by_friend_list.get(i);
                if (by_friend.getPhoneNumber().equals(friend.getPhoneNumber()))
                    ar_friend_list.remove(friend);
            }

        }

    }


    private void clickListViewItem(int i_position){
        Friend friend = ar_friend_list.get(i_position-1);
        friend.setAllo(null);
        SingleToneData singleToneData = SingleToneData.getInstance();
        singleToneData.setCurrentByFriend(friend);

        Intent intent = new Intent(context, ByChangeActivity.class);
        startActivity(intent);
    }


    @Override
    public void onResume() {
        super.onResume();
        getByFriendAlloList();
    }



}


