package br.ufal.ic.p2.wepayu.core;

import br.ufal.ic.p2.wepayu.exceptions.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.Locale;

public class ValidadorEmpregado {

    public static double converterParaDouble(String valorStr, Exception erroNulo, Exception erroNumerico, Exception erroNaoNegativo) throws Exception {
        if (valorStr == null || valorStr.trim().isEmpty()) {
            throw erroNulo;
        }
        double valorConvertido;
        try {
            valorConvertido = Double.parseDouble(valorStr.replace(",", "."));
        } catch (NumberFormatException excecao) {
            try {
                erroNumerico.initCause(excecao);
            } catch (IllegalStateException ignorado) {
            }
            throw erroNumerico;
        }
        if (valorConvertido < 0) {
            throw erroNaoNegativo;
        }
        return valorConvertido;
    }

    public static LocalDate converterParaData(String dataStr, Exception erroFormatoInvalido) throws Exception {
        if (dataStr == null || dataStr.trim().isEmpty()) {
            throw erroFormatoInvalido;
        }
        String[] partesData = dataStr.split("/");
        if (partesData.length != 3) {
            throw erroFormatoInvalido;
        }
        try {
            int dia = Integer.parseInt(partesData[0]);
            int mes = Integer.parseInt(partesData[1]);
            int ano = Integer.parseInt(partesData[2]);
            return LocalDate.of(ano, mes, dia);
        } catch (Exception excecao) {
            try {
                erroFormatoInvalido.initCause(excecao);
            } catch (IllegalStateException ignorado) {
            }
            throw erroFormatoInvalido;
        }
    }

    public static void validarIntervaloDatas(LocalDate dataInicial, LocalDate dataFinal) throws Exception {
        if (dataInicial.isAfter(dataFinal)) {
            throw new DataInicialNaoPodeSerPosteriorException();
        }
    }

    public static double validarDadosComuns(String nome, String endereco, String tipo, String salario) throws Exception {
        if (nome == null || nome.trim().isEmpty()) {
            throw new NomeNaoPodeSerNuloException();
        }
        if (endereco == null || endereco.trim().isEmpty()) {
            throw new EnderecoNaoPodeSerNuloException();
        }
        return converterParaDouble(salario,
                new SalarioNaoPodeSerNuloException(),
                new SalarioDeveSerNumericoException(),
                new SalarioDeveSerNaoNegativoException());
    }

    public static double validarComissao(String comissao) throws Exception {
        return converterParaDouble(comissao,
                new ComissaoNaoPodeSerNulaException(),
                new ComissaoDeveSerNumericaException(),
                new ComissaoDeveSerNaoNegativaException());
    }

    public static double validarHorasTrabalhadas(String horasStr) throws Exception {
        double horas;
        try {
            horas = Double.parseDouble(horasStr.replace(",", "."));
        } catch (Exception excecao) {
            HorasDevemSerNumericasException erro = new HorasDevemSerNumericasException();
            erro.initCause(excecao);
            throw erro;
        }
        if (horas <= 0) {
            throw new HorasDevemSerPositivasException();
        }
        return horas;
    }

    public static double validarValorMonetarioPositivo(String valorStr) throws Exception {
        double valor;
        try {
            valor = Double.parseDouble(valorStr.replace(",", "."));
        } catch (Exception excecao) {
            ValorDeveSerNumericoException erro = new ValorDeveSerNumericoException();
            erro.initCause(excecao);
            throw erro;
        }
        if (valor <= 0) {
            throw new ValorDeveSerPositivoException();
        }
        return valor;
    }

    public static String formatarMoeda(double valor) {
        return String.format(Locale.GERMAN, "%.2f", valor);
    }

    public static String formatarHoras(double horas) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##", new DecimalFormatSymbols(new Locale("pt", "BR")));
        return decimalFormat.format(horas);
    }
}
