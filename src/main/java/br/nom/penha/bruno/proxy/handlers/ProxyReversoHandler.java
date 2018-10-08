package br.nom.penha.bruno.proxy.handlers;

import java.net.URI;
import java.util.concurrent.ConcurrentMap;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.impl.Base64;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.nom.penha.bruno.proxy.cachearquivos.CacheArquivoVerticle;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.AuthRequest;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.RequisicaoAutenticacao;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.SessionToken;
import br.nom.penha.bruno.proxy.reverso.comum.ConstantesProxyReverso;
import br.nom.penha.bruno.proxy.reverso.comum.TrataProxyReverso;
import br.nom.penha.bruno.proxy.reverso.configuracao.ConfiguracaoProxyReverso;


public class ProxyReversoHandler implements Handler<HttpServerRequest> {

	/**
	 * Log
	 */
	private static final Logger log = LoggerFactory.getLogger(ProxyReversoHandler.class);

	/**
	 * Vert.x
	 */
	private final Vertx vertx;
	private final boolean precisoAutenticaoEdeACL;

	public ProxyReversoHandler(Vertx vertx, boolean precisoAutenticaoEdeACL) {
		this.vertx = vertx;
		this.precisoAutenticaoEdeACL = precisoAutenticaoEdeACL;
	}

	@Override
	public void handle(final HttpServerRequest req) {

		final ConcurrentMap<String, byte[]> mapaCache = vertx.sharedData().getMap(CacheArquivoVerticle.MAPA_CACHE_ARQUIVO);
		final ConfiguracaoProxyReverso config = TrataProxyReverso.getConfig(ConfiguracaoProxyReverso.class,
				mapaCache.get(ProxyReversoVerticle.configAfterDeployment()));
		final SecretKey chaveSecreta = new SecretKeySpec(mapaCache.get(ProxyReversoVerticle.getInicioRecursos() + config.ssl.caminhoDoSymKey), "AES");

		String testoSePrecisoAutenticacao = precisoAutenticaoEdeACL ? "[Sessão obrigatória]" : "[Sessão opcional]";
		log.info("Tratando chamada ao uma requisicao ao proxy [" + req.method() + " " + req.uri() + " " + testoSePrecisoAutenticacao);
		log.debug("Cabeçalho:  " + TrataProxyReverso.getCookieHeadersAsJSON(req.headers()));

		if (config.ajustaPapeis == null) {
			log.error("Nenhum ajuste foi encontrado");
			TrataProxyReverso.sendFailure(log, req, 500, "Nenhum ajuste de regra foi encontrado");
			return;
		}

		String caminhoURI = req.absoluteURI().getPath().toString();
		String[] caminho = caminhoURI.split("/");
		if (caminho.length < 2) {
			log.info("Redirecionando para um serviço comum..");
			req.response().setStatusCode(302);
			req.response().headers().add("Location", config.servicoComum);
			req.response().end();
		}
		else {
			final Buffer bufferDosDados = new Buffer();
			req.bodyHandler(new Handler<Buffer>() {

				@Override
				public void handle(Buffer buffer) {
					bufferDosDados.appendBuffer(buffer);
				}

			});
			req.endHandler(new VoidHandler() {

				@Override
				protected void handle() {

					String refererSid = null;
					if (TrataProxyReverso.estahNuloOuVazioAposUmTrim(TrataProxyReverso.parseTokenDeUmaQueryString(req.absoluteURI(), ConstantesProxyReverso.SID))) {
						try {
							URI refererURI = new URI(req.headers().get(ConstantesProxyReverso.HEADER_REFERER));
							refererSid = TrataProxyReverso.parseTokenDeUmaQueryString(refererURI, ConstantesProxyReverso.SID);
						}
						catch (Exception e) {
						}
					}



					if (!precisoAutenticaoEdeACL) {
						if (!TrataProxyReverso.estahNuloOuVazioAposUmTrim(req.headers().get(ConstantesProxyReverso.HEADER_REFERER))
								|| ConstantesProxyReverso.ACCEPTED_X_REQUESTED_WITH_VALUE.equals(req.headers()
										.get(ConstantesProxyReverso.HEADER_X_REQUESTED_WITH))) {
							new ClienteProxyReverso().doProxy(vertx, req, null, config, log);
							return;
						}
					}


					Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").create();

					SessionToken tokenSessao = null;
					String stringRequisicao = "";
					String stringToken = TrataProxyReverso.getCookieValue(req.headers(), ConstantesProxyReverso.COOKIE_SESSION_TOKEN);

					if (stringToken != null && !stringToken.isEmpty()) {
						log.debug(String.format("Session token found. Authenticating using authentication token."));
						byte[] sessaoDecriptografada = null;
						try {
							Cipher c = Cipher.getInstance("AES");
							c.init(Cipher.DECRYPT_MODE, chaveSecreta);
							sessaoDecriptografada = c.doFinal(Base64.decode(stringToken));

							tokenSessao = gson.fromJson(new String(sessaoDecriptografada), SessionToken.class);

							AuthRequest requisicaoAutenticacao = new AuthRequest("NAME_PASSWORD", "", "");
							RequisicaoAutenticacao req = new RequisicaoAutenticacao();
							req.setTokenAutenticacao(tokenSessao.getAuthToken());
							req.getAutenticacao().getAuthRequestList().add(requisicaoAutenticacao);
							stringRequisicao = gson.toJson(req);
						}
						catch (Exception e) {
							log.error(e.getMessage());
							TrataProxyReverso.sendFailure(log, req, 500, "Não foi possivel decriptografar a sessao: " + e.getMessage());
							return;
						}

						log.debug("Enviando a solicitação da requisicao ao servidor.");
						HttpClient authClient = vertx.createHttpClient()
								.setHost(config.dependenciasServico.getHost("auth"))
								.setPort(config.dependenciasServico.getPorta("auth"));
						final HttpClientRequest authReq = authClient.request("POST",
								config.dependenciasServico.getCaminhosRequisicao("auth", "auth"),
								new ResponseAutenticacaoHandler(vertx, req, mapaCache, bufferDosDados.toString(), tokenSessao, false, refererSid));

						authReq.setChunked(true);
						authReq.write(stringRequisicao);
						authReq.end();
					}
					else {
						log.info("O token de sessão e o cabeçalho de requisição não foram encontrados. Redirecionando para a tela de login");

						TrataProxyReverso.sendRedirect(log, req, mapaCache, ProxyReversoVerticle.getInicioWeb() + "auth/login.html");
						return;
					}
				}
			});
		}
	}
}