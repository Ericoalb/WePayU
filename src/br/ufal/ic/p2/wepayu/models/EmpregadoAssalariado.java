package br.ufal.ic.p2.wepayu.models;

public class EmpregadoAssalariado extends Empregado {
    private String salarioMensal;

    public EmpregadoAssalariado(String id, String nome, String endereco, String tipo, String salarioMensal) {
        super(id, nome, endereco, tipo);
        this.salarioMensal = salarioMensal;
    }

    public String getSalarioMensal() {
        return salarioMensal;
    }

    public void setSalarioMensal(String salarioMensal) {
        this.salarioMensal = salarioMensal;
    }
}
