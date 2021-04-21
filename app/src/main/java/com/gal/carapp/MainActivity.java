package com.gal.carapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.google.zxing.*;

import com.gal.carapp.Perms.LocationPerm;
import com.gal.carapp.Users.UserRegistration;
import com.google.zxing.common.HybridBinarizer;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener { // we implement LocationListener in order to get updates on changing location, provers and such
    static GoogleMap mMap;

//    String apik = BuildConfig.API_KEY;
    LocationPerm locationPerm = LocationPerm.getInstance(); //singleton
    private static final int LOCATION_REQUEST_CODE = 1; // the code to recognize a specific permission request
    Context context;

    TextView txtLocData, txtDistance, txtFinalDistance, txtTimer, txtMapDist;
    TextView txtUserName;
    TextView txtPassword;

    Button btnStart;
    Button btnStop;
    Button btnRegister;
    Button btnLogin;

    LocationManager locationManager;
    Location myLocation;
    Location secondLocation;
    Location startPoint;
    static double accumulatedDistance = 0;

    //TODO check these vars. decide if they work instead of original
    static Double lat1 = null;
    static Double lon1 = null;
    static Double lat2 = null;
    static Double lon2 = null;
    static Double distance = 0.0;
    static int status = 0;
    int locationsIndex = 0;

    ArrayList<LatLng> pathLocations = new ArrayList<>();
    public enum markerType {HOME_PIN, CURRENT_LOC_PIN, MAP_PIN}

    Timer t;
    TimerCounter timer;
    int count = 0;
    boolean isTimerRunning = false;

    private Button lock;
    private Button disable;
    private Button enable;
    static final int RESULT_ENABLE = 1;

    DevicePolicyManager deviceManger;
    ActivityManager activityManager;
    ComponentName compName;

    /** based on google's approach **/
    private static final String TAG = MainActivity.class.getSimpleName();

    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // UI elements.
    private Button mRequestLocationUpdatesButton;
    private Button mRemoveLocationUpdatesButton;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };
/*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myReceiver = new MyReceiver();
        setContentView(R.layout.activity_main);

        // Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }
    }
*/
    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        mRequestLocationUpdatesButton = (Button) findViewById(R.id.btnStart);
        mRemoveLocationUpdatesButton = (Button) findViewById(R.id.btnStop);

        mRequestLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    mService.requestLocationUpdates();
                }
            }
        });

        mRemoveLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.removeLocationUpdates();
            }
        });

        // Restore the state of the buttons when the activity (re)launches.
        setButtonsState(Utils.requestingLocationUpdates(this));

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return  PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService.requestLocationUpdates();
            } else {
                // Permission denied.
                setButtonsState(false);
                Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                Toast.makeText(MainActivity.this, Utils.getLocationText(location),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonsState(sharedPreferences.getBoolean(Utils.KEY_REQUESTING_LOCATION_UPDATES,
                    false));
        }
    }

    private void setButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            mRequestLocationUpdatesButton.setEnabled(false);
            mRemoveLocationUpdatesButton.setEnabled(true);
        } else {
            mRequestLocationUpdatesButton.setEnabled(true);
            mRemoveLocationUpdatesButton.setEnabled(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myReceiver = new MyReceiver();
        // Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        lock =(Button)findViewById(R.id.lock);

        disable = (Button)findViewById(R.id.btnDisable);
        enable = (Button)findViewById(R.id.btnEnable);
        setLockManager();

        setPointer();
        checkForPermission();
//        LocationPermissions perms = new LocationPermissions(this,LOCATION_REQUEST_CODE, this);

//        updateLocationData(); // update the ui according to the last known location
        updateValuesFromBundle(savedInstanceState);

        pathLocations = Utils.getGpsPath(this);
    }

    private void setLockManager() {
        deviceManger = (DevicePolicyManager)getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager)getSystemService(
                Context.ACTIVITY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);

        lock.setOnClickListener(this);
        disable.setOnClickListener(this);
        enable.setOnClickListener(this);
    }

    private void checkForPermission() {
        /*  original working location permission
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
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), LOCATION_REQUEST_CODE);
        } else { // if the list is empty, then we already have all the permission that we need
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2_000, 5, this); // we request updates for the location
            // we specify the provider (String), the minimum time (int -milliseconds), minimum distance (int - meters), and the listener for the updates (LocationListener)
        }

         */
        if (locationPerm.checkForPermission(MainActivity.this, context))
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2_000, 10, this);
        else Log.e("perms", "we need permission");

    }

    private void setPointer() {
        context = this;
        txtLocData = findViewById(R.id.txtLocData);
        txtDistance = findViewById(R.id.txtDistance);
        txtFinalDistance = findViewById(R.id.txt_final_dist);
        txtTimer = findViewById(R.id.txt_timer);
        txtMapDist = findViewById(R.id.txt_map_dist);

//        txtUserName = findViewById(R.id.txtUserName);
//        txtPassword = findViewById(R.id.txtPassword);

        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
//        btnLogin = findViewById(R.id.btnLogin);
//        btnRegister = findViewById(R.id.btnRegister);

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE); // getting the location manager (object) from the system services
//        updateLocationData();
//        startPoint=getLocation();
//        myLocation=getLocation();
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startPoint.setLatitude(getLocation().getLatitude());
//                startPoint.setLongitude(getLocation().getLongitude());
                if (getLocation() == null) {
                    Toast.makeText(context, "Make sure GPS is Enabled", Toast.LENGTH_SHORT).show();
                    return;
                }
                getLocation().reset();
                if (!isTimerRunning) {
                    startTimer();
                }
                distance = 0.0;
