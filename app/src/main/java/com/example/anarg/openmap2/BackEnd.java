package com.example.anarg.openmap2;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.osmdroid.util.GeoPoint;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Backend class for this project which contains helpful functions required for the app to function properly.
 * @author Anarghya Das
 */
public class BackEnd {
    /**
     * Takes the response String from the server and parses the json data to create an Arraylist of
     * trains each with their specific data.
     * @param s json string from the server
     * @return ArrayList of trains
     */
    public ArrayList<Train> jsonGov(String s){
        try {
            ArrayList<Train> allInformation = new ArrayList<>();
            JsonValue jsonValue = Json.parse(s);
            JsonArray jsonArray = jsonValue.asArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject o = jsonArray.get(i).asObject();
                String direction = o.get("direction").asString();
//            String locoNo=o.get("locoNo").asString();
//            long trainID=o.get("trainId").asLong();
                String trackName = o.get("trackName").asString();
                String trainName = o.get("trainName").asString();
                String trainNum = Integer.toString(o.get("trainNo").asInt());
                Train t;
                if (!trainName.isEmpty() && !trainNum.equals("0")) {
                    t = new Train(Integer.parseInt(trainNum), trainName, trackName);
                } else {
                    t = new Train(0, null, null);
                }
                t.setDirection(direction);
                JsonArray signals = o.get("zSignals").asArray();
                int index;
                String station = "No name";
                JsonArray relays = null;
                if (signals.size() != 0) {
                    for (int j = 0; j < signals.size(); j++) {
                        JsonObject o1 = signals.get(j).asObject();
                        trackName = o1.get("trackName").asString();
                        station = o1.get("station").asString();
                        index = o1.get("index").asInt();
//                    System.out.println("Signal "+(j+1)+" ahead (Stn Code): "+station);
//                    System.out.println("Track Name: "+trackName);
                        JsonObject aspectSignal = o1.get("ztoAspectSignal").asObject();
                        String signalName = aspectSignal.get("objectName").asString();
//                    System.out.println("Signal "+(j+1)+" ahead Signal Name: "+signalName);
                        if (aspectSignal.get("relays").isArray()) {
                            relays = aspectSignal.get("relays").asArray();
                            ArrayList<String> channelDescriptions = new ArrayList<>();

                            for (int k = 0; k < relays.size(); k++) {
                                JsonObject relaysObject = relays.get(k).asObject();
                                if (relaysObject.get("currentStatus").asString().equalsIgnoreCase("Up")) {
                                    channelDescriptions.add(relaysObject.get("channelDescription").asString());
                                }
                            }
//                        System.out.println("Signal "+(j+1)+" Aspect: "+signalColor(channelDescriptions));
                            String aspect = signalColor(channelDescriptions);
                            Signal sig = new Signal(station, signalName, aspect, index);
                            if (sig != null) {
                                t.addSignals(sig);
                            }
                        }
                    }
                }
                if (t.getTrainName() != null) {
                    allInformation.add(t);
                }
            }
            return allInformation;
        }catch (Exception e){
            return null;
        }
    }
    /**
     *Checks the train name with the arrayList of trains and returns the corresponding train object.
     * @param t name of the train
     * @param to arraylist of trains
     * @return train object
     */
    public boolean checkTrainName(String t,ArrayList<Train> to){
        for (Train c: to){
            if (c.getTrainName().equals(t)){
                return true;
            }
        }
        return false;
    }
    /**
     *Checks the train number with the arrayList of trains and returns the corresponding train object.
     * @param t train number
     * @param to arraylist of trains
     * @return train object
     */
    public boolean checkTrainNumber(String t,ArrayList<Train> to){
        for (Train c: to){
            if (c.getTrainId()==Integer.parseInt(t)){
                return true;
            }
        }
        return false;
    }
    /**
     *Checks the track name with the arrayList of trains and returns the corresponding train object.
     * @param param3 name of the track
     * @param to arraylist of trains
     * @return train object
     */
    public boolean checkTrackName(String param3,ArrayList<Train> to) {
        for (Train c: to){
            if (c.getTrackName().equals(param3)){
                return true;
            }
        }
        return false;
    }
    /**
     *Checks the train name with the arrayList of trains and returns the corresponding train object.
     * @param t name of the train
     * @param t arraylist of trains
     * @return train object
     */
    public Train getTrainFromName(String s,ArrayList<Train> t){
        for (Train to: t){
            if(to.getTrainName().equals(s)){
                return to;
            }
        }
        return null;
    }
    /**
     * Generates the hashmap of Signal IDs and geo coordinates by parsing json based on the response
     * from the server.
     * @param s json string
     * @return HashMap of Signal ID and geo coordinates
     */
    public HashMap<String, GeoPoint> jsonPlot(String s){
        try {
            HashMap<String, GeoPoint> m = new HashMap<>();
            JsonValue jsonValue = Json.parse(s);
            JsonArray jsonArray = jsonValue.asArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject arrayObject = jsonArray.get(i).asObject();
                String signalId = arrayObject.get("signalId").asString();
                JsonObject coo = arrayObject.get("coordinate").asObject();
                double latitude = coo.get("latitude").asDouble();
                double longitude = coo.get("longitude").asDouble();
                GeoPoint gp = new GeoPoint(latitude, longitude);
                m.put(signalId, gp);
            }
            return m;
        }catch (Exception e){
            return null;
        }
    }
    /**
     * Sets the signal color of a signal based on the color code received from the server
     * @param a ArrayList of codes
     * @return color value for corresponding code
     */
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
