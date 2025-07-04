package com.example.gasteiapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Activity de Cadastro, responsável pelo registro de novos usuários.
public class FrmCadastro extends AppCompatActivity {
    // Componentes da UI.
    private EditText editNome, editEmail, editSenha;
    private AppCompatButton btnCadastrar;
    private ImageView btnVoltar;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_frm_cadastro);
        // Ajusta o padding da tela para acomodar as barras do sistema (status bar, navigation bar).
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa o DatabaseHelper.
        dbHelper = new DatabaseHelper(this);

        // Referencia os componentes da UI.
        editNome = findViewById(R.id.editNome);
        editEmail = findViewById(R.id.editEmailC);
        editSenha = findViewById(R.id.editSenhaC);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnVoltar = findViewById(R.id.btnVoltar);

        // Permite a rolagem horizontal do campo de nome se o texto for muito longo.
        editNome.setHorizontallyScrolling(true);
        editNome.setMovementMethod(new ScrollingMovementMethod());

        // Listener para o botão de cadastrar.
        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editEmail.getText().toString();
                String password = editSenha.getText().toString();

                // Validação simples dos campos.
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(FrmCadastro.this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Verifica se o e-mail já está cadastrado.
                if (dbHelper.checkUser(email)) {
                    Toast.makeText(FrmCadastro.this, "Este e-mail já está cadastrado.", Toast.LENGTH_SHORT).show();
                } else {
                    // Tenta adicionar o usuário ao banco de dados.
                    boolean success = dbHelper.addUser(email, password);
                    if (success) {
                        Toast.makeText(FrmCadastro.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                        finish(); // Finaliza a Activity de cadastro após o sucesso.
                    } else {
                        Toast.makeText(FrmCadastro.this, "Erro ao realizar o cadastro.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Listener para o botão de voltar, que finaliza a Activity e retorna à tela anterior (login).
        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
