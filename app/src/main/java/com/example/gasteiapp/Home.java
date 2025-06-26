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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;

// Activity Home, responsável por exibir a lista de gastos do usuário.
public class Home extends AppCompatActivity {
    // Componentes da UI
    private RecyclerView recyclerView;
    private FloatingActionButton btnAdd;
    private Toolbar my_toolbar;
    private GastoAdapter gastoAdapter;
    private HomeViewModel homeViewModel;
    private Chip chipCurrentMonth, chipLastMonth, chipAll;
    private TextView tvGastosTitle;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        // Ajusta o padding da tela para acomodar as barras do sistema (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa a Toolbar
        my_toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(my_toolbar);

        // Obtém o ID do usuário logado das SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        userId = sharedPreferences.getInt(MainActivity.USER_ID_KEY, -1);

        // Configura o RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        gastoAdapter = new GastoAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(gastoAdapter);

        // Referencia os outros componentes da UI
        btnAdd = findViewById(R.id.btnAdd);
        chipCurrentMonth = findViewById(R.id.chip_current_month);
        chipLastMonth = findViewById(R.id.chip_last_month);
        chipAll = findViewById(R.id.chip_all);
        tvGastosTitle = findViewById(R.id.tvGastosTitle);

        // Inicializa o ViewModel e observa as mudanças na lista de gastos
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.getGastos().observe(this, gastos -> {
            gastoAdapter.setGastos(gastos);
            if (gastos == null || gastos.isEmpty()) {
                Toast.makeText(this, "Sem gastos para o filtro selecionado.", Toast.LENGTH_SHORT).show();
            }
        });

        // Configura os listeners para os chips de filtro
        chipCurrentMonth.setOnClickListener(v -> loadDataWithFilter("CURRENT_MONTH"));
        chipLastMonth.setOnClickListener(v -> loadDataWithFilter("LAST_MONTH"));
        chipAll.setOnClickListener(v -> loadDataWithFilter("ALL"));

        // Listener para o botão de adicionar novo gasto
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, AddGasto.class);
            startActivity(intent);
        });

        // Listener para cliques nos itens da lista de gastos (para edição)
        gastoAdapter.setOnItemClickListener(gasto -> {
            Intent intent = new Intent(Home.this, AddGasto.class);
            intent.putExtra("edit_mode", true);
            intent.putExtra("gasto", gasto);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Carrega os dados com o filtro "ALL" (todos os gastos) ao retomar a Activity
        loadDataWithFilter("ALL");
        chipAll.setChecked(true);
    }

    // Carrega os dados de gastos com base no filtro selecionado
    private void loadDataWithFilter(String filter) {
        // Atualiza o estado dos chips de filtro
        chipAll.setChecked("ALL".equals(filter));
        chipCurrentMonth.setChecked("CURRENT_MONTH".equals(filter));
        chipLastMonth.setChecked("LAST_MONTH".equals(filter));

        // Atualiza o título da seção de gastos com base no filtro
        if (tvGastosTitle != null) {
            if ("CURRENT_MONTH".equals(filter)) {
                tvGastosTitle.setText("Seus Gastos - Mês Atual");
            } else if ("LAST_MONTH".equals(filter)) {
                tvGastosTitle.setText("Seus Gastos - Mês Anterior");
            } else {
                tvGastosTitle.setText("Seus Gastos");
            }
        }

        // Carrega os gastos usando o ViewModel, se o userId for válido
        if (userId != -1) {
            homeViewModel.loadGastos(userId, filter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Infla o menu da Toolbar
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Lida com cliques nos itens do menu da Toolbar
        if (item.getItemId() == R.id.action_logout) {
            // Limpa as SharedPreferences e retorna para a tela de login
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
