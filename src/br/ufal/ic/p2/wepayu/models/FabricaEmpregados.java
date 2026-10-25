package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.exceptions.*;

public class FabricaEmpregados {

    public static Empregado criarEmpregado(String idEmpregado, String nome, String endereco, String tipo, double salario) throws Exception {
        if ("horista".equals(tipo)) {
            return new EmpregadoHorista(idEmpregado, nome, endereco, salario);
        } else if ("assalariado".equals(tipo)) {
            return new EmpregadoAssalariado(idEmpregado, nome, endereco, salario);
        } else if ("comissionado".equals(tipo)) {
            throw new TipoNaoAplicavelException();
        } else {
            throw new TipoInvalidoException();
        }
    }

    public static Empregado criarComissionado(String idEmpregado, String nome, String endereco, String tipo, double salario, double comissao) throws Exception {
        if (!"comissionado".equals(tipo)) {
            throw new TipoNaoAplicavelException();
        }
        return new EmpregadoComissionado(idEmpregado, nome, endereco, salario, comissao);
    }

    public static Empregado converterTipo(Empregado empregadoAtual, String novoTipo, Double novoSalarioOuComissao) throws Exception {
        Empregado novoEmpregado;
        if ("assalariado".equals(novoTipo)) {
            novoEmpregado = new EmpregadoAssalariado(empregadoAtual.getId(), empregadoAtual.getNome(), empregadoAtual.getEndereco(), empregadoAtual.getSalario());
        } else if ("horista".equals(novoTipo)) {
            double salarioHorario = (novoSalarioOuComissao != null) ? novoSalarioOuComissao : empregadoAtual.getSalario();
            novoEmpregado = new EmpregadoHorista(empregadoAtual.getId(), empregadoAtual.getNome(), empregadoAtual.getEndereco(), salarioHorario);
        } else if ("comissionado".equals(novoTipo)) {
            if (novoSalarioOuComissao == null) {
                throw new ComissaoNaoPodeSerNulaException();
            }
            novoEmpregado = new EmpregadoComissionado(empregadoAtual.getId(), empregadoAtual.getNome(), empregadoAtual.getEndereco(), empregadoAtual.getSalario(), novoSalarioOuComissao);
        } else {
            throw new TipoInvalidoException();
        }
        novoEmpregado.copiarDadosDe(empregadoAtual);
        return novoEmpregado;
    }
}
