package com.allo;

import android.content.Context;
import android.util.Log;

import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.util.KakaoParameterException;

/**
 * Created by uncheon on 2015. 8. 20..
 */
public class KaKaoInvite {
    Context context;

    public KaKaoInvite(Context context){
        this.context = context;
    }

    public boolean sendMessage(){
        try {



            LoginUtils loginUtils = new LoginUtils(context);

            String st_nickname = loginUtils.getNickname();
            String st_title = loginUtils.getMyAlloTitle();
            String st_artist = loginUtils.getMyAlloArtist();
            String st_image = loginUtils.getMyAlloImage();

            Log.i("kakao talk", "title : "+st_title + ", image : "+st_image);

            String msg = "기다림을 즐겨라! "+
                        "지금 바로 무료 컬러링 '알로'를 사용해보세요! ";

            if (!st_title.equals(""))
                msg = msg + "'"+st_nickname+
                        "'님이 설정한 통화연결음 '"+
                        st_title+"'을(를) 들어보시라고 초대하셨습니다.";



            final KakaoLink kakaoLink = KakaoLink.getKakaoLink(context);
            final KakaoTalkLinkMessageBuilder messageBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();
            messageBuilder.addText(msg);
            if (!st_image.equals(""))
                messageBuilder.addImage(st_image, 240, 240);
            messageBuilder.addAppButton("앱으로 이동");
//            messageBuilder.addAppButton("앱으로 이동",
//                    new AppActionBuilder()
//                            .addActionInfo(AppActionInfoBuilder
//                                    .createAndroidActionInfoBuilder()
//                                    .setExecuteParam("execparamkey1=1111")
//                                    .setMarketParam("referrer=kakaotalklink")
//                                    .build())
//                            .addActionInfo(AppActionInfoBuilder
//                                    .createiOSActionInfoBuilder()
//                                    .setExecuteParam("execparamkey1=1111")
//                                    .build())
//                            .setUrl("your-website url") // PC 카카오톡 에서 사용하게 될 웹사이트 주소
//                            .build());



            final String linkContents = messageBuilder.build();
            kakaoLink.sendMessage(linkContents, context);



        } catch (KakaoParameterException e) {
            e.printStackTrace();
        }

        return true;
    }
}
