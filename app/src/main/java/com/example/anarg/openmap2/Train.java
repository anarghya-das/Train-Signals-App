package com.example.anarg.openmap2;

import java.util.ArrayList;

public class Train {
    int trainId;
    String stationCode, CurrentLoc;
    ArrayList<Signal> signals;

    public Train(int id) {
        signals=new ArrayList<>();
        trainId = id;
    }

    public void setStationCode(String stc) {
        stationCode = stc;
    }

    public void setCurrentLoc(String loc) {
        CurrentLoc = loc;
    }

    public void addSignals(Signal s){
        signals.add(s);
    }

    public ArrayList<Signal> getSignals(){
        return signals;
    }

    @Override
    public String toString() {
        String s="Train Number: "+trainId+"\n"+"Station Code: "+stationCode+"\n"+"Current Location: "+CurrentLoc+"\n"+"Signals:\n"+signals+"\n";
        return s;
    }
}
