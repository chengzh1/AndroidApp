package com.fitmap.activites;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.fitmap.R;
import com.fitmap.utils.Navigation;

import java.util.HashSet;
import java.util.Set;

/**
 * Activity for sign up
 */
public class SignUpActivity extends Activity {

    EditText userName;
    EditText password;
    EditText weightText;
    SharedPreferences settings;

    public static String MISSING_INFO_ERROR = "Please enter your user name, password and weight first!";
    public static String INVALID_USER_NAME = "User name already exist!";
    public static String ALL_USERS = "all users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        userName = (EditText) findViewById(R.id.signup_userName);
        password = (EditText) findViewById(R.id.signup_password);
        weightText = (EditText) findViewById(R.id.signup_weight);
        settings = this.getSharedPreferences(Navigation.settingFile, Context.MODE_PRIVATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getActionBar();
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER | Gravity.CENTER_VERTICAL);
        View customNav = LayoutInflater.from(this).inflate(R.layout.signup_header, null); // layout which contains your button.
        actionBar.setCustomView(customNav, lp);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        return true;
    }

    /**
     * Check validation of input
     * @param view view
     */
    public void validate(View view) {
        String user_name = userName.getText().toString();
        String pw = password.getText().toString();
        String weight = weightText.getText().toString();
        if (user_name.equals("") || pw.equals("") || weight.equals("")) {
            Toast.makeText(this, MISSING_INFO_ERROR, Toast.LENGTH_SHORT).show();
            userName.setText("");
            password.setText("");
            return;
        }
        Set<String> users = settings.getStringSet(ALL_USERS, new HashSet<String>());
        for (String user : users) {
            if (user_name.equals(user)) {
                Toast.makeText(this, INVALID_USER_NAME, Toast.LENGTH_SHORT).show();
                userName.setText("");
                password.setText("");
                return;
            }
        }
        SharedPreferences.Editor editor = settings.edit();
        users.add(user_name);
        editor.putStringSet(ALL_USERS, users);
        editor.putString(Navigation.TAG_USER, user_name);
        editor.putString(user_name, pw);
        editor.putFloat(user_name + "_weight", Float.valueOf(weight));
        editor.putBoolean(AccountActivity.FITBIT_ACCESS, false);
        editor.commit();
        Toast.makeText(this, "Hi, " + user_name + ", You've successfully signed up!", Toast.LENGTH_SHORT).show();
        Navigation.gotoAccount(this);
    }

    /**
     * Reset input
     * @param view view
     */
    public void reset(View view) {
        userName.setText("");
        password.setText("");
        weightText.setText("");
    }
}
