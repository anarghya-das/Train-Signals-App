package com.example.anarg.openmap2;

import java.util.TimerTask;

public class RepeatTimer extends TimerTask {
   private boolean hasStarted;
   private Signal s;
   private SignalActivity signalActivity;
   private MainActivity mainActivity;

   RepeatTimer(){}

   RepeatTimer(Signal signal,SignalActivity so){
       hasStarted=false;
       s=signal;
       mainActivity=null;
       signalActivity=so;
   }

    RepeatTimer(Signal signal,MainActivity mainActivity){
        hasStarted=false;
        s=signal;
        signalActivity=null;
        this.mainActivity=mainActivity;
    }
    @Override
    public void run() {
       hasStarted=true;
       if (signalActivity!=null) {
           signalActivity.playSpeech(s);
       }if (mainActivity!=null){
           mainActivity.playSpeech(s);
        }
    }

    public boolean isRunning(){
       return this.hasStarted;
    }

}
