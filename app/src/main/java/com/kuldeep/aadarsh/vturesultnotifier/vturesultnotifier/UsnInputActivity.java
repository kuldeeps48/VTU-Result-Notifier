package com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class UsnInputActivity extends ActionBarActivity {
    private EditText usn_edittext;
    private Button start;
    private Spinner cbcs_semester;
    private String usn, sem;
    private String base_url, url;
    public static final String PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usn_input_v2);
        //To recieve messages from service and check if we should change Button text Immediately
        IntentFilter filter = new IntentFilter("com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier.ButtonText");
        this.registerReceiver(new Receiver(), filter);

        // Check and update button status
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean checkingResult = settings.getBoolean("checkingResult", false);
        start = (Button) findViewById(R.id.start_button);
        if (checkingResult) {
            start.setText(R.string.stop_button);
        }

        // Get result type and initialize UsnInputActivity
        cbcs_semester = (Spinner) findViewById(R.id.select_cbcs_semester);
        TextView header = (TextView) findViewById(R.id.text_view_header);
        String result_type = getIntent().getStringExtra("RESULT_TYPE");
        String headerText = result_type + " RESULT";
        header.setText(headerText);
        switch (result_type) {
            case "CBCS":
                cbcs_semester.setVisibility(View.VISIBLE);
                String[] items = new String[]{"Sem 1", "Sem 2", "Sem 3", "Sem 4", "Sem 5", "Sem 6",
                        "Sem 7", "Sem 8"};
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
                cbcs_semester.setAdapter(adapter);
                base_url = "http://result.vtu.ac.in/cbcs_results2017.aspx?usn=";
                break;

            case "OLD SCHEME":
                cbcs_semester.setVisibility(View.INVISIBLE);
                base_url = "http://results.vtu.ac.in/results/result_page.php?usn=";
                break;

            case "REVALUATION":
                cbcs_semester.setVisibility(View.INVISIBLE);
                base_url = "http://results.vtu.ac.in/reval_results/result_page.php?usn=";
                break;
        }

        //Set button click listener on Start button
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Hide Keyboard
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);


                // Check button state
                if (start.getText() == getResources().getString(R.string.start_button)) {

                    // Get USN as string
                    usn_edittext = (EditText) findViewById(R.id.usn_edittext);
                    usn = usn_edittext.getText().toString();

                    Pattern pattern = Pattern.compile("^[1-4]([A-Z]|[a-z]){2}\\d{2}([A-Z]|[a-z]){2}\\d{3}$");
                    Matcher matcher = pattern.matcher(usn);

                    if (matcher.find()) {
                        // Change Button text
                        start.setText(R.string.stop_button);

                        // Format result; especially required for CBCS results
                        // Get semester value
                        if (cbcs_semester.isShown()) {
                            sem = cbcs_semester.getSelectedItem().toString();
                            url = base_url + usn + "&sem=" + sem.substring(4);
                        } else {
                            url = base_url + usn;
                        }

                        //Run service
                        Intent serviceIntent = new Intent(UsnInputActivity.this, ResultCheckService.class);
                        serviceIntent.putExtra("RESULT_PAGE_URL", url);
                        getApplicationContext().startService(serviceIntent);

                        dialogOnServiceStart();

                    } else {
                        Toast.makeText(UsnInputActivity.this, R.string.proper_usn, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Change button text
                    start.setText(R.string.start_button);

                    // Stop service
                    stopService(new Intent(UsnInputActivity.this, ResultCheckService.class));
                    Toast.makeText(UsnInputActivity.this, R.string.service_stopped, Toast.LENGTH_SHORT).show();
                }


            }
        });

        FloatingActionButton floating_button = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floating_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsnInputActivity.this, ResultDisplay.class);
                startActivity(intent);
                Log.i("UsnInputActivity", "Showed last stored result");
            }
        });

        //Bottom Banner Ad
        AdView adView = (AdView) findViewById(R.id.adViewBottom);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    // Service started dialog
    public void dialogOnServiceStart() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater alert_layout = LayoutInflater.from(this);
        final View view = alert_layout.inflate(R.layout.dialog_on_service_start, null);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int value) {
                // Do nothing, go back to main UI
            }
        });
        alertDialogBuilder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check and update button status
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean checkingResult = settings.getBoolean("checkingResult", false);
        start = (Button) findViewById(R.id.start_button);
        if (checkingResult) {
            start.setText(R.string.stop_button);
        }
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // Check and update button status
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            boolean checkingResult = settings.getBoolean("checkingResult", false);
            start = (Button) findViewById(R.id.start_button);
            if (checkingResult) {
                start.setText(R.string.stop_button);
            }
            else
                start.setText(R.string.start_button);
        }
    }
}


