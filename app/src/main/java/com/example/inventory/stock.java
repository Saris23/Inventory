package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventory.base.ButtonMenu;
import com.example.inventory.base.InputUtils;
import com.example.inventory.base.Producto;
import com.example.inventory.base.ProductoAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class stock extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private List<Producto> listaProductos = new ArrayList<>();
    private ProductoAdapter adapter;
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
        ImageButton btnScaner = findViewById(R.id.btnScaner);
        ButtonMenu.setupMenu(btnMenu, this);
        EditText txtCodigo = findViewById(R.id.txtCodigo);
        EditText txtNProducto = findViewById(R.id.txtNProducto);
        EditText txtCantidad = findViewById(R.id.txtCantidad);
        EditText txtPrecio = findViewById(R.id.txtPrecio);
        RecyclerView rvLista = findViewById(R.id.recyclerProductos);
        // Instancias de firebase
        if (user == null) {
            Intent noUser = new Intent(stock.this, login.class);
            startActivity(noUser);
            finish();
            return;
        }
        rvLista.setLayoutManager(new LinearLayoutManager(this));
        // Llama al adapter
        adapter = new ProductoAdapter(this, listaProductos);
        rvLista.setAdapter(adapter);
        cargarProductos();
        // Llama al bottom dialog de editar
        adapter.setOnItemClickListener(producto -> mostrarDialog(producto));

        // Recibe el codigo que trae el intent del scaner
        String codBarra = getIntent().getStringExtra("codigobarra");
        txtCodigo.setText(codBarra);

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
                                txtCodigo.setText("");
                                txtNProducto.setText("");
                                txtCantidad.setText("");
                                txtPrecio.setText("");
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
        // Boton para el escaner
        btnScaner.setOnClickListener(v ->{
            try {
                Intent scan = new Intent(stock.this, Scanner.class);
                scan.putExtra("origen", "agregar");
                startActivity(scan);
                finish();
            } catch (Exception e) {
                // Mostrar el error en logcat
                Log.e("ScannerError", "Error al abrir el scanner", e);

                // Mostrar un mensaje al usuario
                Toast.makeText(stock.this, "No se pudo abrir el scanner", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void cargarProductos() {
        // Agarra el uid del user
        String userUid = user.getUid();
        db.collection("usuarios")
                .document(userUid)
                .collection("productos")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this,"Error al cargar los productos",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    listaProductos.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Producto p = doc.toObject(Producto.class);
                        listaProductos.add(p);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
    private void mostrarDialog(Producto producto) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_editar_producto, null);
        dialog.setContentView(view);

        EditText etNombre = view.findViewById(R.id.txtNombreEdit);
        EditText etCantidad = view.findViewById(R.id.txtCantidadEdit);
        EditText etPrecio = view.findViewById(R.id.txtPrecioEdit);
        Button btnEditar = view.findViewById(R.id.btnEditar);

        // Rellenar datos actuales del producto
        etNombre.setText(producto.getNombre());
        etCantidad.setText(String.valueOf(producto.getCantidad()));
        etPrecio.setText(String.valueOf(producto.getPrecio()));

        btnEditar.setOnClickListener(v -> {
            String nuevoNombre = etNombre.getText().toString().trim();
            int nuevaCantidad = Integer.parseInt(etCantidad.getText().toString());
            double nuevoPrecio = Double.parseDouble(etPrecio.getText().toString());

            // Actualizar Firestore
            db.collection("usuarios")
                    .document(user.getUid())
                    .collection("productos")
                    .document(producto.getCodigo())
                    .update("nombre", nuevoNombre,
                            "cantidad", nuevaCantidad,
                            "precio", nuevoPrecio)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show());
        });
        dialog.show();
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        InputUtils.handleTouchOutsideEditText(this, ev);
        return super.dispatchTouchEvent(ev);
    }
}