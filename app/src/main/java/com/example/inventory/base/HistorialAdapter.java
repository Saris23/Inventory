package com.example.inventory.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventory.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder> {

    private List<ventaDiaria> ventasDiarias;
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'de' MMMM, yyyy", new Locale("es", "ES"));

    public HistorialAdapter(List<ventaDiaria> ventasDiarias, Context context) {
        this.ventasDiarias = ventasDiarias;
        this.context = context;
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_venta_diaria, parent, false);
        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        ventaDiaria venta = ventasDiarias.get(position);
        holder.txtFecha.setText(dateFormat.format(venta.getFecha()));
        holder.txtTotal.setText(String.format(Locale.US, "$%.2f", venta.getTotalVendido()));
    }

    @Override
    public int getItemCount() {
        return ventasDiarias.size();
    }

    public static class HistorialViewHolder extends RecyclerView.ViewHolder {
        TextView txtFecha;
        TextView txtTotal;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            txtFecha = itemView.findViewById(R.id.txtFechaHistorial);
            txtTotal = itemView.findViewById(R.id.txtTotalDiario);
        }
    }
}