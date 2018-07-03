package com.example.anarg.openmap2;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;


public class RequestTaskPost extends AsyncTask<String,String,String> {
    private AsyncResponse delegate;
    private BackEnd backEnd;
    private final int CONN_WAIT_TIME = 5000;
    private final int CONN_DATA_WAIT_TIME = 2000;
    @SuppressLint("StaticFieldLeak")
    MainScreenActivity m;

    RequestTaskPost(MainScreenActivity m,AsyncResponse delegate){
        backEnd=new BackEnd();
        this.m=m;
        this.delegate=delegate;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            return post(strings[0],"string");
        }catch (SocketTimeoutException e){
            return "connection";
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        if (s==null){
            delegate.processFinish("null");
        }else if (s.equals("connection")){
            delegate.processFinish("null2");
        }
        else {
            ArrayList<Train> allTrains = backEnd.jsonGov(s);
            m.setTrains(allTrains);
            m.createTrainNameView(trainArray(allTrains), allTrains);
            m.createTrainIDView(trainID(allTrains), allTrains);
            m.createTrackNameView(trackName(allTrains),allTrains);
            delegate.processFinish("done");
        }
    }
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
    private String convertInputStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }


    //Helper Method
    private String[] trainArray(ArrayList<Train> t){
        String[] trains=new String[t.size()];
        for (int i=0;i<t.size();i++){
                trains[i]=t.get(i).getTrainName();
        }
        return trains;
    }

    private String[] trainID(ArrayList<Train> t){
        String[] ids=new String[t.size()];
        for (int i=0;i<t.size();i++){
                ids[i] = Integer.toString(t.get(i).getTrainId());
        }
        return ids;
    }

    private String[] trackName(ArrayList<Train> t){
        String[] trackName=new String[t.size()];
        for (int i=0;i<t.size();i++){
            trackName[i]=t.get(i).getTrackName();
        }
        return trackName;
    }
}
