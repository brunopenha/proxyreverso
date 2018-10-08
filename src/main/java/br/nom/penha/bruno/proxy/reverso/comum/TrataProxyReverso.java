package br.nom.penha.bruno.proxy.reverso.comum;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.logging.Logger;

import com.google.gson.Gson;

import net.iharder.Base64;

/**
 * Trata alguns casos do proxy reverso
 * <p/>
 *
 */
public class TrataProxyReverso {

	private static final String DELEMITADOR_COMUM_PARA_COOKIES = ";";
	public static final String CAMINHO_MODELO_ERRO_AUTENTICACAO = "auth/authError.html";

	/**
	 * Envia uma falha com uma mensagem e encerra a conexao.
	 *
	 * @param req - Requisicao HTTP 
	 * @param msg - Mensagem retorada para o cliente
	 */
	public static void sendFailure(Logger log, final HttpServerRequest req, final String msg) {
		sendFailure(log, req, 500, msg);
	}

	public static void sendFailure(Logger log, final HttpServerRequest req, final int statusCode, final String msg) {
		log.error(msg);
		req.response().setChunked(true);
		req.response().setStatusCode(statusCode);
		req.response().setStatusMessage("Internal Server Error.");
		req.response().write(msg);
		req.response().end();
	}

	public static void send200OKResponse(Logger log, final HttpServerRequest req, final Buffer response, final String contentType) {
		req.response().putHeader("content-type", contentType);
		req.response().setChunked(true);
		req.response().setStatusCode(200);
		req.response().setStatusMessage("OK");
		req.response().write(response);
		req.response().end();
	}

	public static void sendRedirect(Logger log, final HttpServerRequest req, ConcurrentMap<String, byte[]> sharedCacheMap, String path) {
		try {
			// Conserva a uri da requisicao original
			req.response()
					.headers()
					.add("Set-Cookie", String.format("original-request=%s", Base64.encodeBytes(req.absoluteURI().toString().getBytes("UTF-8"))));
			req.response().setChunked(true);
			req.response().setStatusCode(200);
			req.response().write(new String(sharedCacheMap.get(path)));
			req.response().end();
		}
		catch (Exception e) {
			TrataProxyReverso.sendFailure(log, req, 500, e.getMessage());
			return;
		}
	}

	public static <T> T getConfig(final Class<T> clazz, final byte[] fileContents) {
		String fileAsString = new String(fileContents);

		Gson g = new Gson();
		T c = g.fromJson(fileAsString, clazz);
		return c;
	}

	public static String getHeadersAsJSON(MultiMap headers, String filter) {
		java.util.Map<String, String> matched = new HashMap<String, String>();
		for (String key : headers.names()) {
			if (key.equals(filter)) {
				matched.put(key, headers.get(key));
			}
		}
		return new Gson().toJson(matched);
	}

	public static String getHeadersAsJSON(MultiMap headers) {
		return new Gson().toJson(headers);
	}

	public static String getCookieHeadersAsJSON(MultiMap headers) {
		return getHeadersAsJSON(headers, "Cookie");
	}

	public static String getCookieValue(MultiMap headers, String name) {

		for (String key : headers.names()) {
			if (key.equals("Cookie")) {
				String headerValue = headers.get(key);
				System.out.println("Cookie --> " + headerValue);
				for (String cookie : headerValue.split(DELEMITADOR_COMUM_PARA_COOKIES)) {
					if (cookie.trim().startsWith(name)) {
						String cookieValue = cookie.trim().replace(name + "=", "");
						return cookieValue;
					}
				}
			}
		}
		return null;
	}

	public static String parseTokenDeUmaQueryString(URI uri, String key) {
		List<NameValuePair> nameValuePairs = URLEncodedUtils.parse(uri, "utf-8");
		for (NameValuePair nvp : nameValuePairs) {
			if (nvp.getName().equals(key)) {
				return nvp.getValue();
			}
		}
		return null;
	}

	public static boolean estahNuloOuVazioAposUmTrim(String str) {
		if (str != null) {
			if (!str.trim().isEmpty()) {
				return false;
			}
		}

		return true;
	}
}