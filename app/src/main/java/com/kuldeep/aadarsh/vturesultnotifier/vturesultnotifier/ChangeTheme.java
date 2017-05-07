package com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import static com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier.MainActivity.changeThemeFlag;

public class ChangeTheme {
        // Kill activity and start new activity
        public static void changeTheme(Activity activity) {
            activity.finish();
            changeThemeFlag = true;
            activity.startActivity(new Intent(activity, activity.getClass()));
            Toast.makeText(activity.getApplicationContext(), "Welcome to the Dark Side!", Toast.LENGTH_LONG).show();
    }

    // Set theme when new activity is started
    public static void onActivityCreateSetTheme(Activity activity) {
        if (changeThemeFlag) {
            activity.setTheme(R.style.Theme_AppCompat);
        }
        else {
            activity.setTheme(R.style.Theme_AppCompat_DayNight);
        }
    }
}
