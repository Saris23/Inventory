package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class startpage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_startpage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button btnRegitrate = findViewById(R.id.btnRegistrate);
        Button btnIniciaSesion = findViewById(R.id.btnIniciaSesion);

        // boton al registro
        btnRegitrate.setOnClickListener(view ->{
            Intent startInt = new Intent(startpage.this, register.class);
            startActivity(startInt);
            finish();
        });

        // boton al inicio de sesion
        btnIniciaSesion.setOnClickListener(view ->{
            Intent startInt = new Intent(startpage.this, login.class);
            startActivity(startInt);
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Si ya esta logueado va directamente al Main
            Intent intent = new Intent(startpage.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}