package br.nom.penha.bruno.proxy.cachearquivos;

import java.util.HashSet;
import java.util.Set;

import io.vertx.core.AsyncResult;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

class ResultadoAtualizacaoArquivoCache implements AsyncResult<Set<FileCacheEntry>> {

    /**
     * Log
     */
    private static final Logger LOG = LoggerFactory.getLogger(ResultadoAtualizacaoArquivoCache.class);


    private boolean tarefasDesignadasCompletadas = false;

    private Throwable cause = null;

    private boolean succeeded = false;

    private Set<FileCacheEntry> listaArquivosAtualizados = new HashSet<FileCacheEntry>();

    private Set<FileCacheEntry> listaArquivosComFalhas = new HashSet<FileCacheEntry>();

    private Set<String> arquivosPendentes = new HashSet<String>();

    public boolean foiFinalizado() {
        return arquivosPendentes.isEmpty() && hasCompletedTaskAssignments();
    }

    /**
     * Setters/Getters...
     */


    public boolean hasCompletedTaskAssignments() {
        return tarefasDesignadasCompletadas;
    }

    public void setHasCompletedTaskAssignments(boolean completedTaskAssignments) {
        this.tarefasDesignadasCompletadas = completedTaskAssignments;
    }

    public void addPendingFile(String file) {
        arquivosPendentes.add(file);
    }

    public void addUpdatedFile(FileCacheEntry file) {
        listaArquivosAtualizados.add(file);
    }

    public void addFailedFile(FileCacheEntry file) {
        listaArquivosComFalhas.add(file);
    }

    public Set<FileCacheEntry> getFailedFiles() {
        return listaArquivosComFalhas;
    }

    public void removePendingFile(String file) {
        arquivosPendentes.remove(file);
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    @Override
    public boolean succeeded() {
        return succeeded;
    }

    @Override
    public boolean failed() {
        return !succeeded;
    }


    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public Throwable cause() {
        return cause;
    }


    @Override
    public Set<FileCacheEntry> result() {
        return listaArquivosAtualizados;
    }

}

