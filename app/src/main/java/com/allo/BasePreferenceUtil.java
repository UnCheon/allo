package com.allo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by baek_uncheon on 2015. 3. 3..
 */
public class BasePreferenceUtil
{
    private SharedPreferences _sharedPreferences;

    protected BasePreferenceUtil(Context $context)
    {
        super();
        _sharedPreferences = PreferenceManager.getDefaultSharedPreferences($context);
    }

    /**
     * key 수동 설정
     *
     * @param key
     *           키 값
     * @param value
     *           내용
     */
    protected void put(String $key, String $value)
    {
        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putString($key, $value);
        editor.commit();
    }

    /**
     * String 값 가져오기
     *
     * @param key
     *           키 값
     * @return String (기본값 null)
     */
    protected String get(String $key)
    {
        return _sharedPreferences.getString($key, null);
    }

    /**
     * key 설정
     *
     * @param key
     *           키 값
     * @param value
     *           내용
     */
    protected void put(String $key, boolean $value)
    {
        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putBoolean($key, $value);
        editor.commit();
    }

    /**
     * Boolean 값 가져오기
     *
     * @param key
     *           키 값
     * @param defValue
     *           기본값
     * @return Boolean
     */
    protected boolean get(String $key, boolean $default)
    {
        return _sharedPreferences.getBoolean($key, $default);
    }

    /**
     * key 설정
     *
     * @param key
     *           키 값
     * @param value
     *           내용
     */
    protected void put(String $key, int $value)
    {
        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putInt($key, $value);
        editor.commit();
    }

    /**
     * int 값 가져오기
     *
     * @param key
     *           키 값
     * @param defValue
     *           기본값
     * @return int
     */
    protected int get(String $key, int $default)
    {
        return _sharedPreferences.getInt($key, $default);
    }
}