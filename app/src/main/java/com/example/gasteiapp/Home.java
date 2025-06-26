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

import java.util.ArrayList;

public class Home extends AppCompatActivity {
    RecyclerView recyclerView;
    FloatingActionButton btnAdd;
    Toolbar my_toolbar;
    DatabaseHelper dbHelper;
    GastoAdapter gastoAdapter;
    private Chip chipCurrentMonth, chipLastMonth, chipAll;

    ArrayList<String> spinnerCategoria, spinnerFmPagamento, date, value, descricao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        my_toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(my_toolbar);
        dbHelper = new DatabaseHelper(this);
        spinnerCategoria = new ArrayList<>();
        spinnerFmPagamento = new ArrayList<>();
        date = new ArrayList<>();
        value = new ArrayList<>();
        descricao = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnAdd = findViewById(R.id.btnAdd);

        chipCurrentMonth = findViewById(R.id.chip_current_month);
        chipLastMonth = findViewById(R.id.chip_last_month);
        chipAll = findViewById(R.id.chip_all);

        chipCurrentMonth.setOnClickListener(v -> displayData("CURRENT_MONTH"));
        chipLastMonth.setOnClickListener(v -> displayData("LAST_MONTH"));
        chipAll.setOnClickListener(v -> displayData("ALL"));

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
        displayData("ALL"); // Default to show all
    }

    private void displayData(String filter) {
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        int userId = sharedPreferences.getInt(MainActivity.USER_ID_KEY, -1);

        if (userId != -1) {
            Cursor cursor = dbHelper.getGastosByUser(userId, filter);
            // Clear existing data to avoid duplicates on resume
            spinnerCategoria.clear();
            spinnerFmPagamento.clear();
            date.clear();
            value.clear();
            descricao.clear();

            if (cursor.getCount() == 0 ){
                Toast.makeText(this, "Sem gastos para o filtro selecionado.", Toast.LENGTH_SHORT).show();
            }else{
                while (cursor.moveToNext()){
                    spinnerCategoria.add(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY)));
                    spinnerFmPagamento.add(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FORMAPAGAMENTO)));
                    String dateFromDb = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE));
                    date.add(formatDateForDisplay(dateFromDb));
                    value.add(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VALUE)));
                    descricao.add(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)));
                }
            }
            gastoAdapter = new GastoAdapter(this, cursor);
            recyclerView.setAdapter(gastoAdapter);
            gastoAdapter.notifyDataSetChanged();

            gastoAdapter.setOnItemClickListener(new GastoAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Cursor c) {
                    int id = c.getInt(c.getColumnIndexOrThrow("id"));
                    String description = c.getString(c.getColumnIndexOrThrow("description"));
                    double value = c.getDouble(c.getColumnIndexOrThrow("value"));
                    String date = c.getString(c.getColumnIndexOrThrow("date"));
                    String category = c.getString(c.getColumnIndexOrThrow("category"));
                    String formaPagamento = c.getString(c.getColumnIndexOrThrow("forma_pagamento"));

                    Intent intent = new Intent(Home.this, AddGasto.class);
                    intent.putExtra("edit_mode", true);
                    intent.putExtra("gasto_id", id);
                    intent.putExtra("description", description);
                    intent.putExtra("value", value);
                    intent.putExtra("date", date);
                    intent.putExtra("category", category);
                    intent.putExtra("forma_pagamento", formaPagamento);
                    startActivity(intent);
                }
            });
        }
    }

    private String formatDateForDisplay(String dateStr) {
        if (dateStr == null) return "";
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date date = dbFormat.parse(dateStr);
            return displayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateStr; // fallback to original string
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
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