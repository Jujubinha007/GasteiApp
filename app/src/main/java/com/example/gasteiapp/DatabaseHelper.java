package com.example.gasteiapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

// Classe auxiliar para gerenciar o banco de dados SQLite da aplicação.
// Responsável pela criação, atualização e operações CRUD das tabelas.
public class DatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "GasteiApp.db"; // Nome do arquivo do banco de dados
    private static final int DATABASE_VERSION = 5; // Versão do banco de dados

    // Nomes das tabelas e colunas
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
    private static final String COLUMN_PASSWORD = "password"; // Armazena o hash da senha

    private static final String TABLE_PLACES = "places";
    private static final String COLUMN_PLACE_ID = "place_id";
    private static final String COLUMN_PLACE_NAME = "place_name";
    private static final String COLUMN_PLACE_LATITUDE = "place_latitude";
    private static final String COLUMN_PLACE_LONGITUDE = "place_longitude";
    private static final String COLUMN_PLACE_USER_ID = "place_user_id";

    // Construtor do DatabaseHelper
    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    // Chamado quando o banco de dados é criado pela primeira vez.
    // Cria as tabelas de usuários, gastos e locais.
    public void onCreate(SQLiteDatabase db) {
        String query_users = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_PASSWORD + " TEXT);"; // Coluna para o hash da senha
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
    // Chamado quando a versão do banco de dados é atualizada.
    // Lida com a adição de novas colunas ou tabelas em versões futuras.
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Lógica de atualização para a versão 4 (adição da coluna image_path)
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
        
        // Lógica de atualização para a versão 5 (adição de colunas de localização e tabela de locais)
        if (oldVersion < 5) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_LATITUDE + " REAL");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_LONGITUDE + " REAL");
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_LOCATION_NAME + " TEXT");
            } catch (Exception e) {
                // As colunas podem já existir se a atualização anterior falhou parcialmente
            }
            
            // Cria a tabela de locais se ela ainda não existir
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
    // Chamado quando a versão do banco de dados é rebaixada.
    // Recria as tabelas, o que resulta na perda de dados.
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

        cv.put(COLUMN_USERNAME, username);
        cv.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, cv);
        return result != -1; // Retorna true se a inserção foi bem-sucedida
    }

    // Verifica se um usuário com o nome de usuário fornecido já existe.
    public boolean checkUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = { COLUMN_USER_ID };
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = { username };
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0; // Retorna true se o usuário existe
    }

    // Obtém o hash da senha de um usuário pelo nome de usuário.
    public String getPasswordHash(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = { COLUMN_PASSWORD };
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = { username };
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        String passwordHash = null;
        if (cursor.moveToFirst()) {
            passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
        }
        cursor.close();
        return passwordHash; // Retorna o hash da senha ou null se o usuário não for encontrado
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
        return userId; // Retorna o ID do usuário ou -1 se não for encontrado
    }

    // Adiciona um novo gasto ao banco de dados.
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
        return result != -1; // Retorna true se a inserção foi bem-sucedida
    }

    // Sobrecarga do método addGasto para casos sem imagem ou localização.
    public boolean addGasto(String category, double value, String date, String formaPagamento, String description, int userId) {
        return addGasto(category, value, date, formaPagamento, description, userId, null, null, null, null);
    }

    // Obtém todos os gastos de um usuário.
    public Cursor getGastosByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE user_id = " + userId + " ORDER BY " + COLUMN_ID + " DESC";
        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor; // Retorna um Cursor com os resultados
    }

    // Obtém os gastos de um usuário com base em um filtro (mês atual, mês anterior, todos).
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
            // O caso "ALL" não precisa de modificação adicional na query
        }

        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor; // Retorna um Cursor com os resultados filtrados
    }

    // Atualiza um gasto existente no banco de dados.
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
        return rows > 0; // Retorna true se a atualização foi bem-sucedida
    }

    // Sobrecarga do método updateGasto para casos sem imagem ou localização.
    public boolean updateGasto(int id, String description, double value, String date, String formaPagamento, String category) {
        return updateGasto(id, description, value, date, formaPagamento, category, null, null, null, null);
    }

    // Exclui um gasto do banco de dados.
    public boolean deleteGasto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0; // Retorna true se a exclusão foi bem-sucedida
    }

    // Métodos para gerenciamento de locais

    // Adiciona um novo local ao banco de dados.
    public boolean addPlace(String placeName, double latitude, double longitude, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PLACE_NAME, placeName);
        cv.put(COLUMN_PLACE_LATITUDE, latitude);
        cv.put(COLUMN_PLACE_LONGITUDE, longitude);
        cv.put(COLUMN_PLACE_USER_ID, userId);
        long result = db.insert(TABLE_PLACES, null, cv);
        return result != -1; // Retorna true se a inserção foi bem-sucedida
    }

    // Obtém todos os locais salvos de um usuário.
    public Cursor getUserPlaces(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PLACES + " WHERE " + COLUMN_PLACE_USER_ID + " = " + userId;
        return db.rawQuery(query, null); // Retorna um Cursor com os locais
    }

    // Exclui um local do banco de dados.
    public boolean deletePlace(int placeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_PLACES, COLUMN_PLACE_ID + " = ?", new String[]{String.valueOf(placeId)});
        return rows > 0; // Retorna true se a exclusão foi bem-sucedida
    }

    // Atualiza um local existente no banco de dados.
    public boolean updatePlace(int placeId, String placeName, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PLACE_NAME, placeName);
        cv.put(COLUMN_PLACE_LATITUDE, latitude);
        cv.put(COLUMN_PLACE_LONGITUDE, longitude);
        int rows = db.update(TABLE_PLACES, cv, COLUMN_PLACE_ID + " = ?", new String[]{String.valueOf(placeId)});
        return rows > 0; // Retorna true se a atualização foi bem-sucedida
    }

    // Calcula a distância entre duas coordenadas geográficas usando a fórmula de Haversine.
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Raio da Terra em km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // Converte para metros
        return distance;
    }

    // Encontra um local salvo próximo (dentro de 50 metros) das coordenadas atuais.
    public String findNearbyPlace(double currentLat, double currentLon, int userId) {
        Cursor cursor = getUserPlaces(userId);
        String nearbyPlace = null;
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                double placeLat = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PLACE_LATITUDE));
                double placeLon = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PLACE_LONGITUDE));
                String placeName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLACE_NAME));
                
                double distance = calculateDistance(currentLat, currentLon, placeLat, placeLon);
                if (distance <= 50) { // Raio de 50 metros
                    nearbyPlace = placeName;
                    break;
                }
            }
            cursor.close();
        }
        return nearbyPlace;
    }
}
