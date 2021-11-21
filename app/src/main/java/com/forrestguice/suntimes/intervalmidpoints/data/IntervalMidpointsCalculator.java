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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.forrestguice.suntimes.calculator.core.CalculatorProviderContract;
import com.forrestguice.suntimes.intervalmidpoints.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class IntervalMidpointsCalculator
{
    public IntervalMidpointsCalculator() {
    }

    public boolean calculateData(@NonNull Context context, @NonNull IntervalMidpointsData data)
    {
        if (data.date == -1L) {
            data.date = Calendar.getInstance().getTimeInMillis();
        }

        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(data.date);

        Calendar other = Calendar.getInstance();
        other.setTimeInMillis(data.date);

        ArrayList<String> eventCollection = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.event_values)));
        int startPosition = eventCollection.indexOf(data.startEvent);
        int endPosition = eventCollection.indexOf(data.endEvent);
        if (startPosition >= endPosition) {
            other.add(Calendar.DAY_OF_YEAR, 1);
        }

        ContentResolver resolver = context.getContentResolver();
        long[] startData = queryTwilight(resolver, new String[] { data.startEvent }, today.getTimeInMillis(), data.latitude, data.longitude, data.altitude);
        long[] endData = queryTwilight(resolver, new String[] { data.endEvent }, other.getTimeInMillis(), data.latitude, data.longitude, data.altitude);

        data.startTime = startData[0];
        data.endTime = endData[0];
        //Log.d("DEBUG", "startTime: " + data.startTime + " .. endTime: " + data.endTime);
        data.midpoints = (data.startTime > 0 && data.endTime > 0) ? findMidpoints(data.startTime, data.endTime, data.divideBy) : null;
        return true;
    }

    public static long[] findMidpoints(long startTime, long endTime, int divideBy)
    {
        if (divideBy < 2 || divideBy > 4) {
            throw new IllegalArgumentException("divideBy must be between [2, 4]");
        }

        long span = endTime - startTime;
        long chunk = span / divideBy;

        long[] result = new long[divideBy - 1];
        for (int i=0; i<divideBy - 1; i++) {
            result[i] = startTime + ((i + 1) * chunk);
        }
        return result;
    }

    public long[] queryTwilight(ContentResolver resolver, String[] projection, long date, double latitude, double longitude, double altitude)
    {
        long[] retValue = new long[projection.length];
        Arrays.fill(retValue, -1);

        Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_SUN + "/" + date );
        String selection = CalculatorProviderContract.COLUMN_CONFIG_LATITUDE + "=? AND "
                + CalculatorProviderContract.COLUMN_CONFIG_LONGITUDE + "=? AND "
                + CalculatorProviderContract.COLUMN_CONFIG_ALTITUDE + "=?";
        String[] selectionArgs = new String[] { Double.toString(latitude), Double.toString(longitude), Double.toString(altitude) };
        //Log.d("DEBUG", "Selection Args:" + selectionArgs[0] + ", " + selectionArgs[1] + " " + selectionArgs[2]);

        Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, null);
        if (cursor != null)
        {
            cursor.moveToFirst();
            for (int i=0; i<projection.length; i++) {
                retValue[i] = cursor.isNull(i) ? -1 : cursor.getLong(i);
            }
            cursor.close();
        }
        return retValue;
    }

}