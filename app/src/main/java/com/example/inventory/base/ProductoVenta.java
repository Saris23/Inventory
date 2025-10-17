package com.example.inventory.base;

public class ProductoVenta {
    private String codigo;
    private String nombre;
    private double precio;
    private int cantidad;
    private int stock;

    public ProductoVenta(String codigo, String nombre, double precio, int cantidad, int stock) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.stock = stock;
    }

    public  String getCodigo() {return codigo; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getCantidad() { return cantidad; }
    public int getStock() { return stock; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getSubtotal() {
        return precio * cantidad;
    }
}

