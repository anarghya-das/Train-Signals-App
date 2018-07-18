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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.eclipsesource.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class shows the signal view with the next 3 signals in front of the train also displaying
 * the relevant train information entered by the user.
 * @author Anarghya Das
 */
public class SignalActivity extends AppCompatActivity implements AsyncResponse {
    //Stores the user information, android id and the preferred audio language of the app
    private String trainName,trackName,android_id,audioLanguage;
    private int trainNo;
    private long phone;
    //Stores the state of the next 3 signals
    private ImageView img1,img2,img3;
    private TextView tv1,tv2,tv3;
    //Text View which changes the preferred audio language
    private TextView b;
    //Stores the information whether the media is paused and error occurred or not
    private boolean mediaPause,error;
    //Stores the time for which the error has continued occurring
    private int errorFrequency;
    //Seek bar which controls the repeat frequency of the audio
    private SeekBar seekBar;
    //Stores the media references of the different languages the audio is in
    private MediaPlayer mediaPlayer,speech_green_en,speech_red_en,speech_yellow_en,
            speech_yellowyellow_en,speech_green_hi,speech_red_hi,speech_yellow_hi,
            speech_yellowyellow_hi,speech_green_b,speech_red_b,speech_yellow_b,speech_yellowyellow_b;
    //Stores the reference of thread control class
    private ThreadControl threadControl;
    //Stores the reference of SignalPostRequest async Task
    private SignalPostRequest SignalPostRequest;
    //Stores the current and changed repeat frequency for the audio
    private int repeatFrequency,changeFrequnecy;
    //Stores the condition whether repeat is on or off
    private boolean repeat;
    //Creates the repeat timer which repeats the audio
    private RepeatTimer repeatTimer;
    //Controls the repeat timer
    private Timer timer;
    //Alert Dialog reference to show errors
    private AlertDialog dialog;
    //Stores the reference of the next three signals of the train
    private Signal currentSignal,currentSignal2,currentSignal3;
    //Stores the reference of the mute button and the repeat button
    private FloatingActionButton audioButton,repeatButton;
    //Store the link to the TMS URL from where the data is fetched
    private static final String tmsURL = "http://tms.affineit.com:4445/SignalAhead/Json/SignalAhead";
    //Timeout duration of the app after it encounters an error
    private static final int TIMEOUT_ERROR_TIME=60000;//in milliseconds ~ 60 seconds

//    private static final String backEndServer= "http://irtrainsignalsystem.herokuapp.com/cgi-bin/senddevicelocation";
    //    private static final String backEndServer= "http://192.168.0.106/railway/senddevicelocations.cgi";

    /**
     * Initialises all the above instance variables
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signal_screen);
        repeatFrequency=10;
        changeFrequnecy=10;
        mediaPause=false;
        repeat=true;
        error=false;
        errorFrequency=0;
        repeatTimer=new RepeatTimer();
        currentSignal=null;
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
        repeatButton=findViewById(R.id.repeatButton);
        audioButton= findViewById(R.id.soundButton);
        seekBar=findViewById(R.id.repeatBar);
        seekBar.setMax(30);
        seekBar.setProgress(repeatFrequency);
        seekBar.setOnTouchListener(onTouchListener);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
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
        audioLanguage= preferences.getString("audio","Bengali");
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
        speech_green_b=MediaPlayer.create(this,R.raw.green_b);
        speech_red_b=MediaPlayer.create(this,R.raw.red_b);
        speech_yellow_b=MediaPlayer.create(this,R.raw.yellow_b);
        speech_yellowyellow_b=MediaPlayer.create(this,R.raw.yellowyellow_b);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu= navigation.getMenu();
        MenuItem menuItem= menu.getItem(1);
        menuItem.setChecked(true);
        threadControl=new ThreadControl();
        SignalPostRequest= new SignalPostRequest(trainName,this,threadControl,this);
        SignalPostRequest.execute(tmsURL);
    }
    /**
     * Sets the value of media pause
     * @param value media pause state
     */
    public void setMediaPause(boolean value){
        mediaPause=value;
    }
    /**
     * The onTouch listener of the seek bar which allows it to work properly in a Scroll View
     */
    private SeekBar.OnTouchListener onTouchListener= new SeekBar.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action)
            {
                case MotionEvent.ACTION_DOWN:
                    // Disallow ScrollView to intercept touch events.
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;

                case MotionEvent.ACTION_UP:
                    // Allow ScrollView to intercept touch events.
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }

            // Handle Seekbar touch events.
            v.onTouchEvent(event);
            return true;
        }
    };
    /**
     * onSeekBarChangeListener gets the changed output from the seek bar and does relevant job according
     * to the output.
     */
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener= new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
           changeFrequnecy=progress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Toast.makeText(SignalActivity.this,"Current Repetition Frequency: "+changeFrequnecy+" seconds",Toast.LENGTH_SHORT).show();
            repeat = repeatFrequency != 0;
            seekBar.setVisibility(View.INVISIBLE);
            repeatButton.setVisibility(View.VISIBLE);
        }
    };
    /**
     * Controls the onClick actions of the bottom navigation bar
     */
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
                    i.putExtra("sound",mediaPause);
                    i.putExtra("language",audioLanguage);
                    mediaPause=true;
                    endAllSounds();
                    threadControl.pause();
                    mHandler.removeCallbacks(timerTask);
                    SignalActivity.this.startActivity(i);
                    break;
            }
            return false;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
