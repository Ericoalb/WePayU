package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.exceptions.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;

public class Sindicato implements Serializable {
    private static final long serialVersionUID = 1L;

    public void validarIdDisponivel(Collection<Empregado> todosEmpregados, String idEmpregadoAtual, String idSindicato) throws Exception {
        if (idSindicato == null || idSindicato.trim().isEmpty()) {
            throw new IdentificacaoSindicatoNulaException();
        }
        for (Empregado outroEmpregado : todosEmpregados) {
            if (!outroEmpregado.getId().equals(idEmpregadoAtual) && outroEmpregado.isSindicalizado() && idSindicato.equals(outroEmpregado.getIdSindicato())) {
                throw new IdentificacaoSindicatoDuplicadaException();
            }
        }
    }

    public Empregado obterMembro(Collection<Empregado> todosEmpregados, String idMembro) throws Exception {
        if (idMembro == null || idMembro.trim().isEmpty()) {
            throw new IdentificacaoMembroNulaException();
        }
        for (Empregado empregado : todosEmpregados) {
            if (empregado.isSindicalizado() && idMembro.equals(empregado.getIdSindicato())) {
                return empregado;
            }
        }
        throw new MembroNaoExisteException();
    }

    public void lancarTaxaServico(Collection<Empregado> todosEmpregados, String idMembro, LocalDate data, double valor) throws Exception {
        Empregado membro = obterMembro(todosEmpregados, idMembro);
        membro.adicionarTaxaServico(new TaxaServico(data, valor));
    }
}
