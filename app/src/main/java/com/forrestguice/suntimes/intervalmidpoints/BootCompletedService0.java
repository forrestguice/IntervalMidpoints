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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

/**
 * This is a foreground service that runs on BOOT_COMPLETED. It displays a notification while
 * waiting for the Suntimes alarm scheduler to run, keeping the process running (ready to respond).
 * This is done because on some devices the query to the content-provider times-out and alarms are
 * never rescheduled.
 */
public abstract class BootCompletedService0 extends Service
{
    public static String TAG = "BootCompletedService";

    public static String ACTION_MAIN = Intent.ACTION_MAIN;
    public static String ACTION_EXIT = "exit";

    public abstract String getNotificationChannelID();    // { return CHANNEL_ID_MAIN; }
    public abstract int getNotificationID();              // { return 100; }
    public abstract Intent getMainActivityIntent();

    @Override
    public void onCreate()
    {
        super.onCreate();
        ContextCompat.registerReceiver(BootCompletedService0.this, receiver, getIntentFilter(), ContextCompat.RECEIVER_NOT_EXPORTED);
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
        NotificationCompat.Builder notification = createMainNotification(this);

        // api34+...
        // if (Build.VERSION.SDK_INT >= 34) {
        //    ServiceCompat.startForeground(this, NOTIFICATION_MAIN, notification.build(), FOREGROUND_SERVICE_TYPE);   // we are obligated to startForeground within 5s
        //} else

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(getNotificationID(), notification.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE);   // we are obligated to startForeground within 5s
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
                    Intent mainActivity = getMainActivityIntent();
                    mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mainActivity);
                }

                updateNotification(getNotificationID(), createExitNotification(this).build());
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
        public BootCompletedService0 getService() {
            return BootCompletedService0.this;
        }
    }

    /////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////

    /**
     * Notifications
     */
    protected void updateNotification(int id, Notification notification)
    {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }

    protected NotificationCompat.Builder createMainNotification(Context context)
    {
        NotificationCompat.Builder notification = Notifications.createNotificationBuilder(context, createNotificationChannel(context));
        notification.setContentTitle(getNotificationTitle(context))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSilent(true)
                .setContentText(getNotificationMessage(context))
                .setSmallIcon(R.drawable.ic_about)
                .setOngoing(true);
        notification.addAction(R.drawable.ic_done, context.getString(R.string.action_dismiss), getServicePendingIntent(context, ACTION_MAIN));
        return notification;
    }

    protected NotificationCompat.Builder createExitNotification(Context context)
    {
        NotificationCompat.Builder notification = Notifications.createNotificationBuilder(context, createNotificationChannel(context));
        notification.setContentTitle(getNotificationTitle(context))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setProgress(1, 0, true)
                .setSilent(true)
                .setContentText(getNotificationExitMessage(context))
                .setSmallIcon(R.drawable.ic_done)
                .setOngoing(true);
        return notification;
    }

    protected String getNotificationTitle(Context context) {
        return context.getString(R.string.app_name);
    }

    protected String getNotificationMessage(Context context) {
        return context.getString(R.string.notification_message);
    }

    protected String getNotificationExitMessage(Context context) {
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

    protected String getNotificationChannelTitle(Context context) {
        return context.getString(R.string.notificationChannel_main_title);
    }

    protected String getNotificationChannelDescription(Context context) {
        return context.getString(R.string.notificationChannel_main_desc);
    }

    @TargetApi(24)
    protected int getNotificationChannelImportance(Context context) {
        return NotificationManager.IMPORTANCE_DEFAULT;
    }

    @Nullable
    protected String createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            return Notifications.createNotificationChannel(context, getNotificationChannelID(), getNotificationChannelTitle(context), getNotificationChannelDescription(context), getNotificationChannelImportance(context), null, null);
        } else return null;
    }

}