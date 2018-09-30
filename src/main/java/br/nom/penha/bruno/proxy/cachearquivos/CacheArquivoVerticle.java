package br.nom.penha.bruno.proxy.cachearquivos;

import java.util.Set;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 */
public class CacheArquivoVerticle extends AbstractVerticle {

	/**
	 * Log
	 */
	private static final Logger log = LoggerFactory.getLogger(CacheArquivoVerticle.class);

	public static final String CANAL_CACHE_ARQUIVO = "file.cache.channel";

	public static final String MAPA_CACHE_ARQUIVO = "file.cache.map";

	private static CacheArquivoImpl cacheArquivo;

	private static final int TEMPO_ATUALIZACAO_EM_MILISSEGUNDO = 30 * 1000; 

	public void atualizacaoAgendamento(final CacheArquivoImpl cache, final long refreshIntervalMillis) {

		log.debug("Iniciando atualização do cache...");

		cache.updateCache(new AsyncResult<Set<FileCacheEntry>>() {
			

			@Override
			public Set<FileCacheEntry> result() {
				return this.result();
			}

			@Override
			public Throwable cause() {
				return this.cause();
			}

			@Override
			public boolean succeeded() {
				return this.succeeded();
			}

			@Override
			public boolean failed() {
				return this.failed();
			}
		});
	}

	public void start() {

		final EventBus bus = vertx.eventBus();

		final CacheArquivoImpl FILE_CACHE = getInstanciaCacheArquivo(this.getVertx());

		log.debug("Registrando um ouvinte no barramento de enventos no canal [" + CANAL_CACHE_ARQUIVO + "] para lidar com a requisição.");
		bus.consumer(CANAL_CACHE_ARQUIVO, new TrataRequisicaoColocarArquivoEmCache(FILE_CACHE));

		atualizacaoAgendamento(FILE_CACHE, TEMPO_ATUALIZACAO_EM_MILISSEGUNDO);

	}

	public synchronized static CacheArquivoImpl getInstanciaCacheArquivo(Vertx vertx) {
		if (null == cacheArquivo) {
			cacheArquivo = new CacheArquivoImpl(vertx);
		}
		return cacheArquivo;
	}

}