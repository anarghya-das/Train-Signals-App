package com.example.anarg.openmap2;



import com.eclipsesource.json.JsonObject;

import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    private static final String govURl = "http://tms.affineit.com:4445/SignalAhead/Json/SignalAhead";

    @Test
    public void JsonTest() throws IOException {
        BackEnd b=new BackEnd();
        String s=postagain(govURl,"sf");
        ArrayList<Train> t=b.jsonGov(s);
        Train to=b.getTrainFromName("Howrah-Bandel Local",t);
        System.out.println(get("http://14.139.219.37/railway/jsonrender.php"));
//        System.out.println(to);
//        ArrayList<Train> ts=new ArrayList<>();
//        System.out.println(ts.size());
//        System.out.println(ts==null);
    }
    private String jsonPost(){
        JsonObject o=new JsonObject();
        o.add("deviceId","5c");
        JsonObject o2=new JsonObject();
        o2.add("trainNo",12312);
        o2.add("phone",8961501754L);
        o2.add("trainName","aSD");
        o2.add("trackName","As");
        o.add("info",o2);
        JsonObject o3=new JsonObject();
        o3.add("latitude",22.5817837);
        o3.add("longitude", 88.4587077);
        o.add("coordinate",o3);
        o.add("status","notactive");
//        Log.d("worksend", o.toString());
        return o.toString();
    }
    private String postagain(String s,String p){
        String response;
        try {
            // This is getting the url from the string we passed in
            URL url = new URL(s);

            // Create the urlConnection
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();


            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            urlConnection.setRequestProperty("Content-Type", "application/json");

            urlConnection.setRequestMethod("POST");


            // Send the post body
            if (p != null&&!p.isEmpty()) {
                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                writer.write(p);
                writer.flush();
            }

            int statusCode = urlConnection.getResponseCode();

            if (statusCode ==  200) {

                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

                response = convertInputStreamToString(inputStream);

            }
                //Close our InputStream and Buffered reader
                // From here you can convert the string to JSON with whatever JSON parser you like to use
                // After converting the string to JSON, I call my custom callback. You can follow this process too, or you can implement the onPostExecute(Result) method
             else {
                // Status code is not 200
                // Do something to handle the error
                throw new Exception();
            }

        } catch (Exception e) {
            return null;
        }
        return response;
    }
    static String convertInputStreamToString(java.io.InputStream is) {
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
        connection.setRequestMethod("GET");
//        connection.setReadTimeout(READ_TIMEOUT);
//        connection.setConnectTimeout(CONNECTION_TIMEOUT);

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