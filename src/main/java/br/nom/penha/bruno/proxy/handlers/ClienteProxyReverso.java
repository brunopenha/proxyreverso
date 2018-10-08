package br.nom.penha.bruno.proxy.handlers;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.logging.Logger;

import br.nom.penha.bruno.proxy.reverso.comum.TrataProxyReverso;
import br.nom.penha.bruno.proxy.reverso.configuracao.AjustaRegra;
import br.nom.penha.bruno.proxy.reverso.configuracao.ConfiguracaoProxyReverso;

public class ClienteProxyReverso {


	ClienteProxyReverso() {
	}

	public void doProxy(final Vertx vertx, final HttpServerRequest req, final String messageBody, final ConfiguracaoProxyReverso config, final Logger log) {

		// req as uri
		URI reqURI = null;
		try {
			reqURI = new URI(req.uri());
		}
		catch (URISyntaxException e) {
			TrataProxyReverso.sendFailure(log, req, 500, "Bad URI: " + req.uri());
			return;
		}


		java.util.Map<String, String> localAssets = new HashMap<>();
		localAssets.put("/favicon.ico", "image/jpg");
		localAssets.put("/css/bootstrap.min.css", "text/css");
		for (String assetPath : localAssets.keySet()) {
			if (reqURI.getPath().equals(assetPath)) {
				String path = ProxyReversoVerticle.getInicioWeb() + assetPath;
				Buffer b = vertx.fileSystem().readFileSync(path);
				String contentType = localAssets.get(assetPath);
				TrataProxyReverso.send200OKResponse(log, req, b, contentType);
				return;
			}
		}

		String uriPath = reqURI.getPath().toString();
		String[] path = uriPath.split("/");
		if (path.length < 2) {
			TrataProxyReverso.sendFailure(log, req, 500, "Expected first node in URI path to be rewrite token.");
			return;
		}
		String rewriteToken = path[1];
		log.debug("Rewrite token --> " + rewriteToken);

		AjustaRegra r = config.ajustaPapeis.get(rewriteToken);
		if (r == null) {
			TrataProxyReverso.sendFailure(log, req, 500, "Couldn't find rewrite rule for '" + rewriteToken + "'");
			return;
		}

		String targetPath = uriPath.substring(rewriteToken.length() + 1);
		log.debug("Target path --> " + targetPath);

		String queryString = reqURI.getQuery();
		String spec = r.getProtocolo() + "://" + r.getHost() + ":" + r.getPorta() + targetPath;
		spec = queryString != null ? spec + "?" + queryString : spec;
		log.debug("Constructing target URL from --> " + spec);
		URL targetURL = null;
		try {
			targetURL = new URL(spec);
		}
		catch (MalformedURLException e) {
			TrataProxyReverso.sendFailure(log, req, 500, "Falhou em construir a URL " + spec);
			return;
		}

		log.info("Target URL --> " + targetURL.toString());


		final HttpClient client = vertx.createHttpClient();

		log.debug("Setting host --> " + targetURL.getHost());
		client.setHost(targetURL.getHost());

		log.debug("Setting port --> " + targetURL.getPort());
		client.setPort(targetURL.getPort());

		// TODO need to be tested
		if (r.getProtocolo().equalsIgnoreCase("https")) {
			log.debug("Creating HTTPS client");
			client.setSSL(true);
			client.setTrustStorePath(TrataProxyReverso.estahNuloOuVazioAposUmTrim(r.getCaminhoDoTrustStore()) ? ProxyReversoVerticle.getInicioRecursos()
					+ config.ssl.caminhoDoTrustStore : r.getCaminhoDoTrustStore());
			client.setTrustStorePassword(TrataProxyReverso.estahNuloOuVazioAposUmTrim(r.getSenhaDoTrustStore())
					? config.ssl.senhaDoTrustStore
					: r.getSenhaDoTrustStore());
		}

		final HttpClientRequest cReq = client.request(req.method(), targetURL.getPath().toString(), new Handler<HttpClientResponse>() {
			public void handle(HttpClientResponse cRes) {

				req.response().setStatusCode(cRes.statusCode());
//				req.response().headers().add(cRes.headers());
				req.response().setChunked(true);
				cRes.bodyHandler(new Handler<Buffer>() {
					public void handle(Buffer data) {
						req.response().write(data);
					}
				});
				cRes.endHandler(new Handler() {
					@Override
					public void handle(Object event) {
						req.response().end();
						
					}
				});
			}
		});

//		cReq.setheaders().set(req.headers());
		cReq.setChunked(true);

		if (null != messageBody) {
			cReq.write(messageBody);
		}

		cReq.end();
	}

}
