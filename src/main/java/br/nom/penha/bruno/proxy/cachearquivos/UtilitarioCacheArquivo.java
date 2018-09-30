package br.nom.penha.bruno.proxy.cachearquivos;

import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;

/**
 */
public class UtilitarioCacheArquivo {

	/**
	 */
	public static void readFile(final EventBus barramento, final Logger log, final String caminho, final AsyncResult<byte[]> tratador) {
		log.debug("Enviando um evento ao barramento de mensagens pelo canal [" + CacheArquivoVerticle.CANAL_CACHE_ARQUIVO + "] requisitando [" + caminho + "] do CacheArquivo.");

		barramento.send(CacheArquivoVerticle.CANAL_CACHE_ARQUIVO, caminho);
	}

}