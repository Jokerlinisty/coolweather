package com.coolweather.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库建表相关
 * Created by linwei on 2016-10-09.
 */
public class CoolWeatherOpenHelper extends SQLiteOpenHelper{
    /**
     *  Province表建表语句
     */
    public static final String CREATE_PROVINCE = "create table province (id integer primary key autoincrement, province_name text)";

    /**
     *  City表建表语句
     */
    public static final String CREATE_CITY = "create table city (id integer primary key autoincrement, city_name text, province_id integer)";

    /**
     *  County表建表语句
     */
    public static final String CREATE_COUNTY = "create table county (id integer primary key autoincrement, county_name text, county_code text, city_id integer)";

    public CoolWeatherOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PROVINCE);    // 创建Province表
        db.execSQL(CREATE_CITY);        // 创建City表
        db.execSQL(CREATE_COUNTY);      // 创建County表
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
