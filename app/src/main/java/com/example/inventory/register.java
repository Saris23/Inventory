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

        // boton para registrar al usuario
        btnRegistro.setOnClickListener(view ->{
            String nombre = txtNombre.getText().toString().trim();
            String documento = txtDocumento.getText().toString().trim();
            String email = txtCorreo.getText().toString().trim();
            String password = txtContra.getText().toString().trim();
            String confirmPassword = txtConfirma.getText().toString().trim();

            // verifica que no hayan campos no esten vacios
            if (nombre.isEmpty() || documento.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(register.this, "Debe llenar todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            // verificar que las contraseñas coincidan
            if (!password.equals(confirmPassword)) {
                Toast.makeText(register.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }
            // Registro de usuario
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // aun no funciona (se busca guardar el usuario en el firestore ligado al mismo UID)
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("documento", documento);
                                userMap.put("nombre", nombre);
                                userMap.put("gmail", email);
                                //userMap.put("uid", user.getUid());

                                // Guarda el documento bajo la colección 'usuarios' con el UID como ID
                                db.collection("usuarios")
                                        .document(user.getUid())
                                        .set(userMap)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                            startActivity(inicio);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error al guardar usuario", Toast.LENGTH_SHORT).show();
                                        });
                            }

                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(this, "Este correo ya está registrado", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        });
    }
}