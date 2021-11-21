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

package com.forrestguice.suntimes.intervalmidpoints.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimes.addon.SuntimesInfo;
import com.forrestguice.suntimes.intervalmidpoints.AppSettings;
import com.forrestguice.suntimes.intervalmidpoints.BuildConfig;
import com.forrestguice.suntimes.intervalmidpoints.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.AUTHORITY;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_ALARM_NAME;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_ALARM_SUMMARY;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_ALARM_TIMEMILLIS;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_ALARM_TITLE;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_CONFIG_APP_VERSION;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_CONFIG_APP_VERSION_CODE;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_CONFIG_PROVIDER;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_CONFIG_PROVIDER_VERSION;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_CONFIG_PROVIDER_VERSION_CODE;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.EXTRA_ALARM_NOW;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.EXTRA_ALARM_OFFSET;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.EXTRA_ALARM_REPEAT;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.EXTRA_ALARM_REPEAT_DAYS;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.EXTRA_LOCATION_ALT;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.EXTRA_LOCATION_LAT;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.EXTRA_LOCATION_LON;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.QUERY_ALARM_CALC;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.QUERY_ALARM_CALC_PROJECTION;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.QUERY_ALARM_INFO;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.QUERY_ALARM_INFO_PROJECTION;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.QUERY_CONFIG;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.QUERY_CONFIG_PROJECTION;

public class IntervalMidpointsProvider extends ContentProvider
{
    private static final int URIMATCH_CONFIG = 0;
    private static final int URIMATCH_ALARM_INFO = 40;
    private static final int URIMATCH_ALARM_INFO_FOR_NAME = 50;
    private static final int URIMATCH_ALARM_CALC_FOR_NAME = 60;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static
    {
        uriMatcher.addURI(AUTHORITY, QUERY_CONFIG, URIMATCH_CONFIG);
        uriMatcher.addURI(AUTHORITY, QUERY_ALARM_INFO, URIMATCH_ALARM_INFO);
        uriMatcher.addURI(AUTHORITY, QUERY_ALARM_INFO + "/*", URIMATCH_ALARM_INFO_FOR_NAME);
        uriMatcher.addURI(AUTHORITY, QUERY_ALARM_CALC + "/*", URIMATCH_ALARM_CALC_FOR_NAME);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        HashMap<String, String> selectionMap = processSelection(processSelectionArgs(selection, selectionArgs));
        long[] range;
        Cursor cursor = null;
        int uriMatch = uriMatcher.match(uri);
        switch (uriMatch)
        {
            case URIMATCH_ALARM_INFO:
                Log.i(getClass().getSimpleName(), "URIMATCH_ALARM_INFO");
                cursor = queryAlarmInfo(null, uri, projection, selectionMap, sortOrder);
                break;

            case URIMATCH_ALARM_INFO_FOR_NAME:
                Log.i(getClass().getSimpleName(), "URIMATCH_ALARM_INFO_FOR_NAME");
                cursor = queryAlarmInfo(uri.getLastPathSegment(), uri, projection, selectionMap, sortOrder);
                break;

            case URIMATCH_ALARM_CALC_FOR_NAME:
                Log.i(getClass().getSimpleName(), "URIMATCH_ALARM_CALC_FOR_NAME");
                cursor = queryAlarmTime(uri.getLastPathSegment(), uri, projection, selectionMap, sortOrder);
                break;

            case URIMATCH_CONFIG:
                Log.i(getClass().getSimpleName(), "URIMATCH_CONFIG");
                cursor = queryConfig(uri, projection, selectionMap, sortOrder);
                break;

            default:
                Log.e(getClass().getSimpleName(), "Unrecognized URI! " + uri);
                break;
        }
        return cursor;
    }

    /**
     * queryAlarmInfo
     */
    public Cursor queryAlarmInfo(@Nullable String alarmName, @NonNull Uri uri, @Nullable String[] projection, HashMap<String, String> selectionMap, @Nullable String sortOrder)
    {
        Log.d("DEBUG", "queryAlarmInfo: " + alarmName);
        String[] columns = (projection != null ? projection : QUERY_ALARM_INFO_PROJECTION);
        MatrixCursor cursor = new MatrixCursor(columns);

        Context context = getContext();
        if (context != null)
        {
            String[] alarms = (alarmName != null)
                    ? new String[] { alarmName }
                    : context.getResources().getStringArray(R.array.alarm_names);

            for (int j=0; j<alarms.length; j++)
            {
                Object[] row = new Object[columns.length];
                for (int i=0; i<columns.length; i++)
                {
                    switch (columns[i])
                    {
                        case COLUMN_ALARM_NAME:
                            row[i] = alarms[j];
                            break;

                        case COLUMN_ALARM_TITLE:
                            row[i] = getAlarmTitle(context, alarms[j]);
                            break;

                        case COLUMN_ALARM_SUMMARY:
                            row[i] = getAlarmSummary(context, alarms[j]);
                            break;

                        default:
                            row[i] = null;
                            break;
                    }
                }
                cursor.addRow(row);
            }

        } else Log.d("DEBUG", "context is null!");
        return cursor;
    }

