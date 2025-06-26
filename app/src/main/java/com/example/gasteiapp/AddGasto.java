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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddGasto extends AppCompatActivity {
    Toolbar my_toolbar;
    private Spinner spinnerCategoria, spinnerFmPagamento;
    private EditText editData, editValor, editDescricao;
    private Button btnAddGasto;
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
        btnAddFoto = findViewById(R.id.btnEntrar);

        btnAddGasto.setOnClickListener(v -> {
            String category = spinnerCategoria.getSelectedItem().toString();
            String formaPagamento = spinnerFmPagamento.getSelectedItem().toString();
            String description = editDescricao.getText().toString();
            String date = editData.getText().toString();
            String valueStr = editValor.getText().toString();

            if (catAdapter.isEmpty() || pagAdapter.isEmpty() || date.isEmpty() || valueStr.isEmpty() || description.isEmpty()) {
                Toast.makeText(AddGasto.this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            double value = Double.parseDouble(valueStr);

            SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
            int userId = sharedPreferences.getInt(MainActivity.USER_ID_KEY, -1);

            if (userId != -1) {
                boolean success = dbHelper.addGasto("", value, date, "", "", userId);
                if (success) {
                    Toast.makeText(AddGasto.this, "Gasto adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddGasto.this, "Erro ao adicionar o gasto.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AddGasto.this, "Erro: Usuário não logado.", Toast.LENGTH_SHORT).show();
            }


        });

        btnAddFoto.setOnClickListener(v -> {
            Intent intent = new Intent(AddGasto.this, CameraActivity.class);
            startActivity(intent);
        });
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
