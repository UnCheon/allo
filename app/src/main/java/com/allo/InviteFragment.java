package com.allo;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by baek_uncheon on 2015. 7. 27..
 */
public class InviteFragment extends Fragment {
    Context context;


    ArrayList<Friend> al_friend;


    ImageView iv_back;


    ListView lv_invite;


    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite, container, false);




        setLayout(view);
        setListener();

        getFriend();

        return view;
    }

    private void setLayout(View view) {


        iv_back = (ImageView) view.findViewById(R.id.iv_back);

        lv_invite = (ListView) view.findViewById(R.id.lv_invite);

    }

    private void setListener() {

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).openDrawerLayout();
            }
        });


    }


    private void getFriend() {
        AsyncHttpClient myClient;

        myClient = new AsyncHttpClient();
        myClient.setTimeout(SingleToneData.getInstance().getTimeOutValue());
        PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
        myClient.setCookieStore(myCookieStore);

        String url = context.getString(R.string.url_allo_friend);

        RequestParams params = new RequestParams();
        LoginUtils loginUtils = new LoginUtils(context);

        params.put("id", loginUtils.getId());
        params.put("pw", loginUtils.getPw());


        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("(friend fragment)", new String(responseBody));
                getFriendFinish(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, getResources().getString(R.string.on_failure), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getFriendFinish(String st_response_body) {
        try {
            JSONObject jo_result = new JSONObject(st_response_body);
            String st_status = jo_result.getString("status");
            if (st_status.equals("success")) {
                JSONObject jo_response = jo_result.getJSONObject("response");

                JSONArray ja_friend_list = jo_response.getJSONArray("friend_list");

                al_friend = new ArrayList<>();

                for (int i = 0; i < ja_friend_list.length(); i++) {
                    JSONObject jo_friend = ja_friend_list.getJSONObject(i);
                    Friend friend = new Friend();
                    if (jo_friend.has("nickname"))
                        friend.setNickname(jo_friend.getString("nickname"));
                    if (jo_friend.has("phone_number"))
                        friend.setPhoneNumber(jo_friend.getString("phone_number"));

                    al_friend.add(friend);
                }

            } else {
                ErrorHandler errorHandler = new ErrorHandler(context);
                errorHandler.handleErrorCode(jo_result);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        ContactSync contactSync = new ContactSync(context);
        ArrayList<Contact> ar_contact = contactSync.getContactList();

        Log.i("contact size", String.valueOf(ar_contact.size()));

        for (int i = 0; i < ar_contact.size(); i++) {
            Contact contact = ar_contact.get(i);
            for (int j = 0; j < al_friend.size(); j++) {
                Friend friend = al_friend.get(j);
                Log.i("phoneumber", contact.getPhonenum() + ":" + friend.getPhoneNumber());

                if (contact.getPhonenum().equals(friend.getPhoneNumber())) {
                    ar_contact.remove(contact);
                }
            }
        }

        Log.i("contact size", String.valueOf(ar_contact.size()));


        if (ar_contact.size() == 0) {

        } else {
            InviteAdapter adapter = new InviteAdapter(context, R.layout.invite_list_item, ar_contact);
            lv_invite.setAdapter(adapter);
        }


    }
}
