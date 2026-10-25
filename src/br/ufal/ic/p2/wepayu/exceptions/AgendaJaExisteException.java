package br.ufal.ic.p2.wepayu.exceptions;

public class AgendaJaExisteException extends WePayUException {
    public AgendaJaExisteException() {
        super("Agenda de pagamentos ja existe");
    }
}
