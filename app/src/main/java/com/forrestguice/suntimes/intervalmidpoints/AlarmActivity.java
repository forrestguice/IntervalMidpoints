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

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;

import com.forrestguice.suntimes.alarm.AlarmHelper;
import com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProvider;
import com.forrestguice.suntimes.intervalmidpoints.data.IntervalMidpointsProviderContract;

/**
 * AlarmPicker version of the MainActivity; select and return an alarm
 */
public class AlarmActivity extends MainActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setResult(Activity.RESULT_CANCELED, new Intent());

        Intent intent = getIntent();
        if (intent.hasExtra(IntervalMidpointsProviderContract.EXTRA_ALARM_EVENT))
        {
            String alarmID = intent.getStringExtra(IntervalMidpointsProviderContract.EXTRA_ALARM_EVENT);
            if (alarmID != null)
            {
                Uri alarmUri = Uri.parse(alarmID);
                String intervalID = alarmUri.getLastPathSegment();
                if (AppSettings.isValidIntervalID(intervalID)) {
                    loadUserInput(intervalID, false);
                }
            }
        }
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
    protected MidpointActionCompat onCreateMidpointActions() {
        return new AlarmActionCompat();
    }

    protected void onDone(String midpointID)
    {
        Intent result = new Intent();    // e.g. content://suntimes.intervalmidpoints.provider/eventInfo/sunrise_sunset_2_0
        result.putExtra(IntervalMidpointsProviderContract.COLUMN_CONFIG_PROVIDER, IntervalMidpointsProvider.getAuthority());
        result.putExtra(IntervalMidpointsProviderContract.COLUMN_EVENT_NAME, midpointID);
        result.putExtra(IntervalMidpointsProviderContract.COLUMN_EVENT_TITLE, IntervalMidpointsProvider.getEventTitle(this, midpointID));
        result.putExtra(IntervalMidpointsProviderContract.COLUMN_EVENT_SUMMARY, IntervalMidpointsProvider.getEventSummary(this, midpointID));
        result.setData(Uri.parse(AlarmHelper.getEventInfoUri(IntervalMidpointsProvider.getAuthority(), midpointID)));
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
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * AlarmActionCompat
     */
    private class AlarmActionCompat extends MidpointActionCompat
    {
        public AlarmActionCompat() {
        }

        @Override
        protected int getMenuResId() {
            return R.menu.menu_addalarm;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode)
        {
            actionMode = null;
            resultsCardAdapter.setSelectedIndex(-1);
        }

        @Override
        public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, MenuItem item)
        {
            if (midpointID != null)
            {
                if (item.getItemId() == R.id.action_select)
                {
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