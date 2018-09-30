package br.nom.penha.bruno.proxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class ProxyInicialTest {

	private Vertx vertx;

	  @Before
	  public void configuracao(TestContext contexto) {
	    vertx = Vertx.vertx();
	    vertx.deployVerticle(ProxyInicial.class.getName(),
	        contexto.asyncAssertSuccess());
	  }

	  @After
	  public void terminando(TestContext contexto) {
	    vertx.close(contexto.asyncAssertSuccess());
	  }

	  @Test
	  public void testMyApplication(TestContext contexto) {
	    final Async assincrono = contexto.async();

	    vertx.createHttpClient().getNow(8080, "localhost", "/",
	     retorno -> {
	      retorno.handler(body -> {
	        contexto.assertTrue(body.toString().contains("Olá"));
	        assincrono.complete();
	      });
	    });
	  }

}
