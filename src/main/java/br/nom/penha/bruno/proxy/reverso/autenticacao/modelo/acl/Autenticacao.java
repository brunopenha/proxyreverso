package br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hpark
 */
public class Autenticacao {

    private List<AuthRequest> authRequestList;

    public Autenticacao() {
        authRequestList = new ArrayList<AuthRequest>();
    }

    public Autenticacao(List<AuthRequest> authRequestList) {
        this.authRequestList = authRequestList;
    }

    public List<AuthRequest> getAuthRequestList() {
        return authRequestList;
    }

    public void setAuthRequestList(List<AuthRequest> authRequestList) {
        this.authRequestList = authRequestList;
    }
}