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

import com.forrestguice.suntimes.actions.SuntimesActionsContract;
import com.forrestguice.suntimes.addon.SuntimesInfo;
import com.forrestguice.suntimes.alarm.AlarmHelper;
import com.forrestguice.suntimes.intervalmidpoints.AppSettings;
import com.forrestguice.suntimes.intervalmidpoints.BuildConfig;
import com.forrestguice.suntimes.intervalmidpoints.MainActivity;
import com.forrestguice.suntimes.intervalmidpoints.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_ACTION_CLASS;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_ACTION_DESC;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_ACTION_NAME;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_ACTION_TITLE;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_ACTION_TYPE;

import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.AUTHORITY;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_CONFIG_APP_VERSION;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_CONFIG_APP_VERSION_CODE;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_CONFIG_PROVIDER;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_CONFIG_PROVIDER_VERSION;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_CONFIG_PROVIDER_VERSION_CODE;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_EVENT_NAME;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_EVENT_SUMMARY;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_EVENT_TIMEMILLIS;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.COLUMN_EVENT_TITLE;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.EXTRA_ALARM_NOW;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.EXTRA_ALARM_OFFSET;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.EXTRA_ALARM_REPEAT;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.EXTRA_ALARM_REPEAT_DAYS;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.EXTRA_LOCATION_ALT;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.EXTRA_LOCATION_LAT;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.EXTRA_LOCATION_LON;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.QUERY_ACTIONS;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.QUERY_EVENT_CALC;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.QUERY_EVENT_CALC_PROJECTION;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.QUERY_EVENT_INFO;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.QUERY_CONFIG;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.QUERY_CONFIG_PROJECTION;
import static com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract.QUERY_EVENT_INFO_PROJECTION;

public class IntervalMidpointsProvider extends ContentProvider
{
    private static final int URIMATCH_CONFIG = 0;

    private static final int URIMATCH_ACTION_INFO = 20;
    private static final int URIMATCH_ACTION_INFO_FOR_NAME = 30;

