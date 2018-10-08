package br.nom.penha.bruno.proxy.cachearquivos;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

public class TrataRequisicaoColocarArquivoEmCache implements Handler<Message<String>> {

    /**
     * Log
     */
    private static final Logger log = LoggerFactory.getLogger(TrataRequisicaoColocarArquivoEmCache.class);

    private final CacheArquivoImpl arquivoCache;

    public TrataRequisicaoColocarArquivoEmCache(CacheArquivoImpl arquivoCacheParam) {
        this.arquivoCache = arquivoCacheParam;
    }

    @Override
    public void handle(final Message<String> mensagem) {

        log.debug("Requisicao de cache em  [" + mensagem.address() + "] para [ " + mensagem.body() + "]");

        final String caminho = mensagem.body();

        if (arquivoCache.getMapInterno().get(caminho) != null) {
            ArquivosCacheInseridos e = arquivoCache.getMapInterno().get(caminho);
            mensagem.reply(e.conteudoArquivo());
            return;
        }

        arquivoCache.insereArquivo(caminho, CacheArquivoVerticle.CANAL_CACHE_ARQUIVO + caminho
                , new AsyncResultHandler<ArquivosCacheInseridos>() {
            @Override
            public void handle(AsyncResult<ArquivosCacheInseridos> evento) {
                if (evento.succeeded()) {
                    mensagem.reply(evento.result().conteudoArquivo());
                } else {
                    log.error(evento.cause());
                    mensagem.fail(-1, "Falha ao localizar o arquivo em [" + caminho + "]");

                }
            }
        });

        
        

    }


}
