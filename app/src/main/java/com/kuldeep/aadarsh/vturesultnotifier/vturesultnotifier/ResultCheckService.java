package com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier;

import android.app.Activity;
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
import android.preference.PreferenceManager;
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
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class ResultCheckService extends Service {

    private String TAG = "ResultCheckService";
    private String page_url;
    private CheckWebPage task;
    private int counter = 0;
    public HttpURLConnection urlConnection;

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
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);
        //Get URL from UsnInputActivity's intent
        page_url = intent.getStringExtra("RESULT_PAGE_URL");

        //Keep a notification so that service is not killed
        //Intent notificationIntent = new Intent(ResultCheckService.this, MainActivity.class);
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //PendingIntent pendingIntent = PendingIntent.getActivity(ResultCheckService.this, 0, notificationIntent,
        //        0);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("VTU Result Notifier")
                .setContentText("Will Keep Checking For Your Result")
                .setSmallIcon(R.drawable.small_icon_doing)
                //.setContentIntent(pendingIntent)
                .build();
        //Make it a foreground activity to prevent being killed
        startForeground(7, notification);

        //Fuck sharedPreferences!
        // Wasted half a day and it doesn't even work. We'll use good old fashioned files to share state
        File p = getApplicationContext().getFilesDir();
        Log.i(TAG, p.toString());
        try {
            FileWriter out = new FileWriter(new File(p, "state.txt"));
            out.write("true");
            out.close();
        } catch (IOException e) {
            Log.i(TAG, e.toString());
        }

        //Web-page checking will be done Asynchronously to prevent main thread from hanging
        task = new CheckWebPage();
        task.execute();

        return START_REDELIVER_INTENT;
    }



    @Override
    public void onDestroy(){
        Log.v("ResCheck OnDestroy","Trying to kill Service");
        //Cancel the async task before killing service
        task.onCancelled();
        while (!task.isCancelled()) {
            Log.v("ResCheck OnDestroy", "Task not cancelled!");
            task.cancel(true);
        }
        Log.v("ResCheck OnDestroy", "Task Killed, Service Killed");
        stopForeground(true);

        //Change running status
        File p = getApplicationContext().getFilesDir();
        Log.i(TAG, p.toString());
        try {
            FileWriter out = new FileWriter(new File(p, "state.txt"));
            out.write("false");
            out.close();
        } catch (IOException e) {
            Log.i(TAG, e.toString());
        }

        //Send a broadcast to UsnInputActivity to change Button text
        Intent intent = new Intent();
        intent.setAction("com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier.ButtonText");
        sendBroadcast(intent);

        super.onDestroy();

    }

    private class CheckWebPage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String...urls) {
            while (!task.isCancelled()) {
                if(counter > 200){
                    break;
                }
                counter++;
                Log.i(TAG, "Getting Results.." + counter);
                try {
                    URL url = new URL(page_url);
                    Log.i(TAG, url.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.setReadTimeout(5000);
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
                        Thread.sleep(500);
                    } else {
                        Log.i(TAG, "Result notification shown..");

                        File path = getApplicationContext().getFilesDir();
                        Log.i(TAG, path.toString());
                        //Store web page in a file to display later
                        try {
                            FileWriter out = new FileWriter(new File(path, "my-file-name.html"));
                            out.write(content.toString());
                            out.close();
                        } catch (IOException e) {
                            Log.e(TAG, e.toString());
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


                        break;
                    }
                } catch (MalformedURLException e) {
                    Log.e(TAG, "MalformedURL!!");
                    break;
                } catch (IOException e) {
                    Log.i(TAG, "Connection Error/Timeout..." + e.toString());
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted..");
                    urlConnection.disconnect();
                    task.cancel(true);
                    break;
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    urlConnection.disconnect();
                    task.cancel(true);
                    break;
                }

            }
            return "done";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Change running status
            File p = getApplicationContext().getFilesDir();
            Log.i(TAG, p.toString());
            try {
                FileWriter out = new FileWriter(new File(p, "state.txt"));
                out.write("false");
                out.close();
            } catch (IOException e) {
                Log.i(TAG, e.toString());
            }

            //Send a broadcast to UsnInputActivity to change Button text
            Intent intent = new Intent();
            intent.setAction("com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier.ButtonText");
            sendBroadcast(intent);
            Log.v("ResCheck OnPostExec","Done with Async Task");
            ResultCheckService.this.stopSelf();
        }

        @Override
        protected void onCancelled() {

            File p = getApplicationContext().getFilesDir();
            Log.i(TAG, p.toString());
            try {
                FileWriter out = new FileWriter(new File(p, "state.txt"));
                out.write("false");
                out.close();
            } catch (IOException e) {
                Log.i(TAG, e.toString());
            }

            //Send a broadcast to UsnInputActivity to change Button text
            Intent intent = new Intent();
            intent.setAction("com.kuldeep.aadarsh.vturesultnotifier.vturesultnotifier.ButtonText");
            sendBroadcast(intent);

            Log.v("ResCheck OnCancelled","Trying to kill AsyncTask");
            ResultCheckService.this.stopSelf();
            super.onCancelled();
        }
    }

}

