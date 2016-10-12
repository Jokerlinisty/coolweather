package com.coolweather.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;

import com.coolweather.app.R;
import com.coolweather.app.db.CoolWeatherDb;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.vo.WeatherInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.prefs.PreferencesFactory;

/**
 * 数据解析工具类
 * Created by linwei on 2016-10-09.
 */
public class Utility {
    /**
     * 读取res/raw下的（城市代码）文件信息并返回
     * @param context
     * @param resourceId 文件资源id（如：R.raw.id）
     * @param encode 编码
     */
    public static String readCityCodeFromRaw(Context context, int resourceId, String encode) {
        InputStream fis = null;
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            fis = context.getResources().openRawResource(resourceId);
            reader = new BufferedReader(new InputStreamReader(fis, encode));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            Log.e("Utility", "failed to read file from raw, resourceId= "+ resourceId +", caused by: " + e.toString(), e);
            e.printStackTrace();
        } finally {
            if (fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    /**
     * 保存天气城市代码到数据库中
     * 格式：[{"cityList":[{"areaList":[{"id":"101010100","name":"北京"},{"id":"101011400","name":"门头沟"}],"name":"北京"}],"name":"北京"}]
     */
    public static void saveCityCodeToDB(CoolWeatherDb coolWeatherDb, String cityCodeJson){
        if (TextUtils.isEmpty(cityCodeJson)){
            return;
        }
        SQLiteDatabase db = null;
        try {
            db = coolWeatherDb.getDb();
            // 开启事务
            db.beginTransaction();
            // 先清空省市区表数据
            coolWeatherDb.cleanTableData(new String[]{CoolWeatherDb.T_COUNTY, CoolWeatherDb.T_CITY, CoolWeatherDb.T_PROVINCE});
            // 解析城市代码Json
            JSONArray jsonArray = new JSONArray(cityCodeJson);
            for (int i=0; i< jsonArray.length(); i++){
                // 第一级：省份
                JSONObject province = jsonArray.optJSONObject(i);
                String provinceName = province.getString("name");
                long provinceRowId = coolWeatherDb.saveProvince(new Province(provinceName)); // 保存省份

                // 第二级：城市
                JSONArray cityList = province.optJSONArray("cityList");
                if(cityList != null){
                    for (int j=0; j< cityList.length(); j++){
                        JSONObject city = cityList.optJSONObject(j);
                        String cityName = city.getString("name");
                        long cityRowId = coolWeatherDb.saveCity(new City(cityName, (int) provinceRowId)); // 保存城市

                        // 第三级：县/区
                        JSONArray countyList = city.optJSONArray("areaList");
                        if (countyList != null){
                            for (int k=0; k< countyList.length(); k++){
                                JSONObject county = countyList.optJSONObject(k);
                                String countyName = county.getString("name");
                                String countyCode = county.getString("id");
                                coolWeatherDb.saveCounty(new County(countyName, countyCode, (int) cityRowId)); // 保存县区
                            }
                        }
                    }
                }
            }
            // 事务成功
            db.setTransactionSuccessful();
        } catch (JSONException e) {
            Log.e("Utility", "failed to save cityCode to DB, caused by: " + e.toString(), e);
            e.printStackTrace();
        }finally {
            // 结束事务
            db.endTransaction();
        }
    }

    /**
     * 解析服务器返回的天气信息JSON数据，并保存到本地
     * 数据格式：{"weatherinfo":{"city":"北京","cityid":"101010100","temp1":"-2℃","temp2":"16℃","weather":"晴","img1":"n0.gif","img2":"d0.gif","ptime":"18:00"}}
     */
    public static void handleWeatherResponse(Context context, String response){
        if (TextUtils.isEmpty(response)){
            return;
        }
        try {
            // 解析Json数据
            JSONObject responseJson = new JSONObject(response);
            JSONObject weatherInfoJson = responseJson.optJSONObject("weatherinfo");
            WeatherInfo weatherInfo = new WeatherInfo();
            weatherInfo.setCityName(weatherInfoJson.getString("city"));
            weatherInfo.setWeatherCode(weatherInfoJson.getString("cityid"));
            weatherInfo.setLowTemp(weatherInfoJson.getString("temp1"));
            weatherInfo.setHighTemp(weatherInfoJson.getString("temp2"));
            weatherInfo.setWeather(weatherInfoJson.getString("weather"));
            weatherInfo.setPublishTime(weatherInfoJson.getString("ptime"));
            weatherInfo.setCitySelected(true);

            // 保存天气信息
            saveWeatherInfo(context, weatherInfo);
        } catch (JSONException e) {
            Log.e("Utility", "failed to convert "+ response +" to JSONObject! caused by: " + e.toString(), e);
            e.printStackTrace();
        }
    }

    /**
     * 将服务器返回的所有天气信息存储到SharedPreferences文件中。
     */
    public static void saveWeatherInfo(Context context, WeatherInfo weatherInfo){
        if (weatherInfo == null){
            return;
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("city_name", weatherInfo.getCityName());
        editor.putString("weather_code", weatherInfo.getWeatherCode());
        editor.putString("low_temp", weatherInfo.getLowTemp());
        editor.putString("high_temp", weatherInfo.getHighTemp());
        editor.putString("weather", weatherInfo.getWeather());
        editor.putString("publish_time", weatherInfo.getPublishTime());
        editor.putString("current_date", new SimpleDateFormat("yyyy年M月d日", Locale.CHINA).format(new Date()));
        editor.putBoolean("city_selected", weatherInfo.isCitySelected());
        editor.commit();
    }
}
