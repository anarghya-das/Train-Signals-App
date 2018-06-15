package com.example.anarg.openmap2;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
import org.osmdroid.util.GeoPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void JsonTest(){
        BackEnd b=new BackEnd();
//        System.out.println(b.jsonPlot(HttpGet("http://anarghya321.pythonanywhere.com/api/railwaysignals.json")));
        System.out.println(HttpPost("http://tms.affineit.com:4445/SignalAhead/Json/SignalAhead"));
        ArrayList<Signal> s=new ArrayList<>();
        if (s.size()==0) {
            System.out.println(s.toString());
        }
//        System.out.println(HttpPost("http://httpbin.org/post"));
    }
    private String HttpGet(String s){
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
            response = httpclient.execute(new HttpGet(s));
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                responseString = out.toString();
                out.close();
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
//            return "error";
        } catch (IOException e) {
//            return "error";
        }
        return responseString;
    }
    private String HttpPost(String s){
        HttpClient httpClient= new DefaultHttpClient();
        HttpPost httpPost;
        HttpResponse response;
        String resString=null;
        try {
            httpPost=new HttpPost(s);
            response= httpClient.execute(httpPost);
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                resString = out.toString();
                System.out.println(resString.length());
                out.close();
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resString;
    }


}