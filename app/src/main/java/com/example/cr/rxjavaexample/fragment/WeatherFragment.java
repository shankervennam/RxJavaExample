package com.example.cr.rxjavaexample.fragment;

import android.app.Fragment;
import android.content.Context;
import android.database.CursorIndexOutOfBoundsException;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import com.example.cr.rxjavaexample.R;
import com.example.cr.rxjavaexample.adapter.WeatherForcastListAdapter;
import com.example.cr.rxjavaexample.helpers.TemperatureFormatter;
import com.example.cr.rxjavaexample.models.CurrentWeather;
import com.example.cr.rxjavaexample.models.WeatherForcast;
import com.example.cr.rxjavaexample.services.LocationService;
import com.example.cr.rxjavaexample.services.WeatherService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okio.Timeout;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subscriptions.CompositeSubscription;

public class WeatherFragment extends Fragment
{
    private static final String KEY_CURRENT_WEATHER = "key_current_Weather";
    private static final String KEY_WEATHER_FORECAST = "key_weather_forecast";
    private static final long LOCATION_TIMEOUT_SECONDS = 20;
    private static final String TAG = WeatherFragment.class.getCanonicalName();

    private CompositeSubscription compositeSubscription;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView locationTextView, temperatureTextView, attributionTextView;
    private ListView forecastListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState)
    {
        compositeSubscription = new CompositeSubscription();

        final View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        locationTextView = (TextView) container.findViewById(R.id.loc_name);

        temperatureTextView = (TextView) container.findViewById(R.id.current_temp);

        forecastListView = (ListView) container.findViewById(R.id.weather_forecast);

        final WeatherForcastListAdapter adapter = new WeatherForcastListAdapter(new ArrayList<WeatherForcast>(), getActivity());
        forecastListView.setAdapter(adapter);

        attributionTextView = (TextView) rootView.findViewById(R.id.attribute);

        attributionTextView.setVisibility(View.INVISIBLE);

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swip_refresh);

        swipeRefreshLayout.setColorSchemeResources(R.color.brand_main, R.color.colorAccent, R.color.colorPrimaryDark);

        swipeRefreshLayout.setOnRefreshListener((new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                updateWeather();
            }
        }));

        updateWeather();

        return rootView;
    }

    private void updateWeather()
    {
        swipeRefreshLayout.setRefreshing(true);

        final LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        final LocationService locationService = new LocationService(locationManager);

        final Observable fetchObservable = locationService.getLocation()
                .timeout(LOCATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .flatMap(new Func1<Location, Observable<HashMap<String, WeatherForcast>>>()
                {
                    @Override
                    public Observable<HashMap<String, WeatherForcast>> call(Location location)
                    {
                        final WeatherService weatherService = new WeatherService();
                        final double longitude = location.getLongitude();
                        final double latitude = location.getLatitude();


                        return Observable.zip(
                                weatherService.fetchCurrentWeather(longitude, latitude),
                                weatherService.fetchWeatherForecasts(longitude, latitude),
                                new Func2<CurrentWeather, List<WeatherForcast>,
                                        HashMap<String, WeatherForcast>>()
                                {
                                    @Override
                                    public HashMap<String, WeatherForcast> call(CurrentWeather currentWeather,
                                                                                List<WeatherForcast> weatherForcasts)
                                    {
                                        HashMap weatherData = new HashMap();
                                        weatherData.put(KEY_CURRENT_WEATHER, currentWeather);
                                        weatherData.put(KEY_WEATHER_FORECAST, weatherForcasts);
                                        return weatherData;
                                    }
                                }
                        );
                    }
                });
        compositeSubscription.add(fetchObservable
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<HashMap<String, WeatherForcast>>()
                            {
                                @Override
                                public void onCompleted()
                                {
                                    swipeRefreshLayout.setRefreshing(false);
                                    attributionTextView.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onError(Throwable e)
                                {
                                    swipeRefreshLayout.setRefreshing(false);

                                    if(e instanceof TimeoutException)
                                    {
                                        Crouton.makeText(getActivity(), R.string.error_location_unavailable, Style.ALERT).show();
                                    }
                                    else if(e instanceof TimeoutException )
                                    {
                                        Crouton.makeText(getActivity(), R.string.error_fetch_weather, Style.ALERT).show();
                                    }
                                    else
                                    {
                                        Log.e(TAG, e.getMessage());
                                        e.printStackTrace();
                                        throw new RuntimeException("see inner exception");
                                    }
                                }

                                @Override
                                public void onNext(HashMap<String, WeatherForcast> stringWeatherForcastHashMap)
                                {
                                    //updating UI weather
                                    final CurrentWeather currentWeather = (CurrentWeather) stringWeatherForcastHashMap.get(KEY_CURRENT_WEATHER);
                                    locationTextView.setText(currentWeather.getLocationName());
                                    temperatureTextView.setText(TemperatureFormatter.format(currentWeather.getmTemperature()));

                                    //update weather forecast
                                    final List<WeatherForcast> weatherForecasts = (List<WeatherForcast>)
                                            stringWeatherForcastHashMap.get(KEY_WEATHER_FORECAST);
                                    final WeatherForcastListAdapter adapter = (WeatherForcastListAdapter) forecastListView.getAdapter();
                                    adapter.clear();
                                    adapter.addAll(weatherForecasts);
                                }
                            }));
    }
}
