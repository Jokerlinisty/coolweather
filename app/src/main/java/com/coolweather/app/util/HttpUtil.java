package com.coolweather.app.util;

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
                    conn.setRequestProperty("contentType", "UTF-8");
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(8000);

                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null){
                        response.append(line);
                    }

                    // 回调onFinish方法
                    if (listener != null){
                        listener.onFinish(response.toString());
                    }
                }catch (Exception e){
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
