package com.example.anarg.openmap2;

import java.util.TimerTask;

/**
 * Repeat Timer class for the repeating timer Task
 * @author Anarghya Das
 */
public class RepeatTimer extends TimerTask {
    //private instance variables
   private boolean hasStarted;
   private Signal s;
   private SignalActivity signalActivity;
   private MainActivity mainActivity;
   /**
    * Default empty constructor
    */
   RepeatTimer(){}
    /**
     * Parameterised constructor for Signal Activity
     * @param signal signal
     * @param so signal activity reference
     */
   RepeatTimer(Signal signal,SignalActivity so){
       hasStarted=false;
       s=signal;
       mainActivity=null;
       signalActivity=so;
   }
    /**
     * Parameterised constructor for Main Activity
     * @param signal signal
     * @param mainActivity main activity
     */
    RepeatTimer(Signal signal,MainActivity mainActivity){
        hasStarted=false;
        s=signal;
        signalActivity=null;
        this.mainActivity=mainActivity;
    }
    /**
     * Method which runs when the timer starts and does the assigned task
     */
    @Override
    public void run() {
       hasStarted=true;
       if (signalActivity!=null) {
           signalActivity.playSpeech(s);
       }if (mainActivity!=null){
           mainActivity.playSpeech(s);
        }
    }
    /**
     * Checks if the timer is running or not
     * @return current status
     */
    public boolean isRunning(){
       return this.hasStarted;
    }

}
