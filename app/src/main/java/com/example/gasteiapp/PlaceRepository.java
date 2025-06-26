package com.example.gasteiapp;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

// Repositório para gerenciar operações de dados relacionadas a locais.
// Atua como uma camada de abstração entre o ViewModel e a fonte de dados (DatabaseHelper).
public class PlaceRepository {
    private DatabaseHelper dbHelper; // Instância do DatabaseHelper para acesso ao banco de dados

    // Construtor do repositório. Recebe o contexto para inicializar o DatabaseHelper.
    public PlaceRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Obtém todos os locais salvos de um usuário específico.
    public List<Place> getUserPlaces(int userId) {
        Cursor cursor = dbHelper.getUserPlaces(userId);
        List<Place> places = new ArrayList<>();
        // Verifica se o cursor não é nulo e se contém dados
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Extrai os dados de cada coluna do cursor
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("place_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("place_name"));
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("place_latitude"));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("place_longitude"));
                // Cria um novo objeto Place com os dados extraídos e adiciona à lista
                places.add(new Place(id, name, latitude, longitude));
            } while (cursor.moveToNext()); // Move para a próxima linha do cursor
            cursor.close(); // Fecha o cursor após o uso para liberar recursos
        }
        return places;
    }

    // Adiciona um novo local ao banco de dados.
    public boolean addPlace(Place place, int userId) {
        return dbHelper.addPlace(place.getName(), place.getLatitude(), place.getLongitude(), userId);
    }

    // Atualiza um local existente no banco de dados.
    public boolean updatePlace(Place place) {
        return dbHelper.updatePlace(place.getId(), place.getName(), place.getLatitude(), place.getLongitude());
    }

    // Exclui um local do banco de dados pelo seu ID.
    public boolean deletePlace(int placeId) {
        return dbHelper.deletePlace(placeId);
    }

    // Encontra um local salvo próximo a uma dada coordenada.
    public String findNearbyPlace(double currentLat, double currentLon, int userId) {
        return dbHelper.findNearbyPlace(currentLat, currentLon, userId);
    }
}
