package com.example.gasteiapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.widget.ImageButton;
import android.app.DatePickerDialog;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.location.Location;
import android.location.LocationManager;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.AlertDialog;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.view.View;
import android.view.MenuItem;

// Activity para adicionar ou editar um gasto.
public class AddGasto extends AppCompatActivity {
    private Toolbar my_toolbar;
    private Spinner spinnerCategoria, spinnerFmPagamento;
    private EditText editData, editValor, editDescricao;
    private Button btnAddGasto;
    private Button btnLimpar;
    private ImageButton btnAddFoto;
    private ImageView imagePreview;
    private ImageButton btnRemoveImage;
    private View imageContainer;
    private DatabaseHelper dbHelper;
    private String currentImagePath;

    // Elementos relacionados à localização.
    private Button btnGetLocation;
    private Button btnSelectPlace;
    private EditText editLocation;
    private CheckBox checkSavePlace;
    private LinearLayout layoutSavePlace;
    
    // Dados de localização.
    private Double currentLatitude;
    private Double currentLongitude;
    private LocationManager locationManager;
    
    // Código de requisição de permissão de localização.
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // Launcher para iniciar a CameraActivity e obter o resultado (caminho da imagem).
    ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.hasExtra("image_path")) {
                            currentImagePath = data.getStringExtra("image_path");
                            imagePreview.setImageURI(Uri.parse(currentImagePath));
                            imageContainer.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_gasto);
        // Ajusta o padding da tela para acomodar as barras do sistema (status bar, navigation bar).
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa as views.
        my_toolbar = findViewById(R.id.my_toolbar);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        spinnerFmPagamento = findViewById(R.id.spinnerFmPagamento);
        editData = findViewById(R.id.editData);
        editValor = findViewById(R.id.editValor);
        editDescricao = findViewById(R.id.editDescricao);
        imagePreview = findViewById(R.id.imagePreview);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);
        imageContainer = findViewById(R.id.imageContainer);

        // Elementos de localização.
        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnSelectPlace = findViewById(R.id.btnSelectPlace);
        editLocation = findViewById(R.id.editLocation);
        checkSavePlace = findViewById(R.id.checkSavePlace);
        layoutSavePlace = findViewById(R.id.layoutSavePlace);
        
        // Inicializa o LocationManager.
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Configuração do spinner de categoria.
        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(
                this, R.array.categorias_array, android.R.layout.simple_spinner_item);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(catAdapter);

        // Configuração do spinner de formas de pagamento.
        ArrayAdapter<CharSequence> pagAdapter = ArrayAdapter.createFromResource(
                this, R.array.pagamento_array,
                android.R.layout.simple_spinner_item);
        pagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFmPagamento.setAdapter(pagAdapter);

        // Inicializa o DatabaseHelper.
        dbHelper = new DatabaseHelper(this);

        // Configuração da Toolbar.
        setSupportActionBar(my_toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Habilita o botão de voltar.
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }

        // Inicializa botões.
        btnAddGasto = findViewById(R.id.btnSalvar);
        btnLimpar = findViewById(R.id.btnLimpar);
        btnAddFoto = findViewById(R.id.btnEntrar);

        // Remove listeners padrão para configurar em modo de edição ou criação.
        btnAddGasto.setOnClickListener(null);
        btnLimpar.setOnClickListener(null);

        Intent intent = getIntent();
        boolean isEditMode = intent.getBooleanExtra("edit_mode", false);
        int gastoId = -1;
        // Verifica se a Activity foi iniciada no modo de edição.
        if (isEditMode) {
            gastoId = intent.getIntExtra("gasto_id", -1);
            String description = intent.getStringExtra("description");
            double value = intent.getDoubleExtra("value", 0);
            String date = intent.getStringExtra("date");
            String category = intent.getStringExtra("category");
            String formaPagamento = intent.getStringExtra("forma_pagamento");
            currentImagePath = intent.getStringExtra("image_path");

            // Carrega dados de localização existentes se estiver no modo de edição.
            String existingLocation = intent.getStringExtra("location_name");
            currentLatitude = intent.getDoubleExtra("latitude", Double.NaN);
            currentLongitude = intent.getDoubleExtra("longitude", Double.NaN);
            
            if (existingLocation != null && !existingLocation.isEmpty()) {
                editLocation.setText(existingLocation);
            }
            
            if (!Double.isNaN(currentLatitude) && !Double.isNaN(currentLongitude)) {
                layoutSavePlace.setVisibility(View.VISIBLE);
            }

            // Preenche os campos da UI com os dados do gasto para edição.
            editDescricao.setText(description);
            editValor.setText(String.valueOf(value));
            editData.setText(date);
            // Define a seleção dos spinners.
            if (category != null) {
                int catPos = catAdapter.getPosition(category);
                if (catPos >= 0) spinnerCategoria.setSelection(catPos);
            }
            if (formaPagamento != null) {
                int pagPos = pagAdapter.getPosition(formaPagamento);
                if (pagPos >= 0) spinnerFmPagamento.setSelection(pagPos);
            }

            // Exibe a imagem se houver um caminho.
            if (currentImagePath != null && !currentImagePath.isEmpty()) {
                imagePreview.setImageURI(Uri.parse(currentImagePath));
                imageContainer.setVisibility(View.VISIBLE);
            }

            // Configura o botão "Limpar" para "Excluir" no modo de edição.
            btnLimpar.setText("Excluir");
            btnLimpar.setBackground(ContextCompat.getDrawable(this, R.drawable.button_background_red));
            btnLimpar.setTextColor(Color.WHITE);
            btnLimpar.setBackgroundTintList(null);
            final int finalGastoIdDelete = gastoId;
            btnLimpar.setOnClickListener(v -> {
                if (finalGastoIdDelete != -1) {
                    boolean success = dbHelper.deleteGasto(finalGastoIdDelete);
                    if (success) {
                        Toast.makeText(AddGasto.this, "Gasto excluído com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddGasto.this, "Erro ao excluir o gasto.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Configura o botão "Salvar" para "Salvar Alterações" no modo de edição.
            btnAddGasto.setText("Salvar Alterações");
            btnAddGasto.setBackground(ContextCompat.getDrawable(this, R.drawable.button_background_dark_green));
            btnAddGasto.setTextColor(Color.WHITE);
            btnAddGasto.setBackgroundTintList(null);
            final int finalGastoIdUpdate = gastoId;
            btnAddGasto.setOnClickListener(v -> {
                handleSaveOrUpdate(catAdapter, pagAdapter, true, finalGastoIdUpdate);
            });
        } else {
            // Configura o botão "Limpar" para o modo de criação.
            btnLimpar.setText("Limpar");
            btnLimpar.setBackground(ContextCompat.getDrawable(this, R.drawable.button_background_gray));
            btnLimpar.setTextColor(Color.BLACK);
            btnLimpar.setBackgroundTintList(null);
            btnLimpar.setOnClickListener(v -> {
                // Limpa todos os campos de entrada.
                editDescricao.setText("");
                editValor.setText("");
                editData.setText("");
                spinnerCategoria.setSelection(0);
                spinnerFmPagamento.setSelection(0);
                // Limpa a imagem.
                currentImagePath = null;
                imagePreview.setImageURI(null);
                imageContainer.setVisibility(View.GONE);
                // Limpa os dados de localização.
                currentLatitude = null;
                currentLongitude = null;
                editLocation.setText("");
                layoutSavePlace.setVisibility(View.GONE);
                checkSavePlace.setChecked(false);
            });

            // Configura o botão "Adicionar Gasto" para o modo de criação.
            btnAddGasto.setText("Adicionar Gasto");
            btnAddGasto.setBackground(ContextCompat.getDrawable(this, R.drawable.button_background_dark_green));
            btnAddGasto.setTextColor(Color.WHITE);
            btnAddGasto.setBackgroundTintList(null);
            btnAddGasto.setOnClickListener(v -> {
                handleSaveOrUpdate(catAdapter, pagAdapter, false, -1);
            });
        }

        // Listener para o botão de adicionar foto.
        btnAddFoto.setOnClickListener(v -> {
            Intent intentCamera = new Intent(AddGasto.this, CameraActivity.class);
            cameraLauncher.launch(intentCamera);
        });

        // Listener para o botão de remover imagem.
        btnRemoveImage.setOnClickListener(v -> {
            currentImagePath = null;
            imagePreview.setImageURI(null);
            imageContainer.setVisibility(View.GONE);
        });

        // Listener para o campo de data (abre o DatePickerDialog).
        editData.setOnClickListener(v -> showDatePickerDialog());

        // Listener para o botão de obter localização atual.
        btnGetLocation.setOnClickListener(v -> getCurrentLocation());

        // Listener para o botão de selecionar locais salvos.
        btnSelectPlace.setOnClickListener(v -> showSavedPlaces());

        // Listener para o CheckBox de salvar local, controla a visibilidade do layout.
        checkSavePlace.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layoutSavePlace.setVisibility(View.VISIBLE);
            } else {
                layoutSavePlace.setVisibility(View.GONE);
            }
        });
    }

    @Override
    // Lida com a seleção de itens na Toolbar (ex: botão de voltar).
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Limpa todos os campos da UI.
    private void clearFields() {
        editDescricao.setText("");
        editValor.setText("");
        editData.setText("");
        spinnerCategoria.setSelection(0);
        spinnerFmPagamento.setSelection(0);
        currentImagePath = null;
        imagePreview.setImageURI(null);
        imageContainer.setVisibility(View.GONE);
        currentLatitude = null;
        currentLongitude = null;
        editLocation.setText("");
        layoutSavePlace.setVisibility(View.GONE);
        checkSavePlace.setChecked(false);
    }

    // Exibe o DatePickerDialog para seleção da data.
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year1, monthOfYear, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    editData.setText(sdf.format(newDate.getTime()));
                }, year, month, day);

        datePickerDialog.show();
    }

    // Lida com a lógica de salvar ou atualizar um gasto.
    private void handleSaveOrUpdate(ArrayAdapter<CharSequence> catAdapter, ArrayAdapter<CharSequence> pagAdapter, boolean isUpdate, int gastoId) {
        String category = spinnerCategoria.getSelectedItem().toString();
        String formaPagamento = spinnerFmPagamento.getSelectedItem().toString();
        String description = editDescricao.getText().toString();
        String date = editData.getText().toString();
        String valueStr = editValor.getText().toString();

        // Validação dos campos obrigatórios.
        if (category.isEmpty() || formaPagamento.isEmpty() || date.isEmpty() || valueStr.isEmpty() || description.isEmpty()) {
            Toast.makeText(AddGasto.this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        double value;
        try {
            value = Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            Toast.makeText(AddGasto.this, "Valor inválido. Por favor, insira um número válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        int userId = sharedPreferences.getInt(MainActivity.USER_ID_KEY, -1);

        if (userId != -1) {
            // Lida com os dados de localização.
            String locationName = editLocation.getText().toString().trim();
            if (locationName.isEmpty()) {
                locationName = null;
                currentLatitude = null;
                currentLongitude = null;
            }
            
            // Salva o local se solicitado e se as coordenadas GPS estiverem disponíveis e o nome do local não for apenas coordenadas.
            if (checkSavePlace.isChecked() && currentLatitude != null && currentLongitude != null && locationName != null && !locationName.matches("^-?\\d+\\.\\d+,\\s*-?\\d+\\.\\d+$")) {
                boolean placeSaved = dbHelper.addPlace(locationName, currentLatitude, currentLongitude, userId);
                if (placeSaved) {
                    Toast.makeText(AddGasto.this, "Local salvo para uso futuro!", Toast.LENGTH_SHORT).show();
                }
            }
            
            boolean success;
            // Chama o método de atualização ou adição no DatabaseHelper.
            if (isUpdate) {
                success = dbHelper.updateGasto(gastoId, description, value, date, formaPagamento, category, currentImagePath, currentLatitude, currentLongitude, locationName);
            } else {
                success = dbHelper.addGasto(category, value, date, formaPagamento, description, userId, currentImagePath, currentLatitude, currentLongitude, locationName);
            }

            if (success) {
                String message = isUpdate ? "Gasto atualizado com sucesso!" : "Gasto adicionado com sucesso!";
                Toast.makeText(AddGasto.this, message, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                String message = isUpdate ? "Erro ao atualizar o gasto." : "Erro ao adicionar o gasto.";
                Toast.makeText(AddGasto.this, message, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(AddGasto.this, "Erro: Usuário não encontrado.", Toast.LENGTH_SHORT).show();
        }
    }

    // Métodos de localização.

    // Obtém a localização GPS atual do dispositivo.
    private void getCurrentLocation() {
        // Verifica e solicita permissão de localização se necessário.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Exibe feedback de carregamento.
        btnGetLocation.setText("Localizando...");
        btnGetLocation.setEnabled(false);

        try {
            // Tenta obter a última localização conhecida do GPS ou da rede.
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                
                // Verifica primeiro se há locais salvos próximos.
                SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
                int userId = sharedPreferences.getInt(MainActivity.USER_ID_KEY, -1);
                
                String nearbyPlace = dbHelper.findNearbyPlace(currentLatitude, currentLongitude, userId);
                
                if (nearbyPlace != null) {
                    // Se um local salvo próximo for encontrado, preenche automaticamente e oculta a opção de salvar.
                    editLocation.setText(nearbyPlace);
                    layoutSavePlace.setVisibility(View.GONE); // Oculta a opção de salvar, pois o local já existe.
                    
                    // Exibe feedback de sucesso com o nome do local.
                    btnGetLocation.setText("✓ " + nearbyPlace);
                    btnGetLocation.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
                    
                    Toast.makeText(this, "Local reconhecido: " + nearbyPlace, Toast.LENGTH_LONG).show();
                    
                    // Reinicia o botão após 2 segundos.
                    btnGetLocation.postDelayed(() -> {
                        btnGetLocation.setText("Local Atual");
                        btnGetLocation.setBackgroundTintList(getResources().getColorStateList(R.color.dark_green));
                    }, 2000);
                    
                } else {
                    // Nenhum local próximo encontrado - exibe coordenadas e opção para salvar.
                    String locationText = String.format("%.6f, %.6f", currentLatitude, currentLongitude);
                    editLocation.setText(locationText);
                    layoutSavePlace.setVisibility(View.VISIBLE);
                    checkSavePlace.setChecked(false); // Reseta o checkbox.
                    
                    btnGetLocation.setText("Local Atual");
                    
                    Toast.makeText(this, "Localização obtida! Você pode salvar este local para uso futuro.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Não foi possível obter a localização atual. Verifique se o GPS está ativado.", Toast.LENGTH_LONG).show();
                btnGetLocation.setText("Local Atual");
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show();
            btnGetLocation.setText("Local Atual");
        }
        
        btnGetLocation.setEnabled(true);
    }

    // Exibe um diálogo com a lista de locais salvos para seleção.
    private void showSavedPlaces() {
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        int userId = sharedPreferences.getInt(MainActivity.USER_ID_KEY, -1);
        
        // Obtém a lista de locais salvos do banco de dados.
        Cursor cursor = dbHelper.getUserPlaces(userId);
        List<String> placeDisplayNames = new ArrayList<>();
        List<String> placeNames = new ArrayList<>();
        List<Double> placeLatitudes = new ArrayList<>();
        List<Double> placeLongitudes = new ArrayList<>();
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String placeName = cursor.getString(cursor.getColumnIndexOrThrow("place_name"));
                double placeLat = cursor.getDouble(cursor.getColumnIndexOrThrow("place_latitude"));
                double placeLon = cursor.getDouble(cursor.getColumnIndexOrThrow("place_longitude"));
                
                placeNames.add(placeName);
                placeLatitudes.add(placeLat);
                placeLongitudes.add(placeLon);
                
                // Exibe a distância se a localização atual estiver disponível.
                String displayName = placeName;
                if (currentLatitude != null && currentLongitude != null) {
                    double distance = DatabaseHelper.calculateDistance(currentLatitude, currentLongitude, placeLat, placeLon);
                    if (distance < 1000) {
                        displayName = String.format("%s (%.0fm)", placeName, distance);
                    } else {
                        displayName = String.format("%s (%.1fkm)", placeName, distance / 1000);
                    }
                    
                    // Adiciona um indicador para locais muito próximos.
                    if (distance <= 50) {
                        displayName = displayName + " - Você está aqui!";
                    }
                }
                placeDisplayNames.add(displayName);
            }
            cursor.close();
        }
        
        if (placeNames.isEmpty()) {
            Toast.makeText(this, "Nenhum local salvo encontrado.\nUse 'Local Atual' para salvar lugares!", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Constrói e exibe o diálogo de seleção de locais.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecionar Local Salvo");
        builder.setItems(placeDisplayNames.toArray(new String[0]), (dialog, which) -> {
            String selectedPlace = placeNames.get(which);
            editLocation.setText(selectedPlace);
            
            // Define as coordenadas para o local selecionado.
            currentLatitude = placeLatitudes.get(which);
            currentLongitude = placeLongitudes.get(which);
            
            // Oculta a opção de salvar local, pois este já é um local salvo.
            layoutSavePlace.setVisibility(View.GONE);
            
            Toast.makeText(this, "Local selecionado: " + selectedPlace, Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancelar", null);
        builder.setNeutralButton("Gerenciar Locais", (dialog, which) -> {
            Intent intent = new Intent(AddGasto.this, ManagePlacesActivity.class);
            startActivity(intent);
        });
        builder.show();
    }

    @Override
    // Lida com o resultado da requisição de permissão de localização.
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Permissão de localização é necessária para usar o GPS", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
