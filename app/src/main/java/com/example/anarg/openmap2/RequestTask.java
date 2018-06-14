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
import org.osmdroid.util.GeoPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class RequestTask  extends AsyncTask<String, String, ArrayList<String>> {
    private BackEnd b;
    private MainActivity gp;
    private ArrayList<String> check;
    private ThreadControl thread;
    private String param;


    public RequestTask(BackEnd b,MainActivity gp,ThreadControl t,String param){
        this.b=b;
        this.gp=gp;
        check=new ArrayList<>();
        thread=t;
        this.param=param;
    }

    @Override
    protected ArrayList<String> doInBackground(String... uri) {
        ArrayList<String> a=new ArrayList<>();
        boolean t=true;
        try {
            while (t) {
                thread.waitIfPaused();
                //Stop work if control is cancelled.
                if (thread.isCancelled()) {
                    break;
                }
                if (!uri[0].equals("") && !uri[1].equals("")) {
                    a.add(HttpGet(uri[0]));
                    a.add(HttpPost(uri[1]));
                } else {
                    check = gp.getReq();
                    String s = HttpPost(uri[1]);
                    if (check.size() == 0) {
                        check.add(s);
                    } else {
                        check.add(s);
                        Log.d("list", Integer.toString(check.size()));
                        int l = check.size();
                        if (s.equals(check.get(l - 2))) {
                            a.add("");
                        } else {
                            a.add(HttpPost(uri[1]));
                        }
                        check.add(s);
                        gp.setReq(check);
                    }
                }
                t = false;
            }
        }catch (Exception e){
            return null;
        }
        return a;
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
    protected void onPostExecute(ArrayList<String> result) {
        if (result==null){
            gp.exceptionRaised("There was some problem connecting to the Server!\nPlease try again later.");
        }
        if(result.size()==2) {
            HashMap<String, GeoPoint> h = b.jsonPlot(result.get(0));
            Train t=b.getTrainFromName(param,b.jsonGov(result.get(1)));
            Log.d("result", Integer.toString(h.size()));
            gp.populateMarkers(h);
            gp.addInitialSignals(t.getSignals());
            gp.setMapCenter();
        }
        if (result.size()==1){
                if (!result.get(0).equals("")) {
                    Log.d("list", "RUN");
                    Train t=b.getTrainFromName(param,b.jsonGov(result.get(0)));
                    gp.updateSignals(t.getSignals());
                }
        }
    }
}
