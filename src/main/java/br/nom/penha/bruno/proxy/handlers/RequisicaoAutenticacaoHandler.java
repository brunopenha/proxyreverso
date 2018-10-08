package br.nom.penha.bruno.proxy.handlers;

import static br.nom.penha.bruno.proxy.reverso.comum.TrataProxyReverso.estahNuloOuVazioAposUmTrim;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentMap;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.nom.penha.bruno.proxy.cachearquivos.CacheArquivoVerticle;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.AuthRequest;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.RequisicaoAutenticacao;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.SessionToken;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.Usuario;
import br.nom.penha.bruno.proxy.reverso.comum.ConstantesProxyReverso;
import br.nom.penha.bruno.proxy.reverso.comum.TrataProxyReverso;
import br.nom.penha.bruno.proxy.reverso.configuracao.ConfiguracaoProxyReverso;

public class RequisicaoAutenticacaoHandler implements Handler<HttpServerRequest> {

	/**
	 * Log
	 */
	private static final Logger log = LoggerFactory.getLogger(RequisicaoAutenticacaoHandler.class);

	/**
	 * Vert.x
	 */
	private final Vertx vertx;

	public RequisicaoAutenticacaoHandler(Vertx vertx) {
		this.vertx = vertx;
	}

	@Override
	public void handle(final HttpServerRequest req) {

		final ConcurrentMap<String, byte[]> mapaCaches = vertx.sharedData().getMap(CacheArquivoVerticle.MAPA_CACHE_ARQUIVO);
		final ConfiguracaoProxyReverso config = TrataProxyReverso.getConfig(ConfiguracaoProxyReverso.class,
				mapaCaches.get(ProxyReversoVerticle.configAfterDeployment()));

		final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").create();

		if (req.method().equalsIgnoreCase("POST")) {
			log.info("Recebendo um POST para autorização...");

			final Usuario usuario = new Usuario();
			req.bodyHandler(new Handler<Buffer>() {

				@Override
				public void handle(Buffer dados) {
					Usuario usuarioAserAutenticado = gson.fromJson(dados.toString(), Usuario.class);
					if (usuarioAserAutenticado != null) {
						if (!estahNuloOuVazioAposUmTrim(usuarioAserAutenticado.getId()) && !estahNuloOuVazioAposUmTrim(usuarioAserAutenticado.getSenha())) {
							usuario.setId(usuarioAserAutenticado.getId());
							usuario.setSenha(usuarioAserAutenticado.getSenha());

							AuthRequest reqAutenticacao = new AuthRequest("NAME_PASSWORD", usuario.getId(), usuario.getSenha());
							RequisicaoAutenticacao request = new RequisicaoAutenticacao();
							request.getAutenticacao().getAuthRequestList().add(reqAutenticacao);
							String authRequestStr = gson.toJson(request);
							SessionToken sessionToken = new SessionToken(usuario.getId(), null, null);

							HttpClient client = vertx.createHttpClient()
									.setHost(config.dependenciasServico.getHost("auth"))
									.setPort(config.dependenciasServico.getPorta("auth"));
							final HttpClientRequest cReq = client.request("POST",
									config.dependenciasServico.getCaminhosRequisicao("auth", "auth"),
									new ResponseAutenticacaoHandler(vertx, req, mapaCaches, "", sessionToken, true, null));

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

			String sessionTokenCookie = TrataProxyReverso.getCookieValue(req.headers(), ConstantesProxyReverso.COOKIE_SESSION_TOKEN);
			if (sessionTokenCookie != null) {
				log.info("session-token cookie found. removing existing cookie");
				DateFormat df = new SimpleDateFormat("EEE, MMM dd yyyy hh:mm:ss zzz");
				// set to GMT
				df.setTimeZone(TimeZone.getTimeZone(""));
				req.response().headers().add("Set-Cookie", String.format("session-token=; expires=%s", df.format(new Date(0))));
			}

			TrataProxyReverso.sendRedirect(log, req, mapaCaches, ProxyReversoVerticle.getInicioWeb() + "auth/login.html");
		}
	}
}