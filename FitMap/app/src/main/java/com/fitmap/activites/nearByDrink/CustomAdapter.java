package com.fitmap.activites.nearByDrink;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.fitmap.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is a custom adapter for the layout of
 * {@link com.fitmap.activites.nearByDrink.NearbyDrinkActivity}.
 */
public class CustomAdapter extends BaseAdapter{
    public static String KEY_REFERENCE = "reference";
    public static String KEY_NAME = "name";
    public static String KEY_RATING = "rating";
    public static String KEY_DISTANCE = "distance";
    public static String KEY_STYLE = "style";
    public static String KEY_LOCATION = "location";

    private ArrayList<HashMap<String, String>> placesListItems;
    private final int resource;

    public CustomAdapter(ArrayList<HashMap<String, String>> placesListItems, int resource) {
        this.placesListItems = placesListItems;
        this.resource = resource;
    }

    @Override
    public int getCount() {
        return placesListItems.size();
    }

    @Override
    public Object getItem(int position) {
        return placesListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            itemView = inflater.inflate(resource, null);
        }

        TextView reference = (TextView) itemView.findViewById(R.id.reference);
        TextView location = (TextView) itemView.findViewById(R.id.location);
        TextView placeName = (TextView) itemView.findViewById(R.id.place_name);
        TextView rating = (TextView) itemView.findViewById(R.id.rating);
        ImageView image = (ImageView) itemView.findViewById(R.id.icon);
        RatingBar ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBar);
        TextView distance = (TextView) itemView.findViewById(R.id.distance);

        reference.setText(placesListItems.get(position).get(KEY_REFERENCE));
        location.setText(placesListItems.get(position).get(KEY_LOCATION));
        placeName.setText(placesListItems.get(position).get(KEY_NAME));

        String s = placesListItems.get(position).get(KEY_STYLE);

        if (s.equals("cafe")) {
            image.setImageResource(R.drawable.cafe);
        } else if (s.equals("grocery_or_supermarket")) {
            image.setImageResource(R.drawable.grocery_or_supermarket);
        } else if (s.equals("restaurant") || s.equals("subway_station")) {
            image.setImageResource(R.drawable.restaurant);
        } else if (s.equals("bar")) {
            image.setImageResource(R.drawable.bar);
        }
        image.setVisibility(View.VISIBLE);

        rating.setText(placesListItems.get(position).get(KEY_RATING));
        ratingBar.setRating(Float.parseFloat(placesListItems.get(position).get(KEY_RATING)));
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(parent.getResources().getColor(R.color.green2), PorterDuff.Mode.SRC_ATOP);
        ratingBar.setVisibility(View.VISIBLE);
        ratingBar.setClickable(false);
        ratingBar.setFocusable(false);
        distance.setText(placesListItems.get(position).get(KEY_DISTANCE) + "mi");
        return itemView;
    }
}
