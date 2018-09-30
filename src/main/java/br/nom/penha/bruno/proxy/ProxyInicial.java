package br.nom.penha.bruno.proxy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class ProxyInicial extends AbstractVerticle {

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		vertx 
			.createHttpServer()
			.requestHandler(req -> {
				req.response().end("<h1> Olá da minha primeira aplicação feita com Vertx </h1>");
			})
			.listen(8080, resultado -> {
				if(resultado.succeeded()) {
					System.out.println("Sucesso!");
					startFuture.complete();
				}else {
					System.out.println("Oh, oh... Temos um problema...");
					startFuture.fail(resultado.cause());
				}
			});
		
	}
}
