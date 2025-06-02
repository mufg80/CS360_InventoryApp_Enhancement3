/*
 * InventoryDatabase.java
 * This class manages the SQLite database for the inventory management application.
 * It extends SQLiteOpenHelper to handle database creation, upgrades, and table definitions.
 * The database includes two tables: one for inventory items and one for users.
 * Author: Shannon Musgrave
 * Created: June 2025
 * Version: 1.0
 */
package com.zybooks.inventoryproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * InventoryDatabase class extends SQLiteOpenHelper to manage the creation and versioning
 * of the SQLite database for storing inventory items and user information.
 */
public class InventoryDatabase extends SQLiteOpenHelper {
    // Database name and version constants
    private static final String DATABASE_NAME = "Inventory.db";
    private static final int VERSION = 6;

    /**
     * Constructor for InventoryDatabase.
     * Initializes the SQLiteOpenHelper with the provided context, database name, and version.
     *
     * @param context The application context used to access the database
     */
    public InventoryDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    /**
     * Creates the database tables when the database is first initialized.
     * Defines the schema for the users and inventoryitems tables, including a foreign key
     * relationship to link inventory items to users.
     *
     * @param sqLiteDatabase The SQLiteDatabase instance to execute table creation
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create the users table to store user credentials
        sqLiteDatabase.execSQL("CREATE TABLE " + UserTable.TABLE + " (" +
                UserTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UserTable.COL_USERNAME + " TEXT, " +
                UserTable.COL_PASSWORD_HASH + " TEXT)");

        // Create the inventoryitems table with a foreign key to users
        sqLiteDatabase.execSQL("CREATE TABLE " + InventoryTable.TABLE + " (" +
                InventoryTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                InventoryTable.COL_TITLE + " TEXT, " +
                InventoryTable.COL_DESCRIPTION + " TEXT, " +
                InventoryTable.COL_QUANTITY + " INTEGER NOT NULL, " +
                InventoryTable.COL_USER_ID + " INTEGER, " +
                "FOREIGN KEY(" + InventoryTable.COL_USER_ID + ") REFERENCES " +
                UserTable.TABLE + "(" + UserTable.COL_ID + "))");
    }

    /**
     * Handles database schema upgrades by dropping existing tables and recreating them.
     * This ensures the database schema is updated to the latest version.
     *
     * @param sqLiteDatabase The SQLiteDatabase instance to execute table operations
     * @param oldVersion The previous database version
     * @param newVersion The new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // Drop existing tables if they exist
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + InventoryTable.TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + UserTable.TABLE);
        // Recreate tables with the updated schema
        onCreate(sqLiteDatabase);
    }

    /**
     * Inner class defining the schema for the inventoryitems table.
     * Provides constants for the table name and column names to ensure consistency.
     */
    public static final class InventoryTable {
        public static final String TABLE = "inventoryitems";
        public static final String COL_ID = "_id"; // Primary key
        public static final String COL_TITLE = "title"; // Item title
        public static final String COL_DESCRIPTION = "description"; // Item description
        public static final String COL_QUANTITY = "quantity"; // Item quantity
        public static final String COL_USER_ID = "user"; // Foreign key to users table
    }

    /**
     * Inner class defining the schema for the users table.
     * Provides constants for the table name and column names to ensure consistency.
     */
    public static final class UserTable {
        public static final String TABLE = "users";
        public static final String COL_ID = "_id"; // Primary key
        public static final String COL_USERNAME = "username"; // User login name
        public static final String COL_PASSWORD_HASH = "passwordhash"; // Hashed user password
    }
}