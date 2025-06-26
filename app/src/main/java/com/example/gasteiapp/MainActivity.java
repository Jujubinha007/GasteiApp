package com.example.gasteiapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

// Activity principal, responsável pela tela de login do usuário.
public class MainActivity extends AppCompatActivity {
    // Componentes da UI
    private EditText editEmail, editSenha;
    private TextView text_TelaCadastro;
    private AppCompatButton btnEntrar;

    // ViewModel para a lógica de login
    private LoginViewModel loginViewModel;

    // Constantes para SharedPreferences
    public static final String SHARED_PREFS = "shared_prefs";
    public static final String USER_ID_KEY = "user_id_key";
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // Ajusta o padding da tela para acomodar as barras do sistema (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa o ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Referencia os componentes da UI
        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        text_TelaCadastro = findViewById(R.id.txtTelaLogin);
        btnEntrar = findViewById(R.id.btnEntrar);
        sharedpreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        // Verifica se o usuário já está logado
        int loggedInUserId = sharedpreferences.getInt(USER_ID_KEY, -1);
        if (loggedInUserId != -1) {
            // Se estiver logado, vai direto para a tela Home
            Intent i = new Intent(MainActivity.this, Home.class);
            startActivity(i);
            finish(); // Finaliza a MainActivity para não voltar para ela ao pressionar "back"
            return;
        }

        // Permite a rolagem horizontal do campo de e-mail se o texto for muito longo
        editEmail.setHorizontallyScrolling(true);
        editEmail.setMovementMethod(new ScrollingMovementMethod());

        // Listener para o texto de cadastro, que leva para a tela de cadastro
        text_TelaCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FrmCadastro.class);
                startActivity(intent);
            }
        });

        // Listener para o botão de entrar
        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editEmail.getText().toString();
                String password = editSenha.getText().toString();

                // Validação simples dos campos
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Chama o método de login no ViewModel
                loginViewModel.login(email, password);
            }
        });

        // Observa o resultado do login vindo do ViewModel
        loginViewModel.getLoginResult().observe(this, isValid -> {
            if (isValid) {
                // Se o login for válido, observa o ID do usuário
                loginViewModel.getUserId().observe(this, userId -> {
                    if (userId != null && userId != -1) {
                        // Salva o ID do usuário nas SharedPreferences
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putInt(USER_ID_KEY, userId);
                        editor.apply();

                        // Navega para a tela Home
                        Intent intent = new Intent(MainActivity.this, Home.class);
                        startActivity(intent);
                        finish(); // Finaliza a MainActivity
                    }
                });
            } else {
                // Se o login for inválido, exibe uma mensagem de erro
                Toast.makeText(MainActivity.this, "E-mail ou senha inválidos.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
