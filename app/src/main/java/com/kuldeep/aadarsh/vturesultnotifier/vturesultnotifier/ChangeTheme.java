package com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import static com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier.MainActivity.longPress;

public class ChangeTheme {
        // Kill activity and start new activity
        public static void changeTheme(Activity activity) {
            activity.finish();
            longPress = true;
            activity.startActivity(new Intent(activity, activity.getClass()));
    }

    // Set theme when new activity is started
    public static void onActivityCreateSetTheme(Activity activity) {
        if (longPress) {
            activity.setTheme(R.style.DarkAppTheme);
            Toast.makeText(activity.getApplicationContext(), "Welcome to the Dark Side!", Toast.LENGTH_LONG).show();
        }
        else {
            activity.setTheme(R.style.AppTheme);
        }
    }
}
