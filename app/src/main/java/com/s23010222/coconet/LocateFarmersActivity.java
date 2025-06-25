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

public class LocateFarmersActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ImageView backButton;
    private TextView titleText;
    private EditText addressInput;
    private Button btnSearch, btnZoomIn, btnZoomOut, btnMyLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private int farmerCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate_farmers);

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
        titleText.setText("Locate Farmers");

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
                        loadFarmerLocations(); // Reload farmer markers
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
        loadFarmerLocations(); // Load farmer locations from database
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLankaCenter, 7.5f));

        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void loadFarmerLocations() {
        // Reset farmer count
        farmerCount = 0;

        // Query Firestore for all farmers
        db.collection("users")
                .whereEqualTo("role", "Farmer")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Check if farmer has coordinates
                        Double latitude = document.getDouble("latitude");
                        Double longitude = document.getDouble("longitude");
                        String username = document.getString("username");
                        String city = document.getString("city");

                        if (latitude != null && longitude != null && username != null) {
                            LatLng farmerLocation = new LatLng(latitude, longitude);

                            // Create marker for farmer
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(farmerLocation)
                                    .title(username + " (Farmer)")
                                    .snippet(city != null ? city : "Location")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                    .draggable(false)
                                    .visible(true);

                            mMap.addMarker(markerOptions);
                            farmerCount++;

                            // Log for debugging
                            System.out.println("Added farmer marker: " + username + " at " + latitude + ", " + longitude);
                        }
                    }

                    if (farmerCount > 0) {
                        Toast.makeText(this, "Found " + farmerCount + " farmers with locations", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No farmers with locations found in database", Toast.LENGTH_LONG).show();
                    }

                    // Also load farmer posts with locations
                    loadFarmerPostsWithLocations();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading farmer locations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    System.out.println("Error loading farmers: " + e.getMessage());
                });
    }

    private void loadFarmerPostsWithLocations() {
        // Query Firestore for farmer posts with coordinates
        db.collection("farmer_posts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int postCount = 0;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Check if post has coordinates
                        Double latitude = document.getDouble("latitude");
                        Double longitude = document.getDouble("longitude");
                        String productName = document.getString("productName");
                        String location = document.getString("location");
                        String price = document.getString("price");

                        if (latitude != null && longitude != null && productName != null) {
                            LatLng postLocation = new LatLng(latitude, longitude);

                            // Create marker for farmer post
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(postLocation)
                                    .title(productName)
                                    .snippet("Price: " + (price != null ? price : "N/A") + " | Location: " + (location != null ? location : "N/A"))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                    .draggable(false)
                                    .visible(true);

                            mMap.addMarker(markerOptions);
                            postCount++;

                            // Log for debugging
                            System.out.println("Added post marker: " + productName + " at " + latitude + ", " + longitude);
                        }
                    }

                    if (postCount > 0) {
                        Toast.makeText(this, "Loaded " + postCount + " farmer posts with locations", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No farmer posts with locations found", Toast.LENGTH_SHORT).show();
                    }

                    // Show total summary
                    int totalMarkers = farmerCount + postCount;
                    if (totalMarkers == 0) {
                        Toast.makeText(this, "No location data found. Farmers need to register with their locations first.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading farmer posts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    System.out.println("Error loading posts: " + e.getMessage());
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}