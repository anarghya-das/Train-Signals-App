package com.example.anarg.openmap2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * This async task is related to the Main Screen activity which is responsible for connecting to the backend
 * server, sending device "active" requests and checking whether no other device is logged in with the same
 * train information.
 * @author Anarghya Das
 */
public class ServerPost extends AsyncTask<String,Void,String> {
    @SuppressLint("StaticFieldLeak")
    //Stores the reference to main screen activity
    private MainScreenActivity mainScreenActivity;
    //stores the parameters entered by the user
    private String param,param2,param3,android_id;
    private long num;
    //Stores the reference to async response interface
    private AsyncResponse asyncResponse;
    //HTTP connection parameters
    private final int CONN_WAIT_TIME = 30000; //in milliseconds
    private final int CONN_DATA_WAIT_TIME = 30000; //in milliseconds
    /**
     * Constructor to initialize the above instance variables.
     * @param mainScreenActivity Stores the main activity reference
     * @param param3 stores the track name
     * @param param stores the train name
     * @param param2 stores the train number
     * @param num stores the phone number
     * @param android_id stores the android id of the phone
     * @param asyncResponse stores the async response interface reference
     */
    ServerPost(MainScreenActivity mainScreenActivity, String param3, String param, String param2,
               long num,String android_id,AsyncResponse asyncResponse) {
        this.mainScreenActivity=mainScreenActivity;
        this.param=param;
        this.param2=param2;
        this.param3=param3;
        this.num=num;
        this.android_id=android_id;
        this.asyncResponse=asyncResponse;
    }
    /**
     * The network connections are done here in background
     * @param strings urls of the severs to be connected
     * @return response from the server
     */
    @Override
    protected String doInBackground(String... strings) {
        try {
            return post(strings[0],strings[1]);
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
        try {
            if (s.trim().equals("good")) {
                Intent i = new Intent(mainScreenActivity, SignalActivity.class);
                i.putExtra("Signal", param);
                i.putExtra("TrainNumber",Integer.parseInt(param2));
                i.putExtra("TrackName",param3);
                i.putExtra("Phone",num);
                i.putExtra("id",android_id);
                mainScreenActivity.startActivity(i);
//                Toast.makeText(mainScreenActivity,"YAAAAY",Toast.LENGTH_SHORT).show();
            } else {
                if (s.equals("error")){
                    asyncResponse.processFinish("error");
                }
            }
        }
        catch (NullPointerException e){
            asyncResponse.processFinish("error2");
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
