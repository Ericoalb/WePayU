package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.time.LocalDate;

public interface AgendaPagamento extends Serializable {
    String getDescricao();
    boolean ehDiaDePagamento(LocalDate data);
    LocalDate getInicioPeriodo(LocalDate data);
    int getDiasTaxaSindical(LocalDate data);
    double calcularSalarioBase(double salarioMensal);
}
