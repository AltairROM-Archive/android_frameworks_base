/*
 * Copyright (C) 2022 Altair ROM Project
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

package com.android.internal.util.custom;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings;

public class MonetUtils {

    public static final String KEY_MONET_COLOR_TYPE = "monet_engine_color_type";
    public static final String KEY_MONET_COLOR_OVERRIDE = "monet_engine_color_override";
    public static final String KEY_MONET_COLOR_ACCENT = "monet_engine_color_accent";
    public static final String KEY_MONET_TINT_SURFACE = "monet_engine_tint_surface";
    public static final String KEY_MONET_CHROMA_FACTOR = "monet_engine_chroma_factor";
    public static final String KEY_MONET_ACCURATE_SHADES = "monet_engine_accurate_shades";
    public static final String KEY_MONET_LINEAR_LIGHTNESS = "monet_engine_linear_lightness";
    public static final String KEY_MONET_WHITE_LUMINANCE = "monet_engine_white_luminance_user";

    public static final int COLOR_TYPE_DEFAULT = 0;
    public static final int COLOR_TYPE_CUSTOM = 1;
    public static final int COLOR_TYPE_INTERNAL = 2;

    private Context mContext;

    public MonetUtils(Context context) {
        mContext = context;
    }

    private int getInt(String key, int defaultValue) {
        return Settings.Secure.getInt(mContext.getContentResolver(), key, defaultValue);
    }

    private void putInt(String key, int value) {
        Settings.Secure.putInt(mContext.getContentResolver(), key, value);
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        return Settings.Secure.getInt(mContext.getContentResolver(), key,
                defaultValue ? 1 : 0) != 0;
    }

    private void putBoolean(String key, boolean value) {
        Settings.Secure.putInt(mContext.getContentResolver(), key, value ? 1 : 0);
    }

    public int getColorType() {
        return getInt(KEY_MONET_COLOR_TYPE, COLOR_TYPE_DEFAULT);
    }

    public void setColorType(int colorType) {
        switch (colorType) {
            case COLOR_TYPE_CUSTOM:
            case COLOR_TYPE_INTERNAL:
                putInt(KEY_MONET_COLOR_TYPE, colorType);
                break;
            default:
                putInt(KEY_MONET_COLOR_TYPE, COLOR_TYPE_DEFAULT);
                break;
        }
    }

    public int getOverrideColor() {
        return getInt(KEY_MONET_COLOR_OVERRIDE, -1);
    }

    public void setOverrideColor(int color) {
        putInt(KEY_MONET_COLOR_OVERRIDE, color);
    }

    public int getAccentColor() {
        return getInt(KEY_MONET_COLOR_ACCENT, -1);
    }

    public void setAccentColor(int color) {
        putInt(KEY_MONET_COLOR_ACCENT, color);
    }

    public boolean isSurfaceTintEnabled() {
        return getBoolean(KEY_MONET_TINT_SURFACE, true);
    }

    public void setSurfaceTintEnabled(boolean enable) {
        putBoolean(KEY_MONET_TINT_SURFACE, enable);
    }

    public boolean isAccurateShadesEnabled() {
        return getBoolean(KEY_MONET_ACCURATE_SHADES, true);
    }

    public void setAccurateShadesEnabled(boolean enable) {
        putBoolean(KEY_MONET_ACCURATE_SHADES, enable);
    }

    public boolean isLinearLightnessEnabled() {
        return getBoolean(KEY_MONET_LINEAR_LIGHTNESS, false);
    }

    public void setLinearLightnessEnabled(boolean enable) {
        putBoolean(KEY_MONET_LINEAR_LIGHTNESS, enable);
    }

    public void resetAccentColor() {
        setAccentColor(mContext.getResources().getColor(android.R.color.system_accent1_500));
    }
}
