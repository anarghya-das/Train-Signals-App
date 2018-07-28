package com.example.anarg.openmap2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.eclipsesource.json.JsonObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;

/**
 * This class controls the map view of the signals plotted on their respective geo locations.
 * @author Anarghya Das
 */
public class MainActivity extends AppCompatActivity implements AsyncResponse{ //AppCompatActivity
    //Request integer for runtime location request
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    //Mapview which stores the map to be displayed
    private MapView map = null;
    //Map controller
    private IMapController mapController;
    //Stores the current location overlay of the user
    private MyLocationNewOverlay myLocationoverlay;
    //Reference to the backend class
    private BackEnd backend;
    //Stores all the markers on the map
    private ArrayList<Marker> allMarkers;
    //Stores the current signals with their corresponding Markers
    private HashMap<Signal,Marker> signalMarker;
    //Reference to the threadControl class
    private ThreadControl threadControl;
    //Stores the user information
    private String trainName,trackName;
    private int trainNo;
    private long phone;
    //Stores the android ID and the current audio language
    private String android_id,audioLanguage;
    //Stores the current location coordinates of the user
    private IGeoPoint myLocation;
    //Reference to the Request Task async Task
    private RequestTask requestTask;
    private double user_Lat,user_Long;
    //Stores the URL of the server from where the coordinates of signals are received
//    private static final String reqURl = "http://14.139.219.37/railway/jsonrender.php";
    private static final String reqURl = "https://irtrainsignalsystem.herokuapp.com/cgi-bin/signals";
//    private static final String reqURl = "http://192.168.0.102/railway/signals.cgi";
    //Stores the URL of the TMS server from where the train data is fetched
    private static final String tmsURL = "http://tms.affineit.com:4445/SignalAhead/Json/SignalAhead";

//    private static final String backEndServer= "http://irtrainsignalsystem.herokuapp.com/cgi-bin/senddevicelocation";

    //Stores the media references of the different languages the audio is in
    private MediaPlayer mediaPlayer,speech_green_en,speech_red_en,speech_yellow_en,
        speech_yellowyellow_en,speech_green_hi,speech_red_hi,speech_yellow_hi,
        speech_yellowyellow_hi,speech_green_b,speech_red_b,speech_yellow_b,speech_yellowyellow_b;
    //Stores the pausable, error, repeat and change conditions
    private boolean mediaPause,firstChange,repeat,error;
    //Stores the change, repeat and error Frequencies
    private int repeatFrequency,changeFrequnecy,errorFrequency;
    //Stores the repeat timer reference which repeats the audio
    private RepeatTimer repeatTimer;
    //Stores the current signal
    private Signal currentSignal;
    //Store the seekBar reference
    private SeekBar seekBar;
    //Stores the repeat and pause button reference
    private FloatingActionButton repeatButton,audioButton;
    //Controls the timer
    private Timer timer;
    //Stores the changing language text view reference.
    private TextView b;
    //Stores the alert dialog reference
    private AlertDialog dialog;
    //Stores whether the current location permission is enabled or not
    private boolean locationPermission;
    //Timeout duration of the app after it encounters an error
    private static final int TIMEOUT_ERROR_TIME=60000;//in milliseconds ~ 60 seconds
    //Checks whether the initial connection to the database server is okay or not
    private boolean initialError,restart;
    //Stores the location manager reference which checks whether GPS is on or not
    private LocationManager manager;

    private AlertDialog loadingDialog;

