package com.example.gasteiapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.database.Cursor;
import android.widget.Toast;
import android.widget.Button;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.chip.Chip;
import android.widget.TextView;

import java.util.ArrayList;

// Activity Home, responsável por exibir a lista de gastos do usuário.
public class Home extends AppCompatActivity {
    // Componentes da UI.
    RecyclerView recyclerView;
    FloatingActionButton btnAdd;
    Toolbar my_toolbar;
    DatabaseHelper dbHelper;
    GastoAdapter gastoAdapter;
    private Chip chipCurrentMonth, chipLastMonth, chipAll;
    private TextView tvGastosTitle;

    // Listas para armazenar dados dos gastos (não utilizados diretamente após a adoção do Cursor no adapter).
    ArrayList<String> spinnerCategoria, spinnerFmPagamento, date, value, descricao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        // Ajusta o padding da tela para acomodar as barras do sistema (status bar, navigation bar).
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa a Toolbar e o DatabaseHelper.
        my_toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(my_toolbar);
        dbHelper = new DatabaseHelper(this);
        // Inicializa as listas (algumas podem estar obsoletas com o uso do Cursor no Adapter).
        spinnerCategoria = new ArrayList<>();
        spinnerFmPagamento = new ArrayList<>();
        date = new ArrayList<>();
        value = new ArrayList<>();
        descricao = new ArrayList<>();

        // Configura o RecyclerView e o botão de adicionar gasto.
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnAdd = findViewById(R.id.btnAdd);

        // Referencia os chips de filtro e o título dos gastos.
        chipCurrentMonth = findViewById(R.id.chip_current_month);
        chipLastMonth = findViewById(R.id.chip_last_month);
        chipAll = findViewById(R.id.chip_all);
        tvGastosTitle = findViewById(R.id.tvGastosTitle);

        // Configura os listeners para os chips de filtro.
        chipCurrentMonth.setOnClickListener(v -> displayData("CURRENT_MONTH"));
        chipLastMonth.setOnClickListener(v -> displayData("LAST_MONTH"));
        chipAll.setOnClickListener(v -> displayData("ALL"));

