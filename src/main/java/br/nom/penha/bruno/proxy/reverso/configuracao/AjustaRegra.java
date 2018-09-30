package br.nom.penha.bruno.proxy.reverso.configuracao;

/**
 * Usado na classe de Configuração
 */
public class AjustaRegra {

	private String nome;
	private String protocolo;
	private String host;
	private Integer porta;
	private String caminhoDoTrustStore;
	private String senhaDoTrustStore;
	
	public AjustaRegra(String protocolo, String host, Integer porta) {
		this(protocolo, host, null, porta);
	}

	public AjustaRegra(String protocolo, String host, String nome, Integer porta) {
		this(protocolo, host, nome, porta, null, null);
	}

	public AjustaRegra(String protocolo, String host, String nome, Integer porta, String caminhoDoTrustStore, String SenhaDoTrustStore) {
		this.protocolo = protocolo;
		this.host = host;
		this.porta = porta;
		this.nome = nome;
		this.caminhoDoTrustStore = caminhoDoTrustStore;
		this.senhaDoTrustStore = SenhaDoTrustStore;
	}

	public String getProtocolo() {
		return null == protocolo ? "http" : protocolo;
	}

	public String getHost() {
		return host;
	}

	public String getNome() {
		return nome;
	}

	public Integer getPorta() {
		return null == porta ? ("http".equals(protocolo) ? 80 : 443) : porta;
	}

	public String getCaminhoDoTrustStore() {
		return caminhoDoTrustStore;
	}

	public String getSenhaDoTrustStore() {
		return senhaDoTrustStore;
	}
}
