package com.example.gasteiapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FrmCadastro extends AppCompatActivity {
    private EditText editText;
    private ImageView btnVoltar;

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

        editText = findViewById(R.id.editNome);
        editText = findViewById(R.id.editEmailC);
        editText = findViewById(R.id.editSenhaC);
        btnVoltar = findViewById(R.id.btnVoltar);

        // Ativa rolagem horizontal
        editText.setHorizontallyScrolling(true);
        editText.setMovementMethod(new ScrollingMovementMethod());

        //Sai do cadastro e vai pra tela de login
        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FrmCadastro.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}