package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.exceptions.DataInvalidaException;
import br.ufal.ic.p2.wepayu.exceptions.HorasDevemSerPositivasException;
import java.io.Serializable;
import java.time.LocalDate;

public class CartaoPonto implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate data;
    private double horas;

    public CartaoPonto(LocalDate data, double horas) throws Exception {
        if (data == null) {
            throw new DataInvalidaException();
        }
        if (horas <= 0) {
            throw new HorasDevemSerPositivasException();
        }
        this.data = data;
        this.horas = horas;
    }

    public LocalDate getData() {
        return data;
    }

    public double getHoras() {
        return horas;
    }

    public double getHorasNormais() {
        return Math.min(horas, 8.0);
    }

    public double getHorasExtras() {
        return Math.max(0.0, horas - 8.0);
    }
}
