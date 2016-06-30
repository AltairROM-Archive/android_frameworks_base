package com.android.systemui.statusbar.phone;

import com.android.systemui.FontSizeUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Color;
import android.os.Handler;
import android.os.UserHandle;
import android.view.View;
import com.android.systemui.R;
import com.android.systemui.cm.UserContentObserver;
import com.android.systemui.statusbar.policy.Clock;

import java.util.Timer;
import java.util.TimerTask;

import cyanogenmod.providers.CMSettings;

/**
 * To control your...clock
 */
public class ClockController {

    public static final int STYLE_HIDE_CLOCK    = 0;
    public static final int STYLE_CLOCK_RIGHT   = 1;
    public static final int STYLE_CLOCK_CENTER  = 2;
    public static final int STYLE_CLOCK_LEFT    = 3;

    private final IconMerger mNotificationIcons;
    private final Context mContext;
    private final SettingsObserver mSettingsObserver;
    private Clock mRightClock, mCenterClock, mLeftClock, mActiveClock;

    private int mClockLocation;
    private int mAmPmStyle;
    private int mIconTint = Color.WHITE;
    private boolean mClockShowSeconds = false;
    private int mClockDateDisplay = Clock.CLOCK_DATE_DISPLAY_GONE;
    private int mClockDateStyle = Clock.CLOCK_DATE_STYLE_REGULAR;
    private String mClockDateFormat = "";
    private int mClockFontStyle = Clock.FONT_NORMAL;

