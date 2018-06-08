package com.example.anarg.openmap2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final int MY_REQUEST_INT = 177;
    private MapView map=null;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationoverlay;
    private GpsMyLocationProvider gp;
    private BackEnd backend;
    private static final String reqURl="http://192.168.43.115/jsonrender.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //handle permissions first, before map is created. not depicted here
        permissionsCheck();
        enableStrictMode();
        //load/initialize the osmdroid configuration, this can be done
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

        backend=new BackEnd();
        String js=new RequestTask().doInBackground(reqURl);
        HashMap<String,GeoPoint> gq=backend.jsonPlot(js);
        addSignals(gq);
        Log.d("HashMap Output",gq.get("KOGS7").toString());
        myLocationoverlay = new MyLocationNewOverlay(gp, map);
        myLocationoverlay.enableMyLocation();
        mapController = map.getController();
        mapController.setZoom(12.0f);
        mapController.setCenter(gq.get("KOGS7"));
        map.getOverlays().add(myLocationoverlay);

    }
    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    private void addMarker(GeoPoint gp,String description){
        Marker marker=new Marker(map);
        marker.setPosition(gp);
        marker.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM);
        marker.setTitle(description);
//        marker.setIcon(getResources().getDrawable(R.drawable.signal));

//        map.getOverlays().clear();
        map.getOverlays().add(marker);
//        map.invalidate();
    }

    public void addSignals( HashMap<String,GeoPoint> gp){
        for (String id: gp.keySet()){
            addMarker(gp.get(id),id);
        }
    }
    private void permissionsCheck(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},MY_REQUEST_INT);
            }
            return;
        }else {
            gp=new GpsMyLocationProvider(getApplicationContext());
        }
    }

    public void enableStrictMode()
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
    }
}
