package com.example.anarg.openmap2;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class RequestTaskPost  extends AsyncTask<String, String, String> {
    private BackEnd b;
    private MainActivity a;
    private ArrayList<String> check;


    public RequestTaskPost(BackEnd b, MainActivity s){
        this.b=b;
        this.a=s;
        check=new ArrayList<>();
    }

    @Override
    protected String doInBackground(String... strings) {
        check = a.getReq();
        String s = HttpPost(strings[0]);
        if (check.size() == 0) {
            check.add(s);
        } else {
            check.add(s);
            Log.d("list", Integer.toString(check.size()));
            int l = check.size();
            if (s.equals(check.get(l - 2))) {
                s="";
            }
            check.add(s);
            a.setReq(check);
        }
        return s;
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


    @Override
    protected void onPostExecute(String s) {
        if (!s.equals("")) {
            Log.d("list", "RUN");
            ArrayList<Signal> aa = b.getSignals(b.jsonGov(s));
            a.updateMarker(aa);
        }
    }
}
