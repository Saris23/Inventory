package com.example.inventory;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.inventory.base.AdapterVenta;
import com.example.inventory.base.FormateadorDinero;
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
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private AdapterVenta adapter;
    private final List<ProductoVenta> listaVenta = new ArrayList<>();
    private TextView txtTotal;
    private double total = 0.0;

    // <-- NUEVO: Declarar los componentes de pago y cambio
    private EditText txtPago;
    private TextView etCambio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venta);

        RecyclerView recyclerVenta = findViewById(R.id.recyclerVenta);
        ImageButton btnRegreso = findViewById(R.id.btnRegreso);
        Button btnScan = findViewById(R.id.btnScanVenta);
        Button btnFinalizar = findViewById(R.id.btnFinalizarV);
        txtTotal = findViewById(R.id.etTotalVenta);

        txtPago = findViewById(R.id.txtPago);
        etCambio = findViewById(R.id.etCambio);

        if (user == null) {
            Intent noUser = new Intent(venta.this, login.class);
            startActivity(noUser);
            finish();
            return;
        }
        recyclerVenta.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdapterVenta(listaVenta, nuevoTotal -> {
            total = nuevoTotal;
            txtTotal.setText("Total: " + FormateadorDinero.formatear(total));
            calcularCambio();
        });
        recyclerVenta.setAdapter(adapter);

        String codigo = getIntent().getStringExtra("codigobarra");
        if (codigo != null) {
            buscarProductoFirestore(codigo);
        }

        txtPago.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                calcularCambio();
            }
        });


        btnFinalizar.setOnClickListener(v -> {
            if (listaVenta.isEmpty()) {
                Toast.makeText(this, "No hay productos en la venta", Toast.LENGTH_SHORT).show();
                return;
            }

            String pagoStr = txtPago.getText().toString();
            if (pagoStr.isEmpty()) {
                Toast.makeText(this, "Ingresa con cuánto te pagan", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double pagoCliente = Double.parseDouble(pagoStr);
                if (pagoCliente < total) {
                    Toast.makeText(this, "El pago es insuficiente", Toast.LENGTH_SHORT).show();
                    return;
                }
                finalizarYGuardarVenta();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "El valor del pago no es válido", Toast.LENGTH_SHORT).show();
            }
        });
        btnRegreso.setOnClickListener(view ->{
            Intent venta = new Intent(venta.this, MainActivity.class);
            startActivity(venta);
            finish();
        });
        btnScan.setOnClickListener(view ->{
            Intent venta = new Intent(venta.this, Scanner.class);
            venta.putExtra("origen", "vender");
            startActivity(venta);
            finish();
        });
    }

    private void calcularCambio() {
        String pagoStr = txtPago.getText().toString();
        if (!pagoStr.isEmpty()) {
            try {
                double pagoCliente = Double.parseDouble(pagoStr);
                double cambio = pagoCliente - total;

                if (cambio >= 0) {
                    etCambio.setText("Cambio: " + FormateadorDinero.formatear(cambio));
                } else {
                    etCambio.setText("Faltan: " + FormateadorDinero.formatear(Math.abs(cambio)));
                }
            } catch (NumberFormatException e) {
                etCambio.setText("Cambio: $ 0");
            }
        } else {
            etCambio.setText("Cambio: $ 0");
        }
    }


    @SuppressLint("NotifyDataSetChanged")
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
                        long stockDisponible = documentSnapshot.getLong("cantidad");

                        if (stockDisponible <= 0) {
                            Toast.makeText(this, "Producto agotado. No hay stock.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ProductoVenta productoEnCarrito = null;
                        for (ProductoVenta p : listaVenta) {
                            if (p.getCodigo().equals(codigo)) {
                                productoEnCarrito = p;
                                break;
                            }
                        }

                        if (productoEnCarrito != null) {
                            if (productoEnCarrito.getCantidad() >= stockDisponible) {
                                Toast.makeText(this, "No puedes agregar más. Stock máximo alcanzado (" + stockDisponible + ")", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            productoEnCarrito.setCantidad(productoEnCarrito.getCantidad() + 1);
                        } else {
                            String nombre = documentSnapshot.getString("nombre");
                            double precio = documentSnapshot.getDouble("precio");
                            ProductoVenta productoNuevo = new ProductoVenta(codigo, nombre, precio, 1,(int) stockDisponible);
                            listaVenta.add(productoNuevo);
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
        txtTotal.setText("Total: " + FormateadorDinero.formatear(total));
        calcularCambio();
    }

    private void finalizarYGuardarVenta() {
        CollectionReference ventasRef = db.collection("usuarios")
                .document(user.getUid())
                .collection("ventas");

        Map<String, Object> ventaData = new HashMap<>();
        ventaData.put("fecha", new Timestamp(new Date()));
        ventaData.put("total", total);

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

        WriteBatch batch = db.batch();
        batch.set(ventasRef.document(), ventaData);

        for (ProductoVenta productoVendido : listaVenta) {
            DocumentReference productoRef = db.collection("usuarios")
                    .document(user.getUid())
                    .collection("productos")
                    .document(productoVendido.getCodigo());
            batch.update(productoRef, "cantidad", FieldValue.increment(-productoVendido.getCantidad()));
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(venta.this, "Venta finalizada y guardada por " + FormateadorDinero.formatear(total), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(venta.this, MainActivity.class);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(venta.this, "Error al guardar la venta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}

