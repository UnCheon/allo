package com.allo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by baek_uncheon on 2015. 3. 3..
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i("GcmBroadcastReceiver.java | onReceive", "|" + "=================" + "|");
        Bundle bundle = intent.getExtras();
        for (String key : bundle.keySet())
        {
            Object value = bundle.get(key);
            Log.i("GcmBroadcastReceiver.java | onReceive", "|" + String.format("%s : %s (%s)", key, value.toString(), value.getClass().getName()) + "|");
        }
        Log.i("GcmBroadcastReceiver.java | onReceive", "|" + "================="+"|");

        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, intent.setComponent(comp));
        setResultCode(Activity.RESULT_OK);
    }
}


