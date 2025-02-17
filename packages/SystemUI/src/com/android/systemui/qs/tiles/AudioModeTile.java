/*
 * Copyright (C) 2014 The Android Open Source Project
 *           (C) 2021-2022 Altair ROM Project
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

package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.provider.Settings.System;
import android.service.quicksettings.Tile;
import android.view.View;
import android.widget.Switch;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.systemui.R;
import com.android.systemui.R.drawable;
import com.android.systemui.dagger.qualifiers.Background;
import com.android.systemui.dagger.qualifiers.Main;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.SystemSetting;
import com.android.systemui.qs.tileimpl.QSTileImpl;

import javax.inject.Inject;

/** Quick settings tile: Invert colors **/
public class AudioModeTile extends QSTileImpl<BooleanState> {

    private final Icon mIconAudioMono = ResourceIcon.get(drawable.ic_qs_audio_mode_mono);
    private final Icon mIconAudioStereo = ResourceIcon.get(drawable.ic_qs_audio_mode_stereo);
    private final SystemSetting mSetting;

    private boolean mListening;

    @Inject
    public AudioModeTile(QSHost host,
            @Background Looper backgroundLooper,
            @Main Handler mainHandler,
            FalsingManager falsingManager,
            MetricsLogger metricsLogger,
            StatusBarStateController statusBarStateController,
            ActivityStarter activityStarter,
            QSLogger qsLogger) {
        super(host, backgroundLooper, mainHandler, falsingManager, metricsLogger,
                statusBarStateController, activityStarter, qsLogger);

        mSetting = new SystemSetting(mContext, mHandler, System.MASTER_MONO) {
            @Override
            protected void handleValueChanged(int value, boolean observedChange) {
                handleRefreshState(value);
            }
        };
    }

    @Override
    protected void handleDestroy() {
        super.handleDestroy();
        mSetting.setListening(false);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void handleSetListening(boolean listening) {
        super.handleSetListening(listening);
        mSetting.setListening(listening);
    }

    @Override
    protected void handleUserSwitch(int newUserId) {
        mSetting.setUserId(newUserId);
        handleRefreshState(mSetting.getValue());
    }

    @Override
    public Intent getLongClickIntent() {
        return new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
    }

    @Override
    public void handleClick(View v) {
        mSetting.setValue(mState.value ? 0 : 1);
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_audio_mode);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        final int value = arg instanceof Integer ? (Integer) arg : mSetting.getValue();
        final boolean enabled = value != 0;
        if (state.slash == null) {
            state.slash = new SlashState();
        }
        state.value = enabled;
        state.slash.isSlashed = !state.value;
        state.state = Tile.STATE_ACTIVE;
        state.label = mContext.getString(R.string.quick_settings_audio_mode);
        if (enabled) {
            state.icon = mIconAudioMono;
            state.secondaryLabel = mContext.getString(R.string.quick_settings_audio_mode_mono);
        } else {
            state.icon = mIconAudioStereo;
            state.secondaryLabel = mContext.getString(R.string.quick_settings_audio_mode_stereo);
        }
        state.contentDescription = state.label;
        state.expandedAccessibilityClassName = Switch.class.getName();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.ALTAIR;
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(
                    R.string.accessibility_quick_settings_audio_mode_changed_mono);
        } else {
            return mContext.getString(
                    R.string.accessibility_quick_settings_audio_mode_changed_stereo);
        }
    }
}
