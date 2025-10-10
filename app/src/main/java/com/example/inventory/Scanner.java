package com.example.inventory;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Scanner extends AppCompatActivity {
    Button btnRegreso, btnresult;
    PreviewView previewView;
    TextView txtCode;
    ExecutorService cameraExecutor;
    BarcodeScanner barcodeScanner;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scanner);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtCode = findViewById(R.id.txtCode);
        previewView = findViewById(R.id.previewView);
        btnresult = findViewById(R.id.btnBarcode);
        btnRegreso = findViewById(R.id.btnBack);
        cameraExecutor = Executors.newSingleThreadExecutor();
        barcodeScanner = BarcodeScanning.getClient();

        // Para volver a la activity anterior
        String origen = getIntent().getStringExtra("origen");
        btnRegreso.setOnClickListener(v -> {
            Intent intent;
            if ("agregar".equals(origen)) {
                intent = new Intent(Scanner.this, stock.class);
                startActivity(intent);
                finish();
            } else if ("vender".equals(origen)) {
                intent = new Intent(Scanner.this, venta.class);
                startActivity(intent);
                finish();
            } else {
                // Por defecto, se manda a Main
                intent = new Intent(Scanner.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        startCamera();
                    } else {
                        new AlertDialog.Builder(Scanner.this)
                                .setTitle("Permiso requerido")
                                .setMessage("La aplicación necesita acceso a la cámara para poder escanear.")
                                .setPositiveButton("Conceder permiso", (dialog, which) -> {
                                    // vuelve a pedir el permiso
                                    Intent ajustes = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package",getPackageName(),null);
                                    ajustes.setData(uri);
                                    startActivity(ajustes);
                                })
                                .setNegativeButton("Cancelar", (dialog, which) -> {
                                    finishAffinity();
                                })
                                .show();
                    }
                });
        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        Size screenSize = new Size(1280, 720);

        ResolutionSelector resolutionSelector = new ResolutionSelector.Builder()
                .setResolutionStrategy(
                        new ResolutionStrategy(screenSize, ResolutionStrategy.FALLBACK_RULE_NONE)
                )
                .build();
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Preview
                Preview preview = new Preview.Builder()
                        .setResolutionSelector(resolutionSelector)
                        .build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // ImageAnalysis
                ImageAnalysis imageAnalyzer = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalyzer.setAnalyzer(cameraExecutor, imageProxy -> {
                    processImageProxy(imageProxy);
                });

                // Selector de cámara (trasera por defecto)
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Enlazar preview + análisis al ciclo de vida
                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    private void processImageProxy(ImageProxy imageProxy){
        @SuppressWarnings("UnsafeOptInUsageError")
        Image mediaImage = imageProxy.getImage();

        if(mediaImage != null){
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            barcodeScanner.process(image)
                    .addOnSuccessListener(barcodes ->{
                        for(Barcode barcode : barcodes){
                            handleBarcode(barcode);
                        }
                    })
                    .addOnFailureListener(e -> {
                        new AlertDialog.Builder(Scanner.this)
                                .setTitle("Aviso")
                                .setMessage("Falló al escanear el código")
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .show();
                    })
                    .addOnCompleteListener(task -> {
                        imageProxy.close();
                    });
        }
    }
    private void handleBarcode(Barcode barcode) {
        String codigo = null;
        if (barcode.getUrl() != null) {
            codigo = barcode.getUrl().getUrl();
        } else if (barcode.getDisplayValue() != null) {
            codigo = barcode.getDisplayValue();
        }
        if (codigo != null) {
            String finalCodigo = codigo;
            txtCode.setText(codigo);
            btnresult.setOnClickListener(v -> {
                String origen = getIntent().getStringExtra("origen");
                Intent intent;
                if ("agregar".equals(origen)) {
                    intent = new Intent(Scanner.this, stock.class);
                } else if ("vender".equals(origen)) {
                    intent = new Intent(Scanner.this, venta.class);
                } else {
                    // Por defecto va a main
                    intent = new Intent(Scanner.this, MainActivity.class);
                }
                intent.putExtra("codigobarra",finalCodigo);
                startActivity(intent);
                finish();
            });
        } else {
            Toast.makeText(Scanner.this,"No se detecta código de barras",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        // Cada vez que el usuario regresa a la app, se vuelve a comprobar el permiso
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // Ya tiene permiso → inicia la cámara
            startCamera();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}