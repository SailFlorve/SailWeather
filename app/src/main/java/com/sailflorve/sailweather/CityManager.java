package com.sailflorve.sailweather;


import android.util.Log;
import android.widget.Toast;

import com.sailflorve.sailweather.db.SavedCity;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class CityManager {
    private static String cities;

    private static List<String> cityStringList;
    private static List<SavedCity> savedCities;

    static {
        cityStringList = new ArrayList<>();
        savedCities = new ArrayList<>();
    }

    public static void loadCities() {
        cityStringList = new ArrayList<>();
        try {
            savedCities = DataSupport.findAll(SavedCity.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (savedCities.size() > 0) {
            for (SavedCity city : savedCities) {
                cityStringList.add(city.getName());
            }
        }
    }

    public static void addCity(SavedCity city) {
        List<SavedCity> cities = DataSupport.where("weatherId = ?", city.getWeatherId()).find(SavedCity.class);
        if (cities.size() > 0) return;
        city.save();
        cityStringList.add(city.getName());
        savedCities.add(city);
    }

    public static void deleteCity(int position) {
        savedCities.get(position).delete();
        Toast.makeText(MyApplication.getContext(), savedCities.get(position).getName() + " 已被删除", Toast.LENGTH_SHORT).show();
        cityStringList.remove(position);
        savedCities.remove(position);
    }

    public static List<String> getCityStringList() {
        return cityStringList;
    }

    public static List<SavedCity> getSavedCityList() {
        return savedCities;
    }
}
