package br.nom.penha.bruno.proxy.cachearquivos;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.file.FileProps;
import org.vertx.java.core.file.FileSystem;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import br.nom.penha.bruno.proxy.reverso.comum.AsyncResultImpl;

/**
 * Cache do Arquivo
 * <p/>
 * Implementação do cache de Arquivo.
 *
 *
 */
public class CacheArquivoImpl {

	/**
	 * Log
	 */
	private static final Logger LOG = LoggerFactory.getLogger(CacheArquivoImpl.class);

	private final ConcurrentMap<String, ArquivosCacheInseridos> mapaCache;

	private final ConcurrentMap<String, byte[]> mapaCacheLocal;

	private FileSystem fs;

	public CacheArquivoImpl(Vertx vertx) {
		super();
		this.fs = vertx.fileSystem();
		this.mapaCacheLocal = vertx.sharedData().getMap(CacheArquivoVerticle.MAPA_CACHE_ARQUIVO);
		this.mapaCache = new ConcurrentHashMap<>();
	}

	protected java.util.Map<String, ArquivosCacheInseridos> getMapInterno() {
		return mapaCache;
	}

	public void colocaArquivoEmSincronia(final String caminho, final String atualizaNotificacoesCanal) {
		LOG.debug("Requisição de atualização de arquivo feita para [" + caminho + "]");

		final FileProps propriedadesArquivo = fs.propsSync(caminho);
		final byte[] bytes = fs.readFileSync(caminho).getBytes();

		ArquivosCacheInseridos inseridos = new ArquivosCacheInseridos() {

			@Override
			public String getCanalNotificacoesEventosNoBarramento() {
				return atualizaNotificacoesCanal;
			}

			@Override
			public long ultimaModificacao() {
				return propriedadesArquivo.lastModifiedTime().getTime();
			}

			@Override
			public FileProps propriedadesArquivo() {
				return propriedadesArquivo;
			}

			@Override
			public byte[] conteudoArquivo() {
				return bytes;
			}
		};

		boolean ehNovo = !mapaCache.containsKey("path");

		String msgConfirmacao = ehNovo ? "Adicionando" : "Atualizando";
		msgConfirmacao += " arquivo [" + caminho + "], modificado [" + inseridos.ultimaModificacao() + "] no cache.";
		LOG.debug(msgConfirmacao);

		mapaCache.put(caminho, inseridos);

		mapaCacheLocal.put(caminho, inseridos.conteudoArquivo());
	}

	public void insereArquivo(final String caminho, final String atulizacaCanalNotificacao, final AsyncResultHandler<ArquivosCacheInseridos> trataArquivosAsyncHandler) {

		LOG.debug("Requisição de atualização de arquivo feita para [" + caminho + "]");

		fs.props(caminho, new AsyncResultHandler<FileProps>() {
			@Override
			public void handle(final AsyncResult<FileProps> resultado) {
				if (resultado.succeeded()) {

					final FileProps fileProps = resultado.result();

					fs.readFile(caminho, new AsyncResultHandler<Buffer>() {
						public void handle(AsyncResult<Buffer> arq) {


						if (arq.succeeded()) {
							final byte[] bytes = arq.result().getBytes();

							ArquivosCacheInseridos arquivoInserido = new ArquivosCacheInseridos() {

								@Override
								public String getCanalNotificacoesEventosNoBarramento() {
									return atulizacaCanalNotificacao;
								}

								@Override
								public long ultimaModificacao() {
									return fileProps.lastModifiedTime().getTime();
								}

								@Override
								public FileProps propriedadesArquivo() {
									return fileProps;
								}

								@Override
								public byte[] conteudoArquivo() {
									return bytes;
								}
							};

							boolean ehNovo = !mapaCache.containsKey("path");

							String confirmMsg = ehNovo ? "Adicionando" : "Atualizando";
							confirmMsg += " arquivo [" + caminho + "], foi modificado em [" + arquivoInserido.ultimaModificacao() + "] no cache.";
							LOG.debug(confirmMsg);

							mapaCache.put(caminho, arquivoInserido);

							mapaCacheLocal.put(caminho, arquivoInserido.conteudoArquivo());

							trataArquivosAsyncHandler.handle(new AsyncResultImpl(true, arquivoInserido, null));
							resultado.succeeded();
							return;

						}
						else {
							LOG.error("Falha ao carregar o arquivo para o cache [" + caminho + "]", resultado.cause());
							trataArquivosAsyncHandler.handle(new AsyncResultImpl(false));
							resultado.failed();
						}
					
						}
					});
				}
				else {
					LOG.error("Falha em localizar o arquivo cache em [" + caminho + "]", resultado.cause());
					trataArquivosAsyncHandler.handle(new AsyncResultImpl(false));
					resultado.cause();
				}

			}

				

		
		});
	}

