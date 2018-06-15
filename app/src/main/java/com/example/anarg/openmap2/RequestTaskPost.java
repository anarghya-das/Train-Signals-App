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

public class RequestTaskPost extends AsyncTask<String,String,String> {
    BackEnd backEnd;
    MainScreenActivity m;

    public RequestTaskPost(MainScreenActivity m){
        backEnd=new BackEnd();
        this.m=m;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            return HttpPost(strings[0]);
        } catch (Exception e) {
            return null;
        }
    }

    private String HttpPost(String s) throws IOException,NullPointerException{
        HttpClient httpClient= new DefaultHttpClient();
        HttpPost httpPost;
        HttpResponse response;
        String resString=null;
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
            if (resString==null || resString.isEmpty()){
                throw new NullPointerException();
            }
        return resString;
    }

    private String HttpGet(String s) throws IOException, NullPointerException{
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
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
            if (responseString==null || responseString.isEmpty()){
                throw new NullPointerException();
            }
        return responseString;
    }

    @Override
    protected void onPostExecute(String s) {
        if (s==null){
            m.exceptionRaised();
        }
        else {
            ArrayList<Train> allTrains = backEnd.jsonGov(s);
            m.createTrainNameView(trainArray(allTrains), allTrains);
            m.createTrainIDView(trainID(allTrains), allTrains);
            m.createTrackNameView(trackName(allTrains),allTrains);
        }
    }

//    private String arrayPrint(String[] a){
//        String s="";
//        for (int i=0;i<a.length;i++){
//            s=s+a[i]+" ";
//        }
//        return s;
//    }
    //Helper Method
    private String[] trainArray(ArrayList<Train> t){
        String[] trains=new String[t.size()];
        for (int i=0;i<t.size();i++){
                trains[i]=t.get(i).getTrainName();
        }
        return trains;
    }

    private String[] trainID(ArrayList<Train> t){
        String[] ids=new String[t.size()];
        for (int i=0;i<t.size();i++){
                ids[i] = Integer.toString(t.get(i).getTrainId());
        }
        return ids;
    }

    private String[] trackName(ArrayList<Train> t){
        String[] trackName=new String[t.size()];
        for (int i=0;i<t.size();i++){
            trackName[i]=t.get(i).getTrackName();
        }
        return trackName;
    }
}
