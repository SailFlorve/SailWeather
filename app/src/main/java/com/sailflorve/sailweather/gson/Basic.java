package com.sailflorve.sailweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String cityWeatherId;

    @SerializedName("prov")
    public String province;

    @SerializedName("cnty")
    public String country;

    public Update update;

    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}

