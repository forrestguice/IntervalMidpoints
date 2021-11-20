// SPDX-License-Identifier: GPL-3.0-or-later
/*
    Copyright (C) 2021 Forrest Guice
    This file is part of Suntimes.

    Suntimes is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Suntimes is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Suntimes.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.suntimes.intervalmidpoints.ui;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;

import com.forrestguice.suntimes.addon.SuntimesInfo;
import com.forrestguice.suntimes.intervalmidpoints.R;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class DisplayStrings
{
    public static CharSequence formatDate(@NonNull Context context, long date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return formatDate(context, calendar);
    }

    public static CharSequence formatDate(@NonNull Context context, Calendar date)
    {
        Calendar now = Calendar.getInstance(date.getTimeZone());
        boolean isThisYear = now.get(Calendar.YEAR) == date.get(Calendar.YEAR);

        if (dateFormat_short == null || dateFormat_long == null)
        {
            Locale locale = Locale.getDefault();
            dateFormat_short = new SimpleDateFormat(context.getString( R.string.format_date ), locale);
            dateFormat_long = new SimpleDateFormat(context.getString( R.string.format_date_long), locale);
        }

        SimpleDateFormat dateFormat = isThisYear ? dateFormat_short : dateFormat_long;
        dateFormat.setTimeZone(date.getTimeZone());
        return dateFormat.format(date.getTime());
    }
    private static SimpleDateFormat dateFormat_short = null, dateFormat_long = null;


    public static CharSequence formatTime(@NonNull Context context, long dateTime, TimeZone timezone, boolean is24Hr)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateTime);
        String format = (is24Hr ? context.getString(R.string.format_time24) : context.getString(R.string.format_time12));
        SimpleDateFormat timeFormat = new SimpleDateFormat(format, Locale.getDefault());
        timeFormat.setTimeZone(timezone);
        return timeFormat.format(calendar.getTime());
    }

    public static SpannableString formatLocation(@NonNull Context context, @NonNull SuntimesInfo info)
    {
        if (info.location == null || info.location.length < 4) {
            return new SpannableString("");
        }

        SuntimesInfo.SuntimesOptions options = info.getOptions(context);
        boolean useAltitude = options.use_altitude;
        if (!useAltitude || info.location[3] == null || info.location[3].equals("0") || info.location[3].isEmpty()) {
            return formatLocation(context, Double.parseDouble(info.location[1]), Double.parseDouble(info.location[2]), null);
        } else {
            try {
                return formatLocation(context, Double.parseDouble(info.location[1]), Double.parseDouble(info.location[2]), Double.parseDouble(info.location[3]), 4, options.length_units);

            } catch (NumberFormatException e) {
                Log.e("formatLocation", "invalid altitude! " + e);
                return new SpannableString(context.getString(R.string.format_location, info.location[1], info.location[2]));
            }
        }
    }

    public static SpannableString formatLocation(@NonNull Context context, double latitude, double longitude, @Nullable Integer places)
    {
        formatter.setRoundingMode(RoundingMode.FLOOR);
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(places != null ? places : 4);
        return new SpannableString(context.getString(R.string.format_location, formatter.format(latitude), formatter.format(longitude)));
    }

    public static SpannableString formatLocation(@NonNull Context context, double latitude, double longitude, double meters, @Nullable Integer places, String units)
    {
        String altitude = formatHeight(context, meters, units, 0, true);
        String altitudeTag = context.getString(R.string.format_tag, altitude);
        formatter.setRoundingMode(RoundingMode.FLOOR);
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(places != null ? places : 4);
        String displayString = context.getString(R.string.format_location_long, formatter.format(latitude), formatter.format(longitude), altitudeTag);
        return createRelativeSpan(null, displayString, altitudeTag, 0.5f);
    }

    public static String formatHeight(Context context, double meters, String units, int places, boolean shortForm)
    {
        double value;
        String unitsString;
        if (units != null && units.equals(SuntimesInfo.SuntimesOptions.UNITS_IMPERIAL)) {
            value = 3.28084d * meters;
            unitsString = (shortForm ? context.getString(R.string.units_feet_short) : context.getString(R.string.units_feet));

        } else {
            value = meters;
            unitsString = (shortForm ? context.getString(R.string.units_meters_short) : context.getString(R.string.units_meters));
        }
        formatter.setRoundingMode(RoundingMode.FLOOR);
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(places);
        return context.getString(R.string.format_location_altitude, formatter.format(value), unitsString);
    }
    private static NumberFormat formatter = NumberFormat.getInstance();

    public static SpannableString createRelativeSpan(@Nullable SpannableString span, @NonNull String text, @NonNull String toRelative, float relativeSize)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toRelative);
        if (start >= 0) {
            int end = start + toRelative.length();
            span.setSpan(new RelativeSizeSpan(relativeSize), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    public static SpannableString createColorSpan(SpannableString span, String text, String toColorize, int color)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toColorize);
        if (start >= 0)
        {
            int end = start + toColorize.length();
            span.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    public static Spanned fromHtml(String htmlString )
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY);
        else return Html.fromHtml(htmlString);
    }

}
