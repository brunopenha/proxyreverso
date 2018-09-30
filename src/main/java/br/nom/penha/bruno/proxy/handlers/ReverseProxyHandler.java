package br.nom.penha.bruno.proxy.handlers;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import br.nom.penha.bruno.proxy.cachearquivos.CacheArquivoVerticle;
import br.nom.penha.bruno.proxy.reverso.comum.TrataProxyReverso;
import br.nom.penha.bruno.proxy.reverso.configuracao.ConfiguracaoProxyReverso;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;


public class ReverseProxyHandler implements Handler<HttpServerRequest> {

	/**
	 * Log
	 */
	private static final Logger log = LoggerFactory.getLogger(ReverseProxyHandler.class);

	/**
	 * Vert.x
	 */
	private final Vertx vertx;
	private final boolean requiresAuthAndACL;

	public ReverseProxyHandler(Vertx vertx, boolean requiresAuthAndACL) {
		this.vertx = vertx;
		this.requiresAuthAndACL = requiresAuthAndACL;
	}

	@Override
	public void handle(final HttpServerRequest req) {

		final LocalMap<String, byte[]> sharedCacheMap = vertx.sharedData().getLocalMap(CacheArquivoVerticle.MAPA_CACHE_ARQUIVO);
		final ConfiguracaoProxyReverso config = TrataProxyReverso.getConfig(ConfiguracaoProxyReverso.class,
				sharedCacheMap.get(ReverseProxyVerticle.configAfterDeployment()));
		final SecretKey key = new SecretKeySpec(sharedCacheMap.get(ReverseProxyVerticle.getResourceRoot() + config.ssl.caminhoDoSymKey), "AES");

		String sessionRequirementText = requiresAuthAndACL ? "[Session required]" : "[Session not required]";
		log.info("Handling incoming proxy request [" + req.method() + " " + req.uri() + " " + sessionRequirementText);
		log.debug("Headers:  " + TrataProxyReverso.getCookieHeadersAsJSON(req.headers()));

		if (config.ajustaPapeis == null) {
			log.error("Nenhum ajuste foi encontrado");
			TrataProxyReverso.sendFailure(log, req, 500, "Nenhum ajuste de regra foi encontrado");
			return;
		}

		String uriPath = req.absoluteURI();
		String[] path = uriPath.split("/");
		if (path.length < 2) {
			log.info("Redirecionando para um serviço comum..");
			req.response().setStatusCode(302);
			req.response().headers().add("Location", config.servicoComum);
			req.response().end();
		}
		else {
			final Buffer payloadBuffer = new BufferImpl();
			req.bodyHandler(new Handler<Buffer>() {

				@Override
				public void handle(Buffer buffer) {
					payloadBuffer.appendBuffer(buffer);
				}

			});
			/*req.endHandler(new VoidHandler() {

				@Override
				protected void handle() {

					// check for sid in request
					String refererSid = null;
					if (ReverseProxyUtil.isNullOrEmptyAfterTrim(ReverseProxyUtil.parseTokenFromQueryString(req.absoluteURI(), ReverseProxyConstants.SID))) {
						// if it does not exist, then try fetching sid from referer header and pass that down the filters
						try {
							URI refererURI = new URI(req.headers().get(ReverseProxyConstants.HEADER_REFERER));
							refererSid = ReverseProxyUtil.parseTokenFromQueryString(refererURI, ReverseProxyConstants.SID);
						}
						catch (Exception e) {
							// do nothing
						}
					}



					if (!requiresAuthAndACL) {
						// For assets, request header must contain either Referer and/or X-Requested-With
						// (Request with those headers are coming from browser, not from direct user input)
						if (!ReverseProxyUtil.isNullOrEmptyAfterTrim(req.headers().get(ReverseProxyConstants.HEADER_REFERER))
								|| ReverseProxyConstants.ACCEPTED_X_REQUESTED_WITH_VALUE.equals(req.headers()
										.get(ReverseProxyConstants.HEADER_X_REQUESTED_WITH))) {
							// do reverse proxy
							new ReverseProxyClient().doProxy(vertx, req, null, config, log);
							return;
						}
					}


					Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").create();

					SessionToken sessionToken = null;
					String authRequestStr = "";
					String sessionTokenStr = ReverseProxyUtil.getCookieValue(req.headers(), ReverseProxyConstants.COOKIE_SESSION_TOKEN);

					if (sessionTokenStr != null && !sessionTokenStr.isEmpty()) {
						log.debug(String.format("Session token found. Authenticating using authentication token."));
						byte[] decryptedSession = null;
						try {
							Cipher c = Cipher.getInstance("AES");
							c.init(Cipher.DECRYPT_MODE, key);
							decryptedSession = c.doFinal(Base64.decode(sessionTokenStr));

							sessionToken = gson.fromJson(new String(decryptedSession), SessionToken.class);

							AuthRequest authRequest = new AuthRequest("NAME_PASSWORD", "", "");
							AuthenticateRequest request = new AuthenticateRequest();
							request.setAuthenticationToken(sessionToken.getAuthToken());
							request.getAuthentication().getAuthRequestList().add(authRequest);
							authRequestStr = gson.toJson(request);
						}
						catch (Exception e) {
							log.error(e.getMessage());
							ReverseProxyUtil.sendFailure(log, req, 500, "Unable to decrypt session token: " + e.getMessage());
							return;
						}

						log.debug("Sending auth request to authentication server.");
						HttpClient authClient = vertx.createHttpClient()
								.setHost(config.serviceDependencies.getHost("auth"))
								.setPort(config.serviceDependencies.getPort("auth"));
						final HttpClientRequest authReq = authClient.request("POST",
								config.serviceDependencies.getRequestPath("auth", "auth"),
								new AuthResponseHandler(vertx, req, sharedCacheMap, payloadBuffer.toString(), sessionToken, false, refererSid));

						authReq.setChunked(true);
						authReq.write(authRequestStr);
						authReq.end();
					}
					else {
						log.info("session token and basic auth header not found. redirecting to login page");

						// return login page
						ReverseProxyUtil.sendRedirect(log, req, sharedCacheMap, ReverseProxyVerticle.getWebRoot() + "auth/login.html");
						return;
					}
				}
			});*/
		}
	}
}