package com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private EditText usn_edittext;
    private Button start;
    private Button stop;
    private String usn;
    public static boolean longPress = false;
    private static boolean darkTheme = false;


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

    }
}
