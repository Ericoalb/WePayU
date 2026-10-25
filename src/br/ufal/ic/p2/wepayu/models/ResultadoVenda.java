package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.exceptions.DataInvalidaException;
import br.ufal.ic.p2.wepayu.exceptions.ValorDeveSerPositivoException;
import java.io.Serializable;
import java.time.LocalDate;

public class ResultadoVenda implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate data;
    private double valor;

    public ResultadoVenda(LocalDate data, double valor) throws Exception {
        if (data == null) {
            throw new DataInvalidaException();
        }
        if (valor <= 0) {
            throw new ValorDeveSerPositivoException();
        }
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
