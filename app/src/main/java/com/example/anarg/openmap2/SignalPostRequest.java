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

/**
 * This Async Task class is related to the Signal Activity class which handles the network request
 * and updates the UI based on the response.
 * @author Anarghya Das
 */
public class SignalPostRequest extends AsyncTask<String,Void,String> {
    //Reference to the Async Response Interface
    private AsyncResponse response;
    //Stores the current train
    private String train;
    //Stores the reference to the backend class
    private BackEnd backEnd;
    @SuppressLint("StaticFieldLeak")
    //Stores the refer  ence to the Signal Activity class
    private SignalActivity signalActivity;
    //Stores the reference to the Thread Control class
    private ThreadControl threadControl;
    //Stores the connection wait and the read wait times
    private final int CONN_WAIT_TIME = 30000;
    private final int CONN_DATA_WAIT_TIME = 30000;
    /**
     * Constructor which initializes the instance variables that need to be initialized.
     * @param s current train
     * @param signalActivity signal activity reference
     * @param threadControl thread control reference
     * @param response async response reference
     */
    SignalPostRequest(String s,SignalActivity signalActivity,ThreadControl threadControl,AsyncResponse response){
        backEnd=new BackEnd();
        train=s;
        this.signalActivity=signalActivity;
        this.threadControl=threadControl;
        this.response=response;
    }
    /**
     * The network connections are done here in background
     * @param strings urls of the severs to be connected
     * @return response from the server
     */
    @Override
    protected String doInBackground(String... strings) {
        Log.d("TaskTime", "Start ");
        try {
            String s=null;
            boolean t=true;
        while (t) {
            threadControl.waitIfPaused();
            //Stop work if control is cancelled.
            if (threadControl.isCancelled()) {
                break;
            }
//            post(strings[1],strings[2]);
            s=post(strings[0], "sdsd");
            t=false;
        }
            return s;
        } catch (Exception e) {
            return null;
        }
    }
    /**
     * Updates the UI based on the server response
     * @param s server response
     */
    @Override
    protected void onPostExecute(String s) {
        if (s!=null){
            ArrayList<Train> ts=backEnd.jsonParse(s);
            if (ts!=null) {
                Train t = backEnd.getTrainFromName(train, ts);
                if (t != null && t.getSignals().size() != 0) {
                    Log.d("result", t.getSignals().toString());
                    response.processFinish("okay");
                    signalActivity.createSignal(t.getSignals(), t);
                }
            }else{
                response.processFinish("null");
            }
        }else{
            response.processFinish("null");
        }
    }
    /**
     * Method to set Up HTTP POST Request
     * @param u URl
     * @param json JSON Data to be posted
     * @return response
     * @throws IOException throws an exception if not executed properly
     */
    private String post(String u, String json) throws IOException {
        String response;
        // This is getting the url from the string we passed in
        URL url = new URL(u);

        // Create the urlConnection
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(CONN_WAIT_TIME);
        urlConnection.setReadTimeout(CONN_DATA_WAIT_TIME);


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
    /**
     * Converts the input stream object into String
     * @param is input stream object
     * @return String
     */
    private String convertInputStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
