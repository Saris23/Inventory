package com.example.inventory.base;

public class ProductoVenta {
    private String codigo;
    private String nombre;
    private double precio;
    private int cantidad;

    public ProductoVenta(String codigo, String nombre, double precio, int cantidad) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    public  String getCodigo() {return codigo; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getSubtotal() {
        return precio * cantidad;
    }
}

