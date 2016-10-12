package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
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

public class WeatherActivity extends Activity implements View.OnClickListener{

    private LinearLayout weatherInfoLayout; // 天气信息
    private TextView cityNameText;  // 城市名称
    private TextView lowTempText;   // 最低温度
    private TextView highTempText;  // 最高温度
    private TextView weatherText; // 天气描述
    private TextView publishText;     // 发布说明
    private TextView currentDateText; // 当前日期

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
        lowTempText = (TextView) findViewById(R.id.low_temp);
        highTempText = (TextView) findViewById(R.id.high_temp);
        weatherText = (TextView) findViewById(R.id.weather);
        publishText = (TextView) findViewById(R.id.publish_text);
        currentDateText = (TextView) findViewById(R.id.current_date);

        // 切换城市及刷新天气按钮
        switchCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);

        // 县区代码
        countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)){
            // 有县级代码，就查询相应天气
            publishText.setText("同步中，请稍后...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.VISIBLE);
            // 根据县/区代码，查询天气信息
            String address = AddressUtil.getAddressByType(countyCode, AddressUtil.TYPE_WEATHER);
            queryWeatherFromServer(address, AddressUtil.TYPE_WEATHER);
        }else{
            // 直接显示当前县/区天气
            showWeather();
        }
    }

    /**
     * 根据县/区代码，从服务器查询相应天气信息
     */
    private void queryWeatherFromServer(final String address, final String type) {
        if (TextUtils.isEmpty(address)){
            return;
        }
        Log.d("WeatherActivity", "try to search weather info from " + address);
        HttpUtil.sendHttpRequest(address, new HttpCallBackListener(){
            @Override
            public void onFinish(String response) {
                switch (type){
                    case AddressUtil.TYPE_WEATHER:
                        // 解析并保存天气信息到本地
                        Utility.handleWeatherResponse(WeatherActivity.this, response);
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
        cityNameText.setText(prefs.getString("city_name", ""));
        lowTempText.setText(prefs.getString("low_temp", ""));
        highTempText.setText(prefs.getString("high_temp", ""));
        weatherText.setText(prefs.getString("weather", ""));
        publishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
        currentDateText.setText(prefs.getString("current_date", ""));
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
                String weatherCode;
                if (!TextUtils.isEmpty(countyCode)){
                    weatherCode = countyCode;
                }else{
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                    weatherCode = pref.getString("weather_code", "");
                }
                // 根据县/区代码，查询天气信息
                String address = AddressUtil.getAddressByType(countyCode, AddressUtil.TYPE_WEATHER);
                queryWeatherFromServer(address, AddressUtil.TYPE_WEATHER);
                break;
            default:
                break;
        }
    }
}
