package com.allo;

import android.content.Context;
import android.os.*;
import android.os.Process;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by uncheon on 2015. 8. 13..
 */
public abstract class AlloCacheLocalAsycTask extends AsyncTask<String, String, String> {
    String TAG = getClass().getSimpleName();
    Context context;
    String url;
    String cache_path;
    long pre_t;

    final static String SAVE_PATH_ALLO = Environment.getExternalStorageDirectory().getAbsolutePath() + "/allo";
    final static String SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/allo/cache";

    public AlloCacheLocalAsycTask(Context context, String st_url) {
        this.context = context;
        this.url = st_url;
    }

    public abstract void onFinish(String st_cache_path, long l_time);

    public abstract void onFailed();

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
        try {
            pre_t = System.currentTimeMillis();
            setCachePathFromUrl(url);
        } catch (Exception e) {
            Log.i(TAG, "doInBackground Exception");
            publishProgress("FAIL");
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(String... params) {
//        Log.i(TAG, "onProgressUpdate");
        if (params[0].equals("FINISH")) {
            long l_time = System.currentTimeMillis() - pre_t;
            Log.i(TAG, "onProgressUpdate : FINISH "+l_time);
            onFinish(cache_path, l_time);
        } else if (params[0].equals("FAIL")) {
            Log.i(TAG, "onProgressUpdate : FAIL");
            onFailed();
        }
    }

    @Override
    protected void onPostExecute(String params) {
        Log.i(TAG, "onPostExecute");
    }


    public void setCachePathFromUrl(String st_url) throws Exception {
        String st_cache_path = null;
//        file check
        String st_file_name = st_url.replace(context.getText(R.string.url_base_media), "");

        if (isFileExist(st_file_name)) {
            getCachePath(st_file_name);
        } else {
            Log.i(TAG, "setCachePathFromUrl not exist file");
            publishProgress("FAIL");
        }
    }


    private boolean isFileExist(String st_file_name) {
        File allo_dir = new File(SAVE_PATH_ALLO);
        if (!allo_dir.exists()) {
            allo_dir.mkdir();
        }


        File dir = new File(SAVE_PATH);
        // 폴더가 존재하지 않을 경우 폴더를 만듦
        if (!dir.exists()) {
            dir.mkdir();
        }
        // 다운로드 폴더에 동일한 파일명이 존재하는지 확인해서
        // 없으면 다운받고 있으면 해당 파일 실행시킴.
        if (new File(SAVE_PATH + "/" + st_file_name + "allo").exists() == false) {
            Log.i("cache", "file not exist");
            return false;
        } else {
            Log.i("cache", "file exist");
            return true;
        }
    }

    private void getCachePath(String st_file_name) throws Exception {
        Log.i("cache", "make cache file");
        String[] st_components = (st_file_name + "allo").split("\\.");
        String st_extension = st_components[1];
        int length = st_extension.length();
        st_extension = st_extension.substring(0, length - 4);
        Log.i("extension", st_extension);


        String st_cache_file_path = context.getCacheDir() + "/temp." + st_extension;

        FileInputStream fis = new FileInputStream(SAVE_PATH + "/" + st_file_name + "allo");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        int readcount = 0;
        byte[] buffer = new byte[1024];
        while ((readcount = fis.read(buffer, 0, 1024)) != -1) {
            bos.write(buffer, 0, readcount);
        }

        AlloCrypto alloCrypto = new AlloCrypto();
        byte[] decoded_data = alloCrypto.decryptBytes("allo", bos.toByteArray());
        File cache_file = new File(st_cache_file_path);

        FileOutputStream fos = new FileOutputStream(cache_file);
        fos.write(decoded_data);
        fos.close();

        cache_path = st_cache_file_path;
        publishProgress("FINISH");

    }
}
