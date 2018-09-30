package br.nom.penha.bruno.proxy.handlers;

import java.util.ArrayList;
import java.util.List;

import br.nom.penha.bruno.proxy.cachearquivos.CacheArquivoVerticle;
import br.nom.penha.bruno.proxy.reverso.comum.TrataProxyReverso;
import br.nom.penha.bruno.proxy.reverso.configuracao.ConfiguracaoProxyReverso;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;

public class ReverseProxyVerticle extends AbstractVerticle {

	/**
	 * Log
	 */
	private static final Logger log = LoggerFactory.getLogger(ReverseProxyVerticle.class);

	public static final String CONFIG_PATH = "conf/conf.reverseproxy.json";

	private ConfiguracaoProxyReverso config;

	private LocalMap<String, byte[]> sharedCacheMap;

	private static String resourceRoot;
	private static String webRoot;

	public static String getResourceRoot() {
		return resourceRoot;
	}

	public static String getWebRoot() {
		return webRoot;
	}

	public void start() {

		resourceRoot = config().getString("resourceRoot");
		webRoot = config().getString("webRoot");

		sharedCacheMap = vertx.sharedData().getLocalMap(CacheArquivoVerticle.MAPA_CACHE_ARQUIVO);
		config = TrataProxyReverso.getConfig(ConfiguracaoProxyReverso.class, sharedCacheMap.get(resourceRoot + CONFIG_PATH));
		doStart();
	}

	public void doStart() {


//		RouteMatcher routeMatcher = new RouteMatcher();

//		routeMatcher.all("/auth", new AuthRequestHandler(vertx));

		for (String asset : config.recursos) {
			String pattern = "/.*\\." + asset;
			log.debug("Adding asset " + pattern);
//			routeMatcher.all(pattern, new ReverseProxyHandler(vertx, false));
		}

//		routeMatcher.all("/.*", new ReverseProxyHandler(vertx, true));

		final HttpServer httpsServer = vertx.createHttpServer();
//				.requestHandler(routeMatcher)
//				.setSSL(true)
//				.setKeyStorePath(ReverseProxyVerticle.getResourceRoot() + config.ssl.keyStorePath)
//				.setKeyStorePassword(config.ssl.keyStorePassword);

		httpsServer.listen(config.ssl.portaProxyHttps);
	}

//	public static String config() {
//		return BootstrapVerticle.getResourceRoot() + CONFIG_PATH;
//	}

	public static String configAfterDeployment() {
		return getResourceRoot() + CONFIG_PATH;
	}

	public static List<String> dependencies(ConfiguracaoProxyReverso config) {
		List<String> dependencyList = new ArrayList<String>();
//
//		dependencyList.add(BootstrapVerticle.getWebRoot() + "auth/login.html");
//		dependencyList.add(BootstrapVerticle.getWebRoot() + "redirectConfirmation.html");
//
//		dependencyList.add(BootstrapVerticle.getResourceRoot() + config.ssl.symKeyPath);

		return dependencyList;
	}
}