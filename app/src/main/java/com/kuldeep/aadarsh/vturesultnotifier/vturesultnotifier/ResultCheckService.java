package com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class ResultCheckService extends Service {

    private String TAG = "ResultCheckService";
    private String page_url;
    private CheckWebPage task;
    public ResultCheckService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Allow Networking in service
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //Get URL from UsnInputActivity's intent
        page_url = intent.getStringExtra("RESULT_PAGE_URL");

        //Keep a notification so that service is not killed
        Intent notificationIntent = new Intent(ResultCheckService.this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(ResultCheckService.this, 0, notificationIntent,
                0);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("VTU Result Notifier")
                .setContentText("Will Keep Checking For Your Result")
                .setSmallIcon(R.drawable.small_icon_doing)
                .setContentIntent(pendingIntent)
                .build();
        //Make it a foreground activity to prevent being killed
        startForeground(7, notification);

        //Web-page checking will be done Asynchronously to prevent main thread from hanging
        task = new CheckWebPage();
        task.execute();

        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.v("SERVICE","Trying to kill Service");
        //Cancel the async task before killing service
        task.cancel(true);
        while (!task.isCancelled()) {
            Log.v("SERVICE", "Task not cancelled!");
            task.cancel(true);
        }
        Log.v("SERVICE", "Task Killed, Service Killed");
        stopForeground(true);
    }

    private class CheckWebPage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String...urls) {
            while (true) {
                Log.i(TAG, "Getting Results..");
                try {
                    URL url = new URL(page_url);
                    Log.i(TAG, url.toString());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String data;
                    StringBuilder content = new StringBuilder();
                    //Store web content
                    while ((data = bufferedReader.readLine()) != null)
                    {
                        content.append(data);
                    }
                    bufferedReader.close();
                    if (content.toString().contains("University Seat Number is not available or Invalid..!")) {
                        Log.i(TAG, "Result Not Available..");
                        TimeUnit.SECONDS.sleep(5);
                    } else {
                        Log.i(TAG, "Result notification shown..");

                        File path = getApplicationContext().getFilesDir();
                        Log.i(TAG, path.toString());
                        File file = new File(path, "my-file-name.html");
                        //Store web page in a file to display later
                        try {
                            FileWriter out = new FileWriter(new File(path, "my-file-name.html"));
                            out.write(content.toString());
                            out.close();
                        } catch (IOException e) {
                            Log.i(TAG, e.toString());
                        }

                        //Create Notification
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ResultCheckService.this);
                        mBuilder.setContentTitle("Your Result Is Out!");
                        mBuilder.setContentText("Click on this to view it");
                        mBuilder.setSmallIcon(R.drawable.small_icon);
                        mBuilder.setAutoCancel(true);
                        mBuilder.setSound(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.monotone));
                        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
                        //On Clicking Notification Visit result page
                        Intent notificationIntent = new Intent(getApplicationContext(), ResultDisplay.class);
                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
                        mBuilder.setContentIntent(pi);
                        //Create notification and show
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(0, mBuilder.build());

                        SharedPreferences pref = getApplicationContext().getSharedPreferences("vtuResultPreferences", 0); // 0 - for private mode
                        final SharedPreferences.Editor editor = pref.edit();
                        editor.putBoolean("service_started", false);
                        editor.apply();
                        break;
                    }
                } catch (MalformedURLException e) {
                    Log.e(TAG, "MalformedURL!!");
                    break;
                } catch (IOException e) {
                    Log.i(TAG, "Connection Error/Timeout..." + e.toString());
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted..");
                    task.cancel(true);
                    break;
                }
            }
            return "done";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ResultCheckService.this.stopSelf();
        }
    }

}

