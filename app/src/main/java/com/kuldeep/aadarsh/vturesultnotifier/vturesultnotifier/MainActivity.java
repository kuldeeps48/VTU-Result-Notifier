package com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
ToDo: Implement About Us in Settings page with new Activity instead of alert dialog
 */

public class MainActivity extends AppCompatActivity {
    private EditText usn_edittext;
    private Button start;
    private Button stop;
    private String usn;

    public static boolean changeThemeFlag = false;
    public static boolean darkTheme = false;
    private FloatingActionButton floating_button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeTheme.onActivityCreateSetTheme(this);
        setContentView(R.layout.activity_main);


        // Don't tell anyone about this!
        final EditText changeThemeAction = (EditText) findViewById(R.id.usn_edittext);
        final Activity activity = this;
        changeThemeAction.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!darkTheme) {
                    ChangeTheme.changeTheme(activity);
                    darkTheme = true;
                    return true;
                }
                return false;
            }
        });

        start = (Button) findViewById(R.id.start_button);
        //Set button click listener on Start button
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get USN as string
                usn_edittext = (EditText) findViewById(R.id.usn_edittext);
                usn = usn_edittext.getText().toString();

                Pattern pattern = Pattern.compile("^[1-4]([A-Z]|[a-z]){2}\\d{2}([A-Z]|[a-z]){2}\\d{3}$");
                Matcher matcher = pattern.matcher(usn);

                if(matcher.find()) {
                    //Run service
                    Intent serviceIntent = new Intent(MainActivity.this, ResultCheckService.class);
                    serviceIntent.putExtra("USN", usn);
                    getApplicationContext().startService(serviceIntent);
                    Toast.makeText(MainActivity.this,R.string.service_started, Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MainActivity.this,R.string.proper_usn, Toast.LENGTH_SHORT).show();
                }

            }
        });

        stop = (Button) findViewById(R.id.stop_button);
        //Set button click listener on Stop button
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, ResultCheckService.class));
                Toast.makeText(MainActivity.this,R.string.service_stopped, Toast.LENGTH_SHORT).show();
            }
        });

        floating_button = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floating_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ResultDisplay.class);
                startActivity(intent);
                Log.i("MainActivity", "Showed last stored result");
            }
        });

    }

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
            case R.id.action_settings:
                aboutUsDialog();

                /* For implementing Settings
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);*/

                break;
            default:
                break;
        }

        return true;
    }
}
