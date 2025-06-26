package com.example.gasteiapp;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

// Repositório para gerenciar operações de dados relacionadas a gastos.
// Atua como uma camada de abstração entre o ViewModel e a fonte de dados (DatabaseHelper).
public class GastoRepository {
    private DatabaseHelper dbHelper; // Instância do DatabaseHelper para acesso ao banco de dados

    // Construtor do repositório. Recebe o contexto para inicializar o DatabaseHelper.
    public GastoRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Obtém todos os gastos de um usuário específico.
    public List<Gasto> getGastosByUser(int userId) {
        Cursor cursor = dbHelper.getGastosByUser(userId);
        return cursorToGastoList(cursor);
    }

    // Obtém os gastos de um usuário específico, filtrados por um período (mês atual, mês anterior, todos).
    public List<Gasto> getGastosByUser(int userId, String filter) {
        Cursor cursor = dbHelper.getGastosByUser(userId, filter);
        return cursorToGastoList(cursor);
    }

    // Adiciona um novo gasto ao banco de dados.
    public boolean addGasto(Gasto gasto) {
        return dbHelper.addGasto(gasto.getCategory(), gasto.getValue(), gasto.getDate(), gasto.getFormaPagamento(), gasto.getDescription(), gasto.getUserId(), gasto.getImagePath(), gasto.getLatitude(), gasto.getLongitude(), gasto.getLocationName());
    }

    // Atualiza um gasto existente no banco de dados.
    public boolean updateGasto(Gasto gasto) {
        return dbHelper.updateGasto(gasto.getId(), gasto.getDescription(), gasto.getValue(), gasto.getDate(), gasto.getFormaPagamento(), gasto.getCategory(), gasto.getImagePath(), gasto.getLatitude(), gasto.getLongitude(), gasto.getLocationName());
    }

    // Exclui um gasto do banco de dados pelo seu ID.
    public boolean deleteGasto(int gastoId) {
        return dbHelper.deleteGasto(gastoId);
    }

    // Converte um Cursor (resultado de uma consulta ao banco de dados) em uma lista de objetos Gasto.
    private List<Gasto> cursorToGastoList(Cursor cursor) {
        List<Gasto> gastos = new ArrayList<>();
        // Verifica se o cursor não é nulo e se contém dados
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Extrai os dados de cada coluna do cursor
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                double value = cursor.getDouble(cursor.getColumnIndexOrThrow("value"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                String formaPagamento = cursor.getString(cursor.getColumnIndexOrThrow("forma_pagamento"));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));
                Double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"));
                Double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"));
                String locationName = cursor.getString(cursor.getColumnIndexOrThrow("location_name"));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));

                // Cria um novo objeto Gasto com os dados extraídos e adiciona à lista
                gastos.add(new Gasto(id, description, value, date, formaPagamento, category, imagePath, latitude, longitude, locationName, userId));
            } while (cursor.moveToNext()); // Move para a próxima linha do cursor
            cursor.close(); // Fecha o cursor após o uso para liberar recursos
        }
        return gastos;
    }
}
