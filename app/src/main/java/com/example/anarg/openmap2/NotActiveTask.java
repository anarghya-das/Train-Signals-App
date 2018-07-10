package com.example.anarg.openmap2;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Async task which sends "notactive" requests to the backend server which implies that this device
 * is not using the particular train information.
 * @author Anarghya Das
 */
public class NotActiveTask extends AsyncTask<String,Void,Void> {
    /**
     * The network connections are done here in background
     * @param strings urls of the severs to be connected
     * @return response from the server
     */
    @Override
    protected Void doInBackground(String... strings) {
        try {
            post(strings[0],strings[1]);
        } catch (IOException e) {
            return null;
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
