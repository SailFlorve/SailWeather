package com.sailflorve.sailweather;


import android.widget.Toast;

import com.sailflorve.sailweather.db.City;
import com.sailflorve.sailweather.util.Settings;

import java.util.ArrayList;
import java.util.List;

public class CityManager {
    private static String cities;

    private static List<String> cityList;

    private static Settings settings;

    static {
        settings = new Settings(MyApplication.getContext());
        cityList = new ArrayList<>();
    }

    public static void loadCities() {
        cityList = new ArrayList<>();
        cities = (String) settings.get("saved_cities", null);

        if (cities != null) {
            String[] cityArray = cities.split(",");
            for (String city : cityArray) {
                addCity(city);
            }
        }
    }

    public static void saveCities() {
        StringBuilder builder = new StringBuilder();
        for (String city : cityList) {
            builder.append(city);
            builder.append(",");
        }
        settings.put("saved_cities", builder.toString());
    }

    public static void addCity(String cityName) {
        if (!cityList.contains(cityName)) {
            cityList.add(cityName);
            saveCities();
        }
    }

    public static void deleteCity(String cityName) {
        Toast.makeText(MyApplication.getContext(), "已删除 " + cityName, Toast.LENGTH_SHORT).show();
        cityList.remove(cityName);
        saveCities();
    }

    public static List<String> getCityList() {
        return cityList;
    }
}
