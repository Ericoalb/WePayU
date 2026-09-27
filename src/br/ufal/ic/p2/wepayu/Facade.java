package br.ufal.ic.p2.wepayu;

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

    public String getAtributoEmpregado(String emp, String atributo) throws Exception {
        return sistema.getAtributoEmpregado(emp, atributo);
    }

    public String getEmpregadoPorNome(String nome, int indice) throws Exception {
        return sistema.getEmpregadoPorNome(nome, indice);
    }

    public void removerEmpregado(String emp) throws Exception {
        sistema.removerEmpregado(emp);
    }

    public void lancaCartao(String emp, String data, String horas) throws Exception {
        sistema.lancaCartao(emp, data, horas);
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return sistema.getHorasNormaisTrabalhadas(emp, dataInicial, dataFinal);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return sistema.getHorasExtrasTrabalhadas(emp, dataInicial, dataFinal);
    }

    public void lancaVenda(String emp, String data, String valor) throws Exception {
        sistema.lancaVenda(emp, data, valor);
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return sistema.getVendasRealizadas(emp, dataInicial, dataFinal);
    }

    public void alteraEmpregado(String emp, String atributo, String valor) throws Exception {
        sistema.alteraEmpregado(emp, atributo, valor);
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String arg4) throws Exception {
        sistema.alteraEmpregado(emp, atributo, valor, arg4);
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical) throws Exception {
        sistema.alteraEmpregado(emp, atributo, valor, idSindicato, taxaSindical);
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String banco, String agencia, String contaCorrente) throws Exception {
        sistema.alteraEmpregado(emp, atributo, valor, banco, agencia, contaCorrente);
    }

    public void lancaTaxaServico(String membro, String data, String valor) throws Exception {
        sistema.lancaTaxaServico(membro, data, valor);
    }

    public String getTaxasServico(String emp, String dataInicial, String dataFinal) throws Exception {
        return sistema.getTaxasServico(emp, dataInicial, dataFinal);
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
