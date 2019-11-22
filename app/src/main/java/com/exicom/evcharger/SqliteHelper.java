package com.exicom.evcharger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SqliteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "DB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Exicom";
    private static final String KEY_ID = "id";
    private static final String KEY_CHARGER_NAME = "client_sw_no";
    private static final String KEY_CHARGER_NO = "client_dev_no";
    private static final String KEY_CREATED_BY = "created_by";
    private static final String KEY_USER_ID = "user";
    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_FLAG = "is_authenticated";
    public SqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE "+ TABLE_NAME + "(" +KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_CHARGER_NAME+" TEXT not null,"
                + KEY_NICKNAME+" TEXT not null,"
                + KEY_CHARGER_NO+" TEXT not null,"
                + KEY_CREATED_BY+" TEXT not null,"
                + KEY_USER_ID+" TEXT not null,"
                + KEY_PASSWORD+" TEXT not null,"
                + KEY_FLAG +" TEXT not null" +")";
        db.execSQL(sql);
        Log.i("query",sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long addDevice(String client_sw_no, String client_dev_no, String nickname, String created_by, String client_certificate){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_CHARGER_NAME,client_sw_no);
        contentValues.put(KEY_CHARGER_NO, client_dev_no);
        contentValues.put(KEY_NICKNAME, nickname);
        contentValues.put(KEY_CREATED_BY, created_by);
        contentValues.put(KEY_USER_ID, created_by);
        contentValues.put(KEY_PASSWORD, client_certificate);
        contentValues.put(KEY_FLAG,"0");
        long id = db.insert(TABLE_NAME,null,contentValues);
        db.close();
        return  id;
    }

    public boolean checkDevice(String device_name){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ TABLE_NAME +" WHERE "+KEY_CHARGER_NO+" = '"+device_name+"';", null);
        while(cursor.moveToNext()){
            db.close();
            return true;
        }
        db.close();
        return false;
    }


    public List<DataHandler> getData(){
        List<DataHandler> data = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ TABLE_NAME+" ;",null);
        DataHandler dataHandler = null;
        while(cursor.moveToNext()){
            dataHandler = new DataHandler();
            String id = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID));
            String charger_name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHARGER_NAME));
            String nick_name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NICKNAME));
            String flag = cursor.getString(cursor.getColumnIndexOrThrow(KEY_FLAG));
            String password = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PASSWORD));
            dataHandler.setId(id);
            dataHandler.setCharger_serial_no(charger_name);
            dataHandler.setMac_add(nick_name);
            dataHandler.setFlag(flag);
            dataHandler.setClient_certificate(password);
            data.add(dataHandler);
        }
        db.close();
        return data;
    }

    public boolean deleteItem(String charger_no){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+ TABLE_NAME + " WHERE "+ KEY_CHARGER_NO+" = '"+charger_no+"'");
        db.close();
        return true;
    }

    public void clearData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+ TABLE_NAME);
        db.close();
    }
}
