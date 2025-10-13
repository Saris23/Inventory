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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                return;
            }
            finalizarYGuardarVenta();
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

    private void finalizarYGuardarVenta() {
        // Referencia a la colección de ventas del usuario
        CollectionReference ventasRef = db.collection("usuarios")
                .document(user.getUid())
                .collection("ventas");

        // Prepara los datos para el nuevo documento de venta
        Map<String, Object> ventaData = new HashMap<>();
        ventaData.put("fecha", new Timestamp(new Date())); // Guarda la fecha y hora actual
        ventaData.put("total", total);

        // Convierte la lista de productos a un formato simple para Firestore
        List<Map<String, Object>> productosVendidos = new ArrayList<>();
        for (ProductoVenta p : listaVenta) {
            Map<String, Object> productoMap = new HashMap<>();
            productoMap.put("codigo", p.getCodigo());
            productoMap.put("nombre", p.getNombre());
            productoMap.put("cantidadVendida", p.getCantidad());
            productoMap.put("precioUnitario", p.getPrecio());
            productosVendidos.add(productoMap);
        }
        ventaData.put("productos", productosVendidos);

        // Usamos WriteBatch para actualizar el stock y guardar la venta en una sola operación
        WriteBatch batch = db.batch();

        // 1. Añadir la nueva venta al batch
        batch.set(ventasRef.document(), ventaData); // Crea un documento con ID automático

        // 2. Descontar el stock de cada producto en el batch
        for (ProductoVenta productoVendido : listaVenta) {
            // Referencia al documento del producto en el stock
            DocumentReference productoRef = db.collection("usuarios")
                    .document(user.getUid())
                    .collection("productos")
                    .document(productoVendido.getCodigo());

            // Descontamos la cantidad vendida. Usamos FieldValue para hacerlo de forma segura.
            batch.update(productoRef, "cantidad", FieldValue.increment(-productoVendido.getCantidad()));
        }

        // 3. Ejecutar el batch
        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(venta.this, "Venta finalizada y guardada por $" + String.format("%.2f", total), Toast.LENGTH_LONG).show();
            // Regresar al menú principal después de la venta exitosa
            Intent intent = new Intent(venta.this, MainActivity.class);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(venta.this, "Error al guardar la venta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}

