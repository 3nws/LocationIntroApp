package com.example.location_intro_app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.util.SharedPreferencesUtils;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MapStyleOptions;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;

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

    LocationManager manager;

    private GoogleMap map;
    private GoogleApiClient googleApiClient;

    ArrayList<Drawable> gridImages;
    private MapFragment mapFragment;

    public String mapStyle;
    SharedPreferences prefs;
    float GEOFENCE_RADIUS;
    Locale current;

    Context context;
    String languageChoice;

    TabHost host;

    private ArrayList<CircleOptions> circleOptions;
    private ArrayList<Circle> circles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mapStyle = prefs.getString("list_preference_1", "Standard");
        GEOFENCE_RADIUS = Float.parseFloat(prefs.getString("radius", "50"));
        if (GEOFENCE_RADIUS<20 || GEOFENCE_RADIUS>300) {
            Toast.makeText(getApplicationContext(), "Invalid value! Please enter a value between 20-300.", Toast.LENGTH_LONG).show();
            GEOFENCE_RADIUS = 50;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("radius", "50");
            editor.apply();
        }

        circles = new ArrayList<>();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar1 = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar1);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
        toolbar1.inflateMenu(R.menu.main_menu);

        host = findViewById(R.id.tabHost);
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


        manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
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

        setLocale();
        current = getResources().getConfiguration().locale;
        System.out.println("Current locale: " + current);

//        GRID TAB
        gridImages = new ArrayList<>();
        TypedArray imagesArray = getResources().obtainTypedArray(R.array.gridImages);
        for (int i=0;i<imagesArray.length();i++){
            gridImages.add(imagesArray.getDrawable(i));
        }
        imagesArray.recycle();
        String[] titles = context.getResources().getStringArray(R.array.geofenceTitles);
        GridView gridview = findViewById(R.id.gridView);
        gridview.setAdapter(new ImageAdapter(this, titles, gridImages));


