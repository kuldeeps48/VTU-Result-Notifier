package com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    private Button buttonOldScheme, buttonCbcsScheme, buttonRevaluation;
    private Intent intent;
    private RetrieveResultListService notification;
    private String resultNotificationTextColor, getResultNotificationTextColorTagAttribute;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, "ca-app-pub-9246444038362099~5041840565");


        buttonOldScheme = (Button) findViewById(R.id.button_old_scheme);
        buttonOldScheme.setOnClickListener(this);
        buttonCbcsScheme = (Button) findViewById(R.id.button_cbcs_scheme);
        buttonCbcsScheme.setOnClickListener(this);
        buttonRevaluation = (Button) findViewById(R.id.button_revaluation);
        buttonRevaluation.setOnClickListener(this);

        // Get result notifications
        WebView notificationWebView = (WebView) findViewById(R.id.webview);
        // Specify text and background color for notification web view
        notificationWebView.setBackgroundResource(R.drawable.blue_background);
        notificationWebView.setBackgroundColor(Color.TRANSPARENT);
        resultNotificationTextColor = "#" + Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.textColorNotification)).substring(2);
        getResultNotificationTextColorTagAttribute = " style=\"color:" + resultNotificationTextColor + "\"";
        // Display loading status
        String webViewLoadingStatus = "<html><body" + getResultNotificationTextColorTagAttribute + "><i><h3>Loading Announced Results List. . .</h3><br/><h5>You may go ahead and check your results!</h5></i></body></html>";
        notificationWebView.loadDataWithBaseURL("", webViewLoadingStatus, "text/html", "UTF-8", "");
        // Get notifications
        notification = new RetrieveResultListService();
        notification.execute();


        //Bottom Banner Ad
        adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_old_scheme:
                intent = new Intent(MainActivity.this, UsnInputActivity.class);
                intent.putExtra("RESULT_TYPE", "OLD SCHEME");
                startActivity(intent);
                break;

            case R.id.button_cbcs_scheme:
                intent = new Intent(MainActivity.this, UsnInputActivity.class);
                intent.putExtra("RESULT_TYPE", "CBCS");
                startActivity(intent);
                break;

            case R.id.button_revaluation:
                intent = new Intent(MainActivity.this, UsnInputActivity.class);
                intent.putExtra("RESULT_TYPE", "REVALUATION");
                startActivity(intent);
                break;
            case R.id.button_cbcs_revaluation:
                /*intent = new Intent(MainActivity.this, UsnInputActivity.class);
                intent.putExtra("RESULT_TYPE", "CBCS REVALUATION");
                startActivity(intent);*/
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(adView != null)
            adView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adView != null)
            adView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(adView != null)
            adView.destroy();
    }

    private class RetrieveResultListService extends AsyncTask<String, Void, String> {

        private String resultNoticeContent;
        // Result notification View
        protected String doInBackground(String... urls) {
            try {
                resultNoticeContent = "";
                URL url = new URL("http://results.vtu.ac.in/");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String data;
                StringBuilder content = new StringBuilder();
                //Source code in content
                while ((data = bufferedReader.readLine()) != null) {
                    content.append(data);
                }
                bufferedReader.close();

                // Extract the result notifications from VTU
                String contentSourceCode = content.toString();

                // First to last notification content from source code
                int resultNoticeFirstIndexWithParam = contentSourceCode.indexOf("<li ");
                int resultNoticeFirstIndexWithoutParam = contentSourceCode.indexOf("<li>");
                int resultNoticeFirstIndex = (resultNoticeFirstIndexWithoutParam < resultNoticeFirstIndexWithParam) ? resultNoticeFirstIndexWithoutParam : resultNoticeFirstIndexWithParam;
                int resultNoticeLastIndex = contentSourceCode.lastIndexOf("</li>");
                resultNoticeContent = contentSourceCode.substring(resultNoticeFirstIndex, resultNoticeLastIndex);
                resultNoticeContent = resultNoticeContent.replaceAll("justify", "left");

            } catch (IOException e) {
                Log.i("NOTICE_CONTENT", resultNoticeContent);
            }
            return "done";
        }

        protected void onPostExecute(String result) {
            String notificationWebViewContent;
            // Display notification content
            if (resultNoticeContent.equals("")) {
                Log.i("NO_CONNECTION", "No internet connection");
                notificationWebViewContent = "<html><body" + getResultNotificationTextColorTagAttribute +
                        ">" +
                        "<br><br><br><br><center><img src=\"file:///android_asset/notconnectedtointernet.png\" width=\"50%\" height=\"35%\">" +
                        "<br><strong>Oops! Result notifications from VTU could not be loaded. Please check your internet connection." +
                        "</strong></center></body></html>";
            } else {
                notificationWebViewContent = "<html><body " + getResultNotificationTextColorTagAttribute + "><ul>"
                        + resultNoticeContent + "</li></ul><br><br><br></body></html>";
            }

            WebView notificationWebView = (WebView) findViewById(R.id.webview);
            notificationWebView.loadDataWithBaseURL("", notificationWebViewContent, "text/html", "UTF-8", "");
        }
    }
}

