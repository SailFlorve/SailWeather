package com.sailflorve.sailweather.util;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil
{
    public static void sendOkHttpRequest(String address, Callback callback)
    {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

    public static void sendOkHttpRequestForCity(String address, String cityName, Callback callback)
    {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new FormBody.Builder()
                .add("city", cityName)
                .add("key", "d8adf978646b45e2875b82c9fed6d3eb")
                .build();

        Request request = new Request.Builder()
                .url(address)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(callback);
    }
}