//        INDIVIDUAL PLACES
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent,
                                    View v, int position, long id){
                // Send intent to SingleViewActivity
                String[] details = context.getResources().getStringArray(R.array.details);
                String ttsText = context.getResources().getString(R.string.ttsText);
                Intent i = new Intent(getApplicationContext(), DetailsActivity.class);
                String videoID;
                TypedArray videos = getResources().obtainTypedArray(R.array.videos);
                videoID = videos.getString(position).split("=")[1];
                i.putExtra("title", titles[position]);
                i.putExtra("details", details[position]);
                i.putExtra("videoID", videoID);
                i.putExtra("ttsText", ttsText);
                ArrayList<String> images = new ArrayList<>();
                ArrayList<String> highResImages = new ArrayList<>();
                TypedArray places = getResources().obtainTypedArray(R.array.placeImages);
                TypedArray placesH = getResources().obtainTypedArray(R.array.highResPlaceImages);
                TypedArray itemDef;
                TypedArray itemDefH;
                int resId = places.getResourceId(position, 0);
                int resIdH = placesH.getResourceId(position, 0);
                itemDef = getResources().obtainTypedArray(resId);
                itemDefH = getResources().obtainTypedArray(resIdH);
                for (int j = 0;j<itemDef.length();j++){
                    images.add(itemDef.getString(j));
                    highResImages.add(itemDefH.getString(j));
                }
                places.recycle();
                placesH.recycle();
                itemDef.recycle();
                itemDefH.recycle();
                i.putStringArrayListExtra("images", images);
                i.putStringArrayListExtra("highResImages", highResImages);
                startActivityForResult(i, 2);
            }
        });
    }

    public void enableGps(){

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }
        startLocationUpdates();
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String enableGpsMessage = context.getResources().getString(R.string.enableGps);;
        String no = context.getResources().getString(R.string.no);;
        String yes = context.getResources().getString(R.string.yes);
        builder.setMessage(enableGpsMessage)
                .setCancelable(false)
                .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(no, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==2)
        {
            host.setCurrentTab(Integer.parseInt(data.getStringExtra("tab")));
        }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.toggleType: {
                if (map.getMapType() == GoogleMap.MAP_TYPE_NORMAL){
                    map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    if (current.toString().equals("tr")){
                        item.setTitle("Normal görünüme geç");
                    }else {
                        item.setTitle("Switch to normal view");
                    }
                }
                else{
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    if (current.toString().equals("tr")) {
                        item.setTitle("Uydu görünümüne geç");
                    }else {
                        item.setTitle("Switch to satellite view");
                    }
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
                    enableGps();

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
        map.getUiSettings().setCompassEnabled(true);
        SetMapStyle(googleMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SetMapStyle(map);
        GEOFENCE_RADIUS = Float.parseFloat(prefs.getString("radius", "50"));
        setLocale();
        if (GEOFENCE_RADIUS<20 || GEOFENCE_RADIUS>300) {
            Toast.makeText(getApplicationContext(), "Invalid value! Please enter a value between 20-300.", Toast.LENGTH_LONG).show();
            GEOFENCE_RADIUS = 50;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("radius", "50");
            editor.apply();
        }
        invalidateOptionsMenu();
        startLocationUpdates();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setLocale();
        MenuItem toggle = menu.findItem(R.id.toggleType);
        MenuItem options = menu.findItem(R.id.options);
        MenuItem exit = menu.findItem(R.id.exit);
        String ayar = context.getResources().getString(R.string.options);
        String cikis = context.getResources().getString(R.string.exit);
        boolean flag = map.getMapType() == GoogleMap.MAP_TYPE_NORMAL;
        if (flag) {
            System.out.println("TEST "+ current);
            if (current.toString().equals("tr")) {
                toggle.setTitle("Uydu görünümüne geç");
            } else {
                toggle.setTitle("Switch to satellite view");
            }
        }else{
            if (current.toString().equals("tr")) {
                toggle.setTitle("Normal görünüme geç");
            }else {
                toggle.setTitle("Switch to normal view");
            }
        }
        options.setTitle(ayar);
        exit.setTitle(cikis);
        return super.onPrepareOptionsMenu(menu);
    }

    private void setLocale() {
        languageChoice = prefs.getString("language", "English");
        if (languageChoice.equals("English")){
            Locale.setDefault(new Locale("en"));
            context = LocaleHelper.setLocale(MainActivity.this, "en");
            current = new Locale("en");
        }else{
            Locale.setDefault(new Locale("tr"));
            context = LocaleHelper.setLocale(MainActivity.this, "tr");
            current = new Locale("tr");
        }
        TextView v1 = findViewById(R.id.textView1);
        TextView v2 = findViewById(R.id.textView2);
        TextView v3 = findViewById(R.id.textView3);
        TextView v4 = findViewById(R.id.allPlaces);
        v1.setText(context.getResources().getString(R.string.tanitim_metni1));
        v2.setText(context.getResources().getString(R.string.tanitim_metni2));
        v3.setText(context.getResources().getString(R.string.tanitim_metni3));
        v4.setText(context.getResources().getString(R.string.gridTitle));
    }

    private void SetMapStyle(GoogleMap googleMap) {
        if(googleMap == null)
            return;

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mapStyle = prefs.getString("list_preference_1", "Standard");

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            int res;
            if (mapStyle.equals("Silver") || mapStyle.equals("Gümüş")){
                res = R.raw.silver_style;
            } else if (mapStyle.equals("Retro")) {
                res = R.raw.retro_style;
            } else if (mapStyle.equals("Dark") || mapStyle.equals("Siyah Beyaz")) {
                res = R.raw.dark_style;
            } else if (mapStyle.equals("Night") || mapStyle.equals("Gece")) {
                res = R.raw.night_style;
            } else if (mapStyle.equals("Aubergine") || mapStyle.equals("Patlıcan")) {
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
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            return;
        }
        if ( !googleApiClient.isConnected() )
            return;
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
            float zoom = 17f;
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


    private static final long GEO_DURATION = 2 * 60 * 60 * 1000;

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
    private void drawGeofence() {
        Log.d(TAG, "drawGeofence()");

        if ( circles.size() > 0 ){
            for (Circle c:circles) {
                c.remove();
            }
        }
        String[] geofenceLocations = getResources().getStringArray(R.array.geofenceLocations);
        circleOptions = new ArrayList<>();
        for (int i = 0; i < geofenceLocations.length; i+=2) {
            LatLng center = new LatLng(Double.parseDouble(geofenceLocations[i]), Double.parseDouble(geofenceLocations[i+1]));
            CircleOptions circleOption = new CircleOptions()
                    .center(center)
                    .strokeColor(Color.argb(50, 70,70,70))
                    .fillColor( Color.argb(100, 150,150,150) )
                    .radius(GEOFENCE_RADIUS);
            circleOptions.add(circleOption);
        }
        for (CircleOptions c:circleOptions) {
            circles.add(map.addCircle(c));
        }
    }

}