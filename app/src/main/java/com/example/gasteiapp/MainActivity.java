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

public class MainActivity extends AppCompatActivity {
    private EditText editEmail, editSenha;
    private TextView text_TelaCadastro;
    private AppCompatButton btnEntrar;
    private DatabaseHelper dbHelper;

    public static final String SHARED_PREFS = "shared_prefs";
    public static final String USER_ID_KEY = "user_id_key";
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        text_TelaCadastro = findViewById(R.id.txtTelaLogin);
        btnEntrar = findViewById(R.id.btnEntrar);
        sharedpreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        int loggedInUserId = sharedpreferences.getInt(USER_ID_KEY, -1);
        if (loggedInUserId != -1) {
            Intent i = new Intent(MainActivity.this, Home.class);
            startActivity(i);
            finish();
        }

        editEmail.setHorizontallyScrolling(true);
        editEmail.setMovementMethod(new ScrollingMovementMethod());

        text_TelaCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FrmCadastro.class);
                startActivity(intent);
            }
        });

        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editEmail.getText().toString();
                String password = editSenha.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean isValid = dbHelper.checkUser(email, password);
                if (isValid) {
                    int userId = dbHelper.getUserId(email);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putInt(USER_ID_KEY, userId);
                    editor.apply();

                    Intent intent = new Intent(MainActivity.this, Home.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "E-mail ou senha inválidos.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
