package com.zybooks.inventoryproject;
import android.content.Context;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

// Public class extending the SQ helper class.
public class InventoryDatabase extends SQLiteOpenHelper {
    // Name and version statics.
    private static final String DATABASE_NAME = "Inventory.db";
    private static final int VERSION = 2;
    // Initializing object with a context.
    public InventoryDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    // Oncreate creates tables for database holding inventoryitem and user objects.
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + InventoryTable.TABLE + " (" +
                InventoryTable.COL_ID + " integer primary key autoincrement, " +
                InventoryTable.COL_TITLE + " text, " +
                InventoryTable.COL_DESCRIPTION + " text, " +
                InventoryTable.COL_QUANTITY + " integer)");

        sqLiteDatabase.execSQL("create table " + UserTable.TABLE + " (" +
                UserTable.COL_ID + " integer primary key autoincrement, " +
                UserTable.COL_USERNAME + " text, " +
                UserTable.COL_PASSWORD_HASH + " text)");

    }

    // onupgrade drops previous tables for upgrade data changes.
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists " + InventoryTable.TABLE);
        sqLiteDatabase.execSQL("drop table if exists " + UserTable.TABLE);
        onCreate(sqLiteDatabase);
    }

    // Two public data classes to safe table and column names for SQL.
    public static final class InventoryTable{
        public static final String TABLE = "inventoryitems";
        public static final String COL_ID = "_id";
        public static final String COL_TITLE = "title";
        public static final String COL_DESCRIPTION = "description";
        public static final String COL_QUANTITY = "quantity";
    }

    public static final class UserTable{
        public static final String TABLE = "users";
        public static final String COL_ID = "_id";
        public static final String COL_USERNAME = "username";
        public static final String COL_PASSWORD_HASH = "passwordhash";
    }
}
