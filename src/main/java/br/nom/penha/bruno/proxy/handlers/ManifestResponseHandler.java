package br.nom.penha.bruno.proxy.handlers;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.SessionToken;
import br.nom.penha.bruno.proxy.reverso.comum.TrataProxyReverso;
import br.nom.penha.bruno.proxy.reverso.configuracao.ConfiguracaoProxyReverso;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import net.iharder.Base64;

public class ManifestResponseHandler implements Handler<HttpClientResponse> {

	private static final Logger log = LoggerFactory.getLogger(ManifestResponseHandler.class);
	private final HttpServerRequest req;
	private final Vertx vertx;
	private final LocalMap<String, byte[]> sharedCacheMap;
	private final String payload;
	private final SessionToken sessionToken;
	private final boolean authPosted;

	public ManifestResponseHandler(Vertx vertx, HttpServerRequest req, LocalMap<String, byte[]> sharedCacheMap, String payload, SessionToken sessionToken,
			boolean authPosted) {
		this.req = req;
		this.vertx = vertx;
		this.sharedCacheMap = sharedCacheMap;
		this.payload = payload;
		this.sessionToken = sessionToken;
		this.authPosted = authPosted;
	}

	@Override
	public void handle(final HttpClientResponse res) {

		final ConfiguracaoProxyReverso config = TrataProxyReverso.getConfig(ConfiguracaoProxyReverso.class,
				sharedCacheMap.get(ReverseProxyVerticle.configAfterDeployment()));
		final SecretKey key = new SecretKeySpec(sharedCacheMap.get(ReverseProxyVerticle.getResourceRoot() + config.ssl.caminhoDoSymKey), "AES");

		res.bodyHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer data) {

				final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").create();

				if (res.statusCode() >= 200 && res.statusCode() < 300) {


					byte[] encryptedSession = null;
					try {
						Cipher c = Cipher.getInstance("AES");
						c.init(Cipher.ENCRYPT_MODE, key);
						encryptedSession = c.doFinal(gson.toJson(sessionToken).getBytes("UTF-8"));
					}
					catch (Exception e) {
						TrataProxyReverso.sendFailure(log, req, 500, "failed to encrypt session token. " + e.getMessage());
						return;
					}
					req.response().headers().add("Set-Cookie", String.format("session-token=%s", Base64.encodeBytes(encryptedSession).replace("\n", "")));

					if (authPosted) {
//						TrataProxyReverso.sendRedirect(log, req, sharedCacheMap, ReverseProxyVerticle.getWebRoot() + "redirectConfirmation.html");
					}
					else {
						// do reverse proxy
						new ReverseProxyClient().doProxy(vertx, req, payload, config, log);
					}
				}
				else {
					TrataProxyReverso.sendFailure(log, req, res.statusCode(), data.toString());
				}
			}
		});

/*		res.endHandler(new VoidHandler() {

			@Override
			protected void handle() {
				// TODO exit gracefully if no data has been received
			}

		});
*/	}
}
