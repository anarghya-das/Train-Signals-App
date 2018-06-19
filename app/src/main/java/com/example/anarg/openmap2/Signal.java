package com.example.anarg.openmap2;

public class Signal {
    private String stnCode,SignalName,SignalAspect,signalID;
    private int index;
    Signal(String s1, String s2, String s3, int i){
        index=i;
        stnCode=s1;
        SignalName=s2;
        SignalAspect=s3;
        signalID=stnCode+SignalName;
    }

    public String getSignalID(){
        return signalID;
    }

    public String getSignalAspect() {
        return SignalAspect;
    }

    public int getIndex(){ return index; }

    @Override
    public String toString() {
        String s="Index: "+index+"\n"+"Signal ID: "+signalID+"\n"+"Station Code: "+stnCode+"\n"+"Signal Name: "+SignalName+"\n"+"Signal Aspect: "+SignalAspect+"\n";
        return s;
    }

}
