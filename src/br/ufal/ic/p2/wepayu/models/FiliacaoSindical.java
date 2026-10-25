package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.exceptions.IdentificacaoSindicatoNulaException;
import br.ufal.ic.p2.wepayu.exceptions.TaxaSindicalDeveSerNaoNegativaException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FiliacaoSindical implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean sindicalizado = false;
    private String idSindicato;
    private double taxaSindical = 0.0;
    private List<TaxaServico> taxasServico = new ArrayList<>();
    private double debitoSindicato = 0.0;

    public boolean isSindicalizado() {
        return sindicalizado;
    }

    public String getIdSindicato() {
        return idSindicato;
    }

    public double getTaxaSindical() {
        return taxaSindical;
    }

    public double getDebitoSindicato() {
        return debitoSindicato;
    }

    public void setDebitoSindicato(double debitoSindicato) {
        if (debitoSindicato < 0) {
            throw new IllegalArgumentException("Debito sindical nao pode ser negativo.");
        }
        this.debitoSindicato = debitoSindicato;
    }

    public void adicionarTaxaServico(TaxaServico taxa) {
        if (taxa == null) {
            throw new IllegalArgumentException("Taxa de servico nao pode ser nula.");
        }
        taxasServico.add(taxa);
    }

    public List<TaxaServico> getTaxasServico() {
        return Collections.unmodifiableList(taxasServico);
    }

    public double getTaxasServicoNoPeriodo(LocalDate inicio, LocalDate fim) {
        double total = 0.0;
        for (TaxaServico taxa : taxasServico) {
            if (!taxa.getData().isBefore(inicio) && taxa.getData().isBefore(fim)) {
                total += taxa.getValor();
            }
        }
        return total;
    }

    public void filiar(String idSindicato, double taxaSindical) throws Exception {
        if (idSindicato == null || idSindicato.trim().isEmpty()) {
            throw new IdentificacaoSindicatoNulaException();
        }
        if (taxaSindical < 0) {
            throw new TaxaSindicalDeveSerNaoNegativaException();
        }
        this.sindicalizado = true;
        this.idSindicato = idSindicato;
        this.taxaSindical = taxaSindical;
    }

    public void desfiliar() {
        this.sindicalizado = false;
        this.idSindicato = null;
        this.taxaSindical = 0.0;
    }
}
