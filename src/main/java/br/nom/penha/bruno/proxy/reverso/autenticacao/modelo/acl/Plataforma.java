package br.nom.penha.bruno.proxy.reverso.autenticacao.modelo.acl;

import java.util.ArrayList;
import java.util.List;

public class Plataforma {

	private String name;
	private List<Dominio> domains;

	public Plataforma() {
		this.domains = new ArrayList<Dominio>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Dominio> getDomains() {
		return domains;
	}

	public void setDomains(List<Dominio> domains) {
		this.domains = domains;
	}
}
