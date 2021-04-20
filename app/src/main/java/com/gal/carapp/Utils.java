
/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gal.carapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import androidx.core.content.SharedPreferencesCompat;

import com.google.android.gms.common.util.SharedPreferencesUtils;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

class Utils {
    protected static SharedPreferences sp;
    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }

    static void storeGpsPath(Context context, ArrayList<LatLng> pathLocations, int index) {
        String latValue = String.valueOf(pathLocations.get(index).latitude);
        sp = context.getSharedPreferences("latLon", Context.MODE_PRIVATE);

        sp
                .edit()
                .putString( latValue, String.valueOf(pathLocations.get(index).longitude))
                .apply();
    }

    static ArrayList<LatLng> getGpsPath(Context context) {
        ArrayList<LatLng> pathLocations = new ArrayList<>();
        sp = context.getSharedPreferences("latLon", Context.MODE_PRIVATE);
        Map<String, ?> latKeys = sp.getAll();
        for ( Map.Entry<String, ?> entry : latKeys.entrySet()) {
            Double lon = Double.parseDouble((String)entry.getValue());
            LatLng latLng = new LatLng(Double.parseDouble(entry.getKey()), lon);
            pathLocations.add(latLng);
        }
        return pathLocations;
    }

    static void eraseSharedPrefs(Context context) {
        sp = context.getSharedPreferences("latLon", Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }
}