package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.core.FolhaDePagamento;
import br.ufal.ic.p2.wepayu.exceptions.SalarioDeveSerNaoNegativoException;
import java.time.LocalDate;
import java.util.Locale;

public class EmpregadoAssalariado extends Empregado {
    private static final long serialVersionUID = 1L;

    private double salarioMensal;

    public EmpregadoAssalariado(String id, String nome, String endereco, String tipo, double salarioMensal) throws Exception {
        super(id, nome, endereco, tipo);
        if (salarioMensal < 0) {
            throw new SalarioDeveSerNaoNegativoException();
        }
        this.salarioMensal = salarioMensal;
        setAgendaPagamento(new AgendaMensal("$"));
    }

    public EmpregadoAssalariado(String id, String nome, String endereco, double salarioMensal) throws Exception {
        this(id, nome, endereco, "assalariado", salarioMensal);
    }

    @Override
    public double getSalario() {
        return salarioMensal;
    }

    @Override
    public void setSalario(double salario) throws Exception {
        if (salario < 0) {
            throw new SalarioDeveSerNaoNegativoException();
        }
        this.salarioMensal = salario;
    }

    public double getSalarioMensal() {
        return salarioMensal;
    }

    public void setSalarioMensal(double salarioMensal) throws Exception {
        setSalario(salarioMensal);
    }

    @Override
    public DadosPagamento processarFolha(LocalDate data, boolean confirmarAlteracoes) {
        if (!getAgendaPagamento().ehDiaDePagamento(data)) {
            return null;
        }

        LocalDate inicioPeriodo = getAgendaPagamento().getInicioPeriodo(data);
        LocalDate fimPeriodoExclusivo = data.plusDays(1);
        int diasTaxaSindical = getAgendaPagamento().getDiasTaxaSindical(data);

        double salarioBruto = getAgendaPagamento().calcularSalarioBase(getSalario());
        double totalDescontos = 0.0;

        if (isSindicalizado()) {
            double taxaSindical = diasTaxaSindical * getTaxaSindical();
            double taxasServico = getFiliacaoSindical().getTaxasServicoNoPeriodo(inicioPeriodo, fimPeriodoExclusivo);
            totalDescontos = taxaSindical + taxasServico;
        }

        double salarioLiquido = salarioBruto - totalDescontos;

        String linha = String.format(Locale.GERMAN, "%-48s %13s %9s %15s %s\r\n",
                getNome(),
                FolhaDePagamento.formatarMoeda(salarioBruto),
                FolhaDePagamento.formatarMoeda(totalDescontos),
                FolhaDePagamento.formatarMoeda(salarioLiquido),
                getMetodoPagamento().getDescricaoFolha(getEndereco()));

        return new DadosPagamento(salarioBruto, totalDescontos, salarioLiquido,
                0, 0, 0.0, 0.0, 0.0, linha);
    }
}
