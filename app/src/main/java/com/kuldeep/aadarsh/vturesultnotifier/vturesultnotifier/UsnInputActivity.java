package com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
ToDo: Implement About Us in Settings page with new Activity instead of alert dialog
 */

public class UsnInputActivity extends ActionBarActivity {
    private EditText usn_edittext;
    private Button start;
    private Button stop;
    private Spinner cbcs_semester;
    private String usn, sem;
    private String base_url, url;

    public static boolean changeThemeFlag = false;
    public static boolean darkTheme = false;
    private FloatingActionButton floating_button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeTheme.onActivityCreateSetTheme(this);
        setContentView(R.layout.activity_usn_input);


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

        // Get result type and initialize UsnInputActivity
        cbcs_semester = (Spinner) findViewById(R.id.select_cbcs_semester);
        String result_type = getIntent().getStringExtra("RESULT_TYPE");
        switch (result_type) {
            case "CBCS":
                cbcs_semester.setVisibility(View.VISIBLE);
                String[] items = new String[]{"1", "2", "3"};
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
                cbcs_semester.setAdapter(adapter);
                base_url = "http://result.vtu.ac.in/cbcs_results2017.aspx?usn=";
                break;

            case "OLD":
                cbcs_semester.setVisibility(View.INVISIBLE);
                base_url = "http://results.vtu.ac.in/results/result_page.php?usn=";
                break;

            case "REVALUATION":
                cbcs_semester.setVisibility(View.INVISIBLE);
                base_url = "http://results.vtu.ac.in/reval_results/result_page.php?usn=";
                break;
        }

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
                    // Format result; especially required for CBCS results
                    // Get semester value
                    if (cbcs_semester.isShown()) {
                        sem = cbcs_semester.getSelectedItem().toString();
                        url = base_url + usn + "&sem=" + sem;
                    }
                    else {
                        url = base_url + usn;
                    }
                    //Run service
                    Intent serviceIntent = new Intent(UsnInputActivity.this, ResultCheckService.class);
                    serviceIntent.putExtra("RESULT_PAGE_URL", url);
                    getApplicationContext().startService(serviceIntent);
                    Toast.makeText(UsnInputActivity.this,R.string.service_started, Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(UsnInputActivity.this,R.string.proper_usn, Toast.LENGTH_SHORT).show();
                }

            }
        });

        stop = (Button) findViewById(R.id.stop_button);
        //Set button click listener on Stop button
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(UsnInputActivity.this, ResultCheckService.class));
                Toast.makeText(UsnInputActivity.this,R.string.service_stopped, Toast.LENGTH_SHORT).show();
            }
        });

        floating_button = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floating_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsnInputActivity.this, ResultDisplay.class);
                startActivity(intent);
                Log.i("UsnInputActivity", "Showed last stored result");
            }
        });
    }
}
