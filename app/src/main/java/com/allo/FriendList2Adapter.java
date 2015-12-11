package com.allo;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class FriendList2Adapter extends ArrayAdapter<Friend> {

    private int resId;
    private ArrayList<Friend> ar_contact;

    private LayoutInflater Inflater;
    private Context context;
    public int listCount = 0;
    Allo allo;

    ProgressDialog pd = null;

    public FriendList2Adapter(Context context, int textViewResourceId, List<Friend> objects, Allo allo) {
        super(context, textViewResourceId, objects);
        this.context = context;
        resId = textViewResourceId;
        ar_contact = (ArrayList<Friend>) objects;
        listCount = ar_contact.size();
        this.allo = allo;
        Inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


    }


    @Override
    public View getView(int position, View v, ViewGroup parent) {
        ViewHolder holder;
        if (v == null) {
            v = Inflater.inflate(resId, null);
            holder = new ViewHolder();
            holder.tv_friend_name = (TextView) v.findViewById(R.id.tv_friend_name);
            holder.tv_phone_number = (TextView) v.findViewById(R.id.tv_phone_number);
            holder.btn_gift = (Button) v.findViewById(R.id.btn_gift);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        final Friend friend = ar_contact.get(position);


        if (friend != null) {
            holder.tv_friend_name.setText(friend.getNickname());
            holder.tv_phone_number.setText(friend.getPhoneNumber());

            holder.btn_gift.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String msg = "";
                    if (allo.getIsUcc())
                        msg = "\n'" + allo.getTitle() + "'을(를) " + friend.getNickname() + "에게 선물하시겠습니까? \n\n이용권이 소모되지 않습니다.\n";
                    else
                        msg = "\n'" + allo.getTitle() + "'을(를) " + friend.getNickname() + "에게 선물하시겠습니까? \n\n이용권 1개가 사용됩니다.\n";

                    AlertDialog.Builder alert_confirm = new AlertDialog.Builder(context);
                    alert_confirm.setTitle("알로 선물하기").setMessage(msg).setCancelable(false).setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    gift(friend, allo);
                                }
                            }).setNegativeButton("취소",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 'No'
                                    return;
                                }
                            });
                    AlertDialog alert = alert_confirm.create();
                    alert.show();
                }
            });
        }
        return v;
    }

    private void gift(Friend friend, Allo allo) {
        AsyncHttpClient myClient;

        myClient = new AsyncHttpClient();
        myClient.setTimeout(SingleToneData.getInstance().getTimeOutValue());
        PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
        myClient.setCookieStore(myCookieStore);

        String url = context.getString(R.string.url_gift);

        RequestParams params = new RequestParams();
        LoginUtils loginUtils = new LoginUtils(context);

        params.put("id", loginUtils.getId());
        params.put("pw", loginUtils.getPw());
        params.put("allo_id", allo.getId());
        params.put("friend_phone_number", String.valueOf(friend.getPhoneNumber()));
        params.put("msg", "선물받으세요~");


        pd = ProgressDialog.show(context, "", context.getString(R.string.wait_gift), true);
        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pd.dismiss();
                Log.i("(friend fragment)", new String(responseBody));
                getFriendFinish(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pd.dismiss();
                System.out.print(new String(responseBody));
                Toast.makeText(context, context.getString(R.string.on_failure), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getFriendFinish(String st_response_body) {
        try {
            JSONObject jo_result = new JSONObject(st_response_body);
            String st_status = jo_result.getString("status");
            if (st_status.equals("success")) {
                JSONObject jo_response = jo_result.getJSONObject("response");
                if (jo_response.has("cash")) {
                    int i_cash = jo_response.getInt("cash");
                    SingleToneData.getInstance().setCash(String.valueOf(i_cash));
                }

                String msg = "\n선물하기가 완료되었습니다.\n";

                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(context);
                alert_confirm.setTitle("선물하기").setMessage(msg).setCancelable(false).setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity) context).finish();
                                ((MainActivity) MainActivity.mainContext).onReloadGiftMainFragment();
                                return;
                            }
                        });
                AlertDialog alert = alert_confirm.create();
                alert.show();

            } else {
                ErrorHandler errorHandler = new ErrorHandler(context);
                errorHandler.handleErrorCode(jo_result);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private class ViewHolder {
        TextView tv_friend_name;
        TextView tv_phone_number;
        Button btn_gift;

    }
}