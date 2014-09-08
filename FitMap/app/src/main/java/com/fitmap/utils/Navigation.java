package com.fitmap.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.fitmap.activites.AccountActivity;
import com.fitmap.activites.ExerciseTrackActivity;
import com.fitmap.activites.FitBitActivity;
import com.fitmap.activites.MainActivity;
import com.fitmap.activites.SignUpActivity;
import com.fitmap.activites.nearByDrink.NearbyDrinkActivity;
import com.fitmap.activites.dailyRun.NewRunActivity;
import com.fitmap.R;

/**
 * This class helps navigate from one activity to another activity.
 */
public class Navigation {
    public static final String settingFile = "settings";
    public static final String TAG_REMEMBER = "remember";
    public static final String TAG_USER = "current_user";

    /**
     * Navigates to the {@link com.fitmap.activites.dailyRun.NewRunActivity}
     * @param activity  current application context
     */
    public static void dailyRun(Activity activity) {
        Intent intent = new Intent(activity, NewRunActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        activity.finish();
    }

    /**
     * Navigates to the {@link com.fitmap.activites.ExerciseTrackActivity}
     * @param activity  current application context
     */
    public static void exerciseTrack(Activity activity) {
        Intent intent = new Intent(activity, ExerciseTrackActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        activity.finish();
    }

    /**
     * Navigates to the {@link com.fitmap.activites.nearByDrink.NearbyDrinkActivity}
     * @param activity  current application context
     */
    public static void nearbyDrink(Activity activity) {
        Intent intent = new Intent(activity, NearbyDrinkActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        activity.finish();
    }

    /**
     * Navigates to the {@link com.fitmap.activites.AccountActivity}
     * @param activity  current application context
     */
    public static void gotoAccount(Activity activity) {
        Intent intent = new Intent(activity, AccountActivity.class);

        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        activity.finish();
    }

    /**
     * Log out. Navigates to the {@link com.fitmap.activites.MainActivity}
     * @param activity  current application context
     */
    public static void logout(Activity activity) {
        SharedPreferences settings = activity.getSharedPreferences(settingFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(TAG_REMEMBER, false);
        editor.putString(TAG_USER, null);
        editor.commit();

        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        activity.finish();
    }

    /**
     * Log in. Navigates to the {@link com.fitmap.activites.dailyRun.NewRunActivity}
     * @param activity  current application context
     */
    public static void login(Activity activity){
        Intent intent = new Intent(activity, NewRunActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.startActivity(intent);
        activity.finish();
    }

    /**
     * Sign up. Navigates to the {@link com.fitmap.activites.SignUpActivity}
     * @param activity  current application context
     */
    public static void signUp(Activity activity){
        Intent intent = new Intent(activity, SignUpActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * Sign into Fitbit account. Navigates to the {@link com.fitmap.activites.FitBitActivity}
     * @param activity  current application context
     */
    public static void signInFitbit(Activity activity) {
        Intent intent = new Intent(activity, FitBitActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
