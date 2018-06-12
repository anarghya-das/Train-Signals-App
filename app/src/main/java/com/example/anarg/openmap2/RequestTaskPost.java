package com.example.anarg.openmap2;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
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
        return HttpPost(strings[0]);
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
        ArrayList<Train> allTrains=backEnd.jsonGov(s);
        Log.d("train", allTrains.toString());
        m.createTrainNameView(trainArray(allTrains),allTrains);
        m.createTrainIDView(trainID(allTrains),allTrains);
        Log.d("array", Integer.toString(trainArray(allTrains).length));
        Log.d("array", Integer.toString(trainID(allTrains).length));
        Log.d("array", arrayPrint(trainArray(allTrains)));
        Log.d("array", arrayPrint(trainID(allTrains)));
    }
    private String arrayPrint(String[] a){
        String s="";
        for (int i=0;i<a.length;i++){
            s=s+a[i]+" ";
        }
        return s;
    }
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
            int j=t.get(i).getTrainId();
            ids[i]=Integer.toString(j);
        }
        return ids;
    }
}
