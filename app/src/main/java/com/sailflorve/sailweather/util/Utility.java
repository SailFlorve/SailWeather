package com.sailflorve.sailweather.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.sailflorve.sailweather.MyApplication;
import com.sailflorve.sailweather.db.City;
import com.sailflorve.sailweather.db.County;
import com.sailflorve.sailweather.db.Province;
import com.sailflorve.sailweather.gson.BingImages;
import com.sailflorve.sailweather.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utility {
    //response为JSON数组，先把数组里每个省的信息对应到Province类，再将Province类添加到数据库。
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    //解析出来json数据，为一组天气数据，返回实例化的weather类
    public static Weather handleWeatherResponse(String response) {
        String weatherJson = response.replace("HeWeather5", "HeWeather");
        try {
            JSONObject jsonObject = new JSONObject(weatherJson);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(jsonArray.length() - 1).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List<Weather> handleInputCityResponse(String response) {
        String weatherJson = response.replace("HeWeather5", "HeWeather");
        List<Weather> weathers = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(weatherJson);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            for (int i = 0; i < jsonArray.length(); i++) {
                String weatherContent = jsonArray.getJSONObject(i).toString();
                weathers.add(new Gson().fromJson(weatherContent, Weather.class));
            }
            return weathers;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BingImages handleBingResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("images");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, BingImages.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //返回网络是否畅通
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    public static int compareVersion(String version1, String version2) {
        if (version1.equals(version2)) {
            return 0;
        }

        String[] version1Array = version1.split("\\.");
        String[] version2Array = version2.split("\\.");

        int index = 0;
        int minLen = Math.min(version1Array.length, version2Array.length);
        int diff = 0;

        while (index < minLen && (diff =
                Integer.parseInt(version1Array[index]) - Integer.parseInt(version2Array[index])) == 0) {
            index++;
        }

        if (diff == 0) {
            for (int i = index; i < version1Array.length; i++) {
                if (Integer.parseInt(version1Array[i]) > 0) {
                    return 1;
                }
            }

            for (int i = index; i < version2Array.length; i++) {
                if (Integer.parseInt(version2Array[i]) > 0) {
                    return -1;
                }
            }
            return 0;
        } else {
            return diff > 0 ? 1 : -1;
        }
    }

    public static boolean isNewDay() {
        Settings settings = new Settings(MyApplication.getContext());
        String oldDay = (String) settings.get("day", "0");

        SimpleDateFormat formatter = new SimpleDateFormat("dd", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String newDay = formatter.format(curDate);

        if (oldDay.equals(newDay)) return false;
        else {
            settings.put("day", newDay);
            return true;
        }
    }
}
