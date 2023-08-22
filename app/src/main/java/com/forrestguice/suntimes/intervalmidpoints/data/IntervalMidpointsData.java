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

import androidx.annotation.Nullable;

import com.forrestguice.suntimes.intervalmidpoints.AppSettings;

import java.util.Calendar;

public class IntervalMidpointsData
{
    protected int index;
    protected long[] midpoints = null;

    protected double longitude, latitude, altitude;
    protected int divideBy = 2;
    protected String startEvent, endEvent;
    protected long startTime = -1L, endTime = -1L;
    protected long date = -1L;

    public void setDate(long date) {
        this.date = date;
    }
    public long getDate() {
        return date;
    }

    public void setDivideBy(int divideBy) {
        this.divideBy = divideBy;
    }
    public int getDivideBy() {
        return divideBy;
    }

    @Nullable
    public Calendar getMidpoint(int i)
    {
        if (i < 0 || i >= midpoints.length || midpoints[i] == -1L) {
            return null;

        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(midpoints[i]);
            return calendar;
        }
    }
    public long[] getMidpoints() {
        return midpoints;
    }

    public String getStartEvent() {
        return startEvent;
    }
    public long getStartTime() {
        return startTime;
    }

    public String getEndEvent() {
        return endEvent;
    }
    public long getEndTime() {
        return endTime;
    }

    public long getLength() {
        return endTime - startTime;
    }

    public long getPeriodMillis() {
        return getLength() / divideBy;
    }

    public IntervalMidpointsData(String intervalID, double latitude, double longitude, double altitude)
    {
        initFromInterval(AppSettings.getInterval(intervalID));
        this.date = Calendar.getInstance().getTimeInMillis();
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public IntervalMidpointsData(String[] interval, double latitude, double longitude, double altitude)
    {
        initFromInterval(interval);
        this.date = Calendar.getInstance().getTimeInMillis();
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public void initFromInterval(String[] interval)
    {
        this.startEvent = interval[0];
        this.endEvent = interval[1];
        this.divideBy = Integer.parseInt(interval[2]);
        this.index = Integer.parseInt(interval[3]);
    }
}