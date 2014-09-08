package com.fitmap.webservice;

import com.google.api.client.util.Key;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents a list of Places.
 */
public class PlacesList implements Serializable{
    @Key
    public String status;
    @Key
    public List<Place> results;
}
