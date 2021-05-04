package edu.sjsu.shubhushahade.project4;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private Uri uri = LocationProvider.CONTENT_URI;
    private GoogleMap mMap;
    private final LatLng LOCATION_UNIV = new LatLng(37.335371, -121.881050);
    private final LatLng LOCATION_CS = new LatLng(37.333714, -121.881860);
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //FloatingActionButton uninstall = (FloatingActionButton) findViewById(R.id.uninstallBtn);
        //uninstall.setOnClickListener(this::onClick);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(LOCATION_CS));

        mMap.setOnMapClickListener(point -> {
            mMap.addMarker(new MarkerOptions().position(point));
            insertLocation insertMarker = new insertLocation();
            ContentValues values = new ContentValues();

            values.put("latitude", point.latitude);
            values.put("longitude", point.longitude);
            values.put("zoom", mMap.getCameraPosition().zoom);
            insertMarker.execute(values);
            Toast.makeText(getBaseContext(), "Marker added", Toast.LENGTH_SHORT).show();
        });

        mMap.setOnMapLongClickListener(point -> {
            mMap.clear();
            deleteLocation deleteMarkers = new deleteLocation();
            deleteMarkers.execute();
            Toast.makeText(getBaseContext(), "All markers deleted", Toast.LENGTH_LONG).show();
        });
    }

    private class insertLocation extends AsyncTask<ContentValues, Void, Void> {
        @Override
        protected Void doInBackground(ContentValues... contentValues) {
            getContentResolver().insert(LocationProvider.CONTENT_URI, contentValues[0]);
            return null;
        }
    }

    private class deleteLocation extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            getContentResolver().delete(LocationProvider.CONTENT_URI, null, null);
            return null;
        }
    }

    public void switchView(View view) {
        CameraUpdate update = null;
        if (view.getId() == R.id.CityBtn) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            update = CameraUpdateFactory.newLatLngZoom(LOCATION_UNIV, 10f);
        } else if (view.getId() == R.id.UniBtn) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            update = CameraUpdateFactory.newLatLngZoom(LOCATION_UNIV, 14f);
        } else if (view.getId() == R.id.CSBtn) {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            update = CameraUpdateFactory.newLatLngZoom(LOCATION_CS, 18f);
        }
        mMap.animateCamera(update);
    }


    /*
    Delete app function not working
     */
    private void onClick(View v) {
        Intent delete = new Intent(Intent.ACTION_DELETE,
                Uri.parse("package:" + getPackageName()));
        startActivity(delete);
    }

    /*
    Get current location also not working
     */

    public void getLocation(View view) {
        FusedLocationProviderClient provider = LocationServices.getFusedLocationProviderClient(context);
        if (!isLocationEnabled()) showSettingAlert();
        else if (!checkPermission()) requestPermission();
        else provider.getLastLocation().addOnSuccessListener(this::onSuccess);
    }

    /*
    Methods below are for getting current location
     */

    private boolean isLocationEnabled() {
        LocationManager manager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void showSettingAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setMessage("Please enable location service.");
        alertDialog.setPositiveButton("Enable", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            context.startActivity(intent);
        });
        alertDialog.setNegativeButton("Cancel", (dialog, which) ->
                dialog.cancel());
        alertDialog.show();
    }

    private boolean checkPermission() {
        int result1 = ActivityCompat.checkSelfPermission
                (context, Manifest.permission.ACCESS_FINE_LOCATION);
        int result2 = ActivityCompat.checkSelfPermission
                (context, Manifest.permission.ACCESS_COARSE_LOCATION);
        return result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
    }

    public void onSuccess(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Toast.makeText(context, "Your Name is at \n" +
                            "Lat " + latitude + "\nLong: " + longitude,
                    Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(context, "Unable to get location", Toast.LENGTH_LONG).show();
    }
}