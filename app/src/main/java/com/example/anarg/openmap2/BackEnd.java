package com.example.anarg.openmap2;

import android.annotation.SuppressLint;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.osmdroid.util.GeoPoint;
import java.util.ArrayList;
import java.util.HashMap;

public class BackEnd {

    public ArrayList<Train> jsonGov(String s){
        ArrayList<Train> allInformation=new ArrayList<>();
        JsonValue jsonValue = Json.parse(s);
        JsonArray jsonArray = jsonValue.asArray();
        for(int i=0;i<jsonArray.size();i++){
            JsonObject o=jsonArray.get(i).asObject();
            String direction=o.get("direction").asString();
//            String locoNo=o.get("locoNo").asString();
//            long trainID=o.get("trainId").asLong();
            String trackName=o.get("trackName").asString();
            String trainName= o.get("trainName").asString();
            String trainNum=Integer.toString(o.get("trainNo").asInt());
            Train t;
            if (!trainName.isEmpty()&&!trainNum.equals("0")) {
                t = new Train(Integer.parseInt(trainNum), trainName,trackName);
            }
            else {
                t = new Train(0,null,null);
            }
            t.setDirection(direction);
            JsonArray signals=o.get("zSignals").asArray();
            int index;
            String station="No name";
            JsonArray relays=null;
            if(signals.size()!=0) {
                for (int j = 0; j < signals.size(); j++) {
                    JsonObject o1 = signals.get(j).asObject();
                    trackName = o1.get("trackName").asString();
                    station = o1.get("station").asString();
                    index= o1.get("index").asInt();
//                    System.out.println("Signal "+(j+1)+" ahead (Stn Code): "+station);
//                    System.out.println("Track Name: "+trackName);
                    JsonObject aspectSignal = o1.get("ztoAspectSignal").asObject();
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
                        Signal sig=new Signal(station,signalName,aspect,index);
                        if(sig!=null) {
                            t.addSignals(sig);
                        }
                    }
                }
            }
            if(t.getTrainName()!=null) {
                allInformation.add(t);
            }
        }
        return allInformation;
    }

    public boolean checkTrainName(String t,ArrayList<Train> to){
        for (Train c: to){
            if (c.getTrainName().equals(t)){
                return true;
            }
        }
        return false;
    }

    public Train getTrain(String t, ArrayList<Train> to){
        for (Train c: to){
            if (c.getTrainName().equals(t)){
                return c;
            }
        }
        return null;
    }

    public boolean checkTrainNumber(String t,ArrayList<Train> to){
        for (Train c: to){
            if (c.getTrainId()==Integer.parseInt(t)){
                return true;
            }
        }
        return false;
    }
    public boolean checkTrackName(String param3,ArrayList<Train> to) {
        for (Train c: to){
            if (c.getTrackName().equals(param3)){
                return true;
            }
        }
        return false;
    }



    public ArrayList<Signal> getSignals(ArrayList<Train> t){
        ArrayList<Signal> sg=new ArrayList<>();
        for (Train a: t){
            sg.addAll(a.getSignals());
        }
        return  sg;
    }
    public Train getTrainFromName(String s,ArrayList<Train> t){
        for (Train to: t){
            if(to.getTrainName().equals(s)){
                return to;
            }
        }
        return null;
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
            double latitude= coo.get("latitude").asDouble();
            double longitude= coo.get("longitude").asDouble();
            GeoPoint gp=new GeoPoint(latitude,longitude);
            m.put(signalId,gp);
        }
        return m;
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
