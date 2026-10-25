package br.ufal.ic.p2.wepayu.models;

public class MetodoPagamentoEmMaos implements MetodoPagamento {
    private static final long serialVersionUID = 1L;

    @Override
    public String getTipo() {
        return "emMaos";
    }

    @Override
    public String getDescricaoFolha(String endereco) {
        return "Em maos";
    }
}
