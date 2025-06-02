/*
 * InventoryRepo.java
 * This class provides data access methods for interacting with the SQLite database.
 * It handles CRUD operations for InventoryItem and User objects, including creating,
 * reading, updating, and deleting records.
 * Author: Shannon Musgrave
 * Created: June 2025
 * Version: 1.0
 */
package com.zybooks.inventoryproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

/**
 * InventoryRepo class manages database operations for InventoryItem and User entities.
 * It provides methods to create, read, update, and delete records in the inventory and user tables.
 */
public class InventoryRepo {
    // Database instance for executing queries
    private SQLiteDatabase database;
    // Reference to the InventoryDatabase helper
    private final InventoryDatabase inventoryDatabase;

    /**
     * Constructor for InventoryRepo.
     * Initializes the InventoryDatabase helper with the provided context.
     *
     * @param context The application context used to initialize the database
     */
    public InventoryRepo(Context context) {
        this.inventoryDatabase = new InventoryDatabase(context);
    }

    /**
     * Opens a writable database connection.
     */
    public void open() {
        database = inventoryDatabase.getWritableDatabase(); // Get a writable database instance
    }

    /**
     * Closes the database connection.
     */
    public void close() {
        inventoryDatabase.close(); // Release database resources
    }

    /**
     * Creates a new inventory item record in the database.
     *
     * @param item The InventoryItem to insert
     * @return The row ID of the newly inserted item, or -1 if an error occurs
     */
    public long createInventoryItem(InventoryItem item) {
        // Prepare ContentValues with item data
        ContentValues values = new ContentValues();
        values.put(InventoryDatabase.InventoryTable.COL_TITLE, item.getTitle());
        values.put(InventoryDatabase.InventoryTable.COL_DESCRIPTION, item.getDescription());
        values.put(InventoryDatabase.InventoryTable.COL_QUANTITY, item.getQuantity());
        values.put(InventoryDatabase.InventoryTable.COL_USER_ID, item.getUserId());

        // Insert the item into the inventory table
        return database.insert(InventoryDatabase.InventoryTable.TABLE, null, values);
    }

    /**
     * Creates a new user record in the database.
     *
     * @param user The User to insert
     * @return The row ID of the newly inserted user, or -1 if an error occurs
     */
    public long createUser(User user) {
        // Prepare ContentValues with user data
        ContentValues values = new ContentValues();
        values.put(InventoryDatabase.UserTable.COL_USERNAME, user.getUser());
        values.put(InventoryDatabase.UserTable.COL_PASSWORD_HASH, user.getHash());

        // Insert the user into the users table
        return database.insert(InventoryDatabase.UserTable.TABLE, null, values);
    }

