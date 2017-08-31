package com.example.cr.rxjavaexample.services;

import com.example.cr.rxjavaexample.models.CurrentWeather;
import com.example.cr.rxjavaexample.models.WeatherForcast;
import com.google.gson.annotations.SerializedName;

//TODO import org.apache.http.HttpException;

import java.util.ArrayList;
import java.util.List;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;
import rx.functions.Func1;

public class WeatherService
{
    private static final String WEB_SERVICE_BASE_URL = "http://samples.openweathermap.org/data/2.5/";
    private static final String API_KEY = "forecast?id=524901&appid=b1b15e88fa797225412429c1c50c122a1";

    private final OpenWeatherMapWebService weatherMapWebService;

    public WeatherService()
    {
        final RequestInterceptor requestInterceptor = new RequestInterceptor()
        {
            @Override
            public void intercept(RequestFacade request)
            {
                request.addHeader("Accept", "application/json");
            }
        };

        RestAdapter restAdapter = new RestAdapter.Builder()
                            .setEndpoint(WEB_SERVICE_BASE_URL)
                            .setRequestInterceptor(requestInterceptor)
                            .setLogLevel(RestAdapter.LogLevel.FULL)
                            .build();

        weatherMapWebService = restAdapter.create(OpenWeatherMapWebService.class);
    }

    private interface OpenWeatherMapWebService
    {
        @GET("/weather?units=metric&apikey=" + API_KEY)
        Observable<CurrentWeatherDataEnvelope> fetchCurrentWeather(@Query("lon") double longitude,
                                                                   @Query("lat") double latitude);

        @GET("/forecast/daily?units=metric&cnt=7&apikey=" + API_KEY)
        Observable<WeatherForecastListDataEnvelope> fetchWeatherForecasts(@Query("lon") double longitude,
                                                                          @Query("lat") double latitude);
    }

    public Observable<CurrentWeather> fetchCurrentWeather(final double longitude,
                                                          final double latitude)
    {
        return weatherMapWebService.fetchCurrentWeather(longitude, latitude)
                .flatMap(new Func1<CurrentWeatherDataEnvelope,
                        Observable<? extends CurrentWeatherDataEnvelope>>() {
                    @Override
                    public Observable<? extends CurrentWeatherDataEnvelope> call(CurrentWeatherDataEnvelope currentWeatherDataEnvelope) {
                        return null;
                    }
        }).map(new Func1<CurrentWeatherDataEnvelope, CurrentWeather>()
                {
                    @Override
                    public CurrentWeather call(final CurrentWeatherDataEnvelope data)
                    {
                        return new CurrentWeather(data.locationName, data.timestamp,
                                data.weather.get(0).description, data.main.temp, data.main.temp_min, data.main.temp_max);
                    }
                });
    }

    public Observable<List<WeatherForcast>> fetchWeatherForecasts(final double longitude,
                                                                  final double latitude)
    {
        return weatherMapWebService.fetchWeatherForecasts(longitude, latitude)
                .flatMap(new Func1<WeatherForecastListDataEnvelope,
                        Observable<? extends WeatherForecastListDataEnvelope>>()
                {
                    @Override
                    public Observable<? extends WeatherForecastListDataEnvelope> call(final WeatherForecastListDataEnvelope currentWeatherDataEnvelope)
                    {
                        return currentWeatherDataEnvelope.filterWebServiceError();
                    }
                }).map(new Func1<WeatherForecastListDataEnvelope, List<WeatherForcast>>()
                {
                    @Override
                    public List<WeatherForcast> call(WeatherForecastListDataEnvelope weatherForecastListDataEnvelope)
                    {
                        final ArrayList<WeatherForcast> weatherForecast = new ArrayList<WeatherForcast>();

                        for(WeatherForecastListDataEnvelope.ForecastDataEnvelope data:weatherForecastListDataEnvelope.list)
                        {
                            final WeatherForcast weatherForecasts = new WeatherForcast(weatherForecastListDataEnvelope.city.name,
                                                                       data.timestamp, data.weather.get(0).description,
                                                                        data.temp.min, data.temp.max);
                            weatherForecast.add(weatherForecasts);
                        }
                        return weatherForecast;
                        }
                });
    }

    private class WeatherDataEnvelope
    {
        @SerializedName("cod")
        private int httpCode;

        class Weather
        {
            public String description;
        }

        public Observable filterWebServiceError()
        {
            if(httpCode == 200)
            {
                return Observable.just(this);
            }
//            else
//            {
//               //TODO return Observable.error(new HttpException("There was a problem fetching the weather data"));
//            }
            return null;
        }
    }

    private class CurrentWeatherDataEnvelope extends WeatherDataEnvelope
    {
        @SerializedName("name")
        public String locationName;

        @SerializedName("dt")
        public long timestamp;
        public ArrayList<Weather> weather;
        public Main main;

        class Main
        {
            public float temp, temp_min, temp_max;
        }
    }

    private class WeatherForecastListDataEnvelope extends WeatherDataEnvelope
    {
        public Location city;

        public ArrayList<ForecastDataEnvelope> list;

        class Location
        {
            public String name;
        }

        class ForecastDataEnvelope
        {
            @SerializedName("dt")
            public long timestamp;
            public Temperature temp;
            public ArrayList<Weather> weather;
        }

        class Temperature
        {
            public float min, max;
        }
    }
}
