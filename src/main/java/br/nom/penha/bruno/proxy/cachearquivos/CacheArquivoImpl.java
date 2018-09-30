package br.nom.penha.bruno.proxy.cachearquivos;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileProps;
import io.vertx.core.file.FileSystem;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;

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

	private final ConcurrentMap<String, FileCacheEntry> mapaCache;

	private final LocalMap<String, byte[]> mapaCacheLocal;

	private FileSystem fs;

	public CacheArquivoImpl(Vertx vertx) {
		super();
		this.fs = vertx.fileSystem();
		this.mapaCacheLocal = vertx.sharedData().getLocalMap(CacheArquivoVerticle.MAPA_CACHE_ARQUIVO);
		this.mapaCache = new ConcurrentHashMap<>();
	}

	protected java.util.Map<String, FileCacheEntry> getInternalMap() {
		return mapaCache;
	}

	public void putFileSynch(final String path, final String updateNotificationChannel) {
		LOG.debug("Requisição de atualização de arquivo feita para [" + path + "]");

		final FileProps fileProps = fs.propsBlocking(path);
		final byte[] bytes = fs.readFileBlocking(path).getBytes();

		FileCacheEntry entry = new FileCacheEntry() {

			@Override
			public String getEventBusNotificationChannel() {
				return updateNotificationChannel;
			}

			@Override
			public long lastModified() {
				return fileProps.lastModifiedTime();
			}

			@Override
			public FileProps fileProps() {
				return fileProps;
			}

			@Override
			public byte[] fileContents() {
				return bytes;
			}
		};

		boolean isNew = !mapaCache.containsKey("path");

		String confirmMsg = isNew ? "Adding" : "Updating";
		confirmMsg += " file [" + path + "], modified [" + entry.lastModified() + "] to cache.";
		LOG.debug(confirmMsg);

		mapaCache.put(path, entry);

		mapaCacheLocal.put(path, entry.fileContents());
	}

	public void putFile(final String path, final String updateNotificationChannel, final AsyncResult<FileCacheEntry> asyncResultHandler) {

		LOG.debug("Requisição de atualização de arquivo feita para [" + path + "]");

		fs.props(path, result -> {

				if (result.succeeded()) {

					final FileProps fileProps = result.result();

					fs.readFile(path,  arq -> {


						if (arq.succeeded()) {
							final byte[] bytes = arq.result().getBytes();

							FileCacheEntry entry = new FileCacheEntry() {

								@Override
								public String getEventBusNotificationChannel() {
									return updateNotificationChannel;
								}

								@Override
								public long lastModified() {
									return fileProps.lastModifiedTime();
								}

								@Override
								public FileProps fileProps() {
									return fileProps;
								}

								@Override
								public byte[] fileContents() {
									return bytes;
								}
							};

							boolean isNew = !mapaCache.containsKey("path");

							String confirmMsg = isNew ? "Adding" : "Updating";
							confirmMsg += " file [" + path + "], modified [" + entry.lastModified() + "] to cache.";
							LOG.debug(confirmMsg);

							mapaCache.put(path, entry);

							mapaCacheLocal.put(path, entry.fileContents());

							asyncResultHandler.result();//handle(new AsyncResultImpl(true, entry, null));
							result.succeeded();
							return;

						}
						else {
							LOG.error("Failure loading file for cache [" + path + "]", result.cause());
							asyncResultHandler.failed();
							result.failed();
						}
					
					
					});
				}
				else {
					LOG.error("Failure locating file for cache [" + path + "]", result.cause());
					asyncResultHandler.result();
					result.cause();
				}


		
		});
	}

	public void updateCache(final AsyncResult<Set<FileCacheEntry>> updateHandler) {

		final ResultadoAtualizacaoArquivoCache fileCacheUpdateResult = new ResultadoAtualizacaoArquivoCache();

		if (mapaCache.keySet().isEmpty()) {
			fileCacheUpdateResult.setHasCompletedTaskAssignments(true);
			fileCacheUpdateResult.setSucceeded(true);
			updateHandler.result();//handle(fileCacheUpdateResult);
			return;
		}

		for (final String chave : mapaCache.keySet()) {

			final FileCacheEntry dado = mapaCache.get(chave);

			fileCacheUpdateResult.addPendingFile(chave);

			LOG.debug("Verificando [" + chave + "] se tem atualizações...");

			FileCacheEntry currentEntry = mapaCache.get(chave);
			final String updateNotificationChannel = (currentEntry != null && currentEntry.getEventBusNotificationChannel() != null)
					? currentEntry.getEventBusNotificationChannel()
					: null;

			fs.props(chave, resultado -> {
					if (resultado.succeeded()) {
						final long ultimaModificacao = (resultado.result().lastModifiedTime());

						if (dado.lastModified() < ultimaModificacao) {
							LOG.info("Dado em cache [" + chave + "] foi modificado.  Atualizando cache... ");

							putFile(chave, updateNotificationChannel, new AsyncResult<FileCacheEntry>() {
								

								@Override
								public FileCacheEntry result() {
									fileCacheUpdateResult.addUpdatedFile(this.result());
									return this.result();
								}

								@Override
								public Throwable cause() {
									return this.cause();
								}

								@Override
								public boolean succeeded() {
									return this.succeeded();
								}

								@Override
								public boolean failed() {
									LOG.error("Failed to update cache for [" + chave + "]");
									fileCacheUpdateResult.addFailedFile(this.result());
									return this.failed();
								}
							});
						}
						else {
							marcaComoCompletadoEVerificaSeFoiFinalizado(fileCacheUpdateResult, updateHandler, chave);
						}
					}
					else {
						LOG.error("Uma exceção não esperada acontenceu no momento de verificar a chave  [" + chave + "]", resultado.cause());
						marcaComoCompletadoEVerificaSeFoiFinalizado(fileCacheUpdateResult, updateHandler, chave);
					}
				});
			
		}

		fileCacheUpdateResult.setHasCompletedTaskAssignments(true);

	}

	public void marcaComoCompletadoEVerificaSeFoiFinalizado(ResultadoAtualizacaoArquivoCache result, AsyncResult<Set<FileCacheEntry>> updateHandler, String absolutePath) {
		result.removePendingFile(absolutePath);
		if (result.foiFinalizado()) {
			result.setSucceeded(true);
			updateHandler.result();
		}
	}

}
