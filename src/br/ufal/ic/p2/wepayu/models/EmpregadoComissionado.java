package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.core.FolhaDePagamento;
import br.ufal.ic.p2.wepayu.exceptions.ComissaoDeveSerNaoNegativaException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class EmpregadoComissionado extends EmpregadoAssalariado {
    private static final long serialVersionUID = 1L;

    private double comissao;
    private List<ResultadoVenda> vendas = new ArrayList<>();

    public EmpregadoComissionado(String id, String nome, String endereco, double salarioMensal, double comissao) throws Exception {
        super(id, nome, endereco, "comissionado", salarioMensal);
        if (comissao < 0) {
            throw new ComissaoDeveSerNaoNegativaException();
        }
        this.comissao = comissao;
        setAgendaPagamento(new AgendaSemanal(2, 5));
    }

    @Override
    public double getComissao() {
        return comissao;
    }

    @Override
    public void setComissao(double comissao) throws Exception {
        if (comissao < 0) {
            throw new ComissaoDeveSerNaoNegativaException();
        }
        this.comissao = comissao;
    }

    @Override
    public void adicionarVenda(ResultadoVenda venda) {
        if (venda == null) {
            throw new IllegalArgumentException("Venda nao pode ser nula.");
        }
        vendas.add(venda);
    }

    @Override
    public List<ResultadoVenda> getVendas() {
        return Collections.unmodifiableList(vendas);
    }

    @Override
    public double getVendasRealizadas(LocalDate inicio, LocalDate fim) {
        double total = 0.0;
        for (ResultadoVenda venda : vendas) {
            if (!venda.getData().isBefore(inicio) && venda.getData().isBefore(fim)) {
                total += venda.getValor();
            }
        }
        return total;
    }

    @Override
    public DadosPagamento processarFolha(LocalDate data, boolean confirmarAlteracoes) {
        if (!getAgendaPagamento().ehDiaDePagamento(data)) {
            return null;
        }

        LocalDate inicioPeriodo = getAgendaPagamento().getInicioPeriodo(data);
        LocalDate fimPeriodoExclusivo = data.plusDays(1);
        int diasTaxaSindical = getAgendaPagamento().getDiasTaxaSindical(data);

        double fixo = getAgendaPagamento().calcularSalarioBase(getSalario());
        double totalVendasPeriodo = getVendasRealizadas(inicioPeriodo, fimPeriodoExclusivo);
        double taxaComissao = getComissao();
        double comissaoCalculada = Math.floor((totalVendasPeriodo * taxaComissao) * 100.0) / 100.0;
        double salarioBruto = fixo + comissaoCalculada;
        double totalDescontos = 0.0;

        if (isSindicalizado()) {
            double taxaSindical = diasTaxaSindical * getTaxaSindical();
            double taxasServico = getFiliacaoSindical().getTaxasServicoNoPeriodo(inicioPeriodo, fimPeriodoExclusivo);
            totalDescontos = taxaSindical + taxasServico;
        }

        double salarioLiquido = salarioBruto - totalDescontos;

        String linha = String.format(Locale.GERMAN, "%-21s %8s %8s %8s %13s %9s %15s %s\r\n",
                getNome(),
                FolhaDePagamento.formatarMoeda(fixo),
                FolhaDePagamento.formatarMoeda(totalVendasPeriodo),
                FolhaDePagamento.formatarMoeda(comissaoCalculada),
                FolhaDePagamento.formatarMoeda(salarioBruto),
                FolhaDePagamento.formatarMoeda(totalDescontos),
                FolhaDePagamento.formatarMoeda(salarioLiquido),
                getMetodoPagamento().getDescricaoFolha(getEndereco()));

        return new DadosPagamento(salarioBruto, totalDescontos, salarioLiquido,
                0, 0, fixo, totalVendasPeriodo, comissaoCalculada, linha);
    }
}
