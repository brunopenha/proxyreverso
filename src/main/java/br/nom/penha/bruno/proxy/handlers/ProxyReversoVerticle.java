package br.nom.penha.bruno.proxy.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.Verticle;

import br.nom.penha.bruno.proxy.InicializacaoVerticle;
import br.nom.penha.bruno.proxy.cachearquivos.CacheArquivoVerticle;
import br.nom.penha.bruno.proxy.reverso.comum.TrataProxyReverso;
import br.nom.penha.bruno.proxy.reverso.configuracao.ConfiguracaoProxyReverso;

public class ProxyReversoVerticle extends Verticle {

	/**
	 * Log
	 */
	private static final Logger log = LoggerFactory.getLogger(ProxyReversoVerticle.class);

	public static final String CAMINHO_CONFIG = "conf/conf.reverseproxy.json";

	private ConfiguracaoProxyReverso config;

	private ConcurrentMap<String, byte[]> mapaCache;

	private static String inicioRecursos;
	private static String inicioWeb;

	public static String getInicioRecursos() {
		return inicioRecursos;
	}

	public static String getInicioWeb() {
		return inicioWeb;
	}

	public void start() {

		inicioRecursos = container.config().getString("inicioRecursos");
		inicioWeb = container.config().getString("inicioWeb");

		mapaCache = vertx.sharedData().getMap(CacheArquivoVerticle.MAPA_CACHE_ARQUIVO);
		config = TrataProxyReverso.getConfig(ConfiguracaoProxyReverso.class, mapaCache.get(inicioRecursos + CAMINHO_CONFIG));
		doStart();
	}

	public void doStart() {


		RouteMatcher routeMatcher = new RouteMatcher();

		routeMatcher.all("/auth", new RequisicaoAutenticacaoHandler(vertx));

		for (String asset : config.recursos) {
			String pattern = "/.*\\." + asset;
			log.debug("Adding asset " + pattern);
			routeMatcher.all(pattern, new ProxyReversoHandler(vertx, false));
		}

		routeMatcher.all("/.*", new ProxyReversoHandler(vertx, true));

		final HttpServer httpsServer = vertx.createHttpServer()
				.requestHandler(routeMatcher)
				.setSSL(true)
				.setKeyStorePath(ProxyReversoVerticle.getInicioRecursos() + config.ssl.caminhoDoKeyStore)
				.setKeyStorePassword(config.ssl.senhaDoKeyStore);

		httpsServer.listen(config.ssl.portaProxyHttps);
	}

	public static String config() {
		return InicializacaoVerticle.getInicioRecursos() + CAMINHO_CONFIG;
	}

	public static String configAfterDeployment() {
		return getInicioRecursos() + CAMINHO_CONFIG;
	}

	public static List<String> dependencies(ConfiguracaoProxyReverso config) {
		List<String> dependencyList = new ArrayList<String>();

		dependencyList.add(InicializacaoVerticle.getInicioWeb() + "auth/login.html");
		dependencyList.add(InicializacaoVerticle.getInicioWeb() + "redirectConfirmation.html");

		dependencyList.add(InicializacaoVerticle.getInicioRecursos() + config.ssl.caminhoDoSymKey);

		return dependencyList;
	}
}