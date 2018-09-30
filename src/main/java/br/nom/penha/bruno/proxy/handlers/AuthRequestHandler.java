package br.nom.penha.bruno.proxy.handlers;

import static br.nom.penha.bruno.proxy.reverso.comum.TrataProxyReverso.estahNuloOuVazioAposUmTrim;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.nom.penha.bruno.proxy.cachearquivos.CacheArquivoVerticle;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.AuthRequest;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.AuthenticateRequest;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.SessionToken;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.User;
import br.nom.penha.bruno.proxy.reverso.comum.ReverseProxyConstants;
import br.nom.penha.bruno.proxy.reverso.comum.TrataProxyReverso;
import br.nom.penha.bruno.proxy.reverso.configuracao.ConfiguracaoProxyReverso;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;

public class AuthRequestHandler implements Handler<HttpServerRequest> {

	/**
	 * Log
	 */
	private static final Logger log = LoggerFactory.getLogger(AuthRequestHandler.class);

	/**
	 * Vert.x
	 */
	private final Vertx vertx;

	public AuthRequestHandler(Vertx vertx) {
		this.vertx = vertx;
	}

	@Override
	public void handle(final HttpServerRequest req) {

		final LocalMap<String, byte[]> sharedCacheMap = vertx.sharedData().getLocalMap(CacheArquivoVerticle.MAPA_CACHE_ARQUIVO);
		final ConfiguracaoProxyReverso config = TrataProxyReverso.getConfig(ConfiguracaoProxyReverso.class,
				sharedCacheMap.get(ReverseProxyVerticle.configAfterDeployment()));

		final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").create();

		if (req.method().POST.equals("POST")) {
			log.info("Recebendo um POST para autorização...");

			final User user = new User();
			req.bodyHandler(new Handler<Buffer>() {

				@Override
				public void handle(Buffer data) {
					User authenticatingUser = gson.fromJson(data.toString(), User.class);
					if (authenticatingUser != null) {
						if (!estahNuloOuVazioAposUmTrim(authenticatingUser.getUserId()) && !estahNuloOuVazioAposUmTrim(authenticatingUser.getPassword())) {
							user.setUserId(authenticatingUser.getUserId());
							user.setPassword(authenticatingUser.getPassword());

							AuthRequest authRequest = new AuthRequest("NAME_PASSWORD", user.getUserId(), user.getPassword());
							AuthenticateRequest request = new AuthenticateRequest();
							request.getAuthentication().getAuthRequestList().add(authRequest);
							String authRequestStr = gson.toJson(request);
							SessionToken sessionToken = new SessionToken(user.getUserId(), null, null);

							HttpClient client = vertx.createHttpClient();
//									.setHost(config.dependenciasServico.getHost("auth"))
//									.setPort(config.dependenciasServico.getPorta("auth"));
							final HttpClientRequest cReq = client.request(HttpMethod.POST,
									config.dependenciasServico.getCaminhosRequisicao("auth", "auth"),
									new AuthResponseHandler(vertx, req, sharedCacheMap, "", sessionToken, true, null));

							cReq.setChunked(true);
							cReq.write(authRequestStr);
							cReq.end();
						}
					}
				}
			});
		}
		else {
			log.info("Recebendo um GET para autorização");

			String sessionTokenCookie = TrataProxyReverso.getCookieValue(req.headers(), ReverseProxyConstants.COOKIE_SESSION_TOKEN);
			if (sessionTokenCookie != null) {
				log.info("session-token cookie found. removing existing cookie");
				DateFormat df = new SimpleDateFormat("EEE, MMM dd yyyy hh:mm:ss zzz");
				// set to GMT
				df.setTimeZone(TimeZone.getTimeZone(""));
				req.response().headers().add("Set-Cookie", String.format("session-token=; expires=%s", df.format(new Date(0))));
			}

//			TrataProxyReverso.sendRedirect(log, req, sharedCacheMap, ReverseProxyVerticle.getWebRoot() + "auth/login.html");
		}
	}
}