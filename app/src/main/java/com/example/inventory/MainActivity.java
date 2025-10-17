package com.example.inventory;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.inventory.base.ButtonMenu;
import com.example.inventory.base.FormateadorDinero;
import com.example.inventory.base.InputUtils;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView txtVenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnIniciarVenta = findViewById(R.id.btnIniciar);
        txtVenta = findViewById(R.id.txtVenta);
        ButtonMenu.setupMenu(this, R.id.itMain);

        if (user == null) {
            Intent noUser = new Intent(MainActivity.this, login.class);
            startActivity(noUser);
            finish();
            return;
        }
        cargarVentaDelDia();
        btnIniciarVenta.setOnClickListener(v ->{
            Intent venta = new Intent(MainActivity.this, com.example.inventory.venta.class);
            startActivity(venta);
        });
    }

    private void cargarVentaDelDia() {
        if (user == null) return;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date inicioDelDia = cal.getTime();

        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date finDelDia = cal.getTime();

        // 2. Crear la consulta a Firestore
        db.collection("usuarios")
                .document(user.getUid())
                .collection("ventas")
                .whereGreaterThanOrEqualTo("fecha", new Timestamp(inicioDelDia))
                .whereLessThan("fecha", new Timestamp(finDelDia))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        double totalVentaHoy = 0.0;
                        // 3. Recorrer los resultados y sumar los totales
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.contains("total")) {
                                totalVentaHoy += document.getDouble("total");
                            }
                        }
                        txtVenta.setText(FormateadorDinero.formatear(totalVentaHoy));
                    } else {
                        txtVenta.setText("$0.00");
                    }
                });
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        InputUtils.handleTouchOutsideEditText(this, ev);
        return super.dispatchTouchEvent(ev);
    }
}