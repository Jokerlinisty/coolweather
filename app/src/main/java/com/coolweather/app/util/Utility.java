package com.coolweather.app.util;

import android.text.TextUtils;
import android.util.Log;

import com.coolweather.app.db.CoolWeatherDb;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * 数据解析工具类
 * Created by linwei on 2016-10-09.
 */
public class Utility {

    /**
     *  解析和处理服务器返回的省份数据(格式：{"10101":"北京","10102":"上海","10103":"天津","10104":"重庆"})
     *  API: http://www.weather.com.cn/data/city3jdata/china.html
     */
    public synchronized static boolean handleProvincesResponse(CoolWeatherDb coolWeatherDb, String response){
        if (TextUtils.isEmpty(response)){
            return false;
        }
        try {
            JSONObject jsonObject = new JSONObject(response);
            Iterator iter = jsonObject.keys();
            Province province = null;
            while (iter.hasNext()) {
                String provinceCode = (String) iter.next();
                String provinceName = jsonObject.getString(provinceCode);
                province = new Province();
                province.setProvinceName(provinceName);
                province.setProvinceCode(provinceCode);
                // 保存到数据库中
                coolWeatherDb.saveProvince(province);
            }
            return true;
        } catch (JSONException e) {
            Log.e("Utility", "failed to convert "+ response +" to JSONObject! caused by: " + e.toString(), e);
            e.printStackTrace();
        }
        return false;
    }

    /**
     *  解析和处理服务器返回的城市数据(格式：{"01":"南宁","02":"崇左","03":"柳州","04":"来宾","05":"桂林"})
     *  API: http://www.weather.com.cn/data/city3jdata/provshi/10130.html
     */
    public synchronized static boolean handleCitiesResponse(CoolWeatherDb coolWeatherDb, String response, String provinceCode,  int provinceId){
        if (TextUtils.isEmpty(response)){
            return false;
        }
        try {
            JSONObject jsonObject = new JSONObject(response);
            Iterator iter = jsonObject.keys();
            City city = null;
            while (iter.hasNext()) {
                String key = (String) iter.next();
                String cityCode = provinceCode + key; // 拼接provinceCode
                String cityName = jsonObject.getString(key);
                city = new City();
                city.setCityName(cityName);
                city.setCityCode(cityCode);
                city.setProvinceId(provinceId);
                // 保存到数据库中
                coolWeatherDb.saveCity(city);
            }
            return true;
        } catch (JSONException e) {
            Log.e("Utility", "failed to convert "+ response +" to JSONObject! caused by: " + e.toString(), e);
            e.printStackTrace();
        }
        return false;
    }

    /**
     *  解析和处理服务器返回的县份数据(格式：{"01":"南宁","03":"邕宁","04":"横县","05":"隆安","06":"马山","07":"上林","08":"武鸣","09":"宾阳"})
     *  API: http://www.weather.com.cn/data/city3jdata/station/1013001.html
     */
    public synchronized static boolean handleCountiesResponse(CoolWeatherDb coolWeatherDb, String response, String cityCode, int cityId){
        if (TextUtils.isEmpty(response)){
            return false;
        }
        try {
            JSONObject jsonObject = new JSONObject(response);
            Iterator iter = jsonObject.keys();
            County county = null;
            while (iter.hasNext()) {
                String key = (String) iter.next();
                String countyCode = cityCode + key; // 拼接cityCode
                String countyName = jsonObject.getString(key);
                county = new County();
                county.setCountyName(countyName);
                county.setCountyCode(countyCode);
                county.setCityId(cityId);
                // 保存到数据库中
                coolWeatherDb.saveCounty(county);
            }
            return true;
        } catch (JSONException e) {
            Log.e("Utility", "failed to convert "+ response +" to JSONObject! caused by: " + e.toString(), e);
            e.printStackTrace();
        }
        return false;
    }
}
