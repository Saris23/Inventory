package com.example.inventory;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.inventory.base.BaseActivity;
import com.example.inventory.base.InputUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class perfil extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private EditText etNombre, etEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        BaseActivity.setupMenu(btnMenu, this);
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        if (user == null) {
            Intent noUser = new Intent(perfil.this, login.class);
            startActivity(noUser);
            finish();
            return;
        }
        cargarDatosUsuario();
    }
    private void cargarDatosUsuario() {
        db.collection("usuarios")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        String email = documentSnapshot.getString("gmail");
                        etNombre.setText(nombre);
                        etEmail.setText(email);
                    } else {
                        Toast.makeText(this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        InputUtils.handleTouchOutsideEditText(this, ev);
        return super.dispatchTouchEvent(ev);
    }
}