    public static String getAlarmTitle(Context context, String alarmName)
    {
        String[] interval = AppSettings.getInterval(alarmName);
        int i = Integer.parseInt(interval[3]);
        int n = Integer.parseInt(interval[2]);
        if (n <= 2)
            return context.getString(R.string.alarm_title_format_short, interval[0], interval[1]);
        else return context.getString(R.string.alarm_title_format_long, interval[0], interval[1], (i+1) + "", n + "");
    }

    public static String getAlarmSummary(Context context, String alarmName) {
        return context.getString(R.string.alarm_summary_format);
    }

    public Cursor queryAlarmTime(@Nullable String alarmName, @NonNull Uri uri, @Nullable String[] projection, HashMap<String, String> selectionMap, @Nullable String sortOrder)
    {
        Log.d("DEBUG", "queryAlarmTime: " + alarmName);
        String[] columns = (projection != null ? projection : QUERY_ALARM_CALC_PROJECTION);
        MatrixCursor cursor = new MatrixCursor(columns);

        Context context = getContext();
        if (context != null)
        {
            Object[] row = new Object[columns.length];
            for (int i=0; i<columns.length; i++)
            {
                switch (columns[i])
                {
                    case COLUMN_ALARM_NAME:
                        row[i] = alarmName;
                        break;

                    case COLUMN_ALARM_TIMEMILLIS:
                        row[i] = calculateAlarmTime(context, alarmName, selectionMap);
                        break;

                    default:
                        row[i] = null;
                        break;
                }
            }
            cursor.addRow(row);

        } else Log.d("DEBUG", "context is null!");
        return cursor;
    }

    public static ArrayList<Integer> getRepeatDays(@Nullable String repeatDaysString)
    {
        ArrayList<Integer> result = new ArrayList<>();
        if (repeatDaysString != null)
        {
            repeatDaysString = repeatDaysString.replaceAll("\\[", "");
            repeatDaysString = repeatDaysString.replaceAll("]", "");
            String[] repeatDaysArray = repeatDaysString.split(",");
            for (int i=0; i<repeatDaysArray.length; i++) {
                String element = repeatDaysArray[i].trim();
                if (!element.isEmpty()) {
                    result.add(Integer.parseInt(element));
                }
            }
        }
        return result;
    }

    public static Calendar getNowCalendar(String nowString)
    {
        long nowMillis = (nowString != null ? Long.parseLong(nowString) : System.currentTimeMillis());
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(nowMillis);
        return now;
    }

    public long calculateAlarmTime(@NonNull Context context, @Nullable String alarmName, HashMap<String, String> selectionMap)
    {
        if (AppSettings.isValidIntervalID(alarmName))
        {
            Calendar now = getNowCalendar(selectionMap.get(EXTRA_ALARM_NOW));
            long nowMillis = now.getTimeInMillis();

            String offsetString = selectionMap.get(EXTRA_ALARM_OFFSET);
            long offset = offsetString != null ? Long.parseLong(offsetString) : 0L;

            boolean repeating = Boolean.parseBoolean(selectionMap.get(EXTRA_ALARM_REPEAT));
            ArrayList<Integer> repeatingDays = getRepeatDays(selectionMap.get(EXTRA_ALARM_REPEAT_DAYS));

            String latitudeString = selectionMap.get(EXTRA_LOCATION_LAT);
            String longitudeString = selectionMap.get(EXTRA_LOCATION_LON);
            String altitudeString = selectionMap.get(EXTRA_LOCATION_ALT);
            Double latitude = latitudeString != null ? Double.parseDouble(latitudeString) : null;
            Double longitude = longitudeString != null ? Double.parseDouble(longitudeString) : null;
            double altitude = altitudeString != null ? Double.parseDouble(altitudeString) : 0;
            if (latitude == null || longitude == null)
            {
                SuntimesInfo info = SuntimesInfo.queryInfo(context);
                latitude = Double.parseDouble(info.location[1]);
                longitude = Double.parseDouble(info.location[2]);
                altitude = Double.parseDouble(info.location[3]);
            }

            Log.d("DEBUG", "calculateAlarmTime: now: " + nowMillis + ", offset: " + offset + ", repeat: " + repeating + ", repeatDays: " + selectionMap.get(EXTRA_ALARM_REPEAT_DAYS)
                    + ", latitude: " + latitude + ", longitude: " + longitude + ", altitude: " + altitude);

            IntervalMidpointsCalculator calculator = new IntervalMidpointsCalculator();
            IntervalMidpointsData data = new IntervalMidpointsData(alarmName, latitude, longitude, altitude);

            Calendar alarmTime = Calendar.getInstance();
            Calendar eventTime;

            Calendar day = Calendar.getInstance();
            data.setDate(day.getTimeInMillis());
            calculator.calculateData(context, data);
            eventTime = data.getMidpoint(data.index);
            if (eventTime != null)
            {
                eventTime.set(Calendar.SECOND, 0);
                alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
            }

            int c = 0;
            Set<Long> timestamps = new HashSet<>();
            while (now.after(alarmTime)
                    || eventTime == null
                    || (repeating && !repeatingDays.contains(eventTime.get(Calendar.DAY_OF_WEEK))))
            {
                if (!timestamps.add(alarmTime.getTimeInMillis()) && c > 365) {
                    Log.e(getClass().getSimpleName(), "updateAlarmTime: encountered same timestamp twice! (breaking loop)");
                    return -1L;
                }

                Log.w(getClass().getSimpleName(), "updateAlarmTime: advancing by 1 day..");
                day.add(Calendar.DAY_OF_YEAR, 1);
                data.setDate(day.getTimeInMillis());
                calculator.calculateData(context, data);
                eventTime = data.getMidpoint(data.index);
                if (eventTime != null)
                {
                    eventTime.set(Calendar.SECOND, 0);
                    alarmTime.setTimeInMillis(eventTime.getTimeInMillis() + offset);
                }
                c++;
            }

            return eventTime.getTimeInMillis();

        } else {
            return -1L;
        }
    }

