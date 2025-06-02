/*
 * InventoryItem.java
 * This class represents an inventory item in the inventory management application.
 * It encapsulates item properties such as ID, title, description, quantity, and user ID,
 * and provides methods to manage quantity changes with a listener for zero-quantity events.
 * Author: Shannon Musgrave
 * Created: June 2025
 * Version: 1.0
 */
package com.zybooks.inventoryproject;

import androidx.annotation.NonNull;

/**
 * InventoryItem class represents a single inventory item with properties like ID, title,
 * description, quantity, and associated user ID. It includes methods for modifying item
 * details and handling quantity changes with a listener for zero-quantity events.
 */
public class InventoryItem {
    private int id;
    private String title;
    private String description;
    private int quantity; // Changed to int
    private int userId;
    private QtyZeroListener listener;

    public interface QtyZeroListener {
        void onQuantityZero(InventoryItem item);
    }

    public void setQtyZeroListener(QtyZeroListener listener) {
        this.listener = listener;
    }

    public void unsetQtyZeroListener() {
        this.listener = null;
    }

    public InventoryItem(int id, String title, String description, int quantity, int userId) {
        this.id = id;
        setTitle(title);
        setDescription(description);
        setQuantity(quantity);
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getUserId() {
        return userId;
    }

    public void setTitle(String t) {
        this.title = getSubstring(t, 20);
    }

    public void setDescription(String d) {
        this.description = getSubstring(d, 40);
    }

    public void setQuantity(int q) {
        this.quantity = (q > 0 && q < Integer.MAX_VALUE) ? q : 0;
    }

    public void incrementQty() {
        if (quantity < Integer.MAX_VALUE) {
            quantity += 1;
        }
    }

    public void decrementQty() {
        if (quantity > 0) {
            quantity -= 1;
            if (quantity == 0 && listener != null) {
                listener.onQuantityZero(this);
            }
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "[ " + title + " has a quantity of " + quantity + " ]";
    }

    private String getSubstring(String s, int a) {
        return s.substring(0, Math.min(a, s.length()));
    }
}