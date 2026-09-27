package br.ufal.ic.p2.wepayu.models;

import java.util.ArrayList;
import java.util.List;

public class EmpregadoHorista extends Empregado {
    private String salarioHorario;
    private List<point_card_pay_u> cartoes = new ArrayList<>();

    public EmpregadoHorista(String id, String nome, String endereco, String salarioHorario) {
        super(id, nome, endereco, "horista");
        this.salarioHorario = salarioHorario;
    }

    public String getSalarioHorario() {
        return salarioHorario;
    }

    public void setSalarioHorario(String salarioHorario) {
        this.salarioHorario = salarioHorario;
    }

    public void adicionarCartao(point_card_pay_u cartao) {
        cartoes.add(cartao);
    }

    public List<point_card_pay_u> getCartoes() {
        return cartoes;
    }
}
