package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.core.Sistema;

public class Facade {
    private Sistema sistema = Sistema.carregar();

    public void zerarSistema() throws Exception {
        sistema.zerarSistema();
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
        return sistema.criarEmpregado(nome, endereco, tipo, salario);
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws Exception {
        return sistema.criarEmpregado(nome, endereco, tipo, salario, comissao);
    }

    public String getAtributoEmpregado(String idEmpregado, String atributo) throws Exception {
        return sistema.getAtributoEmpregado(idEmpregado, atributo);
    }

    public String getEmpregadoPorNome(String nome, int indice) throws Exception {
        return sistema.getEmpregadoPorNome(nome, indice);
    }

    public void removerEmpregado(String idEmpregado) throws Exception {
        sistema.removerEmpregado(idEmpregado);
    }

    public void lancaCartao(String idEmpregado, String data, String horas) throws Exception {
        sistema.lancaCartao(idEmpregado, data, horas);
    }

    public String getHorasNormaisTrabalhadas(String idEmpregado, String dataInicial, String dataFinal) throws Exception {
        return sistema.getHorasNormaisTrabalhadas(idEmpregado, dataInicial, dataFinal);
    }

    public String getHorasExtrasTrabalhadas(String idEmpregado, String dataInicial, String dataFinal) throws Exception {
        return sistema.getHorasExtrasTrabalhadas(idEmpregado, dataInicial, dataFinal);
    }

    public void lancaVenda(String idEmpregado, String data, String valor) throws Exception {
        sistema.lancaVenda(idEmpregado, data, valor);
    }

    public String getVendasRealizadas(String idEmpregado, String dataInicial, String dataFinal) throws Exception {
        return sistema.getVendasRealizadas(idEmpregado, dataInicial, dataFinal);
    }

    public void alteraEmpregado(String idEmpregado, String atributo, String valor) throws Exception {
        sistema.alteraEmpregado(idEmpregado, atributo, valor);
    }

    public void alteraEmpregado(String idEmpregado, String atributo, String valor, String arg4) throws Exception {
        sistema.alteraEmpregado(idEmpregado, atributo, valor, arg4);
    }

    public void alteraEmpregado(String idEmpregado, String atributo, String valor, String idSindicato, String taxaSindical) throws Exception {
        sistema.alteraEmpregado(idEmpregado, atributo, valor, idSindicato, taxaSindical);
    }

    public void alteraEmpregado(String idEmpregado, String atributo, String valor, String banco, String agencia, String contaCorrente) throws Exception {
        sistema.alteraEmpregado(idEmpregado, atributo, valor, banco, agencia, contaCorrente);
    }

    public void lancaTaxaServico(String membro, String data, String valor) throws Exception {
        sistema.lancaTaxaServico(membro, data, valor);
    }

    public String getTaxasServico(String idEmpregado, String dataInicial, String dataFinal) throws Exception {
        return sistema.getTaxasServico(idEmpregado, dataInicial, dataFinal);
    }

    public void criarAgendaDePagamentos(String descricao) throws Exception {
        sistema.criarAgendaDePagamentos(descricao);
    }

    public String totalFolha(String data) throws Exception {
        return sistema.totalFolha(data);
    }

    public void rodaFolha(String data, String saida) throws Exception {
        sistema.rodaFolha(data, saida);
    }

    public int getNumeroDeEmpregados() {
        return sistema.getNumeroDeEmpregados();
    }

    public void undo() throws Exception {
        sistema.undo();
    }

    public void redo() throws Exception {
        sistema.redo();
    }

    public void encerrarSistema() throws Exception {
        sistema.encerrarSistema();
    }
}
