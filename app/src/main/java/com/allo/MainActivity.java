package com.allo;

import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by baek_uncheon on 2015. 5. 5..
 */
public class MainActivity extends FragmentActivity {


    private LinearLayout llNavList;
    private LinearLayout llContainer;
    private DrawerLayout dlDrawer;

    private LinearLayout ll_home;
    private LinearLayout ll_set_allo;
    private LinearLayout ll_make_allo;
    private LinearLayout ll_gift;
    private LinearLayout ll_invite;
    private LinearLayout ll_friend;
    private LinearLayout ll_setting;

    private ImageView iv_back;


    Fragment currentFragment;


    MainFragment mainFragment;
    MyAlloFragment myAlloFragment;
    UCCFragment uCCFragment;
    GiftFragment giftFragment;
    InviteFragment inviteFragment;
    FriendFragment friendFragment;
    ConfigFragment configFragment;

    KaKaoInvite kaKaoInvite;

    public static Context mainContext;

    Tracker mTracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("RegisterActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());


        mainContext = this;

        initFragment();
        setLayout();
        setListener();
        newContactUpload();
        fragmentReplace(mainFragment);
    }


    private void initFragment() {
        mainFragment = new MainFragment();
        mainFragment.setContext(this);

        myAlloFragment = new MyAlloFragment();
        myAlloFragment.setContext(this);

        uCCFragment = new UCCFragment();
        uCCFragment.setContext(this);

        giftFragment = new GiftFragment();
        giftFragment.setContext(this);


        kaKaoInvite = new KaKaoInvite(this);
        inviteFragment = new InviteFragment();
        inviteFragment.setContext(this);

        friendFragment = new FriendFragment();
        friendFragment.setContext(this);

        configFragment = new ConfigFragment();
        configFragment.setContext(this);


    }


    private void setLayout() {
        llNavList = (LinearLayout) findViewById(R.id.ll_nav_list);
        llContainer = (LinearLayout) findViewById(R.id.ll_container);
        dlDrawer = (DrawerLayout) findViewById(R.id.dl_activity_main_drawer);

        iv_back = (ImageView) findViewById(R.id.iv_back);
        ll_home = (LinearLayout) findViewById(R.id.ll_home);
        ll_set_allo = (LinearLayout) findViewById(R.id.ll_set_allo);
        ll_make_allo = (LinearLayout) findViewById(R.id.ll_make_allo);
        ll_gift = (LinearLayout) findViewById(R.id.ll_gift);
        ll_invite = (LinearLayout) findViewById(R.id.ll_invite);
        ll_friend = (LinearLayout) findViewById(R.id.ll_friend);
        ll_setting = (LinearLayout) findViewById(R.id.ll_setting);

    }

    private void setListener() {
        iv_back.setOnClickListener(menuClickListener);
        ll_home.setOnClickListener(menuClickListener);
        ll_set_allo.setOnClickListener(menuClickListener);
        ll_make_allo.setOnClickListener(menuClickListener);
        ll_gift.setOnClickListener(menuClickListener);
        ll_invite.setOnClickListener(menuClickListener);
        ll_friend.setOnClickListener(menuClickListener);
        ll_setting.setOnClickListener(menuClickListener);
    }

