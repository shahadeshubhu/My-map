package edu.sjsu.shubhushahade.project4;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<Cursor>{

    private Uri uri = LocationProvider.CONTENT_URI;
    private GoogleMap mMap;
    private final LatLng LOCATION_UNIV = new LatLng(37.335371, -121.881050);
    private final LatLng LOCATION_CS = new LatLng(37.333714, -121.881860);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        LoaderManager.getInstance(this).restartLoader(0,  null, this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.addMarker(new MarkerOptions().position(LOCATION_CS));

        mMap.setOnMapClickListener(point -> {
            mMap.addMarker(new MarkerOptions().position(point));
            insertLocation insertMarker = new insertLocation();
            ContentValues values = new ContentValues();

            values.put("latitude", point.latitude);
            values.put("longitude", point.longitude);
            values.put("zoom", mMap.getCameraPosition().zoom);
            insertMarker.execute(values);
            Toast.makeText(getBaseContext(), "Marker added", Toast.LENGTH_LONG).show();
        });

        mMap.setOnMapLongClickListener(point -> {
            mMap.clear();
            deleteLocation deleteMarkers = new deleteLocation();
            deleteMarkers.execute();
            Toast.makeText(getBaseContext(), "All markers deleted", Toast.LENGTH_LONG).show();
        });
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(this, LocationProvider.CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            double latitude = LOCATION_UNIV.latitude;
            double longitude = LOCATION_UNIV.longitude;
            float zoom = 1;
            for(int i = 0; i < data.getCount(); i++){
                latitude = data.getDouble(data.getColumnIndex("latitude"));
                longitude = data.getDouble(data.getColumnIndex("longitude"));
                zoom = data.getFloat(data.getColumnIndex("zoom"));
                LatLng marker = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(marker));
                data.moveToNext();
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
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

    public void onClick(View v) {
        Intent delete = new Intent(Intent.ACTION_DELETE,
                Uri.parse("package:" + getPackageName()));
        startActivity(delete);
    }

    public void getLocation(View view){
        GPSTracker tracker = new GPSTracker(this);
        tracker.getLocation();
    }
}