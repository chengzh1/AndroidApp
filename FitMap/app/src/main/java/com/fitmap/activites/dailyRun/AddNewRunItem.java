package com.fitmap.activites.dailyRun;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.fitmap.R;
import com.fitmap.utils.NewRunDbHelper;
import com.fitmap.utils.TimeHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for adding new run item
 */
public class AddNewRunItem extends DialogFragment {
    private EditText startPointView;
    private EditText endPointView;
    private EditText monthView;
    private EditText dayView;
    private EditText yearView;
    private CheckBox alarmView;
    private TimePicker timePicker;

    GoogleMap googleMap;

    private String startTime, alarm;
    private double startL = 0, startB = 0, endL = 0, endB = 0;

    private onSaveListener saveListener;

    /**
     * Interface for connection NewRunActivity and AddNewRunItem
     */
    public interface onSaveListener{
        public void refresh(int id, boolean ifAlarm, String startTime);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // create a new alert dialog
        AlertDialog.Builder b =  new  AlertDialog.Builder(getActivity())
                .setTitle("Add Daily Run")
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // override it in onStart()
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                             //   removeMapFragment();
                                dialog.dismiss();
                            }
                        }
                );

        LayoutInflater i = getActivity().getLayoutInflater();
        View v = i.inflate(R.layout.add_new_run,null);

        setAllViews(v);

        b.setView(v);

        return b.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            saveListener = (onSaveListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SaveListener");
        }
    }

    @Override
    public void onStart()
    {
        //super.onStart() is where dialog.show() is actually called on the underlying dialog,
        // so we have to do it after this point
        super.onStart();
        AlertDialog d = (AlertDialog)getDialog();
        if(d != null)
        {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Boolean wantToCloseDialog = timeValidation() && locationValidation();
                    if(wantToCloseDialog) {
                        saveData();
                     // removeMapFragment();
                        dismiss();
                    }
                }
            });
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        //re-create the map
        initializeMap();
    }

    @Override
    public void onStop(){
        //remove the map
        removeMapFragment();
        super.onStop();
    }

    /**
     * set all views in the fragment
     * @param v layout's parent view
     */
    private void setAllViews(View v){
        initializeMap();
        startPointView = (EditText) v.findViewById(R.id.add_new_run_start_point);
        endPointView = (EditText) v.findViewById(R.id.add_new_run_end_point);

        monthView = (EditText) v.findViewById(R.id.add_new_run_start_time_month);
        dayView = (EditText) v.findViewById(R.id.add_new_run_start_time_day);
        yearView = (EditText) v.findViewById(R.id.add_new_run_start_time_year);

        timePicker = (TimePicker)v.findViewById(R.id.add_new_run_timePicker);
        alarmView = (CheckBox) v.findViewById(R.id.add_new_run_alarm);

        monthView.setText(TimeHelper.currentMonth());
        dayView.setText(TimeHelper.currentDay());
        yearView.setText(TimeHelper.currentYear());
        setListener();
    }

    /**
     * Verify if the input date and time is valid
     * @return true if input date and time is valid, false otherwise
     */
    private boolean timeValidation(){
        String mont_str = monthView.getText().toString();
        String day_str = dayView.getText().toString();
        String year_str = yearView.getText().toString();
        String hour_str, minute_str;

        //Empty date
        if (mont_str.length() ==  0 || day_str.length() == 0 || year_str.length() == 0)
        {
            Toast.makeText(getActivity(), "Start Date is Empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        int month = Integer.parseInt(mont_str);
        int day = Integer.parseInt(day_str);
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();
        //Start date invalid
        if (month > 12 || month < 1 || day < 1 || day > TimeHelper.dayInMonth[month - 1]){
            Toast.makeText(getActivity(), "Start Date invalid", Toast.LENGTH_SHORT).show();
            return false;
        }

        minute_str = String.valueOf(minute);
        hour_str = String.valueOf(hour);

        //modify date
        if (month < 10){
            mont_str = "0" + mont_str;
        }
        if (day < 10){
            day_str = "0" + day_str;
        }
        if (hour < 10){
            hour_str = "0" + hour_str;
        }
        if (minute < 10){
            minute_str = "0" + minute_str;
        }

        //"yyyy/MM/dd HH:mm" format
        String myTime = year_str + "/" + mont_str + "/" + day_str
                + " " + hour_str + ":" + minute_str;

        if (TimeHelper.isPastTime(myTime)){
            Toast.makeText(getActivity(), myTime + " is already past", Toast.LENGTH_SHORT).show();
            return false;
        }

        //"yyyy/MM/dd HH:mm:ss" format
        startTime = myTime + ":00";

        //set alarm
        if (alarmView.isChecked()) {
            alarm = "Yes";
        }
        else
            alarm = "No";
        return true;
    }

    /**
     * Verify if the address is valid
     * @return true if input address is valid, false otherwise
     */
    private boolean locationValidation(){
        String startAddress = startPointView.getText().toString();
        String endAddress = endPointView.getText().toString();
        //Empty address
        if (startAddress.length() == 0 || endAddress.length() == 0){
            Toast.makeText(getActivity(), "Address is empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        //validate location
        if (startL == 0 && startB == 0 || endL == 0 && endB == 0){
            Toast.makeText(getActivity(), "Invalid address", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Remove google map fragment
     */
    private void removeMapFragment(){
        FragmentTransaction fm = getFragmentManager().beginTransaction();
        fm.remove(getFragmentManager().findFragmentById(R.id.add_new_run_map));
        fm.commit();
    }

    /**
     * Initiate google map
     */
    private void initializeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.add_new_run_map)).getMap();
            if (googleMap == null){
                Toast.makeText(getActivity(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
            googleMap.setMyLocationEnabled(true);
            googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    Location location  = googleMap.getMyLocation();
                    if (location == null)
                        return false;
                       String address = getAddress(location.getLatitude(), location.getLongitude());
                       startPointView.setText(address);
                   //    markAddress(null, location, 1);
                    return false;
                }
            });

        }
    }

    /**
     * Get address from latitude and longitude
     * @param latitude latitude
     * @param longitude longitude
     * @return address
     */
    private String getAddress(double latitude, double longitude){
        Geocoder geocoder;
        List<Address> addresses;
        String address = null, city = null, country = null;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() == 0)
                return null;
            address = addresses.get(0).getAddressLine(0);
            city = addresses.get(0).getAddressLine(1);
            country = addresses.get(0).getAddressLine(2);
        }catch (IOException e){
            e.printStackTrace();
        }
        if (address == null || city == null || country == null)
            return null;
        return address + " " + city + " " + country;
    }

    /**
     * Set listener in the fragment's view
     */
    private void setListener(){
        setEditTextAction(startPointView, 1);
        setEditTextAction(endPointView, 2);

    }

    /**
     * Set listener to EditText
     * @param eT EditText the listener assigned to
     * @param id id to distinct startAddress and endAddress
     */
    private void setEditTextAction(final EditText eT, final int id){
        //set onEditroActionListener
        eT.setOnEditorActionListener(
            new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER ||
                        event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
                    {
                        String strAddress = eT.getText().toString();
                        if (strAddress.length() > 0) {
                            markAddress(strAddress,null, id);
                        }
                        return false;

                    }
                    return false; // pass on to other listeners.
                }
            }
        );
        //set onFocusChangeListener
        eT.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus){
                    String strAddress = eT.getText().toString();
                    if (strAddress.length() > 0) {
                        markAddress(strAddress, null,id);
                    }
                }
            }
        });
    }

    /**
     * Mark address
     * @param strAddress address in String
     * @param location  location for Mark
     * @param id 1 for start address, else for end address
     * @return true if successfully marked, false otherwise
     */
    private boolean markAddress(String strAddress, Location location, int id){
        if (strAddress == null && location == null)
            return false;

        double latitude, longitude;
        if (location != null){
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            if (id == 1){
                startL = longitude;
                startB = latitude;
            }else{
                endL = longitude;
                endB = latitude;
            }
            markMyLocation(latitude, longitude, id);
            return true;
        }

        Geocoder coder = new Geocoder(getActivity());
        List<Address> addresses;
        try{
            addresses = coder.getFromLocationName(strAddress,5);
            if (addresses == null || addresses.size() == 0) {
                startL = 0;
                startB = 0;
                endL = 0;
                endB = 0;
                return false;
            }
            Address address = addresses.get(0);
            latitude = address.getLatitude();
            longitude = address.getLongitude();
            if (id == 1){
                startL = longitude;
                startB = latitude;
            }else{
                endL = longitude;
                endB = latitude;
            }
            Log.d("fitmap", "Your Location is - Lat: " + latitude + "Long: " + longitude);

            markMyLocation(latitude, longitude, id);
            return true;
        }catch(IOException e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mark location on google map
     * @param latitude latitude in double
     * @param longitude longitude in double
     * @param id 1 for start address, else for end address
     */
    private void markMyLocation(double latitude, double longitude, int id) {
        if (googleMap == null)
            return;
        MarkerOptions marker;
        if (id == 1)
            marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title("Start Address");
        else
            marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title("End Address");
        googleMap.addMarker(marker);

        Location myLocation = new Location("");
        myLocation.setLatitude(latitude);
        myLocation.setLongitude(longitude);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 13));
    }

    /**
     * Save data in SQLite
     */
    private void saveData(){
        NewRunDbHelper mDbHelper = new NewRunDbHelper(getActivity());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowId = (int)mDbHelper.insert(db,startTime, startL, startB, endL, endB, alarm);
        db.close();
        if (alarm.equals("Yes"))
            saveListener.refresh(rowId, true, startTime);
        else
            saveListener.refresh(rowId, false, startTime);
        Toast.makeText(getActivity(),"Saved!",Toast.LENGTH_SHORT).show();
    }
}
