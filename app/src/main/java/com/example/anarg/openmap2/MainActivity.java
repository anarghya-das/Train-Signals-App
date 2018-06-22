package com.example.anarg.openmap2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
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
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity { //AppCompatActivity
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private MapView map = null;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationoverlay;
    private BackEnd backend;
    private ArrayList<Marker> allMarkers;
    private ArrayList<Marker> currentMarkers;
    private ArrayList<Signal> currentSignals;
    private ThreadControl threadControl;
    private String trainName,trackName;
    private int trainNo;
    private long phone;
    private String android_id;
    private IGeoPoint myLocation;
    private RequestTask requestTask;
    private double user_Lat,user_Long;
    private static final String reqURl = "http://irtrainsignalsystem.herokuapp.com/cgi-bin/signals";
    private static final String govURl = "http://tms.affineit.com:4445/SignalAhead/Json/SignalAhead";
    private static final String backEndServer= "http://irtrainsignalsystem.herokuapp.com/cgi-bin/senddevicelocation";
    private boolean soundCheck;
    private static MediaPlayer mp;
    private boolean locationPermission;


    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setTraceLifecycle(true);
        //handle permissions first, before map is created. not depicted here
        // Write you code here if permission already given.
        //load/initialize the osmdroid configuration, this can be done
        soundCheck=false;
        locationPermission=false;
        allMarkers = new ArrayList<>();
        currentMarkers = new ArrayList<>();
        currentSignals = new ArrayList<>();
        user_Long=0.0;
        user_Lat=0.0;
        mp= MediaPlayer.create(this,R.raw.sound);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string
        //inflate and create the map
        setContentView(R.layout.activity_main);
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        backend = new BackEnd();
        threadControl = new ThreadControl();
        Intent i = getIntent();
        trainName = i.getStringExtra("Signal");
        trainNo = i.getIntExtra("TrainNumber",0);
        trackName = i.getStringExtra("TrackName");
        phone = i.getLongExtra("Phone", 0);
        android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        new RequestTask(backend, this, threadControl, trainName).execute(reqURl, govURl);
//        setMapCenter();
        askPermission(Manifest.permission.ACCESS_FINE_LOCATION,MY_PERMISSIONS_REQUEST_LOCATION);
        if (locationPermission){
            GpsMyLocationProvider gp = new GpsMyLocationProvider(getApplicationContext());
            myLocationoverlay = new MyLocationNewOverlay(gp, map);
            myLocationoverlay.enableMyLocation();
            myLocationoverlay.setDrawAccuracyEnabled(false);
            map.getOverlays().add(myLocationoverlay);
            myLocation=myLocationoverlay.getMyLocation();
            map.invalidate();
//            Toast.makeText(this,"latitude: "+myLocation.getLatitude()+", Longitude: "+myLocation.getLongitude(),Toast.LENGTH_SHORT).show();
        }
    }

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
               requestTask= new RequestTask(backend, MainActivity.this, threadControl, trainName);
               requestTask.execute("", govURl,backEndServer);
                mHandler.postDelayed(timerTask, 1);
            }};

    public void setMapCenter(){
        mapController = map.getController();
        mapController.setZoom(15.6f);
        GeoPoint g=new GeoPoint(22.578802, 88.365743);
        mapController.setCenter(g);
    }



    public boolean checkCurrentLocation() {

        if (!locationPermission) {
            Log.d("location", "00");
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
        Log.d("key", "onResume");
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        if (map!=null) {
            map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
            mHandler.post(timerTask);
            threadControl.resume();
        }
    }

    public void onPause() {
        super.onPause();
        Log.d("key", "onPause");
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        if (map!=null) {
            map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
            threadControl.pause();
//            mp.pause();
            mHandler.removeCallbacks(timerTask);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        threadControl.cancel();
        requestTask.cancel(true);
        Log.d("key", "onDestroy:");
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

    public void addToMap(ArrayList<Signal> signals){
        for (int i=0;i<currentMarkers.size();i++){
            addColorSignal(signals.get(i),currentMarkers.get(i));
            addMarker(currentMarkers.get(i));
        }
    }

    public void removeMarkers(ArrayList<Marker> marker){
        for (Marker m: marker) {
            map.getOverlays().remove(m);
        }
    }

    public void addInitialSignals(ArrayList<Signal> signals) {
        if (signals.size()!=0){
            for (Signal s : signals) {
                if (checkSignalWithMarker(s) != null) {
                    currentSignals.add(s);
                    currentMarkers.add(checkSignalWithMarker(s));
                }
            }
            playSound();
            addToMap(currentSignals);
        }
    }

    private void playSound(){
        if (soundCheck){
            mp.start();
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


    public void updateSignals(ArrayList<Signal> signals) {
        Log.d("update", signals.toString());
        if (signals.size()!=0){
            if (signalsComparison(signals)) {
                Log.d("update", Boolean.toString(soundCheck));
                removeMarkers(currentMarkers);
                currentSignals.clear();
                currentMarkers.clear();
                addInitialSignals(signals);
                if (currentMarkers.size() != 0) {
                    Toast.makeText(this, "Updated Markers!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean signalsComparison(ArrayList<Signal> s){
        boolean f=false;
        soundCheck=false;
        if (s.size()!=currentSignals.size()){
            for (int i=0;i<s.size();i++) {
                Signal so=s.get(i);
                if(so.getIndex()==1) {
                    soundCheck = true;
                }
            }
                return true;
        }
        else {
            for (int i=0;i<s.size();i++){
                Signal so=s.get(i);
                if (sig(so)){
                    f=true;
                    if(so.getIndex()==1) {
                        soundCheck = true;
                    }
                }
            }
            return f;
        }
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
    public void exceptionRaised(String s) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage(s)
                .setTitle("Error");
        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event)
//    {
//        if ((keyCode == KeyEvent.KEYCODE_BACK))
//        {
//            threadControl.cancel();
//            finish();
//        }
//        return super.onKeyDown(keyCode, event);
//    }
}