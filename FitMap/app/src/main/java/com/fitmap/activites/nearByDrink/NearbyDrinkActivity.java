package com.fitmap.activites.nearByDrink;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.fitmap.R;
import com.fitmap.utils.AlertDialogManager;
import com.fitmap.utils.ConnectionDetector;
import com.fitmap.utils.GPSTracker;
import com.fitmap.utils.TabsControl;
import com.fitmap.webservice.GooglePlaces;
import com.fitmap.webservice.Place;
import com.fitmap.webservice.PlacesList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * This class manages the NearByDrink function. It finds out all
 * the nearby restaurants/groceries/markets/bars/cafes and list them
 * from near to far in a listview.
 */
public class NearbyDrinkActivity extends Activity {
    public String TAG = "Nearby Drink Activity";

    // flag for Internet connection status
    boolean isInternetPresent = false;

    // Connection detector
    ConnectionDetector cd;

    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

    // Google Places
    GooglePlaces googlePlaces;

    // Places List
    PlacesList nearbyPlaces;

    // GPS Location
    GPSTracker gps;

    // Progress dialog
    ProgressDialog pDialog;

    // Places Listview
    ListView lv;

    ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String, String>> ();
    public static String KEY_REFERENCE = "reference";
    public static String KEY_NAME = "name";
    public static String KEY_RATING = "rating";
    public static String KEY_DISTANCE = "distance";
    public static String KEY_STYLE = "style";
    public static String KEY_LOCATION = "location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_drink);

        TabsControl.controlTabs(this);
        Log.w(TAG, "activity created");

        placesListItems.clear();

        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent == false) {
            alert.showAlertDialog(NearbyDrinkActivity.this, "Internet Connection Error", "Please connect to internet first!", false);
            return;
        }

        gps = new GPSTracker(this);
        if (gps.canGetLocation() == true) {
            Log.d("Your Location", "latitude:" + gps.getLatitude() + ", longitude: " + gps.getLongitude());
        } else {
            alert.showAlertDialog(NearbyDrinkActivity.this, "GPS Status", "Couldn't get location information. Please enable GPS", false);
            return;
        }

        lv = (ListView) findViewById(R.id.list);
        new LoadPlaces().execute();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String reference = ((TextView) view.findViewById(R.id.reference)).getText().toString();
                String name = ((TextView) view.findViewById(R.id.place_name)).getText().toString();
                String location = ((TextView) view.findViewById(R.id.location)).getText().toString();
                Intent in = new Intent(getApplicationContext(), SinglePlaceActivity.class);
                in.putExtra(KEY_REFERENCE, reference);
                in.putExtra(KEY_NAME, name);
                in.putExtra(KEY_LOCATION, location);
                startActivity(in);
            }
        });
    }

    /*
     * Background Async Task to Load Google places
     */
    class LoadPlaces extends AsyncTask<String, String, String> {

        /*
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(NearbyDrinkActivity.this);
            pDialog.setMessage(Html.fromHtml("<b>Search</b><br/>Loading Places..."));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            googlePlaces = new GooglePlaces();
            String types = "cafe|grocery_or_supermarket|subway_station|restaurant|bar";
            double radius = 1000;
            try {
                nearbyPlaces = googlePlaces.search(gps.getLatitude(), gps.getLongitude(), radius, types);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (nearbyPlaces == null) {
                        alert.showAlertDialog(NearbyDrinkActivity.this, "Search Status", "Couldn't find nearby places.", false);
                        return;
                    }
                    String status = nearbyPlaces.status;
                    if (status.equals("OK")) {
                        if (nearbyPlaces.results != null) {
                            for (Place p : nearbyPlaces.results) {
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(KEY_REFERENCE, p.reference);
                                map.put(KEY_NAME, p.name);
                                map.put(KEY_RATING, String.valueOf(p.rating));
                                map.put(KEY_DISTANCE,p.getDistance(gps.getLatitude(), gps.getLongitude()));
                                map.put(KEY_STYLE, p.types[0]);
                                map.put(KEY_LOCATION, p.geometry.toString());
                                placesListItems.add(map);
                            }
                            Comparator comparator = new MyComparator();
                            Collections.sort(placesListItems, comparator);
                            CustomAdapter adapter = new CustomAdapter(placesListItems, R.layout.adapter_item);
                            lv.setAdapter(adapter);
                        }
                    } else if(status.equals("ZERO_RESULTS")) {
                        alert.showAlertDialog(NearbyDrinkActivity.this, "Nearby Drinks",
                                "Sorry no places found.",
                                false);
                    } else if(status.equals("UNKNOWN_ERROR")) {
                        alert.showAlertDialog(NearbyDrinkActivity.this, "Error",
                                "Sorry unknown error occurred.",
                                false);
                    } else if(status.equals("OVER_QUERY_LIMIT")) {
                        alert.showAlertDialog(NearbyDrinkActivity.this, "Error",
                                "Sorry query limit to google places is reached",
                                false);
                    } else if(status.equals("REQUEST_DENIED")) {
                        alert.showAlertDialog(NearbyDrinkActivity.this, "Error",
                                "Sorry error occurred. Request is denied",
                                false);
                    } else if(status.equals("INVALID_REQUEST")) {
                        alert.showAlertDialog(NearbyDrinkActivity.this, "Error",
                                "Sorry error occurred. Invalid Request",
                                false);
                    } else {
                        alert.showAlertDialog(NearbyDrinkActivity.this, "Error",
                                "Sorry error occurred.",
                                false);
                    }
                }
            });
        }
    }

    public class MyComparator implements Comparator<HashMap<String, String>>{

        @Override
        public int compare(HashMap<String, String> lhs, HashMap<String, String> rhs) {
            float ln = Float.valueOf(lhs.get(KEY_DISTANCE));
            float rn = Float.valueOf(rhs.get(KEY_DISTANCE));
            if (ln < rn) {
                return -1;
            } else if (ln > rn) {
                return 1;
            }
            return 0;
        }
    }
}
