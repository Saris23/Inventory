package com.example.inventory.base;

import android.content.Intent;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inventory.MainActivity;
import com.example.inventory.R;
import com.example.inventory.historial;
import com.example.inventory.perfil;
import com.example.inventory.startpage;
import com.example.inventory.stock;

public class ButtonMenu extends AppCompatActivity {
    public static void setupMenu(ImageButton btnMenu, AppCompatActivity activity) {
        btnMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(activity, v);
            popupMenu.getMenuInflater().inflate(R.menu.menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.itInicio) {
                    activity.startActivity(new Intent(activity, MainActivity.class));
                    activity.finish();
                    return true;
                } else if (id == R.id.itStock) {
                    activity.startActivity(new Intent(activity, stock.class));
                    activity.finish();
                    return true;
                } else if (id == R.id.itPerfil) {
                    activity.startActivity(new Intent(activity, perfil.class));
                    activity.finish();
                    return true;
                } else if (id == R.id.itHistorial) {
                    activity.startActivity(new Intent(activity, historial.class));
                    activity.finish();
                    return true;
                } else if (id == R.id.itLogout) {
                    activity.startActivity(new Intent(activity, startpage.class));
                    activity.finish();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }
}
