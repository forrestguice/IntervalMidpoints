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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.text.SpannableString;

import com.forrestguice.suntimes.addon.ui.SuntimesUtils;
import com.forrestguice.suntimes.intervalmidpoints.R;

import java.math.RoundingMode;
import java.text.NumberFormat;

public class DisplayStrings
{
    public static SpannableString formatLocation(@NonNull Context context, double latitude, double longitude, double meters, @Nullable Integer places, String units)
    {
        String altitudeTag = "";
        if (meters > 0) {
            SuntimesUtils.TimeDisplayText altitudeDisplay = SuntimesUtils.formatAsHeight(context, meters, SuntimesUtils.LengthUnit.valueOf(units), 0, true);
            String altitude = context.getString(R.string.format_location_altitude, altitudeDisplay.getValue(), altitudeDisplay.getUnits());
            altitudeTag = context.getString(R.string.format_tag, altitude);
        }
        formatter.setRoundingMode(RoundingMode.FLOOR);
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(places != null ? places : 4);
        String displayString = context.getString((meters > 0 ? R.string.format_location_long : R.string.format_location), formatter.format(latitude), formatter.format(longitude), altitudeTag);
        return SuntimesUtils.createRelativeSpan(null, displayString, altitudeTag, 0.5f);
    }
    private static NumberFormat formatter = NumberFormat.getInstance();

}
