package com.example.inventory;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageButton btnMenu = findViewById(R.id.btnMenu);

        btnMenu.setOnClickListener(view ->{
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
            popupMenu.getMenuInflater().inflate(R.menu.menu,popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.itInicio) {
                    Intent inicio = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(inicio);
                    finish();
                    return true;

                } else if (id == R.id.itStock) {
                    Intent stock = new Intent(MainActivity.this, stock.class);
                    startActivity(stock);
                    finish();
                    return true;

                } else if (id == R.id.itPerfil) {
                    Intent perfil = new Intent(MainActivity.this, perfil.class);
                    startActivity(perfil);
                    finish();
                    return true;

                } else if (id == R.id.itHistorial) {
                    Intent history = new Intent(MainActivity.this, historial.class);
                    startActivity(history);
                    finish();
                    return true;

                } else if (id == R.id.itLogout) {
                    Intent logout = new Intent(MainActivity.this, startpage.class);
                    startActivity(logout);
                    finish();
                    return true;
                }

                return false;
            });
            popupMenu.show();
        });
    }
}