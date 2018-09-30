package br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hpark
 */
public class UserList {

    private List<User> userList;

    public UserList() {
        userList = new ArrayList<User>();
    }

    public UserList(List<User> userList) {
        this.userList = userList;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

}
