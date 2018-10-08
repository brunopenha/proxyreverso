package br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl;

import java.util.ArrayList;
import java.util.List;

public class UsuarioAplicacao {

    private String usuario;
    private String organizacao;
    private List<String> papeis;

    public UsuarioAplicacao() {
        this(null, null);
    }

    public UsuarioAplicacao(List<String> papeis) {
        this(null, null, papeis);
    }

    public UsuarioAplicacao(String usuario, String organizacao) {
        this(usuario, organizacao, new ArrayList<String>());
    }

    public UsuarioAplicacao(String usuarioParam, String organizacaoParam, List<String> papeisParam) {
        this.usuario = usuarioParam;
        this.organizacao = organizacaoParam;
        this.papeis = papeisParam;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getOrganizacao() {
        return organizacao;
    }

    public void setOrganizacao(String organizacao) {
        this.organizacao = organizacao;
    }

    public List<String> getPapeis() {
        return papeis;
    }

    public void setPapeis(List<String> papeis) {
        this.papeis = papeis;
    }


}
