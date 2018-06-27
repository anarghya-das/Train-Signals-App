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
import java.util.HashMap;

public class SignalActivity extends AppCompatActivity {
    private String trainName,trackName,android_id;
    private int trainNo;
    private long phone;
    private ImageView img1,img2,img3;
    private TextView tv1,tv2,tv3,tv4;
    private MediaPlayer mediaPlayer,speech_green,speech_red,speech_yellow,speech_yellowyellow;
    private ThreadControl threadControl;
    private GovPost govPost;
    private ArrayList<GovPost> g;
    private static final String govURl = "http://tms.affineit.com:4445/SignalAhead/Json/SignalAhead";
    private static final String backEndServer= "http://irtrainsignalsystem.herokuapp.com/cgi-bin/senddevicelocation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signal_screen);
        img1=findViewById(R.id.firstSignal);
        img1.setTag("");
        img2=findViewById(R.id.secondSignal);
        img2.setTag("");
        img3=findViewById(R.id.thirdSignal);
        img3.setTag("");
        tv1=findViewById(R.id.trainName);
        tv2=findViewById(R.id.trainNumber);
        tv3=findViewById(R.id.trackName);
        tv4=findViewById(R.id.phoneNumber);
        g=new ArrayList<>();
        Intent i = getIntent();
        trainName = i.getStringExtra("Signal");
        trainNo = i.getIntExtra("TrainNumber",0);
        trackName = i.getStringExtra("TrackName");
        phone = i.getLongExtra("Phone", 0);
        android_id=i.getStringExtra("id");
        tv1.setText("Train Name: "+trainName);
        tv2.setText("Train Number: "+trainNo);
        tv3.setText("Track Name: "+trackName);
        tv4.setText("Phone Number: "+phone);
        mediaPlayer=MediaPlayer.create(this,R.raw.sound);
        mediaPlayer.setLooping(false);
        speech_green=MediaPlayer.create(this,R.raw.green);
        speech_green.setLooping(false);
        speech_red=MediaPlayer.create(this,R.raw.red);
        speech_red.setLooping(false);
        speech_yellow=MediaPlayer.create(this,R.raw.yellow);
        speech_yellow.setLooping(false);
        speech_yellowyellow=MediaPlayer.create(this,R.raw.yellowyellow);
        speech_yellowyellow.setLooping(false);
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
    public void createSignal(ArrayList<Signal> signals){
        if (signals!=null) {
            if (signals.size()==0){
                img1.setImageResource(getColor(null));
                img2.setImageResource(getColor(null));
                img3.setImageResource(getColor(null));
            }else {
                for (Signal s : signals) {
                        if (s.getIndex() == 1&&!s.getSignalID().equals(img1.getTag())) {
                            img1.setImageResource(getColor(s));
                            img1.setTag(s.getSignalID());
                                mediaPlayer.start();
                                playSpeech(s);
                        } else if (s.getIndex() == 2&&!s.getSignalID().equals(img1.getTag())) {
                            img2.setImageResource(getColor(s));
                            img2.setTag(s.getSignalID());
                        } else if (s.getIndex() == 3&&!s.getSignalID().equals(img1.getTag())) {
                            img3.setImageResource(getColor(s));
                            img3.setTag(s.getSignalID());
                        }
                }
            }
        }
    }

    private void playSpeech(Signal s) {
        switch (s.getSignalAspect()){
            case "Red":
                speech_red.start();
                break;
            case "Green":
                speech_green.start();
                break;
            case "Yellow":
                speech_yellow.start();
                break;
            case "YellowYellow":
                speech_yellowyellow.start();
                break;
        }
    }
}
