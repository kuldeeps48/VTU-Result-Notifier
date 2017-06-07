package com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by aadarsha on 6/5/17.
 */

public class MainActivity extends ActionBarActivity implements View.OnClickListener{
    private Button buttonOldScheme, buttonCbcsScheme, buttonRevaluation, buttonAboutUs, buttonSendFeedback;
    private WebView mWebView;
    private Intent intent;
    private RetrieveResultNotification notification;
    private String resultNoticeContent;
    private String resultNotificationTextColor, getResultNotificationTextColorTagAttribute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeTheme.onActivityCreateSetTheme(this);
        setContentView(R.layout.activity_main);

        buttonOldScheme = (Button) findViewById(R.id.button_old_scheme);
        buttonOldScheme.setOnClickListener(this);
        buttonCbcsScheme = (Button) findViewById(R.id.button_cbcs_scheme);
        buttonCbcsScheme.setOnClickListener(this);
        buttonRevaluation = (Button) findViewById(R.id.button_revaluation);
        buttonRevaluation.setOnClickListener(this);
        //buttonAboutUs = (Button) findViewById(R.id.button_about_us);
        //buttonAboutUs.setOnClickListener(this);
        //buttonSendFeedback = (Button) findViewById(R.id.button_send_feedback);
        //buttonSendFeedback.setOnClickListener(this);

        // Get result notifications
        WebView notificationWebView = (WebView) findViewById(R.id.webview);

        // Specify text and background color for notification web view
        String webViewNotificationBackground = "#" + Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)).substring(2);
        resultNotificationTextColor = "#" + Integer.toHexString(ContextCompat.getColor(getApplicationContext(), R.color.textColorNotification)).substring(2);
        Log.i("COLOR", webViewNotificationBackground);
        getResultNotificationTextColorTagAttribute = " style=\"color:" + resultNotificationTextColor + "\"";
        notificationWebView.setBackgroundColor(Color.parseColor(webViewNotificationBackground));

        // Display loading status
        String webViewLoadingStatus = "<html><body" + getResultNotificationTextColorTagAttribute + "><i><h3>Loading Result Notifications. . .</h3><br/><h5>You may go ahead and check your results!</h5></i></body></html>";

        notificationWebView.loadDataWithBaseURL("", webViewLoadingStatus, "text/html", "UTF-8", "");

        // Get notifications
        notification = new RetrieveResultNotification();
        notification.execute();



    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_old_scheme:
                intent = new Intent(MainActivity.this, UsnInputActivity.class);
                intent.putExtra("RESULT_TYPE", "OLD");
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

            /*case R.id.button_about_us:
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
                break;

            // Send feedback
            case R.id.button_send_feedback:
                String mailto = "mailto:the.era.labs@gmail.com" +
                        "?subject=" + Uri.encode("Feedback on VTU Result") +
                        "&body=" + Uri.encode("Hi there,\n I used your app and ");

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse(mailto));

                try {
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Please install an Email app in order to send feedback.", Toast.LENGTH_LONG).show();
                }
                break;
                */
        }
    }


    // Result notification View
    private class RetrieveResultNotification extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            try {
                URL url = new URL("http://results.vtu.ac.in/");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String data;
                StringBuilder content = new StringBuilder();
                //Source code in content
                while ((data = bufferedReader.readLine()) != null)
                {
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

                Log.i("NOTICE_CONTENT", resultNoticeContent);

            }
            catch(MalformedURLException ex){
                // hard coded URL
            }
            catch (IOException e) {
                // hard coded URL
            }
            return "done";
        }

        protected void onPostExecute(String result) {
            // Display notification content
            String notificationWebViewContent = "<html><body " + getResultNotificationTextColorTagAttribute + "><ul>" + resultNoticeContent + "</li></ul></body></html>";
            Log.i("HTML", notificationWebViewContent);

            WebView notificationWebView = (WebView) findViewById(R.id.webview);
            notificationWebView.loadDataWithBaseURL("", notificationWebViewContent, "text/html", "UTF-8", "");
        }

    }
}

