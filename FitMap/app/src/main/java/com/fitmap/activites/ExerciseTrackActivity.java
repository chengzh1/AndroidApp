package com.fitmap.activites;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.fitmap.R;
import com.fitmap.utils.Navigation;
import com.fitmap.utils.TabsControl;
import com.fitmap.utils.TimeHelper;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;

/**
 * Activity for Exercise Track Module
 */
public class ExerciseTrackActivity extends Activity {
    public String TAG = "Exercise Track Activity";
    private ProgressDialog pDialog;
    private TextView textView1, textView2;
    private WebView webView1, webView2;

    Context context = this;
    static String finalAuthToken;
    static String finalAuthTokenSecrate;
    static String finalEncodedUserID;

    static String[] weekDay;
    static double[] distances_run;
    static double[] distances_goal;
    static double[] calorie_consume;
    static double[] calorie_goal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_track);

        TabsControl.controlTabs(this);
        Log.w(TAG, "activity created");
        textView1 = (TextView)findViewById(R.id.exercise_track_textView1);
        textView2 = (TextView)findViewById(R.id.exercise_track_textView2);

        SharedPreferences settings = getSharedPreferences(Navigation.settingFile, Context.MODE_PRIVATE);
        boolean fibitAccess = settings.getBoolean(AccountActivity.FITBIT_ACCESS, false);
        //if not connect to fitbit, do nothing
        if (!fibitAccess){
            textView1.setText("Please connect to Fitbit account");
            return;
        }

        webView1 = (WebView) findViewById(R.id.exercise_track_webView1);
        webView2 = (WebView) findViewById(R.id.exercise_track_webView2);

        new RequestData().execute();
    }

    /**
     * Use Google Chart to draw line chart (distance and goals)
     * @param webView webview for loading chart
     * @param data  data of distance and goals
     */
    private void getLineChartView(WebView webView, String data){
        String content =
            "<html>" +
             "<head>" +
              "<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>" +
              "<script type=\"text/javascript\">" +
                "var dataArray = " + data + ";" +
                "google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});" +
                "google.setOnLoadCallback(drawChart);" +
                "function drawChart() {" +
                "var data = google.visualization.arrayToDataTable(dataArray);" +
                "var options = {" +
                                "vAxis: {title: 'Calorie'," +
                                        "titleTextStyle: {fontName: 'Times-Roman'," +
                                                         "fontSize: 30," +
                                                         "bold: true," +
                                                         "italic: true," +
                                                         "color: '#871b47'}," +
                                         "textStyle: {fontSize: 25, bold: true}" +
                                        "}," +
                                "hAxis: {title: 'Date'," +
                                        "titleTextStyle: {fontName: 'Times-Roman'," +
                                                         "fontSize: 30," +
                                                         "bold: true," +
                                                         "italic: true," +
                                                         "color: '#871b47'}," +
                                        "textStyle: {fontSize: 25, bold: true}" +
                                        "}," +
                                "chartArea:{width:'75%'}," +
                                "legend: {position: 'top', textStyle: {fontSize: 30, bold: true}}" +
                                "};" +
                "var chart = new google.visualization.AreaChart(document.getElementById('chart_div'));" +
                "chart.draw(data, options);" +
              "}" +
             "</script>" +
            "</head>" +
            "<body>" +
                "<div id=\"chart_div\" style=\"height: 600px;\"></div>" +
             "</body>" +
             "</html>";

        //Load webview
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.loadDataWithBaseURL( "file:///android_asset/", content, "text/html", "utf-8", null );
        final ProgressDialog mProgress = ProgressDialog.show( this,null, "Loading Image...",true,true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            // when finish loading page
            public void onPageFinished(WebView view, String url) {
                textView1.setText("Calorie Track");
                if(mProgress.isShowing()) {
                    mProgress.dismiss();
                }
            }
        });
    }

    /**
     * Use google chart to draw column chart (calorie and goal)
     * @param webView webview for loading chart
     * @param data  data of calorie and goals
     */
    private void getColumnChartView(WebView webView, String data){
        String content =
                "<html>" +
                        "<head>" +
                        "<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>" +
                        "<script type=\"text/javascript\">" +
                        "var dataArray = " + data + ";" +
                        "google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});" +
                        "google.setOnLoadCallback(drawChart);" +
                        "function drawChart() {" +
                        "var data = google.visualization.arrayToDataTable(dataArray);" +
                        "var options = {" +
                                        "vAxis: {title: 'Distance in miles'," +
                                                "titleTextStyle: {fontName: 'Times-Roman'," +
                                                                 "fontSize: 30," +
                                                                 "bold: true," +
                                                                 "italic: true," +
                                                                 "color: '#871b47'}," +
                                                "textStyle: {fontSize: 25, bold: true}" +
                                                "}," +
                                        "hAxis: {title: 'Date'," +
                                                "titleTextStyle: {fontName: 'Times-Roman'," +
                                                                 "fontSize: 30," +
                                                                 "bold: true," +
                                                                 "italic: true," +
                                                                 "color: '#871b47'}," +
                                                "textStyle: {fontSize: 25, bold: true}" +
                                                "}," +
                                        "chartArea:{width:'75%'}," +
                                        "legend: {position: 'top', textStyle: {fontSize: 30, bold: true}}" +
                                        "};" +
                        "var chart = new google.visualization.AreaChart(document.getElementById('chart_div'));" +
                        "chart.draw(data, options);" +
                        "}" +
                        "</script>" +
                        "</head>" +
                        "<body>" +
                        "<div id=\"chart_div\" style=\"height: 600px;\"></div>" +
                        "</body>" +
                        "</html>";

        //load webview
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);

        webView.loadDataWithBaseURL( "file:///android_asset/", content, "text/html", "utf-8", null );
        final ProgressDialog mProgress = ProgressDialog.show( this,null, "Loading Image...",true,true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            // when finish loading page
            public void onPageFinished(WebView view, String url) {
                textView2.setText("Distance Track");
                if(mProgress.isShowing()) {
                    mProgress.dismiss();
                }
            }
        });
    }

    /**
     * AsyncTask for loading webview
     */
    class LoadWebView extends AsyncTask<String, String, String> {
        private String calorieData;
        private String distnaceData;
        private WebView webView1, webView2;

        public LoadWebView(WebView v1, String calorieData, WebView v2, String distnaceData){
            this.webView1 = v1;
            this.webView2 = v2;
            this.calorieData = calorieData;
            this.distnaceData = distnaceData;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Before starting background thread Show Progress Dialog
            pDialog = new ProgressDialog(ExerciseTrackActivity.this);
            pDialog.setMessage(Html.fromHtml("<b>Loading...</b>"));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            getLineChartView(webView1, calorieData);
            getColumnChartView(webView2, distnaceData);
            pDialog.dismiss();
        }
    }

    /**
     * Send Reqeust to FitBit webservice for the data need
     * @param date Date of the data
     * @return JSON data
     */
    private String requestActivity(String date){
        String consumerkey = FitBitActivity.consumerkey;
        String secretkey = FitBitActivity.secretkey;
        try {
            //send http request
            HttpResponse response;
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 20000);
            HttpConnectionParams.setSoTimeout(httpParameters, 20000);
            HttpClient client = new DefaultHttpClient(httpParameters);

            String timeStamp = String.valueOf(System.currentTimeMillis()/1000);

            String baseString =  "GET&http%3A%2F%2Fapi.fitbit.com%2F1%2Fuser%2F" + finalEncodedUserID + "%2Factivities%2Fdate%2F" + date + ".json&oauth_consumer_key%3D" + consumerkey + "%26oauth_nonce%3Dfitmap%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D" + timeStamp + "%26oauth_token%3D" + finalAuthToken + "%26oauth_version%3D1.0";

            Log.d("fitmap", "Request Data BaseString: " + baseString);
            String key = secretkey + "&" + finalAuthTokenSecrate;

            String signature = URLEncoder.encode(FitBitActivity.computeHmac(baseString, key), "UTF-8");

            //request url
            String url = "http://api.fitbit.com/1/user/" + finalEncodedUserID + "/activities/date/" + date + ".json";
            Log.d("fitmap", "Request Data url: " + url);
            HttpGet request = new HttpGet(url);
            //request header
            String accessToken = " OAuth oauth_consumer_key=\"" + consumerkey + "\", oauth_nonce=\"fitmap\", oauth_signature=\"" + signature + "\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"" + timeStamp + "\", oauth_token=\"" + finalAuthToken + "\", oauth_version=\"1.0\"";
            Log.d("fitmap", "request data token: " + accessToken);
            request.setHeader("Authorization", accessToken);
            //send request
            response = client.execute(request);

            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));
            //get reponse
            String result;
            while ((result = rd.readLine()) != null) {
           //     Log.d("fitmap", "Activity Result: " + result);
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Store result requested from FitBit WebService
     * @param day day of the requested day
     */
    private void setActivityResult(int day){
        String result = requestActivity(weekDay[day]);
        if (result == null){
            Log.e("fitmap", "Request result is null");
            return;
        }
        try {
            Log.d("fitmap", "Json Result: " + result);
            //parson json
            JSONObject json = new JSONObject(result);
            JSONObject summary = json.getJSONObject("summary");

            JSONArray distances = summary.getJSONArray("distances");
            for (int i = 0; i < distances.length(); i ++){
                JSONObject jsonObject = distances.getJSONObject(i);
                if (jsonObject.getString("activity").equals("total")){
                    distances_run[day] = jsonObject.getDouble("distance");
                    break;
                }
            }
            calorie_consume[day] = summary.getDouble("caloriesOut");


            JSONObject goals = json.getJSONObject("goals");
            distances_goal[day] = goals.getDouble("distance");
            calorie_goal[day] = goals.getDouble("caloriesOut");

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    /**
     * AsyncTask to request data from FitBit WebService
     */
    public class RequestData extends AsyncTask<String, Void, String> {

        ProgressDialog pd = null;

        @Override
        protected void onPreExecute() {
            //initialization
            pd = ProgressDialog.show(context, "Please wait",
                    "Loading please wait..", true);
            pd.setCancelable(true);
            weekDay = TimeHelper.weekDay();
            distances_goal = new double[7];
            distances_run = new double[7];
            calorie_consume = new double[7];
            calorie_goal = new double[7];
            SharedPreferences sharedPreferences = getSharedPreferences(Navigation.settingFile, Context.MODE_PRIVATE);
            finalAuthToken = sharedPreferences.getString(FitBitActivity.TAG_AUTH, null);
            finalAuthTokenSecrate = sharedPreferences.getString(FitBitActivity.TAG_AUTHKEY, null);
            finalEncodedUserID = sharedPreferences.getString(FitBitActivity.TAG_USERID, null);
        }

        @Override
        protected String doInBackground(String... params) {
            //get data from the previous seven days
            if (finalAuthToken == null || finalAuthTokenSecrate == null || finalEncodedUserID == null)
                return null;

            for (int i = 0; i < 7; i ++){
                setActivityResult(i);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            pd.dismiss();
            //failed to load data
            if (finalAuthToken == null || finalAuthTokenSecrate == null || finalEncodedUserID == null){
                textView1.setText("Sorry, failed to load data");
              //  String calorieData = "[['Day', 'Real', 'Goal'],['5/1',1000,400],['5/2',1170,460],['5/4',660,1120]]";
              //  String distanceData = "[['Day', 'Real', 'Goal'],['5/1',11,8],['5/2',12,10],['5/4',13,15]]";
                Log.d("fitmap", "null result");
            //    new LoadWebView(webView1, calorieData, webView2, distanceData).execute();
            }else {
                //successfully load data
                String calorieData = "[['Day', 'CalorieOut', 'Goal']";
                String distanceData = "[['Day', 'Distances', 'Goal']";
                for (int i = 0; i < 7; i++) {
                    distanceData += ",['" + weekDay[i + 7] + "'," + distances_run[i] + "," + distances_goal[i] + "]";
                    calorieData += ",['" + weekDay[i + 7] + "'," + calorie_consume[i] + "," + calorie_goal[i] + "]";
                }
                distanceData += "]";
                calorieData += "]";
                Log.d("fitmp", "distance data: " + distanceData);
                Log.d("fitmap", "calorie data: " + calorieData);
                new LoadWebView(webView1, calorieData, webView2, distanceData).execute();
            }
        }
    }

}
