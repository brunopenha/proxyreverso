package br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl;

public class RequisicaoAutenticacao {

    private Autenticacao autenticacao;
    private String tokenAutenticacao;

    public RequisicaoAutenticacao() {
        this(new Autenticacao(), null);
    }

    public RequisicaoAutenticacao(Autenticacao autenticacao, String token) {
        this.autenticacao = autenticacao;
        this.tokenAutenticacao = token;
    }

    public Autenticacao getAutenticacao() {
        return autenticacao;
    }

    public void setAutenticacao(Autenticacao autenticacao) {
        this.autenticacao = autenticacao;
    }

    public String getTokenAutenticacao() {
        return tokenAutenticacao;
    }

    public void setTokenAutenticacao(String token) {
        this.tokenAutenticacao = token;
    }
}
