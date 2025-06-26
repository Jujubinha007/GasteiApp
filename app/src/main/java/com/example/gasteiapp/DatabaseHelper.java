package com.example.gasteiapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.Nullable;

// Classe auxiliar para gerenciar o banco de dados SQLite.
public class DatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    // Nome e versão do banco de dados.
    private static final String DATABASE_NAME = "GasteiApp.db";
    private static final int DATABASE_VERSION = 5;

    // Constantes para a tabela de gastos.
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

    // Constantes para a tabela de usuários.
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // Constantes para a tabela de locais.
    private static final String TABLE_PLACES = "places";
    private static final String COLUMN_PLACE_ID = "place_id";
    private static final String COLUMN_PLACE_NAME = "place_name";
    private static final String COLUMN_PLACE_LATITUDE = "place_latitude";
    private static final String COLUMN_PLACE_LONGITUDE = "place_longitude";
    private static final String COLUMN_PLACE_USER_ID = "place_user_id";

    // Construtor do DatabaseHelper.
    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    // Método chamado quando o banco de dados é criado pela primeira vez.
    public void onCreate(SQLiteDatabase db) {
        // Query para criar a tabela de usuários.
        String query_users = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_PASSWORD + " TEXT);";
        db.execSQL(query_users);

        // Query para criar a tabela de gastos, com chave estrangeira para usuários.
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

        // Query para criar a tabela de locais, com chave estrangeira para usuários.
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
    // Método chamado quando a versão do banco de dados é atualizada.
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Lógica para adicionar a coluna image_path se a versão antiga for menor que 4.
        if (oldVersion < 4) {
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
        
        // Lógica para adicionar colunas de localização à tabela de gastos e criar a tabela de locais se a versão antiga for menor que 5.
        if (oldVersion < 5) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_LATITUDE + " REAL");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_LONGITUDE + " REAL");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_LOCATION_NAME + " TEXT");
            } catch (Exception e) {
                // As colunas podem já existir.
            }
            
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
    // Método chamado quando a versão do banco de dados é rebaixada. Recria as tabelas.
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACES);
        onCreate(db);
    }

    // Adiciona um novo usuário ao banco de dados.
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        // Armazenar senhas em texto puro não é seguro. Em uma aplicação real, hashes e salts devem ser usados.
        cv.put(COLUMN_USERNAME, username);
        cv.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, cv);
        return result != -1;
    }

    // Verifica se um usuário com o nome de usuário fornecido existe.
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

    // Verifica se um usuário com o nome de usuário e senha fornecidos existe (para login).
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Isso não é seguro. Senhas devem ser hashadas e comparadas.
        String[] columns = { COLUMN_USER_ID };
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = { username, password };
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    // Obtém o ID de um usuário pelo nome de usuário.
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

    // Adiciona um novo gasto ao banco de dados, incluindo dados de imagem e localização.
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

    // Adiciona um novo gasto sem dados de imagem ou localização.
    public boolean addGasto(String category, double value, String date, String formaPagamento, String description, int userId) {
        return addGasto(category, value, date, formaPagamento, description, userId, null, null, null, null);
    }

    // Obtém todos os gastos de um usuário.
    public Cursor getGastosByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE user_id = " + userId;
        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    // Obtém os gastos de um usuário com base em um filtro (mês atual, mês passado, todos).
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
            // O caso "ALL" não precisa de modificação extra na query.
        }

        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    // Atualiza um gasto existente, incluindo dados de imagem e localização.
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

    // Atualiza um gasto existente sem dados de imagem ou localização.
    public boolean updateGasto(int id, String description, double value, String date, String formaPagamento, String category) {
        return updateGasto(id, description, value, date, formaPagamento, category, null, null, null, null);
    }

    // Exclui um gasto do banco de dados.
    public boolean deleteGasto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    // Métodos para gerenciamento de locais

    // Adiciona um novo local salvo ao banco de dados para um usuário.
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

    // Obtém todos os locais salvos por um usuário.
    public Cursor getUserPlaces(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PLACES + " WHERE " + COLUMN_PLACE_USER_ID + " = " + userId;
        return db.rawQuery(query, null);
    }

    // Exclui um local salvo do banco de dados.
    public boolean deletePlace(int placeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_PLACES, COLUMN_PLACE_ID + " = ?", new String[]{String.valueOf(placeId)});
        return rows > 0;
    }

    // Atualiza um local salvo existente.
    public boolean updatePlace(int placeId, String placeName, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PLACE_NAME, placeName);
        cv.put(COLUMN_PLACE_LATITUDE, latitude);
        cv.put(COLUMN_PLACE_LONGITUDE, longitude);
        int rows = db.update(TABLE_PLACES, cv, COLUMN_PLACE_ID + " = ?", new String[]{String.valueOf(placeId)});
        return rows > 0;
    }

    // Calcula a distância entre duas coordenadas usando a fórmula de Haversine.
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Raio da Terra em km.
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // Converte para metros.
        return distance;
    }

    // Encontra locais próximos (dentro de 50 metros) às coordenadas atuais de um usuário.
    public String findNearbyPlace(double currentLat, double currentLon, int userId) {
        Cursor cursor = getUserPlaces(userId);
        String nearbyPlace = null;
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                double placeLat = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PLACE_LATITUDE));
                double placeLon = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PLACE_LONGITUDE));
                String placeName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLACE_NAME));
                
                double distance = calculateDistance(currentLat, currentLon, placeLat, placeLon);
                if (distance <= 50) { // Raio de 50 metros.
                    nearbyPlace = placeName;
                    break;
                }
            }
            cursor.close();
        }
        return nearbyPlace;
    }
}
