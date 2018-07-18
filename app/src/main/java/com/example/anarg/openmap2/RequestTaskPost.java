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

/**
 * This Async Task class is related to the Main Screen Activity class which handles the network request
 * and updates the UI based on the response.
 * @author Anarghya Das
 */
public class RequestTaskPost extends AsyncTask<String,String,String> {
    //Stores the reference to async response interface
    private AsyncResponse delegate;
    //Stores the reference to backend class
    private BackEnd backEnd;
    //HTTP request related variables
    private final int CONN_WAIT_TIME = 30000;
    private final int CONN_DATA_WAIT_TIME = 30000;
    @SuppressLint("StaticFieldLeak")
    //Stores the reference to mainscreenacitivty class
    private MainScreenActivity m;
    /**
     * Constructor which initialises most of the instance variables
     * @param m mainscreenactivity refernce
     * @param delegate async response reference
     */
    RequestTaskPost(MainScreenActivity m,AsyncResponse delegate){
        backEnd=new BackEnd();
        this.m=m;
        this.delegate=delegate;
    }
    /**
     * The network connections are done here in background
     * @param strings urls of the severs to be connected
     * @return response from the server
     */
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
    /**
     * Updates the UI based on the server response
     * @param s server response
     */
    @Override
    protected void onPostExecute(String s) {
        if (s==null){
            delegate.processFinish("null");
        }else if (s.equals("connection")){
            delegate.processFinish("null2");
        }
        else {
            ArrayList<Train> allTrains = backEnd.jsonParse(s);
            m.setTrains(allTrains);
            m.createTrainNameView(trainArray(allTrains), allTrains);
            m.createTrainIDView(trainID(allTrains), allTrains);
            m.createTrackNameView(trackName(allTrains),allTrains);
            delegate.processFinish("done");
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
    /**
     * Helper method to convert array list of train objects into train name array.
     * @param t array list of train objects
     * @return array of train names
     */
    private String[] trainArray(ArrayList<Train> t){
        String[] trains=new String[t.size()];
        for (int i=0;i<t.size();i++){
                trains[i]=t.get(i).getTrainName();
        }
        return trains;
    }
    /**
     * Helper method to convert array list of train objects into train IDs array.
     * @param t array list of train objects
     * @return array of train IDs
     */
    private String[] trainID(ArrayList<Train> t){
        String[] ids=new String[t.size()];
        for (int i=0;i<t.size();i++){
                ids[i] = Integer.toString(t.get(i).getTrainId());
        }
        return ids;
    }
    /**
     * Helper method to convert array list of train objects into track name array.
     * @param t array list of train objects
     * @return array of track names
     */
    private String[] trackName(ArrayList<Train> t){
        String[] trackName=new String[t.size()];
        for (int i=0;i<t.size();i++){
            trackName[i]=t.get(i).getTrackName();
        }
        return trackName;
    }
}
