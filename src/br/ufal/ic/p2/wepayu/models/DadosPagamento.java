package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;

public class DadosPagamento implements Serializable {
    private static final long serialVersionUID = 1L;

    private final double salarioBruto;
    private final double descontos;
    private final double salarioLiquido;
    private final long horasNormais;
    private final long horasExtras;
    private final double fixo;
    private final double vendas;
    private final double comissao;
    private final String linhaRelatorio;

    public DadosPagamento(double salarioBruto, double descontos, double salarioLiquido,
                          long horasNormais, long horasExtras,
                          double fixo, double vendas, double comissao,
                          String linhaRelatorio) {
        this.salarioBruto = salarioBruto;
        this.descontos = descontos;
        this.salarioLiquido = salarioLiquido;
        this.horasNormais = horasNormais;
        this.horasExtras = horasExtras;
        this.fixo = fixo;
        this.vendas = vendas;
        this.comissao = comissao;
        this.linhaRelatorio = linhaRelatorio;
    }

    public double getSalarioBruto() {
        return salarioBruto;
    }

    public double getDescontos() {
        return descontos;
    }

    public double getSalarioLiquido() {
        return salarioLiquido;
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

    public String getLinhaRelatorio() {
        return linhaRelatorio;
    }
}
