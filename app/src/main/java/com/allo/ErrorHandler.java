package com.allo;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by baek_uncheon on 2015. 7. 20..
 */
public class ErrorHandler {

    Context context;

    public ErrorHandler(Context context) {
        this.context = context;
    }


    public void handleErrorCode(JSONObject jo_result) {
        String st_error_code = "";

        try {
            JSONObject jo_error = jo_result.getJSONObject("error");
            int i_status_code = Integer.parseInt(jo_error.getString("statusCode"));
            Log.i("error code ", String.valueOf(i_status_code));

            switch (i_status_code) {
                case 101:
                    st_error_code = "필요 파라미터가 없음";
                    break;
                case 102:
                    st_error_code = "파라미터 타입값 미일치";
                    break;
                case 103:
                    st_error_code = "로그인 실패(아이디 없음)";
                    break;
                case 104:
                    st_error_code = "로그인 실패(비밀번호 오류)";
                    break;
                case 105:
                    st_error_code = "토큰 오류";
                    break;
                case 106:
                    st_error_code = "보유하고 있지 않은 알로를 내 알로로 설정";
                    break;
                case 107:
                    st_error_code = "해당 전화번호의 소유자가 없음";
                    break;
                case 108:
                    st_error_code = "존재하지 않는 알로를 구매하려고 함";
                    break;
                case 109:
                    st_error_code = "이미 구입한 알로입니다.";
                    break;
                case 110:
                    st_error_code = "알료 이용권이 부족합니다.";
                    break;
                case 111:
                    st_error_code = "업로드는 20MB까지만 가능합니다.";
                    break;
                case 112:
                    st_error_code = "이미 초대한 전화번호";
                    break;
                case 113:
                    st_error_code = "중복 전화번호입니다.";
                    break;
                default:
                    st_error_code = "error";

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        toastErrorMessage(st_error_code);

    }

    private void toastErrorMessage(String st_message) {
        Toast.makeText(context, st_message, Toast.LENGTH_SHORT).show();
    }

}
