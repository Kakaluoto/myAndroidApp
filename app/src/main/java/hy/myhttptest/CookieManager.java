package hy.myhttptest;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class CookieManager implements CookieJar {
    private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

    @Override

    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> cookies) {
//                        cookieStore.put(httpUrl,cookies);
        Log.d("运行阶段", "进入saveFromResponse");
        Log.d("httpUrl to string", httpUrl.toString());
        Log.d("httpUrl host to string", httpUrl.host());
        if (HttpUtil.mycookie != null) {
            Log.d("查看Cookie是否为空", HttpUtil.mycookie.toString());
        }else {
            Log.d("Cookie是为空", "");
        }
        if (HttpUtil.mycookie == null) {
            cookieStore.put(HttpUrl.parse("http://hyofficial.xyz/verify/"), cookies);
            HttpUtil.mycookie = cookies;
        }
        for (Cookie cookie : cookies) {
            Log.d("cookie Name:", cookie.name());
            Log.d("cookie Path:", cookie.path());
            Log.d("cookie.toString:", cookie.toString());
        }
    }

    @NotNull
    @Override
    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
//                        List<Cookie> cookies = cookieStore.get(HttpUrl.parse(httpUrl.host()));
        Log.d("运行阶段", "进入loadForResponse");
        for(HttpUrl key:cookieStore.keySet()){
            Log.d("cookieStore键值",key.toString());
            Log.d("cookieStore的内容",cookieStore.get(key).toString());
        }
        List<Cookie> cookies;
        if(HttpUtil.mycookie==null)
            cookies = cookieStore.get(HttpUrl.parse("http://hyofficial.xyz/verify/"));
        else
            cookies = HttpUtil.mycookie;
        if (cookies == null) {
            Log.d("error", "没加载到cookie");
        }
        return cookies != null ? cookies : new ArrayList<Cookie>();
    }

}
