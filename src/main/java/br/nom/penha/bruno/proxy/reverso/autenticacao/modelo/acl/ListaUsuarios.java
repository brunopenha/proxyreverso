package br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl;

import java.util.ArrayList;
import java.util.List;

public class ListaUsuarios {

    private List<Usuario> listaUsuarios;

    public ListaUsuarios() {
        listaUsuarios = new ArrayList<Usuario>();
    }

    public ListaUsuarios(List<Usuario> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
    }

    public List<Usuario> getListaUsuarios() {
        return listaUsuarios;
    }

    public void setListaUsuarios(List<Usuario> listaUsuario) {
        this.listaUsuarios = listaUsuario;
    }

}
