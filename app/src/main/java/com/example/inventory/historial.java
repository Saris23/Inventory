package com.example.inventory;

import android.os.Bundle;
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
import com.example.inventory.base.HistorialAdapter;
import com.example.inventory.base.ventaDiaria;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class historial extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private RecyclerView recyclerHistorial;
    private HistorialAdapter adapter;
    private List<ventaDiaria> listaVentasDiarias = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Si te da problemas de visualización, puedes comentar esta línea
        setContentView(R.layout.activity_historial);

        ImageButton btnMenu = findViewById(R.id.btnMenu); // Asegúrate de tener este ID en tu toolbar_layout
        ButtonMenu.setupMenu(btnMenu, this);

        recyclerHistorial = findViewById(R.id.recyclerHistorial);
        recyclerHistorial.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistorialAdapter(listaVentasDiarias, this);
        recyclerHistorial.setAdapter(adapter);

        cargarHistorialVentas();
    }

    private void cargarHistorialVentas() {
        if (user == null) {
            // Manejar caso de usuario no logueado
            return;
        }

        db.collection("usuarios")
                .document(user.getUid())
                .collection("ventas")
                .orderBy("fecha", Query.Direction.DESCENDING) // Traer las más recientes primero
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Usamos LinkedHashMap para mantener el orden de inserción (días)
                        Map<String, Double> ventasAgrupadas = new LinkedHashMap<>();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Timestamp timestamp = document.getTimestamp("fecha");
                            if (timestamp != null) {
                                Date fecha = timestamp.toDate();
                                String diaKey = sdf.format(fecha); // Clave única por día
                                double totalVenta = document.getDouble("total");

                                // Agrupar y sumar
                                double totalActual = ventasAgrupadas.getOrDefault(diaKey, 0.0);
                                ventasAgrupadas.put(diaKey, totalActual + totalVenta);
                            }
                        }

                        // Convertir el mapa agrupado a nuestra lista para el adaptador
                        listaVentasDiarias.clear();
                        try {
                            for (Map.Entry<String, Double> entry : ventasAgrupadas.entrySet()) {
                                Date fechaDia = sdf.parse(entry.getKey());
                                listaVentasDiarias.add(new ventaDiaria(fechaDia, entry.getValue()));
                            }
                        } catch (Exception e) {
                            // Manejar error de parseo
                        }

                        // Notificar al adaptador
                        adapter.notifyDataSetChanged();

                    } else {
                        Toast.makeText(historial.this, "Error al cargar el historial.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private class BaseActivity {
    }
}