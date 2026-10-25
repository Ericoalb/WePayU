package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.exceptions.DescricaoDeAgendaInvalidaException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class AgendaSemanal implements AgendaPagamento {
    private static final long serialVersionUID = 1L;

    private final int frequenciaSemanas;
    private final int diaDaSemana;
    private final String descricao;

    public AgendaSemanal(int diaDaSemana) throws DescricaoDeAgendaInvalidaException {
        this(1, diaDaSemana);
    }

    public AgendaSemanal(int frequenciaSemanas, int diaDaSemana) throws DescricaoDeAgendaInvalidaException {
        if (frequenciaSemanas < 1 || frequenciaSemanas > 52) {
            throw new DescricaoDeAgendaInvalidaException();
        }
        if (diaDaSemana < 1 || diaDaSemana > 7) {
            throw new DescricaoDeAgendaInvalidaException();
        }
        this.frequenciaSemanas = frequenciaSemanas;
        this.diaDaSemana = diaDaSemana;
        if (frequenciaSemanas == 1) {
            this.descricao = "semanal " + diaDaSemana;
        } else {
            this.descricao = "semanal " + frequenciaSemanas + " " + diaDaSemana;
        }
    }

    @Override
    public String getDescricao() {
        return descricao;
    }

    @Override
    public boolean ehDiaDePagamento(LocalDate data) {
        if (data == null) {
            return false;
        }
        if (data.getDayOfWeek().getValue() != diaDaSemana) {
            return false;
        }
        if (frequenciaSemanas == 1) {
            return true;
        }
        LocalDate primeiroDia = LocalDate.of(data.getYear(), 1, 1);
        while (primeiroDia.getDayOfWeek().getValue() != diaDaSemana) {
            primeiroDia = primeiroDia.plusDays(1);
        }
        long semanasDesdePrimeira = ChronoUnit.WEEKS.between(primeiroDia, data);
        long ocorrenciaNoAno = semanasDesdePrimeira + 1;
        return (ocorrenciaNoAno % frequenciaSemanas == 0);
    }

    @Override
    public LocalDate getInicioPeriodo(LocalDate data) {
        return data.minusDays(frequenciaSemanas * 7L - 1);
    }

    @Override
    public int getDiasTaxaSindical(LocalDate data) {
        return frequenciaSemanas * 7;
    }

    @Override
    public double calcularSalarioBase(double salarioMensal) {
        return Math.floor((salarioMensal * 12.0 * frequenciaSemanas / 52.0) * 100.0) / 100.0;
    }

    public int getFrequenciaSemanas() {
        return frequenciaSemanas;
    }

    public int getDiaDaSemana() {
        return diaDaSemana;
    }
}
