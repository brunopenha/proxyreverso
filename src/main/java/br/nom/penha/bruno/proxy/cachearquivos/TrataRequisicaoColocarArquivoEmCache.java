package br.nom.penha.bruno.proxy.cachearquivos;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class TrataRequisicaoColocarArquivoEmCache implements Handler<Message<String>> {

    /**
     * Log
     */
    private static final Logger log = LoggerFactory.getLogger(TrataRequisicaoColocarArquivoEmCache.class);

    private final CacheArquivoImpl fileCache;

    public TrataRequisicaoColocarArquivoEmCache(CacheArquivoImpl fileCache) {
        this.fileCache = fileCache;
    }

    @Override
    public void handle(final Message<String> message) {

        log.debug("FileCache received cache request on [" + message.address() + "] for [ " + message.body() + "]");

        final String path = message.body();

        if (fileCache.getInternalMap().get(path) != null) {
            FileCacheEntry e = fileCache.getInternalMap().get(path);
            message.reply(e.fileContents());
            return;
        }

        fileCache.putFile(path, CacheArquivoVerticle.CANAL_CACHE_ARQUIVO + path
                , new AsyncResult<FileCacheEntry>() {

			@Override
			public FileCacheEntry result() {
				message.reply(this.result().fileContents());
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
				log.error(this.cause());
                message.fail(-1, "Falha ao localizar o arquivo no caminho[" + path + "]");

				return this.failed();
			}
        });

    }


}
