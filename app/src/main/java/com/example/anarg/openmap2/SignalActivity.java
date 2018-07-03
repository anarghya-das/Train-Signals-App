package com.example.anarg.openmap2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.eclipsesource.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SignalActivity extends AppCompatActivity implements AsyncResponse {
    private String trainName,trackName,android_id,audioLanguage;
    private int trainNo;
    private long phone;
    private ImageView img1,img2,img3;
    private TextView tv1,tv2,tv3;
    private TextView b;
    private boolean mediaPause;
    private MediaPlayer mediaPlayer,speech_green_en,speech_red_en,speech_yellow_en,
            speech_yellowyellow_en,speech_green_hi,speech_red_hi,speech_yellow_hi,
            speech_yellowyellow_hi;
    private ThreadControl threadControl;
    private GovPost govPost;
    private ArrayList<GovPost> g;
    private static final String govURl = "http://tms.affineit.com:4445/SignalAhead/Json/SignalAhead";
//    private static final String backEndServer= "http://irtrainsignalsystem.herokuapp.com/cgi-bin/senddevicelocation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signal_screen);
        mediaPause=false;
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0);
        img1=findViewById(R.id.firstSignal);
        img1.setTag("");
        img2=findViewById(R.id.secondSignal);
        img2.setTag("");
        img3=findViewById(R.id.thirdSignal);
        img3.setTag("");
        b= findViewById(R.id.langButton);
        tv1=findViewById(R.id.trainName);
        tv2=findViewById(R.id.trainNumber);
        tv3=findViewById(R.id.trackName);
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
        SharedPreferences preferences= getSharedPreferences("myPref",MODE_PRIVATE);
        audioLanguage= preferences.getString("audio","Hindi");
        b.setText(audioLanguage);
        mediaPlayer=MediaPlayer.create(this,R.raw.sound);
        speech_green_en=MediaPlayer.create(this,R.raw.green_en);
        speech_red_en=MediaPlayer.create(this,R.raw.red_en);
        speech_yellow_en=MediaPlayer.create(this,R.raw.yellow_en);
        speech_yellowyellow_en=MediaPlayer.create(this,R.raw.yellowyellow_en);
        speech_green_hi=MediaPlayer.create(this,R.raw.green_hi);
        speech_red_hi=MediaPlayer.create(this,R.raw.red_hi);
        speech_yellow_hi=MediaPlayer.create(this,R.raw.yellow_hi);
        speech_yellowyellow_hi=MediaPlayer.create(this,R.raw.yellowyellow_hi);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu= navigation.getMenu();
        MenuItem menuItem= menu.getItem(1);
        menuItem.setChecked(true);
        threadControl=new ThreadControl();
        govPost= new GovPost(trainName,this,threadControl,this);
        govPost.execute(govURl);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.home:
                    finish();
                    break;
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
//        new NotActiveTask().execute(backEndServer,jsonPost("notactive"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        endAllSounds();
        for (GovPost go: g) {
            go.cancel(true);
            threadControl.cancel();
        }
    }

    @Override
    public void processFinish(String output) {
        if (output.equals("null")){
            threadControl.pause();
            mHandler.removeCallbacks(timerTask);
            exceptionRaised("Connection Error","There was a problem connecting to the Server.\nPlease try again later.");
        }
    }

    private void endAllSounds() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        if (speech_green_en.isPlaying()) {
            speech_green_en.stop();
        }
        if (speech_green_hi.isPlaying()) {
            speech_green_en.stop();
        }
        if (speech_red_en.isPlaying()) {
            speech_red_en.stop();
        }
        if (speech_red_hi.isPlaying()) {
            speech_red_hi.stop();
        }
        if (speech_yellow_en.isPlaying()) {
            speech_yellow_en.stop();
        }
        if (speech_yellow_hi.isPlaying()) {
            speech_yellow_hi.stop();
        }
        if (speech_yellowyellow_en.isPlaying()) {
            speech_yellowyellow_en.stop();
        }
        if (speech_yellowyellow_hi.isPlaying()) {
            speech_yellowyellow_hi.stop();
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
                govPost= new GovPost(trainName,SignalActivity.this,threadControl,SignalActivity.this);
                govPost.execute(govURl); //backEndServer,jsonPost("active")
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
    public void createSignal(ArrayList<Signal> signals,Train t){
        trackName=t.getTrackName();
        tv3.setText("Track Name: "+trackName);
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
                            if(!mediaPause) {
                                mediaPlayer.start();
                                playSpeech(s, audioLanguage);
                            }
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

    private void playSpeech(Signal s,String so) {
        if (so.equals("Hindi")) {
            switch (s.getSignalAspect()) {
                case "Red":
                    speech_red_hi.start();
                    break;
                case "Green":
                    speech_green_hi.start();
                    break;
                case "Yellow":
                    speech_yellow_hi.start();
                    break;
                case "YellowYellow":
                    speech_yellowyellow_hi.start();
                    break;
            }
        }
        else if (so.equals("English")){
            switch (s.getSignalAspect()) {
                case "Red":
                    speech_red_en.start();
                    break;
                case "Green":
                    speech_green_en.start();
                    break;
                case "Yellow":
                    speech_yellow_en.start();
                    break;
                case "YellowYellow":
                    speech_yellowyellow_en.start();
                    break;
            }
        }
    }

    public void changeLanguage(View view) {
        SharedPreferences preferences=getSharedPreferences("myPref",MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        if (b.getText().equals("Hindi")){
            audioLanguage="English";
            b.setText(audioLanguage);
            editor.putString("audio",audioLanguage);
            editor.apply();
        }else if (b.getText().equals("English")){
            audioLanguage="Hindi";
            b.setText(audioLanguage);
            editor.putString("audio",audioLanguage);
            editor.apply();
        }
    }

    public void soundChange(View view) {
        FloatingActionButton button= findViewById(R.id.soundButton);
        if (button.getTag().equals("audio")){
            mediaPause=true;
            button.setTag("noaudio");
            button.setImageResource(R.drawable.noaudio);
        }else if (button.getTag().equals("noaudio")){
            mediaPause=false;
            button.setTag("audio");
            button.setImageResource(R.drawable.audio);
        }
    }

    public void exceptionRaised(String title,String body) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage(body)
                .setTitle(title);
        builder.setNegativeButton("Restart", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                Intent i=getIntent();
                startActivity(i);
            }
        });
        builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i=new Intent(SignalActivity.this,MainScreenActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("Exit",true);
                startActivity(i);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

}
