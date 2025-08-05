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

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import android.view.View;

import com.forrestguice.suntimes.ContextCompat;
import com.forrestguice.suntimes.addon.AppThemeInfo;
import com.forrestguice.suntimes.addon.LocaleHelper;
import com.forrestguice.suntimes.addon.SuntimesInfo;
import com.forrestguice.suntimes.addon.ui.SuntimesUtils;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

public class SettingsActivity extends AppCompatActivity
{
    private SuntimesInfo suntimesInfo = null;

    @Override
    protected void attachBaseContext(Context context)
    {
        AppThemeInfo.setFactory(new AppThemes());
        suntimesInfo = SuntimesInfo.queryInfo(context);    // obtain Suntimes version info
        super.attachBaseContext( (suntimesInfo != null && suntimesInfo.appLocale != null) ?    // override the locale
                LocaleHelper.loadLocale(context, suntimesInfo.appLocale) : context );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (suntimesInfo.appTheme != null) {    // override the theme
            AppThemeInfo.setTheme(this, suntimesInfo);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getFragmentManager().beginTransaction().replace(android.R.id.content, new IntervalMidpointsPreferenceFragment(), IntervalMidpointsPreferenceFragment.TAG).commit();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        FragmentManager fragments = getFragmentManager();
        IntervalMidpointsPreferenceFragment fragment = (IntervalMidpointsPreferenceFragment) fragments.findFragmentByTag(IntervalMidpointsPreferenceFragment.TAG);
        if (fragment != null) {
            fragment.setSuntimesInfo(suntimesInfo);
        }
    }

    /**
     * IntervalMidpointsFragment
     */
    public static class IntervalMidpointsPreferenceFragment extends PreferenceFragment
    {
        public static final String TAG = "IntervalMidpointsFragment";

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);
        }

        @Override
        public void onResume()
        {
            super.onResume();
            updatePrefs();
        }

        protected void updatePrefs()
        {
            Context context = getActivity();
            TypedArray typedArray = context.obtainStyledAttributes(R.styleable.WarningView);
            int colorWarning = ContextCompat.getColor(context, typedArray.getResourceId(R.styleable.WarningView_colorWarning, R.color.colorWarning_dark));
            typedArray.recycle();

            Preference bootNotification = findPreference(AppSettings.KEY_AUTO_LAUNCH);
            if (bootNotification != null) {
                bootNotification.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o)
                    {
                        View v = getView();
                        if (v != null) {
                            getView().post(new Runnable() {
                                @Override
                                public void run() {
                                    updatePrefs();
                                }
                            });
                        }
                        return true;
                    }
                });
            }

            Preference batteryOptimization = findPreference(AppSettings.PREF_KEY_ALARM_BATTERYOPT);
            if (batteryOptimization != null)
            {
                if (Build.VERSION.SDK_INT >= 23)
                {
                    batteryOptimization.setOnPreferenceClickListener(onBatteryOptimizationClicked(context));
                    batteryOptimization.setSummary(batteryOptimizationMessage(context));

                } else {
                    PreferenceCategory alarmsCategory = (PreferenceCategory) findPreference(AppSettings.PREF_KEY_ALARM_CATEGORY);
                    removePrefFromCategory(batteryOptimization, alarmsCategory);  // battery optimization is api 23+
                }
            }

            Preference autostartPref = findPreference(AppSettings.PREF_KEY_ALARM_AUTOSTART);
            if (autostartPref != null)
            {
                if (AppSettings.hasAutostartSettings(context))
                {
                    autostartPref.setOnPreferenceClickListener(onAutostartPrefClicked(context));
                    autostartPref.setSummary(autostartMessage(context));

                } else {
                    PreferenceCategory alarmsCategory = (PreferenceCategory)findPreference(AppSettings.PREF_KEY_ALARM_CATEGORY);
                    removePrefFromCategory(autostartPref, alarmsCategory);
                }
            }

            Preference notificationPrefs = findPreference(AppSettings.PREF_KEY_ALARM_NOTIFICATIONS);
            if (notificationPrefs != null)
            {
                notificationPrefs.setOnPreferenceClickListener(onNotificationPrefsClicked(context));

                String warningText = context.getString(R.string.pref_summary_alarms_notifications_off);
                CharSequence warning = (AppSettings.showNotificationOnBootCompleted(context)
                        ? SuntimesUtils.createColorSpan(null, warningText, warningText, colorWarning)
                        : warningText);

                if (NotificationManagerCompat.from(context).areNotificationsEnabled())
                {
                    if (BootCompletedService.areNotificationsPaused(context) || BootCompletedService.isChannelMuted(context)) {
                        notificationPrefs.setSummary(warning);
                    } else {
                        notificationPrefs.setSummary(context.getString(R.string.pref_summary_alarms_notifications_on));
                    }
                } else {
                    notificationPrefs.setSummary(warning);
                }
            }
        }

        public static CharSequence batteryOptimizationMessage(Context context)
        {
            TypedArray typedArray = context.obtainStyledAttributes(R.styleable.WarningView);
            int colorWarning = ContextCompat.getColor(context, typedArray.getResourceId(R.styleable.WarningView_colorWarning, R.color.colorWarning_dark));
            typedArray.recycle();

            if (Build.VERSION.SDK_INT >= 23)
            {
                if (AppSettings.isIgnoringBatteryOptimizations(context)) {
                    return context.getString(R.string.pref_summary_alarms_optWhiteList_listed);

                } else {
                    String unlisted = context.getString(AppSettings.aggressiveBatteryOptimizations(context) ? R.string.pref_summary_alarms_optWhiteList_unlisted_aggressive : R.string.pref_summary_alarms_optWhiteList_unlisted);
                    return SuntimesUtils.createColorSpan(null, unlisted, unlisted, colorWarning);
                }
            } else return "";
        }

        public static CharSequence autostartMessage(Context context)
        {
            if (AppSettings.isAutostartDisabled(context))
            {
                TypedArray typedArray = context.obtainStyledAttributes(R.styleable.WarningView);
                int colorWarning = ContextCompat.getColor(context, typedArray.getResourceId(R.styleable.WarningView_colorWarning, R.color.colorWarning_dark));
                typedArray.recycle();

                String disabledString = context.getString(R.string.pref_summary_alarms_autostart_off);
                String summaryString = context.getString(R.string.pref_summary_alarms_autostart_summary, disabledString);
                return SuntimesUtils.createColorSpan(null, summaryString, disabledString, colorWarning);

            } else {
                return context.getString(R.string.pref_summary_alarms_autostart_summary, context.getString(R.string.pref_summary_alarms_autostart_on));
            }
        }

        private static Preference.OnPreferenceClickListener onBatteryOptimizationClicked(final Context context)
        {
            return new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AppSettings.requestIgnoreBatteryOptimization(context);
                    return false;
                }
            };
        }

        private static Preference.OnPreferenceClickListener onAutostartPrefClicked(final Context context)
        {
            return new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AppSettings.openAutostartSettings(context);
                    return false;
                }
            };
        }

        private static Preference.OnPreferenceClickListener onNotificationPrefsClicked(final Context context)
        {
            return new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    AppSettings.openNotificationSettings(context);
                    return false;
                }
            };
        }

        public static void removePrefFromCategory(Preference pref, PreferenceCategory category) {
            if (pref != null && category != null) {
                category.removePreference(pref);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item)
        {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        protected SuntimesInfo info;
        public void setSuntimesInfo(SuntimesInfo info) {
            this.info = info;
        }
    }

}
