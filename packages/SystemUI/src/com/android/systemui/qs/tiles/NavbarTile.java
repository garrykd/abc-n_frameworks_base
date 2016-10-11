/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (C) 2016 ABC Rom
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

import android.content.ComponentName;
import android.content.Intent;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.widget.Switch;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.SecureSetting;

/** Quick settings tile: Invert colors **/
public class NavbarTile extends QSTile<QSTile.BooleanState> {

    private final SecureSetting mSetting;

    private boolean mListening;

    public NavbarTile(Host host) {
        super(host);

        mSetting = new SecureSetting(mContext, mHandler,
                Secure.NAVIGATION_BAR_VISIBLE) {
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
    public void setListening(boolean listening) {
        mSetting.setListening(listening);
    }

    @Override
    protected void handleUserSwitch(int newUserId) {
        mSetting.setUserId(newUserId);
        handleRefreshState(mSetting.getValue());
    }

    @Override
    public Intent getLongClickIntent() {
        return new Intent().setComponent(new ComponentName(
            "com.android.systemui", "com.android.systemui.tuner.NavBarTunerActivity"));
    }

    @Override
    protected void handleClick() {
        MetricsLogger.action(mContext, getMetricsCategory(), !mState.value);
        mSetting.setValue(mState.value ? 0 : 1);
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_navbar_label);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        final int value = arg instanceof Integer ? (Integer) arg : mSetting.getValue();
        final boolean enabled = value != 0;
        state.value = enabled;
        state.label = enabled? mContext.getString(R.string.quick_settings_navbar_enabled_label)
            : mContext.getString(R.string.quick_settings_navbar_disabled_label);
        state.icon = enabled ? ResourceIcon.get(R.drawable.ic_qs_navbar_on)
            : ResourceIcon.get(R.drawable.ic_qs_navbar_off);
        state.contentDescription = state.label;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.PURE_QS;
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(
                    R.string.quick_settings_navbar_enabled_label);
        } else {
            return mContext.getString(
                    R.string.quick_settings_navbar_disabled_label);
        }
    }
}
