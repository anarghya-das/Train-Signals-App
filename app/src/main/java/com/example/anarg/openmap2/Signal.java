package com.example.anarg.openmap2;

/**
 * Signal class which defines the structure of a signal object
 * @author Anarghya Das
 */
public class Signal {
    //private instance variables
    private String stnCode,SignalName,SignalAspect,signalID;
    private int index;
    /**
     * Constructor to initialize a Signal object
     * @param s1 Station code
     * @param s2 Signal name
     * @param s3 Signal Aspect
     * @param i Signal ID
     */
    Signal(String s1, String s2, String s3, int i){
        index=i;
        stnCode=s1;
        SignalName=s2;
        SignalAspect=s3;
        signalID=stnCode+SignalName;
    }
    /**
     * Getter for signal ID.
     * @return signal ID of the signal
     */
    public String getSignalID(){
        return signalID;
    }
    /**
     * Getter for signal aspect.
     * @return signal aspect of the signal
     */
    public String getSignalAspect() {
        return SignalAspect;
    }
    /**
     * Getter for index
     * @return signal index
     */
    public int getIndex(){ return index; }
    /**
     * @return String representation of the signal
     */
    @Override
    public String toString() {
        String s="Index: "+index+"\n"+"Signal ID: "+signalID+"\n"+"Station Code: "+stnCode+"\n"+"Signal Name: "+SignalName+"\n"+"Signal Aspect: "+SignalAspect+"\n";
        return s;
    }

}
