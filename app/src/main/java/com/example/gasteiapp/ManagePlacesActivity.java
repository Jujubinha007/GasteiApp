package com.example.gasteiapp;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ManagePlacesActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddPlace;
    private Toolbar toolbar;
    private DatabaseHelper dbHelper;
    private PlacesAdapter placesAdapter;
    private List<Place> placesList;
    private int userId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_places);
        
        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerViewPlaces);
        fabAddPlace = findViewById(R.id.fabAddPlace);
        
        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gerenciar Locais");
        }
        
        // Initialize database and user
        dbHelper = new DatabaseHelper(this);
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        userId = sharedPreferences.getInt(MainActivity.USER_ID_KEY, -1);
        
        // Setup RecyclerView
        placesList = new ArrayList<>();
        placesAdapter = new PlacesAdapter(placesList, this::onPlaceEdit, this::onPlaceDelete);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(placesAdapter);
        
        // FAB click listener
        fabAddPlace.setOnClickListener(v -> showAddPlaceDialog());
        
        // Load places
        loadPlaces();
    }
    
    private void loadPlaces() {
        placesList.clear();
        Cursor cursor = dbHelper.getUserPlaces(userId);
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("place_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("place_name"));
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("place_latitude"));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("place_longitude"));
                
                placesList.add(new Place(id, name, latitude, longitude));
            }
            cursor.close();
        }
        
        placesAdapter.notifyDataSetChanged();
    }
    
    private void showAddPlaceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_place, null);
        EditText editPlaceName = dialogView.findViewById(R.id.etPlaceName);
        EditText editLatitude = dialogView.findViewById(R.id.etLatitude);
        EditText editLongitude = dialogView.findViewById(R.id.etLongitude);
        
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        
        // Setup custom buttons
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String placeName = editPlaceName.getText().toString().trim();
            String latStr = editLatitude.getText().toString().trim();
            String lonStr = editLongitude.getText().toString().trim();
            
            if (placeName.isEmpty()) {
                Toast.makeText(this, "Nome do local é obrigatório", Toast.LENGTH_SHORT).show();
                return;
            }
            
            double latitude, longitude;
            try {
                latitude = Double.parseDouble(latStr);
                longitude = Double.parseDouble(lonStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
                return;
            }
            
            boolean success = dbHelper.addPlace(placeName, latitude, longitude, userId);
            if (success) {
                Toast.makeText(this, "Local adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                loadPlaces();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Erro ao adicionar local", Toast.LENGTH_SHORT).show();
            }
        });
        
        dialog.show();
    }
    
    private void onPlaceEdit(Place place) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_place, null);
        EditText editPlaceName = dialogView.findViewById(R.id.etPlaceName);
        EditText editLatitude = dialogView.findViewById(R.id.etLatitude);
        EditText editLongitude = dialogView.findViewById(R.id.etLongitude);
        
        // Pre-fill current values
        editPlaceName.setText(place.getName());
        editLatitude.setText(String.valueOf(place.getLatitude()));
        editLongitude.setText(String.valueOf(place.getLongitude()));
        
        // Update title text for edit mode - find the first TextView which is our title
        android.widget.TextView titleView = dialogView.findViewById(android.R.id.title);
        if (titleView == null) {
            // Find by traversing the layout
            titleView = (android.widget.TextView) ((android.view.ViewGroup) dialogView).getChildAt(0);
        }
        if (titleView != null) {
            titleView.setText("Editar Local");
        }
        
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        
        // Setup custom buttons
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String placeName = editPlaceName.getText().toString().trim();
            String latStr = editLatitude.getText().toString().trim();
            String lonStr = editLongitude.getText().toString().trim();
            
            if (placeName.isEmpty()) {
                Toast.makeText(this, "Nome do local é obrigatório", Toast.LENGTH_SHORT).show();
                return;
            }
            
            double latitude, longitude;
            try {
                latitude = Double.parseDouble(latStr);
                longitude = Double.parseDouble(lonStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
                return;
            }
            
            boolean success = dbHelper.updatePlace(place.getId(), placeName, latitude, longitude);
            if (success) {
                Toast.makeText(this, "Local atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                loadPlaces();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Erro ao atualizar local", Toast.LENGTH_SHORT).show();
            }
        });
        
        dialog.show();
    }
    
    private void onPlaceDelete(Place place) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Local")
                .setMessage("Tem certeza que deseja excluir '" + place.getName() + "'?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    boolean success = dbHelper.deletePlace(place.getId());
                    if (success) {
                        Toast.makeText(this, "Local excluído com sucesso!", Toast.LENGTH_SHORT).show();
                        loadPlaces();
                    } else {
                        Toast.makeText(this, "Erro ao excluir local", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 