package com.example.anarg.openmap2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private static final int MY_REQUEST_INT = 177;
    private MapView map = null;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationoverlay;
    private GpsMyLocationProvider gp;
    private BackEnd backend;
    private HashMap<String, GeoPoint> geoPointHashMap;
    private ArrayList<Marker> markerCounter;
    private static final String reqURl = "http://192.168.0.106/jsonrender.php";
    private static final String govURl = "http://tms.affineit.com:4445/SignalAhead/Json/SignalAhead";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //handle permissions first, before map is created. not depicted here
        permissionsCheck();
        //load/initialize the osmdroid configuration, this can be done
        markerCounter = new ArrayList<>();
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
        geoPointHashMap = null;
        ArrayList<Signal> at = null;
        try {
            String gov = new RequestTaskPost().execute(govURl).get();
            String js = new RequestTask().execute(reqURl).get();
            if (js != null && gov != null) {
                geoPointHashMap = backend.jsonPlot(js);
                at = backend.getSignals(backend.jsonGov(gov));
//                Log.d("List",at.toString());
                addSignals(geoPointHashMap, at);
            } else {
                Toast.makeText(this, "Error connecting the DataBase!", Toast.LENGTH_SHORT);
            }
        } catch (InterruptedException e) {
            Toast.makeText(this, "Error connecting the DataBase!", Toast.LENGTH_SHORT);
        } catch (ExecutionException e) {
            Toast.makeText(this, "Error connecting the DataBase!", Toast.LENGTH_SHORT);
        }

        myLocationoverlay = new MyLocationNewOverlay(gp, map);
        myLocationoverlay.enableMyLocation();
        mapController = map.getController();
        mapController.setZoom(15.6f);
        map.getOverlays().add(myLocationoverlay);

        if (myLocationoverlay.getMyLocation() != null) {
            Toast.makeText(this, "Location: " + myLocationoverlay.getMyLocation().toString(), Toast.LENGTH_SHORT);
        }
        if (geoPointHashMap != null && myLocationoverlay.getMyLocation() == null) {
            mapController.setCenter(geoPointHashMap.get("KOGAR16"));
        } else {
            mapController.animateTo(myLocationoverlay.getMyLocation());
        }
    }

    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    private void addMarker(GeoPoint gp, String description, String color) {
        Marker marker = new Marker(map);
        marker.setPosition(gp);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(description);
        if (!color.isEmpty()) {
            if (color.equals("Red")) {
                marker.setIcon(getResources().getDrawable(R.drawable.red));
            } else if (color.equals("Green")) {
                marker.setIcon(getResources().getDrawable(R.drawable.green));
            } else if (color.equals("Yellow")) {
                marker.setIcon(getResources().getDrawable(R.drawable.yellow));
            } else if (color.equals("YellowYellow")) {
                marker.setIcon(getResources().getDrawable(R.drawable.yellowyellow));
            } else if (color.equals("Empty")) {
                marker.setIcon(getResources().getDrawable(R.drawable.empty));
            }
        }

//        map.getOverlays().clear();
        map.getOverlays().add(marker); map.getOverlays();
//        map.invalidate();
        markerCounter.add(marker);
    }

    private String check(String id, ArrayList<Signal> s){
        for (Signal so: s){
            if(so.getSignalID().equals(id)){
                return so.getSignalAspect();
            }
        }
        return "Empty";
    }
    public void addSignals(HashMap<String, GeoPoint> gp, ArrayList<Signal> sg) {
        for(String s: gp.keySet()){
            if(check(s,sg).equals("Empty")) {
                addMarker(gp.get(s), s, "Empty");
            }
            else{
                addMarker(gp.get(s),s,check(s,sg));
            }
        }
//        for (Signal s: sg) {
//                if (check(s,gp)) {
//                    addMarker(gp.get(s.getSignalID()), s.getSignalID(), s.getSignalAspect());
//                }
//                else{
//                    addMarker(gp.get(s.getSignalID()),s.getSignalID(),"");
//                }
//            }
    }

        private void permissionsCheck() {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_REQUEST_INT);
                }
                return;
            } else {
                gp = new GpsMyLocationProvider(getApplicationContext());
            }
        }

//    public void sync(View view) {
//        Button b= findViewById(R.id.button);
//        b.setText("Clicked!");
//        ArrayList<Signal> at = null;
//        try {
//            String gov = new RequestTaskPost().execute(govURl).get();
//            if(gov!=null){
//                at=backend.getSignals(backend.jsonGov(gov));
//                addSignals(geoPointHashMap,at);
//                Toast.makeText(this,"Updated",Toast.LENGTH_SHORT);
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//    }
}
