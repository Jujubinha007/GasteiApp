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

// Activity para gerenciar os locais salvos pelo usuário.
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
        
        // Inicializa as views.
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerViewPlaces);
        fabAddPlace = findViewById(R.id.fabAddPlace);
        
        // Configura a Toolbar.
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Habilita o botão de voltar.
            getSupportActionBar().setTitle("Gerenciar Locais"); // Define o título da Toolbar.
        }
        
        // Inicializa o banco de dados e obtém o ID do usuário.
        dbHelper = new DatabaseHelper(this);
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        userId = sharedPreferences.getInt(MainActivity.USER_ID_KEY, -1);
        
        // Configura o RecyclerView com o adaptador.
        placesList = new ArrayList<>();
        placesAdapter = new PlacesAdapter(placesList, this::onPlaceEdit, this::onPlaceDelete);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(placesAdapter);
        
        // Listener para o botão de adicionar novo local.
        fabAddPlace.setOnClickListener(v -> showAddPlaceDialog());
        
        // Carrega os locais salvos.
        loadPlaces();
    }
    
    // Carrega a lista de locais salvos do usuário a partir do banco de dados.
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
        
        placesAdapter.notifyDataSetChanged(); // Notifica o adaptador sobre as mudanças nos dados.
    }
    
    // Exibe um diálogo para adicionar um novo local.
    private void showAddPlaceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_place, null);
        EditText editPlaceName = dialogView.findViewById(R.id.etPlaceName);
        EditText editLatitude = dialogView.findViewById(R.id.etLatitude);
        EditText editLongitude = dialogView.findViewById(R.id.etLongitude);
        
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        
        // Configura os botões do diálogo (Cancelar e Salvar).
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String placeName = editPlaceName.getText().toString().trim();
            String latStr = editLatitude.getText().toString().trim();
            String lonStr = editLongitude.getText().toString().trim();
            
            // Validação do nome do local.
            if (placeName.isEmpty()) {
                Toast.makeText(this, "Nome do local é obrigatório", Toast.LENGTH_SHORT).show();
                return;
            }
            
            double latitude, longitude;
            try {
                // Tenta converter as coordenadas para double.
                latitude = Double.parseDouble(latStr);
                longitude = Double.parseDouble(lonStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Adiciona o novo local ao banco de dados.
            boolean success = dbHelper.addPlace(placeName, latitude, longitude, userId);
            if (success) {
                Toast.makeText(this, "Local adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                loadPlaces(); // Recarrega a lista de locais.
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Erro ao adicionar local", Toast.LENGTH_SHORT).show();
            }
        });
        
        dialog.show();
    }
    
    // Lida com a edição de um local existente.
    private void onPlaceEdit(Place place) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_place, null);
        EditText editPlaceName = dialogView.findViewById(R.id.etPlaceName);
        EditText editLatitude = dialogView.findViewById(R.id.etLatitude);
        EditText editLongitude = dialogView.findViewById(R.id.etLongitude);
        
        // Preenche os campos do diálogo com os valores atuais do local.
        editPlaceName.setText(place.getName());
        editLatitude.setText(String.valueOf(place.getLatitude()));
        editLongitude.setText(String.valueOf(place.getLongitude()));
        
        // Atualiza o texto do título para o modo de edição.
        android.widget.TextView titleView = dialogView.findViewById(android.R.id.title);
        if (titleView == null) {
            // Tenta encontrar o TextView do título percorrendo o layout.
            titleView = (android.widget.TextView) ((android.view.ViewGroup) dialogView).getChildAt(0);
        }
        if (titleView != null) {
            titleView.setText("Editar Local");
        }
        
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        
        // Configura os botões do diálogo (Cancelar e Salvar).
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String placeName = editPlaceName.getText().toString().trim();
            String latStr = editLatitude.getText().toString().trim();
            String lonStr = editLongitude.getText().toString().trim();
            
            // Validação do nome do local.
            if (placeName.isEmpty()) {
                Toast.makeText(this, "Nome do local é obrigatório", Toast.LENGTH_SHORT).show();
                return;
            }
            
            double latitude, longitude;
            try {
                // Tenta converter as coordenadas para double.
                latitude = Double.parseDouble(latStr);
                longitude = Double.parseDouble(lonStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Atualiza o local no banco de dados.
            boolean success = dbHelper.updatePlace(place.getId(), placeName, latitude, longitude);
            if (success) {
                Toast.makeText(this, "Local atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                loadPlaces(); // Recarrega a lista de locais.
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Erro ao atualizar local", Toast.LENGTH_SHORT).show();
            }
        });
        
        dialog.show();
    }
    
    // Lida com a exclusão de um local.
    private void onPlaceDelete(Place place) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Local")
                .setMessage("Tem certeza que deseja excluir '" + place.getName() + "'?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    // Exclui o local do banco de dados.
                    boolean success = dbHelper.deletePlace(place.getId());
                    if (success) {
                        Toast.makeText(this, "Local excluído com sucesso!", Toast.LENGTH_SHORT).show();
                        loadPlaces(); // Recarrega a lista de locais.
                    } else {
                        Toast.makeText(this, "Erro ao excluir local", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    
    @Override
    // Lida com a seleção de itens no menu da Toolbar (ex: botão de voltar).
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Finaliza a Activity.
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 