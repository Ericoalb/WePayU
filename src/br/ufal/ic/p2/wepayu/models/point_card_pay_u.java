package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.time.LocalDate;

public class point_card_pay_u implements Serializable {
    private static final long serialVersionUID = 1L;
    private LocalDate data;
    private double horas;

    public point_card_pay_u(LocalDate data, double horas) {
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
