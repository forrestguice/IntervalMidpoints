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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.forrestguice.suntimes.addon.SuntimesInfo;
import com.forrestguice.suntimes.intervalmidpoints.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * IntervalResultsViewHolder
 */
public class IntervalResultsViewHolder extends RecyclerView.ViewHolder
{
    public View card;
    public TextView text_time;
    public IntervalResultsData data;

    public IntervalResultsViewHolder(@NonNull View itemView, IntervalResultsAdapterOptions options)
    {
        super(itemView);
        card = itemView.findViewById(R.id.card_time);
        text_time = (TextView) itemView.findViewById(R.id.text_time);
    }

    public void onBindViewHolder(@NonNull Context context, int position, IntervalResultsData data, IntervalResultsAdapterOptions options)
    {
        this.data = data;
        TimeZone timezone = options.timezone != null ? options.timezone : TimeZone.getDefault();
        text_time.setText(DisplayStrings.formatTime(context, data.timeMillis, timezone, options.suntimes_options.time_is24));
    }

    /**
     * IntervalResultData
     */
    public static class IntervalResultsData
    {
        public String intervalID;
        public long timeMillis;

        public IntervalResultsData(@NonNull String intervalID, long timeMillis)
        {
            this.intervalID = intervalID;
            this.timeMillis = timeMillis;
        }
    }

    /**
     * AdapterOptions
     */
    public static class IntervalResultsAdapterOptions
    {
        public SuntimesInfo suntimes_info;
        public SuntimesInfo.SuntimesOptions suntimes_options;
        public TimeZone timezone;
        public boolean is24;

        public IntervalResultsAdapterOptions(Context context, SuntimesInfo info, TimeZone tz, boolean is24) {
            this.suntimes_info = info;
            this.suntimes_options = info.getOptions(context);
            this.timezone = tz;
            this.is24 = is24;
        }
    }

    /**
     * RecyclerView.Adapter
     */
    public static class IntervalResultsAdapter extends RecyclerView.Adapter<IntervalResultsViewHolder>
    {
        protected WeakReference<Context> contextRef;
        protected ArrayList<IntervalResultsData> items = new ArrayList<>();

        public IntervalResultsAdapter(Context context, IntervalResultsAdapterOptions options)
        {
            this.contextRef = new WeakReference<>(context);
            this.options = options;
        }

        public IntervalResultsAdapter(Context context, List<IntervalResultsData> data, IntervalResultsAdapterOptions options)
        {
            this.contextRef = new WeakReference<>(context);
            this.options = options;
            items.addAll(data);
        }

        @NonNull
        @Override
        public IntervalResultsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
        {
            LayoutInflater layout = LayoutInflater.from(viewGroup.getContext());
            View view = layout.inflate(R.layout.card_result_item, viewGroup, false);
            return new IntervalResultsViewHolder(view, options);
        }

        @Override
        public void onBindViewHolder(@NonNull IntervalResultsViewHolder holder, int position)
        {
            Context context = contextRef.get();
            if (context != null) {
                holder.onBindViewHolder(context, position, items.get(position), options);
                attachClickListeners(holder, position);
            }
        }

        @Override
        public void onViewRecycled(@NonNull IntervalResultsViewHolder holder) {
            detachClickListeners(holder);
        }

        private IntervalResultsAdapterOptions options;
        public void setCardOptions(IntervalResultsAdapterOptions options) {
            notifyDataSetChanged();
        }
        public IntervalResultsAdapterOptions getOptions() {
            return options;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public IntervalResultsData getData(int position) {
            if (position >= 0 && position<items.size()) {
                return items.get(position);
            } else return null;
        }

        public void setItems(List<IntervalResultsData> data) {
            items.clear();
            items.addAll(data);
            notifyDataSetChanged();
        }

        public void clearItems()
        {
            items.clear();
            notifyDataSetChanged();
        }

        private void attachClickListeners(@NonNull final IntervalResultsViewHolder holder, int position) {
            holder.card.setOnClickListener(onCardClick(holder));
        }

        private void detachClickListeners(@NonNull IntervalResultsViewHolder holder) {
            holder.text_time.setOnClickListener(null);
        }

        public void setCardAdapterListener( @NonNull IntervalResultsAdapterListener listener ) {
            adapterListener = listener;
        }
        private IntervalResultsAdapterListener adapterListener = new IntervalResultsAdapterListener();

        private View.OnClickListener onCardClick(@NonNull final IntervalResultsViewHolder holder) {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapterListener.onCardClick(holder.getAdapterPosition());
                }
            };
        }
        private View.OnLongClickListener onCardLongClick(@NonNull final IntervalResultsViewHolder holder) {
            return new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return adapterListener.onCardLongClick(holder.getAdapterPosition());
                }
            };
        }
    }

    /**
     * AdapterListener
     */
    public static class IntervalResultsAdapterListener
    {
        public void onCardClick(int position) {}
        public boolean onCardLongClick(int position) { return false; }
    }

}