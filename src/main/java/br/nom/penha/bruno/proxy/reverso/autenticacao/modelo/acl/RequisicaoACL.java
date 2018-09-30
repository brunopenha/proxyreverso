package br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl;

import java.util.ArrayList;
import java.util.List;

public class RequisicaoACL {

	private List<Plataforma> plataformas;

	public RequisicaoACL() {
		this.plataformas = new ArrayList<Plataforma>();
	}

	public List<Plataforma> getPlataformas() {
		return plataformas;
	}

	public void setPlataformas(List<Plataforma> platforms) {
		this.plataformas = platforms;
	}
}
