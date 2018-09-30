package br.nom.penha.bruno.proxy.handlers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.ApplicationUser;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.SessionToken;
import br.nom.penha.bruno.proxy.reverso.comum.ReverseProxyConstants;
import br.nom.penha.bruno.proxy.reverso.comum.TrataProxyReverso;
import br.nom.penha.bruno.proxy.reverso.configuracao.ConfiguracaoProxyReverso;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;

/**
 * @author hpark
 */
public class SignResponseHandler implements Handler<HttpClientResponse> {

	/**
	 * Log
	 */
	private static final Logger log = LoggerFactory.getLogger(SignResponseHandler.class);

	private final HttpServerRequest req;

	private final Vertx vertx;

	private final LocalMap<String, byte[]> sharedCacheMap;

	private final String payload;

	private final SessionToken sessionToken;

	private final boolean authPosted;

	private final String unsignedDocument;

	private final String refererSid;

	public SignResponseHandler(Vertx vertx, HttpServerRequest req, LocalMap<String, byte[]> sharedCacheMap, String payload, SessionToken sessionToken,
			boolean authPosted, String unsignedDocument, String refererSid) {
		this.vertx = vertx;
		this.req = req;
		this.sharedCacheMap = sharedCacheMap;
		this.payload = payload;
		this.sessionToken = sessionToken;
		this.authPosted = authPosted;
		this.unsignedDocument = unsignedDocument;
		this.refererSid = refererSid;
	}

	@Override
	public void handle(final HttpClientResponse res) {

		log.debug("Received response from auth server for sign request");

		final ConfiguracaoProxyReverso config = TrataProxyReverso.getConfig(ConfiguracaoProxyReverso.class,
				sharedCacheMap.get(ReverseProxyVerticle.configAfterDeployment()));
		final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").create();

		// payload signing successful
		res.bodyHandler(new Handler<Buffer>() {

			@Override
			public void handle(Buffer data) {

				if (res.statusCode() >= 200 && res.statusCode() < 300) {
					log.debug("Payload signing successful. Fetching role from engine");

					HttpClient signClient = vertx.createHttpClient();
//							.setHost(config.dependenciasServico.getHost("roles"))
//							.setPort(config.dependenciasServico.getPorta("roles"));
					final HttpClientRequest roleRequest = signClient.request(HttpMethod.POST,
							config.dependenciasServico.getCaminhosRequisicao("roles", "roles"),
							new RoleResponseHandler(vertx, req, sharedCacheMap, payload, sessionToken, authPosted, unsignedDocument, data.toString()));

					String sid = null;
					try {
						sid = TrataProxyReverso.parseTokenDeUmaQueryString(new URI(req.absoluteURI()), ReverseProxyConstants.SID);
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ApplicationUser appUser = new ApplicationUser(sessionToken.getUsername(), !TrataProxyReverso.estahNuloOuVazioAposUmTrim(sid) ? sid : refererSid);

					roleRequest.setChunked(true);
					roleRequest.write(gson.toJson(appUser));
					roleRequest.end();
				}
				// payload signing failed
				else {
					log.debug("Payload signing failed.");

					TrataProxyReverso.sendFailure(log, req, res.statusCode(), data.toString());
					return;
				}
			}
		});

/*		res.endHandler(new VoidHandler() {

			@Override
			protected void handle() {
				// TODO exit gracefully if no data has been received
			}

		});*/
	}
}
