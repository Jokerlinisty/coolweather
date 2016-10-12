package com.coolweather.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.coolweather.app.activity.AutoUpdateService;

/**
 * 自动更新天气Service广播接收器
 * Created by linwei on 2016-10-12.
 */
public class AutoUpdateReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        // 接收到更新广播时，再次启动AutoUpdateService
        Intent i = new Intent(context, AutoUpdateService.class);
        context.startService(i);
    }
}
