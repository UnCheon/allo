package com.allo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

/**
 * Created by baek_uncheon on 2015. 7. 23..
 */
public class NoticeFragment extends Fragment {


    Context context;
    Notice notice;

    public void setContext(Context context) {
        this.context = context;
    }

    public void setNotice(Notice notice) {
        this.notice = notice;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_noti, container, false);

        ImageView iv_noti = (ImageView) view.findViewById(R.id.iv_noti);


        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.notice1)
                .showImageForEmptyUri(R.drawable.notice1)
                .showImageOnFail(R.drawable.notice1)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(0)).build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited())
            imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        imageLoader.displayImage(notice.getImageUrl(), iv_noti, options);

        iv_noti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(context);
                alert_confirm.setTitle("공지사항").setMessage(Html.fromHtml(notice.getValue()))
                        .setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // 'No'
                                        return;
                                    }
                                });
                AlertDialog alert = alert_confirm.create();
                alert.show();


//                Toast.makeText(context, notice.getValue(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}