//        mediaPause=true;
//        threadControl.pause();
    }
    /**
     * Starts the Periodic async tasks and sets media pause to false
     */
    @Override
    protected void onResume() {
        super.onResume();
        mediaPause=false;
        mHandler.post(timerTask);
        threadControl.resume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    /**
     * Stops all the sound media playing currently and removes all the aysnc tasks running in the memory
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        new NotActiveTask().execute(backEndServer,jsonPost("notactive"));
        mHandler.removeCallbacks(timerTask);
        if (repeatTimer.isRunning()) {
            timer.cancel();
        }
        endAllSounds();
        SignalPostRequest.cancel(true);
        threadControl.cancel();
    }
    /**
     * This method runs after the async task is complete and executes proper functions based on the
     * result received.
     * @param output Stores the result of the async task after completion.
     */
    @Override
    public void processFinish(String output) {
        if (!isRunning){
            mHandler.post(timerTask);
        }
        if (output.equals("null")&&!isFinishing()) {
            if (dialog == null) {
                mediaPause = true;
                endAllSounds();
                error=true;
                img1.setImageResource(getColor(null));
                img2.setImageResource(getColor(null));
                img3.setImageResource(getColor(null));
                exceptionRaised("Connection Error", "Please wait while we try to reconnect." +
                        "\nIn the mean while check if your internet connection is working.", false);
            } else if (!dialog.isShowing()) {
                mediaPause = true;
                endAllSounds();
                error=true;
                img1.setImageResource(getColor(null));
                img2.setImageResource(getColor(null));
                img3.setImageResource(getColor(null));
                exceptionRaised("Connection Error", "Please wait while we try to reconnect." +
                        "\nIn the mean while check if your internet connection is working.", false);
            }else if (errorFrequency>=TIMEOUT_ERROR_TIME){
                dialog.dismiss();
                exceptionRaised("Connection Error", "Could not reconnect." +
                        "\nThere might be some problem, please try again later!", true);
                errorFrequency=0;
            }
        }else if (dialog!=null&&dialog.isShowing()&&output.equals("okay")){
            error=false;
            errorFrequency=0;
            dialog.dismiss();
            if (currentSignal!=null&&currentSignal2!=null&currentSignal3!=null) {
                img1.setImageResource(getColor(currentSignal));
                img2.setImageResource(getColor(currentSignal2));
                img3.setImageResource(getColor(currentSignal3));
            }
            if (audioButton.getTag().equals("noaudio")) {
                mediaPause = true;
            }else if (audioButton.getTag().equals("audio")){
                mediaPause=false;
            }
        }
    }
    /**
     * Stops all the sound media currently playing in the background
     */
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
        if (speech_green_b.isPlaying()){
            speech_green_b.stop();
        }
        if (speech_red_b.isPlaying()){
            speech_red_b.stop();
        }
        if (speech_yellowyellow_b.isPlaying()){
            speech_yellowyellow_b.stop();
        }
        if (speech_yellow_b.isPlaying()){
            speech_yellow_b.stop();
        }
    }
    /**
     * Creates a json object of all the user inputs along with android device ID to send it to the
     * server
     * @return The json string to be sent to the server
     */
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
    /**
     * Handler which creates a new async Task every second to fetch the data from the server and do
     * the relevant job after receiving the data.
     */
    private Handler mHandler = new Handler();
    private boolean isRunning=false;
    private Runnable timerTask = new Runnable() {
        @Override
        public void run() {
            if (SignalPostRequest.getStatus()== AsyncTask.Status.FINISHED) {
                Log.d("result", "run: ");
                SignalPostRequest= new SignalPostRequest(trainName,SignalActivity.this,threadControl,SignalActivity.this);
                SignalPostRequest.execute(tmsURL); //backEndServer,jsonPost("active")
                isRunning=true;
            }
            if (error){
                errorFrequency++;
            }
            mHandler.postDelayed(timerTask, 1);
        }};
    /**
     * Repeats the audio notification in the frequency set by the user.
     */
    private void repeatChecks(){
        if(!mediaPause&&currentSignal!=null) {
            if (repeatTimer.isRunning()) {
                if (changeFrequnecy == 0) {
                    repeatFrequency = changeFrequnecy;
                    timer.cancel();
                }
                if (repeatFrequency != changeFrequnecy) {
                    repeatFrequency = changeFrequnecy;
                    timer.cancel();
                    timer = new Timer();
                    repeatTimer = new RepeatTimer(currentSignal, this);
                    timer.scheduleAtFixedRate(repeatTimer, 0, repeatFrequency * 1000);
                }
            }
        }
    }
    /**
     *Returns the drawable integer reference based on the color of the signal passed.
     * @param s The signal
     * @return drawable reference of the signal
     */
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

    /**
     * Creates and updates the signal view in the activity based on the data received from the server
     * @param signals Array list of signals received from the server
     * @param t The current train which the user has selected
     */
    public void createSignal(ArrayList<Signal> signals,Train t){
        repeatChecks();
        trackName=t.getTrackName();
        tv3.setText("Track Name: "+trackName);
        if (signals!=null) {
            if (signals.size()==0){
                img1.setImageResource(getColor(null));
                img2.setImageResource(getColor(null));
                img3.setImageResource(getColor(null));
            }else {
                for (Signal s : signals) {
                        if (s.getIndex() == 1&&!s.getSignalAspect().equals(img1.getTag())) {
                            currentSignal=s;
                            img1.setImageResource(getColor(s));
                            img1.setTag(s.getSignalAspect());
                            if(!mediaPause) {
                                mediaPlayer.start();
                                playSpeech(s);
                                if (repeat) {
                                    if (!repeatTimer.isRunning()) {
                                        repeatTimer = new RepeatTimer(s, this);
                                        timer = new Timer();
                                        timer.scheduleAtFixedRate(repeatTimer, 0, repeatFrequency * 1000);
                                    } else if (repeatTimer.isRunning()) {
                                        timer.cancel();
                                        timer = new Timer();
                                        repeatTimer = new RepeatTimer(s, this);
                                        timer.scheduleAtFixedRate(repeatTimer, 0, repeatFrequency * 1000);
                                    }
                                }

                            }
                        } else if (s.getIndex() == 2&&!s.getSignalAspect().equals(img2.getTag())) {
                            currentSignal2=s;
                            img2.setImageResource(getColor(s));
                            img2.setTag(s.getSignalAspect());
                        } else if (s.getIndex() == 3&&!s.getSignalAspect().equals(img3.getTag())) {
                            currentSignal3=s;
                            img3.setImageResource(getColor(s));
                            img3.setTag(s.getSignalAspect());
                        }
                }
            }
        }
    }
    /**
     * Plays the audio (provided the media is not paused by the user)  corresponding of the current
     * audio language selected.
     * Language support as of now: English, Hindi, Bengali.
     * @param s Signal corresponding to which the audio will be played
     */
    public void playSpeech(Signal s) {
        if (!mediaPause) {
            switch (audioLanguage) {
                case "Hindi":
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
                    break;
                case "English":
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
                    break;
                case "Bengali":
                    switch (s.getSignalAspect()) {
                        case "Red":
                            speech_red_b.start();
                            break;
                        case "Green":
                            speech_green_b.start();
                            break;
                        case "Yellow":
                            speech_yellow_b.start();
                            break;
                        case "YellowYellow":
                            speech_yellowyellow_b.start();
                            break;
                    }
                    break;
            }
        }
    }
    /**
     * OnClick button handler which changes the audio language of the app based on user input
     */
    public void changeLanguage(View view) {
        SharedPreferences preferences=getSharedPreferences("myPref",MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        if (b.getText().equals("Bengali")){
            audioLanguage="Hindi";
            b.setText(audioLanguage);
            editor.putString("audio",audioLanguage);
            editor.apply();
        }else if (b.getText().equals("Hindi")){
            audioLanguage="English";
            b.setText(audioLanguage);
            editor.putString("audio",audioLanguage);
            editor.apply();
        }else if (b.getText().equals("English")){
            audioLanguage="Bengali";
            b.setText(audioLanguage);
            editor.putString("audio",audioLanguage);
            editor.apply();
        }
    }
    /**
     * onClick handler of the mute which stops or starts the media based on user input
     */
    public void soundChange(View view) {
        if (audioButton.getTag().equals("audio")){
            mediaPause=true;
            audioButton.setTag("noaudio");
            audioButton.setImageResource(R.drawable.noaudio);
        }else if (audioButton.getTag().equals("noaudio")){
            mediaPause=false;
            audioButton.setTag("audio");
            audioButton.setImageResource(R.drawable.audio);
        }
    }
    /**
     * Creates a custom dialog box.
     * @param title The title of the dialog box
     * @param body Body text of the dialog box
     * @param buttons If true buttons will appear else not
     */
    public void exceptionRaised(String title,String body,boolean buttons) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage(body)
                .setTitle(title);
        if (buttons) {
            builder.setNegativeButton("Restart", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    Intent i = getIntent();
                    startActivity(i);
                }
            });
            builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(SignalActivity.this, MainScreenActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.putExtra("Exit", true);
                    startActivity(i);
                }
            });
        }
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }
    /**
     *onClick button handler which shows the seek bar
     */
    public void repeatButtonHandler(View view) {
        seekBar.setVisibility(View.VISIBLE);
        repeatButton=findViewById(R.id.repeatButton);
        repeatButton.setVisibility(View.INVISIBLE);
    }
}
