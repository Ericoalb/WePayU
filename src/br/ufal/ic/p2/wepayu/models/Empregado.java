package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.exceptions.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public abstract class Empregado implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String nome;
    private String endereco;
    private String tipo;
    private FiliacaoSindical filiacaoSindical = new FiliacaoSindical();
    private MetodoPagamento metodoPagamento = new MetodoPagamentoEmMaos();
    private AgendaPagamento agendaPagamento;

    public Empregado(String id, String nome, String endereco, String tipo) throws Exception {
        if (nome == null || nome.trim().isEmpty()) {
            throw new NomeNaoPodeSerNuloException();
        }
        if (endereco == null || endereco.trim().isEmpty()) {
            throw new EnderecoNaoPodeSerNuloException();
        }
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new TipoInvalidoException();
        }
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public String getTipo() {
        return tipo;
    }

    public void setNome(String nome) throws Exception {
        if (nome == null || nome.trim().isEmpty()) {
            throw new NomeNaoPodeSerNuloException();
        }
        this.nome = nome;
    }

    public void setEndereco(String endereco) throws Exception {
        if (endereco == null || endereco.trim().isEmpty()) {
            throw new EnderecoNaoPodeSerNuloException();
        }
        this.endereco = endereco;
    }

    public FiliacaoSindical getFiliacaoSindical() {
        return filiacaoSindical;
    }

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(MetodoPagamento metodoPagamento) throws Exception {
        if (metodoPagamento == null) {
            throw new MetodoPagamentoInvalidoException();
        }
        this.metodoPagamento = metodoPagamento;
    }

    public AgendaPagamento getAgendaPagamento() {
        return agendaPagamento;
    }

    public void setAgendaPagamento(AgendaPagamento agendaPagamento) throws Exception {
        if (agendaPagamento == null) {
            throw new AgendaNaoDisponivelException();
        }
        this.agendaPagamento = agendaPagamento;
    }

    public boolean isSindicalizado() {
        return filiacaoSindical.isSindicalizado();
    }

    public String getIdSindicato() {
        return filiacaoSindical.getIdSindicato();
    }

    public double getTaxaSindical() {
        return filiacaoSindical.getTaxaSindical();
    }

    public double getDebitoSindicato() {
        return filiacaoSindical.getDebitoSindicato();
    }

    public void setDebitoSindicato(double debitoSindicato) {
        filiacaoSindical.setDebitoSindicato(debitoSindicato);
    }

    public List<TaxaServico> getTaxasServico() {
        return filiacaoSindical.getTaxasServico();
    }

    public void adicionarTaxaServico(TaxaServico taxa) {
        filiacaoSindical.adicionarTaxaServico(taxa);
    }

    public void filiarSindicato(String idSindicato, double taxaSindical) throws Exception {
        filiacaoSindical.filiar(idSindicato, taxaSindical);
    }

    public void desfiliarSindicato() {
        filiacaoSindical.desfiliar();
    }

    public abstract double getSalario();
    public abstract void setSalario(double salario) throws Exception;

    public double getComissao() throws EmpregadoNaoEhComissionadoException {
        throw new EmpregadoNaoEhComissionadoException();
    }

    public void setComissao(double comissao) throws Exception {
        throw new EmpregadoNaoEhComissionadoException();
    }

    public void adicionarCartao(CartaoPonto cartao) throws EmpregadoNaoEhHoristaException {
        throw new EmpregadoNaoEhHoristaException();
    }

    public List<CartaoPonto> getCartoes() {
        return Collections.emptyList();
    }

    public double getHorasNormaisTrabalhadas(LocalDate inicio, LocalDate fim) throws EmpregadoNaoEhHoristaException {
        throw new EmpregadoNaoEhHoristaException();
    }

    public double getHorasExtrasTrabalhadas(LocalDate inicio, LocalDate fim) throws EmpregadoNaoEhHoristaException {
        throw new EmpregadoNaoEhHoristaException();
    }

    public void adicionarVenda(ResultadoVenda venda) throws EmpregadoNaoEhComissionadoException {
        throw new EmpregadoNaoEhComissionadoException();
    }

    public List<ResultadoVenda> getVendas() {
        return Collections.emptyList();
    }

    public double getVendasRealizadas(LocalDate inicio, LocalDate fim) throws EmpregadoNaoEhComissionadoException {
        throw new EmpregadoNaoEhComissionadoException();
    }

    public abstract DadosPagamento processarFolha(LocalDate data, boolean confirmarAlteracoes);

    public void copiarDadosDe(Empregado origem) {
        try {
            this.setMetodoPagamento(origem.getMetodoPagamento());
        } catch (Exception ignored) {
        }
        if (origem.isSindicalizado()) {
            try {
                this.filiarSindicato(origem.getIdSindicato(), origem.getTaxaSindical());
                this.setDebitoSindicato(origem.getDebitoSindicato());
                for (TaxaServico taxa : origem.getTaxasServico()) {
                    this.adicionarTaxaServico(taxa);
                }
            } catch (Exception ignored) {
            }
        }
        for (CartaoPonto cartao : origem.getCartoes()) {
            try {
                this.adicionarCartao(cartao);
            } catch (Exception ignored) {
            }
        }
        for (ResultadoVenda venda : origem.getVendas()) {
            try {
                this.adicionarVenda(venda);
            } catch (Exception ignored) {
            }
        }
    }
}