    class SettingsObserver extends UserContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        @Override
        protected void observe() {
            super.observe();
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(CMSettings.System.getUriFor(
                    CMSettings.System.STATUS_BAR_AM_PM), false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(CMSettings.System.getUriFor(
                    CMSettings.System.STATUS_BAR_CLOCK), false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(CMSettings.System.getUriFor(
                    CMSettings.System.STATUS_BAR_CLOCK_FONT_STYLE), false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(CMSettings.System.getUriFor(
                    CMSettings.System.STATUS_BAR_CLOCK_SHOW_SECONDS), false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(CMSettings.System.getUriFor(
                    CMSettings.System.STATUS_BAR_CLOCK_DATE_DISPLAY), false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(CMSettings.System.getUriFor(
                    CMSettings.System.STATUS_BAR_CLOCK_DATE_STYLE), false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(CMSettings.System.getUriFor(
                    CMSettings.System.STATUS_BAR_CLOCK_DATE_FORMAT), false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(CMSettings.System.getUriFor(
                    CMSettings.System.STATUS_BAR_CLOCK_USE_CUSTOM_COLOR), false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(CMSettings.System.getUriFor(
                    CMSettings.System.STATUS_BAR_CLOCK_CUSTOM_COLOR), false, this, UserHandle.USER_ALL);
            updateSettings();
        }

        @Override
        protected void unobserve() {
            super.unobserve();
            mContext.getContentResolver().unregisterContentObserver(this);
        }

        @Override
        public void update() {
            updateSettings();
        }
    }

    private final Handler handler = new Handler();
    TimerTask second;

    public ClockController(View statusBar, IconMerger notificationIcons, Handler handler) {
        mRightClock = (Clock) statusBar.findViewById(R.id.clock);
        mCenterClock = (Clock) statusBar.findViewById(R.id.center_clock);
        mLeftClock = (Clock) statusBar.findViewById(R.id.left_clock);
        mNotificationIcons = notificationIcons;
        mContext = statusBar.getContext();

        mActiveClock = mRightClock;
        mSettingsObserver = new SettingsObserver(handler);
        mSettingsObserver.observe();
    }

    private Clock getClockForCurrentLocation() {
        Clock clockForAlignment;
        switch (mClockLocation) {
            case STYLE_CLOCK_CENTER:
                clockForAlignment = mCenterClock;
                break;
            case STYLE_CLOCK_LEFT:
                clockForAlignment = mLeftClock;
                break;
            case STYLE_CLOCK_RIGHT:
            case STYLE_HIDE_CLOCK:
            default:
                clockForAlignment = mRightClock;
                break;
        }
        return clockForAlignment;
    }

    private void updateActiveClock() {
        mActiveClock.setVisibility(View.GONE);
        if (mClockLocation == STYLE_HIDE_CLOCK) {
            return;
        }

        mActiveClock = getClockForCurrentLocation();
        mActiveClock.setVisibility(View.VISIBLE);
        mActiveClock.setAmPmStyle(mAmPmStyle);
        mActiveClock.setShowSeconds(mClockShowSeconds);
        mActiveClock.setDateDisplay(mClockDateDisplay);
        mActiveClock.setDateStyle(mClockDateStyle);
        mActiveClock.setDateFormat(mClockDateFormat);
        mActiveClock.setFontStyle(mClockFontStyle);

        setClockAndDateStatus();
        setTextColor(mIconTint);
        updateFontSize();
    }

    private void updateSettings() {
        ContentResolver resolver = mContext.getContentResolver();
        mAmPmStyle = CMSettings.System.getIntForUser(resolver,
                CMSettings.System.STATUS_BAR_AM_PM, Clock.AM_PM_STYLE_GONE,
                UserHandle.USER_CURRENT);
        mClockLocation = CMSettings.System.getIntForUser(
                resolver, CMSettings.System.STATUS_BAR_CLOCK, STYLE_CLOCK_RIGHT,
                UserHandle.USER_CURRENT);
        mClockShowSeconds = (CMSettings.System.getIntForUser(resolver,
                CMSettings.System.STATUS_BAR_CLOCK_SHOW_SECONDS, 0,
                UserHandle.USER_CURRENT) != 0);
        mClockDateDisplay = CMSettings.System.getIntForUser(resolver,
                CMSettings.System.STATUS_BAR_CLOCK_DATE_DISPLAY, Clock.CLOCK_DATE_DISPLAY_GONE,
                UserHandle.USER_CURRENT);
        mClockDateStyle = CMSettings.System.getIntForUser(resolver,
                CMSettings.System.STATUS_BAR_CLOCK_DATE_STYLE, Clock.CLOCK_DATE_STYLE_REGULAR,
                UserHandle.USER_CURRENT);
        mClockDateFormat = CMSettings.System.getString(resolver,
                CMSettings.System.STATUS_BAR_CLOCK_DATE_FORMAT);
        mClockFontStyle = CMSettings.System.getIntForUser(resolver,
                CMSettings.System.STATUS_BAR_CLOCK_FONT_STYLE, Clock.FONT_NORMAL,
                UserHandle.USER_CURRENT);

        second = new TimerTask()
        {
            @Override
            public void run()
            {
                Runnable updater = new Runnable()
                {
                    public void run()
                    {
                        updateActiveClock();
                    }
                };
                handler.post(updater);
            }
        };
        Timer timer = new Timer();
        timer.schedule(second, 0, 1001);

        int defaultColor = mContext.getResources().getColor(R.color.status_bar_clock_color);
        int clockColor = defaultColor;
        if (CMSettings.System.getIntForUser(resolver,
                CMSettings.System.STATUS_BAR_CLOCK_USE_CUSTOM_COLOR, 0,
                UserHandle.USER_CURRENT) != 0)
        {
            clockColor = CMSettings.System.getIntForUser(resolver,
                    CMSettings.System.STATUS_BAR_CLOCK_CUSTOM_COLOR, defaultColor,
                    UserHandle.USER_CURRENT);
            if (clockColor == Integer.MIN_VALUE) {
                // flag to reset the color
                clockColor = defaultColor;
            }
        }
        mIconTint = clockColor;

        updateActiveClock();
    }

    private void setClockAndDateStatus() {
        if (mNotificationIcons != null) {
            mNotificationIcons.setClockAndDateStatus(mClockLocation);
        }
    }

    public void setVisibility(boolean visible) {
        if (mActiveClock != null) {
            mActiveClock.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setTextColor(int iconTint) {
        mIconTint = iconTint;
        if (mActiveClock != null) {
            mActiveClock.setColor(iconTint);
        }
    }

    public void updateFontSize() {
        if (mActiveClock != null) {
            FontSizeUtils.updateFontSize(mActiveClock, R.dimen.status_bar_clock_size);
        }
    }

    public void cleanup() {
        mSettingsObserver.unobserve();
    }
}
