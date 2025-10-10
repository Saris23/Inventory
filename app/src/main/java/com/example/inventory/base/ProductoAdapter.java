package com.example.inventory.base;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ContentView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventory.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private List<Producto> listaProductos;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Context context;
    private OnItemClickListener listener;

    public ProductoAdapter(Context context, List<Producto> listaProductos) {
        this.listaProductos = listaProductos;
        this.context = context;
    }
    public interface OnItemClickListener {
        void onItemClick(Producto producto);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);
        holder.txtNombre.setText(producto.getNombre());
        holder.txtPrecio.setText("$" + producto.getPrecio());
        holder.txtCantidad.setText(String.valueOf(producto.getCantidad()));

        // Botón mas
        holder.btnMas.setOnClickListener(v -> {
            int nuevaCantidad = producto.getCantidad() + 1;
            producto.setCantidad(nuevaCantidad);
            holder.txtCantidad.setText(String.valueOf(nuevaCantidad));
            actualizarCantidadFirestore(producto);
        });
        // Botón menos
        holder.btnMenos.setOnClickListener(v -> {
            int nuevaCantidad = producto.getCantidad() > 0 ? producto.getCantidad() - 1 : 0;
            producto.setCantidad(nuevaCantidad);
            holder.txtCantidad.setText(String.valueOf(nuevaCantidad));
            actualizarCantidadFirestore(producto);
        });
        // Botón Eliminar
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("⚠️ Eliminar producto")
                    .setMessage("¿Estás seguro de eliminar \"" + producto.getNombre() + "\"?")
                    .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                        eliminarProductoDeFirestore(producto, position);
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();
        });
        // Al hacer clic en la tarjeta
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(producto);
            }
        });
    }
    private void actualizarCantidadFirestore(Producto producto) {
        db.collection("usuarios")
                .document(user.getUid())
                .collection("productos")
                .document(producto.getCodigo())
                .update("cantidad", producto.getCantidad());
    }
    private void eliminarProductoDeFirestore(Producto producto, int position) {
        db.collection("usuarios")
                .document(user.getUid())
                .collection("productos")
                .document(producto.getCodigo())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    listaProductos.remove(position);
                    notifyItemRemoved(position);
                    // se cambia de posicion en el adapter
                    notifyItemRangeChanged(position, listaProductos.size());
                    Toast.makeText(context,"Producto eliminado",Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context,"Error al eliminar el producto",Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtPrecio, txtCantidad;
        ImageButton btnMas, btnMenos, btnDelete;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreProd);
            txtPrecio = itemView.findViewById(R.id.txtPrecioProd);
            txtCantidad = itemView.findViewById(R.id.txtCantidadProd);
            btnMas = itemView.findViewById(R.id.btnMas);
            btnMenos = itemView.findViewById(R.id.btnMenos);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}