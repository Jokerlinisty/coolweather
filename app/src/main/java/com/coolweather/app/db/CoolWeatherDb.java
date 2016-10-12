package com.coolweather.app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库操作工具类
 * Created by linwei on 2016-10-09.
 */
public class CoolWeatherDb {

    public static final String DB_NAME = "cool_weather"; // 数据库名称
    public static final int VERSION = 1; // 数据库版本
    public static final String T_PROVINCE = "province"; // 省份表
    public static final String T_CITY = "city"; // 城市表
    public static final String T_COUNTY = "county"; // 县份表
    private static CoolWeatherDb coolWeatherDb;
    private SQLiteDatabase db;

    /**
     * 构造方法私有化
     */
    private CoolWeatherDb(Context context){
        CoolWeatherOpenHelper openHelper = new CoolWeatherOpenHelper(context, DB_NAME, null, VERSION);
        db = openHelper.getWritableDatabase();
    }

    /**
    * 获取CoolWeatherDb实例
    */
    public synchronized static CoolWeatherDb getInstance(Context context){
        if (coolWeatherDb == null){
            coolWeatherDb = new CoolWeatherDb(context);
        }
        return coolWeatherDb;
    }

    /**
     * 获取db实例
     */
    public SQLiteDatabase getDb() {
        return db;
    }

    /**
     * 清空指定表数据
     */
    public void cleanTableData(String[] tableNames){
        for (String tableName : tableNames){
            db.delete(tableName, null, null);
        }
    }

    /**
     * 查询指定表的记录数
     */
    public int getTableCount(String tableName){
        Cursor cursor = db.rawQuery("select count(*) from " + tableName, null);
        cursor.moveToFirst();
        return (int) cursor.getLong(0);
    }


    /**
    *  新增省份
    */
    public long saveProvince(Province province){
        if (province != null){
            ContentValues contentValues = new ContentValues();
            contentValues.put("province_name", province.getProvinceName());
            return db.insert(T_PROVINCE, null, contentValues);
        }
        return -1;
    }

    /**
     * 查询所有省份
     */
    public List<Province> loadProvinces() {
        List<Province> list = new ArrayList<>();
        Cursor cursor = db.query(T_PROVINCE, null, null, null, null, null, null);
        Province province = null;
        if (cursor.moveToFirst()){
            do {
                province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                list.add(province);
            }while (cursor.moveToNext());
        }
        return list;
    }

    /**
     *  新增城市
     */
    public long saveCity(City city){
        if (city != null){
            ContentValues contentValues = new ContentValues();
            contentValues.put("city_name", city.getCityName());
            contentValues.put("province_id", city.getProvinceId());
            return db.insert(T_CITY, null, contentValues);
        }
        return -1;
    }

    /**
     * 查询某个省份所有城市
     */
    public List<City> loadCities(int provinceId) {
        List<City> list = new ArrayList<>();
        Cursor cursor = db.query(T_CITY, null, "province_id = ?", new String[]{String.valueOf(provinceId)}, null, null, null);
        City city = null;
        if (cursor.moveToFirst()){
            do {
                city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setProvinceId(provinceId);
                list.add(city);
            }while (cursor.moveToNext());
        }
        return list;
    }

    /**
     *  新增县份
     */
    public long saveCounty(County county){
        if (county != null){
            ContentValues contentValues = new ContentValues();
            contentValues.put("county_name", county.getCountyName());
            contentValues.put("county_code", county.getCountyCode());
            contentValues.put("city_id", county.getCityId());
            return db.insert(T_COUNTY, null, contentValues);
        }
        return -1;
    }

    /**
     * 查询某个城市所有县份
     */
    public List<County> loadCounties(int cityId) {
        List<County> list = new ArrayList<>();
        Cursor cursor = db.query(T_COUNTY, null, "city_id = ?", new String[]{String.valueOf(cityId)}, null, null, null);
        County county = null;
        if (cursor.moveToFirst()){
            do {
                county = new County();
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCityId(cityId);
                list.add(county);
            }while (cursor.moveToNext());
        }
        return list;
    }
}
