package com.fitmap.webservice;

import com.google.api.client.util.Key;

import java.io.Serializable;

/**
 * This class represents a Place
 */
public class Place implements Serializable{
    @Key
    public String id;
    @Key
    public String name;
    @Key
    public String reference;
    @Key
    public String icon;
    @Key
    public String[] types;
    @Key
    public float rating;
    @Key
    public String vicinity;
    @Key
    public Geometry geometry;
    @Key
    public String formatted_address;
    @Key
    public String international_phone_number;
    @Key
    public String website;
    @Key
    public String url;

    @Override
    public String toString() {
        return name + " - " + id + " - " + reference;
    }

    public static class Geometry implements Serializable
    {
        @Key
        public Location location;

        @Override
        public String toString() {
            return location.lat + "," + location.lng;
        }

    }

    public static class Location implements Serializable
    {
        @Key
        public double lat;
        @Key
        public double lng;
    }

    public String getDistance(double lat1, double lng1) {
        double earthRadius = 6371; //kilometers
        double dLat = Math.toRadians(geometry.location.lat-lat1);
        double dLng = Math.toRadians(geometry.location.lng-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(geometry.location.lat)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float)(Math.round(earthRadius * c *100))/100;

        return String.valueOf(dist);
    }
}
