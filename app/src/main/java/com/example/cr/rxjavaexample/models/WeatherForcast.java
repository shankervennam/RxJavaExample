package com.example.cr.rxjavaexample.models;

public class WeatherForcast
{
    private final String locationName;
    private final long timeStamp;
    private final String description;
    private final float minTemperature;
    private final float maxTemperature;

    public WeatherForcast(String locationName, long timeStamp, String description, float minTemperature, float maxTemperature)
    {
        this.locationName = locationName;
        this.timeStamp = timeStamp;
        this.description = description;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
    }

    public String getLocationName() {
        return locationName;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getDescription() {
        return description;
    }

    public float getMinTemperature() {
        return minTemperature;
    }

    public float getMaxTemperature() {
        return maxTemperature;
    }
}
