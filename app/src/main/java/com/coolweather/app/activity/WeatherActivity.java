package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolweather.app.R;
import com.coolweather.app.util.AddressUtil;
import com.coolweather.app.util.HttpCallBackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;
import com.coolweather.app.vo.WeatherInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class WeatherActivity extends Activity implements View.OnClickListener{

    private LinearLayout weatherInfoLayout; // 天气信息
    private TextView cityNameText;  // 城市名称
    private TextView currTempText;  // 实时温度
    private TextView lowTempText;   // 最低温度
    private TextView highTempText;  // 最高温度
    private TextView weatherText;   // 天气描述
    private TextView healthText;    // 健康提示
    private TextView publishText;     // 发布说明
    private TextView currentDateText; // 当前日期

    private TextView dayText1, dayText2, dayText3, dayText4, dayText5, dayText6; // 日期Text（其中dayText1=昨天，dayText2=今天。依次...）
    private TextView weather1, weather2, weather3, weather4, weather5, weather6; // 天气Text（weather1=昨天，weather2=今天。依次...）
    private TextView tempText1, tempText2, tempText3, tempText4, tempText5, tempText6; // 温度Text（tempText1=昨天，tempText2=今天。依次...）

    private Button switchCity; // 却换城市按钮
    private Button refreshWeather; // 更新天气按钮
    private String countyCode; // 当前县区代码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_weather);

        // 初始化各种控件
        weatherInfoLayout= (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        currTempText = (TextView) findViewById(R.id.curr_temp);
        lowTempText = (TextView) findViewById(R.id.low_temp);
        highTempText = (TextView) findViewById(R.id.high_temp);
        weatherText = (TextView) findViewById(R.id.weather);
        healthText = (TextView) findViewById(R.id.health);
        publishText = (TextView) findViewById(R.id.publish_text);
        currentDateText = (TextView) findViewById(R.id.current_date);

        weather1 = (TextView) findViewById(R.id.weather_1);
        weather2 = (TextView) findViewById(R.id.weather_2);
        weather3 = (TextView) findViewById(R.id.weather_3);
        weather4 = (TextView) findViewById(R.id.weather_4);
        weather5 = (TextView) findViewById(R.id.weather_5);
        weather6 = (TextView) findViewById(R.id.weather_6);

        dayText1 = (TextView) findViewById(R.id.day_1);
        dayText2 = (TextView) findViewById(R.id.day_2);
        dayText3 = (TextView) findViewById(R.id.day_3);
        dayText4 = (TextView) findViewById(R.id.day_4);
        dayText5 = (TextView) findViewById(R.id.day_5);
        dayText6 = (TextView) findViewById(R.id.day_6);

        tempText1 = (TextView) findViewById(R.id.temp_1);
        tempText2 = (TextView) findViewById(R.id.temp_2);
        tempText3 = (TextView) findViewById(R.id.temp_3);
        tempText4 = (TextView) findViewById(R.id.temp_4);
        tempText5 = (TextView) findViewById(R.id.temp_5);
        tempText6 = (TextView) findViewById(R.id.temp_6);

        // 切换城市及刷新天气按钮
        switchCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);

        // 县/区代码（只在选中县/区时，传过来）
        countyCode = getIntent().getStringExtra("county_code");
        // 如果是选中县区，则查询最新，否则加载已保存城市的天气信息
        if (!TextUtils.isEmpty(countyCode)){
            publishText.setText("同步中，请稍后...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.VISIBLE);
            queryWeatherFromServer(countyCode, AddressUtil.TYPE_WEATHER);
        }else{
            showWeather();
        }
    }

    /**
     * 根据县/区代码，从服务器查询相应天气信息
     */
    private void queryWeatherFromServer(final String countyCode, final String type) {
        // 根据县/区代码，查询天气信息
        String address = AddressUtil.getAddressByType(countyCode, type);
        if (TextUtils.isEmpty(address) || TextUtils.isEmpty(countyCode)){
            return;
        }
        Log.d("WeatherActivity", "try to get weather info from " + address);
        HttpUtil.sendHttpRequest(address, new HttpCallBackListener(){
            @Override
            public void onFinish(String response) {
                switch (type){
                    case AddressUtil.TYPE_WEATHER:
                        // 解析并保存天气信息到本地
                        Utility.handleWeatherResponse(WeatherActivity.this, countyCode, response);
                        // 返回主线程显示天气信息
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showWeather();
                            }
                        });
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onError(Exception e) {
                // 返回主线程更新UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败！");
                    }
                });
            }
        });
    }

    /**
     * 从SharedPreferences文件中读取本地天气信息并显示。
     */
    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // 当天天气
        cityNameText.setText(prefs.getString("city_name", ""));
        currTempText.setText(prefs.getString("curr_temp", ""));
        String lowTemp = prefs.getString("low_temp", ""); // 返回: “低温 10℃”，这里只需要后面3位温度
        if (lowTemp.length() > 3){
            lowTemp = lowTemp.substring(lowTemp.length()-3);
        }
        lowTempText.setText(lowTemp);
        String highTemp = prefs.getString("high_temp", ""); // 返回: “高温 19℃”，这里只需要后面3位温度
        if (highTemp.length() > 3){
            highTemp = highTemp.substring(highTemp.length()-3);
        }
        highTempText.setText(highTemp);
        weatherText.setText(prefs.getString("weather", ""));
        healthText.setText("温馨提示: " + prefs.getString("health", ""));
        currentDateText.setText(prefs.getString("current_date", ""));
        publishText.setText("已更新 " + prefs.getString("update_time", ""));

        // 最近N天天气
        String weatherInfoJson = prefs.getString("weatherInfo", "");
        if (!TextUtils.isEmpty(weatherInfoJson)){
            // 使用Gson重新解析出天气信息
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<WeatherInfo>>(){}.getType();
            List<WeatherInfo> weatherInfoList = gson.fromJson(weatherInfoJson, listType);
            if (weatherInfoList != null && weatherInfoList.size() > 0){
                WeatherInfo weatherInfo = null;
                for (int i =0; i< weatherInfoList.size(); i++){
                    weatherInfo = weatherInfoList.get(i);
                    String weather = weatherInfo.getWeather(); // 天气
                    String low = weatherInfo.getLowTemp(); // 最低温
                    if (!TextUtils.isEmpty(low)){
                        low = low.substring(low.length()-3);
                    }
                    String high = weatherInfo.getHighTemp(); // 最高温
                    if (!TextUtils.isEmpty(high)){
                        high = high.substring(high.length()-3);
                    }
                    String date = weatherInfo.getDate(); // 日期。返回: 12日星期三，这里需要加个换行
                    if (!TextUtils.isEmpty(date)){
                        date = date.substring(0, 3) +"\n" + date.substring(date.length()-3);
                    }
                    switch (i+1){
                        case 1:
                            dayText1.setText(date);
                            weather1.setText(weather);
                            tempText1.setText(high + "\n" + low);
                            break;
                        case 2:
                            dayText2.setText(date);
                            weather2.setText(weather);
                            tempText2.setText(high + "\n" + low);
                            break;
                        case 3:
                            dayText3.setText(date);
                            weather3.setText(weather);
                            tempText3.setText(high + "\n" + low);
                            break;
                        case 4:
                            dayText4.setText(date);
                            weather4.setText(weather);
                            tempText4.setText(high + "\n" + low);
                            break;
                        case 5:
                            dayText5.setText(date);
                            weather5.setText(weather);
                            tempText5.setText(high + "\n" + low);
                            break;
                        case 6:
                            dayText6.setText(date);
                            weather6.setText(weather);
                            tempText6.setText(high + "\n" + low);
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        // 设置相关布局可见性
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);

        // 成功显示天气后，激活AutoUpdateService服务，定时更新天气信息
        Intent i = new Intent(this, AutoUpdateService.class);
        startService(i);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.switch_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishText.setText("正在同步...");
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = pref.getString("weather_code", "");
                // 根据县/区代码，查询天气信息
                queryWeatherFromServer(weatherCode, AddressUtil.TYPE_WEATHER);
                break;
            default:
                break;
        }
    }
}