    /**
     * Retrieves a list of inventory items for a specific user.
     *
     * @param userId The ID of the user whose items are to be retrieved
     * @return A list of InventoryItem objects associated with the user
     */
    public List<InventoryItem> getInventoryItems(int userId) {
        List<InventoryItem> itemlist = new ArrayList<>(); // Initialize the result list

        // Define the columns to retrieve
        String[] projection = {
                InventoryDatabase.InventoryTable.COL_ID,
                InventoryDatabase.InventoryTable.COL_TITLE,
                InventoryDatabase.InventoryTable.COL_DESCRIPTION,
                InventoryDatabase.InventoryTable.COL_QUANTITY,
                InventoryDatabase.InventoryTable.COL_USER_ID
        };

        // Filter by user ID
        String selection = InventoryDatabase.InventoryTable.COL_USER_ID + " = ?";
        String[] selectionArgs = { String.valueOf(userId) };

        // Query the inventory table
        Cursor cursor = database.query(
                InventoryDatabase.InventoryTable.TABLE,
                projection,
                selection,
                selectionArgs,
                null, // No grouping
                null, // No filtering by row groups
                null  // No sort order
        );

        // Iterate through the cursor to build the item list
        while (cursor.moveToNext()) {
            InventoryItem item = new InventoryItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow(InventoryDatabase.InventoryTable.COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.InventoryTable.COL_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.InventoryTable.COL_DESCRIPTION)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(InventoryDatabase.InventoryTable.COL_QUANTITY)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(InventoryDatabase.InventoryTable.COL_USER_ID))
            );
            itemlist.add(item);
        }

        // Close the cursor to prevent resource leaks
        cursor.close();
        return itemlist;
    }

    /**
     * Retrieves all users from the database.
     *
     * @return A list of User objects
     */
    public List<User> getUsers() {
        List<User> userlist = new ArrayList<>(); // Initialize the result list

        // Define the columns to retrieve
        String[] projection = {
                InventoryDatabase.UserTable.COL_ID,
                InventoryDatabase.UserTable.COL_USERNAME,
                InventoryDatabase.UserTable.COL_PASSWORD_HASH
        };

        // Query the users table
        Cursor cursor = database.query(
                InventoryDatabase.UserTable.TABLE,
                projection,
                null, // No selection
                null, // No selection args
                null, // No grouping
                null, // No filtering by row groups
                null  // No sort order
        );

        // Iterate through the cursor to build the user list
        while (cursor.moveToNext()) {
            User user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(InventoryDatabase.UserTable.COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.UserTable.COL_USERNAME)),
                    null, // Password not retrieved from database
                    cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.UserTable.COL_PASSWORD_HASH))
            );
            userlist.add(user);
        }

        // Close the cursor to prevent resource leaks
        cursor.close();
        return userlist;
    }

    /**
     * Retrieves a single inventory item by its ID.
     *
     * @param id The ID of the inventory item to retrieve
     * @return The InventoryItem object, or null if not found
     */
    public InventoryItem getInventoryItem(long id) {
        // Define the columns to retrieve
        String[] projection = {
                InventoryDatabase.InventoryTable.COL_ID,
                InventoryDatabase.InventoryTable.COL_TITLE,
                InventoryDatabase.InventoryTable.COL_DESCRIPTION,
                InventoryDatabase.InventoryTable.COL_QUANTITY,
                InventoryDatabase.InventoryTable.COL_USER_ID
        };

        // Filter by item ID
        String selection = InventoryDatabase.InventoryTable.COL_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };

        // Query the inventory table
        Cursor cursor = database.query(
                InventoryDatabase.InventoryTable.TABLE,
                projection,
                selection,
                selectionArgs,
                null, // No grouping
                null, // No filtering by row groups
                null  // No sort order
        );

        // Create an InventoryItem if found
        InventoryItem item = null;
        if (cursor.moveToFirst()) {
            item = new InventoryItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow(InventoryDatabase.InventoryTable.COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.InventoryTable.COL_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.InventoryTable.COL_DESCRIPTION)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(InventoryDatabase.InventoryTable.COL_QUANTITY)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(InventoryDatabase.InventoryTable.COL_USER_ID))
            );
        }

        // Close the cursor to prevent resource leaks
        cursor.close();
        return item;
    }

    /**
     * Retrieves a user by their username.
     *
     * @param name The username to search for
     * @return The User object, or null if not found
     */
    public User getUser(String name) {
        // Define the columns to retrieve
        String[] projection = {
                InventoryDatabase.UserTable.COL_ID,
                InventoryDatabase.UserTable.COL_USERNAME,
                InventoryDatabase.UserTable.COL_PASSWORD_HASH
        };

        // Filter by username
        String selection = InventoryDatabase.UserTable.COL_USERNAME + " = ?";
        String[] selectionArgs = { name };

        // Query the users table
        Cursor cursor = database.query(
                InventoryDatabase.UserTable.TABLE,
                projection,
                selection,
                selectionArgs,
                null, // No grouping
                null, // No filtering by row groups
                null  // No sort order
        );

        // Create a User if found
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(InventoryDatabase.UserTable.COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.UserTable.COL_USERNAME)),
                    null, // Password not retrieved from database
                    cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.UserTable.COL_PASSWORD_HASH))
            );
        }

        // Close the cursor to prevent resource leaks
        cursor.close();
        return user;
    }

    /**
     * Updates an existing inventory item in the database.
     *
     * @param item The InventoryItem to update
     * @return The number of rows affected (typically 1 if successful, 0 if not)
     */
    public int updateInventoryItem(InventoryItem item) {
        // Prepare ContentValues with updated item data
        ContentValues values = new ContentValues();
        values.put(InventoryDatabase.InventoryTable.COL_TITLE, item.getTitle());
        values.put(InventoryDatabase.InventoryTable.COL_DESCRIPTION, item.getDescription());
        values.put(InventoryDatabase.InventoryTable.COL_QUANTITY, item.getQuantity());
        values.put(InventoryDatabase.InventoryTable.COL_USER_ID, item.getUserId());

        // Filter by item ID
        String selection = InventoryDatabase.InventoryTable.COL_ID + " = ?";
        String[] selectionArgs = { String.valueOf(item.getId()) };

        // Update the item in the inventory table
        return database.update(
                InventoryDatabase.InventoryTable.TABLE,
                values,
                selection,
                selectionArgs);
    }

    /**
     * Deletes an inventory item from the database.
     *
     * @param id The ID of the inventory item to delete
     * @return The number of rows affected (typically 1 if successful, 0 if not)
     */
    public int deleteInventoryItem(long id) {
        // Filter by item ID
        String selection = InventoryDatabase.InventoryTable.COL_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };

        // Delete the item from the inventory table
        return database.delete(InventoryDatabase.InventoryTable.TABLE, selection, selectionArgs);
    }
}