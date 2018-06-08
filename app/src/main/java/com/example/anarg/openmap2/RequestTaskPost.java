package com.example.anarg.openmap2;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RequestTaskPost  extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... strings) {
        HttpClient httpClient= new DefaultHttpClient();
        HttpPost httpPost;
        HttpResponse response;
        String resString=null;
        try {
            httpPost=new HttpPost(strings[0]);
            response= httpClient.execute(httpPost);
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                resString = out.toString();
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
