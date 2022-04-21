package com.example.location_intro_app;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MapStyleOptions;

import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TabHost;

import com.example.location_intro_app.databinding.ActivityMainBinding;
import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        ResultCallback<Status>{

    private ActivityMainBinding binding;

    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleMap map;
    private GoogleApiClient googleApiClient;

    ArrayList<Drawable> gridImages;
    private MapFragment mapFragment;

    ZoomControls zoom;
    public String mapStyle;
    SharedPreferences prefs;
    float GEOFENCE_RADIUS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mapStyle = prefs.getString("list_preference_1", "<unset>");
        GEOFENCE_RADIUS = Float.parseFloat(prefs.getString("radius", "300"));
        if (GEOFENCE_RADIUS<20 || GEOFENCE_RADIUS>300) {
            Toast.makeText(getApplicationContext(), "Değer geçersiz! Lütfen 20-300 arası bir değer girin.", Toast.LENGTH_LONG).show();
            GEOFENCE_RADIUS = 300;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("radius", "300");
            editor.apply();
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        zoom = findViewById(R.id.zoom);

        zoom.setOnZoomInClickListener(view -> map.moveCamera(CameraUpdateFactory.zoomIn()));
        zoom.setOnZoomOutClickListener(view -> map.moveCamera(CameraUpdateFactory.zoomOut()));

        Toolbar toolbar1 = findViewById(R.id.toolbar1);
        Toolbar toolbar2 = findViewById(R.id.toolbar2);
        Toolbar toolbar3 = findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar1);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
        setSupportActionBar(toolbar2);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
        setSupportActionBar(toolbar3);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
        toolbar1.inflateMenu(R.menu.main_menu);
        toolbar2.inflateMenu(R.menu.main_menu);
        toolbar3.inflateMenu(R.menu.main_menu);


        TabHost host = findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Tab One");
        spec.setContent(R.id.tab1);
        spec.setIndicator("", getResources().getDrawable(R.drawable.home));
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Tab Two");
        spec.setContent(R.id.tab2);
        spec.setIndicator("", getResources().getDrawable(R.drawable.map));
        host.addTab(spec);

        // initialize GoogleMaps
        initGMaps();

        // create GoogleApiClient
        createGoogleApi();

        //Tab 3
        spec = host.newTabSpec("Tab Three");
        spec.setContent(R.id.tab3);
        spec.setIndicator("", getResources().getDrawable(R.drawable.mosque));
        host.addTab(spec);

        TextView v1 = findViewById(R.id.textView1);
        TextView v2 = findViewById(R.id.textView2);
        TextView v3 = findViewById(R.id.textView3);
        v1.setShadowLayer(24,4,4,Color.BLACK);
        v2.setShadowLayer(24,4,4,Color.BLACK);
        v3.setShadowLayer(24,4,4,Color.BLACK);

//        GRID TAB
        gridImages = new ArrayList<>();
        TypedArray imagesArray = getResources().obtainTypedArray(R.array.gridImages);
        for (int i=0;i<imagesArray.length();i++){
            gridImages.add(imagesArray.getDrawable(i));
        }
        imagesArray.recycle();
        String[] titles = getResources().getStringArray(R.array.geofenceTitles);
        String[] details = getResources().getStringArray(R.array.details);
        GridView gridview = findViewById(R.id.gridView);
        gridview.setAdapter(new ImageAdapter(this, titles, gridImages));


//        INDIVIDUAL PLACES
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent,
                                    View v, int position, long id){
                // Send intent to SingleViewActivity
                Intent i = new Intent(getApplicationContext(), DetailsActivity.class);
                String videoID;
                TypedArray videos = getResources().obtainTypedArray(R.array.videos);
                videoID = videos.getString(position).split("=")[1];
                i.putExtra("title", titles[position]);
                i.putExtra("details", details[position]);
                i.putExtra("videoID", videoID);
                ArrayList<Integer> images = new ArrayList<>();
                TypedArray places = getResources().obtainTypedArray(R.array.placeImages);
                TypedArray itemDef;
                int resId = places.getResourceId(position, 0);
                itemDef = getResources().obtainTypedArray(resId);
                for (int j = 0;j<itemDef.length();j++){
                    images.add(itemDef.getResourceId(j, 0));
                }
                i.putIntegerArrayListExtra("images", images);
                places.recycle();
                itemDef.recycle();
                startActivity(i);
            }
        });
    }

    // Create GoogleApiClient instance
    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Call GoogleApiClient connection when starting the Activity
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.toggleType: {
                if (map.getMapType() == GoogleMap.MAP_TYPE_SATELLITE){
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    item.setTitle("Uydu stiline geç");
                }
                else{
                    map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    item.setTitle("Normal stile geç");
                }
                return true;
            }
            case R.id.options: {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
                return true;
            }
            case R.id.exit: {
                System.exit(0);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private final int REQ_PERMISSION = 999;

    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }

    // Asks for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                this,
                new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                REQ_PERMISSION
        );
    }

    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch ( requestCode ) {
            case REQ_PERMISSION: {
                if ( grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    // Permission granted
                    startLocationUpdates();

                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }



    // App cannot work without the permissions
    private void permissionsDenied() {
        Log.w(TAG, "permissionsDenied()");
        // TODO close app and warn user
    }

    // Initialize GoogleMaps
    private void initGMaps(){
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // Callback called when Map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
        map.getUiSettings().setZoomControlsEnabled(true);
        SetMapStyle(googleMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SetMapStyle(map);
        GEOFENCE_RADIUS = Float.parseFloat(prefs.getString("radius", "300"));
        if (GEOFENCE_RADIUS<20 || GEOFENCE_RADIUS>300) {
            Toast.makeText(getApplicationContext(), "Değer geçersiz! Lütfen 20-300 arası bir değer girin.", Toast.LENGTH_LONG).show();
            GEOFENCE_RADIUS = 300;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("radius", "300");
            editor.apply();
        }
    }

    private void SetMapStyle(GoogleMap googleMap) {
        if(googleMap == null)
            return;

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mapStyle = prefs.getString("list_preference_1", "Standart");

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            int res;
            if (mapStyle.equals("Gümüş")){
                res = R.raw.silver_style;
            } else if (mapStyle.equals("Retro")) {
                res = R.raw.retro_style;
            } else if (mapStyle.equals("Siyah Beyaz")) {
                res = R.raw.dark_style;
            } else if (mapStyle.equals("Gece")) {
                res = R.raw.night_style;
            } else if (mapStyle.equals("Patlıcan")) {
                res = R.raw.aubergine_style;
            } else {
                res = R.raw.standard;
            }
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, res));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick("+latLng +")");
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private LocationRequest locationRequest;
    // Defined in mili seconds.
    private final int UPDATE_INTERVAL =  3 * 1000;
    private final int FASTEST_INTERVAL = 3 * 900;

    // Start location Updates
    private void startLocationUpdates(){
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if ( checkPermission() )
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        writeActualLocation(location);
    }

    // GoogleApiClient.ConnectionCallbacks connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        askPermission();
        startLocationUpdates();
        startGeofences();
        drawGeofence();
    }

    // GoogleApiClient.ConnectionCallbacks suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }

    // GoogleApiClient.OnConnectionFailedListener fail
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
    }

    private void writeActualLocation(Location location) {
        markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private Marker locationMarker;
    private void markerLocation(LatLng latLng) {
        Log.i(TAG, "markerLocation("+latLng+")");
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        if ( map!=null ) {
            if ( locationMarker != null )
                locationMarker.remove();
            locationMarker = map.addMarker(markerOptions);
            float zoom = 14f;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            map.animateCamera(cameraUpdate);
        }
    }


    private Marker geoFenceMarker;
    private void markerForGeofence(LatLng latLng, String title) {
        Log.i(TAG, "markerForGeofence("+latLng+")");
        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title);
        if ( map!=null ) {
            geoFenceMarker = map.addMarker(markerOptions);
        }
    }


    private static final long GEO_DURATION = 60 * 60 * 1000;

    private void startGeofences() {
        Log.i(TAG, "startGeofences()");
        String[] geofenceLocations = getResources().getStringArray(R.array.geofenceLocations);
        String[] geofenceTitles = getResources().getStringArray(R.array.geofenceTitles);
        ArrayList<Geofence> geofenceList = new ArrayList<>();
        for (int i = 0, j=0; i < geofenceLocations.length; i+=2, j++) {
            Double lat = Double.parseDouble(geofenceLocations[i]);
            Double lon = Double.parseDouble(geofenceLocations[i+1]);
            String title = geofenceTitles[j];
            markerForGeofence(new LatLng(lat, lon), title);
            geofenceList.add(new Geofence.Builder()
                    .setRequestId(Integer.toString(i))
                    .setCircularRegion(
                            lat,
                            lon,
                            GEOFENCE_RADIUS
                    )
                    .setExpirationDuration(GEO_DURATION)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build());
        }
        ArrayList<GeofencingRequest> geofenceRequests = createGeofenceRequests(geofenceList);
        addGeofences(geofenceRequests);
    }

    // Create a Geofence Request
    private ArrayList<GeofencingRequest> createGeofenceRequests( ArrayList<Geofence> geofences ) {
        Log.d(TAG, "createGeofenceRequest");
        ArrayList<GeofencingRequest> geofencingRequests = new ArrayList<>();
        for (Geofence geofence:geofences) {
            geofencingRequests.add(new GeofencingRequest.Builder()
                    .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_DWELL )
                    .addGeofence(geofence)
                    .build());
        }
        return geofencingRequests;
    }

    private final int GEOFENCE_REQ_CODE = 0;
    private PendingIntent createGeofencePendingIntent() {
        Intent intent = new Intent( this, GeofenceTransitionService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofences(ArrayList<GeofencingRequest> requests) {
        Log.d(TAG, "addGeofence");
        if (checkPermission()){
            for (GeofencingRequest request:requests) {
                LocationServices.GeofencingApi.addGeofences(
                        googleApiClient,
                        request,
                        createGeofencePendingIntent()
                ).setResultCallback(this);
            }
        }
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if ( status.isSuccess() ) {
            drawGeofence();
        } else {
            // inform about fail
        }
    }

    // Draw Geofence circle on GoogleMap
    private Circle geoFenceLimits;
    private void drawGeofence() {
        Log.d(TAG, "drawGeofence()");

        if ( geoFenceLimits != null )
            geoFenceLimits.remove();

        String[] geofenceLocations = getResources().getStringArray(R.array.geofenceLocations);
        ArrayList<CircleOptions> circleOptions = new ArrayList<>();
        for (int i = 0; i < geofenceLocations.length; i+=2) {
            LatLng center = new LatLng(Double.parseDouble(geofenceLocations[i]), Double.parseDouble(geofenceLocations[i+1]));
            CircleOptions circleOption = new CircleOptions()
                    .center(center)
                    .strokeColor(Color.argb(50, 70,70,70))
                    .fillColor( Color.argb(100, 150,150,150) )
                    .radius(GEOFENCE_RADIUS);
            geoFenceLimits = map.addCircle(circleOption);
        }

    }

}