package br.nom.penha.bruno.proxy.reverso.configuracao;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public class ConfiguracaoProxyReverso {


	enum BinaryPrefix {
		k, m, g
	}

	public SSL ssl;
	public Map<String, AjustaRegra> ajustaPapeis;
	public String[] recursos;
	public String servicoComum;
	public String tamMaxCargaEmBytes;
	public DependenciasServico dependenciasServico;

	public ConfiguracaoProxyReverso() {

		// "recursos" que podem ser acessados sem autenticação/acl
		recursos = new String[] { "ico, png, jpg, jpeg, gif, css, js, txt" };

		ssl = new SSL();

		//FIXME externalizar isso
		// ssl (como um cliente)
		ssl.caminhoDoTrustStore = "../../../server-truststore.jks";
		ssl.senhaDoTrustStore = "password";

		// ssl (as server)
		ssl.portaProxyHttps = 8989;
		ssl.caminhoDoKeyStore = "../../../server-keystore.jks";
		ssl.senhaDoKeyStore = "password";

		// dependencias do serviço
		dependenciasServico = new DependenciasServico();
		Map<String, String> caminhos = new HashMap<>();
		caminhos.put("auth", "/auth");
		dependenciasServico.dependencias.put("auth", new DescritorServicos("localhost", 8000, caminhos));

		// ajustandos as regras...
		ajustaPapeis = new HashMap<String, AjustaRegra>();
		ajustaPapeis.put("sn", new AjustaRegra("http", "localhost", 8080));
		ajustaPapeis.put("acl", new AjustaRegra("http", "localhost", 9001));
		ajustaPapeis.put("um", new AjustaRegra("http", "localhost", 9000));
		ajustaPapeis.put("google", new AjustaRegra("http", "google.com", 80));
		ajustaPapeis.put("brunopenha", new AjustaRegra("http", "bruno.penha.nom.br", 80));

	}

	public long getTamMaxCargaEmNumerosBytes() {

		if (tamMaxCargaEmBytes.matches("\\d*[a-z]")) {
			String prefixoBinario = tamMaxCargaEmBytes.substring(tamMaxCargaEmBytes.length() - 1, tamMaxCargaEmBytes.length());
			String tamCarga = tamMaxCargaEmBytes.substring(0, tamMaxCargaEmBytes.length() - 1);
			switch (prefixoBinario) {
			case "k":
				return Long.valueOf(tamCarga) * 1024;
			case "m":
				return Long.valueOf(tamCarga) * 1024 * 1024;
			case "g":
				return Long.valueOf(tamCarga) * 1024 * 1024 * 1024;
			default:
				return 512 * 1024;
			}
		}
		else if (tamMaxCargaEmBytes.matches("\\d*")) {
			return Long.valueOf(tamMaxCargaEmBytes);
		}
		// se não encontrar o tamanho padrão, retorna um valor comum de 512k
		else {
			return 512 * 1024;
		}
	}

	public static void main(String[] args) {

		Gson g = new Gson();

		// testo do Pojo
		ConfiguracaoProxyReverso configurationA = new ConfiguracaoProxyReverso();
		System.out.println(g.toJson(configurationA));

		// Testo no Pojo
		String rawConfig = "{\"ssl_client\":{\"caminhoDoTrustStore\":\"../../../server-truststore.jks\",\"senhaDoTrustStore\":\"password\"},\"ssl_server\":{\"caminhoDoKeyStore\":\"../../../server-keystore.jks\",\"portaProxyHttps\":\"8989\",\"senhaDoKeyStore\":\"password\"},\"rewrite_rules\":{\"sn\":{\"protocol\":\"um\",\"host\":\"localhost\",\"porta\":9000},\"google\":{\"protocol\":\"http\",\"host\":\"google.com\",\"porta\":80}}}";
		ConfiguracaoProxyReverso configurationB = g.fromJson(rawConfig, ConfiguracaoProxyReverso.class);

		// e retorno para String
		configurationB.ajustaPapeis.put("bing", new AjustaRegra("http", "bing.com", 80));
		configurationB.ajustaPapeis.remove("google");
		System.out.println(g.toJson(configurationB));

		configurationB.tamMaxCargaEmBytes = "128k";
		System.out.println(configurationB.getTamMaxCargaEmNumerosBytes());
	}


}
