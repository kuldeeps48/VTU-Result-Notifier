package com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ResultDisplay extends ActionBarActivity {
    private WebView display;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_display);

        Context context = getApplicationContext();
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in = null;
        //Take HTML of webpage from file and store it as a string
        try {
            in = new BufferedReader(new FileReader(new File(context.getFilesDir(), "my-file-name.html")));
            while ((line = in.readLine()) != null)
                stringBuilder.append(line);

        } catch (IOException e) {
            Log.i("ResultDisplay", e.toString());
        }

        String contents = stringBuilder.toString();
        Log.i("ResultDisplay", contents);
        display = (WebView) findViewById(R.id.webview);

        // Configure WebView
        display.getSettings().setJavaScriptEnabled(true);
        display.getSettings().setLoadWithOverviewMode(true);
        display.getSettings().setUseWideViewPort(true);
        display.setScrollBarStyle(display.SCROLLBARS_OUTSIDE_OVERLAY);
        display.setScrollbarFadingEnabled(true);
        display.getSettings().setBuiltInZoomControls(true);
        display.getSettings().setSupportZoom(true);

        if (contents.contains("CGPA")) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        //Display String in WebView
        display.loadData(contents, "text/html", "utf-8");

    }

    @Override
    public void onBackPressed() {

        //Read rating status. If not shown show it
        Context context = getApplicationContext();
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(new File(context.getFilesDir(), "rate.txt")));
            while ((line = in.readLine()) != null)
                stringBuilder.append(line);

        } catch (IOException e) {
            Log.e("Rate Status", e.toString());
        }
        String rated = stringBuilder.toString();
        if(!rated.equals("yes")){
            //Show dialog
            rateDialog();
            //write "yes" in status file
            File p = getApplicationContext().getFilesDir();
            Log.i("Rate Status", p.toString());
            try {
                FileWriter out = new FileWriter(new File(p, "rate.txt"));
                out.write("yes");
                out.close();
            } catch (IOException e) {
                Log.e("Rate Status", e.toString());
            }
        }
        else {
            super.onBackPressed();
        }
    }

    public void rateDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater alert_layout = LayoutInflater.from(this);
        final View view = alert_layout.inflate(R.layout.rate_dialog, null);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setNeutralButton("Rate", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int value) {
                // open Link to app in play store
            }
        });
        alertDialogBuilder.show();
    }
}
