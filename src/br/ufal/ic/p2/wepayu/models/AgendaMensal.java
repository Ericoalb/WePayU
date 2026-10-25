package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.exceptions.DescricaoDeAgendaInvalidaException;
import java.time.DayOfWeek;
import java.time.LocalDate;

public class AgendaMensal implements AgendaPagamento {
    private static final long serialVersionUID = 1L;

    private final boolean ultimoDiaUtil;
    private final int diaDoMes;
    private final String descricao;

    public AgendaMensal(String diaStr) throws DescricaoDeAgendaInvalidaException {
        if (diaStr == null || diaStr.trim().isEmpty()) {
            throw new DescricaoDeAgendaInvalidaException();
        }
        String diaTrim = diaStr.trim();
        if ("$".equals(diaTrim)) {
            this.ultimoDiaUtil = true;
            this.diaDoMes = -1;
            this.descricao = "mensal $";
        } else {
            try {
                int dia = Integer.parseInt(diaTrim);
                if (dia < 1 || dia > 28) {
                    throw new DescricaoDeAgendaInvalidaException();
                }
                this.ultimoDiaUtil = false;
                this.diaDoMes = dia;
                this.descricao = "mensal " + dia;
            } catch (NumberFormatException e) {
                DescricaoDeAgendaInvalidaException erro = new DescricaoDeAgendaInvalidaException();
                erro.initCause(e);
                throw erro;
            }
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
        if (ultimoDiaUtil) {
            LocalDate ultimoDia = data.withDayOfMonth(data.lengthOfMonth());
            while (ultimoDia.getDayOfWeek() == DayOfWeek.SATURDAY || ultimoDia.getDayOfWeek() == DayOfWeek.SUNDAY) {
                ultimoDia = ultimoDia.minusDays(1);
            }
            return data.equals(ultimoDia);
        } else {
            return data.getDayOfMonth() == diaDoMes;
        }
    }

    @Override
    public LocalDate getInicioPeriodo(LocalDate data) {
        if (ultimoDiaUtil) {
            return data.withDayOfMonth(1);
        } else {
            return data.minusMonths(1);
        }
    }

    @Override
    public int getDiasTaxaSindical(LocalDate data) {
        if (ultimoDiaUtil) {
            return data.lengthOfMonth();
        } else {
            return data.minusMonths(1).lengthOfMonth();
        }
    }

    @Override
    public double calcularSalarioBase(double salarioMensal) {
        return salarioMensal;
    }

    public boolean isUltimoDiaUtil() {
        return ultimoDiaUtil;
    }

    public int getDiaDoMes() {
        return diaDoMes;
    }
}
