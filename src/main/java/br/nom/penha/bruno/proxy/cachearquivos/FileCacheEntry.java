package br.nom.penha.bruno.proxy.cachearquivos;

import io.vertx.core.file.FileProps;

/**
 * Entradas para arquivos de Cache
 *
 */
public interface FileCacheEntry {

    /**
     * Canal
     * <p/>
     * Se não for nulo, transmite atualizações nesse canal
     * If non-null, broadcast updates on this channel.
     */
    String getEventBusNotificationChannel();

    FileProps fileProps();

    long lastModified();

    byte[] fileContents();

}
