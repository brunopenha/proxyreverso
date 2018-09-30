package br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl;

import java.util.List;

/**
 * @author hpark
 */
public class ApplicationUserList {

    List<ApplicationUser> applicationUserList;

    public ApplicationUserList(List<ApplicationUser> applicationUserList) {
        this.applicationUserList = applicationUserList;
    }

    public List<ApplicationUser> getApplicationUserList() {
        return applicationUserList;
    }

    public void setApplicationUserList(List<ApplicationUser> applicationUserList) {
        this.applicationUserList = applicationUserList;
    }
}
