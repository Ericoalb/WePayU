package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.exceptions.EmpregadoNaoRecebeEmBancoException;
import java.io.Serializable;

public interface MetodoPagamento extends Serializable {
    String getTipo();
    String getDescricaoFolha(String endereco);

    default String getBanco() throws EmpregadoNaoRecebeEmBancoException {
        throw new EmpregadoNaoRecebeEmBancoException();
    }

    default String getAgencia() throws EmpregadoNaoRecebeEmBancoException {
        throw new EmpregadoNaoRecebeEmBancoException();
    }

    default String getContaCorrente() throws EmpregadoNaoRecebeEmBancoException {
        throw new EmpregadoNaoRecebeEmBancoException();
    }
}
