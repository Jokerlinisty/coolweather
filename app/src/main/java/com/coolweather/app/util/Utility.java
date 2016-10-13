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
import com.google.gson.Gson;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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
     * 解析服务器返回的天气信息JSON数据（最近7天），并保存到本地
     * 数据格式：{"desc":"OK","status":1000,"data":{"wendu":"23","ganmao":"各项气象条件适宜，无明显降温过程，发生感冒机率较低。",
     * "forecast":[{"fengxiang":"无持续风向","fengli":"微风级","high":"高温 29℃","type":"多云","low":"低温 22℃","date":"13日星期四"}],
     * "yesterday":{"fl":"3-4级","fx":"北风","high":"高温 25℃","type":"小到中雨","low":"低温 20℃","date":"12日星期三"},"aqi":"27","city":"广州"}}
     */
    public static void handleWeatherResponse(Context context, String weatherCode, String response){
        if (TextUtils.isEmpty(response)){
            return;
        }
        try {
            // 解析Json数据
            JSONObject responseJson = new JSONObject(response);
            int status = responseJson.getInt("status");
            if(status != 1000){
                return;
            }
            // 保存最近7天天气信息
            List<WeatherInfo> weatherList = new ArrayList<>();
            JSONObject data = responseJson.optJSONObject("data");
            String cityName = data.getString("city");  // 城市名称
            String currTemp = data.getString("wendu") + "℃";  // 实时温度
            String health = data.getString("ganmao");// 健康提示

            JSONObject yesterday = data.optJSONObject("yesterday"); // 昨天天气
            WeatherInfo yesterdayWeather = new WeatherInfo();
            yesterdayWeather.setLowTemp(yesterday.getString("low"));
            yesterdayWeather.setHighTemp(yesterday.getString("high"));
            yesterdayWeather.setWeather(yesterday.getString("type")); // 天气状况（晴、多云、小雨等）
            yesterdayWeather.setDate(yesterday.getString("date"));
            yesterdayWeather.setCitySelected(true);
            weatherList.add(yesterdayWeather);

            // 注意：以下列表第一条数据表示当天天气
            JSONArray forecast = data.optJSONArray("forecast");
            if (forecast != null){
                WeatherInfo weatherInfo = null;
                for (int i=0; i< forecast.length(); i++){
                    JSONObject weatherObj = forecast.optJSONObject(i);
                    if (weatherObj == null){
                        continue;
                    }
                    weatherInfo = new WeatherInfo();
                    weatherInfo.setCityName(cityName);
                    weatherInfo.setWeatherCode(weatherCode);
                    if (i == 0){ // 当天，则设置实时温度及健康提示
                        weatherInfo.setCurrTemp(currTemp);
                        weatherInfo.setHealth(health);
                    }
                    weatherInfo.setLowTemp(weatherObj.getString("low"));
                    weatherInfo.setHighTemp(weatherObj.getString("high"));
                    weatherInfo.setWeather(weatherObj.getString("type"));
                    weatherInfo.setDate(weatherObj.getString("date"));
                    weatherList.add(weatherInfo);
                }
            }
            Log.i("Utility", "success to get weatherInfo from HttpResponse, response=" + response);
            // 保存天气信息
            saveWeatherInfo(context, weatherList);
        } catch (JSONException e) {
            Log.e("Utility", "failed to convert "+ response +" to JSONObject! caused by: " + e.toString(), e);
            e.printStackTrace();
        }
    }

    /**
     * 将服务器返回的所有天气信息存储到SharedPreferences文件中。
     */
    public static void saveWeatherInfo(Context context, List<WeatherInfo> weatherList){
        if (weatherList == null && weatherList.size() < 1){
            return;
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        // 当天天气(单独保存一份)
        WeatherInfo todayWeather = weatherList.get(1); // 取第二条数据
        editor.putString("city_name", todayWeather.getCityName());
        editor.putString("weather_code", todayWeather.getWeatherCode());
        editor.putString("curr_temp", todayWeather.getCurrTemp());
        editor.putString("low_temp", todayWeather.getLowTemp());
        editor.putString("high_temp", todayWeather.getHighTemp());
        editor.putString("weather", todayWeather.getWeather());
        editor.putString("health", todayWeather.getHealth());
        editor.putString("current_date", new SimpleDateFormat("yyyy年M月d日 EEEE", Locale.CHINA).format(new Date()));
        editor.putBoolean("city_selected", true); // 已保存标志位（必须，用于下次进入时，直接加载上次选中的城市天气）
        editor.putString("update_time", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));

        // 最近N天天气信息
        Gson gson = new Gson();
        String weatherInfoJson = gson.toJson(weatherList); // 将List转为Json
        editor.putString("weatherInfo", weatherInfoJson);
        editor.commit();
    }
}
