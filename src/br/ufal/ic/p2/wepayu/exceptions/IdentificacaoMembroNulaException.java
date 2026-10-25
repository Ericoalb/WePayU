package br.ufal.ic.p2.wepayu.exceptions;

public class IdentificacaoMembroNulaException extends WePayUException {
    public IdentificacaoMembroNulaException() { super("Identificacao do membro nao pode ser nula."); }
}

