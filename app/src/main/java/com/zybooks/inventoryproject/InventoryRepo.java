package com.zybooks.inventoryproject;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

// Main database querying class for users and inventoryitems.
public class InventoryRepo {
    // Properties holding database and inventorydatabase objects.
    private SQLiteDatabase database;
    private final InventoryDatabase inventoryDatabase;

    // Constructor to initialize InventoryDatabase.
    public InventoryRepo(Context context){
        this.inventoryDatabase = new InventoryDatabase(context);
    }

    // Open and close methods create database reference and close it when finished.
    public void open(){
        database = inventoryDatabase.getWritableDatabase();
    }
    public void close(){
        inventoryDatabase.close();
    }

    // Creates inventory item from an inventory object and returns long.
    public long createInventoryItem(InventoryItem item) {
        ContentValues values = new ContentValues();
        values.put(InventoryDatabase.InventoryTable.COL_TITLE, item.getTitle());
        values.put(InventoryDatabase.InventoryTable.COL_DESCRIPTION, item.getDescription());
        values.put(InventoryDatabase.InventoryTable.COL_QUANTITY, item.getQuantity());

        return database.insert(InventoryDatabase.InventoryTable.TABLE, null, values);
    }

    // Creates User row in database from user object and returns long.
    public long createUser(User user) {
        ContentValues values = new ContentValues();
        values.put(InventoryDatabase.UserTable.COL_USERNAME, user.getUser());
        values.put(InventoryDatabase.UserTable.COL_PASSWORD_HASH, user.getHash());

        return database.insert(InventoryDatabase.UserTable.TABLE, null, values);
    }

