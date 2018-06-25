package com.example.anarg.openmap2;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eclipsesource.json.JsonObject;

import java.util.ArrayList;

public class SignalActivity extends AppCompatActivity {
    private String trainName,trackName,android_id;
    private ArrayList<Train> train;
    private int trainNo;
    private long phone;
    private BackEnd backEnd;
    private ArrayList<Signal> currentSignals;
    private ArrayList<Signal> changedSignals;
    private MediaPlayer mediaPlayer;
    private ThreadControl threadControl;
    private GovPost govPost;
    private ArrayList<GovPost> g;
    private static final String govURl = "http://tms.affineit.com:4445/SignalAhead/Json/SignalAhead";
    private static final String backEndServer= "http://irtrainsignalsystem.herokuapp.com/cgi-bin/senddevicelocation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signal);
        backEnd=new BackEnd();
        currentSignals=new ArrayList<>();
        changedSignals=new ArrayList<>();
        g=new ArrayList<>();
        Intent i = getIntent();
        trainName = i.getStringExtra("Signal");
        trainNo = i.getIntExtra("TrainNumber",0);
        trackName = i.getStringExtra("TrackName");
        phone = i.getLongExtra("Phone", 0);
        android_id=i.getStringExtra("id");
        mediaPlayer=MediaPlayer.create(this,R.raw.sound);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu= navigation.getMenu();
        MenuItem menuItem= menu.getItem(0);
        menuItem.setChecked(true);
        threadControl=new ThreadControl();
        govPost= new GovPost(trainName,this,threadControl);
        govPost.execute(govURl);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case  R.id.signal_view:
                    break;
                case R.id.map_view:
//                    Toast.makeText(SignalActivity.this,"You clicked Map View",Toast.LENGTH_SHORT).show();
                    Intent i=new Intent(SignalActivity.this,MainActivity.class);
                    i.putExtra("Signal", trainName);
                    i.putExtra("TrainNumber",trainNo);
                    i.putExtra("TrackName",trackName);
                    i.putExtra("Phone",phone);
                    i.putExtra("id",android_id);
                    SignalActivity.this.startActivity(i);
                    break;
            }
            return false;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        threadControl.pause();
        mHandler.removeCallbacks(timerTask);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.post(timerTask);
        threadControl.resume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        new NotActiveTask().execute(backEndServer,jsonPost("notactive"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (GovPost go: g) {
            go.cancel(true);
            threadControl.cancel();
        }
    }

    public String jsonPost(String status) {
        JsonObject o = new JsonObject();
        o.add("deviceId", android_id);
        JsonObject o2 = new JsonObject();
        o2.add("trainNo", trainNo);
        o2.add("phone", phone);
        o2.add("trainName", trainName);
        o2.add("trackName", trackName);
        o.add("info", o2);
        JsonObject o3 = new JsonObject();
        o3.add("latitude", 0);
        o3.add("longitude", 0);
        o.add("coordinate", o3);
        o.add("status", status);
        return o.toString();
    }

    private Handler mHandler = new Handler();
    private Runnable timerTask = new Runnable() {
        @Override
        public void run() {
            if (govPost.getStatus()== AsyncTask.Status.FINISHED) {
                govPost= new GovPost(trainName,SignalActivity.this,threadControl);
                govPost.execute(govURl,backEndServer,jsonPost("active"));
                g.add(govPost);
            }
            mHandler.postDelayed(timerTask, 1);
        }};

    public boolean currentCheck(ArrayList<Signal> s){
        boolean f=false;
            for (int i=0;i<s.size();i++){
                Signal so=s.get(i);
                if (sig(so)){
                    changedSignals.add(so);
                    f=true;
                }
            }
            if (f) {
                currentSignals.clear();
                currentSignals.addAll(changedSignals);
                changedSignals.clear();
            }
            return f;
        }

    private boolean sig(Signal s){
        boolean t=true;
        for (Signal so: currentSignals){
            if (so.getSignalID().equals(s.getSignalID())){
                t=false;
                break;
            }
        }
        return t;
    }

    private Integer getColor(Signal s){
        if (s!=null) {
            switch (s.getSignalAspect()) {
                case "Red":
                    return R.drawable.medium_red;
                case "Green":
                    return R.drawable.medium_green;
                case "Yellow":
                    return R.drawable.medium_yellow;
                case "YellowYellow":
                    return R.drawable.medium_yellowyellow;
                default:
                    return R.drawable.medium_none;
            }
        }
        else {
            return R.drawable.medium_none;
        }
    }
    public void createSignal(){
        boolean tag1=false;
        boolean tag2=false;
        boolean tag3=false;
        ImageView img1=findViewById(R.id.firstSignal);
        ImageView img2=findViewById(R.id.secondSignal);
        ImageView img3=findViewById(R.id.thirdSignal);
        if (currentSignals!=null) {
            if (currentSignals.size()==0){
                img1.setImageResource(getColor(null));
                img2.setImageResource(getColor(null));
                img3.setImageResource(getColor(null));
            }else {
                for (Signal s : currentSignals) {
                    if (s.getIndex() == 1) {
                        mediaPlayer.start();
                        img1.setImageResource(getColor(s));
                        tag1=true;
                    } else if (s.getIndex() == 2) {
                        img2.setImageResource(getColor(s));
                        tag2=true;
                    } else if (s.getIndex() == 3) {
                        img3.setImageResource(getColor(s));
                        tag3=true;
                    }
                }
            }
           if (!tag1){
               img1.setImageResource(getColor(null));
           }
           if (!tag2){
               img2.setImageResource(getColor(null));
           }
           if (!tag3){
               img3.setImageResource(getColor(null));
           }
        }
    }
}
