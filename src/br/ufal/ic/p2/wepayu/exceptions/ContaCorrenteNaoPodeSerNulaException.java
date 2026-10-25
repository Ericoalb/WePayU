package br.ufal.ic.p2.wepayu.exceptions;

public class ContaCorrenteNaoPodeSerNulaException extends WePayUException {
    public ContaCorrenteNaoPodeSerNulaException() { super("Conta corrente nao pode ser nulo."); }
}

