package com.example.cr.rxjavaexample.models;

public class CurrentWeather extends WeatherForcast
{
    private final float mTemperature;

    public CurrentWeather(final String locationName, final long timeStamp,final String description,
                          final float temperature, float minTemperature, float maxTemperature)
    {
        super(locationName, timeStamp, description, minTemperature, maxTemperature);
        mTemperature = temperature;
    }

    public float getmTemperature()
    {
        return mTemperature;
    }
}
