package br.ufal.ic.p2.wepayu.models;

public class MetodoPagamentoCorreios implements MetodoPagamento {
    private static final long serialVersionUID = 1L;

    @Override
    public String getTipo() {
        return "correios";
    }

    @Override
    public String getDescricaoFolha(String endereco) {
        return "Correios, " + endereco;
    }
}
