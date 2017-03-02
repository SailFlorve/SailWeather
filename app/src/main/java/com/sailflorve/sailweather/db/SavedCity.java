package com.sailflorve.sailweather.db;

import org.litepal.crud.DataSupport;

public class SavedCity extends DataSupport {
    private String name;
    private String weatherId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }
}
