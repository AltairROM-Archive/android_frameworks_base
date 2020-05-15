package com.android.systemui.custom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;

import com.android.systemui.R;
import com.android.systemui.VendorServices;

import java.util.ArrayList;

import lineageos.providers.LineageSettings;

public class AppPickerServices extends VendorServices {

    private ArrayList<Object> mServices = new ArrayList();

    private void addService(Object obj) {
        if (obj != null) {
            this.mServices.add(obj);
        }
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {

                String packageName = intent.getData().getSchemeSpecificPart();

                String backLongPress = LineageSettings.System.getString(context.getContentResolver(),
                        LineageSettings.System.KEY_BACK_LONG_PRESS_CUSTOM_APP);
                String backDoubleTap = LineageSettings.System.getString(context.getContentResolver(),
                        LineageSettings.System.KEY_BACK_DOUBLE_TAP_CUSTOM_APP);
                String homeLongPress = LineageSettings.System.getString(context.getContentResolver(),
                        LineageSettings.System.KEY_HOME_LONG_PRESS_CUSTOM_APP);
                String homeDoubleTap = LineageSettings.System.getString(context.getContentResolver(),
                        LineageSettings.System.KEY_HOME_DOUBLE_TAP_CUSTOM_APP);
                String appSwitchLongPress = LineageSettings.System.getString(context.getContentResolver(),
                        LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_CUSTOM_APP);
                String appSwitchDoubleTap = LineageSettings.System.getString(context.getContentResolver(),
                        LineageSettings.System.KEY_APP_SWITCH_DOUBLE_TAP_CUSTOM_APP);

                if (packageName.equals(backLongPress)) {
                    resetAction(LineageSettings.System.KEY_BACK_LONG_PRESS_ACTION,
                            LineageSettings.System.KEY_BACK_LONG_PRESS_CUSTOM_APP_FR_NAME);
                }
                if (packageName.equals(backDoubleTap)) {
                    resetAction(LineageSettings.System.KEY_BACK_DOUBLE_TAP_ACTION,
                            LineageSettings.System.KEY_BACK_DOUBLE_TAP_CUSTOM_APP_FR_NAME);
                }
                if (packageName.equals(homeLongPress)) {
                    resetAction(LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION,
                            LineageSettings.System.KEY_HOME_LONG_PRESS_CUSTOM_APP_FR_NAME);
                }
                if (packageName.equals(homeDoubleTap)) {
                    resetAction(LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION,
                            LineageSettings.System.KEY_HOME_DOUBLE_TAP_CUSTOM_APP_FR_NAME);
                }
                if (packageName.equals(appSwitchLongPress)) {
                    resetAction(LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION,
                            LineageSettings.System.KEY_HOME_LONG_PRESS_CUSTOM_APP_FR_NAME);
                }
                if (packageName.equals(appSwitchDoubleTap)) {
                    resetAction(LineageSettings.System.KEY_APP_SWITCH_DOUBLE_TAP_ACTION,
                            LineageSettings.System.KEY_APP_SWITCH_DOUBLE_TAP_CUSTOM_APP_FR_NAME);
                }
            }
        }
    };

    private void resetAction(String action, String name) {
        LineageSettings.System.putInt(mContext.getContentResolver(), action, 0);
        LineageSettings.System.putString(mContext.getContentResolver(), name, "");
    }

    public void start() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        mContext.registerReceiver(mBroadcastReceiver, filter);
    }
}

