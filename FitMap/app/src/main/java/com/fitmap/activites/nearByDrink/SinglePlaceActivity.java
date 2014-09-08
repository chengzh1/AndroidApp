package com.fitmap.activites.nearByDrink;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fitmap.R;
import com.fitmap.utils.AlertDialogManager;
import com.fitmap.utils.ConnectionDetector;
import com.fitmap.webservice.GooglePlaces;
import com.fitmap.webservice.PlaceDetails;
import com.fitmap.webservice.YelpAPI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * This class shows detailed information about a single place
 * which users are interested in.
 */
public class SinglePlaceActivity extends Activity {
    public static String KEY_REFERENCE = "reference";
    public static String KEY_NAME = "name";
    public static String KEY_LOCATION = "location";

    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    ConnectionDetector cd;

    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

    // Google Places
    GooglePlaces googlePlaces;

    // Place Details
    PlaceDetails placeDetails;

    // Progress dialog
    ProgressDialog pDialog;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_place);

        Intent i = getIntent();
        String reference = i.getStringExtra(KEY_REFERENCE);
        String name = i.getStringExtra(KEY_NAME);
        String[] locations = i.getStringExtra(KEY_LOCATION).split(",");
        new LoadSinglePlaceDetails().execute(reference, name);
        double lat = Double.valueOf(locations[0]);
        double lng = Double.valueOf(locations[1]);

        MapFragment fm = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mMap = fm.getMap();
        MarkerOptions dest = new MarkerOptions();
        dest.position(new LatLng(lat, lng));
        mMap.addMarker(dest);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 13));
        mMap.setMyLocationEnabled(true);
    }

    /*
     * Background Async Task to Load Google place details
     */
    class LoadSinglePlaceDetails extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SinglePlaceActivity.this);
            pDialog.setMessage("Loading profile ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            googlePlaces = new GooglePlaces();
            try {
                placeDetails = googlePlaces.getPlaceDetails(params[0]);
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
                    if(placeDetails != null){
                        String status = placeDetails.status;
                        if (status == null) {
                            return;
                        }
                        if(status.equals("OK")){
                            if (placeDetails.result != null) {
                                String name = placeDetails.result.name;
                                String address = placeDetails.result.formatted_address;
                                String phone = placeDetails.result.international_phone_number;
                                String website = placeDetails.result.website;

                                Log.d("Place ", name + address + phone);

                                TextView lbl_name = (TextView) findViewById(R.id.name);
                                TextView lbl_address = (TextView) findViewById(R.id.address);
                                TextView lbl_phone = (TextView) findViewById(R.id.phone_num);
                                TextView lbl_website = (TextView) findViewById(R.id.website);
                                ImageView yelp = (ImageView) findViewById(R.id.yelp);

                                name = name == null ? "Not present" : name;
                                address = address == null ? "Not present" : address;
                                phone = phone == null ? "Not present" : phone;
                                website = website == null ? "Not present" : website;

                                lbl_name.setText(name);
                                lbl_address.setText(address);
                                lbl_phone.setText(phone);
                                lbl_website.setText(website);

                                final String finalName = name;
                                final String finalAddress = address;
                                yelp.setVisibility(View.VISIBLE);
                                yelp.setClickable(true);
                                yelp.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new LoadYelp().execute(finalName, finalAddress);
                                    }
                                });
                            }
                        } else if(status.equals("ZERO_RESULTS")) {
                            alert.showAlertDialog(SinglePlaceActivity.this, "Nearby Places",
                                    "Sorry no place found.",
                                    false);
                        } else if(status.equals("UNKNOWN_ERROR")) {
                            alert.showAlertDialog(SinglePlaceActivity.this, "Error",
                                    "Sorry unknown error occurred.",
                                    false);
                        } else if(status.equals("OVER_QUERY_LIMIT")) {
                            alert.showAlertDialog(SinglePlaceActivity.this, "Error",
                                    "Sorry query limit to google places is reached",
                                    false);
                        } else if(status.equals("REQUEST_DENIED")) {
                            alert.showAlertDialog(SinglePlaceActivity.this, "Error",
                                    "Sorry error occurred. Request is denied",
                                    false);
                        } else if(status.equals("INVALID_REQUEST")) {
                            alert.showAlertDialog(SinglePlaceActivity.this, "Error",
                                    "Sorry error occurred. Invalid Request",
                                    false);
                        } else {
                            alert.showAlertDialog(SinglePlaceActivity.this, "Error",
                                    "Sorry error occurred.",
                                    false);
                        }
                    } else {
                        alert.showAlertDialog(SinglePlaceActivity.this, "Error",
                                "Sorry error occurred.",
                                false);
                    }
                }
            });
        }
    }

    /*
     * Background Async Task to Load Yelp url for a single place
     */
    class LoadYelp extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SinglePlaceActivity.this);
            pDialog.setMessage("Loading profile ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            YelpAPI.queryAPI(params[0], params[1]);
            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            if (YelpAPI.getUrl() == null) {
                return;
            }
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(YelpAPI.getUrl()));
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.single_place, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
