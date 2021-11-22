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

import com.forrestguice.suntimes.alarm.AlarmHelper;

public interface IntervalMidpointsProviderContract
{
    String AUTHORITY = "suntimes.intervalmidpoints.provider";
    String READ_PERMISSION = "suntimes.permission.READ_CALCULATOR";
    String VERSION_NAME = "v0.0.0";
    int VERSION_CODE = 0;

    /*
     * CONFIG
     */
    String COLUMN_CONFIG_PROVIDER = "provider";                             // String (provider reference)
    String COLUMN_CONFIG_PROVIDER_VERSION = "provider_version";             // String (provider version string)
    String COLUMN_CONFIG_PROVIDER_VERSION_CODE = "provider_version_code";   // int (provider version code)
    String COLUMN_CONFIG_APP_VERSION = "app_version";                       // String (app version string)
    String COLUMN_CONFIG_APP_VERSION_CODE = "app_version_code";             // int (app version code)

    String QUERY_CONFIG = "config";
    String[] QUERY_CONFIG_PROJECTION = new String[] {
            COLUMN_CONFIG_PROVIDER_VERSION, COLUMN_CONFIG_PROVIDER_VERSION_CODE,
            COLUMN_CONFIG_APP_VERSION, COLUMN_CONFIG_APP_VERSION_CODE
    };

    /**
     * ALARMS
     */
    String COLUMN_ALARM_NAME = AlarmHelper.COLUMN_ALARM_NAME;              // String (alarm/event ID)
    String COLUMN_ALARM_TITLE = AlarmHelper.COLUMN_ALARM_TITLE;            // String (display string)
    String COLUMN_ALARM_SUMMARY = AlarmHelper.COLUMN_ALARM_SUMMARY;        // String (extended display string)
    String COLUMN_ALARM_TIMEMILLIS = AlarmHelper.COLUMN_ALARM_TIMEMILLIS;  // long (timestamp millis)

    String QUERY_ALARM_INFO = AlarmHelper.QUERY_ALARM_INFO;
    String[] QUERY_ALARM_INFO_PROJECTION = AlarmHelper.QUERY_ALARM_INFO_PROJECTION;

    String QUERY_ALARM_CALC = AlarmHelper.QUERY_ALARM_CALC;
    String[] QUERY_ALARM_CALC_PROJECTION = AlarmHelper.QUERY_ALARM_CALC_PROJECTION;

    String EXTRA_ALARM_NOW = AlarmHelper.EXTRA_ALARM_NOW;                  // long (millis)
    String EXTRA_ALARM_REPEAT = AlarmHelper.EXTRA_ALARM_REPEAT;            // boolean
    String EXTRA_ALARM_REPEAT_DAYS = AlarmHelper.EXTRA_ALARM_REPEAT_DAYS;  // boolean[] .. [m,t,w,t,f,s,s]
    String EXTRA_ALARM_OFFSET = AlarmHelper.EXTRA_ALARM_OFFSET;            // long (millis)
    String EXTRA_ALARM_EVENT = AlarmHelper.EXTRA_ALARM_EVENT;              // eventID

    String EXTRA_LOCATION_LABEL = AlarmHelper.EXTRA_LOCATION_LABEL;        // String
    String EXTRA_LOCATION_LAT = AlarmHelper.EXTRA_LOCATION_LAT;            // double (DD)
    String EXTRA_LOCATION_LON = AlarmHelper.EXTRA_LOCATION_LON;            // double (DD)
    String EXTRA_LOCATION_ALT = AlarmHelper.EXTRA_LOCATION_ALT;            // double (meters)
}
