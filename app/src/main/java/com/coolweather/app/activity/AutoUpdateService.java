package com.coolweather.app.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.coolweather.app.util.AddressUtil;
import com.coolweather.app.util.HttpCallBackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

/**
 * 后台自动更新天气
 * Created by linwei on 2016-10-12.
 */
public class AutoUpdateService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();

        Log.d("AutoUpdateService", "AutoUpdateService-->onStartCommand executed...");
        // 定时器（定时执行AutoUpdateService, 获取已选择县/区的最新天气信息）
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000; // 8小时
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i,0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气信息
     */
    private void updateWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String weatherCode = prefs.getString("weather_code", "");
        String address = AddressUtil.getAddressByType(weatherCode, AddressUtil.TYPE_WEATHER);
        if (TextUtils.isEmpty(address)){
            return;
        }
        HttpUtil.sendHttpRequest(address, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                Utility.handleWeatherResponse(AutoUpdateService.this, weatherCode,  response);
            }

            @Override
            public void onError(Exception e) {
                Log.e("AutoUpdateService", "failed to auto update weather info, caused by: " + e.toString(), e);
                e.printStackTrace();
            }
        });
    }
}
