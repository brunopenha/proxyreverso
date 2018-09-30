package br.nom.penha.bruno.proxy.reverso.autenticacao;

import java.util.List;

import com.google.gson.Gson;

import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.Dominio;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.Plataforma;
import br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl.RequisicaoRaiz;

public class MultipartUtil {

	private MultipartUtil() {

	}

	public static String constroiRequisicaoEntrada(String boundary, String tokenAutenticacao, String dataSessao, String dadosTrafegados) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Content-Type: multipart/form-data; boundary=%s\n\n--%s\n", boundary, boundary));
		sb.append(String.format("Content-Disposition: form-data; name=\"AUTHENTICATION_TOKEN\"\n\n%s\n--%s\n", tokenAutenticacao, boundary));
		sb.append(String.format("Content-Disposition: form-data; name=\"UM_SESSION_DATE\"\n\n%s\n--%s\n", dataSessao, boundary));
		sb.append(String.format("Content-Disposition; form-data; name=\"ORIGINAL_PAYLOAD\"\n\n%s\n--%s--", dadosTrafegados, boundary));

		return sb.toString();
	}

	public static String constroiManifestoRequisicao(String boundary, String unsignedDocument, String signedDocument, String aclManifest) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Content-Type: multipart/form-data; boundary=%s\n\n--%s\n", boundary, boundary));
		sb.append(String.format("%s\n--%s\n", unsignedDocument, boundary));
		sb.append(String.format("Content-Type: text/plain\n\n%s\n--%s\n", signedDocument, boundary));
		sb.append(String.format("Content-Type: text/plain\n\n%s\n--%s--", aclManifest, boundary));

		return sb.toString();
	}

	public static String constroiRequisicaoACL(String nomeDominio, List<String> papeis) {

		RequisicaoRaiz rootRequest = new RequisicaoRaiz();

		Plataforma platform = new Plataforma();

		// a plataforma será sempre um 'Service-Broker' para SP ?
		platform.setName("Service-Broker");

		Dominio domain = new Dominio();
		domain.setTipo("Service");
		domain.setNome(nomeDominio);
		domain.setPapeis(papeis);

		platform.getDomains().add(domain);
		rootRequest.getRequisicaoACL().getPlataformas().add(platform);

		return new Gson().toJson(rootRequest);
	}

	public static String getBoundary(String request) {
		String[] lines = request.split("\n");

		if (lines.length > 0) {
			// a primeira linha da requisição deve conter as informaçãos de boundary
			String[] headerInfo = lines[0].split(";");
			if (headerInfo.length > 1) {
				String boundary = lines[0].split(";")[1];
				return boundary.replace("boundary=", "").trim();
			}
		}

		System.out.println("Não foi encontrado Boundary");
		return null;
	}

	public static String[] parseRequisicaoMultipart(String requisicao, String boundaryStr) {
		String boundary = "--" + boundaryStr;
		return requisicao.split(boundary);
	}


}