//                myLocation = getLocation().distanceTo(myLocation) > distance ? : getLocation() ;
//                measureDistance(startPoint,myLocation);
                updateLocationData();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myLocation = null;
                updateLocationData();
                txtFinalDistance.setText(distance.toString());
                distance = 0.0;
                stopTimer();


            }
        });

        /*
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, UserRegistration.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CarsActivity.class);
                startActivity(intent);
            }
        });

         */
    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == LOCATION_REQUEST_CODE) { // this checks the specific permissions explicitly
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }
//            // this is the version of checking the permission with the data we got back from the permission request
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // check if the first one is granted
//                if (grantResults.length > 1) {//check if there is a second permission
//                    if (grantResults[1] == PackageManager.PERMISSION_GRANTED) { // if there is a second permission then make sure that it is granted as well
//                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2_000, 5, this); // we request updates for the location
//                    }
//                } else {
//                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2_000, 5, this); // we request updates for the location
//                }
//            }
//
//        }
//
//    }

    private Location getLocation() { // method to get the location from the gps or network

        // make sure we have permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        // get the last know location according to the gps and network
        Location locationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNET = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        // get the amount of time since the last update for each
        long gpsLocationTime = locationGps != null ? locationGps.getTime() : 0;
        long netLocationTime = locationNET != null ? locationNET.getTime() : 0;

        // return the location what is the most recent
        return gpsLocationTime > netLocationTime ? locationGps : locationNET;

    }


    private void updateLocationData() { // update the information in the TextView
//        /*Location*/ myLocation = getLocation(); // getting the location
        Location myLocation = getLocation();
        if (myLocation == null) {
            return;
        }// if the location we got is null then leave this method

        String locData = "";
        locData += "latitude: " + myLocation.getLatitude() + "\n";
        locData += "longitude: " + myLocation.getLongitude() + "\n";
        locData += "altitude: " + myLocation.getAltitude() + "\n";
        locData += "provider: " + myLocation.getProvider() + "\n";
        locData += "bearing: " + myLocation.getBearing() + "\n";
        locData += "speed: " + myLocation.getSpeed() * 3.6 + " Km\\h\n";  //getSpeed = Meters\sec >> *3.6 convert to Km\h
        locData += "time: " + myLocation.getTime() + "\n";
        locData += "accuracy: " + myLocation.getAccuracy() + "\n";
        txtLocData.setText(locData);

        txtDistance.setText("distance: " + distance + " meters");
        //DISTANCE A->B
//        Location location2 = getLocation();
//        myLocation.distanceTo(location2);

    }


    private double measureDistance(Location locationA, Location locationB) {
        double errorFactor = 0.95;
        distance += locationA.distanceTo(locationB) * errorFactor;

//        txtDistance.setText("distance: "+accumulatedDistance);
        return distance;
    }

    @Override
    public void onLocationChanged(Location location) {
//        Location currentLoc = getLocation();
//        if(myLocation != null){
//            measureDistance(myLocation,location);
//            updateLocationData(); // update the textView
//        }


        if (status == 0) {
            lat1 = location.getLatitude();
            lon1 = location.getLongitude();
        } else if ((status % 2) != 0) {
            lat2 = location.getLatitude();
            lon2 = location.getLongitude();
            distance += distanceBetweenTwoPoint(lat1, lon1, lat2, lon2);
        } else if ((status % 2) == 0) {
            lat1 = location.getLatitude();
            lon1 = location.getLongitude();
            distance += distanceBetweenTwoPoint(lat2, lon2, lat1, lon1);
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());


        pathLocations.add(latLng);
        txtDistance.setText("Distance: " + distance + " meters");
        status++;

        if (mMap != null) {
            mMap.clear();
            drawMarker(latLng, markerType.CURRENT_LOC_PIN);
            drawPath(pathLocations);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
            calculatePathDistance(latLng, pathLocations);
        }
    }

    double distanceBetweenTwoPoint(double srcLat, double srcLng, double desLat, double desLng) {
        double earthRadius = 3958.75;
        double dLat = Math.toRadians(desLat - srcLat);
        double dLng = Math.toRadians(desLng - srcLng);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(srcLat))
                * Math.cos(Math.toRadians(desLat)) * Math.sin(dLng / 2)
                * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;

        double meterConversion = 1609;

        return  (int)(dist * meterConversion);
    }


    private void startTimer() {
        t = new Timer();

        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                isTimerRunning = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtTimer.setText("Time Elapsed: " + count);
                        count++;
                    }
                });
            }
        }, 0, 1_000);

    }

    private void stopTimer() {

        if (t != null) {
            t.cancel();
            isTimerRunning = false;
        }

//        t.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
////                        txtTimer.setText("Time Elapsed: "+count);
//                        t.cancel();
//                    }
//                });
//            }
//        },0,1);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Toast.makeText(context, "status changed to: " + status, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(context, "provider " + provider + " is enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(context, "provider " + provider + " is disabled", Toast.LENGTH_SHORT).show();

    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        getLocation();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        getLocation();
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (t != null){
            t.cancel();
            isTimerRunning = false;
        }
        //todo check if i need to erase sp
        Utils.eraseSharedPrefs(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("TIME_COUNT_KEY", count);
        outState.putDouble("DISTANCE_KEY", distance);
        outState.putBoolean("IS_TIMER_ON_KEY", isTimerRunning);
        outState.putDouble("PATH_DISTANCE_KEY", accumulatedDistance);
        super.onSaveInstanceState(outState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) { return; }

        // Update the values of  from the Bundle.
        if (savedInstanceState.keySet().contains("TIME_COUNT_KEY")) {
            count = savedInstanceState.getInt("TIME_COUNT_KEY");
        }
        if (savedInstanceState.keySet().contains("DISTANCE_KEY")){
            distance = savedInstanceState.getDouble("DISTANCE_KEY");
        }
        if (savedInstanceState.keySet().contains("IS_TIMER_ON_KEY")){
            isTimerRunning = savedInstanceState.getBoolean("IS_TIMER_ON_KEY");
        }
        if (savedInstanceState.keySet().contains("PATH_DISTANCE_KEY")){
            accumulatedDistance = savedInstanceState.getDouble("PATH_DISTANCE_KEY");
        }

        // Update UI to match restored state
        updateUI();
        if (isTimerRunning) {
            startTimer();
        }
    }

    private void updateUI(){
        txtTimer.setText("Time Elapsed: " + count);
        txtFinalDistance.setText(distance + " meters");
        txtMapDist.setText("Distance on Map: "+accumulatedDistance+"m");
    }

    @Override
    public void onClick(View v) {
        if(v == lock){
            boolean active = deviceManger.isAdminActive(compName);
            if (active) {
                deviceManger.lockNow();
            }
        }

        if(v == enable){
            Intent intent = new Intent(DevicePolicyManager
                    .ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    compName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Additional text explaining why this needs to be added.");
            startActivityForResult(intent, RESULT_ENABLE);
        }

        if(v == disable){
            deviceManger.removeActiveAdmin(compName);
            updateButtonStates();
        }
    }

    private void updateButtonStates() {

        boolean active = deviceManger.isAdminActive(compName);
        if (active) {
            enable.setEnabled(false);
            disable.setEnabled(true);
        } else {
            enable.setEnabled(true);
            disable.setEnabled(false);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i("DeviceAdminSample", "Admin enabled!");
                } else {
                    Log.i("DeviceAdminSample", "Admin enable FAILED!");
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*public static String scanQRImage(Bitmap bMap) {
        String contents = null;

        int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Reader reader = new MultiFormatReader();
        try {
            Result result = reader.decode(bitmap);
            contents = result.getText();
        }
        catch (Exception e) {
            Log.e("QrTest", "Error decoding barcode", e);
        }
        return contents;
    } */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void drawMarker(LatLng latLng, markerType type) {
        MarkerOptions markerOps = new MarkerOptions();

        switch (type) {
//            case HOME_PIN:
//                markerOps.icon(BitmapDescriptorFactory.fromResource(R.drawable.home));
//                break;
            case CURRENT_LOC_PIN:
                markerOps.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_pin));
                break;
            case MAP_PIN:
                markerOps.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin));
                break;
            default:
                break;
        }

        mMap.addMarker(markerOps
                .position(latLng)
                .title("Me"))
                .showInfoWindow();
    }

    private void drawPath(ArrayList<LatLng> pathLocations) {
        PolylineOptions rectLine = new PolylineOptions().width(15).color(R.color.colorAccent);

        for (int i = 0; i < pathLocations.size(); i++) {
            rectLine.add(pathLocations.get(i));
            //todo is it storing correctly lat,lons ?
            Utils.storeGpsPath(this, pathLocations, i);
        }
        mMap.addPolyline(rectLine);
    }

    private void calculatePathDistance(LatLng currentLocation, ArrayList<LatLng> path) {
        if (path.size()<2) { return; }

        if (PolyUtil.isLocationOnPath(currentLocation, path, true)) {
            accumulatedDistance += SphericalUtil.computeDistanceBetween(path.get(locationsIndex),path.get(locationsIndex+1));
            locationsIndex+=1;
            txtMapDist.setText("Distance on Map: "+accumulatedDistance+"m");
            Log.e("PathDistance =", "= "+accumulatedDistance);
        }
    }


}


