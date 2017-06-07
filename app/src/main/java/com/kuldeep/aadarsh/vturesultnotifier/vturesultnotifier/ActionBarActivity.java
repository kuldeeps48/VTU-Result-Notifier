package com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

/**
 * Created by aadarsha on 6/7/17.
 */

public class ActionBarActivity extends AppCompatActivity {

    // ActionBar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_action, menu);
        return true;
    }

    // About Us
    public void aboutUsDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater alert_layout = LayoutInflater.from(this);
        final View view = alert_layout.inflate(R.layout.about_us, null);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int value) {
                // Do nothing, go back to main UI
            }
        });
        alertDialogBuilder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Settings
            case R.id.action_settings:
                aboutUsDialog();

                /* For implementing Settings
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);*/

                break;

            // Send feedback
            case R.id.action_send_feedback:
                String mailto = "mailto:the.era.labs@gmail.com" +
                        "?subject=" + Uri.encode("Feedback on VTU Result") +
                        "&body=" + Uri.encode("Hi there,\n I used your app and ");

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse(mailto));

                try {
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Please install an Email app in order to send feedback.", Toast.LENGTH_LONG).show();
                }
                break;

            default:
                break;
        }

        return true;
    }
}
