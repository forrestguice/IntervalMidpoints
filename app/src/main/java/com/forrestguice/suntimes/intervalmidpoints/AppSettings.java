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

package com.forrestguice.suntimes.intervalmidpoints;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppSettings
{
    public static final String DEF_EVENT_START = "sunset";
    public static final String DEF_EVENT_END = "astrorise";
    public static final int DEF_DIVIDE_BY = 2;

    public static final String KEY_INTERVAL = "interval";
    public static final String DEF_INTERVAL = getMidpointID(DEF_EVENT_START, DEF_EVENT_END, DEF_DIVIDE_BY, 0);

    /**
     * @param intervalID intervalID or midpointID; e.g. sunrise_sunset, sunrise_sunset_3_1, etc
     * @return interval array; 0:startEvent, 1:endEvent, 2: divideBy, 3: index
     */
    public static String[] getInterval(String intervalID)
    {
        String[] retValue = new String[4];
        String[] parts = intervalID.split("_");

        for (int i=0; i<retValue.length; i++) {
            retValue[i] = (i <= parts.length) ? parts[i] : null;
        }
        if (retValue[0] == null) {
            retValue[0] = DEF_EVENT_START;
        }
        if (retValue[1] == null) {
            retValue[1] = DEF_EVENT_END;
        }
        if (retValue[2] == null) {
            retValue[3] = Integer.toString(DEF_DIVIDE_BY);
        }
        if (retValue[3] == null) {
            retValue[3] = "0";
        }
        return retValue;
    }

    /**
     * @param startEvent eventID
     * @param endEvent eventID
     * @return intervalID; e.g. sunrise_sunset
     */
    public static String getIntervalID(String startEvent, String endEvent) {
        return startEvent + "_" + endEvent;
    }

    /**
     * @param startEvent eventID; e.g. sunrise
     * @param endEvent eventID; e.g. sunset
     * @param divideBy divide interval into divideBy chunks; e.g. 2, 3, 4
     * @param i midpoint index; when divideBy is 2 then i is 0; when divideBy is 3 then i may be 0 or 1, etc.
     * @return midpoint identifier; e.g. sunrise_sunset_2_0, sunrise_sunset_3_0, sunrise_sunset_3_1, sunrise_sunset_3_2, sunrise_sunset_4_0, etc.
     */
    public static String getMidpointID(String startEvent, String endEvent, int divideBy, int i) {
        String intervalID = getIntervalID(startEvent, endEvent);
        return intervalID + "_" + divideBy + "_" + i;
    }

    /**
     * @param midpointID midpointID; e.g. sunrise_sunset_3_1
     * @return 0: index, 1: divideBy (where i<n = divideBy-1)
     */
    public static int[] getMidpointIndex(String midpointID) {
        return getMidpointIndex(getInterval(midpointID));
    }

    /**
     * @param interval interval array; @see getInterval()
     * @return @return 0: index, 1: divideBy (where i<n = divideBy-1)
     */
    public static int[] getMidpointIndex(String[] interval)
    {
        int[] retValue = new int[2];
        if (interval.length >= 4)
        {
            retValue[0] = Integer.parseInt(interval[3]);
            retValue[1] = Integer.parseInt(interval[2]);
        }
        return retValue;
    }

    public static void saveIntervalIDPref(Context context, String intervalID) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(KEY_INTERVAL, intervalID);
        prefs.apply();
    }
    public static String loadIntervalIDPref(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(KEY_INTERVAL, DEF_INTERVAL);
    }

}