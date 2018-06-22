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

public class ServerPost extends AsyncTask<String,Void,String> {
    @SuppressLint("StaticFieldLeak")
    private MainScreenActivity mainScreenActivity;
    private String param,param2,param3;
    private long num;
    @SuppressLint("StaticFieldLeak")
    private EditText et;
    private BackEnd backEnd;
    private ArrayList<Train> trains;

    ServerPost(MainScreenActivity mainScreenActivity, String param3, String param, String param2,
               EditText et,ArrayList<Train> t,long num) {
        this.mainScreenActivity=mainScreenActivity;
        this.param=param;
        this.param2=param2;
        this.param3=param3;
        this.et=et;
        this.num=num;
        backEnd=new BackEnd();
        trains=t;
    }


    @Override
    protected String doInBackground(String... strings) {
        try {
            return post(strings[0],strings[1]);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d("result", s);
        try {
            if (s.trim().equals("good")) {

                Intent i = new Intent(mainScreenActivity, SignalActivity.class);
                i.putExtra("Signal", param);
                i.putExtra("TrainNumber",Integer.parseInt(param2));
                i.putExtra("TrackName",param3);
                i.putExtra("Phone",num);
                mainScreenActivity.startActivity(i);
//                Toast.makeText(mainScreenActivity,"YAAAAY",Toast.LENGTH_SHORT).show();
            } else {
                if (s.equals("error")){
                    mainScreenActivity.exceptionRaised();
                }
                else {
                    Toast.makeText(mainScreenActivity, "Enter Valid Train Info!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch (NullPointerException e){
            Toast.makeText(mainScreenActivity, "Something was wrong!", Toast.LENGTH_SHORT).show();
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
