package br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl;

/**
 * @author hpark
 */
public class AuthenticationResponse {

    private Response response;

    public AuthenticationResponse(Response response) {
        super();
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
