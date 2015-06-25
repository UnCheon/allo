package com.allo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by baek_uncheon on 2015. 1. 19..
 */

public class ContactDBOpenHelper {
    private static final String DATABASE_NAME = "Contacts.db";
    public static SQLiteDatabase mDB;
    private static final String TABLE_NAME = "sync_contact";
    private DatabaseHelper mDBHelper;
    private Context mContext;

    private class DatabaseHelper extends SQLiteOpenHelper {


        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE "+TABLE_NAME+" ( _id INTEGER PRIMARY KEY AUTOINCREMENT, phone_number TEXT UNIQUE, is_new BOOLEAN);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP IF EXISTS "+ TABLE_NAME);
            onCreate(db);
        }
    }

    public ContactDBOpenHelper(Context context){
        this.mContext = context;
    }

    public ContactDBOpenHelper open_writableDatabase() throws SQLException{
        mDBHelper = new DatabaseHelper(mContext);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public ContactDBOpenHelper open_readableDatabase() throws SQLException{
        mDBHelper = new DatabaseHelper(mContext);
        mDB = mDBHelper.getReadableDatabase();
        return this;
    }

    public void close(){
        mDB.close();
    }


    public Cursor getAllContacts(){
        return mDB.query(TABLE_NAME, null, null, null, null, null, null, null);
    }



    public Cursor getNewContacts(){
        Cursor cursor = mDB.rawQuery("select * from "+TABLE_NAME+" where is_new;", null);
        return cursor;
    }

    public void updateContacts(){
        ContentValues row;
        row = new ContentValues();
        row.put("is_new", false);
        mDB.update(TABLE_NAME, row, "is_new=1", null);

    }

    public void updateContact(String phone_number, Boolean is_new){
        ContentValues row;
        row = new ContentValues();
        row.put("is_new", is_new);
        mDB.update(TABLE_NAME, row, "phone_number='"+phone_number+"'", null);
    }


    public void setContact(String phone_number, Boolean is_new){
        ContentValues row;
        row = new ContentValues();
        row.put("phone_number", phone_number);
        row.put("is_new", is_new);
        mDB.insert(TABLE_NAME, null, row);
        Log.i("contact db", "insert");
    }





    public void setContacts (ArrayList<Contact> contact_list) {
        ContentValues row;

        String phone_number = null;
        boolean is_new = false;

        mDB.delete(TABLE_NAME, null, null);
        mDB.delete(TABLE_NAME, null, null);


        for (int i = 0 ; i < contact_list.size() ; i++){
            phone_number = contact_list.get(i).getPhonenum();
            is_new = contact_list.get(i).getIsNew();

            row = new ContentValues();
            row.put("phone_number", phone_number);
            row.put("is_new", is_new);

            mDB.insert(TABLE_NAME, null, row);
        }
    }
}
