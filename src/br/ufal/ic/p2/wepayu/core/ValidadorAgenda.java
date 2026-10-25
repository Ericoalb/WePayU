package br.ufal.ic.p2.wepayu.core;

import br.ufal.ic.p2.wepayu.exceptions.DescricaoDeAgendaInvalidaException;
import br.ufal.ic.p2.wepayu.models.AgendaMensal;
import br.ufal.ic.p2.wepayu.models.AgendaPagamento;
import br.ufal.ic.p2.wepayu.models.AgendaSemanal;

public class ValidadorAgenda {
    public static AgendaPagamento validarECriar(String descricao) throws DescricaoDeAgendaInvalidaException {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new DescricaoDeAgendaInvalidaException();
        }
        String[] partes = descricao.trim().split("\\s+");
        if (partes.length < 2 || partes.length > 3) {
            throw new DescricaoDeAgendaInvalidaException();
        }

        String tipo = partes[0];
        if ("semanal".equals(tipo)) {
            if (partes.length == 2) {
                try {
                    int dia = Integer.parseInt(partes[1]);
                    return new AgendaSemanal(1, dia);
                } catch (NumberFormatException e) {
                    DescricaoDeAgendaInvalidaException erro = new DescricaoDeAgendaInvalidaException();
                    erro.initCause(e);
                    throw erro;
                }
            } else {
                try {
                    int semanas = Integer.parseInt(partes[1]);
                    int dia = Integer.parseInt(partes[2]);
                    return new AgendaSemanal(semanas, dia);
                } catch (NumberFormatException e) {
                    DescricaoDeAgendaInvalidaException erro = new DescricaoDeAgendaInvalidaException();
                    erro.initCause(e);
                    throw erro;
                }
            }
        } else if ("mensal".equals(tipo)) {
            if (partes.length == 2) {
                return new AgendaMensal(partes[1]);
            }
            throw new DescricaoDeAgendaInvalidaException();
        } else {
            throw new DescricaoDeAgendaInvalidaException();
        }
    }
}
