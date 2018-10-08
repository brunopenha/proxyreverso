package br.nom.penha.bruno.proxy.handlers;


import java.util.concurrent.ConcurrentMap;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.SessionToken;
import br.nom.penha.bruno.proxy.reverso.comum.TrataProxyReverso;
import br.nom.penha.bruno.proxy.reverso.configuracao.ConfiguracaoProxyReverso;
import net.iharder.Base64;

public class ManifestResponseHandler implements Handler<HttpClientResponse> {

	private static final Logger log = LoggerFactory.getLogger(ManifestResponseHandler.class);
	private final HttpServerRequest req;
	private final Vertx vertx;
	private final ConcurrentMap<String, byte[]> mapaCache;
	private final String cargaDados;
	private final SessionToken tokenSessao;
	private final boolean autenticacaoPublicada;

	public ManifestResponseHandler(Vertx vertx, HttpServerRequest req, ConcurrentMap<String, byte[]> mapaCacheParam, String cargaDadosParam, SessionToken tokenSessaoParam,
			boolean autenticacaoPublicadaParam) {
		this.req = req;
		this.vertx = vertx;
		this.mapaCache = mapaCacheParam;
		this.cargaDados = cargaDadosParam;
		this.tokenSessao = tokenSessaoParam;
		this.autenticacaoPublicada = autenticacaoPublicadaParam;
	}

	@Override
	public void handle(final HttpClientResponse res) {

		final ConfiguracaoProxyReverso config = TrataProxyReverso.getConfig(ConfiguracaoProxyReverso.class,
				mapaCache.get(ProxyReversoVerticle.configAfterDeployment()));
		final SecretKey chaveSecreta = new SecretKeySpec(mapaCache.get(ProxyReversoVerticle.getInicioRecursos() + config.ssl.caminhoDoSymKey), "AES");

		res.bodyHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer data) {

				final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").create();

				if (res.statusCode() >= 200 && res.statusCode() < 300) {


					byte[] sessaoCriptografada = null;
					try {
						Cipher c = Cipher.getInstance("AES");
						c.init(Cipher.ENCRYPT_MODE, chaveSecreta);
						sessaoCriptografada = c.doFinal(gson.toJson(tokenSessao).getBytes("UTF-8"));
					}
					catch (Exception e) {
						TrataProxyReverso.sendFailure(log, req, 500, "failed to encrypt session token. " + e.getMessage());
						return;
					}
					req.response().headers().add("Set-Cookie", String.format("session-token=%s", Base64.encodeBytes(sessaoCriptografada).replace("\n", "")));

					if (autenticacaoPublicada) {
						TrataProxyReverso.sendRedirect(log, req, mapaCache, ProxyReversoVerticle.getInicioWeb() + "redirectConfirmation.html");
					}
					else {
						new ClienteProxyReverso().doProxy(vertx, req, cargaDados, config, log);
					}
				}
				else {
					TrataProxyReverso.sendFailure(log, req, res.statusCode(), data.toString());
				}
			}
		});

		res.endHandler(new VoidHandler() {

			@Override
			protected void handle() {
				// TODO exit gracefully if no data has been received
			}

		});
	}
}
