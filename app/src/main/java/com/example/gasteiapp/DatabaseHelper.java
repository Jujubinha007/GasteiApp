package com.example.gasteiapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "GasteiApp.db";
    private static final int DATABASE_VERSION = 5;

    private static final String TABLE_NAME = "gastos";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_VALUE = "value";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_FORMAPAGAMENTO = "forma_pagamento";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_IMAGE_PATH = "image_path";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_LOCATION_NAME = "location_name";

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    private static final String TABLE_PLACES = "places";
    private static final String COLUMN_PLACE_ID = "place_id";
    private static final String COLUMN_PLACE_NAME = "place_name";
    private static final String COLUMN_PLACE_LATITUDE = "place_latitude";
    private static final String COLUMN_PLACE_LONGITUDE = "place_longitude";
    private static final String COLUMN_PLACE_USER_ID = "place_user_id";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query_users = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_PASSWORD + " TEXT);";
        db.execSQL(query_users);

        String query_gastos = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_VALUE + " DECIMAL, " +
                COLUMN_DATE + " DATE, " +
                COLUMN_FORMAPAGAMENTO + " TEXT, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_IMAGE_PATH + " TEXT, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL, " +
                COLUMN_LOCATION_NAME + " TEXT, " +
                "user_id INTEGER, " +
                "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "));";
        db.execSQL(query_gastos);

        String query_places = "CREATE TABLE " + TABLE_PLACES + " (" +
                COLUMN_PLACE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PLACE_NAME + " TEXT, " +
                COLUMN_PLACE_LATITUDE + " REAL, " +
                COLUMN_PLACE_LONGITUDE + " REAL, " +
                COLUMN_PLACE_USER_ID + " INTEGER, " +
                "FOREIGN KEY(" + COLUMN_PLACE_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "));";
        db.execSQL(query_places);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            // Add image_path column if it doesn't exist
            Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_NAME + ")", null);
            boolean imagePathColumnExists = false;
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int nameIndex = cursor.getColumnIndex("name");
                    if (nameIndex != -1) {
                        if (COLUMN_IMAGE_PATH.equals(cursor.getString(nameIndex))) {
                            imagePathColumnExists = true;
                            break;
                        }
                    }
                }
                cursor.close();
            }

            if (!imagePathColumnExists) {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_IMAGE_PATH + " TEXT");
            }
        }
        
        if (oldVersion < 5) {
            // Add location columns to gastos table
            try {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_LATITUDE + " REAL");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_LONGITUDE + " REAL");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_LOCATION_NAME + " TEXT");
            } catch (Exception e) {
                // Columns might already exist
            }
            
            // Create places table
            String query_places = "CREATE TABLE IF NOT EXISTS " + TABLE_PLACES + " (" +
                    COLUMN_PLACE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PLACE_NAME + " TEXT, " +
                    COLUMN_PLACE_LATITUDE + " REAL, " +
                    COLUMN_PLACE_LONGITUDE + " REAL, " +
                    COLUMN_PLACE_USER_ID + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_PLACE_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "));";
            db.execSQL(query_places);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle downgrade gracefully by recreating tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACES);
        onCreate(db);
    }

    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        // NOTE: Storing passwords in plain text is not secure.
        // In a real application, you should hash and salt the password.
        cv.put(COLUMN_USERNAME, username);
        cv.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, cv);
        return result != -1;
    }

    public boolean checkUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = { COLUMN_USER_ID };
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = { username };
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        // NOTE: This is not secure. Passwords should be hashed and compared.
        String[] columns = { COLUMN_USER_ID };
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = { username, password };
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = { COLUMN_USER_ID };
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = { username };
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
        }
        cursor.close();
        return userId;
    }

    public boolean addGasto(String category, double value, String date, String formaPagamento, String description, int userId, String imagePath, Double latitude, Double longitude, String locationName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_DESCRIPTION, description);
        cv.put(COLUMN_VALUE, value);
        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_FORMAPAGAMENTO, formaPagamento);
        cv.put(COLUMN_CATEGORY, category);
        cv.put(COLUMN_IMAGE_PATH, imagePath);
        cv.put(COLUMN_LATITUDE, latitude);
        cv.put(COLUMN_LONGITUDE, longitude);
        cv.put(COLUMN_LOCATION_NAME, locationName);
        cv.put("user_id", userId);

        long result = db.insert(TABLE_NAME, null, cv);
        return result != -1;
    }

    public boolean addGasto(String category, double value, String date, String formaPagamento, String description, int userId) {
        return addGasto(category, value, date, formaPagamento, description, userId, null, null, null, null);
    }

    public Cursor getGastosByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE user_id = " + userId;
        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    public Cursor getGastosByUser(int userId, String filter) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE user_id = " + userId;

        switch (filter) {
            case "CURRENT_MONTH":
                query += " AND strftime('%Y-%m', " + COLUMN_DATE + ") = strftime('%Y-%m', 'now')";
                break;
            case "LAST_MONTH":
                query += " AND strftime('%Y-%m', " + COLUMN_DATE + ") = strftime('%Y-%m', 'now', '-1 month')";
                break;
            // "ALL" case needs no extra query modification
        }

        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    public boolean updateGasto(int id, String description, double value, String date, String formaPagamento, String category, String imagePath, Double latitude, Double longitude, String locationName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_DESCRIPTION, description);
        cv.put(COLUMN_VALUE, value);
        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_FORMAPAGAMENTO, formaPagamento);
        cv.put(COLUMN_CATEGORY, category);
        cv.put(COLUMN_IMAGE_PATH, imagePath);
        cv.put(COLUMN_LATITUDE, latitude);
        cv.put(COLUMN_LONGITUDE, longitude);
        cv.put(COLUMN_LOCATION_NAME, locationName);
        int rows = db.update(TABLE_NAME, cv, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public boolean updateGasto(int id, String description, double value, String date, String formaPagamento, String category) {
        return updateGasto(id, description, value, date, formaPagamento, category, null, null, null, null);
    }

    public boolean deleteGasto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    // Place management methods
    public boolean addPlace(String placeName, double latitude, double longitude, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PLACE_NAME, placeName);
        cv.put(COLUMN_PLACE_LATITUDE, latitude);
        cv.put(COLUMN_PLACE_LONGITUDE, longitude);
        cv.put(COLUMN_PLACE_USER_ID, userId);
        long result = db.insert(TABLE_PLACES, null, cv);
        return result != -1;
    }

    public Cursor getUserPlaces(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PLACES + " WHERE " + COLUMN_PLACE_USER_ID + " = " + userId;
        return db.rawQuery(query, null);
    }

    public boolean deletePlace(int placeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_PLACES, COLUMN_PLACE_ID + " = ?", new String[]{String.valueOf(placeId)});
        return rows > 0;
    }

    public boolean updatePlace(int placeId, String placeName, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PLACE_NAME, placeName);
        cv.put(COLUMN_PLACE_LATITUDE, latitude);
        cv.put(COLUMN_PLACE_LONGITUDE, longitude);
        int rows = db.update(TABLE_PLACES, cv, COLUMN_PLACE_ID + " = ?", new String[]{String.valueOf(placeId)});
        return rows > 0;
    }

    // Calculate distance between two coordinates using Haversine formula
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        return distance;
    }

    // Find nearby places within 50 meters
    public String findNearbyPlace(double currentLat, double currentLon, int userId) {
        Cursor cursor = getUserPlaces(userId);
        String nearbyPlace = null;
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                double placeLat = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PLACE_LATITUDE));
                double placeLon = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PLACE_LONGITUDE));
                String placeName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLACE_NAME));
                
                double distance = calculateDistance(currentLat, currentLon, placeLat, placeLon);
                if (distance <= 50) { // 50 meters radius
                    nearbyPlace = placeName;
                    break;
                }
            }
            cursor.close();
        }
        return nearbyPlace;
    }
}
