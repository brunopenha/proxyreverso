package br.nom.penha.bruno.proxy.reverso.configuracao;

/**
 * Classe para o tratamento do SSL
*/
public class SSL {

	// propriedades do cliente
    public String caminhoDoTrustStore, senhaDoTrustStore;

    // propriedades do servidor
    public String caminhoDoKeyStore, senhaDoKeyStore;
    public String caminhoDoSymKey;
    public int portaProxyHttps;

}
