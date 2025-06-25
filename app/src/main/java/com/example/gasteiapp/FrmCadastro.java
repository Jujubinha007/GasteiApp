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

public class FrmCadastro extends AppCompatActivity {
    private EditText editNome, editEmail, editSenha;
    private AppCompatButton btnCadastrar;
    private ImageView btnVoltar;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_frm_cadastro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        editNome = findViewById(R.id.editNome);
        editEmail = findViewById(R.id.editEmailC);
        editSenha = findViewById(R.id.editSenhaC);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnVoltar = findViewById(R.id.btnVoltar);

        editNome.setHorizontallyScrolling(true);
        editNome.setMovementMethod(new ScrollingMovementMethod());

        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editEmail.getText().toString();
                String password = editSenha.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(FrmCadastro.this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (dbHelper.checkUser(email)) {
                    Toast.makeText(FrmCadastro.this, "Este e-mail já está cadastrado.", Toast.LENGTH_SHORT).show();
                } else {
                    boolean success = dbHelper.addUser(email, password);
                    if (success) {
                        Toast.makeText(FrmCadastro.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(FrmCadastro.this, "Erro ao realizar o cadastro.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //Sai do cadastro e vai pra tela de login
        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
