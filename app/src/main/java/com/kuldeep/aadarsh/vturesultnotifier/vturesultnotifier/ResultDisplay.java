package com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ResultDisplay extends AppCompatActivity {
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
            in = new BufferedReader(new FileReader(new File(context.getFilesDir(), "my-file-name.txt")));
            while ((line = in.readLine()) != null)
                stringBuilder.append(line);

        } catch (IOException e) {
            Log.i("ResultDisplay", e.toString());
        }

        String contents = stringBuilder.toString();
        Log.i("ResultDisplay", contents);
        display = (WebView) findViewById(R.id.webview);
        //Some settings for webview
        display.setInitialScale(5);
        display.getSettings().setJavaScriptEnabled(true);
        display.getSettings().setLoadWithOverviewMode(true);
        display.getSettings().setUseWideViewPort(true);
        display.setScrollBarStyle(display.SCROLLBARS_OUTSIDE_OVERLAY);
        display.setScrollbarFadingEnabled(true);

        if (contents.contains("CGPA")) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        //Display String in WebView
        display.loadData(contents, "text/html", "utf-8");

    }
}
