package br.ufal.ic.p2.wepayu.exceptions;

public class DataInicialNaoPodeSerPosteriorException extends WePayUException {
    public DataInicialNaoPodeSerPosteriorException() { super("Data inicial nao pode ser posterior aa data final."); }
}

