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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.forrestguice.suntimes.addon.ui.Messages;
import com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProvider;
import com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract;
import com.forrestguice.suntimes.intervalmidpoints.ui.IntervalResultsViewHolder;

/**
 * AlarmPicker version of the MainActivity; select and return an alarm
 */
public class AlarmActivity extends MainActivity
{
    public static final String ACTION_PICK_ALARM = "suntimes.action.PICK_ALARM";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setResult(Activity.RESULT_CANCELED, new Intent());
    }

    @Override
    protected void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void initViews()
    {
        super.initViews();

        alarmActions = new AlarmActionCompat();
        /*text_midpoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                triggerActionMode(view, AppSettings.loadIntervalIDPref(AlarmActivity.this));   // TODO: user selection
            }
        });*/
    }

    /*@Override
    protected void updateViews() {
        super.updateViews();
    }*/

    /*@Override
    protected void onResume() {
        super.onResume();
    }*/

    @Override
    protected void onResultClicked(IntervalResultsViewHolder.IntervalResultsData data) {
        triggerActionMode(text_midpoints, data.intervalID);
    }

    protected void onDone(String midpointID)
    {
        Intent result = new Intent();    // e.g. content://suntimes.intervalmidpoints.provider/alarmInfo/sunrise_sunset_2_0
        result.putExtra(IntervalMidpointsProviderContract.COLUMN_CONFIG_PROVIDER, IntervalMidpointsProviderContract.AUTHORITY);
        result.putExtra(IntervalMidpointsProviderContract.COLUMN_ALARM_NAME, midpointID);
        result.putExtra(IntervalMidpointsProviderContract.COLUMN_ALARM_TITLE, IntervalMidpointsProvider.getAlarmTitle(this, midpointID));
        result.putExtra(IntervalMidpointsProviderContract.COLUMN_ALARM_SUMMARY, IntervalMidpointsProvider.getAlarmSummary(this, midpointID));
        result.setData(Uri.parse("content://" + IntervalMidpointsProviderContract.AUTHORITY + "/" + IntervalMidpointsProviderContract.QUERY_ALARM_INFO + "/" + midpointID));
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_addalarm0, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected ActionMode actionMode = null;
    protected AlarmActionCompat alarmActions;

    private boolean triggerActionMode(View view, String midpointID)
    {
        if (actionMode == null)
        {
            if (midpointID != null)
            {
                alarmActions.setSelection(midpointID);
                actionMode = startSupportActionMode(alarmActions);
                if (actionMode != null) {
                    actionMode.setTitle(IntervalMidpointsProvider.getAlarmTitle(this, midpointID));  // TODO: show/calculate alarm time
                }
            }
            return true;

        } else {
            actionMode.finish();
            triggerActionMode(view, midpointID);
            return false;
        }
    }

    /**
     * AlarmActionCompat
     */
    private class AlarmActionCompat implements android.support.v7.view.ActionMode.Callback
    {
        public AlarmActionCompat() {
        }

        private String midpointID = null;
        public void setSelection(String midpointID ) {
            this.midpointID = midpointID;
        }

        @Override
        public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu)
        {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_addalarm, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode)
        {
            actionMode = null;
        }

        @Override
        public boolean onPrepareActionMode(android.support.v7.view.ActionMode mode, Menu menu)
        {
            Messages.forceActionBarIcons(menu);
            return false;
        }

        @Override
        public boolean onActionItemClicked(android.support.v7.view.ActionMode mode, MenuItem item)
        {
            if (midpointID != null)
            {
                switch (item.getItemId())
                {
                    case R.id.action_select:
                        onDone(midpointID);
                        mode.finish();
                        return true;
                }
            }
            mode.finish();
            return false;
        }
    }

}