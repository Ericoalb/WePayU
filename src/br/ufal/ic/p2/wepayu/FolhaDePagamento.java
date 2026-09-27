package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.models.*;
import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class FolhaDePagamento implements Serializable {

    private static boolean ehUltimoDiaUtil(LocalDate d) {
        LocalDate ultimoDia = d.withDayOfMonth(d.lengthOfMonth());
        while (ultimoDia.getDayOfWeek() == DayOfWeek.SATURDAY || ultimoDia.getDayOfWeek() == DayOfWeek.SUNDAY) {
            ultimoDia = ultimoDia.minusDays(1);
        }
        return d.equals(ultimoDia);
    }

    public static String formatarMoeda(double valor) {
        return String.format(Locale.GERMAN, "%.2f", valor);
    }

    private static String formatarMetodo(Empregado e) {
        String metodo = e.getMetodoPagamento();
        if ("emMaos".equals(metodo)) {
            return "Em maos";
        } else if ("correios".equals(metodo)) {
            return "Correios, " + e.getEndereco();
        } else if ("banco".equals(metodo)) {
            return e.getBanco() + ", Ag. " + e.getAgencia() + " CC " + e.getContaCorrente();
        }
        return metodo;
    }

    public static class ResultadoFolha {
        public String relatorio;
        public double totalFolha;

        public ResultadoFolha(String relatorio, double totalFolha) {
            this.relatorio = relatorio;
            this.totalFolha = totalFolha;
        }
    }

    public static ResultadoFolha calcular(Map<String, Empregado> empregados, LocalDate data, boolean commit) {
        boolean pagaHoristas = (data.getDayOfWeek() == DayOfWeek.FRIDAY);
        boolean pagaAssalariados = ehUltimoDiaUtil(data);
        boolean pagaComissionados = (data.getDayOfWeek() == DayOfWeek.FRIDAY && ChronoUnit.DAYS.between(LocalDate.of(2005, 1, 14), data) % 14 == 0);

        StringBuilder sb = new StringBuilder();
        sb.append("FOLHA DE PAGAMENTO DO DIA ").append(data.toString()).append("\r\n");
        sb.append("====================================\r\n\r\n");

        sb.append("===============================================================================================================================\r\n");
        sb.append("===================== HORISTAS ================================================================================================\r\n");
        sb.append("===============================================================================================================================\r\n");
        sb.append("Nome                                 Horas Extra Salario Bruto Descontos Salario Liquido Metodo\r\n");
        sb.append("==================================== ===== ===== ============= ========= =============== ======================================\r\n");

        List<EmpregadoHorista> horistas = new ArrayList<>();
        for (Empregado e : empregados.values()) {
            if (e instanceof EmpregadoHorista) {
                horistas.add((EmpregadoHorista) e);
            }
        }
        horistas.sort(Comparator.comparing(Empregado::getNome));

        long totalHorasNormaisH = 0;
        long totalHorasExtrasH = 0;
        double totalBrutoH = 0.0;
        double totalDescontosH = 0.0;
        double totalLiquidoH = 0.0;

        if (pagaHoristas) {
            LocalDate periodStart = data.minusDays(6);
            LocalDate periodEnd = data;

            for (EmpregadoHorista h : horistas) {
                double horasNormais = 0.0;
                double horasExtras = 0.0;

                for (point_card_pay_u c : h.getCartoes()) {
                    if (!c.getData().isBefore(periodStart) && !c.getData().isAfter(periodEnd)) {
                        horasNormais += c.getHorasNormais();
                        horasExtras += c.getHorasExtras();
                    }
                }

                double salarioHora = Double.parseDouble(h.getSalarioHorario().replace(",", "."));
                double bruto = horasNormais * salarioHora + horasExtras * salarioHora * 1.5;
                double descontos = 0.0;
                double liquido = 0.0;

                if (!h.getCartoes().isEmpty()) {
                    if (h.isSindicalizado()) {
                        double taxaSemanal = 7.0 * h.getTaxaSindical();
                        double debitoAnterior = h.getDebitoSindicato();
                        double taxaSindicalTotal = debitoAnterior + taxaSemanal;

                        double taxasServico = 0.0;
                        for (TaxaServico ts : h.getTaxasServico()) {
                            if (!ts.getData().isBefore(periodStart) && !ts.getData().isAfter(periodEnd)) {
                                taxasServico += ts.getValor();
                            }
                        }

                        double totalTaxas = taxaSindicalTotal + taxasServico;
                        if (bruto == 0.0) {
                            descontos = 0.0;
                            liquido = 0.0;
                            if (commit) {
                                h.setDebitoSindicato(taxaSindicalTotal);
                            }
                        } else {
                            descontos = Math.min(bruto, totalTaxas);
                            liquido = bruto - descontos;
                            if (commit) {
                                h.setDebitoSindicato(0.0);
                            }
                        }
                    } else {
                        descontos = 0.0;
                        liquido = bruto;
                    }
                }

                totalHorasNormaisH += (long) horasNormais;
                totalHorasExtrasH += (long) horasExtras;
                totalBrutoH += bruto;
                totalDescontosH += descontos;
                totalLiquidoH += liquido;

                sb.append(String.format(Locale.GERMAN, "%-36s %5d %5d %13s %9s %15s %s\r\n",
                        h.getNome(),
                        (long) horasNormais,
                        (long) horasExtras,
                        formatarMoeda(bruto),
                        formatarMoeda(descontos),
                        formatarMoeda(liquido),
                        formatarMetodo(h)));
            }
        }

        sb.append("\r\n");
        sb.append(String.format(Locale.GERMAN, "%-36s %5d %5d %13s %9s %15s\r\n\r\n",
                "TOTAL HORISTAS",
                totalHorasNormaisH,
                totalHorasExtrasH,
                formatarMoeda(totalBrutoH),
                formatarMoeda(totalDescontosH),
                formatarMoeda(totalLiquidoH)));

        sb.append("===============================================================================================================================\r\n");
        sb.append("===================== ASSALARIADOS ============================================================================================\r\n");
        sb.append("===============================================================================================================================\r\n");
        sb.append("Nome                                             Salario Bruto Descontos Salario Liquido Metodo\r\n");
        sb.append("================================================ ============= ========= =============== ======================================\r\n");

        List<EmpregadoAssalariado> assalariados = new ArrayList<>();
        for (Empregado e : empregados.values()) {
            if (e instanceof EmpregadoAssalariado && !(e instanceof EmpregadoComissionado)) {
                assalariados.add((EmpregadoAssalariado) e);
            }
        }
        assalariados.sort(Comparator.comparing(Empregado::getNome));

        double totalBrutoA = 0.0;
        double totalDescontosA = 0.0;
        double totalLiquidoA = 0.0;

        if (pagaAssalariados) {
            LocalDate periodStart = data.withDayOfMonth(1);
            LocalDate periodEnd = data;
            int diasNoMes = data.lengthOfMonth();

            for (EmpregadoAssalariado a : assalariados) {
                double bruto = Double.parseDouble(a.getSalarioMensal().replace(",", "."));
                double descontos = 0.0;

                if (a.isSindicalizado()) {
                    double taxaSindical = diasNoMes * a.getTaxaSindical();
                    double taxasServico = 0.0;
                    for (TaxaServico ts : a.getTaxasServico()) {
                        if (!ts.getData().isBefore(periodStart) && !ts.getData().isAfter(periodEnd)) {
                            taxasServico += ts.getValor();
                        }
                    }
                    descontos = taxaSindical + taxasServico;
                }

                double liquido = bruto - descontos;
                totalBrutoA += bruto;
                totalDescontosA += descontos;
                totalLiquidoA += liquido;

                sb.append(String.format(Locale.GERMAN, "%-48s %13s %9s %15s %s\r\n",
                        a.getNome(),
                        formatarMoeda(bruto),
                        formatarMoeda(descontos),
                        formatarMoeda(liquido),
                        formatarMetodo(a)));
            }
        }

        sb.append("\r\n");
        sb.append(String.format(Locale.GERMAN, "%-48s %13s %9s %15s\r\n\r\n",
                "TOTAL ASSALARIADOS",
                formatarMoeda(totalBrutoA),
                formatarMoeda(totalDescontosA),
                formatarMoeda(totalLiquidoA)));

        sb.append("===============================================================================================================================\r\n");
        sb.append("===================== COMISSIONADOS ===========================================================================================\r\n");
        sb.append("===============================================================================================================================\r\n");
        sb.append("Nome                  Fixo     Vendas   Comissao Salario Bruto Descontos Salario Liquido Metodo\r\n");
        sb.append("===================== ======== ======== ======== ============= ========= =============== ======================================\r\n");

        List<EmpregadoComissionado> comissionados = new ArrayList<>();
        for (Empregado e : empregados.values()) {
            if (e instanceof EmpregadoComissionado) {
                comissionados.add((EmpregadoComissionado) e);
            }
        }
        comissionados.sort(Comparator.comparing(Empregado::getNome));

        double totalFixoC = 0.0;
        double totalVendasC = 0.0;
        double totalComissaoC = 0.0;
        double totalBrutoC = 0.0;
        double totalDescontosC = 0.0;
        double totalLiquidoC = 0.0;

        if (pagaComissionados) {
            LocalDate periodStart = data.minusDays(13);
            LocalDate periodEnd = data;

            for (EmpregadoComissionado c : comissionados) {
                double salMensal = Double.parseDouble(c.getSalarioMensal().replace(",", "."));
                double fixo = Math.floor((salMensal * 24.0 / 52.0) * 100.0) / 100.0;

                double vendas = 0.0;
                for (ResultadoVenda v : c.getVendas()) {
                    if (!v.getData().isBefore(periodStart) && !v.getData().isAfter(periodEnd)) {
                        vendas += v.getValor();
                    }
                }

                double taxaComissao = Double.parseDouble(c.getComissao().replace(",", "."));
                double comissao = Math.floor((vendas * taxaComissao) * 100.0) / 100.0;

                double bruto = fixo + comissao;
                double descontos = 0.0;

                if (c.isSindicalizado()) {
                    double taxaSindical = 14.0 * c.getTaxaSindical();
                    double taxasServico = 0.0;
                    for (TaxaServico ts : c.getTaxasServico()) {
                        if (!ts.getData().isBefore(periodStart) && !ts.getData().isAfter(periodEnd)) {
                            taxasServico += ts.getValor();
                        }
                    }
                    descontos = taxaSindical + taxasServico;
                }

                double liquido = bruto - descontos;

                totalFixoC += fixo;
                totalVendasC += vendas;
                totalComissaoC += comissao;
                totalBrutoC += bruto;
                totalDescontosC += descontos;
                totalLiquidoC += liquido;

                sb.append(String.format(Locale.GERMAN, "%-21s %8s %8s %8s %13s %9s %15s %s\r\n",
                        c.getNome(),
                        formatarMoeda(fixo),
                        formatarMoeda(vendas),
                        formatarMoeda(comissao),
                        formatarMoeda(bruto),
                        formatarMoeda(descontos),
                        formatarMoeda(liquido),
                        formatarMetodo(c)));
            }
        }

        sb.append("\r\n");
        sb.append(String.format(Locale.GERMAN, "%-21s %8s %8s %8s %13s %9s %15s\r\n\r\n",
                "TOTAL COMISSIONADOS",
                formatarMoeda(totalFixoC),
                formatarMoeda(totalVendasC),
                formatarMoeda(totalComissaoC),
                formatarMoeda(totalBrutoC),
                formatarMoeda(totalDescontosC),
                formatarMoeda(totalLiquidoC)));

        double totalGeral = totalBrutoH + totalBrutoA + totalBrutoC;
        sb.append("TOTAL FOLHA: ").append(formatarMoeda(totalGeral)).append("\r\n");

        return new ResultadoFolha(sb.toString(), totalGeral);
    }
}
