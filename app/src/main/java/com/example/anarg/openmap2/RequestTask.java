package com.example.anarg.openmap2;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;
import org.osmdroid.util.GeoPoint;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This Async Task class is related to the Main Activity class which handles the network request
 * and updates the UI based on the response.
 * @author Anarghya Das
 */
public class RequestTask  extends AsyncTask<String, Void, ArrayList<String>> {
    //Stores the Async Response class reference
    private AsyncResponse response;
    //Stores the backend class reference
    private BackEnd b;
    @SuppressLint("StaticFieldLeak")
    //Stores the main activity class refrence
    private MainActivity gp;
    //Stores the thread control class reference
    private ThreadControl thread;
    //Stores the current train name selected
    private String param;
    //HTTP request related variables
    private static final String REQUEST_METHOD = "GET";
    private static final int READ_TIMEOUT = 30000;
    private static final int CONNECTION_TIMEOUT = 30000;

    /***
     * Constructor which initialises most of the instance variables
     * @param b backend reference
     * @param gp main activity reference
     * @param t thread control reference
     * @param param current train name
     * @param response async response reference
     */
    RequestTask(BackEnd b, MainActivity gp, ThreadControl t, String param,AsyncResponse response){
        this.b=b;
        this.gp=gp;
        thread=t;
        this.param=param;
        this.response=response;
    }
    /**
     * Creates the bottom navigation bar and the map before the async task is executed.
     */
    @Override
    protected void onPreExecute() {
        if (gp.getMap()==null) {
            gp.createBottomBar();
            gp.createMap();
        }
    }
    /**
     * The network connections are done here in background
     * @param uri urls of the severs to be connected
     * @return response from the server
     */
    @Override
    protected ArrayList<String> doInBackground(String... uri) {
        ArrayList<String> a = new ArrayList<>();
        boolean t = true;
            try {
                while (t) {
                    thread.waitIfPaused();
                    //Stop work if control is cancelled.
                    if (thread.isCancelled()) {
                        break;
                    }
                    if (!uri[0].equals("") && !uri[1].equals("")) {
                        a.add(get(uri[0]));
                        a.add(post(uri[1],"asd"));
                    }
                    if (uri[0].equals("")){
                        String s2="";
                        gp.checkCurrentLocation();
//                        Log.d("signals",Boolean.toString(b));
//                        if (b){
//                            s2=post(uri[2],gp.jsonPost("active"));
//                        }
                        String s = post(uri[1],"asd");
                        a.add(s);
                        a.add(s2);
                        a.add("sd");
                    }
                    t = false;
                }
                return a;
            }catch (IOException e){
                a.add("null");
                if (e.getMessage().equals("GET")) {
                    return a;
                }else{
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
    }
    /**
     * Updates the UI based on the server response
     * @param result server response
     */
    @Override
    protected void onPostExecute(ArrayList<String> result) {
        Log.d("key", "Request Task onPostExecute: ");
        if (result==null){
            response.processFinish("null");
//            gp.exceptionRaised("There was some problem connecting to the Server!\nPlease try again later.");
        }else if (result.size()==1){ //If the database server fails
            response.processFinish("null1");
        } else {
            if (result.size() == 2) {
                gp.setMapCenterOnLocation();
                HashMap<String, GeoPoint> h = b.jsonPlot(result.get(0));
                ArrayList<Train> ts=b.jsonParse(result.get(1));
                if (ts!=null) {
                    Train t = b.getTrainFromName(param, ts);
                    if (t!=null) {
                        gp.populateMarkers(h);
                        gp.addSignalToMap(t.getSignals());
                        gp.setMapCenter(h.get(getFirstIndex(t.getSignals())));
                        response.processFinish("okay1");
                    }
                }
            }
            if (result.size() == 3) {
                if (!result.get(0).equals("")) {
                    ArrayList<Train> ts=b.jsonParse(result.get(0));
                    if (ts!=null) {
                        Train t = b.getTrainFromName(param, ts);
                        if (t != null) {
                            Log.d("list", "RUN");
                            gp.updateSignalMap(t.getSignals());
                            response.processFinish("okay");
                        }
                    }
                    else{
                        response.processFinish("null");
                    }
                }
////                if (!result.get(1).equals("")){
////                    Log.d("result", result.get(1));
//////                    Toast.makeText(gp, result.get(1),Toast.LENGTH_SHORT).show();
////                }
            }
        }
    }
    /**
     * Helper method which returns the corresponding signal which has the first index form the
     * arraylist of signals
     * @param s arraylist of signals
     * @return signal object
     */
    private String getFirstIndex(ArrayList<Signal> s){
        for (Signal sp: s){
            if (sp.getIndex()==1){
                return sp.getSignalID();
            }
        }
        return null;
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
            urlConnection.setReadTimeout(READ_TIMEOUT);
            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);


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
                if (response==null || response.isEmpty()){
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
     * Method to set Up HTTP GET Request
     * @param url URl
     * @return response
     * @throws IOException throws an exception if not executed properly
     */
    private String get(String url) throws IOException{
        String result;
        String inputLine;
            //Create a URL object holding our url
            URL myUrl = new URL(url);
            //Create a connection
            HttpURLConnection connection =(HttpURLConnection)
                    myUrl.openConnection();
            //Set methods and timeouts
            connection.setRequestMethod(REQUEST_METHOD);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);

            //Connect to our url
        try {
            connection.connect();
        }catch (Exception e){
            throw new IOException("GET");
        }
            //Create a new InputStreamReader
            InputStreamReader streamReader = new
                    InputStreamReader(connection.getInputStream());
            //Create a new buffered reader and String Builder
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder stringBuilder = new StringBuilder();
            //Check if the line we are reading is not null
            while((inputLine = reader.readLine()) != null){
                stringBuilder.append(inputLine);
            }
            //Close our InputStream and Buffered reader
            reader.close();
            streamReader.close();
            //Set our result equal to our stringBuilder
            result = stringBuilder.toString();

            if (result.isEmpty()){
                throw new IOException("GET");
            }

        return result;
    }

}
