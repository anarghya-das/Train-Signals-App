package com.example.anarg.openmap2;



import android.view.ScaleGestureDetector;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.osmdroid.util.GeoPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class BackEnd {

    public ArrayList<Train> jsonGov(String s){
        ArrayList<Train> allInformation=new ArrayList<>();
        JsonValue jsonValue = Json.parse(s);
        JsonArray jsonArray = jsonValue.asArray();
//        System.out.println("Information From Json ");
//        System.out.println();
        for(int i=0;i<jsonArray.size();i++){
            JsonObject o=jsonArray.get(i).asObject();
            String direction=o.get("direction").asString();
            String locoNo=o.get("locoNo").asString();
            long trainID=o.get("trainId").asLong();
            String trainNo=Integer.toString(o.get("trainNo").asInt());

            //Final Deatils
//            System.out.println("Train Number: "+trainNo+locoNo);
//            System.out.println("Station Code: ");
//            System.out.println("Current Loc: ");
//            System.out.println("Direction: "+direction);
            //
            Train t=new Train(Integer.parseInt(trainNo+locoNo));

            JsonArray signals=o.get("signals").asArray();
            String station="No name",trackName="No Name";
            JsonArray relays=null;
            if(signals.size()!=0) {
                for (int j = 0; j < signals.size(); j++) {
                    JsonObject o1 = signals.get(j).asObject();
                    trackName = o1.get("trackName").asString();
                    station = o1.get("station").asString();
//                    System.out.println("Signal "+(j+1)+" ahead (Stn Code): "+station);
//                    System.out.println("Track Name: "+trackName);
                    JsonObject aspectSignal = o1.get("toAspectSignal").asObject();
                    String signalName=aspectSignal.get("objectName").asString();
//                    System.out.println("Signal "+(j+1)+" ahead Signal Name: "+signalName);
                    if (aspectSignal.get("relays").isArray()) {
                        relays = aspectSignal.get("relays").asArray();
                        ArrayList<String> channelDescriptions=new ArrayList<>();

                        for(int k=0;k<relays.size();k++){
                            JsonObject relaysObject= relays.get(k).asObject();
                            if(relaysObject.get("currentStatus").asString().equalsIgnoreCase("Up")){
                                channelDescriptions.add(relaysObject.get("channelDescription").asString());
                            }
                        }
//                        System.out.println("Signal "+(j+1)+" Aspect: "+signalColor(channelDescriptions));
                        String aspect=signalColor(channelDescriptions);
                        Signal sig=new Signal(station,signalName,aspect);
                        if(sig!=null) {
                            t.addSignals(sig);
                        }
                    }
                }
            }


//            System.out.println("Train ID: "+trainID);
//            System.out.println("Train Name: "+trainName);
//            System.out.println("Status to be mapped: "+status);
//            System.out.println();
            allInformation.add(t);
        }
        return allInformation;
    }

    public ArrayList<Signal> getSignals(ArrayList<Train> t){
        ArrayList<Signal> sg=new ArrayList<>();
        for (Train a: t){
            for(Signal s: a.getSignals()){
                sg.add(s);
            }
        }
        return  sg;
    }

    public ArrayList<String> getSignalIds(ArrayList<Signal> s){
        ArrayList<String> f=new ArrayList<>();
        for (Signal so: s){
            f.add(so.getSignalID());
        }
        return f;
    }
    public int exists(ArrayList<String> a,HashMap<String,GeoPoint> h){
        int count=0;
        for (String s: h.keySet()){
            if(a.contains(s)){
                count++;
            }
        }
        return count;
    }

    public HashMap<String, GeoPoint> jsonPlot(String s){
        HashMap<String,GeoPoint> m= new HashMap<>();
        JsonValue jsonValue = Json.parse(s);
        JsonArray jsonArray = jsonValue.asArray();
        for(int i=0;i<jsonArray.size();i++){
            JsonObject arrayObject=jsonArray.get(i).asObject();
            String signalId=arrayObject.get("signalId").asString();
            JsonObject coo=arrayObject.get("coordinate").asObject();
            double latitude= Double.parseDouble(coo.get("latitude").asString());
            double longitude= Double.parseDouble(coo.get("longitude").asString());
            GeoPoint gp=new GeoPoint(latitude,longitude);
            m.put(signalId,gp);
        }
        return m;
    }

    public String postRequest(String url, String params, boolean json){
        String response = "";
        try{
            ContentType type = ContentType.DEFAULT_TEXT;
            if(json){
                type = ContentType.APPLICATION_JSON;
            }
            response = Request.Post(url).bodyString(params, type).execute().returnContent().asString();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return response;
    }

    public String getRequest(String url){
        String response = "";

        try{
            response = Request.Get(url).execute().returnContent().asString();
        }catch(IOException ex){
            ex.printStackTrace();
        }

        return response;
    }


    private String signalColor(ArrayList<String> a) {
        String s="Yellow";
        for (String channelDescription: a) {
            if (channelDescription.contains("RGKE")) {
                s = "Red";
            } else if (channelDescription.contains("HGKE")&&channelDescription.contains("HHGKE")) {
                s = "YellowYellow";
            } else if (channelDescription.contains("DGKE")) {
                s = "Green";
            }
        }
        return  s;
    }
}
