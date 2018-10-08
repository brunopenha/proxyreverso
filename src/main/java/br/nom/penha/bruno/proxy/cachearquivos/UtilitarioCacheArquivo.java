package br.nom.penha.bruno.proxy.cachearquivos;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.logging.Logger;

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