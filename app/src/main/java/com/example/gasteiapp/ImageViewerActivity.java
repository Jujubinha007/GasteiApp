package com.example.gasteiapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.io.File;

// Activity para visualização de imagens em tela cheia.
public class ImageViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        // Inicializa e configura a Toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Habilita o botão de voltar.
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back); // Define o ícone do botão de voltar.
        }

        // Referencia o ImageView para exibir a imagem.
        ImageView imageView = findViewById(R.id.imageView);
        
        // Obtém o caminho da imagem da Intent.
        String imagePath = getIntent().getStringExtra("imagePath");
        if (imagePath != null) {
            File imageFile = new File(imagePath);
            // Verifica se o arquivo da imagem existe e o carrega.
            if (imageFile.exists()) {
                Uri imageUri = Uri.fromFile(imageFile);
                imageView.setImageURI(imageUri);
            } else {
                Toast.makeText(this, "Imagem não encontrada", Toast.LENGTH_SHORT).show();
                finish(); // Fecha a Activity se a imagem não for encontrada.
            }
        } else {
            Toast.makeText(this, "Caminho da imagem inválido", Toast.LENGTH_SHORT).show();
            finish(); // Fecha a Activity se o caminho da imagem for inválido.
        }
    }

    @Override
    // Lida com a seleção de itens no menu da Toolbar (ex: botão de voltar).
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Finaliza a Activity.
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 