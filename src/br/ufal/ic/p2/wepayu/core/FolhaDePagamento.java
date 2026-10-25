package br.ufal.ic.p2.wepayu.core;

import br.ufal.ic.p2.wepayu.models.DadosPagamento;
import br.ufal.ic.p2.wepayu.models.Empregado;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

public class FolhaDePagamento implements Serializable {
    private static final long serialVersionUID = 1L;

    public static class ResultadoFolha {
        private final String relatorio;
        private final double totalFolha;

        public ResultadoFolha(String relatorio, double totalFolha) {
            this.relatorio = relatorio;
            this.totalFolha = totalFolha;
        }

        public String getRelatorio() {
            return relatorio;
        }

        public double getTotalFolha() {
            return totalFolha;
        }
    }

    private static class TotaisCategoria {
        private double bruto = 0.0;
        private double descontos = 0.0;
        private double liquido = 0.0;
        private long horasNormais = 0;
        private long horasExtras = 0;
        private double fixo = 0.0;
        private double vendas = 0.0;
        private double comissao = 0.0;

        public double getBruto() {
            return bruto;
        }

        public double getDescontos() {
            return descontos;
        }

        public double getLiquido() {
            return liquido;
        }

        public long getHorasNormais() {
            return horasNormais;
        }

        public long getHorasExtras() {
            return horasExtras;
        }

        public double getFixo() {
            return fixo;
        }

        public double getVendas() {
            return vendas;
        }

        public double getComissao() {
            return comissao;
        }

        public void somarBruto(double valor) {
            this.bruto += valor;
        }

        public void somarDescontos(double valor) {
            this.descontos += valor;
        }

        public void somarLiquido(double valor) {
            this.liquido += valor;
        }

        public void somarHorasNormais(long horas) {
            this.horasNormais += horas;
        }

        public void somarHorasExtras(long horas) {
            this.horasExtras += horas;
        }

        public void somarFixo(double valor) {
            this.fixo += valor;
        }

        public void somarVendas(double valor) {
            this.vendas += valor;
        }

        public void somarComissao(double valor) {
            this.comissao += valor;
        }
    }

    public static String formatarMoeda(double valor) {
        return String.format(Locale.GERMAN, "%.2f", valor);
    }

    public static ResultadoFolha calcular(Map<String, Empregado> empregados, LocalDate data, boolean confirmarAlteracoes) {
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("FOLHA DE PAGAMENTO DO DIA ").append(data.toString()).append("\r\n");
        relatorio.append("====================================\r\n\r\n");

        TotaisCategoria totaisHoristas = processarHoristas(empregados.values(), data, confirmarAlteracoes, relatorio);
        TotaisCategoria totaisAssalariados = processarAssalariados(empregados.values(), data, confirmarAlteracoes, relatorio);
        TotaisCategoria totaisComissionados = processarComissionados(empregados.values(), data, confirmarAlteracoes, relatorio);

        double totalGeral = totaisHoristas.getBruto() + totaisAssalariados.getBruto() + totaisComissionados.getBruto();
        relatorio.append("TOTAL FOLHA: ").append(formatarMoeda(totalGeral)).append("\r\n");

        return new ResultadoFolha(relatorio.toString(), totalGeral);
    }

    private static TotaisCategoria processarHoristas(Collection<Empregado> todosEmpregados, LocalDate data, boolean confirmarAlteracoes, StringBuilder relatorio) {
        relatorio.append("===============================================================================================================================\r\n");
        relatorio.append("===================== HORISTAS ================================================================================================\r\n");
        relatorio.append("===============================================================================================================================\r\n");
        relatorio.append("Nome                                 Horas Extra Salario Bruto Descontos Salario Liquido Metodo\r\n");
        relatorio.append("==================================== ===== ===== ============= ========= =============== ======================================\r\n");

        List<Empregado> empregadosHoristas = new ArrayList<>();
        for (Empregado empregado : todosEmpregados) {
            if ("horista".equals(empregado.getTipo())) {
                empregadosHoristas.add(empregado);
            }
        }
        empregadosHoristas.sort(Comparator.comparing(Empregado::getNome));

        TotaisCategoria totais = new TotaisCategoria();
        for (Empregado empregado : empregadosHoristas) {
            DadosPagamento dados = empregado.processarFolha(data, confirmarAlteracoes);
            if (dados != null) {
                relatorio.append(dados.getLinhaRelatorio());
                totais.somarBruto(dados.getSalarioBruto());
                totais.somarDescontos(dados.getDescontos());
                totais.somarLiquido(dados.getSalarioLiquido());
                totais.somarHorasNormais(dados.getHorasNormais());
                totais.somarHorasExtras(dados.getHorasExtras());
            }
        }

        relatorio.append("\r\n");
        relatorio.append(String.format(Locale.GERMAN, "%-36s %5d %5d %13s %9s %15s\r\n\r\n",
                "TOTAL HORISTAS",
                totais.getHorasNormais(),
                totais.getHorasExtras(),
                formatarMoeda(totais.getBruto()),
                formatarMoeda(totais.getDescontos()),
                formatarMoeda(totais.getLiquido())));

        return totais;
    }

