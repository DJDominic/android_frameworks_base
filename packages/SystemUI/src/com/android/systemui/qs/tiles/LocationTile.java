/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (C) 2015 The CyanogenMod Project
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

import com.android.internal.logging.MetricsConstants;
import com.android.internal.logging.MetricsLogger;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.android.systemui.R;
import com.android.systemui.qs.QSDetailItems;
import com.android.systemui.qs.QSDetailItemsList;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.LocationController.LocationSettingsChangeCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cyanogenmod.app.StatusBarPanelCustomTile;

/** Quick settings tile: Location **/
public class LocationTile extends QSTile<QSTile.BooleanState> {

    private static final Intent LOCATION_SETTINGS_INTENT
            = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    public static final Integer[] LOCATION_SETTINGS = new Integer[]{
            Settings.Secure.LOCATION_MODE_BATTERY_SAVING,
            Settings.Secure.LOCATION_MODE_SENSORS_ONLY,
            Settings.Secure.LOCATION_MODE_HIGH_ACCURACY
    };

    private final AnimationIcon mEnable =
            new AnimationIcon(R.drawable.ic_signal_location_enable_animation);
    private final AnimationIcon mDisable =
            new AnimationIcon(R.drawable.ic_signal_location_disable_animation);

    private final LocationController mController;
    private final LocationDetailAdapter mDetailAdapter;
    private final KeyguardMonitor mKeyguard;
    private final Callback mCallback = new Callback();
    private final List<Integer> mLocationList = new ArrayList<Integer>();

    public LocationTile(Host host) {
        super(host);
        mController = host.getLocationController();
        mDetailAdapter = new LocationDetailAdapter();
        mKeyguard = host.getKeyguardMonitor();
    }

    @Override
    public DetailAdapter getDetailAdapter() {
        return mDetailAdapter;
    }

    @Override
    protected BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {
        if (listening) {
            mController.addSettingsChangedCallback(mCallback);
            mKeyguard.addCallback(mCallback);
        } else {
            mController.removeSettingsChangedCallback(mCallback);
            mKeyguard.removeCallback(mCallback);
        }
    }

    @Override
    protected void handleClick() {
	 boolean mQSCSwitch = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QS_COLOR_SWITCH, 0) == 1;
        if(mController.isAdvancedSettingsEnabled()) {
            showDetail(true);
        } else {
            boolean wasEnabled = mController.isLocationEnabled();
            mController.setLocationEnabled(!wasEnabled);
            MetricsLogger.action(mContext, getMetricsCategory(), !wasEnabled);
            refreshState();
        }

