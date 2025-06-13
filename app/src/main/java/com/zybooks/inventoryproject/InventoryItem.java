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

    /**
     * Interface for zero quantity listener, this allows other classes to subscribe/unsubscribe to this event.
     */

    public interface QtyZeroListener {
        void onQuantityZero(InventoryItem item);
    }

    public void setQtyZeroListener(QtyZeroListener listener) {
        this.listener = listener;
    }

    public void unsetQtyZeroListener() {
        this.listener = null;
    }

    /**
     * Constructs an InventoryItem with the specified attributes.
     * Initializes the item ID, title, description, quantity, and user ID.
     *
     * @param id The unique identifier of the inventory item.
     * @param title The title or name of the item. Must not be null or empty.
     * @param description A brief description of the item. Can be an empty string but should not be null.
     * @param quantity The number of items available in stock. Must be a non-negative integer.
     * @param userId The ID of the user associated with the inventory item.
     *
     * @throws IllegalArgumentException If title is null or empty, or if quantity is negative.
     */
    public InventoryItem(int id, String title, String description, int quantity, int userId) {
        this.id = id;
        setTitle(title);
        setDescription(description);
        setQuantity(quantity);
        this.userId = userId;
    }

    /**
     * Public accessors for getting, setting, and decrementing/incrementing quantity.
     */

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

    /**
     * Sets the title of the inventory item, ensuring it does not exceed the specified length.
     *
     * @param t The title of the item. If longer than 20 characters, it will be truncated.
     * @throws NullPointerException If the provided title is null.
     */
    public void setTitle(String t) {
        this.title = getSubstring(t, 20);
    }

    /**
     * Sets the description of the inventory item, ensuring it does not exceed the specified length.
     *
     * @param d The description of the item. If longer than 40 characters, it will be truncated.
     * @throws NullPointerException If the provided description is null.
     */
    public void setDescription(String d) {
        this.description = getSubstring(d, 40);
    }

    /**
     * Sets the quantity of the inventory item, ensuring it is within a valid range.
     *
     * @param q The quantity of the item. If the value is outside the range (1 to Integer.MAX_VALUE-1), it defaults to 0.
     */
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

    /**
     * Overriding to string for custom formatting on to string calls.
     */

    @NonNull
    @Override
    public String toString() {
        return "[ " + title + " has a quantity of " + quantity + " ]";
    }

    /**
     * Returns a substring of the given string, ensuring it does not exceed the specified length.
     * If the provided length exceeds the string's actual length, the full string is returned.
     *
     * @param s The input string. Must not be null.
     * @param a The maximum length of the substring.
     * @return A substring of the given string, up to the specified length.
     * @throws NullPointerException If the input string is null.
     */
    private String getSubstring(String s, int a) {
        return s.substring(0, Math.min(a, s.length()));
    }

}