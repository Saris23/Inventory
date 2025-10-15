package com.example.inventory.base;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class FormateadorDinero {

    /**
     * Convierte un valor double a un String con formato de dinero.
     * Ejemplo: 14000.0 se convierte a "$ 14.000"
     * Ejemplo: 1250.5 se convierte a "$ 1.250,5"
     * @param valor El número a formatear.
     * @return El String formateado.
     */
    public static String formatear(double valor) {
        // Símbolos para forzar '.' como separador de miles y ',' como decimal.
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        // El patrón #,##0.## agrupa los miles y solo muestra decimales si existen.
        DecimalFormat formatter = new DecimalFormat("#,##0.##", symbols);

        return "$ " + formatter.format(valor);
    }
}