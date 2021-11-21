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

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimes.addon.AddonHelper;
import com.forrestguice.suntimes.addon.LocaleHelper;
import com.forrestguice.suntimes.addon.SuntimesInfo;
import com.forrestguice.suntimes.addon.ui.Messages;
import com.forrestguice.suntimes.calculator.core.CalculatorProviderContract;
import com.forrestguice.suntimes.intervalmidpoints.ui.AboutDialog;
import com.forrestguice.suntimes.intervalmidpoints.ui.DisplayStrings;
import com.forrestguice.suntimes.intervalmidpoints.ui.HelpDialog;
import com.forrestguice.suntimes.intervalmidpoints.ui.IntervalResultsViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity
{
    public static final String DIALOG_HELP = "helpDialog";
    public static final String DIALOG_ABOUT = "aboutDialog";

    public static final String EXTRA_LOCATION_LABEL = "location_label";
    public static final String EXTRA_LOCATION_LAT = "latitude";
    public static final String EXTRA_LOCATION_LON = "longitude";
    public static final String EXTRA_LOCATION_ALT = "altitude";

    protected String param_location;
    protected double param_latitude = 0, param_longitude = 0, param_altitude = 0;
    protected SuntimesInfo suntimesInfo = null;

    @Override
    protected void attachBaseContext(Context context)
    {
        suntimesInfo = SuntimesInfo.queryInfo(context);    // obtain Suntimes version info
        super.attachBaseContext( (suntimesInfo != null && suntimesInfo.appLocale != null) ?    // override the locale
                LocaleHelper.loadLocale(context, suntimesInfo.appLocale) : context );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (suntimesInfo.appTheme != null) {    // override the theme
            setTheme(getThemeResID(suntimesInfo.appTheme));
        }
        setContentView(R.layout.activity_main);

        initToolBar();
        suntimesInfo.getOptions(this);

        initViews();
        loadUserInput();
    }

    protected void initLocation(Intent intent)
    {
        intent.setExtrasClassLoader(getClassLoader());
        if (intent.hasExtra(EXTRA_LOCATION_LAT) && intent.hasExtra(EXTRA_LOCATION_LON))
        {
            double latitude = intent.getDoubleExtra(EXTRA_LOCATION_LAT, -1000);
            double longitude = intent.getDoubleExtra(EXTRA_LOCATION_LON, -1000);
            if ((latitude >= -90 && latitude <= 90) && (longitude >= -180 && longitude <= 180))
            {
                String labelString = intent.getStringExtra(EXTRA_LOCATION_LABEL);
                param_location = labelString != null ? labelString : "";
                param_latitude = latitude;
                param_longitude = longitude;
                param_altitude = intent.getDoubleExtra(EXTRA_LOCATION_ALT, 0);

            } else {
                initLocation(suntimesInfo);
            }
        } else {
            initLocation(suntimesInfo);
        }
    }

    protected void initLocation(@NonNull SuntimesInfo info)
    {
        param_location = info.location[0];
        param_latitude = Double.parseDouble(info.location[1]);
        param_longitude = Double.parseDouble(info.location[2]);
        param_altitude = Double.parseDouble(info.location[3]);
    }

    protected void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_suntimes);
        }
    }

    protected CharSequence createTitle(SuntimesInfo info) {
        return (suntimesInfo != null && suntimesInfo.location != null && suntimesInfo.location.length >= 4)
                ? suntimesInfo.location[0]
                : getString(R.string.app_name);
    }

    protected TextView text_date;
    protected Spinner spin_startEvent , spin_endEvent, spin_divideBy;
    protected TextView text_startEvent, text_endEvent, text_midpoints;

    protected String startEvent, endEvent;
    protected long startTime = -1L, endTime = -1L;
    protected long date = 1L;
    protected long[] midpoints = null;

    protected RecyclerView resultsCardView;
    protected LinearLayoutManager resultsCardLayout;
    protected IntervalResultsViewHolder.IntervalResultsAdapter resultsCardAdapter;

    protected void initViews()
    {
        text_date = (TextView)findViewById(R.id.text_date);

        spin_startEvent = (Spinner)findViewById(R.id.spin_startevent);
        if (spin_startEvent != null) {
            spin_startEvent.setOnItemSelectedListener(onInputChanged);
        }
        text_startEvent = (TextView)findViewById(R.id.text_startevent);

        View layout_startEvent = findViewById(R.id.layout_startevent);
        if (layout_startEvent != null) {
            layout_startEvent.setOnClickListener(getClickListener(spin_startEvent));
        }

        spin_endEvent = (Spinner)findViewById(R.id.spin_endevent);
        if (spin_endEvent != null) {
            spin_endEvent.setOnItemSelectedListener(onInputChanged);
        }
        text_endEvent = (TextView)findViewById(R.id.text_endevent);

        View layout_endEvent = findViewById(R.id.layout_endevent);
        if (layout_endEvent != null) {
            layout_endEvent.setOnClickListener(getClickListener(spin_endEvent));
        }

        spin_divideBy = (Spinner)findViewById(R.id.spin_divideby);
        if (spin_divideBy != null) {
            spin_divideBy.setOnItemSelectedListener(onInputChanged);
        }
        text_midpoints = (TextView)findViewById(R.id.text_midpoints);

        //View layout_divideBy = findViewById(R.id.layout_divideby);
        //if (layout_divideBy != null) {
        //    layout_divideBy.setOnClickListener(getClickListener(spin_divideBy));
        //}

        resultsCardView = (RecyclerView) findViewById(R.id.resultsView);
        resultsCardView.setHasFixedSize(true);
        resultsCardView.setLayoutManager(resultsCardLayout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        //resultsView.addItemDecoration(cardDecoration);

        resultsCardAdapter = new IntervalResultsViewHolder.IntervalResultsAdapter(this, getResultCardOptions(this));
        resultsCardAdapter.setCardAdapterListener(new IntervalResultsViewHolder.IntervalResultsAdapterListener()
        {
            @Override
            public void onCardClick(int position)
            {
                Log.d("DEBUG", "click " + position);
                onResultClicked(resultsCardAdapter.getData(position));
            }
        });
        resultsCardView.setAdapter(resultsCardAdapter);
    }

    protected void onResultClicked(IntervalResultsViewHolder.IntervalResultsData data)
    {
        /* EMPTY */
    }

    private IntervalResultsViewHolder.IntervalResultsAdapterOptions getResultCardOptions(Context context) {
        return new IntervalResultsViewHolder.IntervalResultsAdapterOptions(context, suntimesInfo, TimeZone.getTimeZone(suntimesInfo.timezone), suntimesInfo.getOptions(context).time_is24);
    }

    private View.OnClickListener getClickListener(final Spinner spinner) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (spinner != null) {
                    spinner.performClick();
                }
            }
        };
    }

    protected void saveUserInput()
    {
        int[] divideByValues = getResources().getIntArray(R.array.divideby_values);
        String[] events = getResources().getStringArray(R.array.event_values);
        int startPosition = spin_startEvent.getSelectedItemPosition();
        int endPosition = spin_endEvent.getSelectedItemPosition();
        AppSettings.saveIntervalIDPref(this, AppSettings.getMidpointID(
                events[startPosition], events[endPosition], divideByValues[spin_divideBy.getSelectedItemPosition()], 0));
    }

    protected void loadUserInput()
    {
        String[] events = getResources().getStringArray(R.array.event_values);
        String[] interval = AppSettings.getInterval(AppSettings.loadIntervalIDPref(this));

        startEvent = (interval.length >= 1 ? interval[0] : events[0]);
        endEvent = (interval.length >= 2 ? interval[1] : events[0]);
        for (int i=0; i<events.length; i++)
        {
            if (events[i].equals(startEvent)) {
                spin_startEvent.setSelection(i, false);
            }
            if (events[i].equals(endEvent)) {
                spin_endEvent.setSelection(i, false);
            }
        }

        int[] divideBy = AppSettings.getMidpointIndex(interval);
        int[] divideByValues = getResources().getIntArray(R.array.divideby_values);
        for (int i=0; i<divideByValues.length; i++)
        {
            if (divideBy[0] == divideByValues[i])
            {
                spin_divideBy.setSelection(i, false);
                break;
            }
        }
    }

    private AdapterView.OnItemSelectedListener onInputChanged = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.d("DEBUG", "input changed");
            saveUserInput();
            updateViews();
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    protected void updateViews()
    {
        checkVersion();    // check dependencies and display warnings

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setTitle(param_location != null ? param_location : createTitle(suntimesInfo));
            actionBar.setSubtitle(DisplayStrings.formatLocation(this, param_latitude, param_longitude, param_altitude, 4, suntimesInfo.getOptions(this).length_units));
        }

        TextView timezoneText = (TextView) findViewById(R.id.bottombar_button0);
        if (timezoneText != null) {
            timezoneText.setText(getTimeZone().getID());
        }

        int[] divideByValues = getResources().getIntArray(R.array.divideby_values);
        int divideBy = divideByValues[spin_divideBy.getSelectedItemPosition()];
        initData(divideBy);   // query provider for start/end times

        text_date.setText(DisplayStrings.formatDate(this, date));
        text_startEvent.setText(startTime >= 0 ? getString(R.string.event_from, formatTime(startTime)) : getString(R.string.event_dne));
        text_endEvent.setText(endTime >= 0 ? getString(R.string.event_to, formatTime(endTime)) : getString(R.string.event_dne));

        resultsCardAdapter.clearItems();
        if (startTime > 0 && endTime > 0)
        {
            ArrayList<IntervalResultsViewHolder.IntervalResultsData> data = new ArrayList<>();
            for (int i=0; i<midpoints.length; i++) {
                data.add(new IntervalResultsViewHolder.IntervalResultsData(AppSettings.getMidpointID(startEvent, endEvent, divideBy, i), midpoints[i]));
            }
            resultsCardAdapter.setItems(data);

            StringBuilder midpointString = new StringBuilder(getString(R.string.midpoints_msg));
            /*for (int i=0; i<midpoints.length; i++)
            {
                midpointString.append("\n");
                midpointString.append(formatTime(midpoints[i]));
            }*/
            text_midpoints.setText(midpointString);

        } else {
            midpoints = null;
            text_midpoints.setText("");
        }
    }

    private CharSequence formatTime(long time)
    {
        SuntimesInfo.SuntimesOptions options = suntimesInfo.getOptions(this);
        TimeZone timezone = getTimeZone();
        return DisplayStrings.formatTime(this, time, timezone, options.time_is24);
    }

    private TimeZone getTimeZone() {
        return suntimesInfo.timezone != null ? TimeZone.getTimeZone(suntimesInfo.timezone) : TimeZone.getDefault();
    }

    private void initData(int divideBy)
    {
        date = Calendar.getInstance().getTimeInMillis();

        int startPosition = spin_startEvent.getSelectedItemPosition();
        int endPosition = spin_endEvent.getSelectedItemPosition();

        String[] events = getResources().getStringArray(R.array.event_values);
        startEvent = events[startPosition];
        endEvent = events[endPosition];

        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(date);

        Calendar other = Calendar.getInstance();
        other.setTimeInMillis(date);

        if (startPosition >= endPosition) {
            other.add(Calendar.DAY_OF_YEAR, 1);
        }

        ContentResolver resolver = getContentResolver();
        long[] startData = queryTwilight(resolver, today.getTimeInMillis(), new String[] { startEvent });
        long[] endData = queryTwilight(resolver, other.getTimeInMillis(), new String[] { endEvent });

        startTime = startData[0];
        endTime = endData[0];
        Log.d("DEBUG", "startTime: " + startTime + " .. endTime: " + endTime);
        midpoints = (startTime > 0 && endTime > 0) ? findMidpoints(startTime, endTime, divideBy) : null;
    }

    private static long[] findMidpoints(long startTime, long endTime, int divideBy)
    {
        if (divideBy < 2 || divideBy > 4) {
            throw new IllegalArgumentException("divideBy must be between [2, 4]");
        }

        long span = endTime - startTime;
        long chunk = span / divideBy;

        long[] result = new long[divideBy - 1];
        for (int i=0; i<divideBy - 1; i++) {
            result[i] = startTime + ((i + 1) * chunk);
        }
        return result;
    }

    public long[] queryTwilight(ContentResolver resolver, long date, String[] projection)
    {
        long[] retValue = new long[projection.length];
        Arrays.fill(retValue, -1);

        Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_SUN + "/" + date );
        String selection = CalculatorProviderContract.COLUMN_CONFIG_LATITUDE + "=? AND "
                          + CalculatorProviderContract.COLUMN_CONFIG_LONGITUDE + "=? AND "
                          + CalculatorProviderContract.COLUMN_CONFIG_ALTITUDE + "=?";
        String[] selectionArgs = new String[] { Double.toString(param_latitude), Double.toString(param_longitude), Double.toString(param_altitude) };

        Log.d("DEBUG", "Selection Args:" + selectionArgs[0] + ", " + selectionArgs[1] + " " + selectionArgs[2]);

        Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, null);
        if (cursor != null)
        {
            cursor.moveToFirst();
            for (int i=0; i<projection.length; i++) {
                retValue[i] = cursor.isNull(i) ? -1 : cursor.getLong(i);
            }
            cursor.close();
        }
        return retValue;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        String appTheme = SuntimesInfo.queryAppTheme(getContentResolver());
        if (appTheme != null && !appTheme.equals(suntimesInfo.appTheme)) {
            recreate();
        } else {
            suntimesInfo = SuntimesInfo.queryInfo(MainActivity.this);    // refresh suntimesInfo
            initLocation(getIntent());
            updateViews();
        }
    }

    protected void checkVersion()
    {
        if (!SuntimesInfo.checkVersion(this, suntimesInfo))
        {
            View view = getWindow().getDecorView().findViewById(android.R.id.content);
            if (!suntimesInfo.hasPermission && suntimesInfo.isInstalled)
                Messages.showPermissionDeniedMessage(this, view);
            else Messages.showMissingDependencyMessage(this, view);
        }
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu)
    {
        Messages.forceActionBarIcons(menu);
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.action_help:
                showHelp();
                return true;

            case R.id.action_about:
                showAbout();
                return true;

            case android.R.id.home:
                AddonHelper.startSuntimesActivity(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            default:
                Log.w(getClass().getSimpleName(), "unhandled result: " + requestCode);
                break;
        }
    }

    protected void showHelp()
    {
        HelpDialog dialog = new HelpDialog();
        if (suntimesInfo != null && suntimesInfo.appTheme != null) {
            dialog.setTheme(getThemeResID(suntimesInfo.appTheme));
        }

        String[] help = getResources().getStringArray(R.array.help_topics);
        String helpContent = help[0];
        for (int i=1; i<help.length; i++) {
            helpContent = getString(R.string.format_help, helpContent, help[i]);
        }
        dialog.setContent(helpContent + "<br/>");
        dialog.show(getSupportFragmentManager(), DIALOG_HELP);
    }

    protected void showAbout() {
        AboutDialog dialog = MainActivity.createAboutDialog(suntimesInfo);
        dialog.show(getSupportFragmentManager(), DIALOG_ABOUT);
    }
    public static AboutDialog createAboutDialog(@Nullable SuntimesInfo suntimesInfo)
    {
        AboutDialog dialog = new AboutDialog();
        if (suntimesInfo != null) {
            dialog.setVersion(suntimesInfo);
            if (suntimesInfo.appTheme != null) {
                dialog.setTheme(getThemeResID(suntimesInfo.appTheme));
            }
        }
        return dialog;
    }

    public static int getThemeResID(@NonNull String themeName) {
        return themeName.equals(SuntimesInfo.THEME_LIGHT) ? R.style.AppTheme_Light : R.style.AppTheme_Dark;
    }

}