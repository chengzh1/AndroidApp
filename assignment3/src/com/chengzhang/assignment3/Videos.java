package com.chengzhang.assignment3;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

//Watch video activity
public class Videos extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);
        
        Intent intent = getIntent();
        String videoName = intent.getStringExtra("videoName");
        
        VideoView videoView = (VideoView)this.findViewById(R.id.videoView);
        MediaController mc = new MediaController(this);
        videoView.setMediaController(mc);
        
        String uriPath;
        //select video according to which button is pressed
        if (videoName.equals("video1"))
        	uriPath = "android.resource://com.chengzhang.assignment3/raw/video1";
        else
        	uriPath = "android.resource://com.chengzhang.assignment3/raw/video2";
        Uri uri = Uri.parse(uriPath);
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.start();
    }
}

