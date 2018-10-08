package br.nom.penha.bruno.proxy.handlers;

import java.util.concurrent.ConcurrentMap;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.SessionToken;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.UsuarioAplicacao;
import br.nom.penha.bruno.proxy.reverso.comum.ConstantesProxyReverso;
import br.nom.penha.bruno.proxy.reverso.comum.TrataProxyReverso;
import br.nom.penha.bruno.proxy.reverso.configuracao.ConfiguracaoProxyReverso;

public class RetornaAssinaturaHandler implements Handler<HttpClientResponse> {

	/**
	 * Log
	 */
	private static final Logger log = LoggerFactory.getLogger(RetornaAssinaturaHandler.class);

	private final HttpServerRequest req;

	private final Vertx vertx;

	private final ConcurrentMap<String, byte[]> mapaCache;

	private final String cargaDados;

	private final SessionToken tokenSessao;

	private final boolean autenticacaoPublicada;

	private final String docNaoAssinada;

	private final String sidReferencia;

	public RetornaAssinaturaHandler(Vertx vertx, HttpServerRequest req, ConcurrentMap<String, byte[]> mapaCacheParma, String cargaDadosParam, SessionToken tokenSessaoParam,
			boolean autenticacaoPublicadaParam, String docNaoAssinadaParam, String sidReferenciaParam) {
		this.vertx = vertx;
		this.req = req;
		this.mapaCache = mapaCacheParma;
		this.cargaDados = cargaDadosParam;
		this.tokenSessao = tokenSessaoParam;
		this.autenticacaoPublicada = autenticacaoPublicadaParam;
		this.docNaoAssinada = docNaoAssinadaParam;
		this.sidReferencia = sidReferenciaParam;
	}

	@Override
	public void handle(final HttpClientResponse res) {

		log.debug("Retorno recebido com sucesso");

		final ConfiguracaoProxyReverso config = TrataProxyReverso.getConfig(ConfiguracaoProxyReverso.class,
				mapaCache.get(ProxyReversoVerticle.configAfterDeployment()));
		final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").create();

		res.bodyHandler(new Handler<Buffer>() {

			@Override
			public void handle(Buffer dados) {

				if (res.statusCode() >= 200 && res.statusCode() < 300) {
					log.debug("Dados obtidos com sucesso");

					HttpClient signClient = vertx.createHttpClient()
							.setHost(config.dependenciasServico.getHost("roles"))
							.setPort(config.dependenciasServico.getPorta("roles"));
					final HttpClientRequest reqPapeis = signClient.request("POST",
							config.dependenciasServico.getCaminhosRequisicao("roles", "roles"),
							new RetornoPapelHandler(vertx, req, mapaCache, cargaDados, tokenSessao, autenticacaoPublicada, docNaoAssinada, dados.toString()));

					String sid = TrataProxyReverso.parseTokenDeUmaQueryString(req.absoluteURI(), ConstantesProxyReverso.SID);
					UsuarioAplicacao appUser = new UsuarioAplicacao(tokenSessao.getUsername(), !TrataProxyReverso.estahNuloOuVazioAposUmTrim(sid) ? sid : sidReferencia);

					reqPapeis.setChunked(true);
					reqPapeis.write(gson.toJson(appUser));
					reqPapeis.end();
				}
				else {
					log.debug("Falha ao assinar os dados");

					TrataProxyReverso.sendFailure(log, req, res.statusCode(), dados.toString());
					return;
				}
			}
		});

		res.endHandler(new VoidHandler() {

			@Override
			protected void handle() {
			}

		});
	}
}
