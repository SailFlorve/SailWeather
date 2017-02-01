package com.sailflorve.sailweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now
{
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public Wind wind;

    public class More
    {
        @SerializedName("txt")
        public String info;

        public String code;
    }

    public class Wind
    {
        @SerializedName("dir")
        public String direction;

        @SerializedName("sc")
        public String power;
    }
}
