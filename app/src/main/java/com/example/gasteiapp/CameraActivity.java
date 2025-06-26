package com.example.gasteiapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Activity responsável por abrir a câmera e capturar fotos.
public class CameraActivity extends AppCompatActivity {

    // Permissões necessárias para a câmera.
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    private PreviewView viewFinder;
    private Button imageCaptureButton;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    // Launcher para solicitar permissões de câmera.
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera(); // Inicia a câmera se a permissão for concedida.
                } else {
                    Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                    finish(); // Fecha a Activity se a permissão for negada.
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Define que o layout não se ajusta às barras do sistema (status/navegação).
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Define a barra de status como transparente.
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_camera);

        // Inicializa os componentes da UI.
        viewFinder = findViewById(R.id.viewFinder);
        imageCaptureButton = findViewById(R.id.image_capture_button);

        // Verifica e solicita permissões de câmera.
        if (allPermissionsGranted()) {
            startCamera(); // Inicia a câmera se as permissões já estiverem concedidas.
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        // Configura o listener para o botão de captura de foto.
        imageCaptureButton.setOnClickListener(v -> takePhoto());
        // Inicializa o executor para operações da câmera.
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    // Tira uma foto e salva o arquivo.
    private void takePhoto() {
        // Retorna se o ImageCapture não estiver configurado.
        if (imageCapture == null) {
            return;
        }

        // Cria um nome de arquivo único para a imagem.
        SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        File file = new File(getBatchDirectoryName(), mDateFormat.format(new java.util.Date()) + ".jpg");

        // Configura as opções de saída para o arquivo de imagem.
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();

        // Captura a imagem e define callbacks para sucesso ou falha.
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            // Chamado quando a imagem é salva com sucesso.
            public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                Uri savedUri = Uri.fromFile(file);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("image_path", savedUri.toString());
                setResult(RESULT_OK, resultIntent); // Retorna o caminho da imagem para a Activity chamadora.
                finish(); // Fecha a CameraActivity.
            }

            @Override
            // Chamado se ocorrer um erro ao salvar a imagem.
            public void onError(ImageCaptureException exception) {
                Log.e("CameraActivity", "Photo capture failed: " + exception.getMessage(), exception);
            }
        });
    }

    // Retorna o diretório onde as imagens serão salvas.
    private String getBatchDirectoryName() {
        String app_folder_path = getExternalFilesDir(null).getAbsolutePath();
        File dir = new File(app_folder_path);
        return app_folder_path;
    }

    // Inicia a visualização da câmera e configura os casos de uso.
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                // Configura o caso de uso de pré-visualização.
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                // Configura o caso de uso de captura de imagem.
                imageCapture = new ImageCapture.Builder().build();

                // Seleciona a câmera traseira por padrão.
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll(); // Desvincula todos os casos de uso anteriores.
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture); // Vincula casos de uso ao ciclo de vida.
            } catch (Exception e) {
                Log.e("CameraActivity", "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // Verifica se todas as permissões necessárias foram concedidas.
    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    // Chamado quando a Activity é destruída, encerra o executor da câmera.
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
