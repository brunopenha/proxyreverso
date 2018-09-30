package br.nom.penha.bruno.proxy.reverso.comum;

import io.vertx.core.AsyncResult;

/**
 */
public class AsyncResultImpl<T> implements AsyncResult<T> {

    private final boolean succeeded;
    private final boolean failed;
    private final T result;
    private final Throwable cause;

    public AsyncResultImpl(boolean succeeded) {
        this(succeeded, null, null);
    }

    public AsyncResultImpl(boolean succeeded, T result, Throwable cause) {
        this.succeeded = succeeded;
        this.failed = !succeeded;
        this.result = result;
        this.cause = cause;
    }

    @Override
    public T result() {
        return result;
    }

    @Override
    public Throwable cause() {
        return cause;
    }

    @Override
    public boolean succeeded() {
        return succeeded;
    }

    @Override
    public boolean failed() {
        return failed;
    }

}