    /**
     * queryConfig
     */
    public Cursor queryConfig(@NonNull Uri uri, @Nullable String[] projection, HashMap<String, String> selectionMap, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : QUERY_CONFIG_PROJECTION);
        MatrixCursor cursor = new MatrixCursor(columns);

        Context context = getContext();
        if (context != null)
        {
            //SuntimesInfo config = SuntimesInfo.queryInfo(context);
            Object[] row = new Object[columns.length];
            for (int i=0; i<columns.length; i++)
            {
                switch (columns[i])
                {
                    case COLUMN_CONFIG_PROVIDER:
                        row[i] = AUTHORITY;
                        break;

                    case COLUMN_CONFIG_PROVIDER_VERSION:
                        row[i] = IntervalMidpointsProviderContract.VERSION_NAME;
                        break;

                    case COLUMN_CONFIG_PROVIDER_VERSION_CODE:
                        row[i] = IntervalMidpointsProviderContract.VERSION_CODE;
                        break;

                    case COLUMN_CONFIG_APP_VERSION:
                        row[i] = BuildConfig.VERSION_NAME + (BuildConfig.DEBUG ? " [" + BuildConfig.BUILD_TYPE + "]" : "");
                        break;

                    case COLUMN_CONFIG_APP_VERSION_CODE:
                        row[i] = BuildConfig.VERSION_CODE;
                        break;

                    default:
                        row[i] = null;
                        break;
                }
            }
            cursor.addRow(row);

        } else Log.d("DEBUG", "context is null!");
        return cursor;
    }

    /**
     * processSelection
     * A query helper method; extracts selection columns/values to HashMap.
     * @param selection a completed selection string (@see processSelectionArgs)
     * @return a HashMap containing <KEY, VALUE> pairs
     */
    public static HashMap<String, String> processSelection(@Nullable String selection)
    {
        HashMap<String, String> retValue = new HashMap<>();
        if (selection != null)
        {
            String[] expressions = selection.split(" or | OR | and | AND ");  // just separators in this context (all interpreted the same)
            for (String expression : expressions)
            {
                String[] parts = expression.split("=");
                if (parts.length == 2) {
                    retValue.put(parts[0].trim(), parts[1].trim());
                } else Log.w("CalendarProvider", "processSelection: Too many parts! " + expression);
            }
        }
        return retValue;
    }

    /**
     * processSelectionArgs
     * A query helper method; inserts arguments into selection string.
     * @param selection a selection string (as passed to query)
     * @param selectionArgs a list of selection arguments
     * @return a completed selection string containing substituted arguments
     */
    @Nullable
    public static String processSelectionArgs(@Nullable String selection, @Nullable String[] selectionArgs)
    {
        String retValue = selection;
        if (selectionArgs != null && selection != null)
        {
            for (int i=0; i<selectionArgs.length; i++)
            {
                if (selectionArgs[i] != null)
                {
                    if (retValue.contains("?")) {
                        retValue = retValue.replaceFirst("\\?", selectionArgs[i]);

                    } else {
                        Log.w("CalendarProvider", "processSelectionArgs: Too many arguments! Given " + selectionArgs.length + " arguments, but selection contains only " + (i+1));
                        break;
                    }
                }
            }
        }
        return retValue;
    }

    public static String getAlarmInfoUri(String midpointID) {
        return "content://" + IntervalMidpointsProviderContract.AUTHORITY + "/" + IntervalMidpointsProviderContract.QUERY_ALARM_INFO + "/" + midpointID;
    }

}