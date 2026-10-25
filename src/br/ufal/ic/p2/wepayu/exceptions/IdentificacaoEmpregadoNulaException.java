package br.ufal.ic.p2.wepayu.exceptions;

public class IdentificacaoEmpregadoNulaException extends WePayUException {
    public IdentificacaoEmpregadoNulaException() { super("Identificacao do empregado nao pode ser nula."); }
}

