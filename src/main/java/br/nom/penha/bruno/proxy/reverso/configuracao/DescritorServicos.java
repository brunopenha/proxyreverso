package br.nom.penha.bruno.proxy.reverso.configuracao;

import java.util.Map;

/**
 */
public class DescritorServicos {

    public String host;
    public Integer porta;
    public Map<String, String> caminhosRequisitados;

    public DescritorServicos(String host, int porta, Map<String, String> caminhosRequisitados) {
        this.host = host;
        this.porta = porta;
        this.caminhosRequisitados = caminhosRequisitados;
    }
}
