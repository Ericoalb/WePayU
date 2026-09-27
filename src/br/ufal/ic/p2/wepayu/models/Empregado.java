package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Empregado implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String nome;
    private String endereco;
    private String tipo;
    private boolean sindicalizado;
    private String idSindicato;
    private double taxaSindical;
    private List<TaxaServico> taxasServico = new ArrayList<>();
    private String metodoPagamento;
    private String banco;
    private String agencia;
    private String contaCorrente;
    private double debitoSindicato = 0.0;

    public Empregado(String id, String nome, String endereco, String tipo) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
        this.sindicalizado = false;
        this.idSindicato = null;
        this.taxaSindical = 0.0;
        this.metodoPagamento = "emMaos";
        this.banco = null;
        this.agencia = null;
        this.contaCorrente = null;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public String getTipo() { return tipo; }
    public boolean isSindicalizado() { return sindicalizado; }
    public String getIdSindicato() { return idSindicato; }
    public double getTaxaSindical() { return taxaSindical; }
    public List<TaxaServico> getTaxasServico() { return taxasServico; }
    public String getMetodoPagamento() { return metodoPagamento; }
    public String getBanco() { return banco; }
    public String getAgencia() { return agencia; }
    public String getContaCorrente() { return contaCorrente; }
    public double getDebitoSindicato() { return debitoSindicato; }
    
    public void setNome(String nome) { this.nome = nome; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setSindicalizado(boolean sindicalizado) { this.sindicalizado = sindicalizado; }
    public void setIdSindicato(String idSindicato) { this.idSindicato = idSindicato; }
    public void setTaxaSindical(double taxaSindical) { this.taxaSindical = taxaSindical; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }
    public void setBanco(String banco) { this.banco = banco; }
    public void setAgencia(String agencia) { this.agencia = agencia; }
    public void setContaCorrente(String contaCorrente) { this.contaCorrente = contaCorrente; }
    public void setDebitoSindicato(double debitoSindicato) { this.debitoSindicato = debitoSindicato; }

    public void adicionarTaxaServico(TaxaServico taxa) {
        taxasServico.add(taxa);
    }
}
