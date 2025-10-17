package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.inventory.base.ButtonMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class home extends AppCompatActivity {
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView txtSaludo = findViewById(R.id.txtSaludo);
        ButtonMenu.setupMenu(this, R.id.itHome);
        if (user != null) {
            // Obtener el nombre desde Firestore
            db.collection("usuarios")
                    .document(user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String nombre = document.getString("nombre");
                                txtSaludo.setText("Bienvenido, "+nombre);
                            }
                        } else {
                            txtSaludo.setText("Bienvenido");
                        }
                    });
        } else {
            Intent noUser = new Intent(home.this, login.class);
            startActivity(noUser);
            finish();
        }
    }
}