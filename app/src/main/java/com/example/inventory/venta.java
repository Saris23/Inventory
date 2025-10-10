package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventory.base.AdapterVenta;
import com.example.inventory.base.ProductoVenta;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class venta extends AppCompatActivity {
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private AdapterVenta adapter;
    private List<ProductoVenta> listaVenta = new ArrayList<>();
    private TextView txtTotal;
    private double total = 0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_venta);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        RecyclerView recyclerVenta = findViewById(R.id.recyclerVenta);
        ImageButton btnRegreso = findViewById(R.id.btnRegreso);
        Button btnScan = findViewById(R.id.btnScanVenta);
        Button btnFinalizar = findViewById(R.id.btnFinalizarV);
        txtTotal = findViewById(R.id.etTotalVenta);
        // Verifica la sesion
        if (user == null) {
            Intent noUser = new Intent(venta.this, login.class);
            startActivity(noUser);
            finish();
            return;
        }
        recyclerVenta.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdapterVenta(listaVenta, nuevoTotal -> {
            total = nuevoTotal;
            txtTotal.setText(String.format("Total: $%.2f", total));
        });
        recyclerVenta.setAdapter(adapter);

        // Busca el codigo que trae el escaner
        String codigo = getIntent().getStringExtra("codigobarra");
        if (codigo != null) {
            buscarProductoFirestore(codigo);
        }
        // Boton de finalizar
        btnFinalizar.setOnClickListener(v -> {
            if (listaVenta.isEmpty()) {
                Toast.makeText(this, "No hay productos en la venta", Toast.LENGTH_SHORT).show();
            } else {
                // Aqui se debe realizar los cambios en firestore
                Toast.makeText(this, "Venta finalizada por $" + total, Toast.LENGTH_SHORT).show();
            }
        });
        // Boton de regreso
        btnRegreso.setOnClickListener(view ->{
            Intent venta = new Intent(venta.this, MainActivity.class);
            startActivity(venta);
            finish();
        });
        // Boton al escaner
        btnScan.setOnClickListener(view ->{
            Intent venta = new Intent(venta.this, Scanner.class);
            venta.putExtra("origen", "vender");
            startActivity(venta);
            finish();
        });
    }
    private void buscarProductoFirestore(String codigo) {
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("usuarios")
                .document(user.getUid())
                .collection("productos")
                .document(codigo)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        double precio = documentSnapshot.getDouble("precio");

                        // Verificar si ya está en la lista
                        boolean existe = false;
                        for (ProductoVenta p : listaVenta) {
                            if (p.getCodigo().equals(codigo)) {
                                p.setCantidad(p.getCantidad() + 1);
                                existe = true;
                                break;
                            }
                        }

                        if (!existe) {
                            ProductoVenta producto = new ProductoVenta(codigo, nombre, precio, 1);
                            listaVenta.add(producto);
                        }

                        adapter.notifyDataSetChanged();
                        recalcularTotal();

                    } else {
                        Toast.makeText(this, "El producto no existe en tu inventario", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al buscar producto", Toast.LENGTH_SHORT).show());
    }
    private void recalcularTotal() {
        double nuevoTotal = 0;
        for (ProductoVenta p : listaVenta) {
            nuevoTotal += p.getSubtotal();
        }
        total = nuevoTotal;
        txtTotal.setText(String.format("Total: $%.2f", total));
    }
}