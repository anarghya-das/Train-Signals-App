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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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


public class MainActivity extends AppCompatActivity implements AsyncResponse{ //AppCompatActivity
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private MapView map = null;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationoverlay;
    private BackEnd backend;
    private ArrayList<Marker> allMarkers;
    private HashMap<Signal,Marker> signalMarker;
    private ThreadControl threadControl;
    private String trainName,trackName;
    private int trainNo;
    private long phone;
    private String android_id,audioLanguage;
    private IGeoPoint myLocation;
    private RequestTask requestTask;
    private double user_Lat,user_Long;
    private static final String reqURl = "http://irtrainsignalsystem.herokuapp.com/cgi-bin/signals";
    private static final String govURl = "http://tms.affineit.com:4445/SignalAhead/Json/SignalAhead";
//    private static final String backEndServer= "http://irtrainsignalsystem.herokuapp.com/cgi-bin/senddevicelocation";
    private MediaPlayer mediaPlayer,speech_green_en,speech_red_en,speech_yellow_en,
            speech_yellowyellow_en,speech_green_hi,speech_red_hi,speech_yellow_hi,
            speech_yellowyellow_hi;
    private boolean mediaPause,firstChange;
    private TextView b;
    private boolean locationPermission;
    private ArrayList<RequestTask> g;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setTraceLifecycle(true);
        //handle permissions first, before map is created. not depicted here
        // Write you code here if permission already given.
        //load/initialize the osmdroid configuration, this can be done
        locationPermission=false;
        mediaPause=false;
        firstChange=false;
        allMarkers = new ArrayList<>();
        signalMarker=new HashMap<>();
        g=new ArrayList<>();
        user_Long=0.0;
        user_Lat=0.0;
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string
        //inflate and create the map
        setContentView(R.layout.activity_main);
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0);
        b=findViewById(R.id.langButton);
        backend = new BackEnd();
        threadControl = new ThreadControl();
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
        Intent i = getIntent();
        trainName = i.getStringExtra("Signal");
        trainNo = i.getIntExtra("TrainNumber",0);
        trackName = i.getStringExtra("TrackName");
        phone = i.getLongExtra("Phone", 0);
        android_id = i.getStringExtra("id");
        requestTask = new RequestTask(backend, this, threadControl, trainName,this);
        requestTask.execute(reqURl, govURl);
