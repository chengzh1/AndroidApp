package com.fitmap.services;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

/**
 * Service to collect gps data
 */
public class LocationService extends Service implements LocationListener {
    public static Context mContext;
    public static String timeTag;
    public static int countTag;
    public static final String dataFile = "locationData";
    public final static String MY_ACTION = "MY_ACTION";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    // flag for gps status
    private boolean isGPSEnabled = false;

    // flag for network status
    private boolean isNetworkEnabled = false;

    // flag for gps status
    private boolean canGetLocation = false;

    private Location location = null;
    private double latitude;
    private double longitude;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 1 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1 * 60;

    private LocationManager locationManager;


    public LocationService(){
        preferences = mContext.getSharedPreferences(dataFile,Context.MODE_PRIVATE);
        editor = preferences.edit();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d("Location Service", "Service Started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        getLocation();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy(){
        countRecord();
        stopUsingGPS();
        Log.d("Location Service", "Service Destroyed");
        super.onDestroy();
    }

    /**
     * Get current location
     * @return current location
     */
    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                Toast.makeText(mContext, "GPS and Network are not available.", Toast.LENGTH_LONG).show();
                showSettingsAlert();
            } else {
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network enabled");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
                    }
                }

                if (location == null && isGPSEnabled) {
                    locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("GPS", "GPS enabled");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                    }
                }
                if (location == null) {
                    Log.d("location", "null");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    /**
     * Stop using GPS
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(LocationService.this);
        }
    }

    /**
     * Get latitude
     * @return latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    /**
     * Get longitude
     * @return longitude
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    /**
     * If can get location
     * @return true for yes, false for no
     */
    public boolean canGetLocation() {
        if (location != null) {
            canGetLocation = true;
        }
        return canGetLocation;
    }

    /**
     * Show alert when GPS is not able to use
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Setting GPS");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        if (canGetLocation()) {
            saveData(getLatitude(), getLongitude());
        } else {
            String message = "Couldn't get location information. Please enable GPS";
            Log.d("Location Service", "Couldn't get location information. Please enable GPS");
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        if(mContext !=null){
            Toast.makeText(mContext, "Enabled new provider\n please go back to continue " + provider,
                    Toast.LENGTH_SHORT).show();}
    }

    @Override
    public void onProviderDisabled(String provider) {
        if(mContext!=null)
        {
            Toast.makeText(mContext, "Disabled provider " + provider,Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Save data
     * @param latitude  latitude
     * @param longitude longitude
     */
    private void saveData(double latitude, double longitude){
        String message = "Save latitude:" + latitude + ", longitude: " + longitude;
        Log.d("Location Serivce", message);

        String latKey = timeTag + "_lat_" + countTag;
        String lonKey = timeTag + "_lon_" + countTag;
        editor.putFloat(latKey, (float)latitude);
        editor.putFloat(lonKey, (float)longitude);
        editor.commit();
        //increase countTag
        countTag ++;
    }

    /**
     * Count record
     */
    private void countRecord(){
        String key = timeTag + "_count";
        //System.out.println(key + " " + countTag);
        editor.putInt(key, countTag);
        editor.commit();

        Intent intent = new Intent();
        intent.setAction(MY_ACTION);
        sendBroadcast(intent);
    }

    /**
     * Set before run the service
     * @param activity  context
     * @param startTime start time
     */
    public static void set(Activity activity, String startTime){
        mContext = activity;
        timeTag = startTime;
        countTag = 0;
    }

    /**
     * Reset the service
     */
    public static void reSet(){
        mContext = null;
        timeTag = null;
        countTag = 0;
    }

    /**
     * Clear sharedPreference
     * @param activity context
     */
    public static void deleteAll(Activity activity){
        SharedPreferences tmpPreferences = activity.getSharedPreferences(dataFile,Context.MODE_PRIVATE);
        SharedPreferences.Editor tmpEditor = tmpPreferences.edit();
        tmpEditor.clear();
        tmpEditor.commit();
    }

    /**
     * Delete an item of sharedPreference
     * @param activity  context
     * @param startTime start time
     */
    public static void deleteWithTag(Activity activity, String startTime){
        SharedPreferences tmpPreferences = activity.getSharedPreferences(dataFile,Context.MODE_PRIVATE);
        SharedPreferences.Editor tmpEditor = tmpPreferences.edit();
        String countKey = startTime + "_count";
        int count = tmpPreferences.getInt(countKey, 0);
        for (int i = 0; i < count; i ++){
            String latKey = startTime + "_lat_" + i;
            String lonKey = startTime + "_lon_" + i;
            tmpEditor.remove(latKey);
            tmpEditor.remove(lonKey);
        }
        tmpEditor.remove(countKey);
        tmpEditor.commit();
    }
}