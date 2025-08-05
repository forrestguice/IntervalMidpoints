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
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

/**
 * This is a foreground service that runs on BOOT_COMPLETED. It displays a notification while
 * waiting for the Suntimes alarm scheduler to run, keeping the process running (ready to respond).
 * This is done because on some devices the query to the content-provider times-out and alarms are
 * never rescheduled.
 */
public class BootCompletedService extends Service
{
    public static String TAG = "IntervalMidpoints";

    public static String ACTION_MAIN = Intent.ACTION_MAIN;
    public static String ACTION_EXIT = "exit";

    @Override
    public void onCreate()
    {
        super.onCreate();
        ContextCompat.registerReceiver(BootCompletedService.this, receiver, getIntentFilter(), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    /**
     * onStart
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        NotificationCompat.Builder notification = createMainNotification(this, getNotificationMessage(this));

        // api34+...
        // if (Build.VERSION.SDK_INT >= 34) {
        //    ServiceCompat.startForeground(this, NOTIFICATION_MAIN, notification.build(), FOREGROUND_SERVICE_TYPE);   // we are obligated to startForeground within 5s
        //} else

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIFICATION_MAIN, notification.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE);   // we are obligated to startForeground within 5s
        }

        handleAction(((intent != null) ? intent.getAction() : null));
        return START_NOT_STICKY;
    }

    protected void handleAction(String action)
    {
        if (action != null)
        {
            if (action.equals(ACTION_MAIN) || (action.equals(ACTION_EXIT)))
            {
                Log.d(TAG, "onStartCommand: " + action);
                if (action.equals(ACTION_MAIN))
                {
                    Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mainActivity);
                }

                updateNotification(NOTIFICATION_MAIN, createExitNotification(this, getExitNotificationMessage(this)).build());
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable()
                {
                    @Override
                    public void run() {
                        stopForeground(true);
                        stopSelf();
                    }
                }, 500);

            } else {
                Log.w(TAG, "onStartCommand: unrecognized action: " + action);
            }
        } else {
            Log.w(TAG, "onStartCommand: null action");
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: " + intent);
            onStartCommand(intent, 0, -1);
        }
    };
    protected static IntentFilter getIntentFilter()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_MAIN);
        filter.addAction(ACTION_EXIT);
        return filter;
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

    /////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////

    /**
     * Notifications
     */
    public static final int NOTIFICATION_MAIN = -10;

    protected void updateNotification(int id, Notification notification)
    {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }

    private static NotificationCompat.Builder createMainNotification(Context context, String message)
    {
        NotificationCompat.Builder notification = createNotificationBuilder(context);
        notification.setContentTitle(context.getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSilent(true)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_about)
                .setOngoing(true);
        notification.addAction(R.drawable.ic_done, context.getString(R.string.action_dismiss), getServicePendingIntent(context, ACTION_MAIN));
        return notification;
    }

    private static NotificationCompat.Builder createExitNotification(Context context, String message)
    {
        NotificationCompat.Builder notification = createNotificationBuilder(context);
        notification.setContentTitle(context.getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setProgress(1, 0, true)
                .setSilent(true)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_done)
                .setOngoing(true);
        return notification;
    }

    protected static String getNotificationMessage(Context context) {
        return context.getString(R.string.notification_message);
    }

    protected static String getExitNotificationMessage(Context context) {
        return context.getString(R.string.notification_message1);
    }

    public static Intent getServiceIntent(Context context, String action)
    {
        Intent intent = new Intent(action);
        intent.setPackage(context.getPackageName());
        return intent;
    }

    private static PendingIntent getServicePendingIntent(Context context, String action)
    {
        Intent intent = getServiceIntent(context, action);
        return PendingIntent.getBroadcast(context, 0, intent,  PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static PendingIntent getMainActivityPendingIntent(Context context)
    {
        Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    /**
     * Notification Channels
     */
    public static final String CHANNEL_ID_MAIN = "intervalmidpoints.notification.channel";

    @TargetApi(26)
    protected static String createNotificationChannel(Context context)
    {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
        {
            String channelID = CHANNEL_ID_MAIN;
            String title = context.getString(R.string.notificationChannel_main_title);
            String desc = context.getString(R.string.notificationChannel_main_desc);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(channelID, title, importance);
            channel.setDescription(desc);
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
            return channelID;
        }
        return "";
    }

    public static NotificationCompat.Builder createNotificationBuilder(Context context)
    {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= 26)
        {
            builder = new NotificationCompat.Builder(context, createNotificationChannel(context));
            builder.setOnlyAlertOnce(true);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    public static boolean isChannelMuted(Context context)
    {
        if (Build.VERSION.SDK_INT >= 26)
        {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
            {
                String channelID = createNotificationChannel(context);
                NotificationChannel channel = notificationManager.getNotificationChannel(channelID);
                return (channel.getImportance() == NotificationManager.IMPORTANCE_NONE);
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

}