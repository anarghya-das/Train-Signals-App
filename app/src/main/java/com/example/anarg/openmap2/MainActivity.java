package com.example.anarg.openmap2;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private MapView map = null;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationoverlay;
    private GpsMyLocationProvider gp;
    private BackEnd backend;
    private HashMap<String, GeoPoint> geoPointHashMap;
    private ArrayList<Marker> markerCounter;
    private ArrayList<Signal> signals;
    private ArrayList<String> req;
    private ThreadControl threadControl;
    private static final String reqURl = "http://192.168.0.106/jsonrender.php";
    private static final String govURl = "http://tms.affineit.com:4445/SignalAhead/Json/SignalAhead";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //handle permissions first, before map is created. not depicted here
            // Write you code here if permission already given.
            //load/initialize the osmdroid configuration, this can be done
            markerCounter = new ArrayList<>();
            geoPointHashMap = new HashMap<>();
            signals=new ArrayList<>();
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
            new RequestTask(backend,this,threadControl).execute(reqURl,govURl);
            if (permissionsCheck()){
                gp = new GpsMyLocationProvider(getApplicationContext());
                myLocationoverlay = new MyLocationNewOverlay(gp, map);
                myLocationoverlay.enableMyLocation();
                map.getOverlays().add(myLocationoverlay);
            }else {
                Toast.makeText(this,"Permission Not granted",Toast.LENGTH_SHORT).show();
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
                new RequestTask(backend, MainActivity.this,threadControl).execute("", govURl);
                mHandler.postDelayed(timerTask, 1);
            }};

    public void setMapCenter(HashMap<String,GeoPoint> a){
        mapController = map.getController();
        mapController.setZoom(15.6f);
        mapController.setCenter(a.get("KOGAR16"));
    }

    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        if (map!=null) {
            map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
            threadControl.resume();
            mHandler.post(timerTask);
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

    private void addMarker(GeoPoint gp, String description, Signal s) {
        Marker marker = new Marker(map);
        marker.setPosition(gp);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(description);
        addColorSignal(s,marker);
//        map.getOverlays().clear();
        map.getOverlays().add(marker); map.getOverlays();
//        map.invalidate();
        markerCounter.add(marker);
    }

    private void addColorSignal(Signal so,Marker marker){

        if (so==null){
            marker.setIcon(getResources().getDrawable(R.drawable.empty));
        } else if (so.getSignalAspect().equals("Red")){
            marker.setIcon(getResources().getDrawable(R.drawable.red));
        }else if (so.getSignalAspect().equals("Green")) {
            marker.setIcon(getResources().getDrawable(R.drawable.green));
        } else if (so.getSignalAspect().equals("Yellow")) {
            marker.setIcon(getResources().getDrawable(R.drawable.yellow));
        } else if (so.getSignalAspect().equals("YellowYellow")) {
            marker.setIcon(getResources().getDrawable(R.drawable.yellowyellow));
        }

    }

    public  void updateMarker(ArrayList<Signal> s){
        int c=0;
        for (Signal so: s){
            if(markerUpdateCheck(so)!=null){
                c++;
                addColorSignal(so,markerUpdateCheck(so));
            }
        }
        Toast.makeText(this,"Updated "+Integer.toString(c)+" markers!",Toast.LENGTH_SHORT).show();
    }
    //Helper Method for updateMarker
    private Marker markerUpdateCheck(Signal s) {
        for (Marker m: markerCounter){
            if(m.getTitle().equals(s.getSignalID())){
                return m;
            }
        }
        return null;
    }

    public void addSignals(HashMap<String, GeoPoint> gp, ArrayList<Signal> sg) {
        for(String s: gp.keySet()){
            if(check(s,sg)!=null) {
                addMarker(gp.get(s), s, check(s,sg));
            }
            else{
                addMarker(gp.get(s),s,check(s,sg));
            }
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
            } else {
//                gp = new GpsMyLocationProvider(getApplicationContext());
                return true;
            }
        }

    public void sync(View view) {
        Button b= findViewById(R.id.button);
        if(b.getText().equals("Pause Sync")){
            threadControl.pause();
            mHandler.removeCallbacks(timerTask);
            b.setText("Resume Sync");
            Toast.makeText(this,"Sync Paused!",Toast.LENGTH_SHORT).show();
        }
        else if (b.getText().equals("Resume Sync")){
            mHandler.post(timerTask);
            threadControl.resume();
            b.setText("Pause Sync");
            Toast.makeText(this,"Sync Resumed!",Toast.LENGTH_SHORT).show();
        }
//        try {
//            ArrayList<String> s=new RequestTask(backend,this).execute("",govURl).get();
//            ArrayList<Signal> a=backend.getSignals(backend.jsonGov(s.get(0)));
//            updateMarker(a);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
    }
}
