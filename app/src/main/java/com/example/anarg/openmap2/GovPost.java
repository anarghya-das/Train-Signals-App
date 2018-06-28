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
import java.util.ArrayList;

public class GovPost extends AsyncTask<String,Void,String> {
    private String train;
    private BackEnd backEnd;
    @SuppressLint("StaticFieldLeak")
    private SignalActivity signalActivity;
    private ThreadControl threadControl;
    private ArrayList<Signal> collection;

    GovPost(String s,SignalActivity signalActivity,ThreadControl threadControl){
        backEnd=new BackEnd();
        train=s;
        this.signalActivity=signalActivity;
        this.threadControl=threadControl;
        collection=new ArrayList<>();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String s=null;
            boolean t=true;
        while (t) {
            threadControl.waitIfPaused();
            //Stop work if control is cancelled.
            if (threadControl.isCancelled()) {
                break;
            }
            post(strings[1],strings[2]);
            s=post(strings[0], "sdsd");
            t=false;
        }
            return s;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        if (s!=null){
            Train t=backEnd.getTrainFromName(train,backEnd.jsonGov(s));
            if (t!=null&&t.getSignals().size()!=0) {
                Log.d("result", t.getSignals().toString());
                Log.d("result", "CHANGE");
                    signalActivity.createSignal(t.getSignals(),t);
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
