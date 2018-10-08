package br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl;


public class Usuario {

    private String id;
    private String senha;
    private String tokenAutenticacao;

    public Usuario() {
        this(null, null, null);
    }

    public Usuario(String idParam, String senhaParam, String tokenParam) {
        this.id = idParam;
        this.senha = senhaParam;
        this.tokenAutenticacao = tokenParam;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTokenAutenticacao() {
        return tokenAutenticacao;
    }

    public void setTokenAutenticacao(String token) {
        this.tokenAutenticacao = token;
    }
}
