package com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

<<<<<<< HEAD
public class ResultDisplay extends AppCompatActivity {
>>>>>>> 4e1d5cdb0b39471ba89ba889cf74227afeee3c9c
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

        } catch (FileNotFoundException e) {
            Log.i("ResultDisplay", e.toString());
        } catch (IOException e) {
            Log.i("ResultDisplay", e.toString());
        }

        String contents = stringBuilder.toString();
        Log.i("ResultDisplay", contents);
        display = (WebView) findViewById(R.id.webview);
        //Display String in WebView
        display.loadDataWithBaseURL(null, contents, "text/html", "utf-8", null);
    }
}
