/*
 * InventoryItemAdapter.java
 * This class is a RecyclerView adapter for displaying a list of inventory items in a grid layout.
 * It binds InventoryItem data to views and handles user interactions through button click listeners.
 * Author: Shannon Musgrave
 * Created: June 2025
 * Version: 1.0
 */
package com.zybooks.inventoryproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * InventoryItemAdapter extends RecyclerView.Adapter to display a list of InventoryItem objects
 * in a grid layout. It manages view creation, data binding, and button click events for
 * incrementing, decrementing, and deleting items.
 */
public class InventoryItemAdapter extends RecyclerView.Adapter<InventoryItemAdapter.ViewHolder> {

    /**
     * Interface for handling button click events in each RecyclerView row.
     */
    public interface OnButtonClickListener {
        /**
         * Called when the decrement button is clicked.
         *
         * @param position The position of the item in the RecyclerView
         */
        void onDecButtonClick(int position);

        /**
         * Called when the increment button is clicked.
         *
         * @param position The position of the item in the RecyclerView
         */
        void onIncButtonClick(int position);

        /**
         * Called when the delete button is clicked.
         *
         * @param position The position of the item in the RecyclerView
         */
        void onDelButtonClick(int position);
    }

    // Listener for button click events
    private OnButtonClickListener myListener;
    // List of inventory items to display
    private final List<InventoryItem> items;

    /**
     * Sets the listener for button click events.
     *
     * @param listener The listener to handle button click events
     */
    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.myListener = listener;
    }

    /**
     * ViewHolder class to hold references to the views for each inventory item row.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView; // Displays the item title
        public TextView descTextView; // Displays the item description
        public TextView qtyTextView; // Displays the item quantity
        public Button decButton; // Button to decrement quantity
        public Button incButton; // Button to increment quantity
        public Button delButton; // Button to delete the item

        /**
         * Constructor for ViewHolder.
         * Initializes view references for the row layout.
         *
         * @param itemView The view for a single row in the RecyclerView
         */
        public ViewHolder(View itemView) {
            super(itemView);
            // Bind views to their respective IDs in the layout
            titleTextView = itemView.findViewById(R.id.title);
            descTextView = itemView.findViewById(R.id.description);
            qtyTextView = itemView.findViewById(R.id.quantity);
            decButton = itemView.findViewById(R.id.decrement_button);
            incButton = itemView.findViewById(R.id.increment_button);
            delButton = itemView.findViewById(R.id.delete_button);
        }
    }

    /**
     * Constructor for InventoryItemAdapter.
     * Initializes the adapter with a list of inventory items to display.
     *
     * @param items The list of InventoryItem objects to be displayed
     */
    public InventoryItemAdapter(List<InventoryItem> items) {
        this.items = items;
    }

    /**
     * Creates a new ViewHolder by inflating the row layout.
     *
     * @param parent   The parent ViewGroup for the inflated layout
     * @param viewType The view type of the new View
     * @return A new ViewHolder containing the inflated view
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the grid_row layout for each item
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_row, parent, false);
        return new ViewHolder(v);
    }

    /**
     * Binds data from an InventoryItem to the views in a ViewHolder.
     * Sets up click listeners for the increment, decrement, and delete buttons.
     *
     * @param holder   The ViewHolder to bind data to
     * @param position The position of the item in the list
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Retrieve the InventoryItem at the specified position
        InventoryItem item = items.get(position);

        // Update the views with item data
        holder.titleTextView.setText(item.getTitle());
        holder.descTextView.setText(item.getDescription());
        holder.qtyTextView.setText(String.valueOf(item.getQuantity()));

        // Set click listener for the delete button
        holder.delButton.setOnClickListener(v -> {
            if (myListener != null) {
                myListener.onDelButtonClick(holder.getAbsoluteAdapterPosition());
            }
        });

        // Set click listener for the decrement button
        holder.decButton.setOnClickListener(v -> {
            if (myListener != null) {
                myListener.onDecButtonClick(holder.getAbsoluteAdapterPosition());
            }
        });

        // Set click listener for the increment button
        holder.incButton.setOnClickListener(v -> {
            if (myListener != null) {
                myListener.onIncButtonClick(holder.getAbsoluteAdapterPosition());
            }
        });
    }

    /**
     * Returns the total number of items in the list.
     *
     * @return The size of the items list
     */
    @Override
    public int getItemCount() {
        return items.size();
    }
}