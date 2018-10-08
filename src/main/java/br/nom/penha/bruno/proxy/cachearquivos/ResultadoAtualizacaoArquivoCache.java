package br.nom.penha.bruno.proxy.cachearquivos;

import java.util.HashSet;
import java.util.Set;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

class ResultadoAtualizacaoArquivoCache implements AsyncResult<Set<ArquivosCacheInseridos>> {

    /**
     * Log
     */
    private static final Logger LOG = LoggerFactory.getLogger(ResultadoAtualizacaoArquivoCache.class);


    private boolean tarefasDesignadasCompletadas = false;

    private Throwable causa = null;

    private boolean inserido = false;

    private Set<ArquivosCacheInseridos> listaArquivosAtualizados = new HashSet<ArquivosCacheInseridos>();

    private Set<ArquivosCacheInseridos> listaArquivosComFalhas = new HashSet<ArquivosCacheInseridos>();

    private Set<String> arquivosPendentes = new HashSet<String>();

    public boolean foiFinalizado() {
        return arquivosPendentes.isEmpty() && completouTarefasAssumidas();
    }

    /**
     * Setters/Getters...
     */


    public boolean completouTarefasAssumidas() {
        return tarefasDesignadasCompletadas;
    }

    public void setCompletouTarefasAssumidas(boolean completouTarefasAssumidas) {
        this.tarefasDesignadasCompletadas = completouTarefasAssumidas;
    }

    public void adicionaArquivosPendentes(String arquivo) {
        arquivosPendentes.add(arquivo);
    }

    public void adicionaArquivoAtualizado(ArquivosCacheInseridos arquivo) {
        listaArquivosAtualizados.add(arquivo);
    }

    public void adicionaArquivoComFalha(ArquivosCacheInseridos arquivo) {
        listaArquivosComFalhas.add(arquivo);
    }

    public Set<ArquivosCacheInseridos> getArquivosComFalha() {
        return listaArquivosComFalhas;
    }

    public void removeArquivosPendentes(String arquivos) {
        arquivosPendentes.remove(arquivos);
    }

    public void setInserido(boolean succeeded) {
        this.inserido = succeeded;
    }

    @Override
    public boolean succeeded() {
        return inserido;
    }

    @Override
    public boolean failed() {
        return !inserido;
    }


    public void setCausa(Throwable cause) {
        this.causa = cause;
    }

    @Override
    public Throwable cause() {
        return causa;
    }


    @Override
    public Set<ArquivosCacheInseridos> result() {
        return listaArquivosAtualizados;
    }

}

