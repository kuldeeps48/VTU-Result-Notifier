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
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class UsnInputActivity extends ActionBarActivity {
    private AutoCompleteTextView usn_edittext;
    private Button start;
    private Spinner cbcs_semester;
    private String usn, sem;
    private String base_url, url;
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String USN_HISTORY = "UsnHistory";
    private SharedPreferences settings;
    private SharedPreferences history;
    private Set<String> EnteredUSN;
    private final String TAG = "USNINPUTACtivity";
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usn_input_v2);

        // Check and update button status
        Context context = getApplicationContext();
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(new File(context.getFilesDir(), "state.txt")));
            while ((line = in.readLine()) != null)
                stringBuilder.append(line);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
        String checkingRes = stringBuilder.toString();
        start = (Button) findViewById(R.id.start_button);
        if (checkingRes.equals("true")) {
            start.setText(R.string.stop_button);
        } else start.setText(R.string.start_button);

        //For autocomplete
        history = getSharedPreferences(USN_HISTORY, 0);
        EnteredUSN = history.getStringSet(USN_HISTORY, new HashSet<String>());

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

            case "CBCS REVALUATION":
                break;
        }

        //Suggestions when entering usn
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, EnteredUSN.toArray(
                new String[EnteredUSN.size()])
        );
        usn_edittext = (AutoCompleteTextView) findViewById(R.id.usn_edittext);
        usn_edittext.setAdapter(adapter);


        //Set button click listener on Start button
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //To recieve messages from service and check if we should change Button text Immediately
                IntentFilter filter = new IntentFilter("com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier.ButtonText");
                getApplicationContext().registerReceiver(new Receiver(), filter);

                //Hide Keyboard
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);


                // Check button state
                if (start.getText() == getResources().getString(R.string.start_button)) {

                    // Get USN as string
                    usn = usn_edittext.getText().toString();

                    // Check for USN validity. MCA USN pattern matches MBA USN.
                    Pattern patternUsnBe = Pattern.compile("^[1-4]([A-Z]|[a-z]){2}\\d{2}([A-Z]|[a-z]){2}\\d{3}$");
                    Matcher matcherUsnBe = patternUsnBe.matcher(usn);
                    Boolean usnBe = matcherUsnBe.find();

                    Pattern patternUsnMca = Pattern.compile("^[1-4]([A-Z]|[a-z]){2}\\d{2}([A-Z]|[a-z]){3}\\d{2}$");
                    Matcher matcherUsnMca = patternUsnMca.matcher(usn);
                    Boolean usnMca = matcherUsnMca.find();

                    if (usnBe || usnMca) {
                        // Change Button text
                        start.setText(R.string.stop_button);
                        addSearchInput(usn_edittext.getText().toString());
                        // Format result; especially required for CBCS results
                        // Get semester value
                        if (cbcs_semester.isShown()) {
                            sem = cbcs_semester.getSelectedItem().toString();

                            // Get UG or PG result, Booleans usnBe and usnMca used because matcher.find() not working here
                            if (usnBe) {
                                url = base_url + usn + "&sem=" + sem.substring(4) + "&prg=UG";
                            }
                            else if (usnMca) {
                                    url = base_url + usn + "&sem=" + sem.substring(4) + "&prg=UG";
                            }
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
                    boolean stopped = stopService(new Intent(UsnInputActivity.this, ResultCheckService.class));
                    while(!stopped){
                        Log.e(TAG, "Not cancelled!");
                        stopService(new Intent(UsnInputActivity.this, ResultCheckService.class));
                    }
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
        adView = (AdView) findViewById(R.id.adViewBottom);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void addSearchInput(String input) {
        if(!EnteredUSN.contains(input)){
            EnteredUSN.add(input);

        }
    }

    private void saveToHistory(){
        history = getSharedPreferences(USN_HISTORY, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = history.edit();
        editor.putStringSet(USN_HISTORY, EnteredUSN);
        editor.apply();
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
        Context context = getApplicationContext();
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(new File(context.getFilesDir(), "state.txt")));
            while ((line = in.readLine()) != null)
                stringBuilder.append(line);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
        String checkingRes = stringBuilder.toString();
        start = (Button) findViewById(R.id.start_button);
        if (checkingRes.equals("true")) {
            start.setText(R.string.stop_button);
        } else start.setText(R.string.start_button);

        if(adView != null)
            adView.resume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveToHistory();
        if(adView != null)
            adView.pause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(adView != null)
            adView.destroy();
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // Check and update button status
            Context context = getApplicationContext();
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            BufferedReader in;
            try {
                in = new BufferedReader(new FileReader(new File(context.getFilesDir(), "state.txt")));
                while ((line = in.readLine()) != null)
                    stringBuilder.append(line);
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
            String checkingRes = stringBuilder.toString();
            start = (Button) findViewById(R.id.start_button);
            if (checkingRes.equals("true")) {
                start.setText(R.string.stop_button);
            } else start.setText(R.string.start_button);
        }
    }
}


