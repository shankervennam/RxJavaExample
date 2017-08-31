package com.example.cr.rxjavaexample.helpers;

import android.content.ContentValues;
import android.content.Context;
import java.text.SimpleDateFormat;

import com.example.cr.rxjavaexample.R;

import java.util.Date;

public class DayFormatter
{
    private final static long MILLI_SECONDS_IN = 1000;
    private final Context context;

    public DayFormatter(Context context)
    {
        this.context = context;
    }

    /*
    Format timestamp into Unix readable
     */

    public String format(final long unitTimeStamp)
    {
        final long milliseconds = unitTimeStamp * MILLI_SECONDS_IN;
        String day;

        if(isToday(milliseconds))
        {
            day = context.getResources().getString(R.string.today);
        }
        else if(isTomorrow(milliseconds))
        {
            day = context.getResources().getString(R.string.tomorrow);
        }
        else
        {
            day = getDayOfWeek(milliseconds);
        }
        return day;
    }

    private String getDayOfWeek(final long milliseconds)
    {
        return new SimpleDateFormat("EEEE").format(new Date(milliseconds));
    }

    private boolean isTomorrow(long milliseconds)
    {
        final SimpleDateFormat dayInYearFormat = new SimpleDateFormat("yyyyD");
        final int tomorrowHash = Integer.parseInt(dayInYearFormat.format(new Date())) + 1;
        final int compareHash = Integer.parseInt(dayInYearFormat.format(new Date(milliseconds)));
        return compareHash == tomorrowHash;
    }

    private boolean isToday(final long milliseconds)
    {
        final SimpleDateFormat dayInYear = new SimpleDateFormat("yyyyD");
        final String nowHash = dayInYear.format(new Date());
        final String compareHash = dayInYear.format(new Date(milliseconds));
        return nowHash.equals(compareHash);
    }
}
