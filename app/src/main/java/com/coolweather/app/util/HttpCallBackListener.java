package com.coolweather.app.util;

/**
 * HttpUtil回调接口
 * Created by linwei on 2016-10-09.
 */
public interface HttpCallBackListener {
    /**
     * 请求完成
     */
    void onFinish(String response);
    /**
     * 请求出错
     */
    void onError(Exception e);
}
