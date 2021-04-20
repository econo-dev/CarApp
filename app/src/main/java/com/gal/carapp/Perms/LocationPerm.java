package com.gal.carapp.Perms;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class LocationPerm {
    private static final int LOCATION_REQUEST_CODE = 1; // the code to recognize a specific permission request
    private static LocationPerm INSTANCE = null;
    private LocationManager locationManager;
    Context context;

    // other instance variables can be here

//    public LocationPerm() {
//    }

    public static LocationPerm getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LocationPerm();
        }
        return (INSTANCE);
    }

    public boolean checkForPermission(Activity activity, Context context) {

        List<String> permissionList = new ArrayList<>(); // this will hold all the permissions we want to ask for

        // ContextCompat.checkSelfPermission check is we have permission for a given component - will return PERMISSION_GRANTED or PERMISSION_DENIED

        // if the ACCESS_COARSE_LOCATION permission isn't granted then we add it to the list of permissions to ask for
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        // if the ACCESS_FINE_LOCATION permission isn't granted then we add it to the list of permissions to ask for
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!permissionList.isEmpty()) { // if the list isn't empty then we do have some permissions to ask for
            ActivityCompat.requestPermissions(activity, permissionList.toArray(new String[permissionList.size()]), LOCATION_REQUEST_CODE);
            return false;
        } else { // if the list is empty, then we already have all the permission that we need
            return true;
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2_000, 5, this); // we request updates for the location
            // we specify the provider (String), the minimum time (int -milliseconds), minimum distance (int - meters), and the listener for the updates (LocationListener)
        }
    }

}
