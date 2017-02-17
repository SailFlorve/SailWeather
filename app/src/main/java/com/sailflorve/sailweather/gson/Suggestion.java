package com.sailflorve.sailweather.gson;


import com.google.gson.annotations.SerializedName;

public class Suggestion {
    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;

    public Sport sport;

    @SerializedName("drsg")
    public Dress dress;

    @SerializedName("flu")
    public Flu flu;

    @SerializedName("trav")
    public Travel travel;

    @SerializedName("uv")
    public UV uv;

    public class Comfort {
        @SerializedName("brf")
        public String level;

        @SerializedName("txt")
        public String info;
    }

    public class CarWash {
        @SerializedName("brf")
        public String level;

        @SerializedName("txt")
        public String info;
    }

    public class Sport {
        @SerializedName("brf")
        public String level;

        @SerializedName("txt")
        public String info;
    }

    public class Dress {
        @SerializedName("brf")
        public String level;

        @SerializedName("txt")
        public String info;
    }

    public class Flu {
        @SerializedName("brf")
        public String level;

        @SerializedName("txt")
        public String info;
    }

    public class Travel {
        @SerializedName("brf")
        public String level;

        @SerializedName("txt")
        public String info;
    }

    public class UV {
        @SerializedName("brf")
        public String level;

        @SerializedName("txt")
        public String info;
    }
}
