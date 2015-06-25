package com.allo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
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

/**
 * Created by baek_uncheon on 2015. 5. 5..
 */
public class MainActivity extends FragmentActivity {

    private ListView lvNavList;
    private LinearLayout llContainer;
    private DrawerLayout dlDrawer;

    Fragment currentFragment;
    HomeFragment homeFragment;
    StoreFragment storeFragment;
    ByFriendAlloFragment byFriendAlloFragment;
    MyAlloFragment myAlloFragment;
    UCCFragment uCCFragment;
    MyAlloNewFragment myAlloNewFragment;


    private String[] navItems = {"홈으로 이동", "나만의 알로(UCC)", "알로 설정", "친구들 알로 보기"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setlayout();
        initFragment();
        newContactUpload();
        fragmentReplace(homeFragment);
    }

    private void setlayout(){
        lvNavList = (ListView)findViewById(R.id.lv_nav_list);
        llContainer = (LinearLayout)findViewById(R.id.ll_container);

        lvNavList.setAdapter(
                new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, navItems));
        lvNavList.setOnItemClickListener(new DrawerItemListener());

        dlDrawer = (DrawerLayout)findViewById(R.id.dl_activity_main_drawer);
    }

    private void initFragment(){
        homeFragment = new HomeFragment();
        homeFragment.setContext(this);

        storeFragment = new StoreFragment();
        storeFragment.setContext(this);

        uCCFragment = new UCCFragment();
        uCCFragment.setContext(this);

        myAlloFragment = new MyAlloFragment();
        myAlloFragment.setContext(this);



        byFriendAlloFragment = new ByFriendAlloFragment();
        byFriendAlloFragment.setContext(this);

        byFriendAlloFragment = new ByFriendAlloFragment();
        byFriendAlloFragment.setContext(this);

        myAlloNewFragment = new MyAlloNewFragment();
        myAlloNewFragment.setContext(this);

    }

    private void newContactUpload(){
        ContactDBOpenHelper mContactDBOpenHelper = new ContactDBOpenHelper(getApplicationContext());
        mContactDBOpenHelper.open_writableDatabase();

        Cursor mCursor;
        mCursor = mContactDBOpenHelper.getNewContacts();

        if(mCursor.getCount() == 0)
            return;

        JSONArray ja_phone_number_list = new JSONArray();

        while(mCursor.moveToNext()){
            try{
                JSONObject contact_json = new JSONObject();
                contact_json.put("phone_number", mCursor.getString(1));
                ja_phone_number_list.put(contact_json);

            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        mContactDBOpenHelper.close();


        String url = getApplicationContext().getString(R.string.url_sync_contact);

        AsyncHttpClient myClient = new AsyncHttpClient();
        myClient.setTimeout(30000);

        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        myClient.setCookieStore(myCookieStore);

        RequestParams params = new RequestParams();

        SingleToneData singleToneData = SingleToneData.getInstance();
        params.put("token", singleToneData.getToken());
        params.put("phone_number_list", ja_phone_number_list);




        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("HTTP RESPONSE......", new String(responseBody));
                try {
                    JSONObject jo_response = new JSONObject(new String(responseBody));
                    String st_status = jo_response.getString("status");
                    if (st_status.equals("success")) {
                        ContactDBOpenHelper mContactDBOpenHelper = new ContactDBOpenHelper(getApplicationContext());
                        mContactDBOpenHelper.open_writableDatabase();
                        mContactDBOpenHelper.updateContacts();
                        mContactDBOpenHelper.close();
                    } else if (st_status.equals("fail")) {
                        String st_error = jo_response.getString("error");
                        if (st_error.equals("not login")) {
                            LoginUtils loginUtils = new LoginUtils(getApplicationContext());
                            loginUtils.onLoginRequired();
                        }
                    }

                } catch (JSONException e) {

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), "네트워크 상태를 확인하세요.", Toast.LENGTH_SHORT).show();

            }
        });

    }




    private class DrawerItemListener implements ListView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            moveFragment(position);
            dlDrawer.closeDrawer(lvNavList);
        }
    }


    public void moveFragment(int position){
        switch (position) {
            case 0:
                fragmentReplace(homeFragment);
                break;
            case 1:
                fragmentReplace(uCCFragment);
                break;
            case 2:
                fragmentReplace(myAlloNewFragment);
                break;
            case 3:
                fragmentReplace(myAlloNewFragment);
                break;
            case 4:
                fragmentReplace(byFriendAlloFragment);
                break;
            case 5:
                fragmentReplace(myAlloNewFragment);
                break;
            case 6:
                fragmentReplace(myAlloFragment);
                break;
            case 7:
                fragmentReplace(myAlloFragment);
                break;
            case 8:
                fragmentReplace(myAlloFragment);
                break;
        }
    }

    public void openDrawerLayout(){
        dlDrawer.openDrawer(lvNavList);
    }

    public void fragmentReplace(Fragment newFragment) {
        currentFragment = newFragment;

        // replace fragment
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.ll_container, newFragment);

        // Commit the transaction
        transaction.commit();

    }

    @Override
    public void onBackPressed() {
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (currentFragment == homeFragment){
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(MainActivity.this);
            alert_confirm.setMessage("프로그램을 종료 하시겠습니까?").setCancelable(false).setPositiveButton("확인",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            moveTaskToBack(true);
                            finish();
                            android.os.Process.killProcess(android.os.Process.myPid());

                            // 'YES'
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
        }else{
            moveFragment(0);
        }


    }
}
