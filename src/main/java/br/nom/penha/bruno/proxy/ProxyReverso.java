package br.nom.penha.bruno.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.Verticle;

import br.nom.penha.bruno.proxy.handlers.ProxyReversoHandler;
import br.nom.penha.bruno.proxy.handlers.RequisicaoAutenticacaoHandler;
import br.nom.penha.bruno.proxy.reverso.comum.TrataProxyReverso;
import br.nom.penha.bruno.proxy.reverso.configuracao.ConfiguracaoProxyReverso;

public class ProxyReverso extends Verticle{


	/**
	 * Log
	 */
	private static final Logger log = LoggerFactory.getLogger(ProxyReverso.class);

	public static final String CAMINHO_CONFIG = "conf/conf.reverseproxy.json";

	private ConfiguracaoProxyReverso config;

	private ConcurrentMap<String, byte[]> mapaCacheCompartihado;

	private static String raizRecursos;
	private static String webRoot;

	public static String getResourceRoot() {
		return raizRecursos;
	}

	public static String getWebRoot() {
		return webRoot;
	}

	public void start() {
		raizRecursos = container.config().getString("resourceRoot");
		webRoot = container.config().getString("webRoot");

		mapaCacheCompartihado = vertx.sharedData().getMap("file.cache.map");
		config = TrataProxyReverso.getConfig(ConfiguracaoProxyReverso.class, mapaCacheCompartihado.get(raizRecursos + CAMINHO_CONFIG));
		// inicio o verticle
		doStart();
	}

	public void doStart() {


		RouteMatcher routeMatcher = new RouteMatcher();

		routeMatcher.all("/auth", new RequisicaoAutenticacaoHandler(vertx));

		for (String asset : config.recursos) {
			String pattern = "/.*\\." + asset;
			log.debug("Adding asset " + pattern);
		}

		routeMatcher.all("/.*", new ProxyReversoHandler(vertx, true));

		final HttpServer httpsServer = vertx.createHttpServer()
				.requestHandler(routeMatcher)
				.setSSL(true)
				.setKeyStorePath(this.raizRecursos + config.ssl.caminhoDoKeyStore)
				.setKeyStorePassword(config.ssl.senhaDoKeyStore);

		httpsServer.listen(config.ssl.portaProxyHttps);
	}

	public static String config() {
		return InicializacaoVerticle.getInicioRecursos() + CAMINHO_CONFIG;
	}

	public static String configAfterDeployment() {
		return getResourceRoot() + CAMINHO_CONFIG;
	}

	public static List<String> dependencies(ConfiguracaoProxyReverso config) {
		List<String> dependencyList = new ArrayList<String>();

		dependencyList.add(InicializacaoVerticle.getInicioWeb() + "auth/login.html");
		dependencyList.add(InicializacaoVerticle.getInicioWeb() + "redirectConfirmation.html");

		dependencyList.add(InicializacaoVerticle.getInicioRecursos() + config.ssl.caminhoDoSymKey);

		return dependencyList;
	}

	
}
