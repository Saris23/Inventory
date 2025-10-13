package com.example.inventory.base;

import java.util.Date;

public class ventaDiaria {
    private Date fecha;
    private double totalVendido;

    public ventaDiaria(Date fecha, double totalVendido) {
        this.fecha = fecha;
        this.totalVendido = totalVendido;
    }

    public Date getFecha() {
        return fecha;
    }

    public double getTotalVendido() {
        return totalVendido;
    }
}