    /**
     * Initialises all the above instance variables
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Loading Time", "mapStarted ");
        super.onCreate(savedInstanceState);
//        setTraceLifecycle(true);
        //handle permissions first, before map is created. not depicted here
        // Write you code here if permission already given.
        //load/initialize the osmdroid configuration, this can be done
            locationPermission = false;
            firstChange = false;
            repeat = true;
            error = false;
            errorFrequency = 0;
            changeFrequnecy = 10;
            repeatFrequency = 10;
            currentSignal = null;
            repeatTimer = new RepeatTimer();
            allMarkers = new ArrayList<>();
            signalMarker = new HashMap<>();
            user_Long = 0.0;
            user_Lat = 0.0;
            initialError=false;
            restart=false;
            Context ctx = getApplicationContext();
            Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
            //setting this before the layout is inflated is a good idea
            //it 'should' ensure that the map has a writable location for the map cache, even without permissions
            //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
            //see also StorageUtils
            //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string
            //inflate and create the map
            setContentView(R.layout.activity_main);
            createBottomBar();
            createMap();
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            am.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                    0);
            b = findViewById(R.id.langButton);
            audioButton = findViewById(R.id.soundButton);
            seekBar = findViewById(R.id.repeatBar);
            seekBar.setMax(30);
            seekBar.setProgress(repeatFrequency);
            seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
            repeatButton = findViewById(R.id.repeatButton);
            backend = new BackEnd();
            threadControl = new ThreadControl();
            mediaPlayer = MediaPlayer.create(this, R.raw.sound);
            speech_green_en = MediaPlayer.create(this, R.raw.green_en);
            speech_red_en = MediaPlayer.create(this, R.raw.red_en);
            speech_yellow_en = MediaPlayer.create(this, R.raw.yellow_en);
            speech_yellowyellow_en = MediaPlayer.create(this, R.raw.yellowyellow_en);
            speech_green_hi = MediaPlayer.create(this, R.raw.green_hi);
            speech_red_hi = MediaPlayer.create(this, R.raw.red_hi);
            speech_yellow_hi = MediaPlayer.create(this, R.raw.yellow_hi);
            speech_yellowyellow_hi = MediaPlayer.create(this, R.raw.yellowyellow_hi);
            speech_green_b = MediaPlayer.create(this, R.raw.green_b);
            speech_red_b = MediaPlayer.create(this, R.raw.red_b);
            speech_yellow_b = MediaPlayer.create(this, R.raw.yellow_b);
            speech_yellowyellow_b = MediaPlayer.create(this, R.raw.yellowyellow_b);
            Intent i = getIntent();
            trainName = i.getStringExtra("Signal");
            trainNo = i.getIntExtra("TrainNumber", 0);
            trackName = i.getStringExtra("TrackName");
            phone = i.getLongExtra("Phone", 0);
            android_id = i.getStringExtra("id");
            mediaPause = i.getBooleanExtra("sound", false);
            audioLanguage = i.getStringExtra("language");
            b.setText(audioLanguage);
            requestTask = new RequestTask(backend, this, threadControl, trainName, this);
            requestTask.execute(reqURl, tmsURL);
            if (requestTask.getStatus()==RequestTask.Status.RUNNING){
                load("Please wait while Map Loads");
            }
//        setMapCenter();
            askPermission(Manifest.permission.ACCESS_FINE_LOCATION, MY_PERMISSIONS_REQUEST_LOCATION);
        manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }
    }
    /**
     * This method runs after the async task is complete and executes proper functions based on the
     * result received.
     * @param output Stores the result of the async task after completion.
     */
    @Override
    public void processFinish(String output) {
        if (output.equals("null")) {
            if (!isRunning){
                mHandler.post(timerTask);
            }
            if (dialog == null) {
                mediaPause = true;
                endAllSounds();
                error=true;
                for (Marker m: signalMarker.values()){
                    removeMarker(m);
                    addColorSignal(null,m);
                    addMarker(m);
                }
                exceptionRaised("Connection Error", "Please wait while we try to reconnect." +
                        "\nIn the mean while check if your internet connection is working.", false);
            } else if (!dialog.isShowing()) {
                mediaPause = true;
                endAllSounds();
                for (Marker m: signalMarker.values()){
                    removeMarker(m);
                    addColorSignal(null,m);
                    addMarker(m);
                }
                error=true;
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
            for (Signal s: signalMarker.keySet()){
                removeMarker(signalMarker.get(s));
                addColorSignal(s,signalMarker.get(s));
                addMarker(signalMarker.get(s));
            }
            if (audioButton.getTag().equals("noaudio")) {
                mediaPause = true;
            }else if (audioButton.getTag().equals("audio")){
                mediaPause=false;
            }
        }else if (output.equals("null1")){
            initialError=true;
            error=true;
            if (dialog == null) {
                mediaPause = true;
                endAllSounds();
                exceptionRaised("Connection Error", "Please wait while we try to reconnect.",false);
                mHandler.post(timerTask);
            }else if (!dialog.isShowing()){
                mediaPause = true;
                endAllSounds();
                exceptionRaised("Connection Error", "Please wait while we try to reconnect.",false);
            }else if (errorFrequency>=TIMEOUT_ERROR_TIME){
                dialog.dismiss();
                exceptionRaised("Connection Error", "Could not reconnect." +
                        "\nThere might be some problem, please try again later!", true);
                errorFrequency=0;
            }
        }else if (output.equals("okay1")){
            loadingDialog.dismiss();
            if (!isRunning) {
                mHandler.post(timerTask);
                Log.d("Loading Time", "mapDone ");
            }
            if (locationPermission&&!restart) {
                GpsMyLocationProvider gp = new GpsMyLocationProvider(getApplicationContext());
                myLocationoverlay = new MyLocationNewOverlay(gp, map);
                myLocationoverlay.enableMyLocation();
                myLocationoverlay.setDrawAccuracyEnabled(false);
                Bitmap bitmapIcon = BitmapFactory.decodeResource(getResources(), R.drawable.train);
                myLocationoverlay.setPersonIcon(bitmapIcon);
                if(myLocationoverlay.getLastFix()!=null) {
                    mapController = map.getController();
                    mapController.setZoom(18.6f);
                    myLocationoverlay.enableFollowLocation();
                }
                map.getOverlays().add(myLocationoverlay);
                myLocation = myLocationoverlay.getMyLocation();
                map.invalidate();
                restart=true;
//            Toast.makeText(this,"latitude: "+myLocation.getLatitude()+", Longitude: "+myLocation.getLongitude(),Toast.LENGTH_SHORT).show();
            }
            if (dialog!=null&&dialog.isShowing()) {
                initialError = false;
                error = false;
                errorFrequency = 0;
                dialog.dismiss();
                if (audioButton.getTag().equals("noaudio")) {
                    mediaPause = true;
                } else if (audioButton.getTag().equals("audio")) {
                    mediaPause = false;
                }
            }
        }
    }
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
            Toast.makeText(MainActivity.this,"Current Repetition Frequency: "+changeFrequnecy+" seconds",Toast.LENGTH_SHORT).show();
            repeat = repeatFrequency != 0;
            seekBar.setVisibility(View.INVISIBLE);
            repeatButton.setVisibility(View.VISIBLE);
        }
    };
    /**
     * Getter which returns the reference to the current map
     */
    public MapView getMap() { return map; }

    /**
     * Creates the bottom navigation bar
     */
    public void createBottomBar(){
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu= navigation.getMenu();
        MenuItem menuItem= menu.getItem(1);
        menuItem.setChecked(true);
    }

    /**
     * Creates and initializes the the map from the OpenStreetMap tile source
     */
    public void createMap(){
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
    }
    /**
     * Controls the onClick actions of the bottom navigation bar
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.map_view:
                    break;
                case R.id.signal_view:
                    Intent intent=new Intent(MainActivity.this,SignalActivity.class);
                    intent.putExtra("language",audioLanguage);
                    setResult(RESULT_OK,intent);
                    finish();
                    break;
            }
            return false;
        }
    };
    /**
     * Creates a json object of all the user inputs along with android device ID to send it to the
     * server
     * @return The json string to be sent to the server
     */
    public String jsonPost(String status){
        JsonObject o=new JsonObject();
        o.add("deviceId",android_id);
        JsonObject o2=new JsonObject();
        o2.add("trainNo",trainNo);
        o2.add("phone",phone);
        o2.add("trainName",trainName);
        o2.add("trackName",trackName);
        o.add("info",o2);
        JsonObject o3=new JsonObject();
        o3.add("latitude",user_Lat);
        o3.add("longitude",user_Long);
        o.add("coordinate",o3);
        o.add("status", status);
//        Log.d("worksend", o.toString());
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
                if (requestTask.getStatus()==AsyncTask.Status.FINISHED&&initialError){
                    requestTask = new RequestTask(backend, MainActivity.this, threadControl, trainName, MainActivity.this);
                    requestTask.execute(reqURl, tmsURL);
                }
                else if (requestTask.getStatus()== AsyncTask.Status.FINISHED) {
                    requestTask = new RequestTask(backend, MainActivity.this, threadControl, trainName,MainActivity.this);
                    requestTask.execute("", tmsURL);// backEndServer
                    isRunning=true;
                }
                if (error){
                    errorFrequency++;
                    Log.d("ERRORTEST", "ET: "+ errorFrequency);
                }
            }};
        /**
         * Sets the map camera to the first signal marker when the map initializes
         */
    public void setMapCenter(GeoPoint g){
        mapController = map.getController();
        if (g!=null) {
            mapController.setZoom(18.6f);
//        GeoPoint g=new GeoPoint(22.578802, 88.365743);
            mapController.setCenter(g);
            mapController.animateTo(g);
        }else{
            mapController.setZoom(13f);
            GeoPoint kolkata=new GeoPoint(22.5726, 88.3639);
            mapController.animateTo(kolkata);
        }
    }

    /**
     * Sets the map camera to the current user location when map is initialized
     */
    public void setMapCenterOnLocation(){
        mapController = map.getController();
        mapController.setZoom(18.6f);
//        mapController.animateTo(myLocation);
        mapController.setCenter(myLocation);
    }

    /**
     * Checks whether the location permission is enabled or not and stores the user coordinates if
     * it is enabled.
     * @return true if user coordinate stored or false otherwise
     */
    public boolean checkCurrentLocation() {

        if (!locationPermission) {
            return false;
        } else {
            myLocation = myLocationoverlay.getMyLocation();
            double user_Lat1=0.0,user_Long1=0.0;
            if (myLocation!=null) {
                 user_Lat1 = myLocation.getLatitude();
                 user_Long1 = myLocation.getLongitude();
            }
            if (user_Lat==user_Lat1||user_Long==user_Long1){
                return false;
            }else {
                user_Lat=user_Lat1;
                user_Long=user_Long1;
                return true;
            }
        }
    }

    /**
     *Switches the camera to the focus on the current user location.
     */
    private void locationToast(){
        if (!locationPermission||!manager.isProviderEnabled( LocationManager.GPS_PROVIDER )) {
            Toast.makeText(this, "Enable Location permission to Use this!", Toast.LENGTH_SHORT).show();
        } else {
            mapController = map.getController();
//            mapController.setZoom(15.6f);
            if (myLocation != null) {
                mapController.animateTo(myLocation);
                myLocationoverlay.enableFollowLocation();
//                mapController.setCenter(myLocation);
            Toast.makeText(this, "Latitude: " + myLocation.getLatitude() +
                    ", Longitude: " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location Not Found!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onResume() {
        super.onResume();
        mediaPause = false;
        threadControl.resume();
        if (requestTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
            mHandler.post(timerTask);
        }
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        if (map!=null) {
            map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
        }
    }

    public void onPause() {
        super.onPause();
        threadControl.pause();
        mediaPause=true;
        restart=true;
        endAllSounds();
        mHandler.removeCallbacks(timerTask);
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        if (map!=null) {
            map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
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
            speech_green_hi.stop();
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

    @Override
    protected void onStop() {
        super.onStop();
//        new NotActiveTask().execute(backEndServer,jsonPost("notactive"));
    }
    /**
     * Stops all the sound media playing currently and removes all the aysnc tasks running in the memory
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        endAllSounds();
        if (repeatTimer.isRunning()) {
            timer.cancel();
        }
            requestTask.cancel(true);
            threadControl.cancel();
    }
    /**
     * Repeats the audio notification in the frequency set by the user.
     */
    private void repeatChecks(){
        if(!mediaPause) {
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
     * Creates the marker and adds the properties to it based on the signal
     * @param gp Coordinate where the marker will be placed
     * @param description Signal ID of the marker
     * @param s //Signal reference corresponding to that marker
     * @return marker after creation
     */
    private Marker configMarker(GeoPoint gp, String description, Signal s) {
            Marker marker = new Marker(map);
            marker.setPosition(gp);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(description);
            addColorSignal(s, marker);
            return marker;
//        map.getOverlays().clear();
//        map.invalidate();
    }
    /**
     * Adds the color to the specific marker based on the signal reference
     * @param so Signal Reference
     * @param marker Marker reference
     */
    private void addColorSignal(Signal so,Marker marker){

        if (so==null){
            marker.setIcon(getResources().getDrawable(R.drawable.empty));
            marker.setId("empty");
        } else if (so.getSignalAspect().equals("Red")){
            marker.setIcon(getResources().getDrawable(R.drawable.red));
            marker.setId("Red");
        }else if (so.getSignalAspect().equals("Green")) {
            marker.setIcon(getResources().getDrawable(R.drawable.green));
            marker.setId("Green");
        } else if (so.getSignalAspect().equals("Yellow")) {
            marker.setIcon(getResources().getDrawable(R.drawable.yellow));
            marker.setId("Yellow");
        } else if (so.getSignalAspect().equals("YellowYellow")) {
            marker.setIcon(getResources().getDrawable(R.drawable.yellowyellow));
            marker.setId("YellowYellow");
        }

    }
    /**
     *onClick listener of the GPS button which calls locationToast() method
     */
    public void sync(View view) {
        locationToast();
    }
    /**
     * Creates the initial list of markers from the server based on their geo coordinates.
     * @param h HashMap of the signal IDs with their corresponding geo coordinates.
     */
    public void populateMarkers(HashMap<String, GeoPoint> h) {
        for (String s: h.keySet()){
            allMarkers.add(configMarker(h.get(s),s,null));
        }
    }
    /**
     * Adds the marker on the map
     * @param marker marker to be added
     */
    public void addMarker(Marker marker){
        map.getOverlays().add(marker);
        map.invalidate();
    }
    /**
     * Removes the marker on the map
     * @param marker marker to be removed
     */
    public void removeMarker(Marker marker){
            map.getOverlays().remove(marker);
    }
    /**
     * Returns the signal of the corresponding index in the HashMap of Signal and Marker
     * @param n the index of the signal
     * @return signal to be returned
     */
    private Signal fromIndex(int n){
        for (Signal s: signalMarker.keySet()){
            if (s.getIndex()==n){
                return s;
            }
        }
        return null;
    }
    /**
     * Updates the markers based on the new array list of signals fetched form the server
     * @param s array list of signal
     */
    public void updateSignalMap(ArrayList<Signal> s) {
        boolean change = false;
        firstChange = false;
        repeatChecks();
        if (s.size() != 0) {
            for (Signal si : s) {
                Signal fromHash = fromIndex(si.getIndex());
                if (fromHash != null && checkSignalWithMarker(si) != null) {
                    if (!fromHash.getSignalAspect().equals(si.getSignalAspect())) {
                        if (si.getIndex() == 1) {
                            firstChange = true;
                        }
                        change = true;
                        removeMarker(signalMarker.get(fromHash));
                        signalMarker.remove(fromHash);
                        signalMarker.put(si, checkSignalWithMarker(si));
                    }
                } else if (fromHash == null && checkSignalWithMarker(si) != null) {
                    if (si.getIndex() == 1) {
                        firstChange = true;
                    }
                    signalMarker.put(si, checkSignalWithMarker(si));
                }
            }
            if (change) {
                addToMap2(signalMarker);
            }
        }
    }
    /**
     *Updates the current HashMap of Signal and Marker using the update logic
     * @param m HashMap of Signal and Marker
     */
    private void addToMap2(HashMap<Signal,Marker> m){
        for (Signal s: m.keySet()){
            if (s.getIndex()==1&&firstChange&&!mediaPause){
                mediaPlayer.start();
                playSpeech(s);
                //Repeat comes here same as Signal Activity
                if (repeat) {
                    if (!repeatTimer.isRunning()) {
                        currentSignal=s;
                        repeatTimer = new RepeatTimer(s, this);
                        timer = new Timer();
                        timer.scheduleAtFixedRate(repeatTimer, 0, repeatFrequency * 1000);
                    } else if (repeatTimer.isRunning()) {
                        currentSignal=s;
                        timer.cancel();
                        timer = new Timer();
                        repeatTimer = new RepeatTimer(s, this);
                        timer.scheduleAtFixedRate(repeatTimer, 0, repeatFrequency * 1000);
                    }
                }
            }
            addColorSignal(s,m.get(s));
            addMarker(m.get(s));
        }
    }
    /**
     * Adds the array list of Signals to the Map
     * @param signals array list of Signals
     */
    public void addSignalToMap(ArrayList<Signal> signals){
        if (signals.size()!=0) {
            for (Signal s : signals) {
                Marker m = checkSignalWithMarker(s);
                if (m != null) {
                    if (s.getIndex()==1){
                        firstChange=true;
                    }
                    signalMarker.put(s, m);
                }
            }
            addToMap2(signalMarker);
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
     * Checks whether the corresponding signal is in the array list of all markers
     * @param s Signal reference
     * @return marker corresponding to the signal
     */
    private Marker checkSignalWithMarker(Signal s) {
            for (Marker m : allMarkers) {
                if (m.getTitle().equals(s.getSignalID())) {
                    return m;
                }
            }
        return null;
    }

    /**
     * Generates the run time permission
     * @param permission The type of permission to be displayed
     * @param requestCode Request code of that particular permission
     */
    private void askPermission(String permission,int requestCode){
        if (ContextCompat.checkSelfPermission(this,permission)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{permission},requestCode);
        }else {
            locationPermission=true;
        }
    }
    /**
     * Displays the relevant information based on the runtime request input by the user
     * @param requestCode Request code
     * @param permissions Permissions asked for
     * @param grantResults Permissions granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    locationPermission=true;
                    GpsMyLocationProvider gp = new GpsMyLocationProvider(getApplicationContext());
                    myLocationoverlay = new MyLocationNewOverlay(gp, map);
                    myLocationoverlay.enableMyLocation();
                    Bitmap bitmapIcon = BitmapFactory.decodeResource(getResources(), R.drawable.train);
                    myLocationoverlay.setPersonIcon(bitmapIcon);
                    myLocationoverlay.setDrawAccuracyEnabled(false);
                    mapController = map.getController();
                    mapController.setZoom(18.6f);
                    myLocationoverlay.enableFollowLocation();
                    map.getOverlays().add(myLocationoverlay);
                    map.invalidate();
                    Toast.makeText(this,"Location Sharing Enabled!",Toast.LENGTH_SHORT).show();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this,"Location Sharing Disabled!",Toast.LENGTH_SHORT).show();
                }
                break;

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
    /**
     * Overrides the back button and finishes the activity.
     * @param keyCode keycode of the back key
     * @param event event to be performed
     * @return returns the new overridden key event
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            Intent intent=new Intent(MainActivity.this,SignalActivity.class);
            intent.putExtra("language",audioLanguage);
            setResult(RESULT_OK,intent);
            finish();
            finish();
        }
        return super.onKeyDown(keyCode, event);
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
                    Intent i = new Intent(MainActivity.this, MainScreenActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.putExtra("Exit", true);
                    startActivity(i);

                }
            });
        }
        dialog = builder.create();
        if (buttons) {
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Signal View", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }
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
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    /**
     * Method which creates the loading dialog when the data takes time to load
     */
    private void load(String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(body)
                .setTitle("Loading");
        loadingDialog = builder.create();
        loadingDialog.setCanceledOnTouchOutside(false);
//        loadingDialog.setCancelable(false);
        loadingDialog.show();
    }
}