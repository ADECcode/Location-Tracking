package com.example.mapsactivity;




import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location previousLocation;
    private DatabaseReference databaseReference;

    private LocationListener locationListener;
    private LocationManager locationManager;
    private final long MIN_TIME = 1000;
    private final long MIN_DIST = 5;

    private EditText editTextLatitude;
    private EditText editTextLongitude;
    private ToggleButton toggleButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);

        editTextLatitude = findViewById(R.id.editText);
        editTextLongitude = findViewById(R.id.editText2);
        toggleButton = findViewById(R.id.toggleButton);

        databaseReference = FirebaseDatabase.getInstance().getReference("Location");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (toggleButton.isChecked()) { // check if toggle button is checked
                        String databaseLatitudeString = dataSnapshot.child("latitude").getValue().toString().substring(1, dataSnapshot.child("latitude").getValue().toString().length() - 1);
                        String databaseLongitudedeString = dataSnapshot.child("longitude").getValue().toString().substring(1, dataSnapshot.child("longitude").getValue().toString().length() - 1);

                        String[] stringLat = databaseLatitudeString.split(", ");
                        Arrays.sort(stringLat);
                        String latitude = stringLat[stringLat.length - 1].split("=")[1];

                        String[] stringLong = databaseLongitudedeString.split(", ");
                        Arrays.sort(stringLong);
                        String longitude = stringLong[stringLong.length - 1].split("=")[1];


                        LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(latLng).title(latitude + " , " + longitude));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


            public void onCancelled(@NonNull DataSnapshot dataSnapshot) {
// This method will be invoked if the data retrieval operation is cancelled.
                Log.d("MapsActivity", "Data retrieval operation cancelled.");
            }
        });
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (previousLocation == null || previousLocation.distanceTo(location) >= MIN_DIST) {
                    previousLocation = location;

                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    editTextLatitude.setText(String.valueOf(latitude));
                    editTextLongitude.setText(String.valueOf(longitude));

                    // store location data in Firebase Realtime Database
                    LocationData locationData = new LocationData(latitude, longitude);
                    databaseReference.setValue(locationData);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleButton.isChecked()) {
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            try {
                                String databaseLatitudeString = dataSnapshot.child("latitude").getValue().toString().substring(1, dataSnapshot.child("latitude").getValue().toString().length() - 1);
                                String databaseLongitudedeString = dataSnapshot.child("longitude").getValue().toString().substring(1, dataSnapshot.child("longitude").getValue().toString().length() - 1);

                                String[] stringLat = databaseLatitudeString.split(", ");
                                Arrays.sort(stringLat);
                                String latitude = stringLat[stringLat.length - 1].split("=")[1];

                                String[] stringLong = databaseLongitudedeString.split(", ");
                                Arrays.sort(stringLong);
                                String longitude = stringLong[stringLong.length - 1].split("=")[1];


                                LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

                                mMap.clear();
                                mMap.addMarker(new MarkerOptions().position(latLng).title(latitude + " , " + longitude));
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d("MapsActivity", "Data retrieval operation cancelled.");
                        }
                    });
                } else {
                    mMap.clear();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                InfoWindow infoWindow = new InfoWindow();
                infoWindow.setTitle("Major");
                infoWindow.setDescription("Description");
                mMap.setInfoWindowAdapter(infoWindow);
                marker.showInfoWindow();
                return true;
            }
        });
    }

    public class LocationData {
        private double latitude;
        private double longitude;

        public LocationData() {}

        public LocationData(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }

    private class InfoWindow implements GoogleMap.InfoWindowAdapter {

        private final View infoWindowView;

        InfoWindow() {
            infoWindowView = getLayoutInflater().inflate(R.layout.info_window_layout, null);
        }

        @Override
        public View getInfoContents(Marker marker) {
            TextView title = infoWindowView.findViewById(R.id.title);
            TextView description = infoWindowView.findViewById(R.id.description);
            title.setText(marker.getTitle());
            description.setText(marker.getSnippet());
            return infoWindowView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        public void setTitle(String major) {
            TextView title = infoWindowView.findViewById(R.id.title);
            title.setText(major);
        }

        public void setDescription(String description) {
            TextView descriptionView = infoWindowView.findViewById(R.id.description);
            descriptionView.setText(description);
        }
    }
}






