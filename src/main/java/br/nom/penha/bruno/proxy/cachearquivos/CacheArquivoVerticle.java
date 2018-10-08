package br.nom.penha.bruno.proxy.cachearquivos;

import java.util.Set;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.Verticle;

/**
 */
public class CacheArquivoVerticle extends Verticle {

	/**
	 * Log
	 */
	private static final Logger log = LoggerFactory.getLogger(CacheArquivoVerticle.class);

	public static final String CANAL_CACHE_ARQUIVO = "file.cache.channel";

	public static final String MAPA_CACHE_ARQUIVO = "file.cache.map";

	private static CacheArquivoImpl cacheArquivo;

	private static final int TEMPO_ATUALIZACAO_EM_MILISSEGUNDO = 30 * 1000; 

	public void atualizacaoAgendamento(final CacheArquivoImpl cache, final long intervaloAtualizacaoEmMilisegundos) {

		log.debug("Iniciando atualização do cache...");
		
		cache.updateCache(new AsyncResultHandler<Set<ArquivosCacheInseridos>>() {
			@Override
			public void handle(AsyncResult<Set<ArquivosCacheInseridos>> evento) {

				for (ArquivosCacheInseridos arquivo : evento.result()) {
					vertx.eventBus().publish(arquivo.getCanalNotificacoesEventosNoBarramento(), true);
				}

				log.debug("Agendando a proxima atualização em " + TEMPO_ATUALIZACAO_EM_MILISSEGUNDO + " milisegundos");

				vertx.setTimer(intervaloAtualizacaoEmMilisegundos, new Handler<Long>() {
					@Override
					public void handle(Long event) {
						atualizacaoAgendamento(cache, intervaloAtualizacaoEmMilisegundos);
					}
				});
			}
		});

		
	}

	public void start() {

		final EventBus bus = vertx.eventBus();

		final CacheArquivoImpl CACHE_ARQUIVO = getInstanciaCacheArquivo(this.getVertx());

		log.debug("Registrando um ouvinte no barramento de enventos no canal [" + CANAL_CACHE_ARQUIVO + "] para lidar com a requisição.");
		bus.registerHandler(CANAL_CACHE_ARQUIVO, new TrataRequisicaoColocarArquivoEmCache(CACHE_ARQUIVO));

		atualizacaoAgendamento(CACHE_ARQUIVO, TEMPO_ATUALIZACAO_EM_MILISSEGUNDO);

	}

	public synchronized static CacheArquivoImpl getInstanciaCacheArquivo(Vertx vertx) {
		if (null == cacheArquivo) {
			cacheArquivo = new CacheArquivoImpl(vertx);
		}
		return cacheArquivo;
	}

}