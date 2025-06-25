package com.s23010222.coconet;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocateDistributorsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ImageView backButton;
    private TextView titleText;
    private EditText addressInput;
    private Button btnSearch, btnZoomIn, btnZoomOut, btnMyLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private int distributorCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate_distributors);

        db = FirebaseFirestore.getInstance();

        backButton = findViewById(R.id.back_button);
        titleText = findViewById(R.id.title_text);
        addressInput = findViewById(R.id.addressInput);
        btnSearch = findViewById(R.id.btnSearch);
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        btnMyLocation = findViewById(R.id.btnMyLocation);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set title for this activity
        titleText.setText("Locate Distributors");

        backButton.setOnClickListener(v -> onBackPressed());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnSearch.setOnClickListener(v -> {
            String address = addressInput.getText().toString().trim();
            if (!address.isEmpty()) {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocationName(address, 1);
                    if (!addressList.isEmpty()) {
                        Address loc = addressList.get(0);
                        LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                        mMap.clear();
                        loadDistributorLocations(); // Reload distributor markers
                        mMap.addMarker(new MarkerOptions().position(latLng).title("Searched Location"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    } else {
                        Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(this, "Geocoder error", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Enter location to search", Toast.LENGTH_SHORT).show();
            }
        });

        btnZoomIn.setOnClickListener(v -> mMap.animateCamera(CameraUpdateFactory.zoomIn()));
        btnZoomOut.setOnClickListener(v -> mMap.animateCamera(CameraUpdateFactory.zoomOut()));

        btnMyLocation.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15));
                        mMap.addMarker(new MarkerOptions()
                                .position(myLatLng)
                                .title("My Location")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    } else {
                        Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        LatLng sriLankaCenter = new LatLng(7.8731, 80.7718);
        loadDistributorLocations(); // Load distributor locations from database
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLankaCenter, 7.5f));

        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void loadDistributorLocations() {
        // Reset distributor count
        distributorCount = 0;

        // Query Firestore for all distributors
        db.collection("users")
                .whereEqualTo("role", "Distributor")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Check if distributor has coordinates
                        Double latitude = document.getDouble("latitude");
                        Double longitude = document.getDouble("longitude");
                        String username = document.getString("username");
                        String city = document.getString("city");

                        if (latitude != null && longitude != null && username != null) {
                            LatLng distributorLocation = new LatLng(latitude, longitude);

                            // Create marker for distributor
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(distributorLocation)
                                    .title(username + " (Distributor)")
                                    .snippet(city != null ? city : "Location")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                    .draggable(false)
                                    .visible(true);

                            mMap.addMarker(markerOptions);
                            distributorCount++;

                            // Log for debugging
                            System.out.println("Added distributor marker: " + username + " at " + latitude + ", " + longitude);
                        }
                    }

                    if (distributorCount > 0) {
                        Toast.makeText(this, "Found " + distributorCount + " distributors with locations", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No distributors with locations found in database", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading distributor locations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    System.out.println("Error loading distributors: " + e.getMessage());
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
