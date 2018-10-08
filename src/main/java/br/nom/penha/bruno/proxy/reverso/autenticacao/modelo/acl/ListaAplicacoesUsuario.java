package br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl;

import java.util.List;

public class ListaAplicacoesUsuario {

    List<UsuarioAplicacao> listaAplicacoesUsuario;

    public ListaAplicacoesUsuario(List<UsuarioAplicacao> applicationUserList) {
        this.listaAplicacoesUsuario = applicationUserList;
    }

    public List<UsuarioAplicacao> getListaAplicacoesUsuario() {
        return listaAplicacoesUsuario;
    }

    public void setListaAplicacoesUsuario(List<UsuarioAplicacao> applicationUserList) {
        this.listaAplicacoesUsuario = applicationUserList;
    }
}
