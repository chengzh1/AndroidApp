package com.chengzhang.location;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private String device_address;
	Button btnShowLocation;
	GPSTracker gps;
	TextView tv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btnShowLocation = (Button) findViewById(R.id.get_location_button);
        
        // show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
             
            @Override
            public void onClick(View arg0) {        
                // create class object
                gps = new GPSTracker(MainActivity.this);
 
                // check if GPS enabled     
                if(gps.canGetLocation()){
                     
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                     
                    // \n is for new line
                    tv = (TextView) findViewById(R.id.lat_lng);
                    device_address = "Your Location is - \nLat: " + latitude + "\nLong: " + longitude; 
            		tv.setText(device_address);
                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();    
                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }
                 
            }
        });
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	 /***for send SMS test ****/
    public void sendMessage(View view){
    	 String phoneNo = "5556";   
         String message = device_address;
         if (message == null)
        	 message = "CMU";
         if (phoneNo.length() > 0 && message.length() > 0) {
            
             sendSMS(phoneNo,message);

         } else {
             Toast.makeText(getBaseContext(),
                     "Please enter both phone number and message.",
                     Toast.LENGTH_SHORT).show();
         }

    }


   private void sendSMS(String phoneNumber, String message) {
	   String SENT = "SMS_SENT";
	   String DELIVERED = "SMS_DELIVERED";

	   PendingIntent sentPI = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(SENT), 0);
	   PendingIntent deliveredPI = PendingIntent.getBroadcast(MainActivity.this,0, new Intent(DELIVERED), 0);

	   // ---when the SMS has been sent---
	   final String string = "deprecation";
	   registerReceiver(new BroadcastReceiver() {
		   @Override
		   public void onReceive(Context arg0, Intent arg1) {
			   switch (getResultCode()) {
			   case Activity.RESULT_OK:
				   Toast.makeText(MainActivity.this, "SMS sent",
						   Toast.LENGTH_SHORT).show();
                break;
			   case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
				   Toast.makeText(MainActivity.this, "Generic failure",
                        Toast.LENGTH_SHORT).show();
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                Toast.makeText(MainActivity.this, "No service",
                        Toast.LENGTH_SHORT).show();
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                Toast.makeText(MainActivity.this, "Null PDU",
                        Toast.LENGTH_SHORT).show();
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                Toast.makeText(getBaseContext(), "Radio off",
                        Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }, new IntentFilter(SENT));

    // ---when the SMS has been delivered---
    registerReceiver(new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            switch (getResultCode()) {
            case Activity.RESULT_OK:
                Toast.makeText(MainActivity.this, "SMS delivered",
                        Toast.LENGTH_SHORT).show();
                break;
            case Activity.RESULT_CANCELED:
                Toast.makeText(MainActivity.this, "SMS not delivered",
                        Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }, new IntentFilter(DELIVERED));
    	SmsManager sms = SmsManager.getDefault();
    	sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
   }

}
