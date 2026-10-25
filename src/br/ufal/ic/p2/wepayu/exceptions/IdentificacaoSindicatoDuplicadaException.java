package br.ufal.ic.p2.wepayu.exceptions;

public class IdentificacaoSindicatoDuplicadaException extends WePayUException {
    public IdentificacaoSindicatoDuplicadaException() { super("Ha outro empregado com esta identificacao de sindicato"); }
}

