package com.s23010222.coconet;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NewOrdersActivity extends AppCompatActivity {

    private RecyclerView ordersRecyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_orders);

        initViews();
        setupRecyclerView();
        loadOrders();
        setupClickListeners();
    }

    private void initViews() {
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        backButton = findViewById(R.id.backButton);
    }

    private void setupRecyclerView() {
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, orderList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        ordersRecyclerView.setLayoutManager(layoutManager);
        ordersRecyclerView.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        orderList.add(new Order(
                "A001",
                "Green Coconut",
                "Mohomad Sajid",
                "New Town | Anuradhapura",
                "Ordered on 20 May 2025 | 9.35 AM",
                "Rs 10 000",
                "100",
                "Pending",
                R.drawable.profile_placeholder
        ));

        orderList.add(new Order(
                "A002",
                "King Coconut",
                "Mohomad Sajid",
                "New Town | Anuradhapura",
                "Ordered on 20 May 2025 | 9.35 AM",
                "Rs 15 000",
                "150",
                "Pending",
                R.drawable.profile_placeholder
        ));

        orderList.add(new Order(
                "A003",
                "Green Coconut",
                "Mohomad Sajid",
                "New Town | Anuradhapura",
                "Ordered on 20 May 2025 | 9.35 AM",
                "Rs 7 000",
                "70",
                "Pending",
                R.drawable.profile_placeholder
        ));

        orderList.add(new Order(
                "A004",
                "Green Coconut",
                "Mohomad Sajid",
                "New Town | Anuradhapura",
                "Ordered on 20 May 2025 | 9.35 AM",
                "Rs 20 000",
                "200",
                "Pending",
                R.drawable.profile_placeholder
        ));

        orderAdapter.notifyDataSetChanged();
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}