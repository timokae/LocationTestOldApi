package com.example.timo.locationtestoldapi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.ZonedDateTime;

public class MainActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 0;
    private final int MY_PERMISSIONS_REQUEST_FINE_LOCATION  = 1;

    int minTime     = 0;
    int minDistance = 0;
    String locationProvider;

    TextView textViewOutput;
    RadioGroup radioGroupLocationSource;
    RadioButton radioButtonNetwork;
    MapView mapView;
    EditText editTextMinTime;
    EditText editTextMinDistance;
    Button buttonMinUpdate;

    LocationListener currentLocationListener;
    LocationManager locationManager;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextMinTime     = findViewById(R.id.editTextMinTime);
        editTextMinDistance = findViewById(R.id.editTextMinDistance);
        editTextMinTime.setText(String.valueOf(minTime));
        editTextMinDistance.setText(String.valueOf(minDistance));

        buttonMinUpdate = findViewById(R.id.buttonMinUpdate);
        buttonMinUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minTime = Integer.parseInt(editTextMinTime.getText().toString());
                minDistance = Integer.parseInt(editTextMinDistance.getText().toString());
                updateLocationRequest(locationProvider, minTime, minDistance, currentLocationListener);

                Toast.makeText(getApplicationContext(), "Distance and Time updated", Toast.LENGTH_SHORT).show();
            }
        });

        textViewOutput = findViewById(R.id.textViewOutput);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        radioGroupLocationSource = findViewById(R.id.radioGroupLocationSource);
        radioButtonNetwork = findViewById(R.id.radioButtonNetwork);
        radioButtonNetwork.setChecked(true);

        locationProvider = LocationManager.NETWORK_PROVIDER;
        currentLocationListener = getLocationListenerNetwork();

        // Ask User for permissions if not granted
        if (!fine_location_permitted()) {
            permit_fine_location(MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        } else {
            // Acquire a reference to the system Location Manager
            locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

            // Define a listener that responds to location updates
            currentLocationListener = getLocationListenerNetwork();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, currentLocationListener);

            radioGroupLocationSource.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    locationManager.removeUpdates(currentLocationListener);
                    switch (checkedId) {
                        case R.id.radioButtonGPS:
                            currentLocationListener = getLocationListenerGPS();
                            locationProvider = LocationManager.GPS_PROVIDER;
                            //updateLocationRequest(LocationManager.GPS_PROVIDER, minTime, minDistance, currentLocationListener);
                            break;
                        case R.id.radioButtonNetwork:
                            currentLocationListener = getLocationListenerNetwork();
                            locationProvider = LocationManager.NETWORK_PROVIDER;
                            //updateLocationRequest(LocationManager.NETWORK_PROVIDER, minTime, minDistance, currentLocationListener);
                            break;
                    }
                    updateLocationRequest(locationProvider, minTime, minDistance, currentLocationListener);

                    Toast.makeText(getApplicationContext(), "Location Provider updated", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void showLocationOnMap(Location pLocation) {
        final Location location = pLocation;
        Log.d("LOCATION", location.toString());

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.clear();
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(
                                location.getLatitude(),
                                location.getLongitude()
                        ),
                        15
                ));
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng( location.getLatitude(), location.getLongitude()))
                        .title("You are here"));
                mapView.onResume();
            }
        });
    }

    // ==== Activity State Update Functions ====

    private void updateLocationRequest(String pLocationProvider, int pMinTime, int pMinDistance, LocationListener pLocationListener) {
        locationManager.removeUpdates(currentLocationListener);
        currentLocationListener = pLocationListener;
        locationManager.requestLocationUpdates(pLocationProvider, pMinTime, pMinDistance, currentLocationListener);
    }

    // ==== Location Listener ====

    private LocationListener getLocationListenerNetwork() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                textViewOutput.setText("Network: " + location.getLatitude() + ", " + location.getLongitude() + "\n" + ZonedDateTime.now());
                showLocationOnMap(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    private LocationListener getLocationListenerGPS() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                textViewOutput.setText("GPS: " + location.getLatitude() + ", " + location.getLongitude() + "\n" + ZonedDateTime.now());
                showLocationOnMap(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    private LocationListener getCurrentLocationListenerWifi() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                textViewOutput.setText("Wifi: " + location.getLatitude() + ", " + location.getLongitude() + "\n" + ZonedDateTime.now());
                showLocationOnMap(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    // ==== Location Permissions ====

    // Coarse Location Permission
    private boolean coarse_location_permitted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void permit_coarse_location(int callback) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, callback);
    }

    // Fine Location Permission
    private boolean fine_location_permitted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void permit_fine_location(int callback) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, callback);
    }

    // Permission Callback Handler
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Coarse Location granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Coarse Location denied!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission denied!", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
}
