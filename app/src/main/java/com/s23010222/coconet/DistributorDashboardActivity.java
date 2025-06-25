package com.s23010222.coconet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class DistributorDashboardActivity extends AppCompatActivity {

    // Header components
    private CircleImageView profileImage;
    private TextView usernameText;

    // Search component
    private EditText searchEditText;

    // See All links
    private TextView seeAllText, seeAllOtherText;

    // Bottom navigation
    private LinearLayout homeTab, locationTab, ordersTab, notificationTab, menuTab;

    private RecyclerView recyclerViewFarmerPosts;
    private List<FarmerPost> farmerPosts;
    private List<FarmerPost> allFarmerPosts; // Store all posts
    private FarmerPostAdapter farmerPostAdapter;
    private FirebaseFirestore db;

    private static final int DASHBOARD_POST_LIMIT = 4; // Show only 4 posts on dashboard

    // Deals banner
    private View dealsBannerCard;

    // Shake-to-refresh sensor fields
    private SensorManager sensorManager;
    private float lastX, lastY, lastZ;
    private long lastShakeTime = 0;
    private static final int SHAKE_THRESHOLD = 800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distributor_dashboard);

        db = FirebaseFirestore.getInstance();
        farmerPosts = new ArrayList<>();
        allFarmerPosts = new ArrayList<>();

        initializeViews();
        setupFarmerPostsRecyclerView();
        loadFarmerPosts();
        setupClickListeners();
        setupUserData();

        // Shake-to-refresh setup
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void initializeViews() {
        // Header components
        profileImage = findViewById(R.id.profileImage);

        // Search component
        searchEditText = findViewById(R.id.searchEditText);

        // See All links
        seeAllText = findViewById(R.id.seeAllText);
        seeAllOtherText = findViewById(R.id.seeAllOtherText);

        // Deals banner
        dealsBannerCard = findViewById(R.id.dealsBannerCard);

        // Bottom navigation
        homeTab = findViewById(R.id.homeTab);
        locationTab = findViewById(R.id.locationTab);
        ordersTab = findViewById(R.id.ordersTab);
        notificationTab = findViewById(R.id.notificationTab);
        menuTab = findViewById(R.id.menuTab);
    }

    private void setupFarmerPostsRecyclerView() {
        recyclerViewFarmerPosts = findViewById(R.id.recyclerViewFarmerPosts);
        farmerPostAdapter = new FarmerPostAdapter(farmerPosts, this::onFarmerPostClick);
        recyclerViewFarmerPosts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewFarmerPosts.setAdapter(farmerPostAdapter);
    }

    private void loadFarmerPosts() {
        db.collection("farmer_posts")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allFarmerPosts.clear();
                    farmerPosts.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        FarmerPost post = new FarmerPost();
                        post.setId(document.getId());
                        post.setProductName(document.getString("productName"));
                        post.setDescription(document.getString("description"));
                        post.setQuantity(document.getString("quantity"));
                        post.setPrice(document.getString("price"));
                        post.setAvailability(document.getString("availability"));
                        post.setStockCondition(document.getString("stockCondition"));
                        post.setMobileNumber(document.getString("mobileNumber"));
                        post.setLocation(document.getString("location"));
                        post.setTimestamp(document.getLong("timestamp"));
                        post.setImageUrl(document.getString("imageUrl"));
                        post.setFarmerId(document.getString("farmerId"));

                        // Load coordinates if available
                        Double latitude = document.getDouble("latitude");
                        Double longitude = document.getDouble("longitude");
                        if (latitude != null && longitude != null) {
                            post.setLatitude(latitude);
                            post.setLongitude(longitude);
                        }

                        allFarmerPosts.add(post);
                    }

                    // Show only the last 4 posts on dashboard
                    displayLimitedPosts();
                })
                .addOnFailureListener(e -> {
                    // Optionally show a Toast or log error
                });
    }

    private void displayLimitedPosts() {
        farmerPosts.clear();

        // Add only the first 4 posts (which are already sorted by timestamp DESC)
        int postsToShow = Math.min(DASHBOARD_POST_LIMIT, allFarmerPosts.size());
        for (int i = 0; i < postsToShow; i++) {
            farmerPosts.add(allFarmerPosts.get(i));
        }

        farmerPostAdapter.notifyDataSetChanged();
    }

    private void onFarmerPostClick(FarmerPost post) {
        Intent intent = new Intent(DistributorDashboardActivity.this, ViewAdActivity.class);
        intent.putExtra("ad_id", post.getId());
        intent.putExtra("user_type", "distributor");
        startActivity(intent);
    }

    private void setupClickListeners() {
        // Profile image click
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfileActivity();
            }
        });

        // Search functionality
        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    openSearchActivity();
                }
            }
        });

        // See All links - Modified to pass all posts
        seeAllText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAllProductsActivity("coconut", allFarmerPosts);
            }
        });

        seeAllOtherText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAllProductsActivity("other", allFarmerPosts);
            }
        });

        // Deals banner click - open all coconut deals
        dealsBannerCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seeAllText.performClick();
            }
        });

        // Bottom navigation clicks
        homeTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Already on home, do nothing or refresh
                refreshDashboard();
            }
        });

        locationTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLocationActivity();
            }
        });

        ordersTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOrdersActivity();
            }
        });

        notificationTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotificationActivity();
            }
        });

        menuTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMenuActivity();
            }
        });
    }

    private void setupUserData() {
        // Set username - you can get this from SharedPreferences or user session
        // Example: usernameText.setText(getUserName());

        // Load profile image - you can use Glide or Picasso for image loading
        // Example: Glide.with(this).load(getUserProfileImageUrl()).into(profileImage);
    }

    // Navigation methods
    private void openProfileActivity() {
        // TODO: Create ProfileActivity
        // Intent intent = new Intent(this, ProfileActivity.class);
        // startActivity(intent);
    }

    private void openSearchActivity() {
        // TODO: Create SearchActivity
        // Intent intent = new Intent(this, SearchActivity.class);
        // startActivity(intent);
    }

    private void openAllProductsActivity(String category, List<FarmerPost> allPosts) {
        Intent intent = new Intent(this, AllProductsActivity.class);
        intent.putExtra("category", category);
        // Pass all posts to the AllProductsActivity
        intent.putParcelableArrayListExtra("all_posts", new ArrayList<>(allPosts));
        startActivity(intent);
    }

    private void openLocationActivity() {
        Intent intent = new Intent(this, LocateFarmersActivity.class);
        startActivity(intent);
    }

    private void openOrdersActivity() {
        // TODO: Create OrdersActivity
        // Intent intent = new Intent(this, OrdersActivity.class);
        // startActivity(intent);
    }

    private void openNotificationActivity() {
        // TODO: Create NotificationActivity
        // Intent intent = new Intent(this, NotificationActivity.class);
        // startActivity(intent);
    }

    private void openMenuActivity() {
        // TODO: Create MenuActivity
        // Intent intent = new Intent(this, MenuActivity.class);
        // startActivity(intent);
    }

    private void refreshDashboard() {
        // Reload data and show limited posts
        loadFarmerPosts();
    }

    // Utility methods that you might want to implement
    private String getUserName() {
        // Get username from SharedPreferences or user session
        // return getSharedPreferences("user_prefs", MODE_PRIVATE).getString("username", "Username");
        return "Username";
    }

    private String getUserProfileImageUrl() {
        // Get profile image URL from SharedPreferences or user session
        // return getSharedPreferences("user_prefs", MODE_PRIVATE).getString("profile_image", "");
        return "";
    }

    // Optional: Handle back button press
    @Override
    public void onBackPressed() {
        // If you want to prevent back navigation or show exit dialog
        // showExitDialog();
        super.onBackPressed();
    }

    // Optional: Handle activity lifecycle
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastShakeTime) > 200) {
                long diffTime = currentTime - lastShakeTime;
                lastShakeTime = currentTime;

                float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    // SHAKE DETECTED! Call your refresh method here
                    refreshDashboard();
                }

                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };
}