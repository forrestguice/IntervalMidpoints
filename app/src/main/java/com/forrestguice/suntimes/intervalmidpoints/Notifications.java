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

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.forrestguice.suntimes.annotation.Nullable;

import androidx.core.app.NotificationCompat;

public class Notifications
{
    @TargetApi(26)
    protected static String createNotificationChannel(Context context, String channelID, int titleResID, int descResID, int importance) {
        return createNotificationChannel(context, channelID, context.getString(titleResID), context.getString(descResID), importance, null, null);
    }

    @TargetApi(26)
    protected static String createNotificationChannel(Context context, String channelID, int titleResID, int descResID, int importance, @Nullable Uri soundUri, @Nullable AudioAttributes audioAttribs) {
        return createNotificationChannel(context, channelID, context.getString(titleResID), context.getString(descResID), importance, soundUri, audioAttribs);
    }

    @TargetApi(26)
    protected static String createNotificationChannel(Context context, String channelID, String title, String desc, int importance, @Nullable Uri soundUri, @Nullable AudioAttributes audioAttribs)
    {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
        {
            NotificationChannel channel = new NotificationChannel(channelID, title, importance);
            channel.setDescription(desc);
            channel.setSound(soundUri, audioAttribs);
            notificationManager.createNotificationChannel(channel);
            return channelID;
        }
        return "";
    }

    public static NotificationCompat.Builder createNotificationBuilder(Context context, String channelID)
    {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= 26)
        {
            builder = new NotificationCompat.Builder(context, channelID);
            builder.setOnlyAlertOnce(true);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    public static boolean isChannelMuted(Context context, String channelID)
    {
        if (Build.VERSION.SDK_INT >= 26)
        {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
            {
                NotificationChannel channel = notificationManager.getNotificationChannel(channelID);
                return (channel != null && channel.getImportance() == NotificationManager.IMPORTANCE_NONE);
            }
        }
        return false;
    }

    public static boolean areNotificationsPaused(Context context)
    {
        if (Build.VERSION.SDK_INT >= 29) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            return notificationManager.areNotificationsPaused();
        } else return false;
    }

    /**
     * https://stackoverflow.com/questions/32366649/any-way-to-link-to-the-android-notification-settings-for-my-app
     * @param context
     */
    public static void openNotificationSettings(Context context)
    {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());                           // Android 5-7
            intent.putExtra("app_uid", context.getApplicationInfo().uid);                       // Android 5-7
            intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());    // Android 8+

        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
        }
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e("AppSettings", "Failed to open notification settings! " + e);
            Toast.makeText(context, e.getClass().getSimpleName() + "!", Toast.LENGTH_SHORT).show();
        }
    }

}