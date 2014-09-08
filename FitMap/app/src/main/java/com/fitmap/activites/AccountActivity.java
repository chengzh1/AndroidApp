package com.fitmap.activites;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import com.fitmap.R;
import com.fitmap.activites.nearByDrink.NearbyDrinkActivity;
import com.fitmap.utils.Navigation;
import com.fitmap.utils.TabsControl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link android.preference.PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
@SuppressWarnings("deprecation")
public class AccountActivity extends PreferenceActivity {
    public String TAG = "Account Activity";
    public static float DEFAULT_WEIGHT = 0;
    public static String FITBIT_ACCESS = "fitbit_account_access";
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    private static SharedPreferences settings;
    CheckBoxPreference fitbit;
    Activity activity;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        setContentView(R.layout.activity_account);

        TabsControl.controlTabs(this);
        Log.w(TAG, "activity created");

        addPreferencesFromResource(R.xml.pref_blank);

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_general);
        getPreferenceScreen().addPreference(fakeHeader);
        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        // Add 'notifications' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_manage_accounts);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_accounts_management);

        // Add 'data and sync' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_about);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_about);

        settings = getSharedPreferences(Navigation.settingFile, Context.MODE_PRIVATE);
        activity = this;

        setInitialPrefValues();

        final String user = settings.getString(Navigation.TAG_USER, "");
        fitbit = (CheckBoxPreference) getPreferenceScreen().findPreference("Fitbit");
        fitbit.setChecked(settings.getBoolean(FITBIT_ACCESS, false));

        fitbit.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                CheckBoxPreference checkBox = (CheckBoxPreference)preference;
                if (checkBox.isChecked()) {
                    checkBox.setChecked(false);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(FITBIT_ACCESS, false);
                    editor.remove(FitBitActivity.TAG_AUTH);
                    editor.remove(FitBitActivity.TAG_AUTHKEY);
                    editor.remove(FitBitActivity.TAG_USERID);
                    editor.commit();
                } else {
                    Navigation.signInFitbit(activity);
                }
                return true;
            }
        });

        Preference dialogPreference = (Preference) getPreferenceScreen().findPreference("dialog_preference");
        dialogPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(preference.getContext());
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.term_of_use, null);
                WebView wb = (WebView) view.findViewById(R.id.term_of_use_webView);
                wb.loadUrl("file:///android_asset/TermOfUsePage.html");
                builder.setView(view)
                       .setTitle(R.string.pref_title_term_of_use);
                builder.create().show();
                return true;
            }
        });

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.

        bindPreferenceSummaryToValue(findPreference("user_name"));
        bindPreferenceSummaryToValue(findPreference("password"));
        bindPreferenceSummaryToValue(findPreference("weight"));
    }

    /*
     * Initialize the values for preference views according to the values
     * stored in sharedPreference.
     */
    private void setInitialPrefValues() {
        EditTextPreference namePref = (EditTextPreference) getPreferenceScreen().findPreference("user_name");
        String user = settings.getString(Navigation.TAG_USER, "");
        namePref.setText(user);
        namePref.setSummary(user);

        EditTextPreference pwPref = (EditTextPreference) getPreferenceScreen().findPreference("password");
        pwPref.setText(settings.getString(user, ""));
        pwPref.setSummary(settings.getString(user, ""));

        EditTextPreference weightPref = (EditTextPreference) getPreferenceScreen().findPreference("weight");
        String weight = settings.getFloat(user + "_weight", DEFAULT_WEIGHT) + "";
        weightPref.setText(weight);
        weightPref.setSummary(weight);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.log_out:
                Navigation.logout(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /** {@inheritDoc} */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link android.preference.PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /** {@inheritDoc} */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
                SharedPreferences.Editor editor = settings.edit();
                String user = settings.getString(Navigation.TAG_USER, "");
                if (preference.getKey().equals("user_name")) {
                    editor.putString(Navigation.TAG_USER, stringValue);
                    Set<String> users = settings.getStringSet(SignUpActivity.ALL_USERS, new HashSet<String>());
                    users.remove(user);
                    users.add(stringValue);
                    String pw = settings.getString(user, "");
                    editor.putString(stringValue, pw);
                    editor.putFloat(stringValue + "_weight", settings.getFloat(user + "_weight", DEFAULT_WEIGHT));
                    editor.putStringSet(SignUpActivity.ALL_USERS, users);
                    editor.putBoolean(stringValue + FITBIT_ACCESS, settings.getBoolean(user + FITBIT_ACCESS, false));
                    editor.remove(user);
                    editor.remove(user + "_weight");
                    editor.remove(user + FITBIT_ACCESS);
                } else if (preference.getKey().equals("password")) {
                    editor.putString(user, stringValue);
                } else if (preference.getKey().equals("weight")) {
                    editor.putFloat(user + "_weight", Float.valueOf(stringValue));
                }
                editor.commit();
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), "")
        );

    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("user_name"));
            bindPreferenceSummaryToValue(findPreference("password"));
            bindPreferenceSummaryToValue(findPreference("weight"));

        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_accounts_management);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_about);
        }
    }
}
