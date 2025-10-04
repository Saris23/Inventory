package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button btnIngresar = findViewById(R.id.btnIngresar);
        Button btnRegistro = findViewById(R.id.btnRegistro);
        EditText txtEmail = findViewById(R.id.txtCorreo);
        EditText txtPass = findViewById(R.id.txtContrasena);

        // Boton ingresar
        btnIngresar.setOnClickListener(view ->{
            String email = txtEmail.getText().toString().trim();
            String password = txtPass.getText().toString().trim();

            // verifica que los campos no esten vacios
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(login.this, "Debe llenar todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Autenticacion para ingresar
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(login.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(login.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(login.this, "Credenciales incorrectas", Toast.LENGTH_LONG).show();
                        }
                    });
        });

        btnRegistro.setOnClickListener(view ->{
            Intent intentR = new Intent(login.this, register.class);
            startActivity(intentR);
            finish();
        });
    }
}