        // Listener para o botão de adicionar novo gasto.
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, AddGasto.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Carrega os dados com o filtro "ALL" (todos os gastos) ao retomar a Activity.
        displayData("ALL");
        chipAll.setChecked(true);
    }

    // Exibe os dados de gastos com base no filtro selecionado.
    private void displayData(String filter) {
        // Atualiza o estado de seleção dos chips de filtro.
        chipAll.setChecked("ALL".equals(filter));
        chipCurrentMonth.setChecked("CURRENT_MONTH".equals(filter));
        chipLastMonth.setChecked("LAST_MONTH".equals(filter));
        
        // Define o título da seção de gastos de acordo com o filtro.
        if (tvGastosTitle != null) {
            if ("CURRENT_MONTH".equals(filter)) {
                tvGastosTitle.setText("Seus Gastos - Mês Atual");
            } else if ("LAST_MONTH".equals(filter)) {
                tvGastosTitle.setText("Seus Gastos - Mês Anterior");
            } else {
                tvGastosTitle.setText("Seus Gastos");
            }
        }

        // Obtém o ID do usuário logado das SharedPreferences.
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        int userId = sharedPreferences.getInt(MainActivity.USER_ID_KEY, -1);

        if (userId != -1) {
            // Obtém todos os gastos do usuário do banco de dados.
            Cursor cursor = dbHelper.getGastosByUser(userId); // busca todos
            String[] allColumns = cursor.getColumnNames();
            android.database.MatrixCursor filteredCursor = new android.database.MatrixCursor(allColumns);

            // Verifica se não há gastos para o filtro selecionado.
            if (cursor.getCount() == 0 ){
                Toast.makeText(this, "Sem gastos para o filtro selecionado.", Toast.LENGTH_SHORT).show();
            } else {
                // Determina os meses de referência para filtragem.
                java.util.Calendar calNow = java.util.Calendar.getInstance();
                int currentMonth = calNow.get(java.util.Calendar.MONTH); // Baseado em 0
                int currentYear = calNow.get(java.util.Calendar.YEAR);
                calNow.add(java.util.Calendar.MONTH, -1);
                int lastMonth = calNow.get(java.util.Calendar.MONTH);
                int lastMonthYear = calNow.get(java.util.Calendar.YEAR);

                // Itera sobre os gastos e filtra de acordo com a seleção.
                while (cursor.moveToNext()) {
                    // Adiciona categorias e formas de pagamento às listas (pode ser redundante).
                    spinnerCategoria.add(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                    spinnerFmPagamento.add(cursor.getString(cursor.getColumnIndexOrThrow("forma_pagamento")));
                    String dateFromDb = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    // Tenta analisar a data em diferentes formatos.
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    boolean parsed = false;
                    for (String pattern : new String[]{"yyyy-MM-dd", "dd / MM / yyyy", "dd/MM/yyyy"}) {
                        try {
                            java.text.DateFormat df = new java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault());
                            cal.setTime(df.parse(dateFromDb));
                            parsed = true;
                            break;
                        } catch (Exception ignored) {}
                    }

                    boolean include = true;
                    if (parsed) {
                        int m = cal.get(java.util.Calendar.MONTH);
                        int y = cal.get(java.util.Calendar.YEAR);
                        if ("CURRENT_MONTH".equals(filter)) {
                            include = (m == currentMonth && y == currentYear);
                        } else if ("LAST_MONTH".equals(filter)) {
                            include = (m == lastMonth && y == lastMonthYear);
                        }
                    } else {
                        // Se não puder analisar a data, inclui apenas para o filtro "ALL".
                        include = "ALL".equals(filter);
                    }

                    if (include) {
                        // Adiciona dados às listas (opcional) e ao Cursor filtrado.
                        spinnerCategoria.add(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                        spinnerFmPagamento.add(cursor.getString(cursor.getColumnIndexOrThrow("forma_pagamento")));
                        date.add(formatDateForDisplay(dateFromDb));
                        value.add(cursor.getString(cursor.getColumnIndexOrThrow("value")));
                        descricao.add(cursor.getString(cursor.getColumnIndexOrThrow("description")));

                        // Adiciona a linha ao MatrixCursor filtrado.
                        Object[] rowData = new Object[allColumns.length];
                        for (int i = 0; i < allColumns.length; i++) {
                            int idx = cursor.getColumnIndexOrThrow(allColumns[i]);
                            int type = cursor.getType(idx);
                            switch (type) {
                                case android.database.Cursor.FIELD_TYPE_INTEGER:
                                    rowData[i] = cursor.getInt(idx);
                                    break;
                                case android.database.Cursor.FIELD_TYPE_FLOAT:
                                    rowData[i] = cursor.getDouble(idx);
                                    break;
                                case android.database.Cursor.FIELD_TYPE_STRING:
                                default:
                                    rowData[i] = cursor.getString(idx);
                                    break;
                            }
                        }
                        filteredCursor.addRow(rowData);
                    }
                }
            }
            // Configura o adaptador com o Cursor filtrado e exibe no RecyclerView.
            gastoAdapter = new GastoAdapter(this, filteredCursor);
            recyclerView.setAdapter(gastoAdapter);
            gastoAdapter.notifyDataSetChanged();

            // Configura o listener de clique para os itens do adaptador (para edição).
            gastoAdapter.setOnItemClickListener(new GastoAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Cursor c) {
                    // Obtém os detalhes do gasto do Cursor.
                    int id = c.getInt(c.getColumnIndexOrThrow("id"));
                    String description = c.getString(c.getColumnIndexOrThrow("description"));
                    double value = c.getDouble(c.getColumnIndexOrThrow("value"));
                    String date = c.getString(c.getColumnIndexOrThrow("date"));
                    String category = c.getString(c.getColumnIndexOrThrow("category"));
                    String formaPagamento = c.getString(c.getColumnIndexOrThrow("forma_pagamento"));
                    String imagePath = null;
                    try {
                        imagePath = c.getString(c.getColumnIndexOrThrow("image_path"));
                    } catch (IllegalArgumentException e) {
                        // Coluna não existe, será nula.
                        imagePath = null;
                    }

                    // Obtém dados de localização de forma segura.
                    String locationName = null;
                    Double latitude = null;
                    Double longitude = null;
                    try {
                        locationName = c.getString(c.getColumnIndexOrThrow("location_name"));
                        int latIndex = c.getColumnIndexOrThrow("latitude");
                        int lonIndex = c.getColumnIndexOrThrow("longitude");
                        if (!c.isNull(latIndex) && !c.isNull(lonIndex)) {
                            latitude = c.getDouble(latIndex);
                            longitude = c.getDouble(lonIndex);
                        }
                    } catch (IllegalArgumentException e) {
                        // Colunas de localização ainda não existem.
                    }

                    // Cria um Intent para abrir a AddGasto Activity em modo de edição.
                    Intent intent = new Intent(Home.this, AddGasto.class);
                    intent.putExtra("edit_mode", true);
                    intent.putExtra("gasto_id", id);
                    intent.putExtra("description", description);
                    intent.putExtra("value", value);
                    intent.putExtra("date", date);
                    intent.putExtra("category", category);
                    intent.putExtra("forma_pagamento", formaPagamento);
                    intent.putExtra("image_path", imagePath);
                    intent.putExtra("location_name", locationName);
                    if (latitude != null && longitude != null) {
                        intent.putExtra("latitude", latitude);
                        intent.putExtra("longitude", longitude);
                    }
                    startActivity(intent);
                }
            });
        }
    }

    // Formata uma string de data do formato "yyyy-MM-dd" para "dd/MM/yyyy".
    private String formatDateForDisplay(String dateStr) {
        if (dateStr == null) return "";
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date date = dbFormat.parse(dateStr);
            return displayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateStr; // Retorna a string original se a formatação falhar.
        }
    }

    @Override
    // Cria o menu de opções na Toolbar.
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    // Lida com a seleção de itens no menu da Toolbar (ex: logout).
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // Limpa as SharedPreferences e retorna para a tela de login.
            SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(Home.this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}