package com.fitmap.activites;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
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

/**
 * Log in activity
 */
public class MainActivity extends Activity {
    private EditText inputName, inputPassword;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = this.getSharedPreferences(Navigation.settingFile, Context.MODE_PRIVATE);

        if (settings.getBoolean(Navigation.TAG_REMEMBER,false))
            Navigation.login(this);

        //for test use, hard code a user
        if (!settings.contains("fitmap")){
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("fitmap", "fitmap");
            editor.putFloat("fitmap_weight", 30);
            editor.commit();
        }

        inputName = (EditText)findViewById(R.id.signin_userName);
        inputPassword = (EditText)findViewById(R.id.signin_password);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getActionBar();
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                Gravity.CENTER | Gravity.CENTER_VERTICAL);
        View customNav = LayoutInflater.from(this).inflate(R.layout.header, null); // layout which contains your button.
        actionBar.setCustomView(customNav, lp);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        return true;
    }

    /**
     * Sign in to home page
     * @param v view
     */
    public void signIn(View v){
        if (!valid())
            return;
        Navigation.login(this);
    }

    /**
     * Sign Up
     * @param v view
     */
    public void signUp(View v) {
        Navigation.signUp(this);
    }

    /**
     * Check the validation of input
     * @return
     */
    private boolean valid(){
        String name = inputName.getText().toString();
        String password = inputPassword.getText().toString();

        //if password or usrename is not right
        if (! password.equals(settings.getString(name, null))) {
            Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
            return false;
        }
        //remember the user
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Navigation.TAG_REMEMBER, true);
        editor.putString(Navigation.TAG_USER, name);
        editor.commit();
        return true;
    }

}
