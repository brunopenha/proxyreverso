package br.nom.penha.bruno.proxy.reverso.comum;

import org.vertx.java.core.AsyncResult;

/**
 */
public class AsyncResultImpl<T> implements AsyncResult<T> {

    private final boolean sucesso;
    private final boolean falha;
    private final T resultado;
    private final Throwable causa;

    public AsyncResultImpl(boolean conseguiuAcesso) {
        this(conseguiuAcesso, null, null);
    }

    public AsyncResultImpl(boolean conseguiu, T resultado, Throwable motivo) {
        this.sucesso = conseguiu;
        this.falha = !conseguiu;
        this.resultado = resultado;
        this.causa = motivo;
    }

    @Override
    public T result() {
        return resultado;
    }

    @Override
    public Throwable cause() {
        return causa;
    }

    @Override
    public boolean succeeded() {
        return sucesso;
    }

    @Override
    public boolean failed() {
        return falha;
    }

}