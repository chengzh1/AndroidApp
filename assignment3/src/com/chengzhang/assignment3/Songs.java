package com.chengzhang.assignment3;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

//Activity to play songs
public class Songs extends Activity {
	private MediaPlayer mediaPlayer;
	private int playbackPosition=0;
	private String songName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_songs);
		//get song selection information from Main through intent
		Intent intent = getIntent();
		songName = intent.getStringExtra("songName");
		
		Button startPlayerBtn = (Button)findViewById(R.id.startPlayerBtn);
        Button pausePlayerBtn = (Button)findViewById(R.id.pausePlayerBtn);
        Button restartPlayerBtn = (Button)findViewById(R.id.restartPlayerBtn);
        // define click listener for start button
        startPlayerBtn.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View view){
		        try{
		        //	playLocalAudio();
		        	playLocalAudio_UsingDescriptor();
		        }catch (Exception e){
		        	e.printStackTrace();
		        }
        	}
        });
        // define click listener for pause button
	    pausePlayerBtn.setOnClickListener(new OnClickListener(){
	    	@Override
	     	public void onClick(View view){		        	
	    		if(mediaPlayer!=null){		        		
	    			playbackPosition = mediaPlayer.getCurrentPosition();		        		
	    			mediaPlayer.pause();
		        }
	        }
	    });
        // define click listener for restart button    	
	    restartPlayerBtn.setOnClickListener(new OnClickListener(){
	    	@Override
	        public void onClick(View view){		        	
	    		if(mediaPlayer!=null && !mediaPlayer.isPlaying()){		        		
	    			mediaPlayer.seekTo(playbackPosition);		        		
	    			mediaPlayer.start();
	    		}
		    }
	    });
	}

	@Override
	protected void onDestroy(){	       	
		super.onDestroy();	       	
		killMediaPlayer();
	}
	
	private void killMediaPlayer(){
	   	if(mediaPlayer!=null){
	       	try{
	       		mediaPlayer.release();
	        }
	        catch(Exception e){
	        	e.printStackTrace();
	        }
	    }
	 } 	
	// open song file and play it
	private void playLocalAudio_UsingDescriptor() throws Exception {
		AssetFileDescriptor fileDesc = null;
		// song selection according songName
		if(songName.equals("song1"))
			fileDesc = getResources().openRawResourceFd(R.raw.song1);
		else
			fileDesc = getResources().openRawResourceFd(R.raw.song2);
		if (fileDesc != null) {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setDataSource(fileDesc.getFileDescriptor(), fileDesc.getStartOffset(), fileDesc.getLength());
			fileDesc.close();
			mediaPlayer.prepare();
			mediaPlayer.start();
		}
	}
 }