    // Read All and return list from database of inventory objects.
    public List<InventoryItem> getInventoryItems() {
        // Instantiate list.
        List<InventoryItem> itemlist = new ArrayList<InventoryItem>();
        // Select rows to return.
        String[] projection = {
                InventoryDatabase.InventoryTable.COL_ID,
                InventoryDatabase.InventoryTable.COL_TITLE,
                InventoryDatabase.InventoryTable.COL_DESCRIPTION,
                InventoryDatabase.InventoryTable.COL_QUANTITY
        };

        // Create cursor for retrieving data.
        Cursor cursor = database.query(
                InventoryDatabase.InventoryTable.TABLE,   // The table to query
                projection,                     // The columns to return
                null,                      // The columns for the WHERE clause
                null,                  // The values for the WHERE clause
                null,                           // don't group the rows
                null,                           // don't filter by row groups
                null                            // The sort order
        );

        // Populate list with cursor.
        while(cursor.moveToNext()) {
            InventoryItem item = new InventoryItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow(InventoryDatabase.InventoryTable.COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.InventoryTable.COL_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.InventoryTable.COL_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.InventoryTable.COL_QUANTITY))
            );
            itemlist.add(item);
        }
        // Close cursor and return list of inventory items.
        cursor.close();
        return itemlist;
    }

    // Get list of users from database.
    public List<User> getUsers() {
        // Instantiate list.
        List<User> userlist = new ArrayList<User>();

        // Select rows to return.
        String[] projection = {
                InventoryDatabase.UserTable.COL_ID,
                InventoryDatabase.UserTable.COL_USERNAME,
                InventoryDatabase.UserTable.COL_PASSWORD_HASH
        };

        // Create cursor to select objects.
        Cursor cursor = database.query(
                InventoryDatabase.UserTable.TABLE,   // The table to query
                projection,                     // The columns to return
                null,                      // The columns for the WHERE clause
                null,                  // The values for the WHERE clause
                null,                           // don't group the rows
                null,                           // don't filter by row groups
                null                            // The sort order
        );

        // Populate list of users.
        while(cursor.moveToNext()) {
            User u = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(InventoryDatabase.UserTable.COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.UserTable.COL_USERNAME)),
                    null,
                    cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.UserTable.COL_PASSWORD_HASH))
            );
            userlist.add(u);
        }
        // Close and return list.
        cursor.close();
        return userlist;
    }

    // Return a single inventory item from database.
    public InventoryItem getInventoryItem(long id) {

        // Select rows to return.
        String[] projection = {
                InventoryDatabase.InventoryTable.COL_ID,
                InventoryDatabase.InventoryTable.COL_TITLE,
                InventoryDatabase.InventoryTable.COL_DESCRIPTION,
                InventoryDatabase.InventoryTable.COL_QUANTITY
        };

        // Selection by column id.
        String selection = InventoryDatabase.InventoryTable.COL_ID + " = ?";

        // Args is id as string.
        String[] selectionArgs = { String.valueOf(id) };

        // Create cursor for returning this query.
        Cursor cursor = database.query(
                InventoryDatabase.InventoryTable.TABLE,   // The table to query
                projection,                     // The columns to return
                selection,                      // The columns for the WHERE clause
                selectionArgs,                  // The values for the WHERE clause
                null,                           // don't group the rows
                null,                           // don't filter by row groups
                null                            // The sort order
        );

        // Get inventory item and set equal to cursor result if found, if not return null.
        InventoryItem item = null;
        if(cursor.moveToFirst()) {
            item = new InventoryItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow(InventoryDatabase.InventoryTable.COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.InventoryTable.COL_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.InventoryTable.COL_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.InventoryTable.COL_QUANTITY))
            );
        }
        // Close cursor and return item.
        cursor.close();
        return item;
    }

    // Return user by searching by name.
    public User getUser(String name) {

        // Select rows to return.
        String[] projection = {
                InventoryDatabase.UserTable.COL_ID,
                InventoryDatabase.UserTable.COL_USERNAME,
                InventoryDatabase.UserTable.COL_PASSWORD_HASH
        };

        // Create selection looking for username column.
        String selection = InventoryDatabase.UserTable.COL_USERNAME + " = ?";
        // Args are name passed in to method.
        String[] selectionArgs = { name };

        // Create cursor to query as instructed.
        Cursor cursor = database.query(
                InventoryDatabase.UserTable.TABLE,   // The table to query
                projection,                     // The columns to return
                selection,                      // The columns for the WHERE clause
                selectionArgs,                  // The values for the WHERE clause
                null,                           // don't group the rows
                null,                           // don't filter by row groups
                null                            // The sort order
        );

        // Return null or user if found.
        User user = null;
        if(cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(InventoryDatabase.UserTable.COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.UserTable.COL_USERNAME)),
                    null,
                    cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabase.UserTable.COL_PASSWORD_HASH))
            );
        }

        // Close cursor and return object.
        cursor.close();
        return user;
    }




    // Update inventory item
    public int updateInventoryItem(InventoryItem item) {

        // Create content object using supplied item properties.
        ContentValues values = new ContentValues();
        values.put(InventoryDatabase.InventoryTable.COL_TITLE, item.getTitle());
        values.put(InventoryDatabase.InventoryTable.COL_DESCRIPTION, item.getDescription());
        values.put(InventoryDatabase.InventoryTable.COL_QUANTITY, item.getQuantity());

        // Selection is col id.
        String selection = InventoryDatabase.InventoryTable.COL_ID + " = ?";

        // Args is value of id as string.
        String[] selectionArgs = { String.valueOf(item.getId()) };

        // Attempt an update on database using item id.
        return database.update(
                InventoryDatabase.InventoryTable.TABLE,
                values,
                selection,
                selectionArgs);
    }

    // Delete object using id.
    public int deleteInventoryItem(long id) {
        // Selection by column id.
        String selection = InventoryDatabase.InventoryTable.COL_ID + " = ?";
        // Args is passed in id field.
        String[] selectionArgs = { String.valueOf(id) };
        // Attempt to perform a deletion on the inventory table database.
        return database.delete(InventoryDatabase.InventoryTable.TABLE, selection, selectionArgs);
    }

}
