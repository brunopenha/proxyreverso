package br.nom.penha.bruno.proxy.cachearquivos;

import org.vertx.java.core.file.FileProps;

/**
 * Entradas para arquivos de Cache
 *
 */
public interface ArquivosCacheInseridos {

    /**
     * Canal
     * <p/>
     * Se não for nulo, transmite atualizações nesse canal
     * If non-null, broadcast updates on this channel.
     */
    String getCanalNotificacoesEventosNoBarramento();

    FileProps propriedadesArquivo();

    long ultimaModificacao();

    byte[] conteudoArquivo();

}
