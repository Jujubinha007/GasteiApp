package com.example.gasteiapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.widget.ImageButton;
import android.app.DatePickerDialog;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import android.content.res.ColorStateList;

public class AddGasto extends AppCompatActivity {
    Toolbar my_toolbar;
    private Spinner spinnerCategoria, spinnerFmPagamento;
    private EditText editData, editValor, editDescricao;
    private Button btnAddGasto;
    private Button btnLimpar;
    private ImageButton btnAddFoto;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_gasto);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        my_toolbar = findViewById(R.id.my_toolbar);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        spinnerFmPagamento = findViewById(R.id.spinnerFmPagamento);
        editData = findViewById(R.id.editData);
        editValor = findViewById(R.id.editValor);
        editDescricao = findViewById(R.id.editDescricao);

        // categoria
        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(
                this, R.array.categorias_array, android.R.layout.simple_spinner_item);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(catAdapter);

        // Pagamentos
        ArrayAdapter<CharSequence> pagAdapter = ArrayAdapter.createFromResource(
                this, R.array.pagamento_array,
                android.R.layout.simple_spinner_item);
        pagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFmPagamento.setAdapter(pagAdapter);

        dbHelper = new DatabaseHelper(this);

        setSupportActionBar(my_toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Corrected this line
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }

        btnAddGasto = findViewById(R.id.btnSalvar);
        btnLimpar = findViewById(R.id.btnLimpar);
        btnAddFoto = findViewById(R.id.btnEntrar);

        // Default listeners (will be overwritten in edit mode if necessary)
        btnAddGasto.setOnClickListener(null);
        btnLimpar.setOnClickListener(null);

        Intent intent = getIntent();
        boolean isEditMode = intent.getBooleanExtra("edit_mode", false);
        int gastoId = -1;
        if (isEditMode) {
            gastoId = intent.getIntExtra("gasto_id", -1);
            String description = intent.getStringExtra("description");
            double value = intent.getDoubleExtra("value", 0);
            String date = intent.getStringExtra("date");
            String category = intent.getStringExtra("category");
            String formaPagamento = intent.getStringExtra("forma_pagamento");

            // Pre-fill fields
            editDescricao.setText(description);
            editValor.setText(String.valueOf(value));
            editData.setText(date);
            // Set spinner selection
            if (category != null) {
                int catPos = catAdapter.getPosition(category);
                if (catPos >= 0) spinnerCategoria.setSelection(catPos);
            }
            if (formaPagamento != null) {
                int pagPos = pagAdapter.getPosition(formaPagamento);
                if (pagPos >= 0) spinnerFmPagamento.setSelection(pagPos);
            }

            // Configure buttons
            // --- Desired order in EDIT MODE ---
            // Top button (btnLimpar) -> Excluir (red)
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

            // Bottom button (btnAddGasto) -> Salvar Alterações (green)
            btnAddGasto.setText("Salvar Alterações");
            btnAddGasto.setBackground(ContextCompat.getDrawable(this, R.drawable.button_background_dark_green));
            btnAddGasto.setTextColor(Color.WHITE);
            btnAddGasto.setBackgroundTintList(null);
            final int finalGastoIdUpdate = gastoId;
            btnAddGasto.setOnClickListener(v -> {
                handleSaveOrUpdate(catAdapter, pagAdapter, true, finalGastoIdUpdate);
            });
        } else {
            // ---------- CREATE MODE CONFIG ----------
            // btnLimpar is now the top button, should be gray
            btnLimpar.setText("Limpar");
            btnLimpar.setBackground(ContextCompat.getDrawable(this, R.drawable.button_background_gray));
            btnLimpar.setTextColor(Color.BLACK);
            btnLimpar.setBackgroundTintList(null);
            btnLimpar.setOnClickListener(v -> {
                // Clear inputs
                editDescricao.setText("");
                editValor.setText("");
                editData.setText("");
                spinnerCategoria.setSelection(0);
                spinnerFmPagamento.setSelection(0);
            });

            // btnAddGasto is now the bottom button, should be dark green
            btnAddGasto.setText("Salvar");
            btnAddGasto.setBackground(ContextCompat.getDrawable(this, R.drawable.button_background_dark_green));
            btnAddGasto.setTextColor(Color.WHITE);
            btnAddGasto.setBackgroundTintList(null);
            btnAddGasto.setOnClickListener(v -> {
                handleSaveOrUpdate(catAdapter, pagAdapter, false, -1);
            });
        }

        // Camera button remains unchanged
        btnAddFoto.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(AddGasto.this, CameraActivity.class);
            startActivity(cameraIntent);
        });

        editData.setOnClickListener(v -> showDatePickerDialog());
    }

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

    // Helper to save or update
    private void handleSaveOrUpdate(ArrayAdapter<CharSequence> catAdapter, ArrayAdapter<CharSequence> pagAdapter, boolean isUpdate, int gastoId) {
        String category = spinnerCategoria.getSelectedItem().toString();
        String formaPagamento = spinnerFmPagamento.getSelectedItem().toString();
        String description = editDescricao.getText().toString();
        String date = editData.getText().toString();
        String valueStr = editValor.getText().toString();

        if (catAdapter.isEmpty() || pagAdapter.isEmpty() || date.isEmpty() || valueStr.isEmpty() || description.isEmpty()) {
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

        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        int userId = sharedPreferences.getInt(MainActivity.USER_ID_KEY, -1);

        boolean success;
        if (isUpdate) {
            success = dbHelper.updateGasto(gastoId, description, value, date, formaPagamento, category);
            if (success) {
                Toast.makeText(AddGasto.this, "Gasto atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddGasto.this, "Erro ao atualizar o gasto.", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (userId == -1) {
                Toast.makeText(AddGasto.this, "Erro: Usuário não logado.", Toast.LENGTH_SHORT).show();
                return;
            }
            success = dbHelper.addGasto(category, value, date, formaPagamento, description, userId);
            if (success) {
                Toast.makeText(AddGasto.this, "Gasto adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddGasto.this, "Erro ao adicionar o gasto.", Toast.LENGTH_SHORT).show();
            }
        }
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
