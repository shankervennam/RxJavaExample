package com.example.cr.rxjavaexample.helpers;

public class TemperatureFormatter
{
    public static String format(float temperature)
    {
        return String.valueOf(Math.round(temperature)) + "o";
    }
}
