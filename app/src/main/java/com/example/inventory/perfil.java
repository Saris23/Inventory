package com.example.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
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

import com.example.inventory.base.ButtonMenu;
import com.example.inventory.base.InputUtils;
import com.example.inventory.base.Producto;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class perfil extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private EditText etNombre, etEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        ButtonMenu.setupMenu(btnMenu, this);
        Button btnEmail = findViewById(R.id.btnCambiarEmail);
        Button btnContra = findViewById(R.id.btnCambiarContra);
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        if (user == null) {
            Intent noUser = new Intent(perfil.this, login.class);
            startActivity(noUser);
            finish();
            return;
        }
        cargarDatosUsuario();
        // Boton cambiar correo
        btnEmail.setOnClickListener(v -> {
            BottomSheetDialog dialog = new BottomSheetDialog(perfil.this);
            View view = LayoutInflater.from(perfil.this).inflate(R.layout.cambiar_correo, null);
            dialog.setContentView(view);
            EditText edtNuevoCorreo = view.findViewById(R.id.txtCorreoNuevo);
            EditText edtContrasenaActual = view.findViewById(R.id.txtContraActual);
            Button btnActualizarCorreo = view.findViewById(R.id.btnActCorreo);
            // Boton del bottom_dialog
            btnActualizarCorreo.setOnClickListener(btnView -> {
                String nuevoCorreo = edtNuevoCorreo.getText().toString().trim();
                String contrasena = edtContrasenaActual.getText().toString().trim();

                if (nuevoCorreo.isEmpty() || contrasena.isEmpty()) {
                    Toast.makeText(perfil.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (user.getEmail().equalsIgnoreCase(nuevoCorreo)){
                    Toast.makeText(perfil.this,"El correo nuevo no puede ser igual al actual",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (user != null && user.getEmail() != null) {
                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), contrasena);
                    // Reautenticar antes de cambiar el correo
                    user.reauthenticate(credential)
                            .addOnSuccessListener(aVoid -> {
                                // Crear una cuenta temporal con el nuevo correo
                                user.verifyBeforeUpdateEmail(nuevoCorreo)
                                        .addOnSuccessListener(a -> {
                                            Toast.makeText(perfil.this,
                                                    "Se envió un correo de verificación al nuevo correo.\nVerifícalo para aplicar el cambio.",
                                                    Toast.LENGTH_LONG).show();
                                            dialog.dismiss();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(perfil.this,
                                                    "Error al enviar verificación: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(perfil.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                            });
                }
            });
            dialog.show();
        });
        // Boton cambiar contraseña
        btnContra.setOnClickListener(v -> {
            BottomSheetDialog dialog = new BottomSheetDialog(perfil.this);
            View viewP = LayoutInflater.from(perfil.this).inflate(R.layout.cambiar_contrasena, null);
            dialog.setContentView(viewP);
            EditText etContraA = viewP.findViewById(R.id.txtContraActual);
            EditText etContraN = viewP.findViewById(R.id.txtContraNueva);
            Button btnCambiarC = viewP.findViewById(R.id.btnActContra);
            // Boton del bottom dialog
            btnCambiarC.setOnClickListener(btnView -> {
                String contraActual = etContraA.getText().toString().trim();
                String nuevaContra = etContraN.getText().toString().trim();
                if (contraActual.isEmpty() || nuevaContra.isEmpty()) {
                    Toast.makeText(perfil.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (nuevaContra.equals(contraActual)) {
                    Toast.makeText(perfil.this, "La nueva contraseña no puede ser igual a la actual", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Reautenticar al usuario antes de cambiar contraseña
                user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), contraActual))
                        .addOnCompleteListener(reAuthTask -> {
                            if (reAuthTask.isSuccessful()) {
                                user.updatePassword(nuevaContra)
                                        .addOnCompleteListener(updateTask -> {
                                            if (updateTask.isSuccessful()) {
                                                Toast.makeText(perfil.this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                                // Cerrar sesión y redirigir al login
                                                FirebaseAuth.getInstance().signOut();
                                                Intent intent = new Intent(perfil.this, login.class);
                                                // limpia la pila de actividades anteriores por si queda alguna abierta
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish(); // cerrar la actividad actual
                                            } else {
                                                Toast.makeText(perfil.this, "Error al actualizar: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(perfil.this, "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show();
                            }
                        });
            });

            dialog.show();
        });

    }
    private void cargarDatosUsuario() {
        db.collection("usuarios")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        String email = documentSnapshot.getString("gmail");
                        etNombre.setText(nombre);
                        etEmail.setText(email);
                    } else {
                        Toast.makeText(this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                });
    }
    private void dialogContra(Producto producto) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_editar_producto, null);
        dialog.setContentView(view);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        InputUtils.handleTouchOutsideEditText(this, ev);
        return super.dispatchTouchEvent(ev);
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (user != null) {
            user.reload().addOnSuccessListener(aVoid -> {
                if (user.isEmailVerified()) {
                    // Ya está verificado, entonces actualiza en Firestore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("usuarios")
                            .document(user.getUid())
                            .update("gmail", user.getEmail());
                }
            });
        }
    }
}