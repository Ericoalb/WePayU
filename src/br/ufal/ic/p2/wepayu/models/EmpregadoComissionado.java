package br.ufal.ic.p2.wepayu.models;

import java.util.ArrayList;
import java.util.List;

public class EmpregadoComissionado extends EmpregadoAssalariado {
    private String comissao;
    private List<ResultadoVenda> vendas = new ArrayList<>();

    public EmpregadoComissionado(String id, String nome, String endereco, String salarioMensal, String comissao) {
        super(id, nome, endereco, "comissionado", salarioMensal);
        this.comissao = comissao;
    }

    public String getComissao() {
        return comissao;
    }

    public void setComissao(String comissao) {
        this.comissao = comissao;
    }

    public void adicionarVenda(ResultadoVenda venda) {
        vendas.add(venda);
    }

    public List<ResultadoVenda> getVendas() {
        return vendas;
    }
}
