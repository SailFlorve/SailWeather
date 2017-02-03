package com.sailflorve.sailweather;


import com.sailflorve.sailweather.db.City;
import com.sailflorve.sailweather.util.Settings;

import java.util.ArrayList;
import java.util.List;

public class CityManager
{
    private static String cities;

    private static List<String> cityList;

    private static Settings settings;

    static
    {
        cityList = new ArrayList<>();
        settings = new Settings(MyApplication.getContext());
        cities = (String) settings.get("saved_cities", null);
    }

    public static void loadCities()
    {
        if (cities != null)
        {
            String[] cityArray = cities.split(",");
            for (String city : cityArray)
            {
                cityList.add(city);
            }
        }
    }

    public static void saveCities()
    {
        StringBuilder builder = new StringBuilder();
        for (String city : cityList)
        {
            builder.append(city);
            builder.append(",");
        }
        settings.put("saved_cities", builder.toString());
    }

    public static void addCity(String cityName)
    {
        if (!cityList.contains(cityName))
        {
            cityList.add(cityName);
        }
    }

    public static void deleteCity(String cityName)
    {
        cityList.remove(cityName);
    }

    public static List<String> getCityList()
    {
        return cityList;
    }
}