    private static final int URIMATCH_EVENT_INFO = 40;
    private static final int URIMATCH_EVENT_INFO_FOR_NAME = 50;
    private static final int URIMATCH_EVENT_CALC_FOR_NAME = 60;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static
    {
        uriMatcher.addURI(AUTHORITY, QUERY_CONFIG, URIMATCH_CONFIG);

        uriMatcher.addURI(AUTHORITY, QUERY_ACTIONS, URIMATCH_ACTION_INFO);
        uriMatcher.addURI(AUTHORITY, QUERY_ACTIONS + "/*", URIMATCH_ACTION_INFO_FOR_NAME);

        uriMatcher.addURI(AUTHORITY, QUERY_EVENT_INFO, URIMATCH_EVENT_INFO);
        uriMatcher.addURI(AUTHORITY, QUERY_EVENT_INFO + "/*", URIMATCH_EVENT_INFO_FOR_NAME);
        uriMatcher.addURI(AUTHORITY, QUERY_EVENT_CALC + "/*", URIMATCH_EVENT_CALC_FOR_NAME);
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
        HashMap<String, String> selectionMap = AlarmHelper.processSelection(AlarmHelper.processSelectionArgs(selection, selectionArgs));
        long[] range;
        Cursor cursor = null;
        int uriMatch = uriMatcher.match(uri);
        switch (uriMatch)
        {
            case URIMATCH_ACTION_INFO:
                Log.i(getClass().getSimpleName(), "URIMATCH_ACTION_INFO");
                cursor = queryActionInfo(null, uri, projection, selectionMap, sortOrder);
                break;

            case URIMATCH_ACTION_INFO_FOR_NAME:
                Log.i(getClass().getSimpleName(), "URIMATCH_ACTION_INFO_FOR_NAME");
                cursor = queryActionInfo(uri.getLastPathSegment(), uri, projection, selectionMap, sortOrder);
                break;

            case URIMATCH_EVENT_INFO:
                Log.i(getClass().getSimpleName(), "URIMATCH_EVENT_INFO");
                cursor = queryEventInfo(null, uri, projection, selectionMap, sortOrder);
                break;

            case URIMATCH_EVENT_INFO_FOR_NAME:
                Log.i(getClass().getSimpleName(), "URIMATCH_EVENT_INFO_FOR_NAME");
                cursor = queryEventInfo(uri.getLastPathSegment(), uri, projection, selectionMap, sortOrder);
                break;

            case URIMATCH_EVENT_CALC_FOR_NAME:
                Log.i(getClass().getSimpleName(), "URIMATCH_EVENT_CALC_FOR_NAME");
                cursor = queryEventTime(uri.getLastPathSegment(), uri, projection, selectionMap, sortOrder);
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

    public static final String ACTION_INTERVAL_MIDPOINTS = "INTERVAL_MIDPOINTS";
    public static final String[] ACTIONS = new String[] { ACTION_INTERVAL_MIDPOINTS };

    public String[] getAllActionIDs() {
        return ACTIONS;
    }
    public ContentValues getActionInfo(Context context, String actionID) {
        if (actionID.equals(ACTION_INTERVAL_MIDPOINTS)) {
            return createDefaultAction(context);
        } else return null;
    }

    public ContentValues createDefaultAction(Context context)
    {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ACTION_NAME, ACTION_INTERVAL_MIDPOINTS);
        values.put(COLUMN_ACTION_TITLE, context.getString(R.string.app_name));
        values.put(COLUMN_ACTION_DESC, context.getString(R.string.app_name));
        values.put(COLUMN_ACTION_TYPE, SuntimesActionsContract.TYPE_ACTIVITY);
        values.put(COLUMN_ACTION_CLASS, MainActivity.class.getName());
        return values;
    }

    public Cursor queryActionInfo(@Nullable String actionName, @NonNull Uri uri, @Nullable String[] projection, HashMap<String, String> selectionMap, @Nullable String sortOrder)
    {
        Log.d("DEBUG", "queryActionInfo: " + actionName);
        String[] columns = (projection != null ? projection : SuntimesActionsContract.QUERY_ACTION_PROJECTION_MIN);
        MatrixCursor cursor = new MatrixCursor(columns);

        Context context = getContext();
        if (context != null)
        {
            String[] actions = (actionName != null) ? new String[] { actionName } : getAllActionIDs();
            for (int j=0; j<actions.length; j++)
            {
                ContentValues actionValues = getActionInfo(context, actions[j]);
                Object[] row = new Object[columns.length];
                for (int i=0; i<columns.length; i++) {
                    row[i] = actionValues.getAsString(columns[i]);
                }
                cursor.addRow(row);
            }

        } else Log.d("DEBUG", "context is null!");
        return cursor;
    }

    /**
     * queryEventInfo
     */
    public Cursor queryEventInfo(@Nullable String eventName, @NonNull Uri uri, @Nullable String[] projection, HashMap<String, String> selectionMap, @Nullable String sortOrder)
    {
        Log.d("DEBUG", "queryEventInfo: " + eventName);
        String[] columns = (projection != null ? projection : QUERY_EVENT_INFO_PROJECTION);
        MatrixCursor cursor = new MatrixCursor(columns);

        Context context = getContext();
        if (context != null)
        {
            String[] alarms = (eventName != null)
                    ? new String[] { eventName }
                    : context.getResources().getStringArray(R.array.alarm_names);

            for (int j=0; j<alarms.length; j++)
            {
                Object[] row = new Object[columns.length];
                for (int i=0; i<columns.length; i++)
                {
                    switch (columns[i])
                    {
                        case COLUMN_EVENT_NAME:
                            row[i] = alarms[j];
                            break;

                        case COLUMN_EVENT_TITLE:
                            row[i] = getEventTitle(context, alarms[j]);
                            break;

                        case COLUMN_EVENT_SUMMARY:
                            row[i] = getEventSummary(context, alarms[j]);
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

    public static String getEventTitle(Context context, String eventName)
    {
        String[] interval = AppSettings.getInterval(eventName);
        int i = Integer.parseInt(interval[3]);
        int n = Integer.parseInt(interval[2]);

        String tag = getEventTag(context, i, n);
        if (n <= 2 || tag == null)
            return context.getString(R.string.alarm_title_format_short, interval[0], interval[1]);
        else return context.getString(R.string.alarm_title_format_long, interval[0], interval[1], tag);
    }

    @Nullable
    public static String getEventTag(Context context, int i, int n)
    {
        switch (n)
        {
            case 2: return null;
            /*case 3:
                switch (i)
                {
                    case 0: return "⅓";            // TODO: are these glyphs supported by all phones?
                    case 1: return "⅔";
                    default: return (i+1) + "/" + n;
                }*/
            case 4:
                switch (i)
                {
                    case 0: return "¼";
                    case 1: return null;
                    case 2: return "¾";
                    default: return context.getString(R.string.alarm_title_tag_format, (i+1) + "", n + "");
                }
            default: return context.getString(R.string.alarm_title_tag_format, (i+1) + "", n + "");
        }
    }

    public static String getEventSummary(Context context, String eventName) {
        return context.getString(R.string.alarm_summary_format);
    }

    public Cursor queryEventTime(@Nullable String eventName, @NonNull Uri uri, @Nullable String[] projection, HashMap<String, String> selectionMap, @Nullable String sortOrder)
    {
        Log.d("DEBUG", "queryEventTime: " + eventName);
        String[] columns = (projection != null ? projection : QUERY_EVENT_CALC_PROJECTION);
        MatrixCursor cursor = new MatrixCursor(columns);

        Context context = getContext();
        if (context != null)
        {
            Object[] row = new Object[columns.length];
            for (int i=0; i<columns.length; i++)
            {
                switch (columns[i])
                {
                    case COLUMN_EVENT_NAME:
                        row[i] = eventName;
                        break;

                    case COLUMN_EVENT_TIMEMILLIS:
                        row[i] = calculateEventTime(context, eventName, selectionMap);
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

    public long calculateEventTime(@NonNull Context context, @Nullable String eventName, HashMap<String, String> selectionMap)
    {
        if (AppSettings.isValidIntervalID(eventName))
        {
            Calendar now = AlarmHelper.getNowCalendar(selectionMap.get(EXTRA_ALARM_NOW));
            long nowMillis = now.getTimeInMillis();

            String offsetString = selectionMap.get(EXTRA_ALARM_OFFSET);
            long offset = offsetString != null ? Long.parseLong(offsetString) : 0L;

            boolean repeating = Boolean.parseBoolean(selectionMap.get(EXTRA_ALARM_REPEAT));
            ArrayList<Integer> repeatingDays = AlarmHelper.getRepeatDays(selectionMap.get(EXTRA_ALARM_REPEAT_DAYS));

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
            IntervalMidpointsData data = new IntervalMidpointsData(eventName, latitude, longitude, altitude);

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
    
}