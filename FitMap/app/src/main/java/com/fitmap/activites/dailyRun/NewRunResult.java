package com.fitmap.activites.dailyRun;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;
import com.facebook.widget.WebDialog;
import com.fitmap.R;
import com.fitmap.services.LocationService;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class NewRunResult extends Activity {
    public final static String TAG = "STARTTIME";
    public final static String NAME = "";
    public final static String CAPTION = "";
    public final static String DESCRIPTION = "";
    private TextView resultView;
    private  ImageView image;
    // Progress dialog
    private ProgressDialog pDialog;

    //facebook
    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
    private final String PENDING_ACTION_BUNDLE_KEY = "com.facebook.fitmap:PendingAction";
    private enum PendingAction {
        NONE,
        POST_PHOTO
    }
    PendingAction pendingAction = PendingAction.NONE;
    private String shareImageUrl;
    private Bitmap shareBitmap;

    private UiLifecycleHelper uiHelper;
    private LoginButton loginButton;
    private boolean canPresentShareDialogWithPhotos;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private ImageView shareButton;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_run_result);

        activity = this;

        //facebook
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);
        }

        canPresentShareDialogWithPhotos = FacebookDialog.canPresentShareDialog(this,
                FacebookDialog.ShareDialogFeature.PHOTOS);
        shareButton = (ImageView) findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performPublish(PendingAction.POST_PHOTO, canPresentShareDialogWithPhotos);
            }
        });

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
            }
        });

     //   resultView = (TextView)findViewById(R.id.new_run_result);
        image = (ImageView)findViewById(R.id.new_run_result_image);

        Intent intent = getIntent();
        String startTime = intent.getStringExtra(TAG);
        if (startTime != null) {
            String url = getGPSData(startTime);
            new LoadImage(url, image).execute();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);

        outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e("Activity", String.format("Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i("Activity", "Success!");
            }
        });
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            shareButton.setClickable(true);
        } else if (state.isClosed()) {
            shareButton.setClickable(false);
        }
        if (pendingAction != PendingAction.NONE &&
                (exception instanceof FacebookOperationCanceledException ||
                        exception instanceof FacebookAuthorizationException)) {
            new AlertDialog.Builder(this)
                    .setTitle("cancel")
                    .setMessage(R.string.permission_not_granted)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            pendingAction = PendingAction.NONE;
        } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
            handlePendingAction();
        }
    }

    private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            // Respond to session state changes, ex: updating the view
        }
    }

    private FacebookDialog.PhotoShareDialogBuilder createShareDialogBuilderForPhoto(Bitmap... photos) {
        return new FacebookDialog.PhotoShareDialogBuilder(this)
                .addPhotos(Arrays.asList(photos));
    }

    private void postPhoto() {
        if (shareImageUrl == null || shareBitmap == null) {
            Toast.makeText(this, "No image to share", Toast.LENGTH_SHORT).show();
            return;
        }
        if (canPresentShareDialogWithPhotos) {
            FacebookDialog shareDialog = createShareDialogBuilderForPhoto(shareBitmap).build();
//            FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
//                    .setName(NAME)
//                    .setCaption(CAPTION)
//                    .setDescription(DESCRIPTION)
//                    .setPicture(shareImageUrl)
//                    .setLink(shareImageUrl).build();
            uiHelper.trackPendingDialogCall(shareDialog.present());
            return;
        } else if (hasPublishPermission()) {
            publishFeedDialog();
        } else {
            pendingAction = PendingAction.POST_PHOTO;
        }
    }

    private boolean hasPublishPermission() {
        Session session = Session.getActiveSession();
        return session != null && session.getPermissions().contains("publish_actions");
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void performPublish(PendingAction action, boolean allowNoSession) {
        Session session = Session.getActiveSession();
        if (session != null) {
            pendingAction = action;
            if (hasPublishPermission()) {
                // We can do the action right away.
                handlePendingAction();
                return;
            } else if (session.isOpened()) {
                // We need to get new permissions, then complete the action when we get called back.
                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, PERMISSIONS));
                return;
            }
        }

        if (allowNoSession) {
            pendingAction = action;
            handlePendingAction();
        }
    }

    private void handlePendingAction() {
        PendingAction previouslyPendingAction = pendingAction;
        // These actions may re-set pendingAction if they are still pending, but we assume they
        // will succeed.
        pendingAction = PendingAction.NONE;

        switch (previouslyPendingAction) {
            case POST_PHOTO:
                postPhoto();
                break;
            case NONE:
                break;
        }
    }

    private void publishFeedDialog() {
        Bundle params = new Bundle();
        params.putString("name", NAME);
        params.putString("caption", CAPTION);
        params.putString("description", DESCRIPTION);
        params.putString("picture", shareImageUrl);
        params.putString("link", shareImageUrl);

        WebDialog feedDialog = (
                new WebDialog.FeedDialogBuilder(this,
                        Session.getActiveSession(),
                        params))
                .setOnCompleteListener(new WebDialog.OnCompleteListener() {

                    @Override
                    public void onComplete(Bundle values,
                                           FacebookException error) {
                        if (error == null) {
                            // When the story is posted, echo the success
                            // and the post Id.
                            final String postId = values.getString("post_id");
                            if (postId != null) {
                                Toast.makeText(activity,
                                        "Posted story, id: " + postId,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // User clicked the Cancel button
                                Toast.makeText(activity.getApplicationContext(),
                                        "Publish cancelled",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else if (error instanceof FacebookOperationCanceledException) {
                            // User clicked the "x" button
                            Toast.makeText(activity.getApplicationContext(),
                                    "Publish cancelled",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Generic, ex: network error
                            Toast.makeText(activity.getApplicationContext(),
                                    "Error posting story",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                })
                .build();
        feedDialog.show();
    }

    private String getGPSData(String mStartTime){
        SharedPreferences preferences = this.getSharedPreferences(LocationService.dataFile,
                Context.MODE_PRIVATE);
        String countKey = mStartTime + "_count";
        int count = preferences.getInt(countKey, 0);
        String text = "";
        String url = "https://maps.googleapis.com/maps/api/staticmap?size=512x512&sensor=false&path=color:0x0000ff%7Cweight:5";
        for (int i = 0; i < count; i ++){
            String latKey = mStartTime + "_lat_" + i;
            String lonKey = mStartTime + "_lon_" + i;
            float lat = preferences.getFloat(latKey, 0);
            float lon = preferences.getFloat(lonKey,0);
            text = text + "lat: " + lat + " lon: " + lon + "\n";
            url = url + "%7c"+ lat + "," + lon;
        }

        System.out.println(url);
        //https://maps.googleapis.com/maps/api/staticmap?size=512x512&sensor=false&path=color:0x0000ff%7Cweight:5%7c40.40265,-79.9114%7c40.397728,-79.91137%7c40.403286,-79.91105%7c40.431767,-79.92341%7c40.431767,-79.92341%7c40.431767,-79.92341%7c40.431767,-79.92341%7c40.431767,-79.92341%7c40.437355,-79.95443


        WebView webView = (WebView)findViewById(R.id.new_run_result_webview);
        webView.loadUrl(url);
        //LocationService.deleteWithTag(this, mStartTime);
        if (count == 0)
            return null;
        return url;

    }

    class LoadImage extends AsyncTask<String, String, String> {
        private String imageUrl;
        private ImageView imageView;
        private Bitmap bitmap;
        public LoadImage(String url, ImageView v){
            imageUrl = url;
            imageView = v;
            bitmap = null;
        }
        /*
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(NewRunResult.this);
            pDialog.setMessage(Html.fromHtml("<b>Loading...</b>"));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(imageUrl).getContent());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                shareImageUrl = imageUrl;
                shareBitmap = bitmap;
            }
            pDialog.dismiss();

        }
    }
}