  	if (!mQSCSwitch) {
        mEnable.setAllowAnimation(true);
        mDisable.setAllowAnimation(true);
	} else {
	 mEnable.setAllowAnimation(false);
	  mDisable.setAllowAnimation(false);
	}
    }

    @Override
    protected void handleLongClick() {
        mHost.startActivityDismissingKeyguard(LOCATION_SETTINGS_INTENT);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        final int currentState = mController.getLocationCurrentState();
	boolean mQSCSwitch = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QS_COLOR_SWITCH, 0) == 1;

        state.visible = true;
        state.label = mContext.getString(getStateLabelRes(currentState));
	 if (mQSCSwitch) {
        switch (currentState) {
            case Settings.Secure.LOCATION_MODE_OFF:
                state.contentDescription = mContext.getString(
                        R.string.accessibility_quick_settings_location_off);
                state.icon = ResourceIcon.get(R.drawable.ic_qs_location_off);
                break;
            case Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
                state.contentDescription = mContext.getString(
                        R.string.accessibility_quick_settings_location_battery_saving);
                state.icon = ResourceIcon.get(R.drawable.ic_qs_location_battery_saving);
                break;
            case Settings.Secure.LOCATION_MODE_SENSORS_ONLY:
                state.contentDescription = mContext.getString(
                        R.string.accessibility_quick_settings_location_gps_only);
                state.icon = state.icon = ResourceIcon.get(R.drawable.ic_qs_location_on);
                break;
            case Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:
                state.contentDescription = mContext.getString(
                        R.string.accessibility_quick_settings_location_high_accuracy);
                state.icon = ResourceIcon.get(R.drawable.ic_qs_location_on);
                break;
            default:
                state.contentDescription = mContext.getString(
                        R.string.accessibility_quick_settings_location_on);
                state.icon = ResourceIcon.get(R.drawable.ic_qs_location_on);
        	} 
	} else {
	  switch (currentState) {
            case Settings.Secure.LOCATION_MODE_OFF:
                state.contentDescription = mContext.getString(
                        R.string.accessibility_quick_settings_location_off);
                state.icon = mDisable;
                break;
            case Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
                state.contentDescription = mContext.getString(
                        R.string.accessibility_quick_settings_location_battery_saving);
                state.icon = ResourceIcon.get(R.drawable.ic_qs_location_battery_saving);
                break;
            case Settings.Secure.LOCATION_MODE_SENSORS_ONLY:
                state.contentDescription = mContext.getString(
                        R.string.accessibility_quick_settings_location_gps_only);
                state.icon = mEnable;
                break;
            case Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:
                state.contentDescription = mContext.getString(
                        R.string.accessibility_quick_settings_location_high_accuracy);
                state.icon = mEnable;
                break;
            default:
                state.contentDescription = mContext.getString(
                        R.string.accessibility_quick_settings_location_on);
                state.icon = mEnable;
	   }
        }
    }

    private int getStateLabelRes(int currentState) {
        switch (currentState) {
            case Settings.Secure.LOCATION_MODE_OFF:
                return R.string.quick_settings_location_off_label;
            case Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
                return R.string.quick_settings_location_battery_saving_label;
            case Settings.Secure.LOCATION_MODE_SENSORS_ONLY:
                return R.string.quick_settings_location_gps_only_label;
            case Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:
                return R.string.quick_settings_location_high_accuracy_label;
            default:
                return R.string.quick_settings_location_label;
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsLogger.QS_LOCATION;
    }

    @Override
    protected String composeChangeAnnouncement() {
        switch (mController.getLocationCurrentState()) {
            case Settings.Secure.LOCATION_MODE_OFF:
                return mContext.getString(
                        R.string.accessibility_quick_settings_location_changed_off);
            case Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
                return mContext.getString(
                        R.string.accessibility_quick_settings_location_changed_battery_saving);
            case Settings.Secure.LOCATION_MODE_SENSORS_ONLY:
                return mContext.getString(
                        R.string.accessibility_quick_settings_location_changed_gps_only);
            case Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:
                return mContext.getString(
                        R.string.accessibility_quick_settings_location_changed_high_accuracy);
            default:
                return mContext.getString(
                        R.string.accessibility_quick_settings_location_changed_on);
        }
    }

    private final class Callback implements LocationSettingsChangeCallback,
            KeyguardMonitor.Callback {
        @Override
        public void onLocationSettingsChanged(boolean enabled) {
            refreshState();
        }

        @Override
        public void onKeyguardChanged() {
            refreshState();
        }
    };

    private class AdvancedLocationAdapter extends ArrayAdapter<Integer> {
        public AdvancedLocationAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_single_choice, mLocationList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            CheckedTextView label = (CheckedTextView) inflater.inflate(
                    android.R.layout.simple_list_item_single_choice, parent, false);
            label.setText(getStateLabelRes(getItem(position)));
            return label;
        }
    }

    private class LocationDetailAdapter implements DetailAdapter, AdapterView.OnItemClickListener {

        private AdvancedLocationAdapter mAdapter;
        private QSDetailItemsList mDetails;

        @Override
        public int getTitle() {
            return R.string.quick_settings_location_detail_title;
        }

        @Override
        public Boolean getToggleState() {
            boolean state = mController.getLocationCurrentState()
                    != Settings.Secure.LOCATION_MODE_OFF;
            rebuildLocationList(state);
            return state;
        }

        @Override
        public Intent getSettingsIntent() {
            return LOCATION_SETTINGS_INTENT;
        }

        @Override
        public void setToggleState(boolean state) {
            mController.setLocationEnabled(state);
            rebuildLocationList(state);
            fireToggleStateChanged(state);
        }

        @Override
        public int getMetricsCategory() {
            return MetricsLogger.QS_LOCATION;
        }

        @Override
        public View createDetailView(Context context, View convertView, ViewGroup parent) {
            mDetails = QSDetailItemsList.convertOrInflate(context, convertView, parent);
            mDetails.setEmptyState(R.drawable.ic_qs_location_off,
                    R.string.accessibility_quick_settings_location_off);
            mAdapter = new LocationTile.AdvancedLocationAdapter(context);
            mDetails.setAdapter(mAdapter);

            final ListView list = mDetails.getListView();
            list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            list.setOnItemClickListener(this);
            mDetails.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

            return mDetails;
        }

        private void rebuildLocationList(boolean populate) {
            mLocationList.clear();
            if (populate) {
                mLocationList.addAll(Arrays.asList(LOCATION_SETTINGS));
                mDetails.getListView().setItemChecked(mAdapter.getPosition(
                        mController.getLocationCurrentState()), true);
            }
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mController.setLocationMode((Integer) parent.getItemAtPosition(position));
        }
    }
}
