package com.sailflorve.sailweather.gson;

/**
 * Created by xdygx on 2017/2/3 0003.
 */

public class CityInfo
{
    public Basic basic;
    public String status;

    public class Basic
    {
        public String city;
        public String id;
    }
}
