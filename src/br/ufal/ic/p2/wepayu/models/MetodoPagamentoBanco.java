package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.exceptions.AgenciaNaoPodeSerNulaException;
import br.ufal.ic.p2.wepayu.exceptions.BancoNaoPodeSerNuloException;
import br.ufal.ic.p2.wepayu.exceptions.ContaCorrenteNaoPodeSerNulaException;

public class MetodoPagamentoBanco implements MetodoPagamento {
    private static final long serialVersionUID = 1L;

    private String banco;
    private String agencia;
    private String contaCorrente;

    public MetodoPagamentoBanco(String banco, String agencia, String contaCorrente) throws Exception {
        if (banco == null || banco.trim().isEmpty()) {
            throw new BancoNaoPodeSerNuloException();
        }
        if (agencia == null || agencia.trim().isEmpty()) {
            throw new AgenciaNaoPodeSerNulaException();
        }
        if (contaCorrente == null || contaCorrente.trim().isEmpty()) {
            throw new ContaCorrenteNaoPodeSerNulaException();
        }
        this.banco = banco;
        this.agencia = agencia;
        this.contaCorrente = contaCorrente;
    }

    @Override
    public String getTipo() {
        return "banco";
    }

    @Override
    public String getDescricaoFolha(String endereco) {
        return banco + ", Ag. " + agencia + " CC " + contaCorrente;
    }

    @Override
    public String getBanco() {
        return banco;
    }

    @Override
    public String getAgencia() {
        return agencia;
    }

    @Override
    public String getContaCorrente() {
        return contaCorrente;
    }
}
