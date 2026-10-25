package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.core.FolhaDePagamento;
import br.ufal.ic.p2.wepayu.exceptions.SalarioDeveSerNaoNegativoException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class EmpregadoHorista extends Empregado {
    private static final long serialVersionUID = 1L;

    private double salarioHorario;
    private List<CartaoPonto> cartoes = new ArrayList<>();

    public EmpregadoHorista(String id, String nome, String endereco, double salarioHorario) throws Exception {
        super(id, nome, endereco, "horista");
        if (salarioHorario < 0) {
            throw new SalarioDeveSerNaoNegativoException();
        }
        this.salarioHorario = salarioHorario;
        setAgendaPagamento(new AgendaSemanal(1, 5));
    }

    @Override
    public double getSalario() {
        return salarioHorario;
    }

    @Override
    public void setSalario(double salario) throws Exception {
        if (salario < 0) {
            throw new SalarioDeveSerNaoNegativoException();
        }
        this.salarioHorario = salario;
    }

    public double getSalarioHorario() {
        return salarioHorario;
    }

    public void setSalarioHorario(double salarioHorario) throws Exception {
        setSalario(salarioHorario);
    }

    @Override
    public void adicionarCartao(CartaoPonto cartao) {
        if (cartao == null) {
            throw new IllegalArgumentException("Cartao de ponto nao pode ser nulo.");
        }
        cartoes.add(cartao);
    }

    @Override
    public List<CartaoPonto> getCartoes() {
        return Collections.unmodifiableList(cartoes);
    }

    @Override
    public double getHorasNormaisTrabalhadas(LocalDate inicio, LocalDate fim) {
        double total = 0.0;
        for (CartaoPonto cartao : cartoes) {
            if (!cartao.getData().isBefore(inicio) && cartao.getData().isBefore(fim)) {
                total += cartao.getHorasNormais();
            }
        }
        return total;
    }

    @Override
    public double getHorasExtrasTrabalhadas(LocalDate inicio, LocalDate fim) {
        double total = 0.0;
        for (CartaoPonto cartao : cartoes) {
            if (!cartao.getData().isBefore(inicio) && cartao.getData().isBefore(fim)) {
                total += cartao.getHorasExtras();
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

        double horasNormais = getHorasNormaisTrabalhadas(inicioPeriodo, fimPeriodoExclusivo);
        double horasExtras = getHorasExtrasTrabalhadas(inicioPeriodo, fimPeriodoExclusivo);

        double salarioHora = getSalario();
        double salarioBruto = horasNormais * salarioHora + horasExtras * salarioHora * 1.5;
        double totalDescontos = 0.0;
        double salarioLiquido = 0.0;

        if (!getCartoes().isEmpty()) {
            if (isSindicalizado()) {
                double taxaPeriodo = diasTaxaSindical * getTaxaSindical();
                double debitoAnterior = getDebitoSindicato();
                double taxaSindicalTotal = debitoAnterior + taxaPeriodo;
                double taxasServico = getFiliacaoSindical().getTaxasServicoNoPeriodo(inicioPeriodo, fimPeriodoExclusivo);

                double totalTaxas = taxaSindicalTotal + taxasServico;
                totalDescontos = Math.min(salarioBruto, totalTaxas);
                salarioLiquido = salarioBruto - totalDescontos;
                if (confirmarAlteracoes) {
                    setDebitoSindicato(totalTaxas - totalDescontos);
                }
            } else {
                totalDescontos = 0.0;
                salarioLiquido = salarioBruto;
            }
        }

        String linha = String.format(Locale.GERMAN, "%-36s %5d %5d %13s %9s %15s %s\r\n",
                getNome(),
                (long) horasNormais,
                (long) horasExtras,
                FolhaDePagamento.formatarMoeda(salarioBruto),
                FolhaDePagamento.formatarMoeda(totalDescontos),
                FolhaDePagamento.formatarMoeda(salarioLiquido),
                getMetodoPagamento().getDescricaoFolha(getEndereco()));

        return new DadosPagamento(salarioBruto, totalDescontos, salarioLiquido,
                (long) horasNormais, (long) horasExtras,
                0.0, 0.0, 0.0, linha);
    }
}
