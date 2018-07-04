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
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class RequestTask  extends AsyncTask<String, Void, ArrayList<String>> {
    private AsyncResponse response;
    private BackEnd b;
    @SuppressLint("StaticFieldLeak")
    private MainActivity gp;
    private ThreadControl thread;
    private String param;
    private static final String REQUEST_METHOD = "GET";
    private static final int READ_TIMEOUT = 15000;
    private static final int CONNECTION_TIMEOUT = 15000;


    RequestTask(BackEnd b, MainActivity gp, ThreadControl t, String param,AsyncResponse response){
        this.b=b;
        this.gp=gp;
        thread=t;
        this.param=param;
        this.response=response;
    }

    @Override
    protected void onPreExecute() {
        if (gp.getMap()==null) {
            gp.createBottomBar();
            gp.createMap();
        }
    }

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
            } catch (Exception e) {
                return null;
            }
    }

    @Override
    protected void onPostExecute(ArrayList<String> result) {
        Log.d("key", "Request Task onPostExecute: ");
        if (result==null){
            response.processFinish("null");
//            gp.exceptionRaised("There was some problem connecting to the Server!\nPlease try again later.");
        }else {
            if (result.size() == 2) {
                HashMap<String, GeoPoint> h = b.jsonPlot(result.get(0));
                ArrayList<Train> ts=b.jsonGov(result.get(1));
                if (ts!=null&&h!=null) {
                    response.processFinish("okay");
                    Train t = b.getTrainFromName(param, ts);
                    gp.populateMarkers(h);
                    gp.addSignalToMap(t.getSignals());
                    gp.setMapCenter(h.get(getFirstIndex(t.getSignals())));
                }else {
                    response.processFinish("null");
                }
            }
            if (result.size() == 3) {
                if (!result.get(0).equals("")) {
                    response.processFinish("okay");
                    ArrayList<Train> ts=b.jsonGov(result.get(0));
                    if (ts!=null) {
                        Train t = b.getTrainFromName(param, ts);
                        if (t != null) {
                            Log.d("list", "RUN");
                            gp.updateSignalMap(t.getSignals());
                        }
                    }
                    else{
                        response.processFinish("null");
                    }
                }
//                if (!result.get(1).equals("")){
//                    Log.d("result", result.get(1));
////                    Toast.makeText(gp, result.get(1),Toast.LENGTH_SHORT).show();
//                }
            }
        }
    }
    private String getFirstIndex(ArrayList<Signal> s){
        for (Signal sp: s){
            if (sp.getIndex()==1){
                return sp.getSignalID();
            }
        }
        return null;
    }


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

    private String convertInputStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private String get(String url) throws IOException {
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
            connection.connect();
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
                throw new IOException();
            }

        return result;
    }

}
