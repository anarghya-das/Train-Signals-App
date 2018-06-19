package com.example.anarg.openmap2;



import com.eclipsesource.json.JsonObject;

import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void JsonTest() throws IOException {
        System.out.println(postagain("http://irtrainsignalsystem.herokuapp.com/cgi-bin/sendd" +
                "evicelocation",jsonPost()));
    }
    private String jsonPost(){
        JsonObject o=new JsonObject();
        o.add("deviceId","Asv");
        JsonObject o2=new JsonObject();
        o2.add("trainNo",12312);
        o2.add("phone",123222);
        o2.add("trainName","aSD");
        o2.add("trackName","As");
        o.add("info",o2);
        JsonObject o3=new JsonObject();
        o3.add("latitude",22.5827312);
        o3.add("longitude", 88.4572688);
        o.add("coordinate",o3);
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

}