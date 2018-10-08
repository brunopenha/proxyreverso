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

import br.nom.penha.bruno.proxy.reverso.autenticacao.MultipartUtil;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.SessionToken;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.UsuarioAplicacao;
import br.nom.penha.bruno.proxy.reverso.comum.TrataProxyReverso;
import br.nom.penha.bruno.proxy.reverso.configuracao.ConfiguracaoProxyReverso;

public class RetornoPapelHandler implements Handler<HttpClientResponse> {

	private static final Logger log = LoggerFactory.getLogger(RetornoPapelHandler.class);

	private final HttpServerRequest req;

	private final Vertx vertx;

	private final ConcurrentMap<String, byte[]> mapaCache;

	private final String dadosCarregados;

	private final SessionToken tokenSessao;

	private final boolean autenticacaoEnviada;

	private final String docNaoAssinado;

	private final String docAssinado;

	public RetornoPapelHandler(Vertx vertx, HttpServerRequest req, ConcurrentMap<String, byte[]> mapaCacheParam,
			String dadosCarregadosParam, SessionToken tokenSessaoParam, boolean autenticacaoEnviadaParam,
			String unsignedDocument, String docAssinadoParam) {
		this.req = req;
		this.vertx = vertx;
		this.mapaCache = mapaCacheParam;
		this.dadosCarregados = dadosCarregadosParam;
		this.tokenSessao = tokenSessaoParam;
		this.autenticacaoEnviada = autenticacaoEnviadaParam;
		this.docNaoAssinado = unsignedDocument;
		this.docAssinado = docAssinadoParam;
	}

	@Override
	public void handle(final HttpClientResponse res) {

		final ConfiguracaoProxyReverso config = TrataProxyReverso.getConfig(ConfiguracaoProxyReverso.class,
				mapaCache.get(ProxyReversoVerticle.configAfterDeployment()));
		final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").create();

		res.bodyHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer dados) {

				if (res.statusCode() >= 200 && res.statusCode() < 300) {
					log.debug("Papeis obtidos com sucesso. Obtendo o manifesto da ACL");
					HttpClient manifestoCliente = vertx.createHttpClient()
							.setHost(config.dependenciasServico.getHost("acl"))
							.setPort(config.dependenciasServico.getPorta("acl"));
					final HttpClientRequest papelSolicitado = manifestoCliente.request("POST",
							config.dependenciasServico.getCaminhosRequisicao("acl", "manifest"),
							new ManifestResponseHandler(vertx, req, mapaCache, dadosCarregados, tokenSessao,
									autenticacaoEnviada));

					String reqManifesto = MultipartUtil.constroiRequisicaoACL("",
							gson.fromJson(dados.toString(), UsuarioAplicacao.class).getPapeis());
					String reqMultiPart = MultipartUtil.constroiManifestoRequisicao("BaB03x", docNaoAssinado,
							docAssinado, reqManifesto);

					papelSolicitado.setChunked(true);
					papelSolicitado.write(reqMultiPart);
					papelSolicitado.end();

					log.debug("Enviado a requisição do manifesto");
				} else {
					log.debug("Falha em obter o papel.");

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
