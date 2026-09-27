package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Empregado implements Serializable {
    private String id;
    private String nome;
    private String endereco;
    private String tipo;
    private boolean sindicalizado;
    private String idSindicato;
    private double taxaSindical;
    private List<TaxaServico> taxasServico = new ArrayList<>();

    public Empregado(String id, String nome, String endereco, String tipo) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
        this.sindicalizado = false;
        this.idSindicato = null;
        this.taxaSindical = 0.0;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public String getTipo() { return tipo; }
    public boolean isSindicalizado() { return sindicalizado; }
    public String getIdSindicato() { return idSindicato; }
    public double getTaxaSindical() { return taxaSindical; }
    public List<TaxaServico> getTaxasServico() { return taxasServico; }
    
    public void setNome(String nome) { this.nome = nome; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setSindicalizado(boolean sindicalizado) { this.sindicalizado = sindicalizado; }
    public void setIdSindicato(String idSindicato) { this.idSindicato = idSindicato; }
    public void setTaxaSindical(double taxaSindical) { this.taxaSindical = taxaSindical; }

    public void adicionarTaxaServico(TaxaServico taxa) {
        taxasServico.add(taxa);
    }
}
