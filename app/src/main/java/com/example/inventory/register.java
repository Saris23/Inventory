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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class register extends AppCompatActivity {
    String[] arrdocument={"C.C", "C.E"};
    // Variable global para la conexion de fireBase
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
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
        spDoc.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,arrdocument));
        db = FirebaseFirestore.getInstance();

        Intent inicio = new Intent(register.this, login.class);
        // boton del link de iniciar sesion
        btnInicio.setOnClickListener(view ->{
            startActivity(inicio);
            finish();
        });

        btnRegistro.setOnClickListener(v -> {
            String nombre = txtNombre.getText().toString().trim();
            String documento = txtDocumento.getText().toString().trim();
            String email = txtCorreo.getText().toString().trim();
            String password = txtContra.getText().toString().trim();
            String confirmPassword = txtConfirma.getText().toString().trim();

            // Validaciones básicas
            if (nombre.isEmpty() || documento.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(register.this, "Debe llenar todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(register.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }
            // Verificar documento
            db.collection("usuarios")
                    .whereEqualTo("documento", documento)
                    .get()
                    .addOnCompleteListener(queryTask -> {
                        if (!queryTask.isSuccessful()) {
                            Toast.makeText(register.this, "Error al verificar documento", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!queryTask.getResult().isEmpty()) {
                            // documento ya existe
                            Toast.makeText(register.this, "El documento ya está registrado", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Documento libre -> crear en Firebase Auth
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(authTask -> {
                                    if (!authTask.isSuccessful()) {
                                        // error creando cuenta (correo duplicado, email inválido, etc.)
                                        Exception ex = authTask.getException();
                                        if (ex instanceof FirebaseAuthUserCollisionException) {
                                            Toast.makeText(register.this, "Este correo ya está registrado", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(register.this, "Error Auth: " + (ex != null ? ex.getMessage() : "unknown"), Toast.LENGTH_LONG).show();
                                        }
                                        return;
                                    }
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    if (firebaseUser == null) {
                                        Toast.makeText(register.this, "Error inesperado al crear usuario", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    // Crear mapa con datos del usuario
                                    Map<String, Object> usuario = new HashMap<>();
                                    usuario.put("documento", documento);
                                    usuario.put("nombre", nombre);
                                    usuario.put("gmail", email);
                                    usuario.put("uid", firebaseUser.getUid());
                                    // Guardar usuario, exceptuando la contraseña
                                    db.collection("usuarios")
                                            .document(firebaseUser.getUid())
                                            .set(usuario)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(register.this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                                                startActivity(inicio);
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(register.this, "Error al guardar usuario", Toast.LENGTH_SHORT).show();
                                                // eliminar usuario de Auth si Firestore falla (para evitar cuentas huérfanas)
                                                firebaseUser.delete();

                                            });
                                });
                    });
        });

    }
}