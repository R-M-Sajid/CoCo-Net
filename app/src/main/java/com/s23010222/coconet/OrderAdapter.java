package com.s23010222.coconet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.productName.setText(order.getProductName() + " - Order ID " + order.getOrderId());
        holder.customerName.setText(order.getCustomerName());
        holder.location.setText(order.getLocation());
        holder.orderDateTime.setText(order.getOrderDateTime());
        holder.paymentAmount.setText(order.getPaymentAmount());
        holder.quantity.setText(order.getQuantity());
        holder.orderStatus.setText(order.getOrderStatus());
        holder.profileImage.setImageResource(order.getProfileImage());

        if (order.getOrderStatus().equals("Pending")) {
            holder.orderStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView productName, customerName, location, orderDateTime, paymentAmount, quantity, orderStatus;
        ImageView profileImage;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);

            productName = itemView.findViewById(R.id.productName);
            customerName = itemView.findViewById(R.id.customerName);
            location = itemView.findViewById(R.id.location);
            orderDateTime = itemView.findViewById(R.id.orderDateTime);
            paymentAmount = itemView.findViewById(R.id.paymentAmount);
            quantity = itemView.findViewById(R.id.quantity);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            profileImage = itemView.findViewById(R.id.profileImage);
        }
    }
}