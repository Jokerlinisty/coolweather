package com.coolweather.app.util;

import android.text.TextUtils;

/**
 * 访问地址信息工具类
 * Created by linwei on 2016-10-10.
 */
public class AddressUtil {
    public static final String TYPE_WEATHER = "weather"; // 天气类型（返回天气信息）
    public static final String DEFAULT_CITY_CODE = "101010100"; // 默认城市：北京

    /**
     * 根据类型，返回对应服务器地址
     */
    public static String getAddressByType(String code, String type){
        String address = "";
        switch (type){
            case TYPE_WEATHER:
                if (TextUtils.isEmpty(code)){
                    code = DEFAULT_CITY_CODE; // 不存在，则默认城市天气
                }
                // address = "http://www.weather.com.cn/data/cityinfo/" + code + ".html";
                address = "http://wthrcdn.etouch.cn/weather_mini?citykey=" + code.trim();
            break;
            default:
                break;
        }
        return address;
    }
}
