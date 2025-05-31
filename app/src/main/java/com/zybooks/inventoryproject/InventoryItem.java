package com.zybooks.inventoryproject;
import java.lang.Integer;
import androidx.annotation.NonNull;

// Data class for each inventory item.
public class InventoryItem {

    // Properties for item, id for database only.
    private int id;
    private String title;
    private String description;
    private String quantity;

    private QtyZeroListener listener;

    // Listener for quantity hitting zero for sms.
    public interface QtyZeroListener{
        void onQuantityZero(InventoryItem item);
    }
    // Allow object to sign up or cancel events.
    public void setQtyZeroListener(QtyZeroListener listener){
        this.listener = listener;
    }
    public void unsetQtyZeroListener(){
        this.listener = null;
    }

    // Constructor for initializing class with all info.
    public InventoryItem(int id, String title, String description, String quantity) {
        this.id = id;
        // Using setters to apply conditions to protect data.
        setTitle(title);
        setDescription(description);
        setQuantity(quantity);
    }

    // Public getters to provide access to data.
    public int getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
    public String getQuantity(){
        return quantity;
    }

    // Public setters for mutating data.
    public void setTitle(String t) {
        // Keep strings to 20 for titles.
        this.title = getSubstring(t, 20);
    }

    public void setDescription(String d) {
        // Keep strings to 40 for desc.
        this.description = getSubstring(d, 40);
    }
    public void setQuantity(String q){
        int i = 0;
        // Parse string safely no need for exception, just make quantity 1 if error.
        try{
            i = Integer.parseInt(q);
        }catch (NumberFormatException e){
            i = 1;
        }
        // Make sure no negative values as well.
        this.quantity = i > 0 && i < Integer.MAX_VALUE ? Integer.toString(i) : "0";
    }

    // Functions for increment and decrement buttons.
    public void incrementQty(){
        // No try/catch here since its already a protected field using setter above.
        int num = Integer.parseInt(quantity);
        // Just make sure not to go above max.
        if (Integer.MAX_VALUE > num){
            num += 1;
        }
        this.quantity = Integer.toString(num);
    }
    public void decrementQty(){
        // No try/catch here since its already a protected field using setter above.
        int num = Integer.parseInt(quantity);
        // Just make sure not to go below 0.
        if(num > 0){
            num -= 1;
            this.quantity = Integer.toString(num);
            // If num was 1 or more and now is 0, fire event.
            if(num == 0 && listener != null){
                listener.onQuantityZero(this);
            }
        }

    }
    // Override to string for use with SMS texting.
    @NonNull
    @Override
    public String toString(){
        return "[ " + title + " has a quantity of " + quantity + " ]";
    }
    // Small helper method for cutting strings down to size.
    private String getSubstring(String s, int a){
        return s.substring(0, Math.min(a, s.length()));
    }
}
