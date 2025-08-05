// SPDX-License-Identifier: GPL-3.0-or-later
/*
    Copyright (C) 2025 Forrest Guice
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver
{
    public static final String TAG = "IntervalMidpoints";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        final String action = intent.getAction();
        Uri data = intent.getData();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onReceive: " + action + ", " + data);
        }

        if (action != null) {
            if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
                onBootCompleted(context, intent);

            } else Log.w(TAG, "onReceive: `" + action + "` is not a recognized action!");
        } else Log.w(TAG, "onReceive: null action!");
    }

    protected void onBootCompleted(Context context, Intent intent)
    {
        if (AppSettings.showNotificationOnBootCompleted(context))
        {
            if (Build.VERSION.SDK_INT >= 26) {
                Log.i(TAG, "onReceive: BOOT COMPLETED; starting foreground service...");
                context.startForegroundService(new Intent(context, BootCompletedService.class));

            } else {
                Log.i(TAG, "onReceive: BOOT COMPLETED; starting background service...");
                context.startService(new Intent(context, BootCompletedService.class));

                /*Log.i(TAG, "onReceive: BOOT COMPLETED; starting activity...");
                // api29+; starting activities from background no longer works because of https://developer.android.com/guide/components/activities/background-starts#exceptions
                context.startActivity(new Intent(context, MainActivity.class));*/
            }
        }
    }
}
