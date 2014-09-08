package com.fitmap.activites.dailyRun;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

import com.fitmap.R;
import com.fitmap.activites.AccountActivity;
import com.fitmap.services.LocationService;
import com.fitmap.services.MusicService;
import com.fitmap.utils.Navigation;
import com.fitmap.utils.NewRunDbHelper;
import com.fitmap.utils.Song;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Display details for each item in Daily Run
 */
public class NewRunItemFragment extends Fragment implements MediaController.MediaPlayerControl{
	final static String ARG_POSITION = "position", ARG_STARTTIME = "startTime",
                        ARG_STARTL = "startL", ARG_STARTB = "startB",   //start longtitude and latitude
                        ARG_ENDL = "endL",     ARG_ENDB = "endB",
                        ARG_ISEND = "isend";
    final static double CALORIES_INDEX = 0.75;
    public int mCurrentPosition = -1, mIsEnd;
    public String mStartTime;
    public double mStartL, mStartB, mEndL, mEndB;
    public double runningDist;
    public ArrayList<Song> songList;

    private GoogleMap mMap;

    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    private MusicController controller;
    private boolean paused=false;
    private boolean playbackPaused=false;

    private ServiceConnection musicConnection;
    private MyReceiver myReceiver;
    private static SharedPreferences settings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
    	View view = inflater.inflate(R.layout.new_run_item, container, false);
        initMusicService();
        setController(view);
        settings = getActivity().getSharedPreferences(Navigation.settingFile, Context.MODE_PRIVATE);
        view.post(new Runnable() {
            public void run() {
                controller.show();
            }
        });
        MapFragment fm = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mMap = fm.getMap();

