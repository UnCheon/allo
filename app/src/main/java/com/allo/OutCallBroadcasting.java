package com.allo;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;


public class OutCallBroadcasting extends BroadcastReceiver {
    private static int pState = TelephonyManager.CALL_STATE_IDLE;

    public String TAG = getClass().getSimpleName();

    long l_out_call_time = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle bundle = intent.getExtras();

        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_IDLE) {
                    if (System.currentTimeMillis() - l_out_call_time > 500) {
                        Log.i("idle", "phone_state idle, outcall!!");
                        SingleToneData.getInstance().setPhoneState("IDLE");
                    }
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {

                } else if (state == TelephonyManager.CALL_STATE_RINGING) {
                    Log.i("idle", "phone_state ringing, outcall!!");
                    SingleToneData.getInstance().setPhoneState("IDLE");
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);


        if (action.equals("android.intent.action.PHONE_STATE")) {

            String state = bundle.getString(TelephonyManager.EXTRA_STATE);

            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
//                Log.d(TAG, " EXTRA_STATE_IDLE ");
//                Log.i("idle", "phone_state extra_state_idle, outcall!!");
//                SingleToneData.getInstance().setPhoneState("IDLE");

//                Intent mIntent = new Intent(context, BackgroundService.class);
//                context.stopService(mIntent);


            } else if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                Log.d(TAG, " EXTRA_STATE_RINGING INCOMMING NUMBER : " + bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER));

            } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                Log.d(TAG, " EXTRA_STATE_OFFHOOK ");
            }

        } else if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            Log.d(TAG, " OUTGOING CALL : " + bundle.getString(Intent.EXTRA_PHONE_NUMBER));


            String phone_number = bundle.getString(Intent.EXTRA_PHONE_NUMBER);
            if (!BackgroundService.isInstanceCreated()) {
                Log.i(TAG, "service is not init");

                l_out_call_time = System.currentTimeMillis();
                SingleToneData.getInstance().setPhoneState("OUTGOING");
                SingleToneData.getInstance().setOutCallTime(l_out_call_time);
                Intent mIntent = new Intent(context, BackgroundService.class);
                mIntent.putExtra("phone_number", phone_number);
                context.startService(mIntent);
            } else {
                Log.i(TAG, "service is init");
            }
        }
    }
}



