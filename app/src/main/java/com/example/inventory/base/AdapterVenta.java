package com.example.inventory.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventory.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class AdapterVenta extends RecyclerView.Adapter<AdapterVenta.ViewHolder> {
    private List<ProductoVenta> lista;
    private OnTotalChangeListener listener;
    public interface OnTotalChangeListener {
        void onTotalChange(double nuevoTotal);
    }
    public AdapterVenta(List<ProductoVenta> lista, OnTotalChangeListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductoVenta producto = lista.get(position);
        holder.txtNombre.setText(producto.getNombre());
        holder.txtPrecio.setText("$" + producto.getPrecio());
        holder.txtCantidad.setText(String.valueOf(producto.getCantidad()));
        // Boton menos
        holder.btnMas.setOnClickListener(v -> {
            producto.setCantidad(producto.getCantidad() + 1);
            notifyItemChanged(position);
            actualizarTotal();
        });
        // Boton menos
        holder.btnMenos.setOnClickListener(v -> {
            if (producto.getCantidad() > 1) {
                producto.setCantidad(producto.getCantidad() - 1);
                notifyItemChanged(position);
                actualizarTotal();
            }
        });
        // Boton eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            lista.remove(position);
            notifyItemRemoved(position);
            actualizarTotal();
        });
    }
    private void actualizarTotal() {
        double total = 0;
        for (ProductoVenta p : lista) {
            total += p.getSubtotal();
        }
        listener.onTotalChange(total);
    }
    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtPrecio, txtCantidad;
        ImageButton btnMas, btnMenos, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreProd);
            txtPrecio = itemView.findViewById(R.id.txtPrecioProd);
            txtCantidad = itemView.findViewById(R.id.txtCantidadProd);
            btnMas = itemView.findViewById(R.id.btnMas);
            btnMenos = itemView.findViewById(R.id.btnMenos);
            btnEliminar = itemView.findViewById(R.id.btnDelete);
        }
    }
}

