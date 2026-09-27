package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.time.LocalDate;

public class ResultadoVenda implements Serializable {
    private static final long serialVersionUID = 1L;
    private LocalDate data;
    private double valor;

    public ResultadoVenda(LocalDate data, double valor) {
        this.data = data;
        this.valor = valor;
    }

    public LocalDate getData() {
        return data;
    }

    public double getValor() {
        return valor;
    }
}
