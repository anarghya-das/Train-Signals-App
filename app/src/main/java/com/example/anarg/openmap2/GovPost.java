package com.example.anarg.openmap2;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class GovPost extends AsyncTask<String,Void,String> {
    private String train;
    private BackEnd backEnd;
    @SuppressLint("StaticFieldLeak")
    private SignalActivity signalActivity;
    private ThreadControl threadControl;


    GovPost(String s,SignalActivity signalActivity,ThreadControl threadControl){
        backEnd=new BackEnd();
        train=s;
        this.signalActivity=signalActivity;
        this.threadControl=threadControl;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            boolean t=true;
        while (t) {
            threadControl.waitIfPaused();
            //Stop work if control is cancelled.
            if (threadControl.isCancelled()) {
                break;
            }
            t=false;
        }
            return post(strings[0], "sdsd");
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        if (s!=null){
            Train t=backEnd.getTrainFromName(train,backEnd.jsonGov(s));
            Log.d("result", t.getSignals().toString());
            if (signalActivity.currentCheck(t.getSignals())) {
                Log.d("result", "CHANGE");
                signalActivity.createSignal();
            }
        }
    }

    private String post(String u, String json) throws IOException {
        String response;
        // This is getting the url from the string we passed in
        URL url = new URL(u);

        // Create the urlConnection
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();


        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        urlConnection.setRequestProperty("Content-Type", "application/json");

        urlConnection.setRequestMethod("POST");


        // OPTIONAL - Sets an authorization header
        urlConnection.setRequestProperty("Authorization", "someAuthString");

        // Send the post body
        if (json != null&&!json.isEmpty()) {
            OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
            writer.write(json);
            writer.flush();
        }

        int statusCode = urlConnection.getResponseCode();


        if (statusCode ==  200) {

            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

            response = convertInputStreamToString(inputStream);
            if (response==null||response.isEmpty()){
                throw new IOException();
            }

        }
        // From here you can convert the string to JSON with whatever JSON parser you like to use
        // After converting the string to JSON, I call my custom callback. You can follow this process too, or you can implement the onPostExecute(Result) method
        else {
            // Status code is not 200
            // Do something to handle the error
            throw new IOException();
        }

        return response;
    }
    private String convertInputStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