    private static TotaisCategoria processarAssalariados(Collection<Empregado> todosEmpregados, LocalDate data, boolean confirmarAlteracoes, StringBuilder relatorio) {
        relatorio.append("===============================================================================================================================\r\n");
        relatorio.append("===================== ASSALARIADOS ============================================================================================\r\n");
        relatorio.append("===============================================================================================================================\r\n");
        relatorio.append("Nome                                             Salario Bruto Descontos Salario Liquido Metodo\r\n");
        relatorio.append("================================================ ============= ========= =============== ======================================\r\n");

        List<Empregado> empregadosAssalariados = new ArrayList<>();
        for (Empregado empregado : todosEmpregados) {
            if ("assalariado".equals(empregado.getTipo())) {
                empregadosAssalariados.add(empregado);
            }
        }
        empregadosAssalariados.sort(Comparator.comparing(Empregado::getNome));

        TotaisCategoria totais = new TotaisCategoria();
        for (Empregado empregado : empregadosAssalariados) {
            DadosPagamento dados = empregado.processarFolha(data, confirmarAlteracoes);
            if (dados != null) {
                relatorio.append(dados.getLinhaRelatorio());
                totais.somarBruto(dados.getSalarioBruto());
                totais.somarDescontos(dados.getDescontos());
                totais.somarLiquido(dados.getSalarioLiquido());
            }
        }

        relatorio.append("\r\n");
        relatorio.append(String.format(Locale.GERMAN, "%-48s %13s %9s %15s\r\n\r\n",
                "TOTAL ASSALARIADOS",
                formatarMoeda(totais.getBruto()),
                formatarMoeda(totais.getDescontos()),
                formatarMoeda(totais.getLiquido())));

        return totais;
    }

    private static TotaisCategoria processarComissionados(Collection<Empregado> todosEmpregados, LocalDate data, boolean confirmarAlteracoes, StringBuilder relatorio) {
        relatorio.append("===============================================================================================================================\r\n");
        relatorio.append("===================== COMISSIONADOS ===========================================================================================\r\n");
        relatorio.append("===============================================================================================================================\r\n");
        relatorio.append("Nome                  Fixo     Vendas   Comissao Salario Bruto Descontos Salario Liquido Metodo\r\n");
        relatorio.append("===================== ======== ======== ======== ============= ========= =============== ======================================\r\n");

        List<Empregado> empregadosComissionados = new ArrayList<>();
        for (Empregado empregado : todosEmpregados) {
            if ("comissionado".equals(empregado.getTipo())) {
                empregadosComissionados.add(empregado);
            }
        }
        empregadosComissionados.sort(Comparator.comparing(Empregado::getNome));

        TotaisCategoria totais = new TotaisCategoria();
        for (Empregado empregado : empregadosComissionados) {
            DadosPagamento dados = empregado.processarFolha(data, confirmarAlteracoes);
            if (dados != null) {
                relatorio.append(dados.getLinhaRelatorio());
                totais.somarBruto(dados.getSalarioBruto());
                totais.somarDescontos(dados.getDescontos());
                totais.somarLiquido(dados.getSalarioLiquido());
                totais.somarFixo(dados.getFixo());
                totais.somarVendas(dados.getVendas());
                totais.somarComissao(dados.getComissao());
            }
        }

        relatorio.append("\r\n");
        relatorio.append(String.format(Locale.GERMAN, "%-21s %8s %8s %8s %13s %9s %15s\r\n\r\n",
                "TOTAL COMISSIONADOS",
                formatarMoeda(totais.getFixo()),
                formatarMoeda(totais.getVendas()),
                formatarMoeda(totais.getComissao()),
                formatarMoeda(totais.getBruto()),
                formatarMoeda(totais.getDescontos()),
                formatarMoeda(totais.getLiquido())));

        return totais;
    }
}
