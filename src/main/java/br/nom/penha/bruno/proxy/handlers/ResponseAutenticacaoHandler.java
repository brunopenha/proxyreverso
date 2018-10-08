package br.nom.penha.bruno.proxy.handlers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentMap;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.nom.penha.bruno.proxy.reverso.autenticacao.MultipartUtil;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.AuthenticationResponse;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.SessionToken;
import br.nom.penha.bruno.proxy.reverso.comum.ConstantesProxyReverso;
import br.nom.penha.bruno.proxy.reverso.comum.TrataProxyReverso;
import br.nom.penha.bruno.proxy.reverso.configuracao.ConfiguracaoProxyReverso;
import net.iharder.Base64;

public class ResponseAutenticacaoHandler implements Handler<HttpClientResponse> {

	/**
	 * Log
	 */
	private static final Logger log = LoggerFactory.getLogger(ResponseAutenticacaoHandler.class);

	private final HttpServerRequest req;

	private final Vertx vertx;

	private final ConcurrentMap<String, byte[]> sharedCacheMap;

	private final String payload;

	private final SessionToken sessionToken;

	private final boolean authPosted;

	private final String refererSid;

	public ResponseAutenticacaoHandler(Vertx vertx, HttpServerRequest req, ConcurrentMap<String, byte[]> sharedCacheMap2, String payload, SessionToken sessionToken,
			boolean authPosted, String refererSid) {
		this.vertx = vertx;
		this.req = req;
		this.sharedCacheMap = sharedCacheMap2;
		this.payload = payload;
		this.sessionToken = sessionToken;
		this.authPosted = authPosted;
		this.refererSid = refererSid;
	}

	@Override
	public void handle(final HttpClientResponse res) {

		final ConfiguracaoProxyReverso config = TrataProxyReverso.getConfig(ConfiguracaoProxyReverso.class,
				sharedCacheMap.get(ProxyReversoVerticle.configAfterDeployment()));

		res.bodyHandler(new Handler<Buffer>() {
			public void handle(Buffer data) {

				Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").create();

				if (res.statusCode() >= 200 && res.statusCode() < 300) {
					final AuthenticationResponse response = gson.fromJson(data.toString(), AuthenticationResponse.class);

					if (response != null && response.getResponse() != null) {
						if ("success".equals(response.getResponse().getAuthentication())) {
							log.debug("authentication successful.");

							sessionToken.setAuthToken(response.getResponse().getAuthenticationToken());
							sessionToken.setSessionDate(response.getResponse().getSessionDate());

							if (payload.length() > config.getTamMaxCargaEmNumerosBytes()) {
								TrataProxyReverso.sendFailure(log,
										req,
										413,
										String.format("Request entity too large. Maximum payload size %s", config.tamMaxCargaEmBytes));
								return;
							}

							String uriPath;
							if (authPosted) {
								String originalRequest = TrataProxyReverso.getCookieValue(req.headers(), ConstantesProxyReverso.COOKIE_ORIGINAL_HEADER);
								String uri = null;
								try {
									uri = new String(Base64.decode(originalRequest));
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								try {
									uriPath = new URI(uri).getPath();
								}
								catch (URISyntaxException e) {
									TrataProxyReverso.sendFailure(log, req, 500, "Bad URI: " + req.uri());
									return;
								}
							}
							else {
								uriPath = req.absoluteURI().getPath();
							}
							String[] path = uriPath.split("/");

							if (path.length < 2) {
								log.error("Expected path to contain slash '/' but does not:  " + uriPath);
							}

							if (!path[1].equals(config.servicoComum) && !path[1].equals("auth")) {
								String sid = TrataProxyReverso.parseTokenDeUmaQueryString(req.absoluteURI(), ConstantesProxyReverso.SID);
								if (TrataProxyReverso.estahNuloOuVazioAposUmTrim(sid)) {
									if (TrataProxyReverso.estahNuloOuVazioAposUmTrim(refererSid)) {
										log.error("SID is required for request to non-default service");
										TrataProxyReverso.sendFailure(log, req, 400, "SID is required for request to non-default service");
										return;
									}
								}
							}
							String unsignedDocument = MultipartUtil.constroiRequisicaoEntrada("AaB03x",
									response.getResponse().getAuthenticationToken(),
									new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").format(response.getResponse().getSessionDate()),
									payload);

							HttpClient signClient = vertx.createHttpClient()
									.setHost(config.dependenciasServico.getHost("auth"))
									.setPort(config.dependenciasServico.getPorta("auth"));
							final HttpClientRequest signRequest = signClient.request("POST",
									config.dependenciasServico.getCaminhosRequisicao("auth", "sign"),
									new RetornaAssinaturaHandler(vertx, req, sharedCacheMap, payload, sessionToken, authPosted, unsignedDocument, refererSid));

							signRequest.setChunked(true);
							signRequest.write(unsignedDocument);
							signRequest.end();


						}
						else {

							if (!TrataProxyReverso.estahNuloOuVazioAposUmTrim(response.getResponse().getMessage())) {
								TrataProxyReverso.sendFailure(log, req, 401, response.getResponse().getMessage());
								return;
							}
							else {
								TrataProxyReverso.sendFailure(log, req, 401, data.toString());
								return;
							}
						}
					}
					else {

						TrataProxyReverso.sendFailure(log, req, 500, "Received OK status, but did not receive any response message");
						return;
					}
				}
				else {
					TrataProxyReverso.sendFailure(log, req, 500, data.toString());
					return;
				}
			}
		});
		/*res.endHandler(new VoidHandler() {
			public void handle() {
				// TODO exit gracefully if no body has been received				
			}
		});*/
	}
}
