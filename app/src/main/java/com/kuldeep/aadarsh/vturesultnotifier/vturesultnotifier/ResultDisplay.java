package com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

public class ResultDisplay extends AppCompatActivity {
    private String html;
    private WebView display;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_display);

        html = getIntent().getStringExtra("html_data");
        Log.i("ResultDisplay", html);
        display = (WebView) findViewById(R.id.webview);
        display.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
    }
}
