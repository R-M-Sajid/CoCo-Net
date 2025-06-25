package com.s23010222.coconet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import de.hdodenhof.circleimageview.CircleImageView;
import com.airbnb.lottie.LottieAnimationView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private TextView helloText, usernameText;
    private LinearLayout viewProfileLayout, ordersLayout, deliveryStatusLayout, settingsLayout, logoutLayout;
    private FrameLayout loadingOverlay;
    private LottieAnimationView loadingLottie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupClickListeners();
        setUserData("Mohomad Sajid");
        setupLoadingOverlay();
    }

    private void initViews() {
        profileImage = findViewById(R.id.profile_image);
        helloText = findViewById(R.id.hello_text);
        usernameText = findViewById(R.id.username_text);
        viewProfileLayout = findViewById(R.id.view_profile_layout);
        ordersLayout = findViewById(R.id.orders_layout);
        deliveryStatusLayout = findViewById(R.id.delivery_status_layout);
        settingsLayout = findViewById(R.id.settings_layout);
        logoutLayout = findViewById(R.id.logout_layout);
    }

    private void setupClickListeners() {
        viewProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "View Profile clicked", Toast.LENGTH_SHORT).show();
            }
        });

        ordersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Orders clicked", Toast.LENGTH_SHORT).show();
            }
        });

        deliveryStatusLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Delivery Status clicked", Toast.LENGTH_SHORT).show();
            }
        });

        settingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Settings clicked", Toast.LENGTH_SHORT).show();
            }
        });

        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Profile image clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUserData(String username) {
        usernameText.setText(username);
    }

    private void setupLoadingOverlay() {
        loadingOverlay = new FrameLayout(this);
        loadingOverlay.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        loadingOverlay.setBackgroundColor(0x80000000); // semi-transparent black
        loadingOverlay.setVisibility(View.GONE);
        loadingLottie = new LottieAnimationView(this);
        FrameLayout.LayoutParams lottieParams = new FrameLayout.LayoutParams(300, 300);
        lottieParams.gravity = android.view.Gravity.CENTER;
        loadingLottie.setLayoutParams(lottieParams);
        loadingLottie.setAnimation(R.raw.loading);
        loadingLottie.loop(true);
        loadingOverlay.addView(loadingLottie);
        addContentView(loadingOverlay, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void showLogoutDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            showLoading();
            new Handler().postDelayed(() -> {
                // Clear login state
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();
                hideLoading();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }, 4000); // 4 seconds, same as login
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
            if (loadingLottie != null) loadingLottie.playAnimation();
        }
    }

    private void hideLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
            if (loadingLottie != null) loadingLottie.cancelAnimation();
        }
    }

    public void updateProfileImage(int resourceId) {
        profileImage.setImageResource(resourceId);
    }

    public void updateUsername(String username) {
        usernameText.setText(username);
    }
}