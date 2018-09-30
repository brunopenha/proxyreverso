package br.nom.penha.bruno.proxy.handlers;

import java.util.concurrent.ConcurrentMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.nom.penha.bruno.proxy.reverso.autenticacao.MultipartUtil;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.ApplicationUser;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.SessionToken;
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
public class RoleResponseHandler implements Handler<HttpClientResponse> {

	private static final Logger log = LoggerFactory.getLogger(RoleResponseHandler.class);

	private final HttpServerRequest req;

	private final Vertx vertx;

	private final LocalMap<String, byte[]> sharedCacheMap;

	private final String payload;

	private final SessionToken sessionToken;

	private final boolean authPosted;

	private final String unsignedDocument;

	private final String signedDocument;

	public RoleResponseHandler(Vertx vertx, HttpServerRequest req, LocalMap<String, byte[]> sharedCacheMap, String payload, SessionToken sessionToken,
			boolean authPosted, String unsignedDocument, String signedDocument) {
		this.req = req;
		this.vertx = vertx;
		this.sharedCacheMap = sharedCacheMap;
		this.payload = payload;
		this.sessionToken = sessionToken;
		this.authPosted = authPosted;
		this.unsignedDocument = unsignedDocument;
		this.signedDocument = signedDocument;
	}

	@Override
	public void handle(final HttpClientResponse res) {

		final ConfiguracaoProxyReverso config = TrataProxyReverso.getConfig(ConfiguracaoProxyReverso.class,
				sharedCacheMap.get(ReverseProxyVerticle.configAfterDeployment()));
		final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").create();

		res.bodyHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer data) {

				// role fetch successful
				if (res.statusCode() >= 200 && res.statusCode() < 300) {
					log.debug("Successfully fetched role. Getting manifest from ACL");
					HttpClient manifestClient = vertx.createHttpClient();
//							.setHost(config.dependenciasServico.getHost("acl"))
//							.setPorta(config.dependenciasServico.getPorta("acl"));
					final HttpClientRequest roleRequest = manifestClient.request(HttpMethod.POST,
							config.dependenciasServico.getCaminhosRequisicao("acl", "manifest"),
							new ManifestResponseHandler(vertx, req, sharedCacheMap, payload, sessionToken, authPosted));

					String manifestRequest = MultipartUtil.constroiRequisicaoACL("", gson.fromJson(data.toString(), ApplicationUser.class).getRoles());
					String multipartManifestRequest = MultipartUtil.constroiManifestoRequisicao("BaB03x", unsignedDocument, signedDocument, manifestRequest);

					roleRequest.setChunked(true);
					roleRequest.write(multipartManifestRequest);
					roleRequest.end();

					log.debug("Sent get manifest request from acl");
				}
				else {
					log.debug("Failed to fetch role.");

					TrataProxyReverso.sendFailure(log, req, res.statusCode(), data.toString());
					return;
				}
			}
		});

		/*res.endHandler(new VoidHandler() {

			@Override
			protected void handle() {
				// TODO exit gracefully if no data has been received
			}

		});
*/	}
}