	public void updateCache(final AsyncResultHandler<Set<ArquivosCacheInseridos>> trataAtualizacao) {

		final ResultadoAtualizacaoArquivoCache resultadoAtualizacaoArquivo = new ResultadoAtualizacaoArquivoCache();

		if (mapaCache.keySet().isEmpty()) {
			resultadoAtualizacaoArquivo.setCompletouTarefasAssumidas(true);
			resultadoAtualizacaoArquivo.setInserido(true);
			trataAtualizacao.handle(resultadoAtualizacaoArquivo);
			return;
		}
		
		for (final String chave : mapaCache.keySet()) {

			final ArquivosCacheInseridos inserido = mapaCache.get(chave);

			resultadoAtualizacaoArquivo.adicionaArquivosPendentes(chave);

			LOG.debug("Verificando [" + chave + "] para atualizações...");

			ArquivosCacheInseridos arquivoAtual = mapaCache.get(chave);
			final String updateNotificationChannel = (arquivoAtual != null && arquivoAtual.getCanalNotificacoesEventosNoBarramento() != null)
					? arquivoAtual.getCanalNotificacoesEventosNoBarramento()
					: null;

			fs.props(chave, new AsyncResultHandler<FileProps>() {
				@Override
				public void handle(final AsyncResult<FileProps> resultado) {
					if (resultado.succeeded()) {
						final long ultimaModificacao = (resultado.result().lastModifiedTime().getTime());

						if (inserido.ultimaModificacao() < ultimaModificacao) {
							LOG.info("Cache incluido [" + chave + "] foi modificado.  Atualizando cache... ");

							insereArquivo(chave, updateNotificationChannel, new AsyncResultHandler<ArquivosCacheInseridos>() {
								@Override
								public void handle(AsyncResult<ArquivosCacheInseridos> evento) {
									if (evento.failed()) {
										LOG.error("Falha ao atualizar o cache para [" + chave + "]");
									}
									else {
										if (evento.succeeded()) {
											resultadoAtualizacaoArquivo.adicionaArquivoAtualizado(evento.result());
										}
										else {
											resultadoAtualizacaoArquivo.adicionaArquivoComFalha(evento.result());
										}
									}
									marcaComoCompletadoEVerificaSeFoiFinalizado(resultadoAtualizacaoArquivo, trataAtualizacao, chave);
								}
							});
						}
						else {
							marcaComoCompletadoEVerificaSeFoiFinalizado(resultadoAtualizacaoArquivo, trataAtualizacao, chave);
						}
					}
					else {
						LOG.error("Ocorreu uma exceção não esperada ao atualizar o arquivo  [" + chave + "]", resultado.cause());
						marcaComoCompletadoEVerificaSeFoiFinalizado(resultadoAtualizacaoArquivo, trataAtualizacao, chave);
					}
				}
			});
		}


		resultadoAtualizacaoArquivo.setCompletouTarefasAssumidas(true);

	}

	public void marcaComoCompletadoEVerificaSeFoiFinalizado(ResultadoAtualizacaoArquivoCache result, AsyncResultHandler<Set<ArquivosCacheInseridos>> updateHandler, String absolutePath) {
		result.removeArquivosPendentes(absolutePath);
		if (result.foiFinalizado()) {
			result.setInserido(true);
			updateHandler.handle(result);
		}
	}

}
