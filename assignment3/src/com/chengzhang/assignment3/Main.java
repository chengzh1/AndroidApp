package com.chengzhang.assignment3;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

//Main Activity
public class Main extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//set link to Website
		final TextView v1 = (TextView) findViewById(R.id.officeWebsiteAddress);
		  v1.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					String linkAddress = "http://www.wangleehom.com";
					link(linkAddress);       
				}
			});
		  
		final TextView v2 = (TextView) findViewById(R.id.networkAddress);
		  v2.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					String linkAddress = "https://www.facebook.com/leehom";
					link(linkAddress);       
				}
			}); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	// callback of song button
	public void songListen(View view){
		Intent intent = new Intent(this, Songs.class);
		//song selection according which button is pressed
		switch(view.getId()){
		case R.id.song1Button:
			intent.putExtra("songName", "song1");
			break;
		case R.id.song2Button:
			intent.putExtra("songName", "song2");
			break;
		default:
			break;
		}
		startActivity(intent);
	}
	// callback of video button
	public void videoWatch(View view){
		Intent intent = new Intent(this, Videos.class);
		//video selection according which button is pressed
		switch(view.getId()){
		case R.id.video1Button:
			intent.putExtra("videoName", "video1");
			break;
		case R.id.video2Button:
			intent.putExtra("videoName", "video2");
			break;
		default:
			break;
		}
		startActivity(intent);
	}
	//callback of send email button
	public void emailList(View view){
		Intent intent = new Intent(this, MailingList.class);
		startActivity(intent);	
	}
	//callback of wallpaper button
	public void wallpaperList(View view){
		Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
	    intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
	        new ComponentName(this, LiveWallpaperService.class));
	    startActivity(intent);
	}
	
	public void link(String address){
		Uri webpage = Uri.parse(address);
		Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
		startActivity(webIntent);
	}
}
