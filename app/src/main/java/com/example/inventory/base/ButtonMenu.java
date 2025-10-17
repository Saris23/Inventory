package com.example.inventory.base;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inventory.MainActivity;
import com.example.inventory.R;
import com.example.inventory.historial;
import com.example.inventory.home;
import com.example.inventory.perfil;
import com.example.inventory.startpage;
import com.example.inventory.stock;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class ButtonMenu extends AppCompatActivity {
    public static void setupMenu(Activity activity, int selectedItemId) {
        BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bnv_bottom);
        if (bottomNavigationView == null) return;

        // Marca el ítem actual como seleccionado
        bottomNavigationView.setSelectedItemId(selectedItemId);

        // Listener para navegación
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.itHome && !(activity instanceof home)) {
                activity.startActivity(new Intent(activity, home.class));
                activity.overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.itStock && !(activity instanceof stock)) {
                activity.startActivity(new Intent(activity, stock.class));
                activity.overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.itMain && !(activity instanceof MainActivity)) {
                activity.startActivity(new Intent(activity, MainActivity.class));
                activity.overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.itHistorial && !(activity instanceof historial)) {
                activity.startActivity(new Intent(activity, historial.class));
                activity.overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.itPerfil && !(activity instanceof perfil)) {
                activity.startActivity(new Intent(activity, perfil.class));
                activity.overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }
}
