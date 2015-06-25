package com.allo;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class UCCFragment extends Fragment {

    Context context;
    ImageView iv_side_menu;
    Button btn_start_ucc;

    public UCCFragment() {
        // Required empty public constructor
    }

    public void setContext(Context context){
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ucc, container, false);

        // Inflate the layout for this fragment

        setLayout(view);
        setListener();

        return view;

    }

    private void setLayout(View v){

        iv_side_menu = (ImageView) v.findViewById(R.id.iv_side_menu);
        btn_start_ucc = (Button) v.findViewById(R.id.btn_start_ucc);
    }

    private void setListener(){
        iv_side_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)context).openDrawerLayout();
            }
        });
        btn_start_ucc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnStartUcc();
            }
        });
    }
    private void btnStartUcc() {
        Intent intent = new Intent(context, RecordActivity.class);
        context.startActivity(intent);


    }
}
