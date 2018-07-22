package com.example.recoded.product.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.recoded.product.Data.ProductContract.ProductEntry;

public class ProductDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ProductContract.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "Products.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;// it should be one as udacity

    /**
     * Constructs a new instance of {@link ProductDbHelper}.
     *
     * @param context of the app
     */

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the Products table
        String SQL_CREATE_PRODUCT_TABLE = "CREATE TABLE " + ProductEntry.TABLE_NAME + " ("
                + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT , "
                + ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL , "
                + ProductEntry.COLUMN_PRODUCT_PRICE + " TEXT NOT NULL , "
                + ProductEntry.COLUMN_PRODUCT_QUANTITY + "  TEXT NOT NULL , "
                + ProductEntry.COLUMN_PRODUCT_SUPPLIERNAME + " TEXT NOT NULL , "
                + ProductEntry.COLUMN_PRODUCT_SUPPLIERPHONENUMBER + " TEXT NOT NULL  ); ";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PRODUCT_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}
