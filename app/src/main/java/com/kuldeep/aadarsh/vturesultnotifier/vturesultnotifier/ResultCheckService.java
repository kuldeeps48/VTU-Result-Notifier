package com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class ResultCheckService extends Service {

    private String TAG = "ResultCheckService";
    private String usn;
    public ResultCheckService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        usn = intent.getStringExtra("USN");
        CheckWebPage task = new CheckWebPage();

        //Keep a notifiation so that service is not killed
        Intent notificationIntent = new Intent(this, ResultCheckService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("VTU Result Notifier")
                .setContentText("Will Keep Checking For Your Result")
                .setSmallIcon(R.drawable.small_icon)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(7, notification);
        task.execute();
        return START_STICKY;
    }

    private class CheckWebPage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String...urls) {
            while (true) {
                Log.i(TAG, "Getting Results..");
                try {
                    URL url = new URL("http://results.vtu.ac.in/results/result_page.php?usn=" + usn);
                    Log.i(TAG, url.toString());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String data;
                    StringBuilder content = new StringBuilder();
                    while ((data = bufferedReader.readLine()) != null)
                    {
                        content.append(data);
                    }
                    bufferedReader.close();
                    if (content.toString().contains("University Seat Number is not available or Invalid..!")) {
                        Log.i(TAG, "Result Not Available..");
                        TimeUnit.SECONDS.sleep(15);
                    } else {
                        Log.i(TAG, "Result notification shown..");
                        //Create Notification
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ResultCheckService.this);
                        mBuilder.setContentTitle("Your Result Is Out!");
                        mBuilder.setContentText("Click this notification to visit result page.");
                        mBuilder.setSmallIcon(R.drawable.small_icon);
                        mBuilder.setAutoCancel(true);
                        //On Clicking Visit result page
                        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
                        notificationIntent.setData(Uri.parse(url.toString()));
                        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
                        mBuilder.setContentIntent(pi);
                        //Create notification and show
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(0, mBuilder.build());
                        break;
                    }
                } catch (MalformedURLException e) {
                    Log.e(TAG, "MalformedURL!!");
                } catch (IOException e) {
                    Log.i(TAG, "Connection Error/Timeout..." + e.toString());
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted..");
                }
            }
            return "done";
        }
    }

}

