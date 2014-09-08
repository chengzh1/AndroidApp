package com.fitmap.activites;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fitmap.R;
import com.fitmap.utils.Navigation;
import com.google.api.client.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


/**
 * Activity to use OAuth to connect to FitBit webserivce
 */
public class FitBitActivity extends Activity {
    public final static String TAG_AUTH = "finalAuthToken";
    public final static String TAG_AUTHKEY = "finalAuthTokenSecrate";
    public final static String TAG_USERID = "finalEncodedUserID";
    public final static String consumerkey = "01f910f7ebb549d5bb231f52b0649e4a";
    public final static String secretkey = "fa6188ecfbad41dd97e2dbf45ff9493b";

    Context context = this;
    ProgressDialog progressBar;
    WebView webView;
    static String finalAuthToken = "";
    static String finalAuthTokenSecrate = "";
    static String finalEncodedUserID = "";
    String authVerifer = "";

    String tempAccessToken = "";
    String tempAccessTokenSecret = "";

    @SuppressLint({ "NewApi", "NewApi", "NewApi" })
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fit_bit);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        webView = (WebView) findViewById(R.id.webview1);

        new TokenAccess().execute();

    }

    /**
     * Compute signature
     * @param baseString base string for computation
     * @param key   key for computation
     * @return  computed signature
     */
    public static String computeHmac(String baseString, String key)
    {
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secret = new SecretKeySpec(key.getBytes(), mac.getAlgorithm());
            mac.init(secret);

            byte[] digest = mac.doFinal(baseString.getBytes());
            return new String(Base64.encodeBase64(digest));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * Call to request tempAccessToken and tempAccessTokenSecret
     */
    private void login() {
        try {
            //send http post request
            HttpResponse response;
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 20000);
            HttpConnectionParams.setSoTimeout(httpParameters, 20000);
            HttpClient client = new DefaultHttpClient(httpParameters);

            String timeStamp = String.valueOf(System.currentTimeMillis()/1000);

            String baseString = "POST&https%3A%2F%2Fapi.fitbit.com%2Foauth%2Frequest_token&oauth_consumer_key%3D" + consumerkey + "%26oauth_nonce%3Dfitmap%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D"+ timeStamp + "%26oauth_version%3D1.0";
            String key = secretkey + "&";
            //get signature
            String signature = URLEncoder.encode(computeHmac(baseString, key), "UTF-8");

            //request token
            HttpPost request = new HttpPost("https://api.fitbit.com/oauth/request_token");
            String accessToken = " OAuth oauth_consumer_key=\"" + consumerkey + "\", oauth_nonce=\"fitmap\", oauth_signature=\"" + signature + "\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"" + timeStamp + "\", oauth_version=\"1.0\"";
            Log.d("fitmap", accessToken);
            request.setHeader("Authorization", accessToken);

            response = client.execute(request);

            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));
            //get response
            String webServiceInfo;
            while ((webServiceInfo = rd.readLine()) != null) {
                Log.d("fitmap"," ****Step 1***Webservice: " + webServiceInfo);
                String tokens[]=webServiceInfo.split("=");
                tempAccessToken = tokens[1].split("&")[0];
                tempAccessTokenSecret = tokens[2].split("&")[0];
                Log.d("fitmap", "Auth token: Temporary Access Token: " + tempAccessToken);
                Log.d("fitmap","Temporary Access Token Secret: " + tempAccessTokenSecret);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Call for user identification
     */
    private void openCallbackURL() {
        progressBar = new ProgressDialog(context);
        progressBar.setCancelable(true);
        progressBar.setMessage("Loading...");
        progressBar.show();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.getUrl();
                String finalToken = view.getUrl();
                Log.d("fitmap", "Final Whole Response:" + finalToken);

                if (progressBar.isShowing()) {
                    progressBar.dismiss();
                }
                //when url has called back, it will contain access token and verify id
                if (finalToken.contains("fitmap")) {
                    String[] tokens = finalToken.split("&");
                    authVerifer = tokens[2].split("=")[1];
                    Log.d("fitmap", "get auth verifer: " + authVerifer);
                    new FinalTokenAccess().execute();

                }
            }
        });

        //load webview
        String fitUrl = "https://www.fitbit.com/oauth/authorize?oauth_token="
                + tempAccessToken + "&display=touch";
        webView.loadUrl(fitUrl);

        webView.requestFocus(View.FOCUS_DOWN);
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!arg0.hasFocus()) {
                            arg0.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });

    }

    /**
     * Get Final authorized token
     */
    private void getFinalToken() {
        try {
            //set http post request
            HttpResponse response;
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 20000);
            HttpConnectionParams.setSoTimeout(httpParameters, 20000);
            HttpClient client = new DefaultHttpClient(httpParameters);

            String timeStamp = String.valueOf(System.currentTimeMillis()/1000);

            String baseString = "POST&https%3A%2F%2Fapi.fitbit.com%2Foauth%2Faccess_token&oauth_consumer_key%3D01f910f7ebb549d5bb231f52b0649e4a%26oauth_nonce%3Dfitmap%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D" + timeStamp + "%26oauth_token%3D" + tempAccessToken + "%26oauth_verifier%3D" + authVerifer + "%26oauth_version%3D1.0";
            Log.d("fitmap", "Final BaseString:" + baseString);
            String key = secretkey + "&" + tempAccessTokenSecret;

            String signature = URLEncoder.encode(computeHmac(baseString, key), "UTF-8");
            //request url
            HttpPost request = new HttpPost("https://api.fitbit.com/oauth/access_token");
            //header
            String accessToken = " OAuth oauth_consumer_key=\"" + consumerkey + "\", oauth_nonce=\"fitmap\", oauth_signature=\"" + signature + "\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"" + timeStamp + "\", oauth_token=\"" + tempAccessToken + "\", oauth_verifier=\"" + authVerifer + "\", oauth_version=\"1.0\"";
            Log.d("fitmap", "final access token: " + accessToken);
            request.setHeader("Authorization", accessToken);

            response = client.execute(request);

            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));
            //get response
            String webServiceInfo2;
            while ((webServiceInfo2 = rd.readLine()) != null) {
                Log.d("fitmap", "****Step 2***Webservice: " + webServiceInfo2
                        + "---Size:" + webServiceInfo2.length());
                finalAuthToken = webServiceInfo2.substring(12, 44);
                finalAuthTokenSecrate = webServiceInfo2.substring(64, 96);
                finalEncodedUserID = webServiceInfo2.substring(113, 119);
                Log.d("fitmap", "Final Auth token:Webservice Result: "
                        + finalAuthToken + "----" + finalAuthTokenSecrate
                        + "---" + finalEncodedUserID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * AsyncTask to request token
     */
    public class TokenAccess extends AsyncTask<String, Void, String> {

        ProgressDialog pd = null;

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(context, "Please wait",
                    "Loading please wait..", true);
            pd.setCancelable(true);

        }
        @Override
        protected String doInBackground(String... params) {
            login();
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            openCallbackURL();
            pd.dismiss();
        }
    }

    /**
     * Async task to get final token
     */
    public class FinalTokenAccess extends AsyncTask<String, Void, String> {

        ProgressDialog pd = null;

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(context, "Please wait",
                    "Loading please wait..", true);
            pd.setCancelable(true);

        }

        @Override
        protected String doInBackground(String... params) {
            getFinalToken();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            pd.dismiss();
            if (finalAuthToken.length() > 0 && finalAuthTokenSecrate.length() > 0 && finalEncodedUserID.length() > 0){
                SharedPreferences sharedPreferences = getSharedPreferences(Navigation.settingFile, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(TAG_AUTH, finalAuthToken);
                editor.putString(TAG_AUTHKEY, finalAuthTokenSecrate);
                editor.putString(TAG_USERID, finalEncodedUserID);
                editor.putBoolean(AccountActivity.FITBIT_ACCESS, true);
                editor.commit();
            }
            Intent intent = new Intent(FitBitActivity.this,
                    AccountActivity.class);
            startActivity(intent);
            finish();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }
}