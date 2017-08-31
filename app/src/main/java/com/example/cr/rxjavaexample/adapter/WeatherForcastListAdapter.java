package com.example.cr.rxjavaexample.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ViewDragHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.cr.rxjavaexample.R;
import com.example.cr.rxjavaexample.helpers.DayFormatter;
import com.example.cr.rxjavaexample.helpers.TemperatureFormatter;
import com.example.cr.rxjavaexample.models.WeatherForcast;

import java.util.List;

public class WeatherForcastListAdapter extends ArrayAdapter
{
    public WeatherForcastListAdapter(final List<WeatherForcast> weatherForcast, final Context context)
    {
        super(context, 0, weatherForcast);
    }

    @Override
    public boolean isEnabled(int position)
    {
        return false;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent)
    {
        ViewHolder viewHolder;

        if(convertView == null)
        {
            final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.weather_forecast_list_item, null);

            viewHolder = new ViewHolder();
            viewHolder.dayTextView = (TextView) convertView.findViewById(R.id.day);
            viewHolder.descriptionTextView = (TextView) convertView.findViewById(R.id.description);
            viewHolder.maximumTemperatureTextView = (TextView) convertView.findViewById(R.id.maximum_temperature);
            viewHolder.minimumTemperatureTextView = (TextView) convertView.findViewById(R.id.minimum_temperature);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final WeatherForcast weatherForcast = (WeatherForcast) getItem(position);

        final DayFormatter dayFormatter = new DayFormatter(getContext());
        final String day = dayFormatter.format(weatherForcast.getTimeStamp());
        viewHolder.dayTextView.setText(day);
        viewHolder.descriptionTextView.setText(weatherForcast.getDescription());
        viewHolder.maximumTemperatureTextView.setText(TemperatureFormatter.format(weatherForcast.getMaxTemperature()));
        viewHolder.minimumTemperatureTextView.setText(TemperatureFormatter.format(weatherForcast.getMinTemperature()));

        return convertView;
    }

    private class ViewHolder
    {
        private TextView dayTextView, descriptionTextView,
                maximumTemperatureTextView, minimumTemperatureTextView;
    }
}
