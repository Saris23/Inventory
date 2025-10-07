package com.example.inventory.base;

public class Producto {
    private String nombre;
    private String codigo;
    private int cantidad;
    private double precio;

    public Producto() {} // Necesario para Firestore

    public Producto(String nombre, String codigo, int cantidad, double precio) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    public String getNombre() { return nombre; }
    public String getCodigo() { return codigo; }
    public int getCantidad() { return cantidad; }
    public double getPrecio() { return precio; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public void setPrecio(double precio) { this.precio = precio; }
}
