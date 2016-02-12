/*
 * Copyright (C) 2016 The Altair ROM Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Color;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.CmSystem;
import android.provider.Settings;
import android.provider.Telephony;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Slog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.systemui.R;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

/**
 * This widget displays the current date and time, used in the pull-down
 * notification area.
 */
public class DateTimeHeaderView extends LinearLayout {
    private static final String TAG = "DateTimeHeaderView";

    private TextView mClockView;
    private TextView mDateView;
    private boolean mUpdating = false;
    private SimpleDateFormat mDateFormat;

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ((action.equals("android.intent.action.TIME_TICK")) ||
                (action.equals("android.intent.action.TIMEZONE_CHANGED"))) {
                    updateViews();
            }
        }
    };
    
    public DateTimeHeaderView(Context context) {
        super(context);
        initViews();
    }
    
    public DateTimeHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }
    
    private void initViews() {
        Context context = getContext();
        String[] str = getTimeText();
        int textColor = context.getResources().getColor(R.color.date_time_label_text_color);
        
        LinearLayout.LayoutParams layoutParams = 
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
                                              LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = 8;

        mClockView = new TextView(context);
        mClockView.setTextSize(32.0f);
        mClockView.setTextColor(textColor);
        mClockView.setText(str[0]);
        mClockView.setLayoutParams(layoutParams);

        mDateView = new TextView(context);
        mDateView.setTextColor(textColor);
        mDateView.setTextSize(12.0f);
        mDateView.setText(str[1]);
        mDateView.setLayoutParams(layoutParams);

        addView(mClockView);
        addView(mDateView);
        setGravity(Gravity.CENTER_VERTICAL);
    }
    
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setUpdates(true);
    }
    
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setUpdates(false);
    }
    
    private final void updateViews() {
        String[] str = getTimeText();
        mClockView.setText(str[0]);
        mDateView.setText(str[1]);
        invalidate();
    }
    
    void setUpdates(boolean update) {
        if (update != mUpdating) {
            Context context = getContext();
            mUpdating = update;
            if (update) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.TIME_TICK");
                filter.addAction("android.intent.action.TIMEZONE_CHANGED");
                getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
                updateViews();
                return;
            }
            context.unregisterReceiver(mIntentReceiver);
        }
    }
    
    private String[] getTimeText() {
        Calendar calendar = Calendar.getInstance();
        Context context = getContext();

        Date date = calendar.getTime();
        int min = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (!DateFormat.is24HourFormat(context)) {
            if (hour > 12) hour -= 12;
            if (hour == 0) hour = 12;
        }
        return new String[] {
            hour + ":" + (min > 9 ? Integer.valueOf(min).toString() : "0" + Integer.valueOf(min).toString()),
            DateFormat.format("EEEE", date) + "\n" + DateFormat.getLongDateFormat(context).format(date).toUpperCase()
        };
    }
}

