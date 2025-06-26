package com.example.gasteiapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

// Activity para gerenciar locais salvos.
public class ManagePlacesActivity extends AppCompatActivity {

    // Componentes da UI
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddPlace;
    private Toolbar toolbar;

    // Adapter para a lista de locais
    private PlacesAdapter placesAdapter;

    // ViewModel para a lógica de gerenciamento de locais
    private ManagePlacesViewModel managePlacesViewModel;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_places);

        // Inicializa a Toolbar e a configura
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Habilita o botão de voltar
            getSupportActionBar().setTitle("Gerenciar Locais"); // Define o título da Toolbar
        }

        // Obtém o ID do usuário logado das SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        userId = sharedPreferences.getInt(MainActivity.USER_ID_KEY, -1);

        // Configura o RecyclerView
        recyclerView = findViewById(R.id.recyclerViewPlaces);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Inicializa o adapter com uma lista vazia e os listeners de edição/exclusão
        placesAdapter = new PlacesAdapter(new ArrayList<>(), this::onPlaceEdit, this::onPlaceDelete);
        recyclerView.setAdapter(placesAdapter);

        // Configura o FloatingActionButton para adicionar novos locais
        fabAddPlace = findViewById(R.id.fabAddPlace);
        fabAddPlace.setOnClickListener(v -> showAddPlaceDialog(null)); // Passa null para indicar que é um novo local

        // Inicializa o ViewModel e observa as mudanças na lista de locais
        managePlacesViewModel = new ViewModelProvider(this).get(ManagePlacesViewModel.class);
        managePlacesViewModel.getPlaces().observe(this, places -> placesAdapter.setPlaces(places));

        // Carrega os locais se o userId for válido
        if (userId != -1) {
            managePlacesViewModel.loadPlaces(userId);
        }
    }

    // Exibe um diálogo para adicionar ou editar um local.
    // Se 'place' for null, é um novo local; caso contrário, é uma edição.
    private void showAddPlaceDialog(Place place) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_place, null);
        EditText editPlaceName = dialogView.findViewById(R.id.etPlaceName);
        EditText editLatitude = dialogView.findViewById(R.id.etLatitude);
        EditText editLongitude = dialogView.findViewById(R.id.etLongitude);

        // Preenche os campos com os dados do local se estiver no modo de edição
        if (place != null) {
            editPlaceName.setText(place.getName());
            editLatitude.setText(String.valueOf(place.getLatitude()));
            editLongitude.setText(String.valueOf(place.getLongitude()));
        }

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Listener para o botão de cancelar do diálogo
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        // Listener para o botão de salvar do diálogo
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String placeName = editPlaceName.getText().toString().trim();
            String latStr = editLatitude.getText().toString().trim();
            String lonStr = editLongitude.getText().toString().trim();

            // Validação do nome do local
            if (placeName.isEmpty()) {
                Toast.makeText(this, "Nome do local é obrigatório", Toast.LENGTH_SHORT).show();
                return;
            }

            double latitude, longitude;
            try {
                // Tenta converter as coordenadas para double
                latitude = Double.parseDouble(latStr);
                longitude = Double.parseDouble(lonStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
                return;
            }

            // Chama o ViewModel para adicionar ou atualizar o local
            if (place == null) {
                managePlacesViewModel.addPlace(new Place(0, placeName, latitude, longitude), userId);
                Toast.makeText(this, "Local adicionado com sucesso!", Toast.LENGTH_SHORT).show();
            } else {
                place.setName(placeName);
                place.setLatitude(latitude);
                place.setLongitude(longitude);
                managePlacesViewModel.updatePlace(place, userId);
                Toast.makeText(this, "Local atualizado com sucesso!", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss(); // Fecha o diálogo
        });

        dialog.show(); // Exibe o diálogo
    }

    // Callback para quando um local é clicado para edição
    private void onPlaceEdit(Place place) {
        showAddPlaceDialog(place); // Reutiliza o diálogo de adicionar/editar
    }

    // Callback para quando um local é clicado para exclusão
    private void onPlaceDelete(Place place) {
        // Exibe um diálogo de confirmação antes de excluir
        new AlertDialog.Builder(this)
                .setTitle("Excluir Local")
                .setMessage("Tem certeza que deseja excluir '" + place.getName() + "'?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    // Chama o ViewModel para excluir o local
                    managePlacesViewModel.deletePlace(place.getId(), userId);
                    Toast.makeText(this, "Local excluído com sucesso!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Lida com o clique no botão de voltar da Toolbar
        if (item.getItemId() == android.R.id.home) {
            finish(); // Finaliza a Activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 