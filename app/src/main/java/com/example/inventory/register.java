package com.example.inventory;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class register extends AppCompatActivity {

    String[] arrdocument={"C.C", "C.E"};
    // Variable global para la conexion de la fireBase firestore
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // se llaman los editext y los botones del xml
        Button btnInicio = findViewById(R.id.txtInicioS);
        Button btnRegistro = findViewById(R.id.btnRegistrar);
        EditText txtNombre = findViewById(R.id.txtName);
        EditText txtCorreo = findViewById(R.id.txtEmail);
        EditText txtContra = findViewById(R.id.txtPassword);
        EditText txtConfirma = findViewById(R.id.txtConfirmpass);
        EditText txtDocumento = findViewById(R.id.txtDocument);
        Spinner spDoc = findViewById(R.id.spDocument);
        // adapter para las opciones del spinner
        spDoc.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,arrdocument));

        // boton del link de iniciar sesion
        btnInicio.setOnClickListener(view ->{
            Intent inicio = new Intent(register.this, login.class);
            startActivity(inicio);
            finish();
        });

        // boton para registrar al usuario
        btnRegistro.setOnClickListener(view ->{
            String nombre = txtNombre.getText().toString().trim();
            String email = txtCorreo.getText().toString().trim();
            String password = txtContra.getText().toString().trim();
            String confirmPassword = txtConfirma.getText().toString().trim();

            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            }

            try {

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}