//        setMapCenter();
        askPermission(Manifest.permission.ACCESS_FINE_LOCATION,MY_PERMISSIONS_REQUEST_LOCATION);
        if (locationPermission){
            GpsMyLocationProvider gp = new GpsMyLocationProvider(getApplicationContext());
            myLocationoverlay = new MyLocationNewOverlay(gp, map);
            myLocationoverlay.enableMyLocation();
            myLocationoverlay.setDrawAccuracyEnabled(false);
            Bitmap bitmapIcon = BitmapFactory.decodeResource(getResources(), R.drawable.train);
            myLocationoverlay.setPersonIcon(bitmapIcon);
            map.getOverlays().add(myLocationoverlay);
            myLocation=myLocationoverlay.getMyLocation();
            map.invalidate();
//            Toast.makeText(this,"latitude: "+myLocation.getLatitude()+", Longitude: "+myLocation.getLongitude(),Toast.LENGTH_SHORT).show();
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

    public MapView getMap() { return map; }

    public void createBottomBar(){
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu= navigation.getMenu();
        MenuItem menuItem= menu.getItem(2);
        menuItem.setChecked(true);
    }

    public void createMap(){
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.home:
                    break;
                case R.id.map_view:
                    break;
                case R.id.signal_view:
                    finish();
                    break;
            }
            return false;
        }
    };

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

        private Handler mHandler = new Handler();
        private Runnable timerTask = new Runnable() {
            @Override
            public void run() {
                if (requestTask.getStatus()== AsyncTask.Status.FINISHED) {
                    requestTask = new RequestTask(backend, MainActivity.this, threadControl, trainName,MainActivity.this);
                    requestTask.execute("", govURl);// backEndServer
                    g.add(requestTask);
                }
                mHandler.postDelayed(timerTask, 1);
            }};

    public void setMapCenter(GeoPoint g){
        mapController = map.getController();
        mapController.setZoom(15.6f);
//        GeoPoint g=new GeoPoint(22.578802, 88.365743);
        mapController.setCenter(g);
    }




    public boolean checkCurrentLocation() {

        if (!locationPermission) {
            return false;
        } else {
            myLocation = myLocationoverlay.getMyLocation();
            double user_Lat1 = myLocation.getLatitude();
            double user_Long1 = myLocation.getLongitude();
            if (user_Lat==user_Lat1||user_Long==user_Long1){
                return false;
            }else {
                user_Lat=user_Lat1;
                user_Long=user_Long1;
                return true;
            }
        }
    }

    private void locationToast(){
        if (!locationPermission) {
            Toast.makeText(this, "Enable Location permission to Use this!", Toast.LENGTH_SHORT).show();
        } else {
            mapController = map.getController();
//            mapController.setZoom(15.6f);
            if (myLocation != null) {
                mapController.animateTo(myLocation);
            Toast.makeText(this, "Latitude: " + myLocation.getLatitude() +
                    ", Longitude: " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location Not Found!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onResume() {
        super.onResume();
        mHandler.post(timerTask);
        threadControl.resume();
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
        mHandler.removeCallbacks(timerTask);
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        if (map!=null) {
            map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
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

    @Override
    protected void onStop() {
        super.onStop();
//        new NotActiveTask().execute(backEndServer,jsonPost("notactive"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        endAllSounds();
        for (RequestTask go: g) {
            go.cancel(true);
            threadControl.cancel();
        }
    }

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

    public void sync(View view) {
        locationToast();
    }

    public void populateMarkers(HashMap<String, GeoPoint> h) {
        for (String s: h.keySet()){
            allMarkers.add(configMarker(h.get(s),s,null));
        }
    }

    public void addMarker(Marker marker){
        map.getOverlays().add(marker);
        map.invalidate();
    }

    public void removeMarker(Marker marker){
            map.getOverlays().remove(marker);
    }

    //Experiment
    private Signal fromIndex(int n){
        for (Signal s: signalMarker.keySet()){
            if (s.getIndex()==n){
                return s;
            }
        }
        return null;
    }
    public void updateSignalMap(ArrayList<Signal> s){
        boolean change=false;
        firstChange=false;
        if (s.size()!=0) {
            for (Signal si : s) {
                Signal fromHash = fromIndex(si.getIndex());
                if (fromHash != null && checkSignalWithMarker(si) != null) {
                    if (!fromHash.getSignalID().equals(si.getSignalID())) {
                        if (si.getIndex()==1){
                            firstChange=true;
                        }
                        change=true;
                        removeMarker(signalMarker.get(fromHash));
                        signalMarker.remove(fromHash);
                        signalMarker.put(si, checkSignalWithMarker(si));
                    }
                } else if (fromHash == null && checkSignalWithMarker(si) != null) {
                    if (si.getIndex()==1){
                        firstChange=true;
                    }
                    signalMarker.put(si, checkSignalWithMarker(si));
                }
            }
            if (change) {
                addToMap2(signalMarker);
            }
        }
    }
    private void addToMap2(HashMap<Signal,Marker> m){
        for (Signal s: m.keySet()){
            if (s.getIndex()==1&&firstChange&&!mediaPause){
                mediaPlayer.start();
                playSpeech(s,audioLanguage);
            }
            addColorSignal(s,m.get(s));
            addMarker(m.get(s));
        }
    }
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
    //close

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

    private Marker checkSignalWithMarker(Signal s) {
            for (Marker m : allMarkers) {
                if (m.getTitle().equals(s.getSignalID())) {
                    return m;
                }
            }
        return null;
    }


    private void askPermission(String permission,int requestCode){
        if (ContextCompat.checkSelfPermission(this,permission)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{permission},requestCode);
        }else {
            locationPermission=true;
        }
    }

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
                    myLocationoverlay.setDrawAccuracyEnabled(false);
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


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
//            threadControl.cancel();
            finish();
        }
        return super.onKeyDown(keyCode, event);
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
                Intent i=new Intent(MainActivity.this,MainScreenActivity.class);
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