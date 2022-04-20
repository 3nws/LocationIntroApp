package com.example.location_intro_app;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;


public class GeofenceTransitionService extends IntentService {

    private static final String TAG = GeofenceTransitionService.class.getSimpleName();

    public GeofenceTransitionService() {
        super(TAG);
    }

    private String videoOption;

    SharedPreferences prefs;

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        // Handling errors
        if ( geofencingEvent.hasError() ) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode() );
            Log.e( TAG, errorMsg );
            return;
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        videoOption = prefs.getString("list_preference_2", "External app");

        int geoFenceTransition = geofencingEvent.getGeofenceTransition();
        // Check if the transition type is of interest
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            // Get the geofence that were triggered
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            int idx = getGeofenceTransitionVideoIndex(geoFenceTransition, triggeringGeofences );
            // Play video and log
            playVideo(idx);
        }
    }


    private int getGeofenceTransitionVideoIndex(int geoFenceTransition, List<Geofence> triggeringGeofences) {
        // get the ID of each geofence triggered
        ArrayList<String> triggeringGeofencesList = new ArrayList<>();
        for ( Geofence geofence : triggeringGeofences ) {
            triggeringGeofencesList.add(geofence.getRequestId());
        }

        return Integer.parseInt(triggeringGeofencesList.get(0));
    }

    private void playVideo(int i) {
        i /= 2;
        String[] videoURLS;
        videoURLS = getResources().getStringArray(R.array.videos);
        // Intent to start the main Activity
        Intent intent;
        if (videoOption.equals("External app")) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoURLS[i]));
        } else{
            intent = new Intent(GeofenceTransitionService.this, VideoActivity.class);
        }

        intent.putExtra("videoID", videoURLS[i]);
        startActivity(intent);
    }


    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }
}
