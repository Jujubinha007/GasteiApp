package com.example.gasteiapp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// Activity para adicionar ou editar um gasto.
public class AddGasto extends AppCompatActivity {
    // Componentes da UI
    private Toolbar my_toolbar;
    private Spinner spinnerCategoria, spinnerFmPagamento;
    private EditText editData, editValor, editDescricao;
    private Button btnAddGasto;
    private Button btnLimpar;
    private ImageButton btnAddFoto;
    private ImageView imagePreview;
    private ImageButton btnRemoveImage;
    private View imageContainer;

    // ViewModel para a lógica de adicionar/editar gastos
    private AddGastoViewModel addGastoViewModel;
    private String currentImagePath;

    // Elementos relacionados à localização
    private Button btnGetLocation;
    private Button btnSelectPlace;
    private EditText editLocation;
    private CheckBox checkSavePlace;
    private LinearLayout layoutSavePlace;

    // Dados de localização
    private Double currentLatitude;
    private Double currentLongitude;
    private LocationManager locationManager;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private boolean isEditMode = false;
    private Gasto currentGasto;
    private int userId;

    // Launcher para iniciar a CameraActivity e obter o resultado (caminho da imagem)
    ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.hasExtra("image_path")) {
                        currentImagePath = data.getStringExtra("image_path");
                        imagePreview.setImageURI(Uri.parse(currentImagePath));
                        imageContainer.setVisibility(View.VISIBLE);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gasto);

        // Inicializa o ViewModel e o LocationManager
        addGastoViewModel = new ViewModelProvider(this).get(AddGastoViewModel.class);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Obtém o ID do usuário logado das SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        userId = sharedPreferences.getInt(MainActivity.USER_ID_KEY, -1);

        // Inicializa as views, configura a toolbar, spinners e listeners
        initViews();
        setupToolbar();
        setupSpinners();
        setupListeners();

        // Verifica se a Activity foi iniciada no modo de edição (passando um objeto Gasto)
        Intent intent = getIntent();
        if (intent.hasExtra("gasto")) {
            isEditMode = true;
            currentGasto = intent.getParcelableExtra("gasto");
            setupEditMode(); // Configura a UI para o modo de edição
        } else {
            setupCreateMode(); // Configura a UI para o modo de criação
        }

        // Observa o resultado da operação de salvar/atualizar do ViewModel
        addGastoViewModel.getSaveResult().observe(this, success -> {
            if (success) {
                String message = isEditMode ? "Gasto atualizado com sucesso!" : "Gasto adicionado com sucesso!";
                Toast.makeText(AddGasto.this, message, Toast.LENGTH_SHORT).show();
                finish(); // Finaliza a Activity após o sucesso
            } else {
                String message = isEditMode ? "Erro ao atualizar o gasto." : "Erro ao adicionar o gasto.";
                Toast.makeText(AddGasto.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Inicializa todas as views da Activity
    private void initViews() {
        my_toolbar = findViewById(R.id.my_toolbar);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        spinnerFmPagamento = findViewById(R.id.spinnerFmPagamento);
        editData = findViewById(R.id.editData);
        editValor = findViewById(R.id.editValor);
        editDescricao = findViewById(R.id.editDescricao);
        imagePreview = findViewById(R.id.imagePreview);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);
        imageContainer = findViewById(R.id.imageContainer);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnSelectPlace = findViewById(R.id.btnSelectPlace);
        editLocation = findViewById(R.id.editLocation);
        checkSavePlace = findViewById(R.id.checkSavePlace);
        layoutSavePlace = findViewById(R.id.layoutSavePlace);
        btnAddGasto = findViewById(R.id.btnSalvar);
        btnLimpar = findViewById(R.id.btnLimpar);
        btnAddFoto = findViewById(R.id.btnEntrar);
    }

    // Configura a Toolbar da Activity
    private void setupToolbar() {
        setSupportActionBar(my_toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Habilita o botão de voltar
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back); // Define o ícone do botão de voltar
        }
    }

    // Configura os Spinners de Categoria e Forma de Pagamento
    private void setupSpinners() {
        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(
                this, R.array.categorias_array, android.R.layout.simple_spinner_item);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(catAdapter);

        ArrayAdapter<CharSequence> pagAdapter = ArrayAdapter.createFromResource(
                this, R.array.pagamento_array,
                android.R.layout.simple_spinner_item);
        pagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFmPagamento.setAdapter(pagAdapter);
    }

    // Configura os listeners para os botões e campos de texto
    private void setupListeners() {
        // Listener para o botão de adicionar foto (inicia a CameraActivity)
        btnAddFoto.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(AddGasto.this, CameraActivity.class);
            cameraLauncher.launch(cameraIntent);
        });

        // Listener para o botão de remover imagem
        btnRemoveImage.setOnClickListener(v -> {
            currentImagePath = null;
            imagePreview.setImageURI(null);
            imageContainer.setVisibility(View.GONE);
        });

        // Listeners para os botões de localização e seleção de local salvo
        btnGetLocation.setOnClickListener(v -> getCurrentLocation());
        btnSelectPlace.setOnClickListener(v -> showSavedPlaces());
        // Listener para o campo de data (abre o DatePickerDialog)
        editData.setOnClickListener(v -> showDatePickerDialog());
    }

    // Configura a UI para o modo de edição de um gasto existente
    private void setupEditMode() {
        // Preenche os campos com os dados do gasto existente
        editDescricao.setText(currentGasto.getDescription());
        editValor.setText(String.valueOf(currentGasto.getValue()));
        editData.setText(currentGasto.getDate());

        // Define a seleção dos spinners de Categoria e Forma de Pagamento
        ArrayAdapter<CharSequence> catAdapter = (ArrayAdapter<CharSequence>) spinnerCategoria.getAdapter();
        if (currentGasto.getCategory() != null) {
            int catPos = catAdapter.getPosition(currentGasto.getCategory());
            if (catPos >= 0) spinnerCategoria.setSelection(catPos);
        }

        ArrayAdapter<CharSequence> pagAdapter = (ArrayAdapter<CharSequence>) spinnerFmPagamento.getAdapter();
        if (currentGasto.getFormaPagamento() != null) {
            int pagPos = pagAdapter.getPosition(currentGasto.getFormaPagamento());
            if (pagPos >= 0) spinnerFmPagamento.setSelection(pagPos);
        }

        // Exibe a imagem se houver
        currentImagePath = currentGasto.getImagePath();
        if (currentImagePath != null && !currentImagePath.isEmpty()) {
            imagePreview.setImageURI(Uri.parse(currentImagePath));
            imageContainer.setVisibility(View.VISIBLE);
        }

        // Preenche o campo de localização se houver
        if (currentGasto.getLocationName() != null && !currentGasto.getLocationName().isEmpty()) {
            editLocation.setText(currentGasto.getLocationName());
        }

        // Define as coordenadas de localização
        currentLatitude = currentGasto.getLatitude();
        currentLongitude = currentGasto.getLongitude();
        if (currentLatitude != null && currentLongitude != null) {
            layoutSavePlace.setVisibility(View.VISIBLE);
        }

        // Configura o botão "Limpar" para "Excluir" no modo de edição
        btnLimpar.setText("Excluir");
        btnLimpar.setBackground(ContextCompat.getDrawable(this, R.drawable.button_background_red));
        btnLimpar.setTextColor(Color.WHITE);
        btnLimpar.setBackgroundTintList(null);
        btnLimpar.setOnClickListener(v -> {
            // Chama o ViewModel para excluir o gasto
            addGastoViewModel.deleteGasto(currentGasto.getId());
            Toast.makeText(AddGasto.this, "Gasto excluído com sucesso!", Toast.LENGTH_SHORT).show();
            finish(); // Finaliza a Activity
        });

        // Configura o botão "Salvar" para "Salvar Alterações" no modo de edição
        btnAddGasto.setText("Salvar Alterações");
        btnAddGasto.setBackground(ContextCompat.getDrawable(this, R.drawable.button_background_dark_green));
        btnAddGasto.setTextColor(Color.WHITE);
        btnAddGasto.setBackgroundTintList(null);
        btnAddGasto.setOnClickListener(v -> handleSaveOrUpdate());
    }

    // Configura a UI para o modo de criação de um novo gasto
    private void setupCreateMode() {
        // Configura o botão "Limpar" para limpar os campos
        btnLimpar.setText("Limpar");
        btnLimpar.setBackground(ContextCompat.getDrawable(this, R.drawable.button_background_gray));
        btnLimpar.setTextColor(Color.BLACK);
        btnLimpar.setBackgroundTintList(null);
        btnLimpar.setOnClickListener(v -> clearFields());

        // Configura o botão "Salvar" para adicionar um novo gasto
        btnAddGasto.setText("Salvar");
        btnAddGasto.setBackground(ContextCompat.getDrawable(this, R.drawable.button_background_dark_green));
        btnAddGasto.setTextColor(Color.WHITE);
        btnAddGasto.setBackgroundTintList(null);
        btnAddGasto.setOnClickListener(v -> handleSaveOrUpdate());
    }

    // Limpa todos os campos da UI
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

    // Exibe o DatePickerDialog para seleção da data
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

    // Lida com a lógica de salvar ou atualizar um gasto
    private void handleSaveOrUpdate() {
        String category = spinnerCategoria.getSelectedItem().toString();
        String formaPagamento = spinnerFmPagamento.getSelectedItem().toString();
        String description = editDescricao.getText().toString();
        String date = editData.getText().toString();
        String valueStr = editValor.getText().toString();

        // Validação dos campos obrigatórios
        if (category.isEmpty() || formaPagamento.isEmpty() || date.isEmpty() || valueStr.isEmpty() || description.isEmpty()) {
            Toast.makeText(AddGasto.this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        double value;
        try {
            value = Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            Toast.makeText(AddGasto.this, "Por favor, insira um valor válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        String locationName = editLocation.getText().toString().trim();
        if (locationName.isEmpty()) {
            locationName = null;
            currentLatitude = null;
            currentLongitude = null;
        }

        // Salva o local se a checkbox estiver marcada e as coordenadas estiverem disponíveis
        if (checkSavePlace.isChecked() && currentLatitude != null && currentLongitude != null && locationName != null) {
            // Verifica se não é apenas uma coordenada (para evitar salvar coordenadas como nome de local)
            if (!locationName.contains(",")) { // Not just coordinates
                addGastoViewModel.addPlace(new Place(0, locationName, currentLatitude, currentLongitude), userId);
            }
        }

        // Cria ou atualiza o objeto Gasto e chama o ViewModel
        if (isEditMode) {
            currentGasto.setDescription(description);
            currentGasto.setValue(value);
            currentGasto.setDate(date);
            currentGasto.setFormaPagamento(formaPagamento);
            currentGasto.setCategory(category);
            currentGasto.setImagePath(currentImagePath);
            currentGasto.setLatitude(currentLatitude);
            currentGasto.setLongitude(currentLongitude);
            currentGasto.setLocationName(locationName);
            addGastoViewModel.updateGasto(currentGasto);
        } else {
            Gasto newGasto = new Gasto(0, description, value, date, formaPagamento, category, currentImagePath, currentLatitude, currentLongitude, locationName, userId);
            addGastoViewModel.saveGasto(newGasto);
        }
    }

    // Obtém a localização atual do dispositivo
    private void getCurrentLocation() {
        // Verifica permissão de localização
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Feedback visual para o usuário
        btnGetLocation.setText("Localizando...");
        btnGetLocation.setEnabled(false);

        try {
            // Tenta obter a última localização conhecida via GPS ou rede
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();

                // Verifica se há um local salvo próximo
                String nearbyPlace = addGastoViewModel.findNearbyPlace(currentLatitude, currentLongitude, userId);

                if (nearbyPlace != null) {
                    // Se um local próximo for encontrado, preenche o campo e esconde a opção de salvar
                    editLocation.setText(nearbyPlace);
                    layoutSavePlace.setVisibility(View.GONE);
                    btnGetLocation.setText("✓ " + nearbyPlace);
                    btnGetLocation.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
                    Toast.makeText(this, "Local reconhecido: " + nearbyPlace, Toast.LENGTH_LONG).show();
                    // Reseta o botão após um tempo
                    btnGetLocation.postDelayed(() -> {
                        btnGetLocation.setText("Local Atual");
                        btnGetLocation.setBackgroundTintList(getResources().getColorStateList(R.color.dark_green));
                    }, 2000);

                } else {
                    // Se nenhum local próximo for encontrado, exibe as coordenadas e a opção de salvar
                    String locationText = String.format("%.6f, %.6f", currentLatitude, currentLongitude);
                    editLocation.setText(locationText);
                    layoutSavePlace.setVisibility(View.VISIBLE);
                    checkSavePlace.setChecked(false);
                    btnGetLocation.setText("Local Atual");
                    Toast.makeText(this, "Localização obtida! Você pode salvar este local para uso futuro.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Não foi possível obter a localização atual. Verifique se o GPS está ativado.", Toast.LENGTH_LONG).show();
                btnGetLocation.setText("Local Atual");
            }
        }
 catch (SecurityException e) {
            Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show();
            btnGetLocation.setText("Local Atual");
        }

        btnGetLocation.setEnabled(true);
    }

    // Exibe uma lista de locais salvos para seleção
    private void showSavedPlaces() {
        // Nota: A busca por locais salvos ainda usa o PlaceRepository diretamente aqui
        // para evitar uma refatoração ainda maior neste momento.
        PlaceRepository placeRepository = new PlaceRepository(this);
        List<Place> places = placeRepository.getUserPlaces(userId);
        List<String> placeDisplayNames = new ArrayList<>();

        if (places.isEmpty()) {
            Toast.makeText(this, "Nenhum local salvo encontrado.\nUse 'Local Atual' para salvar lugares!", Toast.LENGTH_LONG).show();
            return;
        }

        // Prepara os nomes dos locais para exibição, incluindo a distância se a localização atual estiver disponível
        for (Place place : places) {
            String displayName = place.getName();
            if (currentLatitude != null && currentLongitude != null) {
                double distance = DatabaseHelper.calculateDistance(currentLatitude, currentLongitude, place.getLatitude(), place.getLongitude());
                if (distance < 1000) {
                    displayName = String.format("%s (%.0fm)", place.getName(), distance);
                } else {
                    displayName = String.format("%s (%.1fkm)", place.getName(), distance / 1000);
                }
                if (distance <= 50) {
                    displayName = displayName + " - Você está aqui!";
                }
            }
            placeDisplayNames.add(displayName);
        }

        // Cria e exibe o AlertDialog com a lista de locais
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecionar Local Salvo");
        builder.setItems(placeDisplayNames.toArray(new String[0]), (dialog, which) -> {
            Place selectedPlace = places.get(which);
            editLocation.setText(selectedPlace.getName());
            currentLatitude = selectedPlace.getLatitude();
            currentLongitude = selectedPlace.getLongitude();
            layoutSavePlace.setVisibility(View.GONE);
            Toast.makeText(AddGasto.this, "Local selecionado: " + selectedPlace.getName(), Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancelar", null);
        // Opção para gerenciar locais salvos
        builder.setNeutralButton("Gerenciar Locais", (dialog, which) -> {
            Intent intent = new Intent(AddGasto.this, ManagePlacesActivity.class);
            startActivity(intent);
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Lida com o resultado da solicitação de permissão de localização
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation(); // Se a permissão for concedida, tenta obter a localização novamente
            } else {
                Toast.makeText(this, "Permissão de localização é necessária para usar o GPS", Toast.LENGTH_SHORT).show();
            }
        }
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
