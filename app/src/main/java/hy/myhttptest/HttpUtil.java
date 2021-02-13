package hy.myhttptest;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {
    public static List<Cookie> mycookie = null;
    //                HttpUtil.sendHttpRequest("http://www.baidu.com", new HttpCallbackListener() {
////                    @Override
////                    public void onFinish(String response) {
////                        showResponse(response);
////                    }
////                    @Override
////                    public void onError(Exception e) {
////                        Log.d("错误信息", e.toString());
////                    }
////                });

    //    HttpUtil.sendOkHttpRequest("http://www.baidu.com", new Callback() {
//        @Override
//        public void onFailure(@NotNull Call call, @NotNull IOException e) {
//            Log.d("错误信息", e.toString());
//        }
//
//        @Override
//        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//            String responseData = response.body().string();
//            showResponse(responseData);
//        }
//    });
    public static void sendHttpRequest(final String address, final HttpCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    InputStream in = connection.getInputStream();//输入流，简单理解为水流
                    //下面对获取到的输入流进行读取
                    //new InputStreamReader(in)理解为给水流匹配一根水管
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    //new BufferedReader(new InputStreamReader(in))理解为给水管匹配一个水龙头
                    //调用水龙头的不同方法可以以不同方式获取源头来的水
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    if (listener != null) {
                        //回调onFinish方法
                        listener.onFinish(response.toString());
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        //回调onError方法
                        listener.onError(e);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();

    }

    //GET请求
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);
    }

    //POST请求
    public static void sendOkHttpRequest(final String address, RequestBody requestBody, okhttp3.Callback callback) {
        CookieManager cookieManager = new CookieManager();
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieManager)
                .build();
        Log.d("运行阶段", "刚刚把client构造完");
//        if (mycookie != null) {
//            Log.d("查看Cookie是否为空", mycookie.toString());
//        }

        Request request = new Request.Builder()
                .url(address)
                .post(requestBody)
                .build();


        Log.d("运行阶段", "刚刚把request构造完");
        client.newCall(request).enqueue(callback);
        Log.d("运行阶段", "刚刚把启动线程完");
    }
}



