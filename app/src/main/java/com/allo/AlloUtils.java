package com.allo;

import android.os.Build;
import android.util.Base64;
import android.util.Log;

/**
 * Created by baek_uncheon on 2015. 7. 20..
 */
public class AlloUtils {

    final String TAG = getClass().getSimpleName();

    private static AlloUtils uniqueInstance;

    public static AlloUtils getInstance() {
        if (uniqueInstance == null)
            uniqueInstance = new AlloUtils();

        return uniqueInstance;
    }

    public String getSecToTime(int i_second) {
        int i_min = i_second / 60;
        int i_sec = i_second % 60;

        String st_min = String.valueOf(i_min);
        String st_sec = String.valueOf(i_sec);

        if (i_min / 10 == 0)
            st_min = "0" + st_min;

        if (i_sec / 10 == 0)
            st_sec = "0" + st_sec;


        String st_time = st_min + ":" + st_sec;

        return st_time;
    }

    public String millisecondToTimeString(int i_milsec) {
        int i_second = i_milsec / 1000;

        int i_min = i_second / 60;
        int i_sec = i_second % 60;

        String st_min = String.valueOf(i_min);
        String st_sec = String.valueOf(i_sec);

        if (i_min / 10 == 0)
            st_min = "0" + st_min;

        if (i_sec / 10 == 0)
            st_sec = "0" + st_sec;


        String st_time = st_min + ":" + st_sec;

        return st_time;
    }

    public byte[] getBase64Decoding(byte[] bytes) {
        return Base64.decode(bytes, 0);
    }

    public byte[] getBase64Encoding(byte[] bytes) {
        return Base64.encode(bytes, 0);
    }



    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public String getDeviceType(){
        String st_device_name = getDeviceName();
        String st_arr[] = st_device_name.split(" ");

        Log.i("device name", st_arr[1]);
        String st_model = "";

        int length = st_arr[1].length();
        String st_skl = st_arr[1].substring(length - 1, length);
        if (st_skl.equals("S") || st_skl.equals("K") || st_skl.equals("L"))
            st_model = st_arr[1].substring(0, length - 1);
        else
            st_model = st_arr[1];


        st_model = st_model.replaceAll("\\s", "");

        Log.i("st_skl", st_skl);
        Log.i("st_model", st_model);




        String st_type = "";
        switch (st_model){
//            type 1 : no mute
            case "SHW-M440":
                st_type = "mute_false";
                break;
            case "SHV-E210":
                st_type = "mute_false";
                break;
            case "SM-G920":
                st_type = "mute_false";
                break;
            case "SM-G925":
                st_type = "mute_false";
                break;
            case "SHV-E250":
                st_type = "mute_false";
                break;
            case "SM-N910":
                st_type = "mute_false";
                break;
            case "SM-N915":
                st_type = "mute_false";
                break;
            case "SM-N916":
                st_type = "mute_false";
                break;
            case "SM-N920":
                st_type = "mute_false";
                break;
//          type 2 : impossible
            case "SHV-E270":
                st_type = "impossible";
                break;
            case "SM-N900":
                st_type = "impossible";
                break;
            case "LG-F510":
                st_type = "impossible";
                break;
            case "LG-F540":
                st_type = "impossible";
                break;
            case "LG-F400":
                st_type = "impossible";
                break;
            case "LG-F490":
                st_type = "impossible";
                break;
            case "LG-F410":
                st_type = "impossible";
                break;
            case "LG-F460":
                st_type = "impossible";
                break;
            case "LG-F470":
                st_type = "impossible";
                break;
            case "LG-D821":
                st_type = "impossible";
                break;
            case "SHW-M420":
                st_type = "impossible";
                break;
            case "SHW-M200":
                st_type = "impossible";
                break;
            default:
                st_type = "mute_true";
        }

        return st_type;
    }

}
