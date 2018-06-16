package com.example.anarg.openmap2;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
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


public class MainActivity extends AppCompatActivity { //AppCompatActivity
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private MapView map = null;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationoverlay;
    private GpsMyLocationProvider gp;
    private BackEnd backend;
    private HashMap<String, GeoPoint> geoPointHashMap;
    private ArrayList<Marker> allMarkers;
    private ArrayList<Marker> currentMarkers;
    private ArrayList<Signal> currentSignals;
    private ArrayList<String> req;
    private ThreadControl threadControl;
    private String param;
    private static final String reqURl = "http://irtrainsignalsystem.herokuapp.com/cgi-bin/signals";
    private static final String govURl = "http://tms.affineit.com:4445/SignalAhead/Json/SignalAhead";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setTraceLifecycle(true);
        //handle permissions first, before map is created. not depicted here
            // Write you code here if permission already given.
            //load/initialize the osmdroid configuration, this can be done
            allMarkers = new ArrayList<>();
            currentMarkers= new ArrayList<>();
            geoPointHashMap = new HashMap<>();
            currentSignals=new ArrayList<>();
            req=new ArrayList<>();
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
            threadControl=new ThreadControl();
            Intent i= getIntent();
            param= i.getStringExtra("Signal");
            new RequestTask(backend,this,threadControl,param).execute(reqURl,govURl);
            if (permissionsCheck()){
                gp = new GpsMyLocationProvider(getApplicationContext());
                myLocationoverlay = new MyLocationNewOverlay(gp, map);
                myLocationoverlay.enableMyLocation();
                map.getOverlays().add(myLocationoverlay);
                map.invalidate();
            }
    }

    public ArrayList<String> getReq(){
        return req;
    }

    public void setReq(ArrayList<String> r){
        this.req=r;
    }

        private Handler mHandler = new Handler();
        private Runnable timerTask = new Runnable() {
            @Override
            public void run() {
                new RequestTask(backend, MainActivity.this,threadControl,param).execute("", govURl);
//                locationToast();
                mHandler.postDelayed(timerTask, 1);
            }};

    public void setMapCenter(){
        mapController = map.getController();
        mapController.setZoom(15.6f);
        GeoPoint g=new GeoPoint(22.578802, 88.365743);
        mapController.setCenter(g);
    }

    private void locationToast(){
        if (myLocationoverlay==null){
            Toast.makeText(this,"Enable Location permission to Use this!",Toast.LENGTH_SHORT).show();
        }
        else {
            mapController = map.getController();
            mapController.setZoom(15.6f);
            if (myLocationoverlay.getMyLocation() != null) {
                IGeoPoint loc = myLocationoverlay.getMyLocation();
//            double lat = myLocationoverlay.getMyLocation().getLatitude();
//            double lo = myLocationoverlay.getMyLocation().getLongitude();
                mapController.animateTo(loc);
//            Toast.makeText(this, "Latitude: " + lat + ", Longitude: " + lo, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location Not Found!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onResume() {
        super.onResume();
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
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        if (map!=null) {
            map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
            threadControl.pause();
            mHandler.removeCallbacks(timerTask);
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

    //Helper Method for addSignals
    private Signal check(String id, ArrayList<Signal> s){
        if(s!=null) {
            for (Signal so : s) {
                if (so.getSignalID().equals(id)) {
                    return so;
                }
            }
            return null;
        }
        else {
            return null;
        }
    }

    public void sync(View view) {
        locationToast();
//        Button b= findViewById(R.id.button);
//        if(b.getText().equals("Pause Sync")){
//            threadControl.pause();
//            mHandler.removeCallbacks(timerTask);
//            b.setText("Resume Sync");
//            Toast.makeText(this,"Sync Paused!",Toast.LENGTH_SHORT).show();
//        }
//        else if (b.getText().equals("Resume Sync")){
//            mHandler.post(timerTask);
//            threadControl.resume();
//            b.setText("Pause Sync");
//            Toast.makeText(this,"Sync Resumed!",Toast.LENGTH_SHORT).show();
//        }
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
        if (signals.size()==0){
            Toast.makeText(this,"No Signal Found!",Toast.LENGTH_SHORT).show();
        }
        else {
            for (Signal s : signals) {
                if (checkSignalWithMarker(s) != null) {
                    currentSignals.add(s);
                    currentMarkers.add(checkSignalWithMarker(s));
                }
            }
            addToMap(currentSignals);
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
        if (signals.size()==0){
            Toast.makeText(this,"No Signal Found!",Toast.LENGTH_SHORT).show();
        }else {
            if (signalsComparison(signals)) {
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
        if (s.size()!=currentSignals.size()){
            return true;
        }
        else {
            for (int i=0;i<currentSignals.size();i++){
                if (!s.get(i).getSignalID().equals(currentSignals.get(i).getSignalID())){
                    f=true;
                    break;
                }
            }
            return f;
        }
    }

    private boolean permissionsCheck() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        }
        else {
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(this,"Location Sharing Enabled!",Toast.LENGTH_SHORT).show();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this,"Location Sharing Disabled!",Toast.LENGTH_SHORT).show();
                }
                return;
            }

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
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            threadControl.cancel();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}