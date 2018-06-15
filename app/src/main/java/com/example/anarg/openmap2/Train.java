package com.example.anarg.openmap2;

import java.util.ArrayList;

public class Train {
    private int trainId;
    private String stationCode, CurrentLoc, trainName, direction,trackName;
    private ArrayList<Signal> signals;

    public Train(int id,String tn,String trackName) {
        signals=new ArrayList<>();
        trainId = id;
        this.trackName=trackName;
        trainName=tn;
    }

    public void setStationCode(String stc) {
        stationCode = stc;
    }

    public void setCurrentLoc(String loc) {
        CurrentLoc = loc;
    }

    public void setDirection(String d){ direction=d; }

    public int getTrainId(){ return trainId; }

    public String getTrainName() { return trainName; }

    public String getDirection() { return direction; }

    public String getTrackName(){ return  trackName; }

    public void addSignals(Signal s){
        signals.add(s);
    }

    public ArrayList<Signal> getSignals(){
        return signals;
    }

    @Override
    public String toString() {
        return "Train Number: "+trainId+"\n"+"Train Name: "+trainName+"\n"+"Station Code: "+stationCode+"\n"+"Current Location: "+CurrentLoc+"\n"+"Signals:\n"+signals+"\n";
    }
}
