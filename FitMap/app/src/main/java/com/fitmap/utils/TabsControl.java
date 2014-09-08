package com.fitmap.utils;


import android.app.Activity;
import android.view.View;
import android.widget.RadioButton;

import com.fitmap.activites.AccountActivity;
import com.fitmap.activites.ExerciseTrackActivity;
import com.fitmap.activites.dailyRun.NewRunActivity;
import com.fitmap.activites.nearByDrink.NearbyDrinkActivity;
import com.fitmap.R;

/**
 * This class controls the navigation of tabs and their appearance.
 */
public class TabsControl {
    public static void controlTabs(Activity activity) {
        View tab_view = activity.findViewById(R.id.navigation_bar);
        RadioButton daily_run_tab = (RadioButton) tab_view.findViewById(R.id.main_tab_daily_run);
        final Activity a = activity;
        daily_run_tab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.dailyRun(a);
            }
        });

        RadioButton exercise_track_tab = (RadioButton) tab_view.findViewById(R.id.main_tab_exercise_track);
        exercise_track_tab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.exerciseTrack(a);
            }
        });

        RadioButton nearby_drink_tab = (RadioButton) tab_view.findViewById(R.id.main_tab_nearby_drink);
        nearby_drink_tab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.nearbyDrink(a);
            }
        });

        RadioButton account_tab = (RadioButton) tab_view.findViewById(R.id.main_tab_account);
        account_tab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.gotoAccount(a);
            }
        });

        if (activity.getClass() == NewRunActivity.class){
            daily_run_tab.setChecked(true);
            daily_run_tab.setEnabled(false);
            exercise_track_tab.setEnabled(true);
            nearby_drink_tab.setEnabled(true);
            account_tab.setEnabled(true);
        }
        else if (activity.getClass() == ExerciseTrackActivity.class) {
            exercise_track_tab.setChecked(true);
            daily_run_tab.setEnabled(true);
            exercise_track_tab.setEnabled(false);
            nearby_drink_tab.setEnabled(true);
            account_tab.setEnabled(true);
        } else if (activity.getClass() == NearbyDrinkActivity.class) {
            nearby_drink_tab.setChecked(true);
            daily_run_tab.setEnabled(true);
            exercise_track_tab.setEnabled(true);
            nearby_drink_tab.setEnabled(false);
            account_tab.setEnabled(true);
        } else if (activity.getClass() == AccountActivity.class) {
            account_tab.setChecked(true);
            daily_run_tab.setEnabled(true);
            exercise_track_tab.setEnabled(true);
            nearby_drink_tab.setEnabled(true);
            account_tab.setEnabled(false);
        }
    }

}
