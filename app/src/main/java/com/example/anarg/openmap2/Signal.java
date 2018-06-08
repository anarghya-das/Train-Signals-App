package com.example.anarg.openmap2;

public class Signal {
    String stnCode,SignalName,SignalAspect,signalID;
    public Signal(String s1,String s2,String s3){
        stnCode=s1;
        SignalName=s2;
        SignalAspect=s3;
        signalID=stnCode+SignalName;
    }

    public String getSignalID(){
        return signalID;
    }

    @Override
    public String toString() {
        String s="Signal ID: "+signalID+"\n"+"Station Code: "+stnCode+"\n"+"Signal Name: "+SignalName+"\n"+"Signal Aspect: "+SignalAspect+"\n";
        return s;
    }

}
