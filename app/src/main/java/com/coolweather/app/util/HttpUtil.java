package com.coolweather.app.util;

import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 联网相关工具类
 * Created by linwei on 2016-10-09.
 */
public class HttpUtil {
    public static void sendHttpRequest(final String address, final HttpCallBackListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(address);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("contentType", "utf-8");
                    conn.setRequestProperty("Accept-Encoding", "identity");
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(8000);

                    // 处理返回数据
                    if (conn.getResponseCode() == 200){
                        InputStream fis = conn.getInputStream();
                        StringBuilder response = new StringBuilder();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "utf-8"));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        // 回调onFinish方法
                        if (listener != null){
                            listener.onFinish(response.toString());
                        }
                    }
                }catch (Exception e){
                    Log.e("HttpUtil", "HttpUtil-->sendHttpRequest() failed, caused by: " + e.toString(), e);
                    e.printStackTrace();
                    // 回调onFinish方法
                    if (listener != null){
                        listener.onError(e);
                    }
                }finally {
                    if(conn != null){
                        conn.disconnect();
                    }
                }
            }
        }).start();
    }
}
