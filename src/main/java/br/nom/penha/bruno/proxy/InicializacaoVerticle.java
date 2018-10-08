package br.nom.penha.bruno.proxy;

import java.util.concurrent.ConcurrentMap;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.platform.Verticle;

import br.nom.penha.bruno.proxy.cachearquivos.CacheArquivoImpl;
import br.nom.penha.bruno.proxy.cachearquivos.CacheArquivoVerticle;
import br.nom.penha.bruno.proxy.handlers.ProxyReversoVerticle;
import br.nom.penha.bruno.proxy.reverso.comum.TrataProxyReverso;
import br.nom.penha.bruno.proxy.reverso.configuracao.ConfiguracaoProxyReverso;


public class InicializacaoVerticle extends Verticle {

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

		container.deployVerticle("br.nom.penha.bruno.proxy.cachearquivos.CacheArquivoVerticle", container.config(), new AsyncResultHandler<String>() {
			@Override
			public void handle(AsyncResult<String> event) {
				publicaVerticlesAdicionais();
			}
		});

	}

	private void publicaVerticlesAdicionais() {
		CacheArquivoImpl instanciaCacheArquivo = CacheArquivoVerticle.getInstanciaCacheArquivo(vertx);

		String caminhoConfig = ProxyReversoVerticle.config();
		instanciaCacheArquivo.colocaArquivoEmSincronia(caminhoConfig, CacheArquivoVerticle.CANAL_CACHE_ARQUIVO);
		ConcurrentMap<String, byte[]> mapaCache = vertx.sharedData().getMap(CacheArquivoVerticle.MAPA_CACHE_ARQUIVO);
		ConfiguracaoProxyReverso config = TrataProxyReverso.getConfig(ConfiguracaoProxyReverso.class, mapaCache.get(caminhoConfig));

		vertx.eventBus().send(CacheArquivoVerticle.CANAL_CACHE_ARQUIVO, caminhoConfig);

		for (String path : ProxyReversoVerticle.dependencies(config)) {
			instanciaCacheArquivo.colocaArquivoEmSincronia(path, CacheArquivoVerticle.CANAL_CACHE_ARQUIVO);

			vertx.eventBus().send(CacheArquivoVerticle.CANAL_CACHE_ARQUIVO, path);
		}
		container.deployVerticle("br.nom.penha.bruno.proxy.handlers.ProxyReversoVerticle", container.config());
	}
}