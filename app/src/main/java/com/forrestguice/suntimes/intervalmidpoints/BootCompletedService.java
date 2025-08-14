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

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * This is a foreground service that runs on BOOT_COMPLETED. It displays a notification while
 * waiting for the Suntimes alarm scheduler to run, keeping the process running (ready to respond).
 * This is done because on some devices the query to the content-provider times-out and alarms are
 * never rescheduled.
 */
public class BootCompletedService extends BootCompletedService0
{
    public static final String CHANNEL_ID_MAIN = "intervalmidpoints.notification.channel";
    public static final int NOTIFICATION_MAIN = -10;

    public String getNotificationChannelID() {
        return CHANNEL_ID_MAIN;
    }
    @Override
    public int getNotificationID() {
        return NOTIFICATION_MAIN;
    }

    @Override
    public Intent getMainActivityIntent() {
        return new Intent(getApplicationContext(), MainActivity.class);
    }

    @Override
    protected String getNotificationMessage(Context context) {
        return context.getString(R.string.notification_message);
    }

    @Override
    protected String getNotificationExitMessage(Context context) {
        return context.getString(R.string.notification_message1);
    }

    @Override
    protected String getNotificationChannelTitle(Context context) {
        return context.getString(R.string.notificationChannel_main_title);
    }

    @Override
    protected String getNotificationChannelDescription(Context context) {
        return context.getString(R.string.notificationChannel_main_desc);
    }

    /**
     * onBind
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private final BootCompletedServiceBinder binder = new BootCompletedServiceBinder();
    public class BootCompletedServiceBinder extends Binder {
        public BootCompletedService getService() {
            return BootCompletedService.this;
        }
    }

    /**
     * BootCompletedReceiver
     */
    public static class BootCompletedReceiver extends com.forrestguice.suntimes.intervalmidpoints.BootCompletedReceiver
    {
        @Override
        protected Intent getBootCompletedServiceIntent(Context context ) {
            return new Intent(context, BootCompletedService.class);
        }

        @Override
        protected boolean showNotificationOnBootCompleted(Context context) {
            return AppSettings.showNotificationOnBootCompleted(context);
        }
    }
}