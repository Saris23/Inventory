package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventory.base.ButtonMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class stock extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stock);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button btnAgregar = findViewById(R.id.btnAgregar);
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        ButtonMenu.setupMenu(btnMenu, this);
        EditText txtCodigo = findViewById(R.id.txtCodigo);
        EditText txtNProducto = findViewById(R.id.txtNProducto);
        EditText txtCantidad = findViewById(R.id.txtCantidad);
        EditText txtPrecio = findViewById(R.id.txtPrecio);
        RecyclerView rvLista = findViewById(R.id.recyclerProductos);
        // Instancias de firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Intent noUser = new Intent(stock.this, login.class);
            startActivity(noUser);
            finish();
            return;
        }
        btnAgregar.setOnClickListener(view ->{
            String codigo = txtCodigo.getText().toString().trim();
            String nombre = txtNProducto.getText().toString().trim();
            String cant = txtCantidad.getText().toString().trim();
            String precio = txtPrecio.getText().toString().trim();
            // Validacion
            if (codigo.isEmpty() || nombre .isEmpty() || cant.isEmpty() || precio.isEmpty()) {
                Toast.makeText(stock.this, "Debe llenar todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            // Agarra el uid del usuario
            String uid = user.getUid();
            // Referencia a la subcolección de productos del usuario
            CollectionReference productosRef = db.collection("usuarios")
                    .document(uid)
                    .collection("productos");
            // Verificar si ya existe un producto con el mismo código
            productosRef.whereEqualTo("codigo", codigo)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                // Ya existe un producto con ese código
                                Toast.makeText(stock.this, "El código ya está registrado", Toast.LENGTH_SHORT).show();
                            } else {
                                // Crear mapa con los datos del producto
                                Map<String, Object> producto = new HashMap<>();
                                producto.put("codigo", codigo);
                                producto.put("nombre", nombre);
                                producto.put("cantidad", Integer.parseInt(cant));
                                producto.put("precio", Double.parseDouble(precio));
                                // Guardar producto
                                productosRef.document(codigo).set(producto)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(stock.this, "Producto agregado con éxito", Toast.LENGTH_SHORT).show();
                                            // Limpia los campos
                                            txtCodigo.setText("");
                                            txtNProducto.setText("");
                                            txtCantidad.setText("");
                                            txtPrecio.setText("");
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(stock.this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(stock.this, "Error al verificar código", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

    }
}