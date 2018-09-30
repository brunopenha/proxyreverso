package br.nom.penha.bruno.proxy.reverso.configuracao;

import java.util.HashMap;
import java.util.Map;

/**
 * Dependencias do serviço
 */
public class DependenciasServico {

    public Map<String, DescritorServicos> dependencias = new HashMap<>();

    public String getHost(String service) {
        for (String key : dependencias.keySet()) {
            if (key.equals(service)) {
                return dependencias.get(key).host;
            }
        }

        return null;
    }

    public Integer getPorta(String servico) {
        for (String key : dependencias.keySet()) {
            if (key.equals(servico)) {
                return dependencias.get(key).porta;
            }
        }

        return null;
    }

    public String getCaminhosRequisicao(String service, String caminhoDaChave) {
        for (String chave : dependencias.keySet()) {
            if (chave.equals(service)) {
                for (String caminhoChaveExistente : dependencias.get(chave).caminhosRequisitados.keySet()) {
                    if (caminhoChaveExistente.equals(caminhoDaChave)) {
                        return dependencias.get(chave).caminhosRequisitados.get(caminhoChaveExistente);
                    }
                }
            }
        }

        return null;
    }
}
