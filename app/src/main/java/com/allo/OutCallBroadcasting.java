package com.allo;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class OutCallBroadcasting extends BroadcastReceiver{
	private static int pState = TelephonyManager.CALL_STATE_IDLE;

    public String TAG = getClass().getSimpleName();

    @Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
        String action = intent.getAction();
        Bundle bundle = intent.getExtras();

        if(action.equals("android.intent.action.PHONE_STATE")){

            String state = bundle.getString(TelephonyManager.EXTRA_STATE);

            if(state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                Intent mIntent = new Intent(context, BackgroundService.class);
                context.stopService(mIntent);
                Log.d(TAG, " EXTRA_STATE_IDLE ");

            }else if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                Log.d(TAG, " EXTRA_STATE_RINGING INCOMMING NUMBER : " + bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER));

            }else if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
                Log.d(TAG, " EXTRA_STATE_OFFHOOK ");
            }
        }else if(action.equals(Intent.ACTION_NEW_OUTGOING_CALL)){

            Log.d(TAG, " OUTGOING CALL : " + bundle.getString(Intent.EXTRA_PHONE_NUMBER));
            Log.d(TAG, " OUTGOING CALL : " + bundle.getString(Intent.EXTRA_PHONE_NUMBER));

            String phone_number = bundle.getString(Intent.EXTRA_PHONE_NUMBER);
            Intent mIntent = new Intent(context, BackgroundService.class);
            mIntent.putExtra("phone_number", phone_number);

            context.startService(mIntent);
        }
    }
}





/*
		TelephonyManager mTelephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);


		mTelephonyManager.listen(new PhoneStateListener(){
            public void onPreciseCallStateChanged(PreciseCallState callState){}


			public void onCallStateChanged(int state, String incomingNumber){
                if(state == TelephonyManager.CALL_STATE_IDLE)
                    Log.i("state", "IDLE");
                else if(state == TelephonyManager.CALL_STATE_OFFHOOK)
                    Log.i("state", "OFFHOOK ");
                else if(state == TelephonyManager.CALL_STATE_RINGING)
                    Log.i("state", "RINGING ");
                TelephonyManager.

				if(state != pState){





					if(state == TelephonyManager.CALL_STATE_IDLE){
						Log.i("state", "IDLE");
						if(pState == TelephonyManager.CALL_STATE_OFFHOOK){
							Intent mIntent = new Intent(context, BackgroundService.class);
							context.stopService(mIntent);

							SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
                    		String ppState = pref.getString("ppState", "");
                    		if (!ppState.equals("RINGING")){
                    		}

                    		SharedPreferences.Editor editor = pref.edit();
                            editor.putString("ppState", "IDLE");
                            editor.commit();

						}
					}else if(state == TelephonyManager.CALL_STATE_OFFHOOK){
						Log.i("state", "OFFHOOK ");
						if(pState == TelephonyManager.CALL_STATE_IDLE){
							Log.i("state", "out call !!");
							SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
							String use = pref.getString("use", "");
							if(use.equals("false")){

							}else{
								Intent mIntent = new Intent(context, BackgroundService.class);
								context.startService(mIntent);
							}

						}
					}else if(state == TelephonyManager.CALL_STATE_RINGING){
                        Log.i("Phone","RINGING");
                        SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
                		SharedPreferences.Editor editor = pref.edit();
                        editor.putString("ppState", "RINGING");
                        editor.commit();
                    }
					pState = state;

				}

			}
		}, PhoneStateListener.LISTEN_CALL_STATE);
    */