        Button controlBtn = (Button) view.findViewById(R.id.floating_button_start);
        controlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button self = (Button) v.findViewById(R.id.floating_button_start);
                if (self.getText().equals("Start")) {
                    startService();
                    self.setText("Stop");
                } else if (self.getText().equals("Stop")) {
                    endService();
                    musicSrv.stopSong();
                }
            }
        });

        ImageView music = (ImageView) view.findViewById(R.id.music);
        music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicSrv.setSong(4);
                musicSrv.playNext();
            }
        });

        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);
            mStartL = savedInstanceState.getDouble(ARG_STARTL);
            mStartB = savedInstanceState.getDouble(ARG_STARTB);
            mEndL = savedInstanceState.getDouble(ARG_ENDL);
            mEndB = savedInstanceState.getDouble(ARG_ENDB);
            mIsEnd = savedInstanceState.getInt(ARG_ISEND);
        }

        return view;
    }

    /**
     * Initialize Music Service
     */
    private void initMusicService() {
        ContentResolver musicResolver = getActivity().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        songList = new ArrayList<Song>();
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                Log.d("song", thisTitle);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
        musicConnection = new ServiceConnection(){

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
                //get service
                musicSrv = binder.getService();
                //pass list
                musicSrv.setList(songList);
                musicBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                musicBound = false;
            }
        };

        if(playIntent == null){
            playIntent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
    }

    /**
     * Set Music controller
     * @param view current view
     */
    private void setController(View view) {
        controller = new MusicController(getActivity());
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                controller.show();
                return false;
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(view);
        controller.setEnabled(true);
    }

    //play next
    private void playNext(){
        musicSrv.playNext();
        if(playbackPaused){
            setController(getView());
            controller.show();
            playbackPaused = false;
        }
        controller.show(0);
    }

    //play previous
    private void playPrev(){
        musicSrv.playPrev();
        if(playbackPaused){
            setController(getView());
            controller.show();
            playbackPaused=false;
        }
        controller.show(0);
    }

    /**
     * Things to clear when return back
     */
    public void clear(){
        NewRunActivity.itemPosition = -1;
        getFragmentManager().popBackStack("head", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        removeMapFragment();
    }

    /**
     * Remove google map fragment
     */
    private void removeMapFragment(){
        FragmentTransaction fm = getFragmentManager().beginTransaction();
        fm.remove(getFragmentManager().findFragmentById(R.id.map));
        fm.commit();
    }

    /**
     * Update the view
     * @param position item's position in list
     * @param startTime start time
     * @param startL    start longitude
     * @param startB    start latitude
     * @param endL      end longitude
     * @param endB      end latitude
     * @param isEnd     if the goal is completed
     */
    public void updateView(int position, String startTime, double startL, double startB,
                           double endL, double endB, int isEnd)
    {
        mCurrentPosition = position;
        mStartTime = startTime;
        mStartL = startL;
        mStartB = startB;
        mEndL = endL;
        mEndB = endB;
        mIsEnd = isEnd;
    }

    @Override
    public void onStart() {
        super.onStart();
        //Register receiver
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationService.MY_ACTION);

        getActivity().registerReceiver(myReceiver, intentFilter);
        // During startup, check if there are arguments passed to the fragment.
        // onStart is a good place to do this because the layout has already been
        // applied to the fragment at this point so we can safely call the method
        // below that sets the article text.
        Bundle args = getArguments();
        if (args != null) {
            // Set article based on argument passed in
            updateView(args.getInt(ARG_POSITION), args.getString(ARG_STARTTIME),
                       args.getDouble(ARG_STARTL, 0), args.getDouble(ARG_STARTB, 0),
                       args.getDouble(ARG_ENDL, 0), args.getDouble(ARG_ENDB, 0),
                       args.getInt(ARG_ISEND,0));
        } else if (mCurrentPosition != -1) {
            // Set article based on saved instance state defined during onCreateView
            updateView(mCurrentPosition, mStartTime, mStartL, mStartB, mEndL, mEndB, mIsEnd);
        }

        setViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(paused){
            setController(getView());
            controller.show();
            paused=false;
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the current article selection in case we need to recreate the fragment
        outState.putInt(ARG_POSITION, mCurrentPosition);
        outState.putString(ARG_STARTTIME, mStartTime);
        outState.putDouble(ARG_STARTL, mStartL);
        outState.putDouble(ARG_STARTB, mStartB);
        outState.putDouble(ARG_ENDL, mEndL);
        outState.putDouble(ARG_ENDB, mEndB);
    }

    @Override
    public void onStop(){
        musicSrv.stopSong();
        getActivity().unregisterReceiver(myReceiver);
        controller.hide();
        getActivity().stopService(playIntent);
        musicSrv=null;
        super.onStop();
    }

    @Override
    public void onPause() {
        paused = true;
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        getActivity().stopService(playIntent);
        musicSrv=null;
        controller.setEnabled(false);
        super.onDestroyView();
    }

    /**
     *  Initialize views
     */
    private void setViews(){
        Log.d("Info:", "time:" + mStartTime + "\nstart longitude: " + mStartL
                + " start latitude: " + mStartB + "\nend longitude: "
                + mEndL + " end longitude: " + mEndB);

        String distance = getDistance(mStartB, mStartL, mEndB, mEndL, "M");

        String user = settings.getString(Navigation.TAG_USER, "");
        TextView distanceView = (TextView) getActivity().findViewById(R.id.new_rum_item_distance);
        TextView caloriesView = (TextView) getActivity().findViewById(R.id.new_run_item_calories);

        distanceView.setText(distance);
        caloriesView.setText(String.format("%.2f", runningDist * CALORIES_INDEX * settings.getFloat(user + "_weight", AccountActivity.DEFAULT_WEIGHT)) + " calories");

        String url = makeURL(mStartB, mStartL, mEndB, mEndL);
        MarkerOptions start = new MarkerOptions();
        MarkerOptions dest = new MarkerOptions();
        start.position(new LatLng(mStartB, mStartL));
        dest.position(new LatLng(mEndB, mEndL));
        mMap.addMarker(start);
        mMap.addMarker(dest);
        new connectAsyncTask(url).execute();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mStartB, mStartL),13));
        mMap.setMyLocationEnabled(true);
    }

    /**
     * Start to collect gps data in background service
     */
    private void startService(){
        LocationService.set(getActivity(), mStartTime);
        Intent intent = new Intent(getActivity(), LocationService.class);
        getActivity().startService(intent);
    }

    /**
     * End the running of background gps service
     */
    private void endService(){
        Intent intent = new Intent(getActivity(), LocationService.class);
        getActivity().stopService(intent);
    }

    //implement methods in MediaController.MediaPlayerControl
    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (musicSrv != null && musicBound && musicSrv.isPng()) {
            return musicSrv.getDur();
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicSrv != null && musicBound && musicSrv.isPng()) {
            return musicSrv.getPosn();
        }
        return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound) {
            return musicSrv.isPng();
        }
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    /**
     * BroadcastReceiver to listen to location service
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            LocationService.reSet();
            NewRunDbHelper mHelper = new NewRunDbHelper(getActivity());
            SQLiteDatabase db = mHelper.getWritableDatabase();
            mHelper.endRun(db, mStartTime);
            db.close();
            displayResult();
        }
    }

    /**
     * Display result after stop
     */
    private void displayResult(){
        NewRunActivity.itemPosition = -1;
        Intent intent = new Intent(getActivity(), NewRunResult.class);
        intent.putExtra(NewRunResult.TAG, mStartTime);
        getActivity().startActivity(intent);
    }

    /**
     * Get distance between two points
     * @param lat1  point1 latitude
     * @param lon1  point1 longitude
     * @param lat2  point2 latitude
     * @param lon2  point2 longitude
     * @param unit  unit for the distance
     * @return distance in good string format
     */
    private String getDistance(double lat1, double lon1, double lat2, double lon2, String unit) {
        String result = "0";
        DecimalFormat df = new DecimalFormat("0.000");
        double dist = calculateDistance(lat1, lon1, lat2, lon2, unit);
        runningDist = dist;
        if (unit.equals("K"))                //kilometer
            result = df.format(dist) + " km";
        else if (unit.equals("M"))           //miles
            result = df.format(dist) + "miles";
        return result;
    }

    /**
     * Calculate distance between two points
     * @param lat1  point1 latitude
     * @param lon1  point1 longitude
     * @param lat2  point2 latitude
     * @param lon2  point2 longitude
     * @param unit  unit for the distance
     * @return distance in double
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2, String unit)
    {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit.equals("K")) {          //Kilometer
            dist = dist * 1.609344;
        } else if (unit.equals("M")) {   //Mile
            dist = dist * 0.8684;
        }

        return dist;
    }

    /**
     * This function converts decimal degrees to radians
     * @param deg degrees
     * @return  radians
     */
    private double deg2rad(double deg)
    {
        return (deg * Math.PI / 180.0);
    }

    /**
     * This function converts radians to decimal degrees
     * @param rad radians
     * @return  decimal degrees
     */
    private double rad2deg(double rad)
    {
        return (rad * 180.0 / Math.PI);
    }

    /**
     * Mark URL to request direction between two points
     * @param sourcelat start point's latitude
     * @param sourcelog start point's longitude
     * @param destlat   end point's latitude
     * @param destlog   end point's longitude
     * @return  URL for request
     */
    public String makeURL (double sourcelat, double sourcelog, double destlat, double destlog){
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString
                .append(Double.toString( sourcelog));
        urlString.append("&destination=");// to
        urlString
                .append(Double.toString( destlat));
        urlString.append(",");
        urlString.append(Double.toString( destlog));
        urlString.append("&sensor=false&mode=walking&alternatives=true");
        return urlString.toString();
    }

    /**
     * Draw path in google map
     * @param result Result from Google Direction APT
     */
    public void drawPath(String result) {
        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            PolylineOptions polyLineOptions = new PolylineOptions();
            polyLineOptions.addAll(list);
            polyLineOptions.color(Color.BLUE);
            polyLineOptions.width(10);
            polyLineOptions.geodesic(true);
            mMap.addPolyline(polyLineOptions);
        }
        catch (JSONException e) {
            Log.e("err", e.getMessage());
        }
    }

    /**
     * Async Task that connected with google direction web service
     */
    private class connectAsyncTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog progressDialog;
        String url;
        connectAsyncTask(String urlPass){
            url = urlPass;
        }
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Fetching route, Please wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }
        @Override
        protected String doInBackground(Void... params) {
            JSONParser jParser = new JSONParser();
            return jParser.getJSONFromUrl(url);
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if(result!=null){
                drawPath(result);
            }
        }
    }

    /**
     * Class for parsing JSON
     */
    public class JSONParser {

        InputStream is = null;
        String json = "";
        // constructor
        public JSONParser() {
        }
        public String getJSONFromUrl(String url) {

            // Making HTTP request
            try {
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);

                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                json = sb.toString();
                is.close();
            } catch (Exception e) {
                Log.e("Buffer Error", "Error converting result " + e.toString());
            }
            return json;

        }
    }

    /**
     * Decode Poly lines
     * @param encoded encoded result
     * @return  Decoded latitude and longitude
     */
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }
}