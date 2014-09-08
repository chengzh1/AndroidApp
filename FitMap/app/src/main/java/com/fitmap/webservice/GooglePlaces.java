package com.fitmap.webservice;

import android.util.Log;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.jackson.JacksonFactory;

import java.io.IOException;

/**
 * This class helps connect to the Google Places API.
 */

@SuppressWarnings("deprecation")
public class GooglePlaces {
    /** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    // Google API key
    private static final String API_KEY = "AIzaSyCzdK0ybshl9vAtdnesRF3O-uial-8wtp4";

    // Google Places serach url's
    private static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
    private static final String PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?";

    private double latitude;
    private double longitude;
    private double radius;

    public PlacesList search(double latitude, double longitude, double radius, String types) throws Exception{
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        try {
            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            GenericUrl reqUrl = new GenericUrl(PLACES_SEARCH_URL);
            reqUrl.put("location", latitude + "," + longitude);
            reqUrl.put("radius", radius);
            if (types != null) {
                reqUrl.put("types", types);
            }
            reqUrl.put("sensor", "false");
            reqUrl.put("key", API_KEY);
            HttpRequest request = httpRequestFactory.buildGetRequest(reqUrl);
            PlacesList list = request.execute().parseAs(PlacesList.class);
            Log.d("Places status", "" + list.status);
            return list;
        } catch (HttpResponseException e) {
            Log.e("Error:", e.getMessage());
            throw e;
        }
    }

    public PlaceDetails getPlaceDetails(String reference) throws Exception {
        HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
        try {
            GenericUrl reqUrl = new GenericUrl(PLACES_DETAILS_URL);
            reqUrl.put("reference", reference);
            reqUrl.put("key", API_KEY);
            HttpRequest request = httpRequestFactory.buildGetRequest(reqUrl);
            PlaceDetails place = request.execute().parseAs(PlaceDetails.class);
            return place;
        } catch (HttpResponseException e) {
            Log.e("Error in Perform Details", e.getMessage());
            throw e;
        }
    }

    public static HttpRequestFactory createRequestFactory(final HttpTransport transport) {
        return transport.createRequestFactory(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
                GoogleHeaders headers = new GoogleHeaders();
                headers.setApplicationName("Carnegie Mellon University-FitMAP-1.0");
                request.setHeaders(headers);
                JsonHttpParser parser = new JsonHttpParser(new JacksonFactory());
                request.addParser(parser);
            }
        });
    }
}
