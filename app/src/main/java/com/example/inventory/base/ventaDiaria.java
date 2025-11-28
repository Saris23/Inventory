package com.example.inventory.base;

import java.util.Date;

public class ventaDiaria {
    private Date fecha;
    private String vendendor;
    private double totalVendido;

    public ventaDiaria(Date fecha, String vendedor, double totalVendido) {
        this.fecha = fecha;
        this.vendendor = vendedor;
        this.totalVendido = totalVendido;
    }

    public Date getFecha() {
        return fecha;
    }
    public String getVendendor(){ return  vendendor; }
    public double getTotalVendido() {
        return totalVendido;
    }
}