    View.OnClickListener menuClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_back:
                    mTracker.send(new HitBuilders.EventBuilder().setCategory("MenuClick").setAction("back click").build());
                    break;
                case R.id.ll_home:
                    mTracker.send(new HitBuilders.EventBuilder().setCategory("MenuClick").setAction("Home click").build());
                    moveFragment(0);
                    break;
                case R.id.ll_set_allo:
                    mTracker.send(new HitBuilders.EventBuilder().setCategory("MenuClick").setAction("MyAllo click").build());
                    moveFragment(1);
                    break;
                case R.id.ll_make_allo:
                    mTracker.send(new HitBuilders.EventBuilder().setCategory("MenuClick").setAction("UCC click").build());
                    moveFragment(2);
                    break;
                case R.id.ll_gift:
                    mTracker.send(new HitBuilders.EventBuilder().setCategory("MenuClick").setAction("Gift click").build());
                    moveFragment(3);
                    break;
                case R.id.ll_invite:
                    mTracker.send(new HitBuilders.EventBuilder().setCategory("MenuClick").setAction("Invite click").build());
                    kaKaoInvite.sendMessage();
//                    moveFragment(4);
                    break;
                case R.id.ll_friend:
                    mTracker.send(new HitBuilders.EventBuilder().setCategory("MenuClick").setAction("Friend click").build());
                    moveFragment(5);
                    break;
                case R.id.ll_setting:
                    mTracker.send(new HitBuilders.EventBuilder().setCategory("MenuClick").setAction("Setting click").build());
                    moveFragment(6);
                    break;
            }
            dlDrawer.closeDrawer(llNavList);
        }
    };

    private void newContactUpload() {

        String st_phone_number_list = "";
        ContactDBOpenHelper mContactDBOpenHelper = new ContactDBOpenHelper(getApplicationContext());
        mContactDBOpenHelper.open_writableDatabase();


        Cursor mCursor;
        mCursor = mContactDBOpenHelper.getNewContacts();


        st_phone_number_list = "[";

        while (mCursor.moveToNext()) {
            if (st_phone_number_list.equals("["))
                st_phone_number_list = st_phone_number_list + "\"" + mCursor.getString(1) + "\"";
            else
                st_phone_number_list = st_phone_number_list + ",\"" + mCursor.getString(1) + "\"";

        }

        st_phone_number_list = st_phone_number_list + "]";
        mContactDBOpenHelper.close();


        Log.i("MainActivity phone_number_list", st_phone_number_list);


        if (!st_phone_number_list.equals("[]")){
            String url = getApplicationContext().getString(R.string.url_sync_contact);

            AsyncHttpClient myClient = new AsyncHttpClient();
            myClient.setTimeout(SingleToneData.getInstance().getTimeOutValue());

            PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
            myClient.setCookieStore(myCookieStore);

            RequestParams params = new RequestParams();

            LoginUtils loginUtils = new LoginUtils(getApplicationContext());
            params.put("id", loginUtils.getId());
            params.put("pw", loginUtils.getPw());
            params.put("phone_number_list", st_phone_number_list);
            Log.i("pnl in main", st_phone_number_list);



            myClient.post(url, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                    Log.i("HTTP RESPONSE......", new String(responseBody));
                    try {
                        JSONObject jo_response_body = new JSONObject(new String(responseBody));
                        String st_status = jo_response_body.getString("status");
                        if (st_status.equals("success")) {
                            ContactDBOpenHelper mContactDBOpenHelper = new ContactDBOpenHelper(getApplicationContext());
                            mContactDBOpenHelper.open_writableDatabase();
                            mContactDBOpenHelper.updateContacts();
                            mContactDBOpenHelper.close();
                        } else if (st_status.equals("fail")) {
                            ErrorHandler errorHandler = new ErrorHandler(getApplicationContext());
                            errorHandler.handleErrorCode(jo_response_body);
                        }

                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Json Error", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.on_failure), Toast.LENGTH_SHORT).show();

                }
            });

        }


    }


    private class DrawerItemListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            moveFragment(position);
            dlDrawer.closeDrawer(llNavList);
        }
    }


    public void moveFragment(int position) {
        switch (position) {
            case 0:
                fragmentReplace(mainFragment);
                break;
            case 1:
                fragmentReplace(myAlloFragment);
                break;
            case 2:
                fragmentReplace(uCCFragment);
                break;
            case 3:
                fragmentReplace(giftFragment);
                break;
            case 4:
                fragmentReplace(inviteFragment);
                break;
            case 5:
                fragmentReplace(friendFragment);
                break;
            case 6:
                fragmentReplace(configFragment);
                break;


        }
    }

    public void openDrawerLayout() {
        dlDrawer.openDrawer(llNavList);
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
        if (dlDrawer.isDrawerOpen(llNavList)) {
            dlDrawer.closeDrawer(llNavList);
            return;
        }


        if (currentFragment == mainFragment) {
            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(MainActivity.this);
            alert_confirm.setTitle("종료").setMessage("\n알로를 종료 하시겠습니까?\n").setCancelable(false).setPositiveButton("확인",
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
        } else {
            moveFragment(0);
        }
    }



    public void onReloadPurchaseMainFragment() {
        mainFragment.onReload();
        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(MainActivity.this);
        alert_confirm.setTitle("구매 완료").setMessage("\n알로설정하기로 이동하시겠습니까?\n").setCancelable(false).setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        openDrawerLayout();
                        moveFragment(1);
//                        dlDrawer.closeDrawer(llNavList);

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

    }
    public void onReloadGiftMainFragment() {
        mainFragment.onReload();
    }



    @Override
    protected void onStart(){
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop(){
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }


//    GCM TEST

    private void sendMessage(){
        String st_my_reg_id = "APA91bHfWyz3mkOAxRtvwnfIJp_AkT-g17S7lsdm140cIb8ZnXkaFAv8xJMe5VQeL07-Ruwa-Q0RlVyfCE8OIRH_F-Gd3hZu_bs41Wgos988yCBMFex0wKI";
        String st_to_reg_id = "APA91bG19RmI5Az83pMMrRA42CBbhlj_Fv8pUa51sIneIdfgE8NQRn4B8na22ORUd16xg80QRDEfojFZ3aXskmJmi57tQ8A1BFcf7keXCWc9VYXRtQErLjA";

    }
}
