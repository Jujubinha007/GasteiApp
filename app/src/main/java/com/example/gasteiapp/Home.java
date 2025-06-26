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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class Home extends AppCompatActivity {
    RecyclerView recyclerView;
    FloatingActionButton btnAdd;
    Toolbar my_toolbar;
    DatabaseHelper dbHelper;
    GastoAdapter gastoAdapter;

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
        displayData();
    }

    private void displayData() {
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        int userId = sharedPreferences.getInt(MainActivity.USER_ID_KEY, -1);

        if (userId != -1) {
            Cursor cursor = dbHelper.getGastosByUser(userId);
            gastoAdapter = new GastoAdapter(this, cursor);
            recyclerView.setAdapter(gastoAdapter);
            if (cursor.getCount() == 0 ){
                Toast.makeText(this, "Sem gastos cadastrados.", Toast.LENGTH_SHORT).show();
            }else{
                while (cursor.moveToNext()){
                    spinnerCategoria.add(cursor.getString(0));
                    spinnerFmPagamento.add(cursor.getString(1));
                    date.add(cursor.getString(2));
                    value.add(cursor.getString(3));
                    descricao.add(cursor.getString(4));

                    displayData();
                }
            }
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